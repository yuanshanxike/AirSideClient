package com.lewis.liveclient.util

import android.opengl.GLES11Ext
import android.opengl.GLES20.*
import android.util.Log
import com.lewis.liveclient.androidApp
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.FloatBuffer


/**
 * Created by lewis on 18-2-25.
 * OpenGL ES操作shader工具
 */

/***********************read shader*****************************/

val shader2StringBuffer = {shader: String ->
  val inputStream = androidApp.resources.assets.open("shader/$shader")
  val stringBuilder = StringBuilder()
  var line: String?
  try {
    val reader = BufferedReader(InputStreamReader(inputStream))
    while (true) {
      line = reader.readLine()
      if (line != null) {
        stringBuilder.append("$line\n")
      } else {
        break
      }
    }
  } catch (e: IOException) {
    e.printStackTrace()
  } finally {
    try {
      inputStream.close()
    } catch (e: IOException) {
      e.printStackTrace()
    }
  }
  stringBuilder.toString()
}

fun shader2StringBuffer(shader: String): String {
  val inputStream = androidApp.resources.assets.open("shader/$shader")
  val stringBuilder = StringBuilder()
  var line: String?
  try {
    val reader = BufferedReader(InputStreamReader(inputStream))
    while (true) {
      line = reader.readLine()
      if (line != null) {
        stringBuilder.append("$line\n")
      } else {
        break
      }
    }
  } catch (e: IOException) {
    e.printStackTrace()
    throw NullPointerException(e.toString())
  } finally {
    try {
      inputStream.close()
    } catch (e: IOException) {
      e.printStackTrace()
      throw NullPointerException(e.toString())
    }
  }
  return stringBuilder.toString()
}


/********************************EGL*******************************/
//若要调用下面的OpenGL ES方法，需要EGL环境（GLSurfaceView自带了一个，所以不需要调用此方法）
//fun initEGL() {
//
//}
/******************************************************************/


/**************************OpenGL ES*******************************/

private fun loadShader(type: Int, shaderSource: String): Int {
  val shader = glCreateShader(type)

  if (shader == 0) {
    throw RuntimeException("Create Shader Failed! \n ${glGetError()}")
  }

  glShaderSource(shader, shaderSource)
  glCompileShader(shader)

  //检测shader是否有语法错误
  val compiled = IntArray(1)
  glGetShaderiv(shader, GL_COMPILE_STATUS,compiled,0)
  if(compiled[0]==0){
    Log.e("gles","Could not compile shader:"+type)
    Log.e("gles","GLES20 Error:"+ glGetShaderInfoLog(shader))
    glDeleteShader(shader)
    throw Exception("shader create fail")
  }

  return shader
}

//private fun createAndLinkProgram(vsId: Int, fsId: Int): Int {
//  val program = glCreateProgram()
//
//  if (program == 0) {
//    throw RuntimeException("Create Program Failed! \n ${glGetError()}")
//  }
//
//  glAttachShader(program, vsId)
//  glAttachShader(program, fsId)
//  glLinkProgram(program)
//  glUseProgram(program)
//
//  return program
//}

//提供一个外部纹理（给相机使用时，用于接收预览数据）注：此纹理默认是绑定到0号framebuffer（GLSurfaceView窗口）上的
val OESTextureId by lazy {
  val tex = IntArray(1)
  //生成一个纹理
  glGenTextures(1, tex, 0)
  //将此纹理绑定到外部纹理上
  glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, tex[0])
  //设置纹理过滤参数
  glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_MIN_FILTER, GL_NEAREST.toFloat())
  glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_MAG_FILTER, GL_LINEAR.toFloat())
  glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE.toFloat())
  glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE.toFloat())
  //解除纹理绑定
  glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0)
  return@lazy tex[0]
}

//private var _shaderProgram: Int? = null
//private var _vertexShader: Int? = null
//private var _fragmentShader: Int? = null
//private val mFBOIds = IntArray(1)

//fun initShaderProgram(vsSource: String, fsSource: String): Int {
//  val vertexShader = loadShader(GL_VERTEX_SHADER, vsSource)
//  val fragmentShader = loadShader(GL_FRAGMENT_SHADER, fsSource)
//  val shaderProgram = createAndLinkProgram(vertexShader, fragmentShader)
//
//  _vertexShader = vertexShader
//  _fragmentShader = fragmentShader
//  _shaderProgram = shaderProgram
//
//  //为创建的texture提供一个framebuffer (用来进行离屏渲染的FBO)
//  glGenFramebuffers(1, mFBOIds, 0)
//  glBindFramebuffer(GL_FRAMEBUFFER, mFBOIds[0])
//  return shaderProgram
//}

