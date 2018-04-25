//copy from android-gpuimage
attribute vec4 aPosition;
uniform mat4 uTextureMatrix;
attribute vec4 aTextureCoordinate;

const int GAUSSIAN_SAMPLES = 9;

uniform vec2 singleStepOffset;

varying vec2 textureCoordinate;
varying vec2 blurCoordinates[GAUSSIAN_SAMPLES];

void main()
{
	gl_Position = aPosition;
	textureCoordinate = (uTextureMatrix * aTextureCoordinate).xy;

	int multiplier = 0;
	vec2 blurStep;

	for (int i = 0; i < GAUSSIAN_SAMPLES; i++)
	{
		multiplier = (i - ((GAUSSIAN_SAMPLES - 1) / 2));

		blurStep = float(multiplier) * singleStepOffset;
		blurCoordinates[i] = textureCoordinate.xy + blurStep;
	}
}
