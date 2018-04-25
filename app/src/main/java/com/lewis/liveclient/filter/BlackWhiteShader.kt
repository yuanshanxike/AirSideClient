package com.lewis.liveclient.filter

import com.lewis.liveclient.util.shader2StringBuffer

/**
 * Created by lewis on 18-4-25.
 *
 */
class BlackWhiteShader : BaseFilter(
    vertexShader = shader2StringBuffer("blackWhite_vertex_shader.glsl")
    , fragmentShader = shader2StringBuffer("blackWhite_fragment_shader.glsl")
)