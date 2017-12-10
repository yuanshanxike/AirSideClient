package com.lewis.liveclient.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.lewis.liveclient.Annotation.AndroidAnnotation;

/**
 * Created by Lewis on 2017/11/26.
 *
 */

public abstract class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        try {
            AndroidAnnotation.init(this);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * setContentView(R.layout.activity_my)在此方法中进行
     */
    protected abstract void init();
}
