package com.lewis.liveclient.view

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity

/**
 * Created by lewis on 18-2-20.
 *
 */
abstract class BaseActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    //Todo
    requestPermissions()
    init()
  }

  abstract fun init()

  fun requestPermissions() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      //camera
      if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
          !=  PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(this
            , arrayOf(Manifest.permission.CAMERA), PackageManager.PERMISSION_GRANTED)
      }
      //READ_EXTERNAL_STORAGE
      if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
          !=  PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(this
            , arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PackageManager.PERMISSION_GRANTED)
      }
      //WRITE_EXTERNAL_STORAGE
      if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
          !=  PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(this
            , arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), PackageManager.PERMISSION_GRANTED)
      }
    }
  }
}