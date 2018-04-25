package com.lewis.liveclient.widget

import android.content.Context
import android.graphics.PixelFormat
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.os.Build
import android.support.annotation.RequiresApi
import android.util.AttributeSet
import android.view.SurfaceHolder
import com.lewis.liveclient.hardcode.AVCodec
import com.lewis.liveclient.jniLink.startLive
import com.lewis.liveclient.jniLink.stopLive
import com.lewis.liveclient.opengl.GPUImageLiveRender
import com.lewis.liveclient.util.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.concurrent.thread

/**
 * Created by lewis on 18-2-21.
 *
 */
class CameraView constructor(context: Context, attrs: AttributeSet? = null)
  : GLSurfaceView(context, attrs) {

//  constructor(activity: Activity) : this(activity, null)  //次构造函数

  private var _camera: Camera? = null  //备用属性
  val camera: Camera get() = _camera ?: throw NullPointerException()

  private val render = CameraRenderer()

  init {
    debugFlags = DEBUG_CHECK_GL_ERROR or DEBUG_LOG_GL_CALLS

    //camera
//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//      val cameraManager: CameraManager =
//          context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
//      for (i in 0..cameraManager.cameraIdList.size) {
//        Log.i("cameraDevice: $i", cameraManager.cameraIdList[i])
//      }
//
//    } else {
      _camera = initCamera(context)
//    }

    setEGLContextClientVersion(2)
    setRenderer(render)
    renderMode = RENDERMODE_WHEN_DIRTY
  }

  override fun surfaceDestroyed(holder: SurfaceHolder?) {
    render.surfaceTexture.release()
    render.stopEncode()
    stopLive()
    super.surfaceDestroyed(holder)
  }

  override fun onPause() {
    super.onPause()
    camera.stopPreview()
    render.surfaceTexture.release()
  }

  private inner class /*companion object*/ CameraRenderer : Renderer {

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private val avCodec = AVCodec(720, 1280)

    private var _surfaceTexture: SurfaceTexture? = null //备用属性
    val surfaceTexture: SurfaceTexture get() = _surfaceTexture
        ?: throw NullPointerException("_surfaceTexture is null")
    private val transformMatrix = FloatArray(16)

    //顶点和纹理坐标
    private val vfData: FloatArray = floatArrayOf(
        1f,  1f,  1f,  1f,
        -1f,  1f,  0f,  1f,
        -1f, -1f,  0f,  0f,
        1f,  1f,  1f,  1f,
        -1f, -1f,  0f,  0f,
        1f, -1f,  1f,  0f
    )

    private var isOnSurfaceCreated = true

    private val dataBuffer: FloatBuffer = ByteBuffer.allocateDirect(vfData.size * 4)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
    get() {
      return if (isOnSurfaceCreated) {
        field.put(vfData, 0, vfData.size).position(0)
        field
      } else {
        field
      }
    }

    //用来存放从texture中读出來的像素数据
    private val buffer = ByteBuffer.allocate(720*1280*4)

    override fun onDrawFrame(gl: GL10?) {
      GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)  //white
      GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
      //更新纹理图像
      surfaceTexture.updateTexImage()
      //获取外部纹理的矩阵，用来确定纹理的采样位置，没有此矩阵可能导致图像翻转等问题
      surfaceTexture.getTransformMatrix(transformMatrix)

      runAllOnGLThread()
      filter.onDraw(OESTextureId, dataBuffer, transformMatrix)
      isOnSurfaceCreated = false

      buffer.position(0)
      GLES20.glReadPixels(0, 0, 720, 1280, GLES20.GL_RGBA   //耗时操作
          , GLES20.GL_UNSIGNED_BYTE, buffer)
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        avCodec.feedData(buffer as ByteBuffer)
      }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
      GLES20.glViewport(0, 0, width, height)
      filter.onOutputSizeChanged(width, height)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
      // 该方法在渲染开始前调用，OpenGL ES的绘制上下文被重建时也会调用。
      //当Activity暂停时，绘制上下文会丢失，当Activity恢复时，绘制上下文会重建。
      filter.init()

      _surfaceTexture = SurfaceTexture(OESTextureId)

      surfaceTexture.setOnFrameAvailableListener {
        //每获取到一帧数据时请求OpenGL ES进行渲染
//        queueEvent { //将任务加入到glThread
//
//        }
        //context位于main线程
        this@CameraView.requestRender()
      }

      //初始化RenderScript的必要上下文参数
      initRenderScript()

      //将此SurfaceTexture作为相机预览输出
      camera.setPreviewTexture(surfaceTexture)
      //开启预览
      camera.startPreview()

      //开始编码并推流
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        thread(start = true, name = "startEncode") {
          avCodec.start()
          startLive(rtmpUrl, SupportSize.VERTICAL_HD.width, SupportSize.VERTICAL_HD.height
              , 0/*not use*/)
        }
      }
    }

    fun stopEncode() {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        avCodec.stop()
      }
    }

  }
}