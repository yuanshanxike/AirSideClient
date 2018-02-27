package com.lewis.liveclient

import android.app.Application
import android.content.Context

/**
 * Created by lewis on 18-2-24.
 *
 */
private var _androidApp: AndroidApplication? = null   //备用属性
val androidApp: AndroidApplication by lazy { _androidApp!! }

class AndroidApplication : Application() {

  override fun onCreate() {
    super.onCreate()
    _androidApp = this
  }
}