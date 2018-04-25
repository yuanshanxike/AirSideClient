package com.lewis.liveclient.hardcode

import android.media.*
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.util.Log
import com.lewis.liveclient.jniLink.packAVCFrame
import com.lewis.liveclient.util.getYUV420FrameBufferByRenderScript
import java.io.File
import java.lang.RuntimeException
import java.nio.ByteBuffer
import kotlin.concurrent.thread

@RequiresApi(Build.VERSION_CODES.KITKAT)  // 要求API >= 19
/**
 * Created by lewis on 18-3-25.
 *
 */

public class AVCodec(private val width: Int,private val height: Int/*, private val filePath: String*/) {
//  private val muxer: MediaMuxer = MediaMuxer(filePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4) //多路复用器，用于音视频混合

  private val videoMime = "video/avc"
  private val rate = 2280000    //
  private val frameRate = 30    //fps
  private val frameInterval = 1 //视频编码关键帧，1秒一关键帧

  private val fpsTime = 1000 / frameRate

  private var yuvArray: ByteArray? = null

//  private var width: Int = -1
//  private var height: Int = -1

  private var audioTrack = -1
  private var videoTrack = -1

  private var audioThread: Thread? = null
  private var videoThread: Thread? = null

  //这里的时间单位是纳秒
  private var _nanoTime: Long? = null
  private val nanoTime: Long get() = _nanoTime ?: throw NullPointerException("nanoTime is null")

  private var _videoEnc: MediaCodec? = null
  private val videoEnc: MediaCodec get() = _videoEnc ?: throw NullPointerException("videoEnc is null")

  private var _nowFeedData: ByteBuffer? = null
  private val nullData: ByteBuffer = ByteBuffer.allocate(width * height * 4).position(0) as ByteBuffer   //没有数据时使用备用数据
  private val nowFeedData: ByteBuffer get() = _nowFeedData ?: nullData /*throw NullPointerException("nowFeedData is null")*/

  private var hasNewData: Boolean = false

  private var cancelFlag: Boolean = false

  private var startFlag: Boolean = false

  private val getInputBuffer
     get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {index: Int ->
       videoEnc.getInputBuffer(index)
     } else {index: Int ->
       videoEnc.inputBuffers[index]
     }

  private val getOutputBuffer
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {index: Int ->
      videoEnc.getOutputBuffer(index)
    } else {index: Int ->
      videoEnc.outputBuffers[index]
    }

  init {
//    val file = File(filePath)
//    if (!file.exists()) file.createNewFile()
    //准备Audio

    //准备Video
    if (width <= 0 || height <= 0) {
      throw RuntimeException("width or height < 0")
    }

    val videoFormat = MediaFormat.createVideoFormat(videoMime, width, height)
    videoFormat.setInteger(MediaFormat.KEY_BIT_RATE, rate)
    videoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, frameRate)
    videoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, frameInterval)
    videoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, checkColorFormat(videoMime))

    _videoEnc = MediaCodec.createEncoderByType(videoMime)
    videoEnc.configure(videoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
    val bundle = Bundle()
    bundle.putInt(MediaCodec.PARAMETER_KEY_VIDEO_BITRATE, rate)
    videoEnc.setParameters(bundle)
  }

  @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
  private fun checkColorFormat(mime: String): Int {
    if (Build.MODEL.equals("HUAWEI P6-C00"))
      return MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar
    for (i in 0..MediaCodecList.getCodecCount()) {
      val info = MediaCodecList.getCodecInfoAt(i)
      if (info.isEncoder) {
        val types = info.supportedTypes
        for (type in types) {
          if (type.equals(mime)) {
            val capabilitiesForType = info.getCapabilitiesForType(type)
            for (colorFormat in capabilitiesForType.colorFormats) {
              if (colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar)
                return MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar
              else if (colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar)
                return MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar
            }
          }
        }
      }
    }
    return MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar
  }

  public fun start() {
    _nanoTime = System.nanoTime()
    synchronized(this) {

      videoThread?.let {
        if (it.isAlive) {
          startFlag = false
          it.join()
        }
      }

      //video start
      videoEnc.start()
      startFlag = true
      videoThread = thread(start = true, name = "videoEncThread") {
        while (!cancelFlag) {
          val time = System.currentTimeMillis()
          if (videoStep(nowFeedData)) break
          val lt = System.currentTimeMillis() - time

          if (fpsTime > lt) {
            try {
              Thread.sleep(fpsTime - lt)
            } catch (e: InterruptedException) {
              e.printStackTrace()
            }
          }
        }
      }
    }
  }

  public fun cancel() {
    cancelFlag = true
    stop()
    cancelFlag = false
//    val file = File(filePath)
//    if (file.exists()) file.delete()
  }

  public fun stop() {
    try {
      synchronized(this) {

        startFlag = false
        videoThread?.join()

        //Audio Stop

        //Video Stop
        videoEnc.stop()
        videoEnc.release()

        //Muxer Stop
//        audioTrack = -1
//        videoTrack = -1
//        muxer.stop()
//        muxer.release()
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  //定时调用，如果没有新数据，就用上一次的数据
  private fun videoStep(byteArray: ByteBuffer): Boolean {
    val index = videoEnc.dequeueInputBuffer(-1)
    if (index >= 0) {
      if (hasNewData) {
        yuvArray = getYUV420FrameBufferByRenderScript(nowFeedData.array(), 720, 1280)   //通过renderscript转为yuv420p
        hasNewData = false
      }
      val buffer = getInputBuffer(index)
      buffer.clear()
      yuvArray?.let {
        buffer.put(it)
        videoEnc.queueInputBuffer(index, 0, it.size
            , (System.nanoTime()-nanoTime)/1000, if(startFlag) 0 else MediaCodec.BUFFER_FLAG_END_OF_STREAM)
      }
    }
    val info = MediaCodec.BufferInfo()
    var outIndex = videoEnc.dequeueOutputBuffer(info, 0)
    do {
      if (outIndex >= 0) {
        val outBuf = getOutputBuffer(outIndex)
//        if (/*audioTrack>=0 &&*/ videoTrack>=0 && info.size>0 && info.presentationTimeUs>0) {
//          muxer.writeSampleData(videoTrack, outBuf, info)
          packAVCFrame(outBuf, info)
//        }
        videoEnc.releaseOutputBuffer(outIndex, false)
        outIndex = videoEnc.dequeueOutputBuffer(info, 0)
        if (info.flags.and(MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
          Log.i(this::class.java.simpleName, "video end")
          videoEnc.releaseOutputBuffer(outIndex, false)
          return true
        }
      } else if (outIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
//        videoTrack = muxer.addTrack(videoEnc.outputFormat)
//        if (/*audioTrack >= 0 &&*/ videoTrack >= 0)
//          muxer.start()
      }
    }while (outIndex >= 0)
    return false
  }

  /**
   * 由外部喂入一帧数据(应在GLThread的上下文中调用)
   * @param data RGBA数据
   */
  public fun feedData(data: ByteBuffer) {
    _nowFeedData = data
    _nowFeedData?.position(0)
    hasNewData = true
  }

}