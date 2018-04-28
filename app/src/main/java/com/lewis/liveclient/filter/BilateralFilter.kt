package com.lewis.liveclient.filter

import android.opengl.GLES20.*
import com.lewis.liveclient.util.SupportSize
import com.lewis.liveclient.util.shader2StringBuffer

/**
 * Created by lewis on 18-4-25.
 *
 */
open class BilateralFilter(private var distanceNormalizationFactor: Float = 8.0f
    , vertexShader: String = shader2StringBuffer("bilateral_vertex_shader.glsl")
    , fragmentShader: String = shader2StringBuffer("bilateral_fragment_shader.glsl"))
  : BaseFilter(vertexShader = vertexShader, fragmentShader = fragmentShader) {

  private var disFactorLocation: Int = GL_NOT_INIT
  private var singleStepOffsetLocation: Int = GL_NOT_INIT

  override fun onInit() {
    super.onInit()
    disFactorLocation = glGetUniformLocation(glProjectId, "distanceNormalizationFactor")
    singleStepOffsetLocation = glGetUniformLocation(glProjectId, "singleStepOffset")
  }

  override fun onInitialized() {
    super.onInitialized()
    setDistanceNormalizationFactor(distanceNormalizationFactor)
    setTexelSize(SupportSize.VERTICAL_HD.width, SupportSize.VERTICAL_HD.height)
  }

  fun setDistanceNormalizationFactor(newValue: Float) {
    distanceNormalizationFactor = newValue
    setFloat(disFactorLocation, newValue)
  }

  private fun setTexelSize(w: Int, h: Int) {
    setFloatVec2(singleStepOffsetLocation, floatArrayOf(1.0f/w.toFloat(), 1.0f/h.toFloat()))
  }

  override fun onOutputSizeChanged(width: Int, height: Int) {
    super.onOutputSizeChanged(width, height)
    setTexelSize(width, height)
  }
}