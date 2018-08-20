package com.lewis.liveclient.hardcode

import android.media.*
import android.os.Build
import android.support.annotation.RequiresApi
import android.util.Log
import com.lewis.liveclient.jniLink.packAACFrame
import com.lewis.liveclient.jniLink.packAVCFrame
import com.lewis.liveclient.util.getYUV420FrameBufferByRenderScript
import com.lewis.liveclient.util.initRenderScript
import com.lewis.liveclient.util.script
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.lang.RuntimeException
import java.nio.ByteBuffer
import kotlin.concurrent.thread

@RequiresApi(Build.VERSION_CODES.KITKAT)  // 要求API >= 19
/**
 * Created by lewis on 18-3-25.
 *
 */

public class AVCodec(private val width: Int,private val height: Int/*, private val filePath: String*/) {
//  private val filePath = "/sdcard/testBug.mp4"
//  private val muxer: MediaMuxer = MediaMuxer(filePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4) //多路复用器，用于音视频混合

  private val audioMine = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {    //音频编码Mine
    MediaFormat.MIMETYPE_AUDIO_AAC
  } else {
    "audio/mp4a-latm"
  }
  private var recorder: AudioRecord? = null    //录音器

  private var _audioEnc: MediaCodec? = null    //音频编码器
  private val audioEnc: MediaCodec get() = _audioEnc ?: throw NullPointerException("audioEnc is null")
  private val audioRate = 64000                //音频码率  64kbps
  private val sampleRate = 44100               //音频采样率  44.1khz
  private val channelCount = 2                 //音频编码通道数
  private val channelConfig = AudioFormat.CHANNEL_IN_STEREO  //音频录制通道,默认为立体声
  private val audioFormat = AudioFormat.ENCODING_PCM_16BIT   //音频录制格式，默认为PCM16bit

  @Volatile private var isRecording = false    //当前是否正在录制音频
  private var bufferSize = -1                  //音频数据缓存区大小