//fun startPipeline(vfBuffer: FloatBuffer, transformMatrix: FloatArray) {
//  val aPositionLocation = _shaderProgram?.let {
//    glGetAttribLocation(it, "aPosition")
//  } ?: throw NullPointerException("aPositionLocation is null")
//  val aTextureCoordLocation = _shaderProgram?.let {
//    glGetAttribLocation(it, "aTextureCoordinate")
//  } ?: throw NullPointerException("aTextureCoordLocation is null")
//  val uTextureMatrixLocation = _shaderProgram?.let {
//    glGetUniformLocation(it, "uTextureMatrix")
//  } ?: throw NullPointerException("uTextureMatrixLocation is null")
//  val uTextureSamplerLocation = _shaderProgram?.let {
//    glGetUniformLocation(it, "uTextureSampler")
//  } ?: throw NullPointerException("uTextureSamplerLocation is null")
//
//  //激活纹理单元0
//  glActiveTexture(GL_TEXTURE0)
//  //绑定外部纹理到纹理单元0
//  glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, OESTextureId)
//  //将此纹理单元传为片段着色器的uTextureSampler外部纹理采样器
//  glUniform1i(uTextureSamplerLocation, 0/*GL_TEXTURE0*/)
//
//  //将纹理矩阵传给片段着色器
//  glUniformMatrix4fv(uTextureMatrixLocation, 1, false, transformMatrix, 0)
//
//  //将顶点和纹理坐标传给顶点着色器
//  vfBuffer.position(0)
//  glEnableVertexAttribArray(aPositionLocation)
//  //顶点坐标每次读取两个顶点值，之后间隔16（每行4个值 * 4个字节）的字节继续读取两个顶点值
//  glVertexAttribPointer(aPositionLocation, 2, GL_FLOAT, false, 16, vfBuffer)
//
//  //纹理坐标从位置2开始读取
//  vfBuffer.position(2)
//  glEnableVertexAttribArray(aTextureCoordLocation)
//  //纹理坐标每次读取两个顶点值，之后间隔16（每行4个值 * 4个字节）的字节继续读取两个顶点值
//  glVertexAttribPointer(aTextureCoordLocation, 2, GL_FLOAT, false, 16, vfBuffer)
//
//  //绘制两个三角形（6个顶点）
//  glDrawArrays(GL_TRIANGLES, 0, 6)
//
//  //为了让所有的渲染操作对主窗口产生影响我们必须通过绑定为0来使默认帧缓冲被激活
//  glBindFramebuffer(GL_FRAMEBUFFER, 0)
//}

fun createAndLinkProgram(vsSource: String, fsSource: String): Int {
  val vsId = loadShader(GL_VERTEX_SHADER, vsSource)
  val fsId = loadShader(GL_FRAGMENT_SHADER, fsSource)

  val program = glCreateProgram()
  val link = IntArray(1)

  if (program == 0) {
    throw RuntimeException("Create Program Failed! \n ${glGetError()}")
  }

  glAttachShader(program, vsId)
  glAttachShader(program, fsId)
  glLinkProgram(program)

  glGetProgramiv(program, GL_LINK_STATUS, link, 0)
  if (link[0] <= 0) {
    throw RuntimeException("Linking Failed")
  }
  glDeleteShader(vsId)
  glDeleteShader(fsId)

  return program
}

/****************************more than one gl program***************************************/

private var _shaderPrograms: IntArray? = null

fun createAndLinkProgram(vsId: Int, fsId: Int): Int {
  val program = glCreateProgram()
  val link = IntArray(1)

  if (program == 0) {
    throw RuntimeException("Create Program Failed! \n ${glGetError()}")
  }

  glAttachShader(program, vsId)
  glAttachShader(program, fsId)
  glLinkProgram(program)

  glGetProgramiv(program, GL_LINK_STATUS, link, 0)
  if (link[0] <= 0) {
    throw RuntimeException("Linking Failed")
  }
  glDeleteShader(vsId)
  glDeleteShader(fsId)

  return program
}

