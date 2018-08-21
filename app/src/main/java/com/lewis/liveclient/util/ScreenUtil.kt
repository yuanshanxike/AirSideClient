package com.lewis.liveclient.util

import android.app.Activity
import android.graphics.Point
import com.lewis.liveclient.androidApp
import java.lang.reflect.Type

/**
 * Created by lewis on 18-2-24.
 * 屏幕尺寸(单位转换)工具
 */

//var activity: Activity? = null

//private val point: Point by lazy {
//  activity?.let {
//    return@lazy getDefaultDisplaySize(it, Point())
//  } ?: throw ScreenUtilError(Point::class.java)
//}

//val screenHeight by lazy {
//  Math.max(point.x, point.y)
//}
//
//val screenWidth by lazy {
//  Math.min(point.x, point.y)
//}

//private fun getDefaultDisplaySize(activity: Activity, size: Point): Point {
//  val display = activity.windowManager.defaultDisplay
//  display.getSize(size)
//  return size
//}

private class ScreenUtilError(type: Type)
  : Exception("error for \' ${type.javaClass.simpleName} \' in ScreenUtil file")


/*******************************expand*******************************/
val displayMetrics = androidApp.resources.displayMetrics!!
val displayDensity get() = displayMetrics.density
inline val Int.dpFloat: Float get() = displayDensity * this + 0.5F
inline val Float.dpFloat: Float get() = displayDensity * this + 0.5F
inline val Int.dp: Int get() = (displayDensity * this + 0.5F).toInt()
inline val Float.dp: Int get() = (displayDensity * this + 0.5F).toInt()

inline val Int.sp: Float get() = (displayMetrics.scaledDensity * this + 0.5F)
inline val Float.sp: Float get() = (displayMetrics.scaledDensity * this + 0.5F)

val screenHeight get() = displayMetrics.heightPixels
val screenWidth get() = displayMetrics.widthPixels

val screenRatio = maxOf(screenHeight, screenWidth) / minOf(screenHeight, screenWidth).toDouble()