#include <jni.h>
#include "Live.h"

extern "C" {
//JNIEXPORT jstring
//
//JNICALL
//Java_com_lewis_liveclient_MainActivity_stringFromJNI(
//    JNIEnv *env,
//    jobject /* this */) {
//  std::string hello = "Hello from C++";
//
//  return env->NewStringUTF(hello.c_str());
//}

JavaVM* jvm = NULL;
jobject obj = NULL;

Live* live;

//this called by JNI, not provided by JNI
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
  LOGD("------ call JNI_OnLoad");
  jvm = vm;
  JNIEnv* env = NULL;
  jint result = -1;
  if (vm->GetEnv((void**)&env, JNI_VERSION_1_4) != JNI_OK) {
    return result;
  }
  return JNI_VERSION_1_4;
}


JNIEXPORT void JNICALL Java_com_lewis_liveclient_jniLink_LivePusher_initLive(
    JNIEnv* env,
    jobject thiz,
    jstring rtmpUrl_,
    jint width,
    jint height,
    jint bitRate
) {
  if (!obj) {
    obj = env->NewGlobalRef(thiz);
  }
  live = new Live(jvm, obj);
  if (live) {
    const char* rtmpUrl = env->GetStringUTFChars(rtmpUrl_, 0);
    live->init(rtmpUrl, width, height, bitRate);
    env->ReleaseStringUTFChars(rtmpUrl_, rtmpUrl);
  }
}

JNIEXPORT void JNICALL Java_com_lewis_liveclient_jniLink_LivePusher_initX264Encode(
    JNIEnv* env,
    jobject thiz,
    jint threadSize
) {
  if (!live)
    return;
  live->initX264Encode(threadSize);
}

JNIEXPORT void JNICALL Java_com_lewis_liveclient_jniLink_LivePusher_x264Coding(
    JNIEnv* env,
    jobject thiz,
    jbyteArray yuv_
) {
  if (!live)
    return;
  jbyte *yuv = env->GetByteArrayElements(yuv_, 0);

  live->startX264Encode((uchar*)yuv);

  env->ReleaseByteArrayElements(yuv_, yuv, 0);
}

JNIEXPORT void JNICALL Java_com_lewis_liveclient_jniLink_LivePusher_startPush(
    JNIEnv* env,
    jobject thiz
) {
  if (!live)
    return;
  live->startPush();
}

JNIEXPORT void JNICALL Java_com_lewis_liveclient_jniLink_LivePusher_send_1sps_1pps(
    JNIEnv* env,
    jobject thiz,
    jbyteArray sps_,
    jint sps_length,
    jbyteArray pps_,
    jint pps_length
) {
  if (!live)
    return;
  jbyte* sps = env->GetByteArrayElements(sps_, 0);
  jbyte* pps = env->GetByteArrayElements(pps_, 0);

  live->add_264_header((uchar*)sps, sps_length, (uchar*)pps, pps_length);

  env->ReleaseByteArrayElements(sps_, sps, 0);
  env->ReleaseByteArrayElements(pps_, pps, 0);
}

JNIEXPORT void JNICALL Java_com_lewis_liveclient_jniLink_LivePusher_send_1video_1body(
    JNIEnv* env,
    jobject thiz,
    jbyteArray body_,
    jint body_length
) {
  if (!live)
    return;
  jbyte* body = env->GetByteArrayElements(body_, 0);

  live->add_264_body((uchar*)body, body_length);

  env->ReleaseByteArrayElements(body_, body, 0);
}

JNIEXPORT void JNICALL Java_com_lewis_liveclient_jniLink_LivePusher_sendAACSpec(
    JNIEnv* env,
    jobject thiz,
    jbyteArray aac_spec_,
    jint spec_length
) {
  if (!live)
    return;
  jbyte* aac_spec = env->GetByteArrayElements(aac_spec_, 0);
  live->sendAACSpec((uchar*)aac_spec, spec_length);
  env->ReleaseByteArrayElements(aac_spec_, aac_spec, 0);
}

JNIEXPORT void JNICALL Java_com_lewis_liveclient_jniLink_LivePusher_sendAACData(
    JNIEnv* env,
    jobject thiz,
    jbyteArray data_,
    jint data_length,
    jlong time_stamp
) {
  if (!live)
    return;
  jbyte* data = env->GetByteArrayElements(data_, 0);
  live->sendAACData((uchar *)data, data_length, (long)time_stamp);
  env->ReleaseByteArrayElements(data_, data, 0);
}

JNIEXPORT void JNICALL Java_com_lewis_liveclient_jniLink_LivePusher_stopRTMP(
    JNIEnv* env,
    jobject thiz
) {
  if (live) {
    live->stop();
    delete live;
  }
  obj = NULL;
  live = NULL;
}

}