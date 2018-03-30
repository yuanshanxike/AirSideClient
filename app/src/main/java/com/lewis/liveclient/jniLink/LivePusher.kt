package com.lewis.liveclient.jniLink

import android.os.Handler
import android.util.Log
import com.lewis.liveclient.callback.OnRtmpConnectListener

/**
 * Created by lewis on 18-3-27.
 *
 */
internal object LivePusher {
  init {
    System.loadLibrary("native-lib")
  }

  var listener: OnRtmpConnectListener? = null

  private val handler = Handler()  //在 ui线程 调用该对象的时候初始化，Looper为sMainLooper

  /**
   * 初始化参数
   * @param rtmpUrl 推流地址
   * @param width 宽度
   * @param height 高度
   */
  external fun initLive(rtmpUrl: String, width: Int, height: Int, bitRate: Int)

  //初始化x264视频编码器
  external fun initX264Encode(threadSize: Int)

//  /**
//   * 初始化音频软编码
//   * @param sampleRate 采样率
//   * @param channel 声道数
//   */
//  external fun initAudioEncode(sampleRat: Int, channel: Int)

  //x264软编码
  external fun x264Coding(byteArray: ByteArray)

  //开启推流线程
  external fun startPush()

  /**
   * 硬编码 推流sps pps 视频信息帧
   * @param sps sps
   * @param sps_length sps_length
   * @param pps pps
   * @param pps_length pps_length
   */
  external fun send_sps_pps(sps: ByteArray, sps_length: Int, pps: ByteArray, pps_length: Int)

  /**
   * 硬编码 推流视频内容
   * @param body body
   * @param body_length body_length
   */
  external fun send_video_body(body: ByteArray, body_length: Int)

  /**
   * 硬编码 音频aac 头
   * @param aac_spec aac_spec
   * @param len len
   */
  external fun sendAACSpec(aac_spec: ByteArray, len: Int)

  /**
   * 硬编码 音频aac体
   * @param data data
   * @param len len
   * @param timestamp timestamp
   */
  external fun sendAACData(data: ByteArray, len: Int, timestamp: Long)

  //停止推流
  external fun stopRTMP()

//  /**
//   * 软编码 自定义音频aac 头
//   * @param data data
//   */
//  external fun setAacSpecificInfos(data: ByteArray)

//  /**
//   * 软编吗添加音频pcm 体
//   * @param data pcm格式音频数据
//   */
//  external fun addAudioData(data: ByteArray)


  //负责接收native层回调过来的rtmp状态
  fun onNativeResponse(status: Int) {
    Log.d("onNativeResponse", status.toString())
    listener?.let {
      handler.post {
        when(status) {
          -101, -102, -103, -104->
              it.rtmpConnect("rtmp连接失败", status)
          -105->
              it.rtmpConnect("rtmp包推流失败", status)
          100->
              it.rtmpConnect("rtmp连接成功", status)
          else->
              it.rtmpConnect("未知状态", status)
        }
      }
    }
  }
}