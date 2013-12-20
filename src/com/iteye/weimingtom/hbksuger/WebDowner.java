package com.iteye.weimingtom.hbksuger;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

public class WebDowner {
	private final static boolean D = false;
	private final static String TAG = "HBKWebDowner";
	
	/**
	 * 
	 * ">([0-9]*)</td>.*\\r*\\n.*luckyNo=\"([0-9]*)\"",
	 * "<td bgcolor=\"#FFFFFF\" width=\"40\"><img src=\"../image/m_(.*)\\.gif\" width=\"30\" height=\"12\"></td>\\r*\\n"
	 * + "<td bgcolor=\"#FFFFFF\" width=\"80\">(.*)</td>\\r*\\n"
	 * + "<td bgcolor=\"#FFFFFF\" width=\"440\"><a class=\"noline\" href=\"(.*)\">(.*)</a></td>",
	 * 
	 * @see http://www.cppblog.com/biao/archive/2010/02/05/107298.html
　	 * 贪婪(*, ?, +)：读入整个串，从后往前匹配
　　	 * 勉强(*?, ??, +?)：从前往后匹配
　　	 * 侵占(*+, ?+, ++)：读入整个串，从前往后匹配，匹配的是整个串
	 */
	private final static String PAGES_REGEXP = 
			"<div class=\"hbkProgram\">.*onClick=\"AttachVideo\\('(.*?)','(.*?)','[0-9]','[0-9]'\\)" + 
			"\"><img border=\"0\" src=\"(.*?)\" class=\"hbkProgramBanner\" /><div class=\"(.*?)\">" + 
			"(.*\\r?\\n?.*?)</div></a></div>";
	//line 2, div : hbkProgramButton, hbkProgramButtonNew
	private final String PAGES_REGEXP2 = 
			"<div class=\"hbkProgram\">.*onClick=\"AttachVideo\\('(.*?)','(.*?)','[0-9]','[0-9]'\\)" + 
			"\"><img border=\"0\" src=\"(.*?)\" class=\"hbkProgramBanner\" /><div class=\"hbkProgramButton\">" +
			"(.*?)</div></a></div>";

	private final static String PAGES_REGEXP3 = 
			"<div class=\"hbkProgram\">.*onClick=\"AttachVideo\\('(.*?)','(.*?)','[0-9]','[0-9]'\\)" /*+ 
			"\"><img border=\"0\" src=\"(.*?)\" class=\"hbkProgramBanner\" /><div class=\"hbkProgramButton\">"*/;
	
	private final static String RSS_PAGES_REGEXP =
			"<li style=\".*?\"><strong>(.*?)</strong>.*\\r?\\n?.*?url=(.*?)\"><img";// + 

	private final static String LANTIS_PAGES_REGEXP =
			"<h3>.*?<a href=\"(.*?)\" target=\"_blank\">(.*?)</a>.*?</h3>\\r?\\n?.*?" + 
			"<a href=\".*?\" target=\".*?\">\\r?\\n?.*?" +
			"<img alt=\".*?\" src=\"(.*?)\" /></a>\\r?\\n?.*?" +
			"<div class=\"box_01\">(.*?)</div>\\r?\\n?.*?" + 
			"<div class=\"box_01\">.*?<a title=\".*?\" href=\"(.*?)\"><img src=\".*?\" alt=\".*?\" />.*?</a>\\r?\\n?.*?" +
			"<a title=\".*?\" href=\"(.*?)\"><img src=\".*?\" alt=\".*?\" />.*?</a>.*?</div>\\r?\\n?.*?" + 
			"<div class=\"box_02\">(.*?)</div>"
			;
	
	private final static String LANTIS_PAGES_REGEXP2 = 
			"<h3>.*?<a href=\"(.*?)\">(.*?)</a>.*?</h3>.*?\\r?\\n?.*?" +
			"<div class=\"box_01\">(.*?)</div>\\r?\\n?.*?" +
			"<div class=\"box_02\">(.*?)</div>"
			;
	
	//"<a target=\"_blank\" href=\"(.*?)\">RSS文件</a>";
	
	private final static String ANIMATE_PAGES_REGEXP =
			"<p class=\"title\"><a href=\"(.*?)\"( onclick=\".*?\")?>(.*?)</a></p>" 
			;
			//><img src=\"(.*?)\"
	
	private final static String ANIMATE_DETAIL_REGEXP = 
			"<a href=\"(.*?)#player\">"
			;
	
