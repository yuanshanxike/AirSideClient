//copy from android-gpuimage
#extension GL_OES_EGL_image_external : require

uniform samplerExternalOES uTextureSampler;

const lowp int GAUSSIAN_SAMPLES = 9;

varying highp vec2 textureCoordinate;
varying highp vec2 blurCoordinates[GAUSSIAN_SAMPLES];

uniform mediump float distanceNormalizationFactor;

lowp vec4 whitening(lowp vec4 color) {
  lowp float r = color.r * 255.0 + 19.0;
  if (r > 255.0) r = 255.0;
  lowp float g = color.g * 255.0 + 11.0;
  if (g > 255.0) g = 255.0;
  lowp float b = color.b * 255.0 + 18.0;
  if (b > 255.0) b = 255.0;
  return vec4(r/255.0, g/255.0, b/255.0, 1.0);
}

void main()
{
  lowp vec4 centralColor;
  lowp float gaussianWeightTotal;
  lowp vec4 sum;
  lowp vec4 sampleColor;
  lowp float distanceFromCentralColor;
  lowp float gaussianWeight;

  centralColor = texture2D(uTextureSampler, blurCoordinates[4]);

  //肤色检测算法
  lowp float R = centralColor.r * 255.0;
  lowp float G = centralColor.g * 255.0;
  lowp float B = centralColor.b * 255.0;
//  if (R > 95.0 && G > 40.0 && B > 20.0 && R > G && R > B && max(max(R, G), B) - min(min(R, G), B) > 15.0 && abs(R-G) > 15.0) {
  if ((R > 95.0 && G > 40.0 && B > 20.0 && R - B > 15.0 && R - G > 15.0)
    || (R > 200.0 && G > 210.0 && B > 170.0 && abs(R-B) < 15.0 && R > B && G > B)) {

  	gaussianWeightTotal = 0.18;
    sum = whitening(centralColor) * 0.18;

    sampleColor = texture2D(uTextureSampler, blurCoordinates[0]);
    distanceFromCentralColor = min(distance(centralColor, sampleColor) * distanceNormalizationFactor, 1.0);
    gaussianWeight = 0.05 * (1.0 - distanceFromCentralColor);
    gaussianWeightTotal += gaussianWeight;
    sum += whitening(sampleColor) * gaussianWeight;

    sampleColor = texture2D(uTextureSampler, blurCoordinates[1]);
    distanceFromCentralColor = min(distance(centralColor, sampleColor) * distanceNormalizationFactor, 1.0);
    gaussianWeight = 0.09 * (1.0 - distanceFromCentralColor);
    gaussianWeightTotal += gaussianWeight;
    sum += whitening(sampleColor) * gaussianWeight;

    sampleColor = texture2D(uTextureSampler, blurCoordinates[2]);
    distanceFromCentralColor = min(distance(centralColor, sampleColor) * distanceNormalizationFactor, 1.0);
    gaussianWeight = 0.12 * (1.0 - distanceFromCentralColor);
    gaussianWeightTotal += gaussianWeight;
    sum += whitening(sampleColor) * gaussianWeight;

    sampleColor = texture2D(uTextureSampler, blurCoordinates[3]);
    distanceFromCentralColor = min(distance(centralColor, sampleColor) * distanceNormalizationFactor, 1.0);
    gaussianWeight = 0.15 * (1.0 - distanceFromCentralColor);
    gaussianWeightTotal += gaussianWeight;
    sum += whitening(sampleColor) * gaussianWeight;

    sampleColor = texture2D(uTextureSampler, blurCoordinates[5]);
    distanceFromCentralColor = min(distance(centralColor, sampleColor) * distanceNormalizationFactor, 1.0);
    gaussianWeight = 0.15 * (1.0 - distanceFromCentralColor);
    gaussianWeightTotal += gaussianWeight;
    sum += whitening(sampleColor) * gaussianWeight;

    sampleColor = texture2D(uTextureSampler, blurCoordinates[6]);
    distanceFromCentralColor = min(distance(centralColor, sampleColor) * distanceNormalizationFactor, 1.0);
    gaussianWeight = 0.12 * (1.0 - distanceFromCentralColor);
    gaussianWeightTotal += gaussianWeight;
    sum += whitening(sampleColor) * gaussianWeight;

    sampleColor = texture2D(uTextureSampler, blurCoordinates[7]);
    distanceFromCentralColor = min(distance(centralColor, sampleColor) * distanceNormalizationFactor, 1.0);
    gaussianWeight = 0.09 * (1.0 - distanceFromCentralColor);
    gaussianWeightTotal += gaussianWeight;
    sum += whitening(sampleColor) * gaussianWeight;

    sampleColor = texture2D(uTextureSampler, blurCoordinates[8]);
    distanceFromCentralColor = min(distance(centralColor, sampleColor) * distanceNormalizationFactor, 1.0);
    gaussianWeight = 0.05 * (1.0 - distanceFromCentralColor);
    gaussianWeightTotal += gaussianWeight;
    sum += whitening(sampleColor) * gaussianWeight;
    gl_FragColor = sum / gaussianWeightTotal;
    // gl_FragColor.r = distanceNormalizationFactor / 20.0;

    return;
  }
  gl_FragColor = centralColor;
}
