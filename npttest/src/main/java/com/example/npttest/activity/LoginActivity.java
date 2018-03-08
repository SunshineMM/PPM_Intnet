package com.example.npttest.activity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.sdk.android.push.CloudPushService;
import com.alibaba.sdk.android.push.CommonCallback;
import com.alibaba.sdk.android.push.noonesdk.PushServiceFactory;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.callback.ItemDragAndSwipeCallback;
import com.chad.library.adapter.base.listener.OnItemSwipeListener;
import com.chad.library.adapter.base.listener.SimpleClickListener;
import com.example.npttest.App;
import com.example.npttest.R;
import com.example.npttest.adapter.PopAdapter;
import com.example.npttest.constant.Constant;
import com.example.npttest.manager.ActivityManager;
import com.example.npttest.util.LogUtils;
import com.example.npttest.util.MD5Utils;
import com.example.npttest.util.SPUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;
import com.zyao89.view.zloading.ZLoadingDialog;
import com.zyao89.view.zloading.Z_TYPE;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import es.dmoral.toasty.Toasty;
import okhttp3.Call;
import okhttp3.MediaType;

import static com.example.npttest.constant.Constant.TABLE_UNAME;
import static com.example.npttest.constant.Constant.UTIME;


/**
 * Created by Administrator on 2017/7/26.
 */

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, OnItemSwipeListener {
    private EditText loginEdtId;
    private TextView loginVerification,loginDomeLogin;
    private Button loginBtnClean1;
    private EditText loginEdtPwd;
    private Button loginBtnClean2;
    private Button loginBtnEye;
    private boolean pwdflag = true;//密码是否可见flag
    private Button loginBtnLogin;
    private String id,pwd;
    //记录第一次按下返回键的时间
    private long firsttime=0;
    private ZLoadingDialog dialog;
    private Button popDown;
    private PopupWindow popupWindow;
    private PopAdapter popAdapter;
    private LinearLayout lin_ver;//浮动窗口依附布局
    private LinearLayout lin_pwd;//浮动窗口依附布局
    private LinearLayout parent,login_main;//浮动窗口依附布局
    private CloudPushService mPushService;

    private int pwidth;// 浮动宽口的宽度
    private boolean init_flag = false;// 浮动窗口显示标示符
    private List<String> list=new ArrayList<>();
    private Boolean vg_flag=true;
    private  RecyclerView recyclerView;
    private String username;
    private String uname,phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        ActivityManager.getInstance().addActivity(this);
        mPushService = PushServiceFactory.getCloudPushService();
        Constant.CODE = (String) SPUtils.get(LoginActivity.this, "code", "");
        //删除
        /*String sql_del="delete from "+TABLE_UNAME+" where "+UNAME+"= '"+"好的吧"+"'";
        //获取数据库操作对象
        Log.e("TAG","删除："+sql_del);
        SQLiteDatabase db=App.dbHelper.getWritableDatabase();
        db.execSQL(sql_del);//执行sql语句
        Toasty.success(LoginActivity.this,"删除成功",Toast.LENGTH_SHORT,true).show();
        db.close();*/
        //query_DB();
        //条件查询
    }

    private void query_DB() {
        String sql_user="select * from "+ TABLE_UNAME +" order by " + UTIME +" desc ";
        getdata(sql_user);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Log.e("TAG","onWindowFocusChanged");
       while (!init_flag){
           initView();
           initPopuWindow();
            init_flag = true;
        }
    }

    //执行sql语句
    private void getdata(String sql_user) {
        list.clear();
        // String sql_user="select * from "+ Constant.TABLE_USER;
        SQLiteDatabase sdb = App.dbHelper.getReadableDatabase();
        Cursor cursor = sdb.rawQuery(sql_user, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String s = cursor.getString(cursor.getColumnIndex(Constant.UNAME));
                Log.e("TAG", s);
                list.add(s);
            }
            recyclerView.setAdapter(popAdapter);
            cursor.close();
            sdb.close();
        }
    }


    //执行sql语句
    private void getdata_insert(String sql_user) {
        //list.clear();
        // String sql_user="select * from "+ Constant.TABLE_USER;
        SQLiteDatabase sdb = App.dbHelper.getReadableDatabase();
        Cursor cursor = sdb.rawQuery(sql_user, null);

        if (cursor.getCount() != 0) {
            String sql_update="update "+ Constant.TABLE_UNAME+" set "+ Constant.UNAME+"='"
                    +loginEdtId.getText().toString().trim()+"',"+ Constant.UTIME+"="
                    +gettime()+" where "+ Constant.UNAME+"="+"'"+loginEdtId.getText().toString().trim()+"'";
            Log.e("TAG","更新："+sql_update);
            SQLiteDatabase db=App.dbHelper.getWritableDatabase();
            db.execSQL(sql_update);
            db.close();
            //Toasty.success(LoginActivity.this,"修改成功",Toast.LENGTH_SHORT,true).show();
        }else {
            //String sql="insert into "+  Constant.TABLE_UNAME+"("+Constant.RRSTRING+") values('"+remags.getText().toString()+"')";
            String sql="insert into "+  Constant.TABLE_UNAME+"("+ Constant.UNAME+","+ Constant.UTIME+") values('"+loginEdtId.getText().toString().trim()+"',"+gettime()+")";
            //获取数据库操作对象
            Log.e("TAG","添加："+sql);
            SQLiteDatabase db=App.dbHelper.getWritableDatabase();
            db.execSQL(sql);//执行sql语句
            //Toasty.success(LoginActivity.this,"添加成功",Toast.LENGTH_SHORT,true).show();
            db.close();
        }
    }


    //获取当前时间
    private long gettime(){
        Date date=new Date();
        long time = (date.getTime()/1000);
        //Log.e("TAG",date.getTime()/1000+"");
        return time;
    }

    /**
     * 初始化浮动窗口
     */
    public void initPopuWindow() {
        // 浮动窗口的布局
        View loginwindow = getLayoutInflater().inflate(
                R.layout.popup_window, null);
        SwipeRefreshLayout swipeRefreshLayout= loginwindow.findViewById(R.id.pop_sweipeLayout);
        recyclerView= loginwindow.findViewById(R.id.pop_RecyclerView);
        swipeRefreshLayout.setEnabled(false);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //item 点击事件
        recyclerView.addOnItemTouchListener(new SimpleClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                //Toast.makeText(LoginActivity.this, ""+position, Toast.LENGTH_SHORT).show();
                //popupWindow.isShowing();
                //init_flag=true;
                loginEdtId.setText(list.get(position));

                String sql_update1="update "+ Constant.TABLE_UNAME+" set "+ Constant.UNAME+"='"
                        +loginEdtId.getText().toString().trim()+"',"+ Constant.UTIME+"="
                        +gettime()+" where "+ Constant.UNAME+"="+"'"+loginEdtId.getText().toString().trim()+"'";
                Log.e("TAG","更新："+sql_update1);
                SQLiteDatabase db=App.dbHelper.getWritableDatabase();
                db.execSQL(sql_update1);
                db.close();
                //Toasty.success(LoginActivity.this,"修改成功",Toast.LENGTH_SHORT,true).show();
            }

            @Override
            public void onItemLongClick(BaseQuickAdapter adapter, View view, int position) {

            }

            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {

            }

            @Override
            public void onItemChildLongClick(BaseQuickAdapter adapter, View view, int position) {

            }
        });
        // 初始化适配器
        popAdapter=new PopAdapter(list);
        //recyclerView.setAdapter(popAdapter);
        ItemDragAndSwipeCallback swipeCallback = new ItemDragAndSwipeCallback(popAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(swipeCallback);
        touchHelper.attachToRecyclerView(recyclerView);

        //开启滑动删除
        popAdapter.enableSwipeItem();
        swipeCallback.setSwipeMoveFlags(ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        popAdapter.setOnItemSwipeListener(this);
        // 定义一个浮动窗口，并设置
        popupWindow = new PopupWindow(loginwindow, pwidth, ActionBar.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setOutsideTouchable(false);
        popupWindow.setTouchable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                popDown.setBackgroundResource(R.mipmap.ic_down);
                lin_pwd.setVisibility(View.VISIBLE);
                lin_ver.setVisibility(View.VISIBLE);
                loginBtnLogin.setVisibility(View.VISIBLE);
                query_DB();
            }
        });
        // 初始化适配器
        //popAdapter=new PopAdapter(list);
        query_DB();
        if (list.size()!=0){
            loginEdtId.setText(list.get(0));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //隐藏顶部状态栏
        /*View decorView = getWindow().getDecorView();
        int option = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(option);*/
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
    }

    //初始化控件
    private void initView() {
        loginEdtId = (EditText) findViewById(R.id.login_edt_id);
        loginBtnClean1 = (Button) findViewById(R.id.login_btn_clean1);
        loginEdtPwd = (EditText) findViewById(R.id.login_edt_pwd);
        loginBtnClean2 = (Button) findViewById(R.id.login_btn_clean2);
        loginBtnEye = (Button) findViewById(R.id.login_btn_eye);
        loginBtnLogin = (Button) findViewById(R.id.login_btn_login);
        loginVerification= (TextView) findViewById(R.id.login_tv_verification);
        popDown= (Button) findViewById(R.id.login_btn_down);
        parent= (LinearLayout) findViewById(R.id.login_lin_username);
        lin_pwd= (LinearLayout) findViewById(R.id.login_lin_pwd);
        lin_ver= (LinearLayout) findViewById(R.id.login_lin_ver);
        loginDomeLogin=findViewById(R.id.login_tv_domelogin);
        login_main=findViewById(R.id.login_lin_main);

        loginBtnLogin.setOnClickListener(this);
        loginBtnClean1.setOnClickListener(this);
        loginBtnClean2.setOnClickListener(this);
        loginBtnEye.setOnClickListener(this);
        loginVerification.setOnClickListener(this);
        popDown.setOnClickListener(this);
        loginDomeLogin.setOnClickListener(this);
        login_main.setOnClickListener(this);

/*        loginBtnClean1.setVisibility(View.GONE);
        loginBtnClean2.setVisibility(View.GONE);
        loginBtnEye.setVisibility(View.GONE);*/

        //用户名输入框内容改变事件
        loginEdtId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //Toast.makeText(LoginActivity.this, "内容改变了beforeTextChanged", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                popupWindow.dismiss();
               // Toast.makeText(LoginActivity.this, "内容改变了onTextChanged", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                //Toast.makeText(LoginActivity.this, "内容改变了afterTextChanged", Toast.LENGTH_SHORT).show();
                popupWindow.dismiss();
                if (loginEdtId.getSelectionEnd()>0){
                    loginBtnClean1.setVisibility(View.VISIBLE);
                }else {
                    //loginBtnClean1.setVisibility(View.GONE);
                }
            }
        });

        //密码输入框监听事件
        loginEdtPwd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (loginEdtPwd.getSelectionEnd()>0){
                    loginBtnClean2.setVisibility(View.VISIBLE);
                }else {
                    //loginBtnClean2.setVisibility(View.GONE);
                }
            }
        });

        //密码框获取焦点事件
        loginEdtPwd.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b){
                    loginBtnEye.setVisibility(View.VISIBLE);
                }else {
                    //loginBtnEye.setVisibility(View.GONE);
                }
            }
        });

        // 获取地址输入框的宽度，用于创建浮动窗口的宽度
        int w = parent.getWidth();
        pwidth = w;

    }


    //检查用户名
    public boolean CheckId(){
        if (TextUtils.isEmpty(loginEdtId.getText().toString().trim())){
            loginEdtId.requestFocus();
            loginEdtId.setError("请输入用户名");
            return false;
        }else {
            id = loginEdtId.getText().toString().trim();
            return true;
        }
    }

    //检查密码
    public boolean CheckPwd(){
        if (TextUtils.isEmpty(loginEdtPwd.getText().toString())){
            loginEdtPwd.requestFocus();
            loginEdtPwd.setError("请输入密码");
            return false;
        }else {
            pwd = loginEdtPwd.getText().toString();
            return true;
        }
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

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.login_btn_login:
                if (CheckId()&&CheckPwd()){
                    getParkosUrl();
                }
                break;
            case R.id.login_btn_clean1:
                loginEdtId.setText("");
                break;
            case R.id.login_btn_clean2:
                loginEdtPwd.setText("");
                break;
            case R.id.login_btn_eye:
                loginBtnEye.setBackgroundResource(pwdflag?R.mipmap.ic_password_hide_white:R.mipmap.ic_password_display_white);
                pwdflag = !pwdflag;
                loginEdtPwd.setInputType(!pwdflag?144:129);
                loginEdtPwd.setSelection(loginEdtPwd.getText().length());
                break;
            case R.id.login_tv_verification:
                startActivity(new Intent(LoginActivity.this,VerificationLogin.class));
                //Toast.makeText(this, "稍后开放，请耐心等待更新", Toast.LENGTH_SHORT).show();
                break;
            case R.id.login_btn_down:
                if (list.size()!=0){
                    Log.e("TAG","onclick");
                    popDown.setBackgroundResource(R.mipmap.ic_up);
                    lin_pwd.setVisibility(View.INVISIBLE);
                    lin_ver.setVisibility(View.INVISIBLE);
                    loginBtnLogin.setVisibility(View.INVISIBLE);
                    vg_flag=!vg_flag;
                    if (init_flag) {
                        // 显示浮动窗口
                        popupWindow.showAsDropDown(parent, 0, -5);
                        init_flag=false;
                    }
                    //popupWindow.showAsDropDown(parent, 0, -3);
                }

                break;

            case R.id.login_tv_domelogin:
                SPUtils.put(LoginActivity.this,"domeloginboo",true);
                Constant.domeLoginBoo=true;
                getParkosUrl();
                break;

            case R.id.login_lin_main:
                popDown.setBackgroundResource(R.mipmap.ic_down);
                lin_pwd.setVisibility(View.VISIBLE);
                lin_ver.setVisibility(View.VISIBLE);
                loginBtnLogin.setVisibility(View.VISIBLE);
                break;
        }
    }

    //用户密码登录
    private void login(String url,String jsons){

        Log.e("TAG","登录参数："+jsons);
        OkHttpUtils.postString().url(url)
                .content(jsons)
                .mediaType(MediaType.parse("application/json; charset=utf-8"))
                .build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                Toasty.error(LoginActivity.this, "请检查网络", Toast.LENGTH_SHORT, true).show();
                if (dialog!=null){
                    dialog.dismiss();
                }
                Log.e("TAG","网络原因");
            }

            @Override
            public void onResponse(String response, int id) {
                if (dialog!=null){
                    dialog.dismiss();
                }
                Log.e("TAG",response);
                JSONObject rpjson = null;
                try {
                    rpjson = new JSONObject(response);
                    int code=rpjson.getInt("code");
                    if (code==100){
                        JSONObject resultjson=rpjson.getJSONObject("result");
                        JSONObject datajson = resultjson.getJSONObject("data");
                        int lrs=datajson.getInt("lrs");
                        username=datajson.getString("nname");
                        App.wmon=datajson.getDouble("wmon");
                        Constant.wtime=datajson.getLong("wtime");
                        Constant.enfree=datajson.getInt("enFree");
                        Log.e("TAG","免费放行的权限***"+Constant.enfree);
                        if (lrs==0){
                            bindAccount();
                            if (!Constant.domeLoginBoo){
                                String sql_user="select * from "+ TABLE_UNAME +" where "+ Constant.UNAME+" = "+"'"+loginEdtId.getText().toString().trim()+"'" ;
                                Log.e("TAG","条件查询："+sql_user);
                                getdata_insert(sql_user);
                            }
                            Constant.logintype=0;
                            SPUtils.put(LoginActivity.this, Constant.LOGINTYPE,0);
                            SPUtils.put(LoginActivity.this, Constant.ID,loginEdtId.getText().toString().trim());
                            SPUtils.put(LoginActivity.this, Constant.PASS,MD5Utils.encode(loginEdtPwd.getText().toString().trim()));
                            SPUtils.put(LoginActivity.this, Constant.USERNAME,username);
                            startActivity(new Intent(LoginActivity.this,IndexActivity.class));
                            Toasty.success(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT, true).show();
                            finish();
                        }else {
                            Toasty.error(LoginActivity.this, "请检查用户名和密码", Toast.LENGTH_SHORT, true).show();
                        }
                    }else {
                        Toasty.error(LoginActivity.this, "请检查该设备是否登记", Toast.LENGTH_SHORT, true).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void getParkosUrl(){
        if (!isFinishing()){
            dialog = new ZLoadingDialog(LoginActivity.this);
            dialog.setLoadingBuilder(Z_TYPE.LEAF_ROTATE)//设置类型STAR_LOADING 五角星
                    .setLoadingColor(Color.parseColor("#55BEB7"))//颜色
                    .setHintText("Loading...")
                    .setHintTextColor(Color.parseColor("#55BEB7"))
                    .setHintTextSize(16) // 设置字体大小 dp
                    .setHintTextColor(Color.GRAY)  // 设置字体颜色
                    .show();
        }
        Log.e("TAG","登录获取COde："+Constant.CODE);
        //请求服务器
        String parkosObject = "{\"cmd\":\"10\",\"type\":\"" + Constant.TYPE + "\",\"code\":\"" + Constant.CODE
                + "\",\"pid\":\"1\",\"dsv\":\"" + Constant.DSV + "\",\"dhv\":\"121\",\"spare\":\"0\",\"sign\":\"abcd\"}";
        LogUtils.e( parkosObject);
        OkHttpUtils.postString().url(Constant.OSURL)
                .content(parkosObject)
                .mediaType(MediaType.parse("application/json; charset=utf-8"))
                .build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                Toasty.error(LoginActivity.this,"连接服务器失败，请检查网络",Toast.LENGTH_SHORT,true);
            }

            @Override
            public void onResponse(String response, int id) {
                Log.e("TAG","请求parkos返回的数据"+response);
                try {
                    JSONObject osjson = new JSONObject(response);
                    String reasonjson = osjson.getString("reason");
                    if (reasonjson.equals("操作成功")) {
                        JSONObject resultjson = osjson.getJSONObject("result");
                        String tourl = resultjson.getString("url");
                        String domeUrl = "http://" + tourl + "/jcont";
                        Log.e("TAG","服务器："+domeUrl);
                        if (domeUrl!=null){
                            Log.e("TAG","jjjjjjjjjjj"+Constant.domeLoginBoo);
                            if (Constant.domeLoginBoo){
                                App.serverurl=Constant.DOMEURL;
                                SPUtils.put(LoginActivity.this, Constant.URL, Constant.DOMEURL);
                                Constant.CODE=Constant.DOMECODE;
                                Log.e("TAG","演示登录");
                                //演示登录
                                String s="{\"cmd\":\"149\",\"type\":\""+ Constant.TYPE+"\",\"code\":\""+ Constant.CODE+"\",\"dsv\":\""+ Constant.DSV+"\"," +
                                        "\"ltype\":\"0\",\"user\":\""+Constant.testusername+"\"," +
                                        "\"pass\":\""+ MD5Utils.encode(Constant.testuserpwd)+"\"," +
                                        "\"sign\":\"abcd\"}";
                                Log.e("TAG","登录参数"+s+"登录code"+Constant.CODE);
                                login(App.serverurl,s);
                            }else {
                                SPUtils.put(LoginActivity.this, Constant.URL, domeUrl);
                                App.serverurl=domeUrl;
                                Log.e("TAG","非演示登录");
                                String s="{\"cmd\":\"149\",\"type\":\""+ Constant.TYPE+"\",\"code\":\""+ Constant.CODE+"\",\"dsv\":\""+ Constant.DSV+"\"," +
                                        "\"ltype\":\"0\",\"user\":\""+loginEdtId.getText().toString().trim()+"\"," +
                                        "\"pass\":\""+ MD5Utils.encode(loginEdtPwd.getText().toString().trim())+"\"," +
                                        "\"sign\":\"abcd\"}";
                                Log.e("TAG","登录参数"+s+"登录code"+Constant.CODE);
                                login(domeUrl,s);
                            }

                        }
                    } else {
                        Toasty.error(LoginActivity.this, "请求服务器失败，您请求的服务器有误", Toast.LENGTH_SHORT, true).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //按键按下的监听事件
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode==KeyEvent.KEYCODE_BACK&&event.getAction()==KeyEvent.ACTION_DOWN){
            long secondTime=System.currentTimeMillis();//第二次按下的时间
            if (secondTime-firsttime>2000){
                Toasty.info(LoginActivity.this, "再按一次退出", Toast.LENGTH_SHORT, true).show();
                firsttime=System.currentTimeMillis();//记录当前按下的时间
            }else {
                //finish();
                ActivityManager.getInstance().exit();
                SPUtils.remove(LoginActivity.this, Constant.ID);
                SPUtils.remove(LoginActivity.this, Constant.PASS);
                finish();
            }
        }
        return false;
    }

    //点击空白处隐藏键盘
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        popDown.setBackgroundResource(R.mipmap.ic_down);
        lin_pwd.setVisibility(View.VISIBLE);
        lin_ver.setVisibility(View.VISIBLE);
        loginBtnLogin.setVisibility(View.VISIBLE);
        return super.onTouchEvent(event);
    }

    @Override
    public void onItemSwipeStart(RecyclerView.ViewHolder viewHolder, int pos) {

    }

    @Override
    public void clearView(RecyclerView.ViewHolder viewHolder, int pos) {

    }

    @Override
    public void onItemSwiped(RecyclerView.ViewHolder viewHolder, int pos) {
        String sql_del="delete from "+ Constant.TABLE_UNAME+" where "+ Constant.UNAME+"="+"'"+list.get(pos)+"'";
        Log.e("TAG",sql_del);
        SQLiteDatabase db=App.dbHelper.getWritableDatabase();
        db.execSQL(sql_del);//执行sql语句
        //Toast.makeText(LoginActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                        /*//将数据从list移除
                        list.remove(position);
                        //刷新适配器
                        popAdapter.notifyDataSetChanged();*/
                        //query_DB();
       /* popupWindow.dismiss();
        if (init_flag) {
            // 显示浮动窗口
            popupWindow.showAsDropDown(parent, 0, -5);
            init_flag=false;
        }*/
        db.close();

    }

    @Override
    public void onItemSwipeMoving(Canvas canvas, RecyclerView.ViewHolder viewHolder, float dX, float dY, boolean isCurrentlyActive) {

    }
}