	private final static String TABS_REGEXP = 
			"<li><a id=.*href=\"(.*)\"></a></li>";
	private final static String TEST_PAGE = 
			"http://hibiki-radio.jp:80/get_program/12";
	private final static boolean SHOW_PAGE_DEBUG_INFO = false;
	private final static String TEST_DETAIL = 
			"http://hibiki-radio.jp/uploads/data/channel/r-2_live/925.xml";

	private final static String LANTIS_TABS_REGEXP = 
			"<li id=\"(.*)\"><a title=\"(.*)\" href=\"(.*)\">";
	
	private final static String ASX_REGEXP = 
			"<ref href.*?=.*?\"(.*?)\".*?/>";
	
	private final static String ASX_URL_REGEXP = 
			"http://www2\\.uliza\\.jp/IF/WMVDisplay\\.aspx\\?(.*?)\"";
	private final static String ASX_URL_HEADER =
			"http://www2.uliza.jp/IF/WMVDisplay.aspx?";
	
	private ArrayList<String> tabs;
	private ArrayList<LantisTabInfo> lantisTabs;
	private Hashtable<String, List<PageInfo>> pages;
	private Hashtable<String, List<LantisPageInfo>> lantisPages;
	private HttpGet req;
	
	public static final class LantisTabInfo {
		public String id;
		public String title;
		public String href;
		
		public LantisTabInfo(String _id, String _title, String _href) {
			this.id = _id;
			this.title = _title;
			this.href = _href;
		}
	}
	
	public static final class LantisPageInfo {
		public String titleHref;
		public String title;
		public String banner;
		public String comment;
		public String asx32k;
		public String asx64k;
		public String time;
		
		public LantisPageInfo(String _titleHref, String _title, String _banner, String _comment, String _asx32k, String _asx64k, String _time) {
			this.titleHref = _titleHref;
			this.title = _title;
			this.banner = _banner;
			this.comment = _comment;
			this.asx32k = _asx32k;
			this.asx64k = _asx64k;
			this.time = _time;
		}
	}
	
	public static final class AnimatePageInfo {
		public String titleHref;
		public String title;
		public String id;
		public String thumbImage;
		
		public AnimatePageInfo(String _titleHref, String _title) {
			final String beginStr = "http://www.animate.tv/radio/details.php?id=";
			
			this.titleHref = _titleHref;
			this.title = _title;
			if (this.titleHref != null && !this.titleHref.contains("http")) {
				this.titleHref = "http://www.animate.tv" + this.titleHref;
			}
			if (this.titleHref != null && this.titleHref.startsWith(beginStr)) {
				this.id = this.titleHref.substring(beginStr.length());
				this.thumbImage = "http://www.animate.tv/radio/visual/" + id + "/main_thumb.jpg";
			}
		}
	}
	
	public static final class DetailInfo {
		public String protocol;
		public String domain;
		public String dir;
		public String channel_type;
		public String flv;
		public String thumbnail;
		
		public String getRtmpUrl() {
			//FIXME:
			//return "rtmp://" + domain + "/" + dir + "/" + flv;
			String url = "rtmpe://" + domain + "/" + dir + "/" + flv;
			int index = url.indexOf('?');
			if (index >= 0) {
				url = url.substring(0, index);
			}
			url = url.replace("mp4:", "");
			return url;
		}
		
		public String getMP3Filename() {
			//FIXME:
			//return "/sdcard/hbksugar/" + dir + "_" + flv + "_download_" + System.currentTimeMillis() + ".mp3";
			String url = flv;
			int index = url.indexOf('?');
			if (index >= 0) {
				url = url.substring(0, index);
			}
			if (url != null) {
				url = url.replace('/', '_');
				url = url.replace("mp4:", "");
			} else {
				url = "unknown";
			}
			File sdcardDir = Environment.getExternalStorageDirectory();
			return sdcardDir.getPath() + "/hbksugar/" + url + "_download_" + System.currentTimeMillis() + ".flv";
		}
	}
	
	public static final class PageInfo {
		public String tab;
		public int startPos;
		public String video1;
		public String video2;
		public String imgSrc;
		public String btnName; //hbkProgramButton or hbkProgramButtonNew
		public String info;
		
