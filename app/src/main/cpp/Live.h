//
// Created by lewis on 18-3-27.
//

#ifndef AIRSIDECLIENT_LIVE_H
#define AIRSIDECLIENT_LIVE_H

#endif //AIRSIDECLIENT_LIVE_H
#include <android/log.h>
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, "NDK", __VA_ARGS__)

#include <queue>
#include <jni.h>
#include <malloc.h>
#include <pthread.h>
#include <string.h>

extern "C" {
#include "include/x264/x264.h"
#include "include/rtmp/rtmp.h"
#define RTMP_FREE(_rtmp) if(_rtmp) {RTMP_Free(_rtmp); _rtmp = NULL;}
#define RTMP_CLOSE(_rtmp) if(_rtmp && RTMP_IsConnected(_rtmp)) RTMP_Close(_rtmp);

using namespace std;

typedef unsigned char uchar;

class Live {
 public:

 private:
  JavaVM* jvm;
  jobject jobj;

  int width;
  int height;
  int bitRate;
  int start_time;                                       //rtmp推流开始时间

  bool isHW_vedio = true;                               //视频是否为硬编（默认开启）
  bool isHW_audio = true;                               //音频是否为硬编（默认开启）

  bool pushFlag = false;                                //推流状态标志
  bool isEncoding = false;                              //是否正在进行音视频的软编码
  bool readyPushing = false;                            //针对软编（是否准备好了推流数据）

  RTMP* rtmpClient;
  char* rtmpUrl;                                        //推流地址

  queue<RTMPPacket*> rtmpQueue;

  pthread_t pushThreadId;
  pthread_mutex_t mutex = PTHREAD_MUTEX_INITIALIZER;    //互斥锁
  pthread_cond_t cond = PTHREAD_COND_INITIALIZER;       //条件变量

 public:
  Live(JavaVM* vm, jobject obj):
      jvm(vm), jobj(obj), width(0), height(0){}

  virtual ~Live(){

  }

 public:
  void init(const char* rtmpUrl, int width, int height, int bitRate);

  /***************************** 软 编 *********************************/

  void initX264Encode(int threadSize);
//  void initAudioEncode(int sampleRat, int channel);

  void startX264Encode(uchar* yuv);
//  void add_audio_data(uchar*);

  /********************************************************************/

  void startPush();

  void stop();

  //封装 MediaCodec 硬编码 得到的数据 <NAL层处理>
  void add_264_header(uchar* sps, int sps_length, uchar* pps, int pps_length);
  void add_264_body(uchar* buf, int len);

  void sendAACSpec(uchar*, int);
  void sendAACData(uchar*, int, long);

 private:
  void addPacket(RTMPPacket* packet, bool);

  void throwNativeInfo(JNIEnv *, jmethodID, jint);

  void add_aac_header();

  static void* push(void*);

  void n420_spin_right(char* dstyuv, const char* srcdata, int width, int height);

  void n420_spin_left(char* dstyuv, const char* srcdata, int width, int height);

  void sendAACSoftData(uchar*, int);
};

}