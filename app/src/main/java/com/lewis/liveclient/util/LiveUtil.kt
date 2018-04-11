package com.lewis.liveclient.util

import com.lewis.liveclient.filter.BeautyFilter
import jp.co.cyberagent.android.gpuimage.GPUImageFilter

/**
 * Created by lewis on 18-4-11.
 * 直播相关工具
 */

//RTMP地址/FMS URL
var rtmpAddress: String = "rtmp://xxxx.xxxx.com/example"

//直播码/串流码
var rtmpCode: String = "012345678abcdefg?ABCD=off&example=0"

//完整推流地址
val rtmpUrl: String by lazy {
  "$rtmpAddress/$rtmpCode"
}

//通过这个字段添加GPUImage滤镜
var filter: GPUImageFilter? = /*BeautyFilter(5f)*/null

//帧宽高的枚举
enum class SupportSize {
  VERTICAL_SD {  //纵向标清 3：4
    override val width: Int
      get() = 576
    override val height: Int
      get() = 720
  },

  VERTICAL_HD {  //纵向高清 9:16
    override val width: Int
      get() = 720
    override val height: Int
      get() = 1280
  },

  VERTICAL_FULL_HD {  //纵向1080p 9:16
    override val width: Int
      get() = 1080
    override val height: Int
      get() = 1920
  },

  HORIZONTAL_SD {  //横向标清 4：3
    override val width: Int
      get() = 720
    override val height: Int
      get() = 567
  },

  HORIZONTAL_HD {  //横向高清 16:9
    override val width: Int
      get() = 1280
    override val height: Int
      get() = 720
  },

  HORIZONTAL_FULL_HD {  //横向1080p 16:9
    override val width: Int
      get() = 1920
    override val height: Int
      get() = 1080
  };

  open val width: Int = 0
  open val height: Int = 0

//  abstract fun getFrameWidth(): Int
//  abstract fun getFrameHeight(): Int
}