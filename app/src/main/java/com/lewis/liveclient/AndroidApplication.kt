package com.lewis.liveclient

import android.app.Application
import android.content.Context
import com.squareup.leakcanary.LeakCanary

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

    //LeakCanary
    if (LeakCanary.isInAnalyzerProcess(this)) {
      // This process is dedicated to LeakCanary for heap analysis.
      // You should not init your app in this process.
      return
    }
    LeakCanary.install(this)
  }
}