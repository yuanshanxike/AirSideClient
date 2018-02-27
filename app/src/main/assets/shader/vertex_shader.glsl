attribute vec4 aPosition;
uniform mat4 uTextureMatrix;
attribute vec4 aTextureCoordinate;

//传给片段着色器的纹理坐标
varying vec2 vTextureCoord;

void main() {
  vTextureCoord = (uTextureMatrix * aTextureCoordinate).xy;
  gl_Position = aPosition;
}
