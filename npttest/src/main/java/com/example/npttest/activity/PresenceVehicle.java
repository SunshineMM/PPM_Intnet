package com.example.npttest.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.SimpleClickListener;
import com.example.npttest.App;
import com.example.npttest.R;
import com.example.npttest.adapter.PresenceVehicleAdapter;
import com.example.npttest.constant.Constant;
import com.example.npttest.entity.MyJson;
import com.example.npttest.entity.Prese;
import com.example.npttest.view.CustomLoadMoreView;
import com.google.gson.Gson;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;
import com.zyao89.view.zloading.ZLoadingDialog;
import com.zyao89.view.zloading.Z_TYPE;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.MediaType;

public class PresenceVehicle extends Activity implements SwipeRefreshLayout.OnRefreshListener {
    private static final int PAGE_SIZE = 6;
    private static final int TOTAL_COUNTER = 100;
    private String carnum;
    private int cartype;
    private int cdtp;
    private String comfirmYy;
    private int ctype;
    private int delayMillis = 1000;
    private ZLoadingDialog dialog;
    private ZLoadingDialog dialog1;
    private Gson gson = new Gson();
    private int index = 0;
    private boolean isErr = false;
    private String jfType;
    private int mCurrentCounter = 0;
    private boolean mLoadMoreEndGone = false;
    SynthesizerListener mSynListener;
    private MyJson myJson = new MyJson();
    @Bind({R.id.prece_return})
    ImageView preceReturn;
    @Bind({R.id.prece_rv_list})
    RecyclerView preceRvList;
    @Bind({R.id.prece_search})
    ImageView preceSearch;
    @Bind({R.id.prece_swipeLayout})
    SwipeRefreshLayout preceSwipeLayout;
    private int preson;
    private int ptype;
    private int rstat;
    private String sid;
    private PresenceVehicleAdapter vehicleAdapter;
    private List<Prese> list = new ArrayList();
    private int mdposition,coutposition;

    protected void onCreate(@Nullable Bundle paramBundle) {
        super.onCreate(paramBundle);
        setContentView(R.layout.presence_vehicle);
        ButterKnife.bind(this);
        SpeechUtility.createUtility(this, "appid=59df2c0c");
        initAdapter();
        preceSwipeLayout.setOnRefreshListener(this);
        preceSwipeLayout.setColorSchemeResources(R.color.colorPrimaryDark);
        preceRvList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        if (!isFinishing()) {
            dialog = new ZLoadingDialog(PresenceVehicle.this);
            dialog.setLoadingBuilder(Z_TYPE.LEAF_ROTATE)//设置类型STAR_LOADING 五角星
                    .setLoadingColor(Color.parseColor("#55BEB7"))//颜色
                    .setHintText("Loading...")
                    .setHintTextColor(Color.parseColor("#55BEB7"))
                    .setHintTextSize(16) // 设置字体大小 dp
                    .setHintTextColor(Color.GRAY) // 设置字体颜色
                    //.setCanceledOnTouchOutside(false)
                    .show();
        }
        getData();
    }
    //车辆出场
    private void carout(String url) {
        Log.e("TAG", "多少次");
        dialog1 = new ZLoadingDialog(PresenceVehicle.this);
        dialog1.setLoadingBuilder(Z_TYPE.LEAF_ROTATE)//设置类型STAR_LOADING 五角星
                .setLoadingColor(Color.parseColor("#55BEB7"))//颜色
                .setHintText("Loading...")
                .setHintTextColor(Color.parseColor("#55BEB7"))
                .setHintTextSize(16) // 设置字体大小 dp
                .setHintTextColor(Color.GRAY)  // 设置字体颜色
                .show();
        //{"cmd":"140","type":"2","code":"17083B3DE","dsv":"110","ptype":"0","io":"0",
        // "num":"京B1FL39","ctype":"2","spare":"0","sign":"abcd"}
        /*String intocar_jS = "{\"cmd\":\"140\",\"type\":\"" + Constant.TYPE + "\",\"code\":\"" + Constant.CODE + "\"," +
                "\"dsv\":\"" + Constant.DSV + "\",\"ptype\":\"0\",\"io\":\"1\",\"num\":\"" + carnum + "\"," +
                "\"ctype\":\"" + cartype + "\",\"spare\":\"0\",\"sign\":\"abcd\"}";*/

        String intocar_jS ="{\"cmd\":\"140\",\"type\":\""+ Constant.TYPE+"\",\"code\":\""+ Constant.CODE+"\",\"dsv\":\""
                + Constant.DSV+"\",\"ptype\":\"6\",\"io\":\"1\",\"num\":\""+sid+"\",\"ctype\":\""+cartype+
                "\",\"muna\":\"1\",\"spare\":\"0\",\"sign\":\"abcd\"}";

        Log.e("TAG", intocar_jS);
        OkHttpUtils.postString().url(url)
                .content(intocar_jS)
                .mediaType(MediaType.parse("application/json; charset=utf-8"))
                .build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                Log.e("TAG", "无网络");
            }

