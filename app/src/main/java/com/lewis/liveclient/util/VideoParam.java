package com.lewis.liveclient.util;

/**
 * Created by lewis on 18-3-13.
 *
 */

import android.annotation.TargetApi;
import android.hardware.Camera.Parameters;
import android.os.Environment;
import android.util.Log;
import java.util.Iterator;

public class VideoParam {
  private static final String TAG = "VideoParam";
  private static final int VIDEO_W = 640;
  private static final int VIDEO_H = 480;
  private static final int FPS = 25;
  private static final String MIME = "video/avc";
  private static final int BPS = 4194304;
  private static final int IFI = 5;
  private static final String SDCARD = Environment.getExternalStorageDirectory().getPath();
  public int[] mFpsRange;
  public String mMime = "video/avc";
  public int mBps = 4194304;
  public int mIfi = 5;

  public VideoParam() {
  }

  public int getMaxFps() {
    return this.mFpsRange != null && this.mFpsRange.length > 1?this.mFpsRange[1] / 1000:25;
  }

  @TargetApi(18)
  public void init(Parameters parameters) {
    if(parameters != null) {
      int[] fps = null;
      if(parameters.getSupportedPreviewFpsRange() != null) {
        Iterator var4 = parameters.getSupportedPreviewFpsRange().iterator();

        while(var4.hasNext()) {
          int[] f = (int[])var4.next();
          if(f[1] >= 25000) {
            fps = f;
          }
        }
      }

      if(fps == null) {
        Log.e("VideoParam", String.format("Not support fps: %d", new Object[]{Integer.valueOf(25)}));
      }

      this.mFpsRange = fps;
    }
  }
}
