package com.sundy.icare.icare_server.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.facebook.drawee.view.SimpleDraweeView;
import com.hyphenate.chat.EMClient;
import com.sundy.icare.icare_server.R;
import com.sundy.icare.icare_server.net.HttpCallback;
import com.sundy.icare.icare_server.net.ResourceTaker;
import com.sundy.icare.icare_server.ui.MCVideoView;
import com.sundy.icare.icare_server.utils.FileUtil;
import com.sundy.icare.icare_server.utils.MyConstant;
import com.sundy.icare.icare_server.utils.MyPreference;
import com.sundy.icare.icare_server.utils.MyUtils;

import org.json.JSONObject;

import cn.jpush.android.api.InstrumentedActivity;

/**
 * Created by sundy on 16/4/4.
 */
public class LoadingActivity extends InstrumentedActivity {

    private final String TAG = "LoadingActivity";
    private Handler mHandler;
    private AQuery aq;
    private TextView txt_time;
    private int second = 5;
    private SimpleDraweeView img_splash;
    private MCVideoView videoView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MyUtils.rtLog(TAG, "---------->onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        FileUtil.initSD();//初始化文件存储目录
        aq = new AQuery(this);
        aq.id(R.id.btn_skip).clicked(onClick);
        txt_time = aq.id(R.id.txt_time).getTextView();
        img_splash = (SimpleDraweeView) aq.id(R.id.img_splash).getView();
        videoView = (MCVideoView) aq.id(R.id.videoView).getView();

        //屏幕的信息
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        MyConstant.SCREEN_WIDTH = metrics.widthPixels;
        MyConstant.SCREEN_HEIGHT = metrics.heightPixels;
        MyConstant.SCREEN_DENSITY = metrics.density;

        //Save UUID
        MyPreference.saveUDID(this);

    }

    @Override
    protected void onResume() {
        MyUtils.rtLog(TAG, "---------->onResume");
        super.onResume();
        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void init() {
        try {
            second = 5;
            if (mHandler == null) {
                mHandler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);
                        if (msg.what == 1) {
                            second--;
                            txt_time.setText(second + "秒");
                            if (second == 1) {
                                second = 5;
                                if (MyPreference.isLogin(LoadingActivity.this)) {
                                    boolean isAutoLogin = MyPreference.getAutoLogin(LoadingActivity.this);
                                    if (isAutoLogin) {
                                        EMClient.getInstance().groupManager().loadAllGroups();
                                        EMClient.getInstance().chatManager().loadAllConversations();
                                        goMain();
                                    } else {
                                        goLogin();
                                    }
                                } else {
                                    goLogin();
                                }
                            } else {
                                mHandler.sendEmptyMessageDelayed(1, 1000);
                            }
                        } else if (msg.what == 2) {
                            if (MyPreference.isLogin(LoadingActivity.this)) {
                                goMain();
                            } else {
                                goLogin();
                            }
                        }
                    }
                };
            }

            ResourceTaker.getBanner(new HttpCallback<JSONObject>(this) {
                @Override
                public void callback(String url, JSONObject data, String status) {
                    super.callback(url, data, status);
                    try {
                        if (data != null) {
                            JSONObject result = data.getJSONObject("result");
                            if (result != null) {
                                String code = result.getString("code");
                                String message = result.getString("message");
                                if (code.equals("1000")) {
                                    JSONObject info = data.getJSONObject("info");
                                    if (info != null) {
                                        String type = info.getString("type");
                                        String sUrl = info.getString("url");
                                        if (type.equals("image")) {
                                            aq.id(R.id.img_splash).visible();
                                            aq.id(R.id.videoView).gone();
                                            img_splash.setImageURI(Uri.parse(sUrl));
                                        } else if (type.equals("video")) {
                                            aq.id(R.id.img_splash).gone();
                                            aq.id(R.id.videoView).visible();
                                            videoView.playVideo(LoadingActivity.this, Uri.parse(sUrl));
                                        } else {
                                            aq.id(R.id.img_splash).visible();
                                            aq.id(R.id.videoView).gone();
                                            img_splash.setImageURI(null);
                                        }
                                    }
                                }
                            }
                        }
                        mHandler.sendEmptyMessageDelayed(1, 2000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //跳转主页面
    private void goMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    //跳转登陆页
    private void goLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    private View.OnClickListener onClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_skip:
                    skipSplash();
                    break;
            }
        }
    };

    //跳过广告
    private void skipSplash() {
        try {
            if (mHandler != null) {
                mHandler.sendEmptyMessage(2);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        MyUtils.rtLog(TAG, "---------->onPause");
        super.onPause();
        try {
            if (mHandler != null) {
                mHandler.removeMessages(1);
                mHandler.sendEmptyMessage(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        MyUtils.rtLog(TAG, "---------->onDestroy");
        super.onDestroy();
        try {
            if (mHandler != null) {
                mHandler = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}