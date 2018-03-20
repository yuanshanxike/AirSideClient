#pragma version(1)
#pragma rs java_package_name(com.lewis.liveclient)
#pragma rs_fp_full //use IEEE 754-2008 standard

//globals *those can be access in java(non-static)
//rs_allocation gRGBA_frame      //input
rs_allocation gNV21_frame;          //output: nv21

int width;
int height;
int uvStart;    //uv index start

//locals *those cann't be access in java(static)
static rs_matrix3x3 transform_matrix;//rgb to yuv
static const float array[9] = {0.299f, 0.587f, 0.114f, -0.169f, -0.331f, 0.5f, 0.5f, -0.419f, -0.081f};

void init() {
  rsMatrixLoad(&transform_matrix, array);
}

void RS_KERNEL rgba2nv21(uchar4 in, uint32_t x, uint32_t y) {
  float3 _yuv = rsMatrixMultiply(&transform_matrix, (float3){in.r, in.g, in.b});
  //float _lum = _yuv.s0;
  //float _u = _yuv.s1 + 128;
  //float _v = _yuv.s2 + 128;

  float _lum = ((66 * in.r + 129 * in.g + 25 * in.b + 128) >> 8) + 16;
  float _u = ((112 * in.r - 94 * in.g - 18 * in.b + 128) >> 8) + 128;
  float _v = ((-38 * in.r - 74 * in.g + 112 * in.b + 128) >> 8) + 128;
  //数值调整
  uchar lum = _lum < 0 ? 0 : (_lum > 255 ? 255 : _lum);         //maybe overfloor
  uchar u = _u < 0 ? 0 : (_u > 255 ? 255 : _u);                 //maybe overfloor
  uchar v = _v < 0 ? 0 : (_v > 255 ? 255 : _v);                 //maybe overfloor

  uchar3 yuv = (uchar3){lum, u, v};
  rsSetElementAt_uchar(gNV21_frame, yuv.s0, x+((height-1 - y)*width));      //Y  *通过修改(y)为(height-1 - y)來实现上下翻转
  if(x%2 == 1 && y%2 == 1) {
    int index = uvStart + ((height-1 - y)/2)*width + (x & (~1));  //*通过修改(y)为(height-1 - y)來实现上下翻转
    rsSetElementAt_uchar(gNV21_frame, yuv.s1, index);          //U
    rsSetElementAt_uchar(gNV21_frame, yuv.s2, index+1);        //V
    //下面这种处理方式不对，只能显示一半
    //rsSetElementAt_uchar(gNV21_frame, yuv.s2, uvStart+(y/2)*width+x/2);    //V
    //rsSetElementAt_uchar(gNV21_frame, yuv.s1, uvStart+(y/2)*width+x/2+1);  //U
  }
}