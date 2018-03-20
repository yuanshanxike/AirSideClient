package com.lewis.liveclient.view

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v8.renderscript.Allocation
import android.support.v8.renderscript.Element
import android.support.v8.renderscript.RenderScript
import android.support.v8.renderscript.Type
import android.util.Log
import com.lewis.liveclient.R
import com.lewis.liveclient.ScriptC_transform
import kotlinx.android.synthetic.main.activity_main.*
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.channels.FileChannel

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

      //test renderscript
//      val rs = RenderScript.create(this)
//      val options = BitmapFactory.Options()
//      options.inScaled = false //禁止缩放（从res文件下的图片文件夹获取图片资源会对图片进行不同程度的缩放）
//      val bitmap = BitmapFactory.decodeResource(resources, R.drawable.lei, options)
//      val rgbaAllocation = Allocation.createFromBitmap(rs, bitmap)
//      val script = ScriptC_transform(rs)
//      script._gNV21_frame = Allocation.createSized(rs, Element.U8(rs)
//          , bitmap.height*bitmap.width*3/2)
////      script._gNV21_frame = Allocation.createTyped(rs, Type.createX())
//      script._height = bitmap.height
//      script._width = bitmap.width
//      script._uvStart = bitmap.height * bitmap.width
//      script.forEach_rgba2nv21(rgbaAllocation)
//      //save to sdcard
////      if (!script._gNV21_frame.byteBuffer.isDirect) {
////        val bytes = script._gNV21_frame.byteBuffer.array()  //byteBuffer使用的是DirectBuffer(JVM管理的内存之外)，不能之直接用.array()这种java方法获取到数组
////        val file = File("/sdcard/img-rs")
////        if (!file.exists())
////          file.createNewFile()
////        val fos = FileOutputStream(file)
////        val bos = BufferedOutputStream(fos)
////        bos.write(bytes)
////        bos.flush()
////        fos.close()
////      } else {
////        Log.e("--->height_width: ", "${bitmap.height},  ${bitmap.width}")
////        Log.e("--->buffer_size: ", "${script._gNV21_frame.bytesSize}")
////        Log.e("--->buffer", "limit: ${script._gNV21_frame.byteBuffer.limit()}" +
////            " capcity: ${script._gNV21_frame.byteBuffer.capacity()}" +
////            " position: ${script._gNV21_frame.byteBuffer.position()}")
////        val bmp = Bitmap.createBitmap(bitmap.width, bitmap.height/4, Bitmap.Config.ARGB_8888)
////        Log.e("--->bytesize: ", "${bmp.byteCount}")
////        bmp.copyPixelsFromBuffer(script._gNV21_frame.byteBuffer)
////        bmp
////      }
//
//      val file = File("/sdcard/img-rs.nv21")
//      if (!file.exists())
//        file.createNewFile()
//      val fos = FileOutputStream(file)
//      val fileChannel = fos.channel
//      while (fileChannel.write(script._gNV21_frame.byteBuffer) != 0) {
//        Log.i("fileChannel: ", "write length > 0")
//      }
    }
  }


}