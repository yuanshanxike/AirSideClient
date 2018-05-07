package com.lewis.liveclient.jniLink

import android.media.MediaCodec
import android.os.Build
import android.support.annotation.RequiresApi
import android.util.Log
import java.nio.ByteBuffer
import kotlin.experimental.and

/**
 * Created by lewis on 18-3-29.
 */

//帧类型：
private const val NAL_SLICE = 1        //非关键帧
private const val NAL_SLICE_DPA = 2
private const val NAL_SLICE_DPB = 3
private const val NAL_SLICE_DPC = 4
private const val NAL_SLICE_IDR = 5    //IDR
private const val NAL_SEI = 6
private const val NAL_SPS = 7          //SPS
private const val NAL_PPS = 8          //PPS
private const val NAL_AUD = 9
private const val NAL_FILLER = 12

@RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
//Network Abstract Layer  NAL
fun packAVCFrame(bb: ByteBuffer, bufferInfo: MediaCodec.BufferInfo) {
  val offset = if (bb.get(2) == 0x01.toByte()) {
    3  // 0x000001
  } else {
    4  // 0x00000001
  }
  val type: Int = bb.get(offset).and(0x1f).toInt()  // 5 bit
  if (type == NAL_SPS) {
    //example: [0, 0, 0, 1, 103, 100, 0, 41, -84, 27, 26, -64, -76, 10, 25, 0, 0, 0, 1, 104, -22, 67, -53]
    //打印发现这里将 SPS帧和 PPS帧合在了一起发送
    // SPS为 [4，len-8)
    // PPS为后4个字节
    val pps = ByteArray(4)
    val sps = ByteArray(bufferInfo.size - 12)
    bb.int   // 抛弃0,0,0,1
    bb.get(sps, 0, sps.size)
    bb.int   // 抛弃0,0,0,1
    bb.get(pps, 0, pps.size)
    LivePusher.send_sps_pps(sps, sps.size, pps, pps.size)
  } else if (type == NAL_SLICE || type == NAL_SLICE_IDR) {
    val bytes = ByteArray(bufferInfo.size)
    bb.get(bytes)
    LivePusher.send_video_body(bytes, bytes.size)
  }
}

@RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
fun packAACFrame(bb: ByteBuffer, bufferInfo: MediaCodec.BufferInfo) {
  if (bufferInfo.size == 2) {
    //查看码流可知，这里应该已经是把关键帧计算好了，所以只需直接发送
    val bytes = ByteArray(2)
    bb.get(bytes)
    bytes.forEach {
      Log.i("AAC1", "$it")
    }
    LivePusher.sendAACSpec(bytes, 2)
  } else {
    val bytes = ByteArray(bufferInfo.size)
    bb.get(bytes)
    bytes.forEach {
      Log.i("AAC2", "$it")
    }
    LivePusher.sendAACData(bytes, bytes.size, bufferInfo.presentationTimeUs / 1000)
  }
}

fun startLive(url: String, width: Int, height: Int, rate: Int) {
  LivePusher.initLive(url, width, height, rate)
  LivePusher.startPush()
}

fun stopLive() {
  LivePusher.stopRTMP()
}
