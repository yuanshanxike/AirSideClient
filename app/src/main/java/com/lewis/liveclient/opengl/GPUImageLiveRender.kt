package com.lewis.liveclient.opengl

import android.hardware.Camera.CameraInfo.*
import android.opengl.GLES20
import android.os.Build
import android.support.annotation.RequiresApi
import com.lewis.liveclient.hardcode.AVCodec
import com.lewis.liveclient.jniLink.startLive
import com.lewis.liveclient.util.SupportSize
import com.lewis.liveclient.util.cameraInfo
import com.lewis.liveclient.util.initRenderScript
import com.lewis.liveclient.util.rtmpUrl
import jp.co.cyberagent.android.gpuimage.GPUImageFilter
import jp.co.cyberagent.android.gpuimage.GPUImageRenderer
import jp.co.cyberagent.android.gpuimage.Rotation
import java.nio.ByteBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.concurrent.thread

/**
 * Created by lewis on 18-4-10.
 *
 */
class GPUImageLiveRender(filter: GPUImageFilter) : GPUImageRenderer(filter) {

  @RequiresApi(Build.VERSION_CODES.KITKAT)
  private val avCodec = AVCodec(720, 1280)

  //用来存放从texture中读出來的像素数据
  private val buffer = ByteBuffer.allocate(720*1280*4)

  override fun onSurfaceCreated(unused: GL10?, config: EGLConfig?) {
    super.onSurfaceCreated(unused, config)

    //初始化RenderScript的必要上下文参数
    initRenderScript()

    //开始编码
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      thread(start = true, name = "startEncode") {
        avCodec.start()
        startLive(rtmpUrl, SupportSize.VERTICAL_HD.width, SupportSize.VERTICAL_HD.height
            , 0/*not use*/)
      }
    }
  }

  override fun onDrawFrame(gl: GL10?) {
    super.onDrawFrame(gl)
    buffer.position(0)
    GLES20.glReadPixels(0, 0, 720, 1280, GLES20.GL_RGBA   //耗时操作
        , GLES20.GL_UNSIGNED_BYTE, buffer)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
      avCodec.feedData(buffer as ByteBuffer)
  }

  fun stopEncode() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
      avCodec.stop()
  }

  fun autoAdaptCameraView() {
    if (cameraInfo.facing == CAMERA_FACING_FRONT) {
      setRotationCamera(Rotation.ROTATION_270, true, false)
    } else if (cameraInfo.facing == CAMERA_FACING_BACK) {
//      setRotationCamera(Rotation.ROTATION_90, false, false)
    }
  }
}