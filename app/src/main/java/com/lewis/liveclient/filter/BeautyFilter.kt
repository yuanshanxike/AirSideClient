package com.lewis.liveclient.filter

import com.lewis.liveclient.util.shader2StringBuffer

/**
 * Created by lewis on 18-4-9.
 * 将 GPUImage的双边滤波器 和 肤色检测算法 结合在一起实现美颜滤镜
 */
class BeautyFilter(distanceNormalizationFactor: Float) : BilateralFilter(distanceNormalizationFactor
    , vertexShader = shader2StringBuffer("beauty_vertex_shader.glsl")
    , fragmentShader = shader2StringBuffer("beauty_fragment_shader.glsl")) {
  constructor(): this(distanceNormalizationFactor = 0.8f)  //次构造函数
}