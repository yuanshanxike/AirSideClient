#include <jni.h>
#include <string>
#include <string.h>
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

/*******************************x264****************************************/

extern "C"
JNIEXPORT void

JNICALL
Java_com_lewis_liveclient_opengl_CameraView_00024CameraRenderer_h264Coding(
    JNIEnv *env,
    jobject /* this */,
    jint width,
    jint height,
    jcharArray path /*yuv图像帧(多个)缓存的文件路径*/
) {
  char* _path = (char*)env->GetCharArrayElements(path, 0);

  FILE* fp_src = fopen(_path, "rb");

  int ret;       //码率
  int y_size;    //luminance size

  int frame_num = 50;        //文件中包含的帧数
  int csp = X264_CSP_I420;   //输入的视频帧格式为i420

  int iNal = 0;              //
  x264_nal_t* pNals = NULL;
  x264_t* pHandle = NULL;
  x264_picture_t* pPic_in = (x264_picture_t*)malloc(sizeof(x264_picture_t));
  x264_picture_t* pPic_out = (x264_picture_t*)malloc(sizeof(x264_picture_t));
  x264_param_t* pParam = (x264_param_t*)malloc(sizeof(x264_param_t));

  //check
  if (fp_src == NULL) {
    //android log

    return;
  }

  x264_param_default(pParam);
  pParam->i_width = width;
  pParam->i_height = height;
  pParam->i_csp = csp;
  //Param other
  pParam->i_threads = X264_SYNC_LOOKAHEAD_AUTO;
  pParam->i_frame_total = 0;    //要编码的总帧数，不知道用0
  pParam->i_fps_den = 1;       //码率分母
  pParam->i_fps_num = 30;      //码率分子
  //参考 http://lazybing.github.io/blog/2017/06/23/x264-paraments-illustra/#section-1
  pParam->i_keyint_max = 30;   //IDR帧的最大间距（帧）
  pParam->i_keyint_min = 22;   //IDR帧的最小间距（帧）

  x264_param_apply_profile(pParam, x264_preset_names[5]); //x264_preset_names[5] is "medium"

  pHandle = x264_encoder_open(pParam);

  x264_picture_init(pPic_out);
  x264_picture_alloc(pPic_in, csp, width, height);


}

/******************************rtmp**************************************/


//  RTMP *rtmp = NULL;
//  RTMPPacket *packet = NULL;
//
//  rtmp = RTMP_Alloc();
//  RTMP_Init(rtmp);