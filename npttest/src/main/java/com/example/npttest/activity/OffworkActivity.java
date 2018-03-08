package com.example.npttest.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.npttest.App;
import com.example.npttest.R;
import com.example.npttest.constant.Constant;
import com.example.npttest.tool.TimeDifferTools;
import com.example.npttest.util.MD5Utils;
import com.example.npttest.util.SPUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;
import com.zyao89.view.zloading.ZLoadingDialog;
import com.zyao89.view.zloading.Z_TYPE;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.dmoral.toasty.Toasty;
import okhttp3.Call;
import okhttp3.MediaType;

/**
 * Created by liuji on 2017/8/29.
 */

public class OffworkActivity extends Activity {
    @Bind(R.id.offwork_return)
    ImageView offworkReturn;
    @Bind(R.id.offwork_img)
    ImageView offworkImg;
    @Bind(R.id.offwork_name)
    TextView offworkName;
    @Bind(R.id.textView3)
    TextView textView3;
    @Bind(R.id.offwork_money)
    TextView offworkMoney;
    @Bind(R.id.offwork_worktime)
    TextView offworkWorktime;
    @Bind(R.id.textView4)
    TextView textView4;
    @Bind(R.id.offwork_password)
    EditText offworkPassword;
    @Bind(R.id.offwork_getver_tv)
    TextView offworkGetverTv;
    @Bind(R.id.offwork_submit)
    Button offworkSubmit;
    private ZLoadingDialog dialog;
    private String username, user;
    private int countSeconds = 120;
    private Context mContext;
    private String wtime;
    private String phone;
    private int loginType;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.offwork);
        ButterKnife.bind(this);
        mContext = this;
        loginType= (int) SPUtils.get(OffworkActivity.this,Constant.LOGINTYPE,0);
        username = (String) SPUtils.get(OffworkActivity.this, Constant.USERNAME, "");
        user = (String) SPUtils.get(OffworkActivity.this, Constant.ID, "");
        phone = (String) SPUtils.get(OffworkActivity.this, Constant.PHONE, "");
        Log.e("TAG", user);
        offworkName.setText(username);
        String wmon = String.format("%.2f", App.wmon / 100);
        offworkMoney.setText(wmon+"元");
        wtime = TimeDifferTools.getDistanceTime(Constant.wtime * 1000, gettime() * 1000);
        offworkWorktime.setText(wtime);
        if (loginType==1){
            //offworkPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            offworkPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            offworkGetverTv.setVisibility(View.VISIBLE);
            offworkPassword.setHint("验证码");
        }
    }

    private long gettime() {
        Date date = new Date();
        long time = (date.getTime() / 1000);
        //Log.e("TAG",date.getTime()/1000+"");
        return time;
    }

    @OnClick({R.id.offwork_return, R.id.offwork_submit, R.id.offwork_getver_tv})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.offwork_return:
                finish();
                break;
            case R.id.offwork_submit:
                if (TextUtils.isEmpty(offworkPassword.getText())) {
                    offworkPassword.setError("请输入密码");
                } else {
                    if (loginType==0){
                        //账号
                        String jsons = "{\"cmd\":\"150\",\"type\":\"" + Constant.TYPE + "\",\"code\":\"" + Constant.CODE + "\",\"dsv\":\"" + Constant.DSV + "\"," +
                                "\"ltype\":\"0\",\"user\":\"" + user + "\",\"pass\":\"" + MD5Utils.encode(offworkPassword.getText().toString().trim()) + "\"," +
                                "\"emon\":\"0\",\"sign\":\"abcd\"}";
                        if (App.serverurl!=null){
                            offWork(App.serverurl,jsons);
                        }
                    }else if (loginType==1){
                        //验证码
                        String jsons = "{\"cmd\":\"150\",\"type\":\"" + Constant.TYPE + "\",\"code\":\"" + Constant.CODE + "\",\"dsv\":\"" + Constant.DSV + "\"," +
                                "\"ltype\":\"5\",\"user\":\"" + phone + "\",\"pass\":\"" + offworkPassword.getText().toString().trim() + "\"," +
                                "\"emon\":\"0\",\"sign\":\"abcd\"}";
                        if (App.serverurl!=null){
                            offWork(App.serverurl,jsons);
                        }
                    }
                }
                break;
           case R.id.offwork_getver_tv:
                String verJson = "{\"cmd\":\"146\",\"type\":\"" + Constant.TYPE + "\",\"code\":\"" + Constant.CODE + "\",\"dsv\":\"" + Constant.DSV + "\"," +
                        "\"phone\":\"" + user + "\",\"vtype\":\"0\",\"sign\":\"abcd\"}";
                if (countSeconds == 120) {
                    startCountBack();
                    getver(App.serverurl, verJson);
                    Log.e("TAG", verJson);
                }
                break;
        }
    }

    //获取短信验证码{"cmd":"146","type":"2","code":"17083B3DE","dsv":"110","phone":"18579155820","vtype":"0","sign":"abcd"}
    private void getver(String url, String Sjson) {
        OkHttpUtils.postString().url(url)
                .content(Sjson)
                .mediaType(MediaType.parse("application/json; charset=utf-8"))
                .build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                Toasty.error(OffworkActivity.this, "请检查网络", Toast.LENGTH_SHORT, true).show();
            }

            @Override
            public void onResponse(String response, int id) {
                Log.e("TAG", response);
                JSONObject rpjson = null;
                try {
                    rpjson = new JSONObject(response);
                    String reasonjson = rpjson.getString("reason");
                    if (reasonjson.equals("操作成功")) {
                        Toasty.info(OffworkActivity.this, "短信验证码正在发送请注意查收", Toast.LENGTH_SHORT, true).show();
                    } else {
                        Toasty.error(OffworkActivity.this, "请检查号码是否过期", Toast.LENGTH_SHORT, true).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //获取验证码信息,进行计时操作
    private void startCountBack() {
        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                offworkGetverTv.setText(countSeconds + "");
                mCountHandler.sendEmptyMessage(0);
            }
        });
    }

    private Handler mCountHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            super.handleMessage(msg);
            if (countSeconds > 0) {
                --countSeconds;
                offworkGetverTv.setText(countSeconds + "s");
                mCountHandler.sendEmptyMessageDelayed(0, 1000);
            } else {
                countSeconds = 120;
                offworkGetverTv.setText("获取验证码");
            }

        }
    };

    //下班
    private void offWork(String url,String jsons) {
        dialog = new ZLoadingDialog(OffworkActivity.this);
        dialog.setLoadingBuilder(Z_TYPE.LEAF_ROTATE)//设置类型STAR_LOADING 五角星
                .setLoadingColor(Color.parseColor("#55BEB7"))//颜色
                .setHintText("Loading...")
                .setHintTextColor(Color.parseColor("#55BEB7"))
                .setHintTextSize(16) // 设置字体大小 dp
                .setHintTextColor(Color.GRAY)  // 设置字体颜色
                .show();

        Log.e("TAG", jsons);
        OkHttpUtils.postString().url(url)
                .content(jsons)
                .mediaType(MediaType.parse("application/json; charset=utf-8"))
                .build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                Toast.makeText(OffworkActivity.this, "请检查网络", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(String response, int id) {
                dialog.dismiss();
                Log.e("TAG", response);
                JSONObject rpjson = null;
                try {
                    rpjson = new JSONObject(response);
                    JSONObject resultjson = rpjson.getJSONObject("result");
                    JSONObject datajson = resultjson.getJSONObject("data");
                    int lrs = datajson.getInt("lrs");
                    if (lrs == 0) {
                        //SPUtils.clear(OffworkActivity.this);
                        SPUtils.remove(OffworkActivity.this, Constant.ID);
                        SPUtils.remove(OffworkActivity.this, Constant.PASS);
                        //dialog.dismiss();
                        Toasty.success(OffworkActivity.this, "下班成功", Toast.LENGTH_SHORT, true).show();
                        startActivity(new Intent(OffworkActivity.this, LoginActivity.class));
                        IndexActivity.indexActivity.finish();
                        finish();
                    } else {
                        Toasty.error(OffworkActivity.this, "您输入的密码有误", Toast.LENGTH_SHORT, true).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
