package com.example.npttest;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.alibaba.sdk.android.push.CloudPushService;
import com.alibaba.sdk.android.push.CommonCallback;
import com.alibaba.sdk.android.push.noonesdk.PushServiceFactory;
import com.alibaba.sdk.android.push.register.GcmRegister;
import com.alibaba.sdk.android.push.register.HuaWeiRegister;
import com.alibaba.sdk.android.push.register.MiPushRegister;
import com.example.npttest.util.DBHelper;

/**
 * Created by liuji on 2017/8/12.
 */

public class App extends Application {
    public static String serverurl;//云地址
    public static String surpluscar;//剩余车位
    public static DBHelper dbHelper = null;
    public static boolean goRefresh = true;
    public static boolean outRefresh = true;
    public static boolean chaRefresh = true;
    public static boolean pvRefresh = true;
    public static boolean zcRefresh=false;
    public static boolean mdRefresh=false;
    public static double wmon;
    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("TAG","Appcreate");
        dbHelper=new DBHelper(this);
        initCloudChannel(this);
    }

    /**
     * 初始化云推送通道
     * @param applicationContext
     */
    private void initCloudChannel(final Context applicationContext) {
        PushServiceFactory.init(applicationContext);
        final CloudPushService pushService = PushServiceFactory.getCloudPushService();
        pushService.register(applicationContext, new CommonCallback() {
            @Override
            public void onSuccess(String response) {
                Log.e("TAG", "init cloudchannel success");
                //setConsoleText("init cloudchannel success");
            }

            @Override
            public void onFailed(String errorCode, String errorMessage) {
                Log.e("TAG", "init cloudchannel failed -- errorcode:" + errorCode + " -- errorMessage:" + errorMessage);
                //setConsoleText("init cloudchannel failed -- errorcode:" + errorCode + " -- errorMessage:" + errorMessage);
            }
        });

        MiPushRegister.register(applicationContext, "XIAOMI_ID", "XIAOMI_KEY"); // 初始化小米辅助推送
        HuaWeiRegister.register(applicationContext); // 接入华为辅助推送
        GcmRegister.register(applicationContext, "send_id", "application_id"); // 接入FCM/GCM初始化推送
    }

}
