#ifndef __RTMPDUMP_JNI_H__
#define __RTMPDUMP_JNI_H__

#include <jni.h>

extern jmethodID RTMP_methodid_log;
extern JNIEnv * RTMP_env;
extern jobject RTMP_obj;
extern jboolean RTMP_verbose;
extern void RTMP_callback_log(char* str);
extern void RTMP_callback_progress(jlong size, jlong timestemp, jdouble duration);

#endif
