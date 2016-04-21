package com.sundy.icare.icare_server.activity;

import android.os.Bundle;

import com.sundy.icare.icare_server.R;
import com.sundy.icare.icare_server.fragment.MainFragment;
import com.sundy.icare.icare_server.utils.MyUtils;

/**
 * Created by sundy on 16/4/4.
 */
public class MainActivity extends BaseActivity {

    private final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MyUtils.rtLog(TAG, "------------->onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initMainFragment();
    }

    private void initMainFragment() {
        switchContent(new MainFragment());
    }

    @Override
    public void onBackPressed() {
        onBack();
    }

}