            @Override
            public void onResponse(String response, int id) {
                Log.e("TAG", "成功");
                Log.e("TAG", response);
                dialog1.dismiss();
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String reasonjson = jsonObject.getString("reason");//操作成功
                    JSONObject resultjson = jsonObject.getJSONObject("result");
                    JSONObject datajson = resultjson.getJSONObject("data");
                    String carnum1 = datajson.getString("num");//车牌号
                    rstat = datajson.getInt("rstat");//放行状态
                    //rstat=2;
                    ptype = datajson.getInt("ptype");//凭证类型（卡或车牌）
                    ctype = datajson.getInt("ctype");//车辆类型
                    cdtp = datajson.getInt("cdtp");//计费类型（月票车）
                    long ctime = datajson.getLong("ctime");//计费时间
                    preson = datajson.getInt("preson");//原因
                    long itime = datajson.getLong("itime");//入场时间
                    double nmon = datajson.getDouble("nmon");//应收金额
                    double rmon = datajson.getDouble("rmon");//实收金额
                    double smon = datajson.getDouble("smon");//折扣金额
                    String sid = datajson.getString("sid");
                    //判断
                    jfjudge();
                    if (rstat == 0) {
                        carout_start_voice();
                        //自动放行
                        Intent zdintent = new Intent(PresenceVehicle.this, CaroutSuccessful.class);
                        zdintent.putExtra("carnum", carnum);
                        zdintent.putExtra("jfType", jfType);
                        zdintent.putExtra("ctype", ctype);
                        zdintent.putExtra("ctime", ctime);
                        zdintent.putExtra("itime", itime);
                        zdintent.putExtra("pvrefresh", true);
                        startActivity(zdintent);
                        //Toast.makeText(InputCarnum.this, "自动放行", Toast.LENGTH_SHORT).show();
                    } else if (rstat == 1) {
                        //确认放行
                        confirmjudge();
                        Intent qrintent = new Intent(PresenceVehicle.this, CaroutConfirmPass.class);
                        qrintent.putExtra("carnum", carnum);
                        qrintent.putExtra("jfType", jfType);
                        qrintent.putExtra("ctype", ctype);
                        qrintent.putExtra("itime", itime);
                        qrintent.putExtra("ctime", ctime);
                        qrintent.putExtra("comfirmYy", comfirmYy);
                        qrintent.putExtra("sid", sid);
                        qrintent.putExtra("cdtp", cdtp);
                        qrintent.putExtra("pvrefresh", true);
                        startActivity(qrintent);

                    } else if (rstat == 2) {
                        //收费放行
                        chargejudge();
                        Intent sfintent = new Intent(PresenceVehicle.this, CaroutChargeActivity.class);
                        sfintent.putExtra("carnum", carnum);
                        sfintent.putExtra("jfType", jfType);
                        sfintent.putExtra("ctype", ctype);
                        sfintent.putExtra("itime", itime);
                        sfintent.putExtra("ctime", ctime);
                        sfintent.putExtra("comfirmYy", comfirmYy);
                        sfintent.putExtra("nmon", nmon);
                        sfintent.putExtra("rmon", rmon);
                        sfintent.putExtra("smon", smon);
                        sfintent.putExtra("sid", sid);
                        sfintent.putExtra("cdtp", cdtp);
                        sfintent.putExtra("pvrefresh", true);
                        startActivity(sfintent);
                    } else if (rstat == 3) {
                        //禁止通行
                        prohibitjudge();
                        Intent jzintent = new Intent(PresenceVehicle.this, ProhibitPass.class);
                        jzintent.putExtra("carnum", carnum);
                        jzintent.putExtra("jfType", jfType);
                        jzintent.putExtra("ctype", ctype);
                        jzintent.putExtra("comfirmYy", comfirmYy);
                        startActivity(jzintent);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        });
    }

    private void carout_start_voice() {
        //1.创建SpeechSynthesizer对象, 第二个参数：本地合成时传InitListener
        SpeechSynthesizer mTts = SpeechSynthesizer.createSynthesizer(this, null);
        //2.合成参数设置，详见《科大讯飞MSC API手册(Android)》SpeechSynthesizer 类
        mTts.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan");//设置发音人
        mTts.setParameter(SpeechConstant.SPEED, "60");//设置语速
        mTts.setParameter(SpeechConstant.VOLUME, "100");//设置音量，范围0~100
        //mTts.setParameter(SpeechConstant.PITCH, "50");// 设置音调
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD); //设置云端

        //设置合成音频保存位置（可自定义保存位置），保存在“./sdcard/iflytek.pcm”
        //保存在SD卡需要在AndroidManifest.xml添加写SD卡权限
        //如果不需要保存合成音频，注释该行代码
        // mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, "./sdcard/iflytek.pcm");

        //3.开始合成
        char[] carnumber = carnum.toCharArray();
        if (carnumber.length == 7) {
            mTts.startSpeaking(String.valueOf(carnumber[0]) + " " + String.valueOf(carnumber[1]) + " " + String.valueOf(carnumber[2])
                    + " " + String.valueOf(carnumber[3]) + " " + String.valueOf(carnumber[4]) + " " + String.valueOf(carnumber[5]) + " " + String.valueOf(carnumber[6]) + " " + "出场成功！", mSynListener);
        } else if (carnumber.length == 8) {
            mTts.startSpeaking(String.valueOf(carnumber[0]) + " " + String.valueOf(carnumber[1]) + " " + String.valueOf(carnumber[2])
                    + " " + String.valueOf(carnumber[3]) + " " + String.valueOf(carnumber[4]) + " " + String.valueOf(carnumber[5]) + " " + String.valueOf(carnumber[6]) + " " + String.valueOf(carnumber[7]) + " " + "出场成功！", mSynListener);
        }

        //合成监听器
        mSynListener = new SynthesizerListener() {
            //会话结束回调接口，没有错误时，error为null
            public void onCompleted(SpeechError error) {
                System.out.println("error--------" + error);
            }

            //缓冲进度回调
            //percent为缓冲进度0~100，beginPos为缓冲音频在文本中开始位置，endPos表示缓冲音频在文本中结束位置，info为附加信息。
            public void onBufferProgress(int percent, int beginPos, int endPos, String info) {
            }

            //开始播放
            public void onSpeakBegin() {
                System.out.println("开始播放");
            }

            //暂停播放
            public void onSpeakPaused() {
            }

            //播放进度回调
            //percent为播放进度0~100,beginPos为播放音频在文本中开始位置，endPos表示播放音频在文本中结束位置.
            public void onSpeakProgress(int percent, int beginPos, int endPos) {
            }

            //恢复播放回调接口
            public void onSpeakResumed() {
            }

            //会话事件回调接口
            public void onEvent(int arg0, int arg1, int arg2, Bundle arg3) {
            }

        };
    }

    private void getData(){
        OkHttpUtils
                .postString()
                .url(App.serverurl)
                .content(gson.toJson(myJson))
                .mediaType(MediaType.parse("application/json; charset=utf-8"))
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        vehicleAdapter.loadMoreEnd(false);
                    }

                    @Override
                    public void onResponse(String paramAnonymousString, int paramAnonymousInt) {
                        if (dialog != null) {
                            dialog.cancel();
                        }
                        Log.e("TAG", gson.toJson(PresenceVehicle.this.myJson));
                        try {
                            JSONObject JSONObject = new JSONObject(paramAnonymousString);
                            ArrayList arrayList = new ArrayList();
                            JSONArray JSONArray = JSONObject.getJSONObject("result").getJSONObject("data").getJSONArray("inners");
                            //获取的数据放在集合里
                            for (int i=0;i<JSONArray.length();i++) {
                                arrayList.add(gson.fromJson(JSONArray.get(i).toString(), Prese.class));
                            }
                            if (arrayList.size() > 0) {
                                vehicleAdapter.addData(arrayList);
                                vehicleAdapter.loadMoreComplete();
                                return;
                            }
                            vehicleAdapter.loadMoreEnd(false);
                            return;
                        } catch (JSONException localJSONException) {
                            localJSONException.printStackTrace();
                            vehicleAdapter.loadMoreEnd(false);
                        }
                    }
                });
    }

    private void getData1(){
        OkHttpUtils
                .postString()
                .url(App.serverurl)
                .content(gson.toJson(myJson))
                .mediaType(MediaType.parse("application/json; charset=utf-8"))
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        vehicleAdapter.loadMoreEnd(false);
                    }

                    @Override
                    public void onResponse(String paramAnonymousString, int paramAnonymousInt) {
                        if (dialog != null) {
                            dialog.cancel();
                        }
                        Log.e("TAG", gson.toJson(PresenceVehicle.this.myJson));
                        try {
                            JSONObject JSONObject = new JSONObject(paramAnonymousString);
                            ArrayList arrayList = new ArrayList();
                            JSONArray JSONArray = JSONObject.getJSONObject("result").getJSONObject("data").getJSONArray("inners");
                            //获取的数据放在集合里
                            for (int i=0;i<JSONArray.length();i++) {
                                arrayList.add(gson.fromJson(JSONArray.get(i).toString(), Prese.class));
                            }
                            if (arrayList.size() > 0) {
                                vehicleAdapter.addData(arrayList);
                                vehicleAdapter.loadMoreComplete();
                                return;
                            }
                            vehicleAdapter.loadMoreEnd(false);
                            return;
                        } catch (JSONException localJSONException) {
                            localJSONException.printStackTrace();
                            vehicleAdapter.loadMoreEnd(false);
                        }
                    }
                });
    }

    public void onRefresh() {
        Log.e("TAG", "刷新***"+App.zcRefresh);
        vehicleAdapter.setEnableLoadMore(false);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                //arrayList.clear();

                /*list.clear();
                myJson.setIndex(0);
                getData();
                if (!App.zcRefresh){
                    vehicleAdapter.notifyDataSetChanged();//防止闪退关键句
                    App.zcRefresh=false;
                }else {
                    App.zcRefresh=false;
                }*/

                if (!App.zcRefresh){
                    Log.e("TAG","手动刷新");
                    list.clear();
                    myJson.setIndex(0);
                    getData();
                    vehicleAdapter.notifyDataSetChanged();//防止闪退关键句
                    App.zcRefresh=false;
                }else {
                    if (App.mdRefresh){
                        Log.e("TAG","修改车牌刷新***item+"+mdposition+"修改后的车牌"+ModifyCarnum.mdcarnum);
                        App.mdRefresh=false;
                        vehicleAdapter.getData().get(mdposition).setPnum(ModifyCarnum.mdcarnum);
                        vehicleAdapter.notifyDataSetChanged();
                        App.zcRefresh=false;
                    }else {
                        Log.e("TAG","出场自动刷新***item+"+coutposition);
                        App.zcRefresh=false;
                        list.remove(coutposition);
                        vehicleAdapter.notifyItemRemoved(coutposition);
                        //vehicleAdapter.notifyDataSetChanged();
                    }
                }

                //vehicleAdapter.notifyItemRemoved(2);
                //vehicleAdapter.notifyItemChanged(2);


                //PresenceVehicle.access$2302(PresenceVehicle.this, 6);
                if (preceSwipeLayout != null) {
                    preceSwipeLayout.setRefreshing(false);
                }
                vehicleAdapter.setEnableLoadMore(true);
            }
        }, this.delayMillis);
    }

    private void initAdapter() {
        vehicleAdapter = new PresenceVehicleAdapter(list);
        vehicleAdapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
            public void onLoadMoreRequested() {
                myJson.setIndex(myJson.getIndex()+1);
                getData();
            }
        }, preceRvList);
        vehicleAdapter.setLoadMoreView(new CustomLoadMoreView());
        preceRvList.setAdapter(vehicleAdapter);
        preceRvList.addOnItemTouchListener(new SimpleClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                //Toast.makeText(PresenceVehicle.this, Integer.toString(position), Toast.LENGTH_SHORT).show();
                Intent carinfointent = new Intent(PresenceVehicle.this, PresenceVehicleInfo.class);
                carinfointent.putExtra("carnum", vehicleAdapter.getData().get(position).getPnum());
                carinfointent.putExtra("cartype", vehicleAdapter.getData().get(position).getCtype());
                carinfointent.putExtra("pztype", vehicleAdapter.getData().get(position).getCdtp());
                carinfointent.putExtra("itime", vehicleAdapter.getData().get(position).getItime());
                carinfointent.putExtra("sid", vehicleAdapter.getData().get(position).getSid());
                startActivity(carinfointent);
            }

            @Override
            public void onItemLongClick(BaseQuickAdapter adapter, View view, int position) {
                Toast.makeText(PresenceVehicle.this, Integer.toString(position) + "long", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                switch (view.getId()) {
                    case R.id.prece_modify:
                        //Toast.makeText(PresenceVehicle.this, "修改", Toast.LENGTH_SHORT).show();
                        Intent modiftintent = new Intent(PresenceVehicle.this, ModifyCarnum.class);
                        modiftintent.putExtra("number", vehicleAdapter.getData().get(position).getPnum());
                        modiftintent.putExtra("cartype", vehicleAdapter.getData().get(position).getCtype());
                        modiftintent.putExtra("pztype", vehicleAdapter.getData().get(position).getCdtp());
                        modiftintent.putExtra("pvrefresh", true);
                        modiftintent.putExtra("sid", vehicleAdapter.getData().get(position).getSid());
                        modiftintent.putExtra("position", position);
                        startActivity(modiftintent);
                        mdposition=position;
                        break;
                    case R.id.prece_outcar:
                        carnum = vehicleAdapter.getData().get(position).getPnum();
                        cartype = vehicleAdapter.getData().get(position).getCtype();
                        sid=vehicleAdapter.getData().get(position).getSid();
                        carout(App.serverurl);
                        coutposition=position;
                        break;
                    case R.id.prece_img:
                        final AlertDialog dialog = new AlertDialog.Builder(PresenceVehicle.this).create();
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.show();
                        ImageView imageView = new ImageView(PresenceVehicle.this);
                        Glide.with(PresenceVehicle.this).load(vehicleAdapter.getData().get(position).getIurl())
                                .centerCrop()
                                .placeholder(R.mipmap.carnum_default)//占位图
                                .error(R.mipmap.carnum_default)//错误网址显示图片
                                .crossFade().into(imageView);
                        Window window = dialog.getWindow();
                        window.getDecorView().setPadding(0, 0, 0, 0);
                        window.setGravity(Gravity.CENTER);
                        window.setContentView(imageView);
                        WindowManager.LayoutParams lp = window.getAttributes();
                        lp.width = WindowManager.LayoutParams.FILL_PARENT;
                        lp.height = WindowManager.LayoutParams.FILL_PARENT;
                        window.setAttributes(lp);
                        imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialog.dismiss();
                            }
                        });
                        break;
                }
            }

            @Override
            public void onItemChildLongClick(BaseQuickAdapter adapter, View view, int position) {

            }
        });
    }

    private void jfjudge() {
        switch (cdtp) {
            case 1:
                jfType = "贵宾车";
                break;
            case 2:
                jfType = "月票车";
                break;
            case 3:
                jfType = "储值车";
                break;
            case 4:
                jfType = "临时车";
                break;
            case 5:
                jfType = "免费车";
                break;
            case 6:
                jfType = "车位池车";
                break;
            case 7:
                jfType = "时租车";
                break;
        }
    }

    private void prohibitjudge() {
        switch (preson) {
            case 0:
                comfirmYy = "黑名单";
                break;
            case 1:
                comfirmYy = "有效期未开始";
                break;
            case 2:
                comfirmYy = "已过期";
                break;
            case 3:
                comfirmYy = "重复进出";
                break;
            case 4:
                comfirmYy = "值班员信息有误";
                break;
            case 5:
                comfirmYy = "未授权";
                break;
            case 6:
                comfirmYy = "需要收费，未找到在场车辆信息";
                break;
            case 7:
                comfirmYy = "通道禁止通行";
                break;
            case 8:
                comfirmYy = "车位池满禁止通行";
                break;
            case 9:
                comfirmYy = "车位满禁止通行";
                break;
            case 10:
                comfirmYy = "无效请求";
                break;
        }
    }

    private void chargejudge() {
        switch (preson) {
            case 0:
                comfirmYy = "临时车";
                break;
            case 1:
                comfirmYy = "储值票车";
                break;
            case 2:
                comfirmYy = "时租车";
                break;
            case 3:
                comfirmYy = "有效期未开始";
                break;
            case 4:
                comfirmYy = "已过期";
                break;
            case 5:
                comfirmYy = "余额不足";
                break;
            case 6:
                comfirmYy = "车位池满";
                break;
            case 7:
                comfirmYy = "车库未授权";
                break;
            case 8:
                comfirmYy = "子库未授权";
                break;
            case 9:
                comfirmYy = "报停车辆";
                break;
            case 10:
                comfirmYy = "已禁用车辆";
                break;
        }
    }

    private void confirmjudge() {
        switch (preson) {
            case 0:
                comfirmYy = "通道确认通行";
                break;
            case 1:
                comfirmYy = "车位满确认放行";
                break;
            case 2:
                comfirmYy = "车位池满确认放行";
                break;
            case 3:
                comfirmYy = "有效期未开始";
                break;
            case 4:
                comfirmYy = "已过期";
                break;

        }
    }

    protected void onDestroy() {
        super.onDestroy();
        if (this.dialog != null)
            this.dialog.cancel();
    }


    public void onStart() {
        super.onStart();
        if (!App.pvRefresh) {
            Log.e("TAG", "onstart 刷新");
            onRefresh();
            App.pvRefresh = true;
        }

        /*if (App.mdRefresh){
            vehicleAdapter.notifyItemChanged(ModifyCarnum.midefyposition);
            Log.e("TAG","修改的item**"+ModifyCarnum.midefyposition);
            App.mdRefresh=false;
        }*/
    }

    @OnClick({R.id.prece_return, R.id.prece_search})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.prece_return:
                finish();
                break;
            case R.id.prece_search:
                startActivity(new Intent(PresenceVehicle.this, QueryCarnum.class));
                break;
        }
    }
}