  private val videoMime = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
    MediaFormat.MIMETYPE_VIDEO_AVC
  } else {
    "video/avc"
  }
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

  @Volatile private var cancelFlag: Boolean = false

  @Volatile private var startFlag: Boolean = false

  private val getInputBuffer
     get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {codec: MediaCodec, index: Int ->
       codec.getInputBuffer(index)
     } else {codec: MediaCodec, index: Int ->
       codec.inputBuffers[index]
     }

  private val getOutputBuffer
     get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {codec: MediaCodec, index: Int ->
       codec.getOutputBuffer(index)
     } else {codec: MediaCodec, index: Int ->
       codec.outputBuffers[index]
     }

  init {
//    val file = File(filePath)
//    if (!file.exists()) file.createNewFile()
    //准备Audio
    val aFormat = MediaFormat.createAudioFormat(audioMine, sampleRate, channelCount)
//    format.setString(MediaFormat.KEY_MIME, audioMine)
    aFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
    aFormat.setInteger(MediaFormat.KEY_BIT_RATE, audioRate)
    aFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 0)
    _audioEnc = MediaCodec.createEncoderByType(audioMine)
    audioEnc.configure(aFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
    bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat) * 2    //双声道
    recorder = AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channelConfig, audioFormat
        , bufferSize)
    if (recorder?.state != AudioRecord.STATE_INITIALIZED) throw Exception("recorder init fail!")


    //准备Video
    if (width <= 0 || height <= 0) {
      throw RuntimeException("width or height < 0")
    }

    val videoFormat = MediaFormat.createVideoFormat(videoMime, width, height)
    videoFormat.setInteger(MediaFormat.KEY_BIT_RATE, rate)
    videoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, frameRate)
    videoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, frameInterval)
    Log.i("colorFormat", "the_color_format_is ${checkColorFormat(videoMime)}")
    val colorFormat = checkColorFormat(videoMime)
    videoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat)
    videoFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 0)

    _videoEnc = MediaCodec.createEncoderByType(videoMime)
    videoEnc.configure(videoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
//    val bundle = Bundle()
//    bundle.putInt(MediaCodec.PARAMETER_KEY_VIDEO_BITRATE, rate)
//    videoEnc.setParameters(bundle)

    //初始化RenderScript的必要上下文参数
    initRenderScript(colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar)
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
    _nanoTime = System.nanoTime()        //记录起始时间
    synchronized(this) {

      audioThread?.let {
        if (it.isAlive) {
          isRecording = false
          it.join()
        }
      }

      videoThread?.let {
        if (it.isAlive) {
          startFlag = false
          it.join()
        }
      }

      //audio start
      audioEnc.start()
      recorder?.startRecording()
      isRecording = true
      audioThread = thread(start = true, name = "audioEncThread") {
        while (isRecording && (!cancelFlag)) {
          Log.i("encode", "audio encode!!!")
          audioStep()
//          try {
//
//          } catch (e : IOException) {
//            e.printStackTrace()
//          }
        }
      }

      //video start
      videoEnc.start()
      startFlag = true
      videoThread = thread(start = true, name = "videoEncThread") {
        while (startFlag && (!cancelFlag)) {
          Log.i("encode", "video encode!!!")
          val time = System.currentTimeMillis()
          videoStep(nowFeedData)
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
      synchronized(ByteArray(0)) {
        isRecording = false
        Log.i("encode", "-----------------no1")
        audioThread?.join()

        startFlag = false
        Log.i("encode", "-----------------no2")
        videoThread?.join()

        Log.i("encode", "-----------------no3")

        //Audio Stop
        recorder?.stop()
        audioEnc.stop()
        audioEnc.release()

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
  private fun videoStep(byteArray: ByteBuffer) {
    val index = videoEnc.dequeueInputBuffer(-1)
    if (index >= 0) {
      if (hasNewData) {
        yuvArray = getYUV420FrameBufferByRenderScript(script, nowFeedData.array()
            , 720, 1280)   //通过renderscript转为yuv420p
        hasNewData = false
      }
      val buffer = getInputBuffer(videoEnc, index)
      buffer.clear()
      yuvArray?.let {
        buffer.put(it)
        videoEnc.queueInputBuffer(index, 0, it.size
            , (System.nanoTime()-nanoTime)/1000, if(startFlag) 0 else MediaCodec.BUFFER_FLAG_END_OF_STREAM)
      }
    }
    val info = MediaCodec.BufferInfo()
    val outIndex = videoEnc.dequeueOutputBuffer(info, 0)
    if (outIndex >= 0) {
      val outBuf = getOutputBuffer(videoEnc, outIndex)
      packAVCFrame(outBuf, info)
      videoEnc.releaseOutputBuffer(outIndex, false)
    }
//    do {
//      if (outIndex >= 0) {
//        val outBuf = getOutputBuffer(videoEnc, outIndex)
////        if (audioTrack>=0 && videoTrack>=0 && info.size>0 && info.presentationTimeUs>0) {
////          muxer.writeSampleData(videoTrack, outBuf, info)
//          packAVCFrame(outBuf, info)
////        }
//        videoEnc.releaseOutputBuffer(outIndex, false)
//        outIndex = videoEnc.dequeueOutputBuffer(info, 0)
//        if (info.flags.and(MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
//          Log.i(this::class.java.simpleName, "video end")
//          return true
//        }
//      } else if (outIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
////        videoTrack = muxer.addTrack(videoEnc.outputFormat)
////        if (audioTrack >= 0 && videoTrack >= 0)
////          muxer.start()
//      }
//    }while (outIndex >= 0)
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

  private fun audioStep() {
    val index = audioEnc.dequeueInputBuffer(-1)
    if (index >= 0) {
      val buffer = getInputBuffer(audioEnc, index)
      buffer.clear()
      recorder?.let {
        val length = it.read(buffer, bufferSize)
        if (length > 0) {
          audioEnc.queueInputBuffer(index, 0, length
              , (System.nanoTime() - nanoTime)/1000, if (isRecording) 0 else MediaCodec.BUFFER_FLAG_END_OF_STREAM)
        } else {
          Log.e("audioEnc", "length--> $length")
        }
      }
    }
    val info = MediaCodec.BufferInfo()
    val outIndex: Int = audioEnc.dequeueOutputBuffer(info, 0)
    if (outIndex >= 0) {
      val buffer = getOutputBuffer(audioEnc, outIndex)
      packAACFrame(buffer, info)
      audioEnc.releaseOutputBuffer(outIndex, false)
    }
//    do {
//      outIndex = audioEnc.dequeueOutputBuffer(info, 0)
//      if (outIndex >= 0) {
//        val buffer = getOutputBuffer(audioEnc, outIndex)
//        buffer.position(info.offset)
//        buffer.limit(info.offset + info.size)
////        if (audioTrack >= 0 && videoTrack >= 0 && info.size > 0 && info.presentationTimeUs > 0) {
////          muxer.writeSampleData(audioTrack, buffer, info)
////        }
//        packAACFrame(buffer, info)
//        audioEnc.releaseOutputBuffer(outIndex, false)
//        if ((info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
//          Log.i(this::class.java.simpleName, "audio end")
//          return true
//        }
//      } else if (outIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
//
//      } else if (outIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
//        Log.e(this::class.java.simpleName, "audio format has changed!")
////        audioTrack = muxer.addTrack(audioEnc.outputFormat)
////        if (audioTrack >= 0 && videoTrack >= 0) {
////          muxer.start()
////        }
//      }
//    } while (outIndex >= 0)
  }

  fun getADTSPacket(bb: ByteBuffer, size: Int): ByteArray {
    val data = ByteArray(size + 7)

    val profile = 2 //AAC LC
    val freqIdx = 4 //44.1kHZ
    val chanCfg = 2 //CPE

    data[0] = 0xff.toByte()
    data[1] = 0xf9.toByte()
    data[2] = (((profile-1) shl 6) + (freqIdx shl 2) + (chanCfg shr 2)).toByte()
    data[3] = ((chanCfg and 3 shl 6) + (size shr 11)).toByte()
    data[4] = (size and 0x7ff shr 3).toByte()
    data[5] = ((size and 7 shl 5) + 0x1f).toByte()
    data[6] = 0xfc.toByte()

    bb.get(data, 7, size)

    return data
  }


  val path = "/sdcard/66.aac"
  val file = File(path)

  val fos = FileOutputStream(file)
  val bos = BufferedOutputStream(fos)

  private fun writeAACFile(bb: ByteBuffer, bufferInfo: MediaCodec.BufferInfo) {
    if (bufferInfo.size == 2) return

    if (!file.exists())
      file.createNewFile()
    val bytes = ByteArray(bufferInfo.size + 7)
    bb.get(bytes, 7, bufferInfo.size)

    val profile = 2 //AAC LC
    val freqIdx = 4 //44.1kHZ
    val chanCfg = 2 //CPE

    bytes[0] = 0xff.toByte()
    bytes[1] = 0xf9.toByte()
    bytes[2] = (((profile-1) shl 6) + (freqIdx shl 2) + (chanCfg shr 2)).toByte()
    bytes[3] = ((chanCfg and 3 shl 6) + (bufferInfo.size shr 11)).toByte()
    bytes[4] = (bufferInfo.size and 0x7ff shr 3).toByte()
    bytes[5] = ((bufferInfo.size and 7 shl 5) + 0x1f).toByte()
    bytes[6] = 0xfc.toByte()

    bos.write(bytes)
    bos.flush()
  }
}