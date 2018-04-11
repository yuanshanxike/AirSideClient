package com.lewis.liveclient.filter

import android.opengl.GLES20
import jp.co.cyberagent.android.gpuimage.GPUImageBilateralFilter
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Created by lewis on 18-4-9.
 * 将 GPUImage的双边滤波器 和 肤色检测算法 结合在一起实现美颜滤镜
 */
class BeautyFilter(distanceNormalizationFactor: Float) :
    GPUImageBilateralFilter(distanceNormalizationFactor) {
  constructor(): this(distanceNormalizationFactor = 0.8f)  //次构造函数

//  fun onDraw(textureId: Int, vfArray: FloatArray) {
//    runOnDraw {
//      GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
//          GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR.toFloat())
//      GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
//          GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR.toFloat())
//      GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
//          GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE.toFloat())
//      GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
//          GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE.toFloat())
//    }
//    //拆分数组为顶点坐标数组和纹理坐标数组
//    val vArray = FloatArray(4 * 2)
//    val fArray = FloatArray(4 * 2)
//    var line = -2
//    for (i in 0 until vfArray.size) {
//      if (i/4 == 0 || i/4 == 4) continue
//      if (i%4 == 0) line += 2
//      when(i%4) {
//        0->vArray[line] = vfArray[i]
//        1->vArray[line+1] = vfArray[i]
//        2->fArray[line] = vfArray[i]
//        3->fArray[line+1] = vfArray[i]
//      }
//    }
//    val cubeBuffer = ByteBuffer.allocateDirect(4 * 2 * 4)
//        .order(ByteOrder.nativeOrder())
//        .asFloatBuffer()
//    val textureBuffer = ByteBuffer.allocateDirect(4 * 2 * 4)
//        .order(ByteOrder.nativeOrder())
//        .asFloatBuffer()
//
//    onDraw(textureId, cubeBuffer, textureBuffer)
//  }
}