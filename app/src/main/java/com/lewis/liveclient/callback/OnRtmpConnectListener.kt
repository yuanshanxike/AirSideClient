package com.lewis.liveclient.callback

/**
 * Created by lewis on 18-3-28.
 */
interface OnRtmpConnectListener {

  fun rtmpConnect(msg: String, code: Int)

}