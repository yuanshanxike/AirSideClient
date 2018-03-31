//
// Created by lewis on 18-3-27.
//

#include "Live.h"

void Live::init(const char* rtmpUrl, int width, int height, int bitRate) {
  LOGD("video pusher path:%s width:%d,height:%d,bitrate:%d", rtmpUrl, width,height, bitRate);
  Live::width = width;
  Live::height = height;
  Live::bitRate = bitRate;
  Live::rtmpUrl = (char *)new uchar[strlen(rtmpUrl) + 1];
  memset(Live::rtmpUrl, '\n', strlen(rtmpUrl) + 1);
  memcpy(Live::rtmpUrl, rtmpUrl, strlen(rtmpUrl));
}

void Live::initX264Encode(int threadSize) {
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

void Live::startX264Encode(uchar *yuv) {

}

void Live::startPush() {
  stop();
  while (pushFlag); //等待上一次推流完成
  LOGD("开启推流线程");
  pthread_create(&pushThreadId, NULL, Live::push, this);
}

void Live::stop() {
  pthread_mutex_lock(&mutex);
  LOGD("停止推流");
  pushFlag = false;
  pthread_cond_signal(&cond);
  pthread_mutex_unlock(&mutex);
}

void Live::add_264_header(uchar *sps, int sps_length, uchar *pps, int pps_length) {
  uint32_t body_size = (uint32_t)(13 + sps_length + 3 + pps_length);
  RTMPPacket* packet = (RTMPPacket*)malloc(sizeof(RTMPPacket));
  if (!RTMPPacket_Alloc(packet, body_size)) {
    free(packet);
    return;
  }
  char* body = packet->m_body;
  int i = 0;
  body[i++] = 0x17;
  body[i++] = 0x00;
  //composition time 0x000000
  body[i++] = 0x00;
  body[i++] = 0x00;
  body[i++] = 0x00;

  body[i++] = 0x01;
  body[i++] = sps[1];
  body[i++] = sps[2];
  body[i++] = sps[3];
  body[i++] = (char) 0xff;
  //sps
  body[i++] = (char) 0xE1;
  body[i++] = (char) ((sps_length >> 8) & 0xff);
  body[i++] = (char) (sps_length & 0xff);
  memcpy(&body[i], sps, (size_t) sps_length);
  i += sps_length;
  //pps
  body[i++] = 0x01;
  body[i++] = (char) ((pps_length >> 8) & 0xff);
  body[i++] = (char) (pps_length & 0xff);
  memcpy(&body[i], pps, (size_t) pps_length);
  i+=pps_length;
  packet->m_packetType = RTMP_PACKET_TYPE_VIDEO;
  packet->m_nBodySize = (uint32_t) body_size;
  packet->m_nChannel = 0x04;
  packet->m_nTimeStamp = 0;
  packet->m_hasAbsTimestamp = 0;
  packet->m_headerType = RTMP_PACKET_SIZE_MEDIUM;

  addPacket(packet, true);
}

void Live::add_264_body(uchar *buf, int len) {
  if (buf[2] == 0x00) {  //00 00 00 01
    buf += 4;
    len -= 4;
  } else if (buf[2] == 0x01) { //00 00 01
    buf += 3;
    len -= 3;
  }
  int body_size = len + 9;
  RTMPPacket *packet = (RTMPPacket*)malloc(sizeof(RTMPPacket));
  RTMPPacket_Alloc(packet, (uint32_t)body_size);
  char* body = packet->m_body;
  int type = buf[0] & 0x1f;
  body[0] = 0x27;
  if (type == NAL_SLICE_IDR) {
    body[0] = 0x17;
  }
  body[1] = 0x01;
  body[2] = 0x00;
  body[3] = 0x00;
  body[4] = 0x00;

  body[5] = (char)((len >> 24) & 0xff);
  body[6] = (char)((len >> 16) & 0xff);
  body[7] = (char)((len >> 8) & 0xff);
  body[8] = (char)(len & 0xff);

  memcpy(&body[9], buf, (size_t)len);
  packet->m_hasAbsTimestamp = 0;
  packet->m_nBodySize = (uint32_t)body_size;
  packet->m_packetType = RTMP_PACKET_TYPE_VIDEO;
  packet->m_nChannel = 0x04;
  packet->m_headerType = RTMP_PACKET_SIZE_LARGE;
  packet->m_nTimeStamp = RTMP_GetTime() - start_time;

  addPacket(packet, true);
}

/**
 * 音视频数据加入推流队列
 * @param packet
 * @param isVideo 用来判断包类型
 */
void Live::addPacket(RTMPPacket *packet, bool isVideo) {
  pthread_mutex_lock(&mutex);
  if (isVideo) {
    if (isHW_vedio) {
      goto ADD;
    } else {
      if (readyPushing) {
        goto ADD;
      } else {
        goto FREE;
      }
    }
  } else {
    if (isHW_audio) {
      goto ADD;
    } else {
      if (readyPushing) {
        goto ADD;
      } else {
        goto FREE;
      }
    }
  }
  ADD:
  rtmpQueue.push(packet);
  goto UNLOCK;

  FREE:
  RTMPPacket_Free(packet);
  free(packet);
  goto UNLOCK;

  UNLOCK:
  pthread_cond_signal(&cond);
  pthread_mutex_unlock(&mutex);
}

void Live::throwNativeInfo(JNIEnv *env, jmethodID methodId, jint code) {
  if (env && methodId && code) {
    env->CallVoidMethod(jobj, methodId, code);
  } else {
    LOGD("调用java方法失败");
  }
}

void Live::add_aac_header() {

}

void *Live::push(void *args) {
  LOGD("线程开启");
  Live* live = (Live*) args;
  live->pushFlag = true;   //修改标志位，让调用线程等待

  JNIEnv* env;
  live->jvm->AttachCurrentThread(&env, 0);
  jclass clazz = env->GetObjectClass(live->jobj);
  jmethodID postID = env->GetMethodID(clazz, "onNativeResponse", "(I)V");  //通过这个id调用java方法
  do {
    live->rtmpClient = RTMP_Alloc();
    if (!live->rtmpClient) {
      live->throwNativeInfo(env, postID, -101);
      goto END;
    }
    RTMP_Init(live->rtmpClient);
    live->rtmpClient->Link.timeout = 3;
    live->rtmpClient->Link.flashVer = RTMP_DefaultFlashVer;
    if (!RTMP_SetupURL(live->rtmpClient, live->rtmpUrl)) {
      live->throwNativeInfo(env, postID, -102);
      goto END;
    }
    RTMP_EnableWrite(live->rtmpClient);
    if (!RTMP_Connect(live->rtmpClient, NULL)) {
      live->throwNativeInfo(env, postID, -103);
      goto END;
    }
    if (!RTMP_ConnectStream(live->rtmpClient, 0)) {
      live->throwNativeInfo(env, postID, -104);
      goto END;
    }

    live->throwNativeInfo(env, postID, 100);
    live->start_time = RTMP_GetTime();

    while (live->pushFlag) {
      pthread_mutex_lock(&live->mutex);
      pthread_cond_wait(&live->cond, &live->mutex);
      if (!live->pushFlag) {
        LOGD("推流结束");
        pthread_mutex_unlock(&live->mutex);
        goto END;
      }
      if (live->rtmpQueue.empty()) {
        LOGD("推流队列为空，继续循环");
        pthread_mutex_unlock(&live->mutex);
        continue;
      }
      int size = live->rtmpQueue.size();
      for (int i=0; i<size; i++) {
        if (!live->pushFlag) {
          LOGD("推流结束");
          pthread_mutex_unlock(&live->mutex);
          goto END;
        }
        RTMPPacket* packet = live->rtmpQueue.front();
        live->rtmpQueue.pop();
        if (packet) {
          packet->m_nInfoField2 = live->rtmpClient->m_stream_id;
          int result = RTMP_SendPacket(live->rtmpClient, packet, 1);                  //发送数据包（推流）
          if (!result) {
            LOGD("RTMP_SendPacket fail");
            RTMPPacket_Free(packet);
            free(packet);
            live->throwNativeInfo(env, postID, -105);
            pthread_mutex_unlock(&live->mutex);
            goto END;
          }
          RTMPPacket_Free(packet);
          free(packet);
          //LOGD("push rtmp");
        }
      }
      pthread_mutex_unlock(&live->mutex);
    }

    END:
    RTMP_CLOSE(live->rtmpClient)
    RTMP_FREE(live->rtmpClient)
  }while (false);    //just exc once?



  return nullptr;
}
