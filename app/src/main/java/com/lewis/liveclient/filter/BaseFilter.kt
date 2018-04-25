package com.lewis.liveclient.filter

import android.graphics.PointF
import android.opengl.GLES20.*
import com.lewis.liveclient.util.createAndLinkProgram
import com.lewis.liveclient.util.shader2StringBuffer
import java.nio.FloatBuffer
import java.util.*

/**
 * Created by lewis on 18-4-24.
 *
 */
const val GL_NOT_INIT = -1

open class BaseFilter(
    private val vertexShader: String = shader2StringBuffer("vertex_shader.glsl")
    , private val fragmentShader: String = shader2StringBuffer("fragment_shader.glsl")
) {

  private val runOnDraw: LinkedList<Runnable> = LinkedList()
  protected var glProjectId: Int = GL_NOT_INIT
  protected var glAttribPosition: Int = GL_NOT_INIT              //顶点着色器VBO属性 id
  protected var glUniformTexture: Int = GL_NOT_INIT              //片元着色器采样器 id
  protected var glAttribTextureCoordinate: Int = GL_NOT_INIT     //纹理坐标属性 id
  protected var glTransformMatrix: Int = GL_NOT_INIT             //纹理坐标变化矩阵
  protected var outputWidth: Int = GL_NOT_INIT                   //输出图像宽度
  protected var outputHeight: Int = GL_NOT_INIT                  //输出图像高度
  private var isInitialized: Boolean = false

  fun init() {
    onInit()
    onInitialized()
  }

  open fun onInit() {
    glProjectId = createAndLinkProgram(vertexShader, fragmentShader)
    glAttribPosition = glGetAttribLocation(glProjectId, "aPosition")
    glUniformTexture = glGetUniformLocation(glProjectId, "uTextureSampler")
    glAttribTextureCoordinate = glGetAttribLocation(glProjectId, "aTextureCoordinate")
    glTransformMatrix = glGetUniformLocation(glProjectId, "uTextureMatrix")
    isInitialized = true
  }

  open fun onInitialized() {}

  fun destroy() {
    isInitialized = false
    glDeleteProgram(glProjectId)
    onDestory()
  }

  open fun onDestory() { }

  open fun onOutputSizeChanged(width: Int, height: Int) {
    outputWidth = width
    outputHeight = height
  }

  open fun onDraw(textureId: Int, vfBuffer: FloatBuffer, transformMatrix: FloatArray) {
    glUseProgram(glProjectId)
    runPendingOnDrawTasks()
    if (!isInitialized) return

    //将surface产生的纹理变化矩阵传给片段着色器
    glUniformMatrix4fv(glTransformMatrix, 1, false, transformMatrix, 0)

    //将顶点和纹理坐标传给顶点着色器
    vfBuffer.position(0)
    //顶点坐标每次读取两个顶点值，之后间隔16（每行4个值 * 4个字节）的字节继续读取两个顶点值
    glVertexAttribPointer(glAttribPosition, 2, GL_FLOAT, false, 16, vfBuffer)
    //启用（默认是禁用的）
    glEnableVertexAttribArray(glAttribPosition)

    //纹理坐标从位置2开始读取
    vfBuffer.position(2)
    //纹理坐标每次读取两个顶点值，之后间隔16（每行4个值 * 4个字节）的字节继续读取两个顶点值
    glVertexAttribPointer(glAttribTextureCoordinate, 2, GL_FLOAT, false, 16
        , vfBuffer)
    //启用（默认是禁用的）
    glEnableVertexAttribArray(glAttribTextureCoordinate)

    onDrawArraysPre()

    //绘制两个三角形（6个顶点）
    glDrawArrays(GL_TRIANGLES, 0, 6)
    //禁用顶点属性
    glDisableVertexAttribArray(glAttribPosition)
    glDisableVertexAttribArray(glAttribTextureCoordinate)
  }

  protected fun onDrawArraysPre() {}

  protected fun runPendingOnDrawTasks() {
    while (!runOnDraw.isEmpty()) {
      runOnDraw.removeFirst().run()
    }
  }

  protected fun runOnDraw(opt: ()->Unit) {
    synchronized(runOnDraw) {
      runOnDraw.addLast(Runnable { kotlin.run(opt) })
    }
  }

  protected fun setInterger(location: Int, intValue: Int) {
    runOnDraw {
      glUniform1i(location, intValue)
    }
  }

  protected fun setFloat(location: Int, floatValue: Float) {
    runOnDraw {
      glUniform1f(location, floatValue)
    }
  }

  protected fun setFloatVec2(location: Int, floatArray: FloatArray) {
    runOnDraw {
      glUniform2fv(location, 1, FloatBuffer.wrap(floatArray))
    }
  }

  protected fun setFloatVec3(location: Int, floatArray: FloatArray) {
    runOnDraw {
      glUniform3fv(location, 1, FloatBuffer.wrap(floatArray))
    }
  }

  protected fun setFloatVec4(location: Int, floatArray: FloatArray) {
    runOnDraw {
      glUniform4fv(location, 1, FloatBuffer.wrap(floatArray))
    }
  }

  protected fun setFloatArray(location: Int, floatArray: FloatArray) {
    runOnDraw {
      glUniform1fv(location, floatArray.size, FloatBuffer.wrap(floatArray))
    }
  }

  protected fun setPoint(location: Int, point: PointF) {
    runOnDraw {
      val vec2 = FloatArray(2)
      vec2[0] = point.x
      vec2[1] = point.y
      glUniform2fv(location, 1, vec2, 0)
    }
  }

  protected fun setUniformMatrix3f(location: Int, matrix: FloatArray) {
    runOnDraw {
      glUniformMatrix3fv(location, 1, false, matrix, 0)
    }
  }

  protected fun setUniformMatrix4f(location: Int, matrix: FloatArray) {
    runOnDraw {
      glUniformMatrix4fv(location,1, false, matrix, 0)
    }
  }
}