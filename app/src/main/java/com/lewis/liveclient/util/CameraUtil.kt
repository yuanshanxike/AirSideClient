package com.lewis.liveclient.util

import android.content.Context
import android.graphics.ImageFormat.NV21
import android.hardware.Camera
import android.widget.Toast

/**
 * Created by lewis on 18-2-24.
 * 相机控制工具
 */
private var _camera: Camera? = null  //备用属性
val camera: Camera get() = _camera ?: throw NullPointerException()

val cameraInfo = Camera.CameraInfo()
val cameraCount = Camera.getNumberOfCameras()

const val INIT_CAMERA_ERROE = -1

fun initCamera(context: Context, isFacing: Boolean = true): Camera {
  var cameraId = if (isFacing) findFrontCamera() else findBackCamera()
  if (cameraId == INIT_CAMERA_ERROE) {
    Toast.makeText(context, "打开${if (isFacing) "前置摄像头" else "后置摄像头"}出现问题"
        , Toast.LENGTH_SHORT).show()
    cameraId = if (!isFacing) findFrontCamera() else findBackCamera()
  }
  _camera = Camera.open(cameraId)
  camera.setDisplayOrientation(90)
  val cp: Camera.Parameters = camera.parameters
  val videoParam = VideoParam()
  videoParam.init(cp)
  val sizes = cp.supportedPreviewSizes
  val size = getOptimalPreviewSizeFullScreen(sizes)

  //记录所选取的尺寸的 ratio
  previewRatio = maxOf(size.height, size.width) / minOf(size.height, size.width).toDouble()

  cp.setPreviewSize(size.width, size.height)
  cp.previewFormat = NV21    //设置成nv21是为了配合x264进行编码
  cp.setPreviewFpsRange(videoParam.mFpsRange[Camera.Parameters.PREVIEW_FPS_MIN_INDEX],
      videoParam.mFpsRange[Camera.Parameters.PREVIEW_FPS_MAX_INDEX])
  camera.parameters = cp
  return camera
}

fun findFrontCamera(): Int {
//  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//
//  } else {
    return findNeedCamera(Camera.CameraInfo.CAMERA_FACING_FRONT)
//  }
}

fun findBackCamera(): Int {
//  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//
//  } else {
    return findNeedCamera(Camera.CameraInfo.CAMERA_FACING_BACK)
//  }
}

fun findNeedCamera(flag: Int): Int {
  for (cameraIdx in 0..cameraCount) {
    Camera.getCameraInfo(cameraIdx, cameraInfo)
    if (cameraInfo.facing == flag) {
      return cameraIdx
    }
  }
  return INIT_CAMERA_ERROE
}

private const val CAMERA_RATIO_4_3 = 1.33f
private const val CAMERA_RATIO_16_9 = 1.77f

var previewRatio = CAMERA_RATIO_16_9.toDouble()

fun getOptimalPreviewSizeFullScreen(sizes: List<Camera.Size>)
    : Camera.Size {
  // Use a very small tolerance because we want an exact match.
  val ASPECT_TOLERANCE = 0.02
  var optimalSizeIndex = -1

  // Because of bugs of overlay and layout, we sometimes will try to
  // layout the viewfinder in the portrait orientation and thus get the
  // wrong size of preview surface. When we change the preview size, the
  // new overlay will be created before the old one closed, which causes
  // an exception. For now, just get the screen size.
  val candidateList: ArrayList<CandidateSize> = ArrayList()

  // Try to find an size match 16:9 ratio
  for (i in 0 until sizes.size) {
    val size: Camera.Size = sizes[i]
    val ratio = Math.max(size.width, size.height) / Math.min(size.width, size.height).toDouble()
    if (Math.abs(ratio - CAMERA_RATIO_16_9) > ASPECT_TOLERANCE)
      continue
    candidateList.add(CandidateSize(i, size))
  }

  if (candidateList.isEmpty()) {
    // Try to find an size match 4:3 ratio
    for (i in 0 until sizes.size) {
      val size: Camera.Size = sizes[i]
      val ratio = Math.max(size.width, size.height) / Math.min(size.width, size.height).toDouble()
      if (Math.abs(ratio - CAMERA_RATIO_4_3) > ASPECT_TOLERANCE)
        continue
      candidateList.add(CandidateSize(i, size))
    }
  }

  if (candidateList.isNotEmpty()) {
    candidateList.sortBy {
      -it.size.width //按宽度(竖屏时的高度)从大到小排序
    }

    for (i in 0 until candidateList.size) {
      val candidateSize = candidateList[i]
      val height = Math.max(candidateSize.size.width, candidateSize.size.height)
      val width = Math.min(candidateSize.size.width, candidateSize.size.height)
      /**
       * Preview区域宽高没必要和屏幕宽高在数值上完全相等，保持相同比例即可；
       * 否则相同比例的高分辨率PreviewSize可能会被过滤掉，影响输出照片质量；
       */
      val scaledHeight = screenWidth * height / width
      val remainH = screenHeight - scaledHeight
      if (remainH < 5.dp) {  //误差太大会导致画面变形
        optimalSizeIndex = candidateList[i].index
        break
      }
    }
  }
  return if (optimalSizeIndex == -1) sizes[0] else sizes[optimalSizeIndex]
}

private data class CandidateSize(val index: Int, val size: Camera.Size) {
  override fun toString(): String {
    return "CandidateSize{index=$index, size=$size}"
  }
}