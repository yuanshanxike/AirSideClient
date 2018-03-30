package com.lewis.liveclient.opengl

import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.os.Build
import android.support.annotation.RequiresApi
import android.util.AttributeSet
import com.lewis.liveclient.hardcode.AVCodec
import com.lewis.liveclient.jniLink.startLive
import com.lewis.liveclient.jniLink.stopLive
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

  private val cameraRenderer by lazy {
    CameraRenderer()
  }

  init {
    debugFlags = DEBUG_CHECK_GL_ERROR or DEBUG_LOG_GL_CALLS

    setEGLContextClientVersion(2)
    setRenderer(cameraRenderer)
    renderMode = RENDERMODE_WHEN_DIRTY

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
  }

  //当销毁窗口（View从窗口脱离）时进行回调，停止编码
  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    cameraRenderer.stopEncode()
    stopLive()
  }

  private inner class /*companion object*/ CameraRenderer : Renderer {

//    //load so
//    init {
//      System.loadLibrary("native-lib")
//    }
//
//    //native func
//    external fun h264Coding(byteArray: ByteArray)

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    val avCodec = AVCodec(720, 1280, "/sdcard/data.mp4")

    var _surfaceTexture: SurfaceTexture? = null //备用属性
    val surfaceTexture: SurfaceTexture get() = _surfaceTexture
        ?: throw NullPointerException("_surfaceTexture is null")
    val transformMatrix = FloatArray(16)

    private val VERTEX_SHADER: String? = shader2StringBuffer("vertex_shader.glsl")
    private val FRAGMENT_SHADER: String? = shader2StringBuffer("fragment_shader.glsl")

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

    override fun onDrawFrame(gl: GL10?) {
      GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)  //white
      GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
      //更新纹理图像
      surfaceTexture.updateTexImage()
      //获取外部纹理的矩阵，用来确定纹理的采样位置，没有此矩阵可能导致图像翻转等问题
      surfaceTexture.getTransformMatrix(transformMatrix)

      startPipeline(dataBuffer, transformMatrix)
      isOnSurfaceCreated = false

      val buffer = ByteBuffer.allocate(720*1280*4).position(0)
      GLES20.glReadPixels(0, 0, 720, 1280, GLES20.GL_RGBA   //耗时操作
          , GLES20.GL_UNSIGNED_BYTE, buffer)
      //rgba
//      val path = "/sdcard/img-rs.nv21"
//      val file = File(path)
//      if (!file.exists())
//        file.createNewFile()
//      val fos = FileOutputStream(file)
//      val bos = BufferedOutputStream(fos)
//      bos.write((buffer as ByteBuffer).array())
//      bos.flush()
//      fos.close()
      //nv21
//      testRenderScriptBySaveYUVFromBuffer((buffer as ByteBuffer).array(), 720, 1280)
//      val bytes = getYUV420FrameBufferByRenderScript((buffer as ByteBuffer).array(), 720, 1280)
//      h264Coding(bytes)
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        avCodec.feedData(buffer as ByteBuffer)
      }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
      GLES20.glViewport(0, 0, width, height)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
      // 该方法在渲染开始前调用，OpenGL ES的绘制上下文被重建时也会调用。
      //当Activity暂停时，绘制上下文会丢失，当Activity恢复时，绘制上下文会重建。
      if (VERTEX_SHADER != null && FRAGMENT_SHADER != null)
        initShaderProgram(VERTEX_SHADER, FRAGMENT_SHADER)

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

      //开始编码
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        thread(start = true, name = "startEncode") {
          avCodec.start()
          startLive("rtmp://send1a.douyu.com/live/3796285r0oaXlsWM?wsSecret=3d5449ad6c2ad5313a650acc9efde032&wsTime=5abe4b92&wsSeek=off&wm=0&tw=0"
              , 720, 1280, 0/*not use*/)
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