package com.iteye.weimingtom.hbksuger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

/**
 * @see http://d.hatena.ne.jp/hidecheck/20110420/1303306153
 * @author Administrator
 *
 */
public class HBKMediaPlayerActivity extends Activity implements Runnable {
	private static final boolean D = false;
	private static final String TAG = "HBKMediaPlayerActivity";

	public static final String EXTRA_FILENAME = "com.iteye.weimingtom.hbksuger.HBKMediaPlayerActivity.fileName";
	
	private static final int ACCELERATION_VALUE = 5000;
	private static final int THREAD_RUNNING_INTERVAL = 500;
	private static final boolean IS_STOPPED_ON_PAUSE = false;
	
	private static final int REQUEST_MP3_PATH = 1;
	private static final int REQUEST_RE_PATH = 2;
	
	private TextView textViewTime;
	private Button buttonPlay;
	private Button buttonRew;
	private Button buttonFF;
	private Button buttonOpenMP3;
	private Button buttonOpenPage;
	private SeekBar seekbar;
	private TextView textViewTitle;
	private TextView textViewLog;
	private Button buttonServicePlay;
	private ImageView imageViewThumbnail;
	private LinearLayout linearLayoutTitle;
	
	private boolean isLoopStopped;
	private MediaPlayer player;
	private Handler handler;
	private int lastPosition = 0;
	private String lastAudioFileName;
	private String lastAudioFileName2;
	