//fun createShaderPrograms(n: Int, vsSourceArray: Array<String>, fsSourceArray: Array<String>)
//    : IntArray {
//  val shaderPrograms = IntArray(n)
//  for (i in 0 until n) {
//    val vertexShader = loadShader(GL_VERTEX_SHADER, vsSourceArray[i])
//    val fragmentShader = loadShader(GL_FRAGMENT_SHADER, fsSourceArray[i])
//    shaderPrograms[i] = createAndLinkProgram(vertexShader, fragmentShader)
//  }
//  _shaderPrograms = shaderPrograms
//  return shaderPrograms
//}
//
//fun useProgramByIndex(programIndex: Int) {
//  _shaderPrograms?.let {
//    if (programIndex < nShader)
//      glUseProgram(it[programIndex])
//  } ?: throw NullPointerException("_shaderPrograms is null")
//}
//
//fun startPipeline(index: Int, vfBuffer: FloatBuffer, transformMatrix: FloatArray) {
//  val aPositionLocation = _shaderPrograms?.let {
//    glGetAttribLocation(it[index], "aPosition")
//  } ?: throw NullPointerException("aPositionLocation is null")
//  val aTextureCoordLocation = _shaderPrograms?.let {
//    glGetAttribLocation(it[index], "aTextureCoordinate")
//  } ?: throw NullPointerException("aTextureCoordLocation is null")
//  val uTextureMatrixLocation = _shaderPrograms?.let {
//    glGetUniformLocation(it[index], "uTextureMatrix")
//  } ?: throw NullPointerException("uTextureMatrixLocation is null")
//  val uTextureSamplerLocation = _shaderPrograms?.let {
//    glGetUniformLocation(it[index], "uTextureSampler")
//  } ?: throw NullPointerException("uTextureSamplerLocation is null")

//  /*三种方式都是可行的，任选其一就可以*/
//  //激活纹理单元0（方式一）
//  glActiveTexture(GL_TEXTURE0)
//  //将0号纹理单元传为片段着色器的uTextureSampler纹理采样器
//  glUniform1i(uTextureSamplerLocation, 0/*GL_TEXTURE0*/)
//  //绑定到外部纹理纹理（方式二）
//  glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, OESTextureId)
//  //将外部纹理单元传为片段着色器的uTextureSampler外部纹理采样器
//  glUniform1i(uTextureSamplerLocation, GLES11Ext.GL_TEXTURE_EXTERNAL_OES)
//  //使用默认绑定。因为shader中只有一个采样器（0号framebuffer上的纹理），所以可直接设置采样器Uniform为0(或GL_TEXTURE0) 或 直接不写(默认值就是0)（方法三）
//  glUniform1i(uTextureSamplerLocation, 0)    //亲测，只要赋的值是0或>=8的数都可以正常进行采样

//  //将纹理矩阵传给片段着色器
//  glUniformMatrix4fv(uTextureMatrixLocation, 1, false, transformMatrix, 0)
//
//  //将顶点和纹理坐标传给顶点着色器
//  vfBuffer.position(0)
//  glEnableVertexAttribArray(aPositionLocation)
//  //顶点坐标每次读取两个顶点值，之后间隔16（每行4个值 * 4个字节）的字节继续读取两个顶点值
//  glVertexAttribPointer(aPositionLocation, 2, GL_FLOAT, false, 16, vfBuffer)
//
//  //纹理坐标从位置2开始读取
//  vfBuffer.position(2)
//  glEnableVertexAttribArray(aTextureCoordLocation)
//  //纹理坐标每次读取两个顶点值，之后间隔16（每行4个值 * 4个字节）的字节继续读取两个顶点值
//  glVertexAttribPointer(aTextureCoordLocation, 2, GL_FLOAT, false, 16, vfBuffer)
//
//  //绘制两个三角形（6个顶点）
//  glDrawArrays(GL_TRIANGLES, 0, 6)
//}


//clear gl
//fun deleteOpenGLES() {
//  _shaderProgram?.let {
//    glDeleteProgram(it)
//  }
//  _vertexShader?.let {
//    glDeleteShader(it)
//  }
//  _fragmentShader?.let {
//    glDeleteShader(it)
//  }
//  glDeleteTextures(1, intArrayOf(OESTextureId), 0)
//  glDeleteFramebuffers(1, mFBOIds, 0)
//}