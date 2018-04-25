package com.lewis.liveclient.view

import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import com.lewis.liveclient.R
import com.lewis.liveclient.callback.OnRtmpConnectListener
import com.lewis.liveclient.filter.BeautyFilter
import com.lewis.liveclient.jniLink.LivePusher
import com.lewis.liveclient.util.*
import com.lewis.liveclient.widget.CameraView
import com.lewis.liveclient.widget.PopWindowDialog
import kotlinx.android.synthetic.main.activity_live.*
import kotlinx.android.synthetic.main.dialog_optiions.*

/**
 * Create by lewis 17-11-26
 */
var isClickSwitch = false
class LiveActivity : BaseActivity(), OnRtmpConnectListener {

  private var cameraView: CameraView? = null

  private val popWindow by lazy {
    PopWindowDialog(this)
  }

  init {
    LivePusher.listener = this
    isClickSwitch = false
  }

  override fun rtmpConnect(msg: String, code: Int) {
    Toast.makeText(this, msg + " code: $code", Toast.LENGTH_SHORT).show()
  }

  override fun init() {
    setContentView(R.layout.activity_live)
    cameraView = CameraView(this)
    root_view.addView(cameraView, 0)
//    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    supportActionBar?.hide()

    mVisible = true

    // Set up the user interaction to manually show or hide the system UI.
//    fullscreen_content.setOnClickListener { toggle() }
    cameraView?.setOnClickListener { toggle() }

    // Upon interacting with UI controls, delay any scheduled hide()
    // operations to prevent the jarring behavior of controls going away
    // while interacting with the UI.
//    dummy_button.setOnTouchListener(mDelayHideTouchListener)
    dummy_button.setOnClickListener {
      hide()
      popWindow.show()
    }

    popWindow.setContentView(R.layout.dialog_optiions)
    popWindow.blackWhiteFilter_switch.setOnCheckedChangeListener { buttonView, isChecked ->
      shaderProgramIndex = if (isChecked) 1 else 0
      isClickSwitch = false
    }
    popWindow.beautyFilter_switch.setOnCheckedChangeListener { buttonView, isChecked ->
      camera.stopPreview()
      filter = if (isChecked)
        BeautyFilter(3f)
      else
        null
      root_view.removeViewInLayout(cameraView)
      cameraView = CameraView(this)
      root_view.addView(cameraView, 0)
      root_view.invalidate()
      cameraView?.setOnClickListener { toggle() }
    }

    back.setOnClickListener { finish() }
  }

  override fun onDestroy() {
    camera.stopPreview()
    LivePusher.listener = null
    super.onDestroy()
  }

//  override fun onOptionsItemSelected(item: MenuItem?): Boolean {
//    when (item?.itemId) {
//      android.R.id.home ->
//          finish()
//    }
//    return super.onOptionsItemSelected(item)
//  }

  override fun onBackPressed() {
    finish()
  }

  private val mHideHandler = Handler()
  private val mHidePart2Runnable = Runnable {
    // Delayed removal of status and navigation bar

    // Note that some of these constants are new as of API 16 (Jelly Bean)
    // and API 19 (KitKat). It is safe to use them, as they are inlined
    // at compile-time and do nothing on earlier devices.
//    fullscreen_content.systemUiVisibility =
//        View.SYSTEM_UI_FLAG_LOW_PROFILE or
//        View.SYSTEM_UI_FLAG_FULLSCREEN or
//        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
//        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
//        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
//        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    cameraView?.systemUiVisibility =
        View.SYSTEM_UI_FLAG_LOW_PROFILE or
        View.SYSTEM_UI_FLAG_FULLSCREEN or
        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
  }
  private val mShowPart2Runnable = Runnable {
    // Delayed display of UI elements
//    supportActionBar?.show()
    topBar.visibility = View.VISIBLE
    fullscreen_content_controls.visibility = View.VISIBLE
  }
  private var mVisible: Boolean = false
  private val mHideRunnable = Runnable { hide() }
  /**
   * Touch listener to use for in-layout UI controls to delay hiding the
   * system UI. This is to prevent the jarring behavior of controls going away
   * while interacting with activity UI.
   */
  private val mDelayHideTouchListener = View.OnTouchListener { _, _ ->
    if (AUTO_HIDE) {
      delayedHide(AUTO_HIDE_DELAY_MILLIS)
    }
    false
  }

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)

    // Trigger the initial hide() shortly after the activity has been
    // created, to briefly hint to the user that UI controls
    // are available.
    delayedHide(100)
  }

  private fun toggle() {
    if (mVisible) {
      hide()
    } else {
      show()
    }
  }

  private fun hide() {
    // Hide UI first
//    supportActionBar?.hide()
    topBar.visibility = View.GONE
    fullscreen_content_controls.visibility = View.GONE
    mVisible = false

    // Schedule a runnable to remove the status and navigation bar after a delay
    mHideHandler.removeCallbacks(mShowPart2Runnable)
    mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY.toLong())
  }

  private fun show() {
    // Show the system bar
//    fullscreen_content.systemUiVisibility =
//        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
//        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
    cameraView?.systemUiVisibility =
        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
    mVisible = true

    // Schedule a runnable to display UI elements after a delay
    mHideHandler.removeCallbacks(mHidePart2Runnable)
    mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY.toLong())
  }

  /**
   * Schedules a call to hide() in [delayMillis], canceling any
   * previously scheduled calls.
   */
  private fun delayedHide(delayMillis: Int) {
    mHideHandler.removeCallbacks(mHideRunnable)
    mHideHandler.postDelayed(mHideRunnable, delayMillis.toLong())
  }

  companion object {
    /**
     * Whether or not the system UI should be auto-hidden after
     * [AUTO_HIDE_DELAY_MILLIS] milliseconds.
     */
    private val AUTO_HIDE = true

    /**
     * If [AUTO_HIDE] is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private val AUTO_HIDE_DELAY_MILLIS = 3000

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private val UI_ANIMATION_DELAY = 300
  }
}
