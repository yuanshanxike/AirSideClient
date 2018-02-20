package com.lewis.liveclient.view

import android.content.Intent
import android.os.Bundle
import com.lewis.liveclient.R
import kotlinx.android.synthetic.main.activity_main.*

/**
 * Created by lewis on 18-2-20.
 *
 */
class FrameActivity : BaseActivity() {

  override fun init() {
    setContentView(R.layout.activity_main)

    start_live.setOnClickListener {
      val intent = Intent()
      intent.setClass(this, LiveActivity::class.java)
      startActivity(intent)
    }
  }


}