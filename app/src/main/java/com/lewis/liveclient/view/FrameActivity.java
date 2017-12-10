package com.lewis.liveclient.view;

import com.lewis.liveclient.R;
import com.lewis.liveclient.animation.MyCountView;
import com.lewis.liveclient.view.BaseActivity;

/**
 * Created by Lewis on 2017/11/26.
 *
 */

public class FrameActivity extends BaseActivity {

    @Override
    protected void init() {
        setContentView(MyCountView.initView(this, R.layout.activity_main));
    }
}
