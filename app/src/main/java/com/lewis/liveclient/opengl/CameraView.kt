package com.lewis.liveclient.opengl

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Created by lewis on 18-2-21.
 *
 */
class CameraView(context: Context, attrs: AttributeSet? = null) : GLSurfaceView(context, attrs) {
  init {
    this.debugFlags = DEBUG_CHECK_GL_ERROR or DEBUG_LOG_GL_CALLS
    setRenderer(renderer)
  }

  private companion object renderer : Renderer {
    override fun onDrawFrame(gl: GL10?) {
      gl?.glClear(GL10.GL_COLOR_BUFFER_BIT or GL10.GL_DEPTH_BUFFER_BIT)

    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
      gl?.glViewport(0, 0, width, height)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
      // 该方法在渲染开始前调用，OpenGL ES的绘制上下文被重建时也会调用。
      //当Activity暂停时，绘制上下文会丢失，当Activity恢复时，绘制上下文会重建。

    }

  }
}