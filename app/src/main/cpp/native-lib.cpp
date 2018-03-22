#include <jni.h>
#include <string>
#include <stdlib.h>
#include "include/x264/x264.h"
#include "include/rtmp/rtmp_sys.h"

extern "C"
JNIEXPORT jstring

JNICALL
Java_com_lewis_liveclient_MainActivity_stringFromJNI(
    JNIEnv *env,
    jobject /* this */) {
  std::string hello = "Hello from C++";

  return env->NewStringUTF(hello.c_str());
}

extern "C"
JNIEXPORT void

JNICALL
Java_com_lewis_liveclient_opengl_CameraView_00024CameraRenderer_h264Coding(
    JNIEnv *env,
    jobject /* this */,
    jbyteArray datas) {
  x264_nal_t* pNals = NULL;
  x264_t* pHandle = NULL;
  x264_picture_t* pPic_in = (x264_picture_t*)malloc(sizeof(x264_picture_t));
  x264_picture_t* pPic_out = (x264_picture_t*)malloc(sizeof(x264_picture_t));
  x264_param_t* pParam = (x264_param_t*)malloc(sizeof(x264_param_t));

  x264_param_default(pParam);


//  RTMP *rtmp = NULL;
//  RTMPPacket *packet = NULL;
//
//  rtmp = RTMP_Alloc();
//  RTMP_Init(rtmp);
}
