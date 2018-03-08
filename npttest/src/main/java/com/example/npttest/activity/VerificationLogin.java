package com.example.npttest.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.npttest.App;
import com.example.npttest.R;
import com.example.npttest.constant.Constant;
import com.example.npttest.manager.ActivityManager;
import com.example.npttest.util.SPUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;
import com.zyao89.view.zloading.ZLoadingDialog;
import com.zyao89.view.zloading.Z_TYPE;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.dmoral.toasty.Toasty;
import okhttp3.Call;
import okhttp3.MediaType;

/**
 * Created by liuji on 2017/8/16.
 */

public class VerificationLogin extends AppCompatActivity {

    @Bind(R.id.verlogin_return)
    ImageView verloginReturn;
    @Bind(R.id.verlogin_phone_et)
    EditText verloginPhoneEt;
    @Bind(R.id.verlogin_clean_btn)
    Button verloginCleanBtn;
    @Bind(R.id.verlogin_ver_et)
    EditText verloginVerEt;
    @Bind(R.id.verlogin_getver_tv)
    TextView verloginGetverTv;
    @Bind(R.id.login_tv_verification)
    TextView loginTvVerification;
    @Bind(R.id.verlogin_login_btn)
    Button verloginLoginBtn;
    private String phone, ver, username;
    private Context mContext;
    private int countSeconds = 120;//倒计时秒数
    //记录第一次按下返回键的时间
    private long firsttime = 0;
    private ZLoadingDialog dialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.verification_login);
        //隐藏顶部状态栏
        /*View decorView = getWindow().getDecorView();
        int option = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(option);*/
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        ButterKnife.bind(this);
        mContext = this;
        ActivityManager.getInstance().addActivity(this);
        //verloginCleanBtn.setVisibility(View.GONE);
        //号码输入框内容改变事件
        verloginPhoneEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (verloginPhoneEt.getSelectionEnd() > 0) {
                    verloginCleanBtn.setVisibility(View.VISIBLE);
                } else {
                    //verloginCleanBtn.setVisibility(View.GONE);
                }
            }
        });
    }

    @OnClick({R.id.verlogin_return, R.id.verlogin_getver_tv, R.id.verlogin_login_btn, R.id.verlogin_clean_btn, R.id.login_tv_verification})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.verlogin_return:
                finish();
                break;
            case R.id.verlogin_getver_tv:
                if (CheckPhone()) {
                    String verJson = "{\"cmd\":\"146\",\"type\":\"" + Constant.TYPE + "\",\"code\":\"" + Constant.CODE + "\",\"dsv\":\"" + Constant.DSV + "\"," +
                            "\"phone\":\"" + verloginPhoneEt.getText().toString() + "\",\"vtype\":\"0\",\"sign\":\"abcd\"}";
                    if (countSeconds == 120) {
                        startCountBack();
                        getver(App.serverurl, verJson);
                        Log.e("TAG", verJson);
                    }
                }
                break;
            case R.id.verlogin_login_btn:
                String Sverlogin = "{\"cmd\":\"149\",\"type\":\"" + Constant.TYPE + "\",\"code\":\"" + Constant.CODE + "\",\"dsv\":\"" + Constant.DSV + "\"," +
                        "\"ltype\":\"2\",\"user\":\"" + verloginPhoneEt.getText().toString() + "\"," +
                        "\"pass\":\"" + verloginVerEt.getText().toString() + "\",\"sign\":\"abcd\"}";
                if (CheckPhone() && CheckVer()) {
                    verlogin(App.serverurl, Sverlogin);
                    Log.e("TAG", Sverlogin);
                }
                break;
            case R.id.verlogin_clean_btn:
                verloginPhoneEt.setText("");
                break;
            case R.id.login_tv_verification:
                finish();
                break;
        }
    }

    //检查电话号码
    public boolean CheckPhone() {
        if (TextUtils.isEmpty(verloginPhoneEt.getText().toString())) {
            verloginPhoneEt.requestFocus();
            verloginPhoneEt.setError("请输入手机号");
            return false;
        } else if (isMobileNO(verloginPhoneEt.getText().toString()) == false) {
            Toasty.error(VerificationLogin.this, "您输入的手机号有误", Toast.LENGTH_SHORT, true).show();
            return false;
        } else {
            phone = verloginPhoneEt.getText().toString();
            return true;
        }
    }

    //使用正则表达式判断电话号码
    public static boolean isMobileNO(String tel) {
        Pattern p = Pattern.compile("^(13[0-9]|15([0-3]|[5-9])|14[5,7,9]|17[1,3,5,6,7,8]|18[0-9])\\d{8}$");
        Matcher m = p.matcher(tel);
        System.out.println(m.matches() + "---");
        return m.matches();
    }

    //检查验证码
    public boolean CheckVer() {
        if (TextUtils.isEmpty(verloginVerEt.getText().toString())) {
            verloginVerEt.requestFocus();
            verloginVerEt.setError("请输入验证码");
            return false;
        } else {
            ver = verloginVerEt.getText().toString();
            return true;
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
                Toasty.error(VerificationLogin.this, "请检查网络", Toast.LENGTH_SHORT, true).show();
            }

            @Override
            public void onResponse(String response, int id) {
                Log.e("TAG", response);
                JSONObject rpjson = null;
                try {
                    rpjson = new JSONObject(response);
                    String reasonjson = rpjson.getString("reason");
                    if (reasonjson.equals("操作成功")) {
                        Toasty.info(VerificationLogin.this, "短信验证码正在发送请注意查收", Toast.LENGTH_SHORT, true).show();
                    } else {
                        Toasty.error(VerificationLogin.this, "请检查号码是否过期", Toast.LENGTH_SHORT, true).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //验证码登录{"cmd":"149","type":"2","code":"17083B3DE","dsv":"110","ltype":"3","user":"18579155820","pass":"7894","sign":"abcd"}
    private void verlogin(String url, String Sjson) {
        dialog = new ZLoadingDialog(VerificationLogin.this);
        dialog.setLoadingBuilder(Z_TYPE.LEAF_ROTATE)//设置类型STAR_LOADING 五角星
                .setLoadingColor(Color.parseColor("#55BEB7"))//颜色
                .setHintText("Loading...")
                .setHintTextColor(Color.parseColor("#55BEB7"))
                .setHintTextSize(16) // 设置字体大小 dp
                .setHintTextColor(Color.GRAY)  // 设置字体颜色
                .show();
        OkHttpUtils.postString().url(url)
                .content(Sjson)
                .mediaType(MediaType.parse("application/json; charset=utf-8"))
                .build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                Toasty.error(VerificationLogin.this, "请检查网络", Toast.LENGTH_SHORT, true).show();
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
                    username = datajson.getString("nname");
                    App.wmon = datajson.getDouble("wmon");
                    Constant.wtime = datajson.getLong("wtime");
                    SPUtils.put(VerificationLogin.this, Constant.PHONE, phone);
                    SPUtils.put(VerificationLogin.this, Constant.USERNAME, username);
                    if (lrs == 0) {
                        Constant.logintype = 1;
                        SPUtils.put(VerificationLogin.this, Constant.LOGINTYPE,1);
                        SPUtils.put(VerificationLogin.this, Constant.ID, verloginPhoneEt.getText().toString().trim());
                        Toasty.success(VerificationLogin.this, "登录成功", Toast.LENGTH_SHORT, true).show();
                        startActivity(new Intent(VerificationLogin.this, IndexActivity.class));
                        finish();
                    } else {
                        Toasty.error(VerificationLogin.this, "验证码无效或已过期", Toast.LENGTH_SHORT, true).show();
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
                verloginGetverTv.setText(countSeconds + "");
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
                verloginGetverTv.setText(countSeconds + "s");
                mCountHandler.sendEmptyMessageDelayed(0, 1000);
            } else {
                countSeconds = 120;
                verloginGetverTv.setText("获取验证码");
            }

        }
    };

    //按键按下的监听事件
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            long secondTime = System.currentTimeMillis();//第二次按下的时间
            if (secondTime - firsttime > 2000) {
                Toasty.info(VerificationLogin.this, "再按一次退出", Toast.LENGTH_SHORT, true).show();
                firsttime = System.currentTimeMillis();//记录当前按下的时间
            } else {
                //finish();
                ActivityManager.getInstance().exit();
                SPUtils.remove(VerificationLogin.this, Constant.ID);
                SPUtils.remove(VerificationLogin.this, Constant.PASS);
                finish();
            }
        }
        return false;
    }

}
