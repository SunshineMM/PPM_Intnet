package com.example.npttest.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.sdk.android.push.CloudPushService;
import com.alibaba.sdk.android.push.CommonCallback;
import com.alibaba.sdk.android.push.noonesdk.PushServiceFactory;
import com.daimajia.numberprogressbar.NumberProgressBar;
import com.daimajia.numberprogressbar.OnProgressBarListener;
import com.example.npttest.App;
import com.example.npttest.R;
import com.example.npttest.constant.Constant;
import com.example.npttest.manager.ActivityManager;
import com.example.npttest.server.Heartbeat;
import com.example.npttest.util.LogUtils;
import com.example.npttest.util.SPUtils;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.dmoral.toasty.Toasty;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.Response;

import static com.example.npttest.constant.Constant.AccessKeyId;
import static com.example.npttest.constant.Constant.AccessKeySecret;
import static com.example.npttest.constant.Constant.EndPoint;
import static com.example.npttest.constant.Constant.OssImgUrl;
import static com.example.npttest.constant.Constant.SecurityToken;
import static com.example.npttest.constant.Constant.ppmBucket;

/**
 * Created by Administrator on 2017/7/21.
 */

public class SpalshActivity extends AppCompatActivity implements OnProgressBarListener {
    @Bind(R.id.spalsh_refresh)
    ImageView spalshRefresh;
    @Bind(R.id.spalsh_tv)
    TextView spalshTv;
    @Bind(R.id.spalsh_qr_img)
    ImageView spalshQrImg;
    @Bind(R.id.spalsh_qr_tv)
    TextView spalshQrTv;
    @Bind(R.id.spalsh_qr_lin)
    LinearLayout spalshQrLin;
    private Timer timer;
    private NumberProgressBar bnp;
    private String id, pwd;
    private CloudPushService mPushService;
    private String username;
    private String uname, phone;
    private String localVersionName;
    private int localVersionCode;
    private ConnectServerTask connectServerTask;
    private boolean domelogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spalsh);
        ButterKnife.bind(this);
        //隐藏顶部状态栏
        View decorView = getWindow().getDecorView();
        int option = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(option);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        domelogin = (boolean) SPUtils.get(this, "domeloginboo", false);
        if (domelogin) {
            Constant.CODE = "ppm_test_parkos";
            Constant.domeLoginBoo = true;
        } else {
            Constant.CODE = (String) SPUtils.get(SpalshActivity.this, "code", "");
        }
        Constant.DSV = String.valueOf(getAPPLocalVersion(this));
        Log.e("TAG", "设备编码：" + Constant.CODE + "\n" + "设备版本：" + Constant.DSV);
        bnp = (NumberProgressBar) findViewById(R.id.pb);
        bnp.setOnProgressBarListener(this);
        mPushService = PushServiceFactory.getCloudPushService();
       /* timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        bnp.incrementProgressBy(1);
                    }
                });
            }
        }, 1000, 25);*/
        //car_depot(App.serverurl);
        bindAccount();
        ActivityManager.getInstance().addActivity(this);
        startSplashPb(1000);
        startService(new Intent(SpalshActivity.this, Heartbeat.class));
    }

    private void startSplashPb(int delay) {
        timer = new Timer();
        bnp.setProgress(0);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        bnp.incrementProgressBy(1);
                    }
                });
            }
        }, delay, 50);
    }

    @OnClick(R.id.spalsh_refresh)
    public void onViewClicked() {
        spalshRefresh.setVisibility(View.INVISIBLE);
        spalshTv.setText("正在连接服务器...");
        startSplashPb(100);
    }


    /**
     * 连接服务器任务
     */
    public class ConnectServerTask extends AsyncTask<Void, Object, Object> {


        @Override
        protected Object doInBackground(Void... params) {
            try {
                //请求服务器
                String jsonObject = "{\"cmd\":\"10\",\"type\":\"" + Constant.TYPE + "\",\"code\":\"" + Constant.CODE
                        + "\",\"pid\":\"1\",\"dsv\":\"" + Constant.DSV + "\",\"dhv\":\"121\",\"spare\":\"0\",\"sign\":\"abcd\"}";
                Log.e("TAG", jsonObject);
                Response response = OkHttpUtils.postString().url(Constant.OSURL)
                        .content(jsonObject)
                        .mediaType(MediaType.parse("application/json; charset=utf-8"))
                        .build().connTimeOut(5000).execute();
                if (response.isSuccessful()) {
                    String res = response.body().string();
                    Log.e("TAG", "连接ParkOS返回结果：" + res);
                    try {
                        JSONObject rejsonObject = new JSONObject(res);
                        int code = rejsonObject.getInt("code");
                        if (code == 100) {
                            JSONObject resultJsonObject = rejsonObject.getJSONObject("result");
                            String serverurl = resultJsonObject.getString("url");
                            Log.e("TAG", "code:" + code);
                            String surl = "http://" + serverurl + "/jcont";
                            SPUtils.put(SpalshActivity.this, Constant.URL, surl);
                            App.serverurl = (String) SPUtils.get(SpalshActivity.this, Constant.URL, "");
                            //获取oss
                            String ossjs = "{\"cmd\":\"162\",\"type\":\"" + Constant.TYPE + "\",\"code\":\"" + Constant.CODE + "\"," +
                                    "\"dsv\":\"" + Constant.DSV + "\",\"sign\":\"abcd\"}";
                            Log.e("TAG", "请求oss参数：" + ossjs);
                            Response ossResponse = OkHttpUtils.postString().url(surl)
                                    .content(ossjs)
                                    .mediaType(MediaType.parse("application/json; charset=utf-8"))
                                    .build().connTimeOut(5000).execute();
                            if (ossResponse.isSuccessful()) {
                                String ossres = ossResponse.body().string();
                                Log.e("TAG", "oss返回的结果：" + ossres);
                                JSONObject ossrejsonObject = new JSONObject(ossres);
                                int osscode = ossrejsonObject.getInt("code");
                                JSONObject resultjson = ossrejsonObject.getJSONObject("result");
                                if (osscode == 100) {
                                    JSONObject datajson = resultjson.getJSONObject("data");
                                    int qrs = datajson.getInt("qrs");
                                    if (qrs == 1) {
                                        AccessKeyId = datajson.getString("accessKeyId");
                                        AccessKeySecret = datajson.getString("accessKeySecret");
                                        SecurityToken = datajson.getString("securityToken");
                                        ppmBucket = datajson.getString("bucket");
                                        EndPoint = datajson.getString("endpoint");
                                        OssImgUrl = datajson.getString("ossImgUrl");
                                        Log.e("TAG", "AccessKeyId:" + AccessKeyId + "\n" + "AccessKeySecret:"
                                                + AccessKeySecret + "\n" + "SecurityToken:" + SecurityToken + "\n" + "EndPoint:" + EndPoint + "\n" + "OssImgUrl:" + OssImgUrl);
                                        //获取登录用户
                                        String jsons = "{\"cmd\":\"172\",\"type\":\"" + Constant.TYPE + "\",\"code\":\"" + Constant.CODE +
                                                "\",\"dsv\":\"" + Constant.DSV + "\",\"sign\":\"abcd\"}";
                                        Log.e("TAG", jsons);
                                        Response loginResponse = OkHttpUtils.postString().url(surl)
                                                .content(jsons)
                                                .mediaType(MediaType.parse("application/json; charset=utf-8"))
                                                .build().connTimeOut(5000).execute();
                                        if (loginResponse.isSuccessful()) {
                                            String loginres = loginResponse.body().string();
                                            Log.e("TAG","获取登录用户返回的结果：" + loginres);
                                            JSONObject loginreJsonObject = new JSONObject(loginres);
                                            int logincode = loginreJsonObject.getInt("code");
                                            if (logincode == 100) {
                                                return loginres;
                                            } else {
                                                return logincode;
                                            }
                                        } else {
                                            return -3;
                                        }
                                    }else {
                                        Log.e("TAG","获取Oss 失败");
                                        return -4;
                                    }
                                } else {
                                    return osscode;
                                }
                            } else {
                                return -2;
                            }
                        } else {
                            return code;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("TAG", "我是异常" + e);
                    }
                } else {
                    return -1;
                }
            } catch (IOException e) {
                Log.e("TAG", e + "");
                return -1;
            }
            return 0;
        }

        @Override
        protected void onProgressUpdate(Object... values) {
            for (Object value : values) {
                if (value instanceof String) {
                    String val = (String) value;
                }
            }
        }

        @Override
        protected void onPostExecute(final Object success) {
            connectServerTask = null;
            timer.cancel();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    bnp.setProgress(100);
                }
            });
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (success instanceof Integer) {
                Integer succ = (Integer) success;
                Log.e("TAG", succ + "我是返回的值");
                if (succ.intValue() == 0) {
                    // finish();
                    spalshTv.setText("服务器连接异常");
                    spalshRefresh.setVisibility(View.VISIBLE);
                    Log.e("TAG","其他异常");
                } else if (succ.intValue() == 501) {
                    spalshQrLin.setVisibility(View.VISIBLE);
                    TelephonyManager TelephonyMgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
                    if (ActivityCompat.checkSelfPermission(SpalshActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    String szImei = TelephonyMgr.getDeviceId();
                    String scode = "ppm" + szImei;
                    spalshQrTv.setText(scode);
                    //Content.CODE= (String) SPUtils.get(GuideActivity.this,"code","");
                    try {
                        Bitmap bitmap = CreateCode("ppm" + szImei, BarcodeFormat.QR_CODE, 256, 256);
                        spalshQrImg.setImageBitmap(bitmap);
                    } catch (WriterException e) {
                        e.printStackTrace();
                    }//设备未注册，跳转到注册页面
                    spalshTv.setText("设备未注册，请注册设备后重试");
                    spalshRefresh.setVisibility(View.VISIBLE);
                } else if (succ.intValue() == -1) {
                    spalshTv.setText("服务器连接失败");
                    spalshRefresh.setVisibility(View.VISIBLE);
                } else if (succ.intValue() == -2) {
                    spalshTv.setText("获取配置失败");
                    spalshRefresh.setVisibility(View.VISIBLE);
                } else if (succ.intValue() == -3) {
                    spalshTv.setText("获取登录用户失败");
                    spalshRefresh.setVisibility(View.VISIBLE);
                } else if (succ.intValue() == -4) {
                    startActivity(new Intent(SpalshActivity.this, LoginActivity.class));
                }else {
                    spalshTv.setText("服务器连接出错" + succ.intValue());
                    spalshRefresh.setVisibility(View.VISIBLE);
                }
            } else if (success instanceof String) {
                String sussStr = (String) success;
                JSONObject loginreJsonObject = null;
                try {
                    loginreJsonObject = new JSONObject(sussStr);
                    JSONObject loginresultjson = loginreJsonObject.getJSONObject("result");
                    JSONObject logindatajson = loginresultjson.getJSONObject("data");
                    int lrs = logindatajson.getInt("lrs");
                    //username=datajson.getString("nname");
                    App.wmon = logindatajson.getDouble("wmon");
                    Constant.wtime = logindatajson.getLong("wtime");
                    Constant.enfree=logindatajson.getInt("enFree");
                    if (lrs == 1) {
                        username = logindatajson.getString("rname");
                        uname = logindatajson.getString("uname");
                        phone = logindatajson.getString("phone");
                        SPUtils.put(SpalshActivity.this, Constant.USERNAME, username);
                        SPUtils.put(SpalshActivity.this, Constant.ID, uname);
                        SPUtils.put(SpalshActivity.this, Constant.PHONE, phone);
                        startActivity(new Intent(SpalshActivity.this, IndexActivity.class));
                        Toasty.info(SpalshActivity.this, "欢迎回来", Toast.LENGTH_SHORT, true).show();
                        finish();
                    } else {
                        // Toasty.error(SpalshActivity.this, "请检查用户名和密码", Toast.LENGTH_SHORT, true).show();
                        startActivity(new Intent(SpalshActivity.this, LoginActivity.class));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("TAG",e+"");
                }

            } else {
                LogUtils.i("未实现");
            }
        }


        @Override
        protected void onCancelled() {
            connectServerTask = null;
            timer.cancel();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    bnp.setProgress(100);
                }
            });
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                get();
            } catch (Exception e) {
                Log.e("TAG", ""+e);
            }
        }
    }

    public Bitmap CreateCode(String str, BarcodeFormat type, int bmpWidth, int bmpHeight) throws WriterException {
        Hashtable<EncodeHintType, String> mHashtable = new Hashtable<EncodeHintType, String>();
        mHashtable.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        // 生成二维矩阵,编码时要指定大小,不要生成了图片以后再进行缩放,以防模糊导致识别失败
        BitMatrix matrix = new MultiFormatWriter().encode(str, type, bmpWidth, bmpHeight, mHashtable);
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        // 二维矩阵转为�?维像素数组（�?直横�?排）
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (matrix.get(x, y)) {
                    pixels[y * width + x] = 0xff000000;
                } else {
                    pixels[y * width + x] = 0xffffffff;
                }
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        // 通过像素数组生成bitmap,具体参�?�api
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    /**
     * 绑定账户接口:CloudPushService.bindAccount调用示例
     * 1. 绑定账号后,可以在服务端通过账号进行推送
     * 2. 一个设备只能绑定一个账号
     */
    private void bindAccount() {
        final String account = Constant.CODE;
        if (account.length() > 0) {
            mPushService.bindAccount(account, new CommonCallback() {
                @Override
                public void onSuccess(String s) {
                    Log.e("TAG", "绑定成功");
                }

                @Override
                public void onFailed(String errorCode, String errorMsg) {
                    Log.e("TAG", "绑定失败");
                }
            });
        }
    }

    //请求parkOs 返回url
    //{"cmd":"10","type":"2","code":"17083B3DE","pid":"1","dsv":"110","dhv":"121","spare":"0","sign":"abcd"}
    private void geturl(final String url) {
        String jsonObject = "{\"cmd\":\"10\",\"type\":\"" + Constant.TYPE + "\",\"code\":\"" + Constant.CODE
                + "\",\"pid\":\"1\",\"dsv\":\"" + Constant.DSV + "\",\"dhv\":\"121\",\"spare\":\"0\",\"sign\":\"abcd\"}";
        Log.e("TAG", jsonObject);
        OkHttpUtils.postString().url(url)
                .content(jsonObject)
                .mediaType(MediaType.parse("application/json; charset=utf-8"))
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        Log.e("TAG", "网络错误");
                        //url_onError();
                        //Toasty.error(SpalshActivity.this,"请检查网络",Toast.LENGTH_SHORT,true).show();
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        Log.e("TAG", "url获取成功");
                        try {
                            JSONObject osjson = new JSONObject(response);
                            String reasonjson = osjson.getString("reason");
                            if (reasonjson.equals("操作成功")) {
                                JSONObject resultjson = osjson.getJSONObject("result");
                                String tourl = resultjson.getString("url");
                                Log.e("TAG", tourl);
                                //App.serverurl = "http://"+tourl+"/jcont";
                                String s = "http://" + tourl + "/jcont";
                                SPUtils.put(SpalshActivity.this, Constant.URL, s);
                                App.serverurl = (String) SPUtils.get(SpalshActivity.this, Constant.URL, "");
                                //handler.sendEmptyMessage(0x124);
                                if (App.serverurl != null) {
                                    getosskey(App.serverurl);
                                    login(s);
                                }
                                //spalsh_start();
                            } else {
                                Log.e("TAG", "操作失败");
                                Toasty.error(SpalshActivity.this, "请求服务器失败，您请求的服务器有误", Toast.LENGTH_SHORT, true).show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }


    private void getosskey(String url) {

        String ossjs = "{\"cmd\":\"162\",\"type\":\"" + Constant.TYPE + "\",\"code\":\"" + Constant.CODE + "\"," +
                "\"dsv\":\"" + Constant.DSV + "\",\"sign\":\"abcd\"}";
        Log.e("TAG", ossjs);
        OkHttpUtils.postString().url(url)
                .content(ossjs)
                .mediaType(MediaType.parse("application/json; charset=utf-8"))
                .build().execute(new StringCallback() {

            @Override
            public void onError(Call call, Exception e, int id) {
                Toast.makeText(SpalshActivity.this, "请检查网络", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(String response, int id) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String reasonjson = jsonObject.getString("reason");//操作成功
                    JSONObject resultjson = jsonObject.getJSONObject("result");
                    JSONObject datajson = resultjson.getJSONObject("data");
                    int qrs = datajson.getInt("qrs");
                    if (qrs == 1) {
                        AccessKeyId = datajson.getString("accessKeyId");
                        AccessKeySecret = datajson.getString("accessKeySecret");
                        SecurityToken = datajson.getString("securityToken");
                        ppmBucket = datajson.getString("bucket");
                        EndPoint = datajson.getString("endpoint");
                        OssImgUrl = datajson.getString("ossImgUrl");
                        Log.e("TAG", "AccessKeyId:" + AccessKeyId + "\n" + "AccessKeySecret:"
                                + AccessKeySecret + "\n" + "SecurityToken:" + SecurityToken + "\n" + "EndPoint:" + EndPoint + "\n" + "OssImgUrl:" + OssImgUrl);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onProgressChange(int current, int max) {
        /*if(current == max) {
            *//*if (aBoolean){
                autoLogin();
            }*//*
            osUrl = "http://www.parkos.cn/jcont";
            geturl(osUrl);
            timer.cancel();
        }*/

        if (current == 1 && null == connectServerTask) {
            connectServerTask = new ConnectServerTask();
            connectServerTask.execute();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timer.cancel();
    }

    //判断当前用户登录状态
    private void login(String url) {
            /*String jsons="{\"cmd\":\"149\",\"type\":\""+Constant.TYPE+"\",\"code\":\""+Constant.CODE+"\",\"dsv\":\""+Constant.DSV+"\"," +
                    "\"ltype\":\"0\",\"user\":\""+id+"\"," +
                    "\"pass\":\""+pwd+"\"," +
                    "\"sign\":\"abcd\"}";*/
        String jsons = "{\"cmd\":\"172\",\"type\":\"" + Constant.TYPE + "\",\"code\":\"" + Constant.CODE +
                "\",\"dsv\":\"" + Constant.DSV + "\",\"sign\":\"abcd\"}";
        Log.e("TAG", jsons);
        OkHttpUtils.postString().url(url)
                .content(jsons)
                .mediaType(MediaType.parse("application/json; charset=utf-8"))
                .build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                Toasty.error(SpalshActivity.this, "请检查网络", Toast.LENGTH_SHORT, true).show();
            }

            @Override
            public void onResponse(String response, int id) {
                Log.e("TAG", response);
                JSONObject rpjson = null;
                try {
                    rpjson = new JSONObject(response);
                    JSONObject resultjson = rpjson.getJSONObject("result");
                    JSONObject datajson = resultjson.getJSONObject("data");
                    int lrs = datajson.getInt("lrs");
                    //username=datajson.getString("nname");
                    App.wmon = datajson.getDouble("wmon");
                    Constant.wtime = datajson.getLong("wtime");
                    if (lrs == 1) {
                        username = datajson.getString("nname");
                        uname = datajson.getString("uname");
                        phone = datajson.getString("phone");
                        SPUtils.put(SpalshActivity.this, Constant.USERNAME, username);
                        SPUtils.put(SpalshActivity.this, Constant.ID, uname);
                        SPUtils.put(SpalshActivity.this, Constant.PHONE, phone);
                        startActivity(new Intent(SpalshActivity.this, IndexActivity.class));
                        Toasty.info(SpalshActivity.this, "欢迎回来", Toast.LENGTH_SHORT, true).show();
                        finish();
                    } else {
                        // Toasty.error(SpalshActivity.this, "请检查用户名和密码", Toast.LENGTH_SHORT, true).show();
                        startActivity(new Intent(SpalshActivity.this, LoginActivity.class));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //获取apk的版本号 currentVersionCode
    private int getAPPLocalVersion(Context ctx) {
        PackageManager manager = ctx.getPackageManager();
        PackageInfo info = null;
        try {
            info = manager.getPackageInfo(ctx.getPackageName(), 0);
            /*localVersionName = info.versionName; // 版本名
            localVersionCode = info.versionCode; // 版本号*/
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return info.versionCode;
    }
}
