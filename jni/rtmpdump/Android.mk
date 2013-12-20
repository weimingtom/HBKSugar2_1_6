LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := rtmp

LOCAL_CFLAGS := -DRTMPDUMP_VERSION=v2.4 
# -DNO_CRYPTO
LOCAL_LDLIBS := -llog

LOCAL_SRC_FILES := librtmp/rtmp.c librtmp/amf.c librtmp/hashswf.c
LOCAL_SRC_FILES += librtmp/parseurl.c rtmpdump-jni.c log.c

LOCAL_STATIC_LIBRARIES := ssl crypto

include $(BUILD_SHARED_LIBRARY)