	private String fileName;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.media_player);
        
        textViewLog = (TextView) findViewById(R.id.textViewLog);
        
        textViewTime = (TextView) findViewById(R.id.textViewTime);
        textViewTime.setText(MSToString(0));
        
		buttonPlay = (Button) findViewById(R.id.button_play);
		buttonPlay.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (player != null) {
					if (player.isPlaying()) {
						buttonPlay.setText("播放");
						player.pause();
					} else {
						buttonPlay.setText("暂停");
						player.start();						
					}
				} else {
					if (lastAudioFileName != null) {
						if (lastAudioFileName.endsWith(".flv")) {
							Intent intent = new Intent(Intent.ACTION_VIEW);
							intent.setDataAndType(Uri.fromFile(new File(lastAudioFileName)), "video/*");
							try {
								startActivity(intent);
							} catch (Throwable e) {
								e.printStackTrace();
								Toast.makeText(HBKMediaPlayerActivity.this, 
										"找不到可用的应用程序", Toast.LENGTH_SHORT)
										.show();
							}
						} else {
							open(lastAudioFileName);
							buttonPlay.setText("暂停");
							player.start();
						}
					} else {
						Toast.makeText(HBKMediaPlayerActivity.this, 
							"音频文件名为空，请先指定mp3路径", 
							Toast.LENGTH_SHORT).show();
						openFile(".mp3", REQUEST_MP3_PATH);
					}
				}
			}
		});
		buttonPlay.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				if (lastAudioFileName != null && lastAudioFileName.length() > 0) {
					Intent intent = new Intent();
					intent.setAction(Intent.ACTION_VIEW);
					intent.setDataAndType(Uri.fromFile(new File(lastAudioFileName)), "audio/*");
					try {
						startActivity(intent);
					} catch (Throwable e) {
						e.printStackTrace();
						Toast.makeText(HBKMediaPlayerActivity.this, 
							"找不到可用的应用程序", Toast.LENGTH_SHORT)
							.show();
					}
				} else {
					Toast.makeText(HBKMediaPlayerActivity.this, 
							"路径为空", Toast.LENGTH_SHORT).show();
				}
				return true;
			}
		});
		buttonRew = (Button) findViewById(R.id.button_rew);
		buttonRew.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (player != null) {
					player.seekTo(player.getCurrentPosition() - ACCELERATION_VALUE);
				}
			}
		});
		buttonFF = (Button) findViewById(R.id.button_ff);
		buttonFF.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (player != null) {
					player.seekTo(player.getCurrentPosition() + ACCELERATION_VALUE);
				}
			}
		});
		seekbar = (SeekBar) findViewById(R.id.seekBar1);
		seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (fromUser && player != null) {
					try {
						player.seekTo(progress);	
					} catch (Throwable e) {
						e.printStackTrace();
					}
				}
			}
		});
		
		buttonOpenMP3 = (Button) findViewById(R.id.buttonOpenMP3);
		buttonOpenMP3.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				openFile(".mp3", REQUEST_MP3_PATH);
			}
		});
		buttonOpenMP3.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_GET_CONTENT);
				intent.setType("*/*");
				try {
					startActivityForResult(intent, REQUEST_RE_PATH);
				} catch (Throwable e) {
					e.printStackTrace();
					Toast.makeText(HBKMediaPlayerActivity.this, 
						"找不到可用的应用程序", Toast.LENGTH_SHORT)
						.show();
				}
				return true;
			}
		});
		
		buttonOpenPage = (Button) findViewById(R.id.buttonOpenPage);
		buttonOpenPage.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				openPage();
			}
		});
		
		buttonServicePlay = (Button) findViewById(R.id.buttonServicePlay);
		buttonServicePlay.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (lastAudioFileName != null) {
	                Intent intent = new Intent(HBKMusicService.ACTION_FOREGROUND);
	                intent.setClass(HBKMediaPlayerActivity.this, HBKMusicService.class);
	                intent.putExtra(HBKMusicService.EXTRA_MUSIC_NAME, lastAudioFileName);
	                startService(intent);
				} else {
					Toast.makeText(HBKMediaPlayerActivity.this, 
							"音频文件名为空，请先指定mp3路径", 
							Toast.LENGTH_SHORT).show();
				}
			}
		});
		buttonServicePlay.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				startActivity(new Intent(HBKMediaPlayerActivity.this, HBKDownloadActivity.class));
				return true;
			}
		});
		
		textViewTitle = (TextView) findViewById(R.id.textViewTitle);
		linearLayoutTitle = (LinearLayout) findViewById(R.id.linearLayoutTitle);
		textViewTitle.setText("MP3播放器");
		textViewTitle.setTextColor(0xff000000);
		linearLayoutTitle.setBackgroundColor(0xfffcabbd);
		
		imageViewThumbnail = (ImageView) findViewById(R.id.imageViewThumbnail);
		
        Intent intent = this.getIntent();
        if (intent != null) {
        	fileName = intent.getStringExtra(EXTRA_FILENAME);
        	if (fileName != null) {
				this.buttonPlay.setText("播放");
				lastAudioFileName2 = lastAudioFileName;
				lastAudioFileName = fileName;
				
				setThubnail(lastAudioFileName);
				
				stopLoop();
				updateCurrentInfo();
				updateLog(fileName);
				seekbar.setProgress(0);
				
				if (lastAudioFileName != null && lastAudioFileName.endsWith(".flv")) {
					buttonServicePlay.setEnabled(false);
					buttonPlay.setText("VLC");
					buttonRew.setEnabled(false);
					buttonFF.setEnabled(false);
				}
        	}
        }
    }

    private void setThubnail(String mp3filename) {
    	String preName = mp3filename.substring(0, mp3filename.lastIndexOf("_download_"));
        String jpgName = preName + ".jpg";
    	imageViewThumbnail.setImageURI(Uri.fromFile(new File(jpgName)));
    }
    
    private void openFile(String suffix, int requestCode) {
    	Intent intent = new Intent(this, HBKDirBrowserActivity.class);
    	intent.putExtra(HBKDirBrowserActivity.EXTRA_KEY_SUFFIX, suffix);    	
    	this.startActivityForResult(intent, requestCode);
    }
    
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (data != null) {
			switch (requestCode) {
			case REQUEST_MP3_PATH:
				{
					String resultPath = data.getStringExtra(HBKDirBrowserActivity.EXTRA_KEY_RESULT_PATH);
					if (resultPath != null) {
						this.buttonPlay.setText("播放");
						lastAudioFileName2 = lastAudioFileName;
						lastAudioFileName = resultPath;
						stopLoop();
						updateCurrentInfo();
						updateLog(resultPath);
						seekbar.setProgress(0);
					}
				}
				break;
				
			case REQUEST_RE_PATH:
				if (resultCode == RESULT_OK && 
					data != null &&
					data.getData() != null) {
					String resultPath = data.getData().getPath();
					this.buttonPlay.setText("播放");
					lastAudioFileName2 = lastAudioFileName;
					lastAudioFileName = resultPath;
					stopLoop();
					updateCurrentInfo();
					updateLog(resultPath);
					seekbar.setProgress(0);
				}
				break;
			}
		}
	}

	private void open(String path) {
		try {
			player = new MediaPlayer();
			player.setDataSource(path);
			player.setOnCompletionListener(new OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mp) {
					stopLoop();
					lastPosition = 0;
					buttonPlay.setText("播放");
				}
			});
			player.setOnBufferingUpdateListener(new OnBufferingUpdateListener() {
				@Override
				public void onBufferingUpdate(MediaPlayer mp, int percent) {
					seekbar.setSecondaryProgress(percent);
				}
			});
			player.prepare();
			seekbar.setProgress(lastPosition);
			seekbar.setMax(player.getDuration());
			if(lastAudioFileName == null || 
				lastAudioFileName2 == null) {
				lastPosition = 0;
			} else if (lastAudioFileName != null && 
				lastAudioFileName2 != null && 
				!lastAudioFileName.equals(lastAudioFileName2)) {
				lastPosition = 0;
			}
			player.seekTo(lastPosition);
			startLoop();
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(HBKMediaPlayerActivity.this, 
					"音频文件打开失败", 
					Toast.LENGTH_SHORT).show();
		}
	}

	private void startLoop() {
		isLoopStopped = false;
		if (handler == null) {
			handler = new Handler();
			handler.post(this);
		}
	}
	
	private void stopLoop() {
		if (player != null) {
			player.release();
			player = null;
		}
		isLoopStopped = true;
		handler = null;
	}
	
	private void updateCurrentInfo() {
		StringBuffer sbTitle = new StringBuffer();
		if (this.lastAudioFileName != null) {
			sbTitle.append(this.lastAudioFileName);
			sbTitle.append("\n");
		}
		sbTitle.append(MSToString(lastPosition));
		if (player != null) {
			sbTitle.append("/" + MSToString(player.getDuration()));
		}
		textViewTime.setText(sbTitle.toString());
	}
	
	@Override
	public void run() {
		if (D) {
			Log.d(TAG, "handler seek");
		}
		if (seekbar != null && player != null && player.isPlaying()) {
			lastPosition = player.getCurrentPosition();
			seekbar.setProgress(lastPosition);
			seekbar.setMax(player.getDuration());
			updateCurrentInfo();
		}
		if (isFinishing() || isLoopStopped) {
			//stop looping
		} else {
			if (handler != null) {
				handler.postDelayed(this, THREAD_RUNNING_INTERVAL);
			}
		}
	}
	
	@Override
	protected void onPause() {
		if (D) {
			Log.d(TAG, "onPause");
		}
		super.onPause();
		if (this.isFinishing()) {
			this.stopLoop();
			buttonPlay.setText("播放");			
		} else {
			if (IS_STOPPED_ON_PAUSE) {
				this.stopLoop();
				buttonPlay.setText("播放");
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		this.stopLoop();
	}
	
	private static String MSToString(int num) {
		int hour = 0;
		int min = 0;
		int sec = 0;
		int msec = 0;
		msec = num % 1000;
		min = sec = (num - msec) / 1000;
		sec %= 60;
		hour = min = (min - sec) / 60;
		min %= 60;
		hour = (min - hour) / 60;
		return String.format("%d:%02d:%02d.%03d", hour, min, sec, msec);
	}
	
	private void updateLog(String mp3filename) {
		if (mp3filename != null && mp3filename.contains(".mp3")) {
			String filename = mp3filename.replace(".mp3", ".txt");
			String str = getStringFromFile(filename);
			textViewLog.setText("");
			if (str != null) {
				textViewLog.append(str);
			}
		} else if (mp3filename != null && mp3filename.contains(".flv")) {
			String filename = mp3filename.replace(".flv", ".txt");
			String str = getStringFromFile(filename);
			textViewLog.setText("");
			if (str != null) {
				textViewLog.append(str);
			}
		}
	}
	
    private String getStringFromFile(String filename) {
    	StringBuffer sb = new StringBuffer();
		InputStream istr = null;
		InputStreamReader reader = null;
		BufferedReader rbuf = null;
		try {
			istr = new FileInputStream(filename);
			reader = new InputStreamReader(istr, "utf8");
			rbuf = new BufferedReader(reader);
			String line;
			while (null != (line = rbuf.readLine())) {
				sb.append(line);
				sb.append('\n');
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (rbuf != null) {
				try {
					rbuf.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (istr != null) {
				try {
					istr.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return sb.toString();
    }
	
	private void openPage() {
		String str = this.textViewLog.getText().toString();
		if (str != null && str.length() > 0) {
			String[] sentenses = str.split("\\n");
			String tabName = null;
			String detailUrl = null;
			StringBuffer infoText = new StringBuffer();
			if (sentenses != null) {
				for (String sentense : sentenses) {
					if (sentense != null) {
						if (sentense.contains("get_program/")) {
							tabName = sentense;
						} else if (sentense.contains("uploads/data/")) {
							detailUrl = sentense;
						} else if (!sentense.startsWith("[") && 
								!sentense.contains("rtmp") &&
								!sentense.contains("rtmpe") &&
								!sentense.contains("length = ")) {
							infoText.append(sentense);
							infoText.append("\n");
						}
					}
				}
			}
			startActivity(new Intent(this, HBKProgramDetailActivity.class)
				.putExtra(HBKProgramDetailActivity.EXTRA_INFO_TEXT, infoText.toString())
				.putExtra(HBKProgramDetailActivity.EXTRA_DETAIL_URL, detailUrl)
				.putExtra(HBKProgramDetailActivity.EXTRA_TAB_URL, tabName)
				.putExtra(HBKProgramDetailActivity.EXTRA_MP3_FILE, lastAudioFileName)
			);
		} else {
    		Toast.makeText(this, 
				"未发现源URL，请指定下载的mp3文件", 
				Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
	}
	
}
