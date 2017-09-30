package com.yan.refreshloadlayouttest.testactivity;

import android.support.v7.app.AppCompatActivity;

import com.yan.refreshloadlayouttest.App;

/**
 * Created by yan on 2017/9/20.
 */

public class BaseActivity extends AppCompatActivity {
    @Override
    protected void onDestroy() {
        super.onDestroy();
//        App.getRefWatcher(getApplicationContext()).watch(this);
    }
}
