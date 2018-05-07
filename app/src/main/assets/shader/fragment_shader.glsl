#extension GL_OES_EGL_image_external : require
precision mediump float;

//外部纹理
uniform samplerExternalOES uTextureSampler;
varying vec2 vTextureCoord;

void main() {
  //获取此纹理（预览图像）对应坐标的颜色值 (采样器采样)
  vec4 vCameraColor = texture2D(uTextureSampler, vTextureCoord);
  //将此灰度值作为输出颜色的RGB值，这样就会变成黑白滤镜
  gl_FragColor = vec4(vCameraColor.r, vCameraColor.g, vCameraColor.b, 1.0);
}
