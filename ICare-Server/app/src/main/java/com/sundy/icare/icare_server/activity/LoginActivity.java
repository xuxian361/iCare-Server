package com.sundy.icare.icare_server.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.androidquery.AQuery;
import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;
import com.sundy.icare.icare_server.R;
import com.sundy.icare.icare_server.net.HttpCallback;
import com.sundy.icare.icare_server.net.ResourceTaker;
import com.sundy.icare.icare_server.ui.MyProgressDialog;
import com.sundy.icare.icare_server.utils.MyPreference;
import com.sundy.icare.icare_server.utils.MyToast;
import com.sundy.icare.icare_server.utils.MyUtils;

import org.json.JSONObject;

/**
 * Created by sundy on 16/4/4.
 */
public class LoginActivity extends BaseActivity {

    private final String TAG = "LoginActivity";
    private boolean isAutoLogin = false;
    private CheckBox cb_remember_pwd;
    private String AREA_CODE = "86";
    private LoginBroadcastReceiver loginBroadcastReceiver;
    private final String Receiver_Action = "com.sundy.icare.activity.LoginActivity";
    private MyProgressDialog progressDialog;

    public void showLoading() {
        MyUtils.rtLog(TAG, "-------->showLoading");
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        progressDialog = new MyProgressDialog(this, this.getWindow().getDecorView());
        progressDialog.show();
    }

    public void closeLoading() {
        MyUtils.rtLog(TAG, "-------->closeLoading");
        if (progressDialog != null)
            progressDialog.dismiss();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MyUtils.rtLog(TAG, "---------->onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        registerLoginReceiver();
        aq = new AQuery(this);
        init();
    }

    private void registerLoginReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Receiver_Action);
        loginBroadcastReceiver = new LoginBroadcastReceiver();
        registerReceiver(loginBroadcastReceiver, intentFilter);
    }

    private void init() {
        aq.id(R.id.txt_register).clicked(onClickListener);
        aq.id(R.id.btn_login).clicked(onClickListener);
        aq.id(R.id.txt_first_visit).clicked(onClickListener);
        aq.id(R.id.txt_forgetpwd).clicked(onClickListener);
        aq.id(R.id.edt_username).getEditText().addTextChangedListener(textWatcher);

        cb_remember_pwd = aq.id(R.id.cb_remember_pwd).getCheckBox();
        cb_remember_pwd.setOnCheckedChangeListener(checkedChangeListener);
        String phone = MyPreference.getPhone(context);
        boolean isAutoLogin = MyPreference.getAutoLogin(context);
        if (!TextUtils.isEmpty(phone)) {
            cb_remember_pwd.setChecked(isAutoLogin);
            aq.id(R.id.edt_username).getEditText().setText(phone);
            aq.id(R.id.edt_password).getEditText().setText("");
        }
    }

    private CompoundButton.OnCheckedChangeListener checkedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            switch (compoundButton.getId()) {
                case R.id.cb_remember_pwd:
                    if (b) {
                        isAutoLogin = true;
                    } else {
                        isAutoLogin = false;
                    }
                    break;
            }
        }
    };

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            if (charSequence.toString().length() == 0) {
                aq.id(R.id.btn_login).background(R.drawable.corner_bg_gray_light);
            } else {
                aq.id(R.id.btn_login).background(R.drawable.corner_btn_green);
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.txt_register:
                    goRegister();
                    break;
                case R.id.btn_login:
                    login2Server();
                    break;
                case R.id.txt_first_visit:
                    go2Main();
                    break;
                case R.id.txt_forgetpwd:
                    goForgetPwd();
                    break;
            }
        }
    };

    //登陆
    private void login2Server() {
        try {
            EditText edt_username = aq.id(R.id.edt_username).getEditText();
            final EditText edt_password = aq.id(R.id.edt_password).getEditText();

            final String username = edt_username.getText().toString();
            final String password = edt_password.getText().toString();

            if (TextUtils.isEmpty(username)) {
                MyToast.rtToast(this, getString(R.string.login_username_cannot_empty));
                return;
            }
            if (TextUtils.isEmpty(password)) {
                MyToast.rtToast(this, getString(R.string.login_password_cannot_empty));
                return;
            }

            showLoading();
            ResourceTaker.login(AREA_CODE, username, password, new HttpCallback<JSONObject>(this) {
                @Override
                public void callback(String url, JSONObject data, String status) {
                    super.callback(url, data, status);
                    closeLoading();
                    try {
                        if (data != null) {
                            JSONObject result = data.getJSONObject("result");
                            if (result != null) {
                                String code = result.getString("code");
                                String message = result.getString("message");
                                if (code.equals("1000")) {
                                    final JSONObject info = data.getJSONObject("info");
                                    if (info != null) {
                                        String easemobAccount = info.getString("easemobAccount");
                                        String easemobPassword = info.getString("easemobPassword");
                                        EMClient.getInstance().login(easemobAccount, easemobPassword, new EMCallBack() {
                                            @Override
                                            public void onSuccess() {
                                                MyUtils.rtLog(TAG, "-------->环信登陆成功");
                                                // ** 第一次登录或者之前logout后再登录，加载所有本地群和回话
                                                EMClient.getInstance().groupManager().loadAllGroups();
                                                EMClient.getInstance().chatManager().loadAllConversations();

                                                MyPreference.saveAutoLogin(LoginActivity.this, isAutoLogin);
                                                saveUserInfo(info);
                                                MyPreference.saveLoginPassword(LoginActivity.this, password);
                                                go2Main();
                                            }

                                            @Override
                                            public void onError(int i, String s) {
                                                MyUtils.rtLog(TAG, "-------->环信登陆失败");
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        MyToast.rtToast(LoginActivity.this, getString(R.string.login_fail));
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onProgress(int i, String s) {
                                                MyUtils.rtLog(TAG, "-------->环信正在登陆");
                                            }
                                        });
                                    }
                                } else {
                                    MyToast.rtToast(LoginActivity.this, message);
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //保存登陆用户信息
    private void saveUserInfo(JSONObject detail) {
        MyPreference.saveUserInfo(this, detail);
    }

    //跳转主页
    private void go2Main() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    //忘记密码
    private void goForgetPwd() {
        Intent intent = new Intent(this, ForgetPasswordActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    //注册
    private void goRegister() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    @Override
    protected void onDestroy() {
        MyUtils.rtLog(TAG, "---------->onDestroy");
        super.onDestroy();
        if (loginBroadcastReceiver != null) {
            unregisterReceiver(loginBroadcastReceiver);
        }
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    private class LoginBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String msg = intent.getStringExtra("msg");
                if (msg.equals("Register_Success")) {
                    boolean isLogin = MyPreference.isLogin(LoginActivity.this);
                    if (isLogin) {
                        finish();
                    }
                }
            }
        }
    }

}

