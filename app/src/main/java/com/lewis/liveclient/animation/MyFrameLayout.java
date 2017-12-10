package com.lewis.liveclient.animation;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * Created by Lewis on 2017/11/26.
 *
 */

public class MyFrameLayout extends FrameLayout {
    //开始位置
    int startLocation = -1;
    //结束位置
    int endLocation = -1;
    //开始颜色
    int startColor = 0;
    //结束颜色
    int endColor = 0;

    public MyFrameLayout(@NonNull Context context) {
        this(context, null);
    }

    public MyFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 所有inflate执行完成后进行回调，在此处执行动画
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }
}
