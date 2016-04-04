package com.sundy.icare.icare_server;

import android.app.Application;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.sundy.icare.icare_server.utils.LruBitmapCache;
import com.sundy.icare.icare_server.utils.MyUtils;

import cn.jpush.android.api.JPushInterface;

/**
 * Created by sundy on 16/4/4.
 */
public class MyApp extends Application {

    public static final String TAG = MyApp.class.getSimpleName();

    private RequestQueue requestQueue;
    private ImageLoader imageLoader;

    private static MyApp myApp;

    @Override
    public void onCreate() {
        super.onCreate();
        myApp = this;

        JPushInterface.setDebugMode(true);    // 设置开启日志,发布时请关闭日志
        JPushInterface.init(this);            // 初始化 JPush

        //-----------------Fresco init------------------//
        Fresco.initialize(this);
        //-----------------环信SDK init------------------//
        int pid = android.os.Process.myPid();
        String processAppName = MyUtils.getAppName(this, pid);
        // 如果app启用了远程的service，此application:onCreate会被调用2次
        // 为了防止环信SDK被初始化2次，加此判断会保证SDK被初始化1次
        // 默认的app会在以包名为默认的process name下运行，如果查到的process name不是app的process name就立即返回
        if (processAppName == null || !processAppName.equalsIgnoreCase("com.sundy.icare")) {
            MyUtils.rtLog(TAG, "enter the service process!");
            return;
        }

    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    public static synchronized MyApp getInstance() {
        return myApp;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return requestQueue;
    }

    public ImageLoader getImageLoader() {
        getRequestQueue();
        if (imageLoader == null) {
            imageLoader = new ImageLoader(this.requestQueue,
                    new LruBitmapCache());
        }
        return this.imageLoader;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (requestQueue != null) {
            requestQueue.cancelAll(tag);
        }
    }

}
