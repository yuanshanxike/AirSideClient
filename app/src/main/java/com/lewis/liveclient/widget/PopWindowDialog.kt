package com.lewis.liveclient.widget

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.*
import com.lewis.liveclient.R
import com.lewis.liveclient.filter.BeautyFilter
import com.lewis.liveclient.util.filter
import com.lewis.liveclient.util.screenHeight
import com.lewis.liveclient.util.screenWidth
import kotlinx.android.synthetic.main.dialog_optiions.*

/**
 * Created by lewis on 18-4-19.
 *
 */
private const val animationDuration: Long = 200

class PopWindowDialog(context: Context?, themeResId: Int) : Dialog(context, themeResId) {

  constructor(context: Context?) : this(context, R.style.Theme_AppCompat) //style

  private var contentView: View? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    window.decorView.setPadding(0, 0/*screenHeight/2*/, 0, 0)
    val param = window.attributes
    param.height = ViewGroup.LayoutParams.WRAP_CONTENT
    param.width = screenWidth
    param.gravity = Gravity.BOTTOM or Gravity.CENTER  //dialog从哪里弹出
    window.attributes = param
    setCanceledOnTouchOutside(true)
  }

  override fun setContentView(layoutResID: Int) {
    contentView = LayoutInflater.from(context).inflate(layoutResID, null)
    super.setContentView(layoutResID)
  }

  override fun setContentView(view: View?) {
    contentView = view
    super.setContentView(view)
  }

  override fun setContentView(view: View?, params: ViewGroup.LayoutParams?) {
    contentView = view
    super.setContentView(view, params)
  }

  //升起动画
  private fun animateUp() {
    contentView?.let {
      val translate = TranslateAnimation(Animation.RELATIVE_TO_SELF, 0f
          , Animation.RELATIVE_TO_SELF, 0f
          , Animation.RELATIVE_TO_SELF, 1f
          , Animation.RELATIVE_TO_SELF, 0f)
      val alpha = AlphaAnimation(0f, 1f)
      val set = AnimationSet(true)
      set.addAnimation(translate)
      set.addAnimation(alpha)
      set.interpolator = DecelerateInterpolator()
      set.duration = animationDuration
      it.startAnimation(set)
    }
  }

  //下降动画
  private fun animateDown() {
    contentView?.let {
      val translate = TranslateAnimation(Animation.RELATIVE_TO_SELF, 0f
          , Animation.RELATIVE_TO_SELF, 0f
          , Animation.RELATIVE_TO_SELF, 0f
          , Animation.RELATIVE_TO_SELF, 1f)
      val alpha = AlphaAnimation(1f, 0f)
      val set = AnimationSet(true)
      set.addAnimation(translate)
      set.addAnimation(alpha)
      set.interpolator = DecelerateInterpolator()
      set.duration = animationDuration
      set.fillAfter = true
      it.startAnimation(set)
    }
  }

  override fun show() {
    super.show()
    animateUp()
  }

  override fun dismiss() {
    animateDown()
    super.dismiss()
  }
}