#pragma version(1)
#pragma rs java_package_name(com.lewis.liveclient)
#pragma rs_fp_full //use IEEE 754-2008 standard

rs_allocation gYUV420_frame;        //output: yuv420

int width;
int height;
int uStart;
int vStart;


void RS_KERNEL rgba2yuv420(uchar4 in, uint32_t x, uint32_t y) {
  float _lum = ((66 * in.r + 129 * in.g + 25 * in.b + 128) >> 8) + 16;
  float _u = ((112 * in.r - 94 * in.g - 18 * in.b + 128) >> 8) + 128;
  float _v = ((-38 * in.r - 74 * in.g + 112 * in.b + 128) >> 8) + 128;
  //数值调整
  uchar lum = _lum < 0 ? 0 : (_lum > 255 ? 255 : _lum);         //maybe overfloor
  uchar u = _u < 0 ? 0 : (_u > 255 ? 255 : _u);                 //maybe overfloor
  uchar v = _v < 0 ? 0 : (_v > 255 ? 255 : _v);                 //maybe overfloor

  uchar3 yuv = (uchar3){lum, u, v};
  rsSetElementAt_uchar(gYUV420_frame, yuv.s0, x+((height-1 - y)*width));      //Y  *通过修改(y)为(height-1 - y)來实现上下翻转
  if(x%2 == 1 && y%2 == 1) {
    int index_u = uStart + ((height-1 - y)/2)*(width/2) + x/2;  //*通过修改(y)为(height-1 - y)來实现上下翻转
    rsSetElementAt_uchar(gYUV420_frame, yuv.s1, index_u);          //U
    int index_v = vStart + ((height-1 - y)/2)*(width/2) + x/2;
    rsSetElementAt_uchar(gYUV420_frame, yuv.s2, index_v);          //V
  }
}