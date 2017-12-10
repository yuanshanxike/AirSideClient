package com.lewis.liveclient.animation;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Created by Lewis on 2017/11/26.
 * 控件自定义参数，对应的自定义控件为“包在ImageView、TextView、Layout等系统控件或布局外围一层的FrameLayout”
 */

public class MyParams extends FrameLayout.LayoutParams {

    public static final int left = 0x1;
    public static final int top = 0x2;
    public static final int right = 0x4;
    public static final int bottom = 0x8;

    //开始位置
    int startLocation = -1;
    //结束位置
    int endLocation = -1;
    //开始颜色
    int startColor = 0;
    //结束颜色
    int endColor = 0;

    public MyParams(@NonNull Context c, @Nullable AttributeSet attrs) {
        super(c, attrs);
    }

    public MyParams(int width, int height) {
        this(width, height, 0);
    }

    public MyParams(int width, int height, int gravity) {
        super(width, height, gravity);
    }


    /*********************set方法提供给XMLPull解析器使用***********************/
    //需要看下源码，暂不确定是否需要

    public void setStartLocation(int startLocation) {
        this.startLocation = startLocation;
    }

    public void setEndLocation(int endLocation) {
        this.endLocation = endLocation;
    }

    public void setStartColor(int startColor) {
        this.startColor = startColor;
    }

    public void setEndColor(int endColor) {
        this.endColor = endColor;
    }
}
