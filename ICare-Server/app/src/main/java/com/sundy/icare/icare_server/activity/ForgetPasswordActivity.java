package com.sundy.icare.icare_server.activity;

import android.os.Bundle;

import com.sundy.icare.icare_server.R;
import com.sundy.icare.icare_server.fragment.ForgetPwd_MobileFragment;

/**
 * Created by sundy on 16/4/21.
 */
public class ForgetPasswordActivity extends BaseActivity {

    private final String TAG = "ForgetPasswordActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        //默认第一次装载 ForgetPwd_MobileFragment
        switchContent(new ForgetPwd_MobileFragment());
    }

}
