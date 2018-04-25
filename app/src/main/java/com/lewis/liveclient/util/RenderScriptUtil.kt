package com.lewis.liveclient.util

import android.graphics.BitmapFactory
import android.support.v8.renderscript.Allocation
import android.support.v8.renderscript.Element
import android.support.v8.renderscript.RenderScript
import android.support.v8.renderscript.Type
import android.util.Log
import com.lewis.liveclient.ScriptC_transform2YUV420p
import com.lewis.liveclient.androidApp
import java.io.File
import java.io.FileOutputStream

/**
 * Created by lewis on 18-3-19.
 * RenderScript相关控制工具
 */

private var _rs: RenderScript? = null
val rs: RenderScript get() = _rs ?: throw NullPointerException("the rs is null In RenderScriptUtil.kt")

private var _script: ScriptC_transform2YUV420p? = null
val script: ScriptC_transform2YUV420p get() = _script ?: throw NullPointerException("the script is null In RenderScriptUtil.kt")

fun initRenderScript() {
  _rs = RenderScript.create(androidApp.baseContext)
  _script = ScriptC_transform2YUV420p(rs)
}

fun getYUV420FrameBufferByRenderScript(array: ByteArray, width: Int, height: Int): ByteArray {
  val rgbaAllocationTypeBuilder = Type.Builder(rs, Element.U8_4(rs))
  //因为我所编写的renderscript中是通过x,y两个传入的坐标进行计算的，所以必须设置宽和高，否则系统当作一维数组处理，那么y就无效了
  rgbaAllocationTypeBuilder.setX(width)
  rgbaAllocationTypeBuilder.setY(height)
  val rgbaAllocation = Allocation.createTyped(rs, rgbaAllocationTypeBuilder.create())
  rgbaAllocation.copyFrom(array)
  script._gYUV420_frame = Allocation.createSized(rs, Element.U8(rs), height*width*3/2)
  script._height = height
  script._width = width
  script._vStart = height * width
  script._uStart = height * width * 5 / 4
  script.forEach_rgba2yuv420(rgbaAllocation)

  script._gYUV420_frame.byteBuffer.position(0)
  val yv12Array = ByteArray(height*width*3/2)
  script._gYUV420_frame.copyTo(yv12Array)

  return yv12Array
}

fun testRenderScriptBySaveYUVFromBuffer(array: ByteArray, width: Int, height: Int): String {
  val rgbaAllocationTypeBuilder = Type.Builder(rs, Element.U8_4(rs))
  //因为我所编写的renderscript中是通过x,y两个传入的坐标进行计算的，所以必须设置宽和高，否则系统当作一维数组处理，那么y就无效了
  rgbaAllocationTypeBuilder.setX(width)
  rgbaAllocationTypeBuilder.setY(height)
  val rgbaAllocation = Allocation.createTyped(rs, rgbaAllocationTypeBuilder.create()) /*Allocation.createSized(rs, Element.U8_4(rs), height * width)*/
  rgbaAllocation.copyFrom(array)
  script._gYUV420_frame = Allocation.createSized(rs, Element.U8(rs), height*width*3/2)
  script._height = height
  script._width = width
  script._uStart = height * width
  script._vStart = height * width * 5 / 4
  script.forEach_rgba2yuv420(rgbaAllocation)

  val path = "/sdcard/img-rs.yv12"
  val file = File(path)
  if (!file.exists())
    file.createNewFile()
  val fos = FileOutputStream(file)
  val fileChannel = fos.channel
  while (fileChannel.write(script._gYUV420_frame.byteBuffer) != 0) {
    Log.i("fileChannel: ", "write length > 0")
  }
  return path
}

fun testRenderScriptBySaveYUVFromResPicture(res: Int): String {
  val options = BitmapFactory.Options()
  options.inScaled = false //禁止缩放（从res文件下的图片文件夹获取图片资源会对图片进行不同程度的缩放）
  val bitmap = BitmapFactory.decodeResource(androidApp.resources, res/*R.drawable.lei*/, options)
  val rgbaAllocation = Allocation.createFromBitmap(rs, bitmap)
  script._gYUV420_frame = Allocation.createSized(rs, Element.U8(rs)
      , bitmap.height*bitmap.width*3/2)
  script._height = bitmap.height
  script._width = bitmap.width
  script._uStart = bitmap.height * bitmap.width
  script._vStart = bitmap.height * bitmap.width * 5 / 4
  script.forEach_rgba2yuv420(rgbaAllocation)

  val path = "/sdcard/img-rs.yv12"
  val file = File(path)
  if (!file.exists())
    file.createNewFile()
  val fos = FileOutputStream(file)
  val fileChannel = fos.channel
  while (fileChannel.write(script._gYUV420_frame.byteBuffer) != 0) {
    Log.i("fileChannel: ", "write length > 0")
  }
  return path
}