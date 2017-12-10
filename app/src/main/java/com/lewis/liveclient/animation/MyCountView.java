package com.lewis.liveclient.animation;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.lewis.liveclient.R;

/**
 * Created by Lewis on 2017/11/27.
 *
 */

public class MyCountView extends LinearLayout {
    private static final String TAG = MyCountView.class.getSimpleName();

    private Context context;

    //开始位置
    private int startLocation = -1;
    //结束位置
    private int endLocation = -1;
    //开始颜色
    private int startColor = 0;
    //结束颜色
    private int endColor = 0;

    public static MyCountView initView(Context context, int resource) {
        MyCountView view = new MyCountView(context);
        view.context = context;
        //获取XML属性
        TypedArray ta = context.obtainStyledAttributes(null, R.styleable.MyFrameLayout);
        view.startLocation = ta.getInteger(R.styleable.MyFrameLayout_startLocation, -1);
        view.endLocation = ta.getInteger(R.styleable.MyFrameLayout_endLocation, -1);
        view.startColor = ta.getInteger(R.styleable.MyFrameLayout_startColor, -2);
        view.endColor = ta.getInteger(R.styleable.MyFrameLayout_endColor, -2);
        ta.recycle();

        Log.d(TAG, "startColor: " + view.startColor + " endColor: " + view.endColor);

        //初始化布局
        LayoutInflater.from(context).inflate(resource, view);

        return view;
    }

    private MyCountView(Context context) {
        this(context, null);
    }

    private MyCountView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private MyCountView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams lp) {
        Log.d(TAG, "invoke generateLayoutParams()");
        MyParams params = (MyParams) lp;
        params.setStartLocation(startLocation);
        params.setEndLocation(endLocation);
        params.setStartColor(startColor);
        params.setEndColor(endColor);
        return super.generateLayoutParams(params /*lp*/);
    }

    @Override
    public void addView(View child) {
        MyFrameLayout frameLayout = new MyFrameLayout(context);
        frameLayout.startLocation = startLocation;
        frameLayout.endLocation = endLocation;
        frameLayout.startColor = startColor;
        frameLayout.endColor = endColor;
        frameLayout.addView(child);
        super.addView(frameLayout);
    }
}
