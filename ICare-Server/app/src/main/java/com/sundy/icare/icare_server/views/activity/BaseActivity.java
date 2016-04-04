package com.sundy.icare.icare_server.views.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MotionEvent;

import com.androidquery.AQuery;

/**
 * Created by sundy on 16/4/4.
 */
public class BaseActivity extends FragmentActivity {

    private final String TAG = "BaseActivity";
    protected Context context;
    protected AQuery aq;

    public BaseActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        aq = new AQuery(this);


    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }
}
