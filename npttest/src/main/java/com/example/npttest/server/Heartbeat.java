package com.example.npttest.server;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.npttest.App;
import com.example.npttest.broadcast.AlarmReceiver;
import com.example.npttest.constant.Constant;
import com.example.npttest.util.SPUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import okhttp3.Call;
import okhttp3.MediaType;

import static com.example.npttest.fragment.Fragment1.shengyTv;

/**
 * Created by liuji on 2017/8/19.
 */

public class Heartbeat extends Service {
    private int frequency;
    private int H_elot,H_olot;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        //String url=intent.getStringExtra("123");
        /*if (myThread==null){
            myThread=new MyThread();
            myThread.start();//启动线程
        }else if (!myThread.isAlive()){
            myThread.start();
        }
        */
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        /*new Thread(){
            @Override
            public void run() {
                super.run();
                    try {
                        handler.sendEmptyMessage(0x0124);
                        sleep(5000);
                        //Log.e("TAG","123");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
            }
        }.start();*/
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.e("TAG", "当前心跳时间" + new Date().toString());
                handler.sendEmptyMessage(0x0124);
            }

        }).start();

        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);

        int anHour = 60 * 1000; // 这是一小时的毫秒数

        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;

        Intent i = new Intent(this, AlarmReceiver.class);

        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);

        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    //获取当前时间
    private int gettime(){
        Date date=new Date();
        int time = (int) (date.getTime()/1000);
        //Log.e("TAG",date.getTime()/1000+"");
        return time;
    }

    //获取心跳 {"cmd":"128","type":"2","code":"17083B3DE","dsv":"110","dtime":"1503122392","spare":"0","sign":"abcd"}

    private void getheart(String url){
        String Sjson="{\"cmd\":\"128\",\"type\":\""+ Constant.TYPE+"\",\"code\":\""+ Constant.CODE+"\",\"dsv\":\""+ Constant.DSV+"\",\"dtime\":\""+gettime()+"\",\"spare\":\"0\",\"sign\":\"abcd\"}";
        Log.e("TAG",Sjson);
        OkHttpUtils.postString().url(url)
                .content(Sjson)
                .mediaType(MediaType.parse("application/json; charset=utf-8"))
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        //Toast.makeText(Heartbeat.this, frequency+"", Toast.LENGTH_SHORT).show();
                            //Toasty.error(Heartbeat.this, "您的网络可能出了一点问题", Toast.LENGTH_SHORT, true).show();
                            Log.e("TAG","您的网络可能出了一点问题");
                    }
                    @Override
                    public void onResponse(String response, int id) {
                        JSONObject jsonObject = null;
                        try {
                            jsonObject = new JSONObject(response);
                            String reasonjson = jsonObject.getString("reason");
                            if (reasonjson.equals("操作成功")) {
                                JSONObject resultjson = jsonObject.getJSONObject("result");
                                JSONObject datajson = resultjson.getJSONObject("data");
                                H_elot= datajson.getInt("elot");
                                H_olot= datajson.getInt("olot");
                                Log.e("TAG", response);
                                Log.e("TAG", datajson.toString());
                                //Log.e("TAG", "成功");
                                handler.sendEmptyMessage(0x0123);
                            } else {
                                Log.e("TAG","连接已断开");
                                frequency++;
                                if (frequency==3){
                                    //Toast.makeText(Heartbeat.this, "您的网络可能出了一点问题", Toast.LENGTH_SHORT).show();
                                    //Toasty.error(Heartbeat.this, "您的网络可能出了一点问题", Toast.LENGTH_SHORT, true).show();
                                    frequency=0;
                                }
                                //Toasty.error(Heartbeat.this, "连接已断开", Toast.LENGTH_SHORT, true).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                });
    }


    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what==0x0123){
                //Toast.makeText(Heartbeat.this, "心跳", Toast.LENGTH_SHORT).show();
                Log.e("TAG","心跳赋值");
                //IndexActivity.errorview.setVisibility(View.INVISIBLE);
                if (shengyTv!=null){
                    shengyTv.setText(String.valueOf(H_elot+H_olot));
                    App.surpluscar= String.valueOf(H_elot+H_olot);
                }

            }else if (msg.what==0x0124){
                String s= (String) SPUtils.get(Heartbeat.this, Constant.URL,"");
                if (s!=null){
                    Log.e("TAG","心跳服务器地址："+s);
                    getheart(s);
                }
            }
        }
    };
}