		public PageInfo(String tab, int startPos, String video1, String video2, String imgSrc, String btnName, String info) {
			this.tab = tab;
			this.startPos = startPos;
			this.video1 = video1;
			this.video2 = video2;
			this.imgSrc = imgSrc;
			this.btnName = btnName;
			this.info = info;
			if (this.info != null) {
				this.info = this.info.replace("<br />\r", "\n");
				this.info = this.info.replace("<br />", "\n");
				this.info = this.info.replace("</div><div class=\"hbkProgramComment\">", "\n");
			}
		}
		
		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer();
			//sb.append("tab == " + tab + "\n");
			//sb.append("video1 == " + video1 + "\n");
			//sb.append("video2 == " + video2 + "\n");
			//sb.append("imgSrc == " + imgSrc + "\n");
			sb.append("btnName == " + btnName + "\n");
			sb.append("info == " + info + "\n");
			return sb.toString();
		}
		
		public String getUrl() {
			return "http://hibiki-radio.jp/uploads/data/channel/" + video1 + "/" + video2 + ".xml";
		}
		
		public String getDescUrl() {
			return "http://hibiki-radio.jp/description/" + video1;
		}
		
		public String getRssUrl() {
			return video1;
		}
	};
	
	public WebDowner() {
		tabs = new ArrayList<String>();
		lantisTabs = new ArrayList<LantisTabInfo>();
		pages = new Hashtable<String, List<PageInfo>>(); 
		lantisPages = new Hashtable<String, List<LantisPageInfo>>();
	}
	
	public void start() {
		try {
			if (false) {
				/**
				 * Count of </div></a></div>
				 */
				getPages(TEST_PAGE);
			} else if (false) {
				getTabs();
				for (String tab : tabs) {
					getPages(tab);
				}
			} else {
				getDetail(TEST_DETAIL);
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			
		}
	}

	public DetailInfo getDetail(String url) throws ClientProtocolException, IOException  {
		DetailInfo info = new DetailInfo();
		URL tabURL = new URL(url);
		String text = getWebText(
				tabURL.getHost(), 
				(tabURL.getPort() == -1 ? 80 : tabURL.getPort()), 
				"http",
				tabURL.getPath(), 
				"shift-jis");
		if (text != null) {
			Pattern pattern;
			Matcher matcher;
			pattern = Pattern.compile("<protocol>(.*?)</protocol>", Pattern.MULTILINE);
			matcher = pattern.matcher(text);
			while (matcher.find()) {
				String str = matcher.group(1);
				if (D) {
					Log.d(TAG, str);
				}
				info.protocol = str;
			}
			pattern = Pattern.compile("<domain>(.*?)</domain>", Pattern.MULTILINE);
			matcher = pattern.matcher(text);
			while (matcher.find()) {
				String str = matcher.group(1);
				if (D) {
					Log.d(TAG, str);
				}
				info.domain = str;
			}
			pattern = Pattern.compile("<dir>(.*?)</dir>", Pattern.MULTILINE);
			matcher = pattern.matcher(text);
			while (matcher.find()) {
				String str = matcher.group(1);
				if (D) {
					Log.d(TAG, str);
				}
				info.dir = str;
			}
			pattern = Pattern.compile("<channel type=\"(.*?)\">", Pattern.MULTILINE);
			matcher = pattern.matcher(text);
			while (matcher.find()) {
				String str = matcher.group(1);
				if (D) {
					Log.d(TAG, str);
				}
				info.channel_type = str;
			}
			pattern = Pattern.compile("<flv>(.*?)</flv>", Pattern.MULTILINE);
			matcher = pattern.matcher(text);
			while (matcher.find()) {
				String str = matcher.group(1);
				if (D) {
					Log.d(TAG, str);
				}
				info.flv = str;
			}
			pattern = Pattern.compile("<thumbnail>(.*?)</thumbnail>", Pattern.MULTILINE);
			matcher = pattern.matcher(text);
			while (matcher.find()) {
				String str = matcher.group(1);
				if (D) {
					Log.d(TAG, str);
				}
				info.thumbnail = str;
			}
		}
		return info;
	}
	
	/**
	 * 
	 * @param tab URL, for example, http://hibiki-radio.jp:80/get_program/1
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public List<PageInfo> getPages(String tab) throws ClientProtocolException, IOException {
		List<PageInfo> tabPages = new ArrayList<PageInfo>();
		pages.put(tab, tabPages);
		URL tabURL = new URL(tab);
		String text = getWebText(
				tabURL.getHost(), 
				(tabURL.getPort() == -1 ? 80 : tabURL.getPort()), 
				"http",
				tabURL.getPath(), 
				"shift-jis");
		text = text.replace("<div class=\"hbkProgram\">", "\n<div class=\"hbkProgram\">");
		text = text.replace("<div style=\"clear:both;\">", "\n");
		text = text.replace("</div></a></div>", "</div></a></div>\n");
		if (D) {
			//Log.d(TAG, text);
		}
		Pattern pattern = Pattern.compile(PAGES_REGEXP);
		Matcher matcher = pattern.matcher(text);
		while (matcher.find()) {
			PageInfo pageInfo = new PageInfo(
					tab,
					matcher.start(1),
					matcher.group(1),
					matcher.group(2),
					matcher.group(3),
					matcher.group(4),
					matcher.group(5)					
					);
			tabPages.add(pageInfo);
		}
		if (false) {
		Pattern pattern2 = Pattern.compile(PAGES_REGEXP2, Pattern.MULTILINE);
		Matcher matcher2 = pattern2.matcher(text);
		while (matcher2.find()) {
			PageInfo pageInfo = new PageInfo(
					tab,
					matcher2.start(1),
					matcher2.group(1),
					matcher2.group(2),
					matcher2.group(3),
					matcher2.group(4),
					matcher2.group(5)
					);
			tabPages.add(pageInfo);
		}
		Collections.sort(tabPages, new Comparator<PageInfo>() {
			@Override
			public int compare(PageInfo lhs, PageInfo rhs) {
				if (lhs == null) {
					return -1;
				}
				if (rhs == null) {
					return 1;
				}
				return lhs.startPos - rhs.startPos;
			}
		});
		}
		if (D) {
			if (SHOW_PAGE_DEBUG_INFO) {
				for (PageInfo pageInfo : tabPages) {
					if (D) {
						Log.d(TAG, pageInfo.toString());
					}
				}
			}
			Log.d(TAG, "tab == " + tab);
			Log.d(TAG, "page size == " + tabPages.size());
		}
		return tabPages;
	}
	
	public List<PageInfo> getRssPages(String tab) throws ClientProtocolException, IOException {
		List<PageInfo> tabPages = new ArrayList<PageInfo>();
		pages.put(tab, tabPages);
		URL tabURL = new URL(tab);
		String text = getWebText(
				tabURL.getHost(), 
				(tabURL.getPort() == -1 ? 80 : tabURL.getPort()), 
				"http",
				tabURL.getPath(), 
				"utf-8");
		Pattern pattern = Pattern.compile(RSS_PAGES_REGEXP);
		Matcher matcher = pattern.matcher(text);
		while (matcher.find()) {
			/*
			String url = matcher.group(1);
			String info = "";
			if (url != null) {
				info = url.replace("http://www.bilibili.tv/", "");
			}
			*/
			String info = matcher.group(1);
			String url = matcher.group(2);
			PageInfo pageInfo = new PageInfo(
					tab,
					matcher.start(1),
					url,
					null,
					null,
					null,
					info					
					);
			tabPages.add(pageInfo);
		}
		return tabPages;
	}
	
	public List<LantisPageInfo> getLantisPages(String tab) throws ClientProtocolException, IOException {
		List<LantisPageInfo> tabPages = new ArrayList<LantisPageInfo>();
		lantisPages.put(tab, tabPages);
		URL tabURL = new URL(tab);
		String text = getWebText(
				tabURL.getHost(), 
				(tabURL.getPort() == -1 ? 80 : tabURL.getPort()), 
				"http",
				tabURL.getPath(), 
				"utf-8");
		
		if (text != null) {
			//text = text.replace("<div class=\"box_01\"></div>", "<div class=\"box_01\"><p> </p></div>");
			text = text.replace("<br />\n", "<br />");
			text = text.replace("</p>\n\n", "</p>");
			text = text.replace("\n<br />", "<br />");
			text = text.replace("</p>\n", "</p>");
			text = text.replace("</a>\n<div class=\"box_01\"><a title=\"", 
				"</a>\n<div class=\"box_01\"></div>\n<div class=\"box_01\"><a title=\""
			);
		}
		
		if (tab.contains("announce")) {
			Pattern pattern = Pattern.compile(LANTIS_PAGES_REGEXP2);
			Matcher matcher = pattern.matcher(text);
			while (matcher.find()) {
				String titleHref = matcher.group(1);
				String title = matcher.group(2);
				String banner = "";
				String comment = matcher.group(3);
				String asx32k = "";
				String asx64k = "";
				String time = matcher.group(4);
				LantisPageInfo pageInfo = new LantisPageInfo(titleHref, title, banner, comment, asx32k, asx64k, time);
				tabPages.add(pageInfo);
			}
		} else {
			Pattern pattern = Pattern.compile(LANTIS_PAGES_REGEXP);
			Matcher matcher = pattern.matcher(text);
			while (matcher.find()) {
				String titleHref = matcher.group(1);
				String title = matcher.group(2);
				String banner = matcher.group(3);
				String comment = matcher.group(4);
				String asx32k = matcher.group(5);
				String asx64k = matcher.group(6);
				String time = matcher.group(7);
				LantisPageInfo pageInfo = new LantisPageInfo(titleHref, title, banner, comment, asx32k, asx64k, time);
				tabPages.add(pageInfo);
			}
		}
		return tabPages;
	}
	
	public List<AnimatePageInfo> getAnimatePages() throws ClientProtocolException, IOException {
		List<AnimatePageInfo> tabPages = new ArrayList<AnimatePageInfo>();
		URL tabURL = new URL("http://www.animate.tv/radio/?m=i");
		String text = getWebText(
				tabURL.getHost(), 
				(tabURL.getPort() == -1 ? 80 : tabURL.getPort()), 
				"http",
				tabURL.getPath(), 
				"utf-8");
		
		//Log.d(TAG, text);
		
		Pattern pattern = Pattern.compile(ANIMATE_PAGES_REGEXP);
		Matcher matcher = pattern.matcher(text);
		while (matcher.find()) {
			String titleHref = matcher.group(1);
			String title = matcher.group(3);
			AnimatePageInfo pageInfo = new AnimatePageInfo(titleHref, title);
			tabPages.add(pageInfo);
		}
		return tabPages;
	}
	
	public String getAnimateDetail(String url) throws ClientProtocolException, IOException {
		String asxUrl = null;
		URL tabURL = new URL(url);
		String text = getWebText(
				tabURL.getHost(), 
				(tabURL.getPort() == -1 ? 80 : tabURL.getPort()), 
				"http",
				tabURL.getPath(), 
				"utf-8");
		
		//Log.d(TAG, text);
		
		Pattern pattern = Pattern.compile(ANIMATE_DETAIL_REGEXP);
		Matcher matcher = pattern.matcher(text);
		while (matcher.find()) {
			asxUrl = matcher.group(1);
		}
		if (asxUrl != null && !asxUrl.contains("http")) {
			asxUrl = "http://www.animate.tv" + asxUrl;
		}
		//return asxUrl;
		return text;
	}
	
	
	public String getASX(String tab) throws ClientProtocolException, IOException {
		URL tabURL = new URL(tab);
		String text = getWebText(
				tabURL.getHost(), 
				(tabURL.getPort() == -1 ? 80 : tabURL.getPort()), 
				"http",
				tabURL.getPath(), 
				"utf-8");
		
		if (text != null) {
			text = text.toLowerCase();
//			System.err.println("getASX == " + text);
		}
		Pattern pattern = Pattern.compile(ASX_REGEXP);
		Matcher matcher = pattern.matcher(text);
		String href = null;
		while (matcher.find()) {
			href = matcher.group(1);
		}
		if (href == null) {
			href = "";
		}
		return href;
	}
	
	public String getASXURL(String tab) throws ClientProtocolException, IOException {
		URL tabURL = new URL(tab);
		String text = getWebText(
				tabURL.getHost(), 
				(tabURL.getPort() == -1 ? 80 : tabURL.getPort()), 
				"http",
				tabURL.getPath(), 
				"utf-8");
		
		if (text != null) {
//			text = text.toLowerCase();
//			System.err.println("getASXURL == " + text);
		}
		Pattern pattern = Pattern.compile(ASX_URL_REGEXP);
		Matcher matcher = pattern.matcher(text);
		String href = null;
		while (matcher.find()) {
			href = matcher.group(1);
		}
		if (href == null) {
			href = "";
		} else {
			href = ASX_URL_HEADER + href;
		}
		return href;
	}

	public String getASX2(String tab) {
    	URL url;
    	BufferedInputStream bis = null;
    	URLConnection connection = null;
    	InputStream conIs = null;
    	String text = null;
    	try {
           url = new URL(tab);
           connection = url.openConnection();
           connection.setUseCaches(true);
           conIs = connection.getInputStream();
           bis = new BufferedInputStream(conIs);
           ByteArrayOutputStream bytes = new ByteArrayOutputStream();
           while (true) {
        	   int i = bis.read();
        	   if (i == -1) {
        		   break;
        	   }
        	   bytes.write(i);
           }
           text = bytes.toString("utf8");
        } catch (MalformedURLException e) {
        	e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        	if (bis != null) {
        		try {
					bis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
        	}
        	if (conIs != null) {
        		try {
        			conIs.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
        	}
        }
		if (text != null) {
			text = text.toLowerCase();
//			System.err.println("getASX2 == " + text);
		}
//		System.err.println("getASX2 == " + text);
		Pattern pattern = Pattern.compile(ASX_REGEXP);
		Matcher matcher = pattern.matcher(text);
		String href = null;
		while (matcher.find()) {
			href = matcher.group(1);
		}
		if (href == null) {
			href = "";
		}
//		System.err.println("getASX2 == " + href);
    	return href;
	}
	
	/**
	 * 
	 * @return URLs, for example, http://hibiki-radio.jp:80/get_program/1
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public List<String> getTabs() throws ClientProtocolException, IOException {
		tabs.clear();
		String text = getWebText("hibiki-radio.jp", 80, "http",
				"/", "shift-jis");
		if (D) {
			//Log.d(TAG, text);
		}
		Pattern pattern = Pattern.compile(TABS_REGEXP, Pattern.MULTILINE);
		Matcher matcher = pattern.matcher(text);
		while (matcher.find()) {
			String str = matcher.group(1);
			if (D) {
				//Log.d(TAG, str);
			}
			tabs.add(str);
		}
		return tabs;
	}
	
	/**
	 * @see http://lantis-net.com/index.html
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public List<LantisTabInfo> getLantisTabs() throws ClientProtocolException, IOException {
		lantisTabs.clear();
		String text = getWebText("lantis-net.com", 80, "http",
				"/index.html", "UTF-8");
		if (D) {
			//Log.d(TAG, text);
		}
		Pattern pattern = Pattern.compile(LANTIS_TABS_REGEXP, Pattern.MULTILINE);
		Matcher matcher = pattern.matcher(text);
		while (matcher.find()) {
			String id = matcher.group(1);
			String title = matcher.group(2);
			String href = matcher.group(3);
			if (href != null) {
				if (href.contains("http")) {
					lantisTabs.add(new LantisTabInfo(id, title, href));
				} else {
					lantisTabs.add(new LantisTabInfo(id, title, "http://lantis-net.com/" + href));
				}
			}
		}
		return lantisTabs;
	}
	
	/**
	 * @see http://blog.csdn.net/firewings_r/article/details/5374851
	 * @param site
	 * @param port
	 * @param protocol
	 * @param uri
	 * @param encoding
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public String getWebText(String site, int port,
			String protocol, String uri, String encoding)
			throws ClientProtocolException, IOException {
		
		BasicHttpParams httpParams = new BasicHttpParams();
//        // 设置连接超时和 Socket 超时，以及 Socket 缓存大小
		HttpConnectionParams.setConnectionTimeout(httpParams, 5 * 1000);
        HttpConnectionParams.setSoTimeout(httpParams, 5 * 1000);
        HttpConnectionParams.setSocketBufferSize(httpParams, 8192);
//        // 设置重定向，缺省为 true
//        HttpClientParams.setRedirecting(httpParams, true);
//        // 设置 user agent
//        String userAgent = "Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.2) Gecko/20100115 Firefox/3.6";
//        HttpProtocolParams.setUserAgent(httpParams, userAgent);
//		
        
        DefaultHttpClient httpclient = new DefaultHttpClient(httpParams);
		String text = null;
		try {
			HttpHost target = new HttpHost(site, port, protocol);
			req = new HttpGet(uri);
			//req.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/537.17 (KHTML, like Gecko) Chrome/24.0.1312.52 Safari/537.17");
			
			HttpResponse rsp = httpclient.execute(target, req);
			HttpEntity entity = rsp.getEntity();
			if (entity != null) {
				text = EntityUtils.toString(entity, encoding);
			}
		} finally {
			httpclient.getConnectionManager().shutdown();
			req = null;
		}
		return text;
	}
	
	public void abort() {
		if (req != null) {
			req.abort();
		}
	}
}
