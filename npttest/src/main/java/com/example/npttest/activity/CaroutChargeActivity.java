package com.example.npttest.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.npttest.App;
import com.example.npttest.R;
import com.example.npttest.constant.Constant;
import com.example.npttest.tool.DateTools;
import com.example.npttest.tool.TimeDifferTools;
import com.example.npttest.util.TimeUtils;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;
import com.telpo.tps550.api.TelpoException;
import com.telpo.tps550.api.printer.ThermalPrinter;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;
import com.zyao89.view.zloading.ZLoadingDialog;
import com.zyao89.view.zloading.Z_TYPE;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.Hashtable;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.dmoral.toasty.Toasty;
import okhttp3.Call;
import okhttp3.MediaType;
import razerdp.basepopup.BasePopupWindow;

/**
 * Created by liuji on 2017/8/1.
 */

public class CaroutChargeActivity extends Activity {

    @Bind(R.id.carout_charge_return)
    ImageView caroutChargeReturn;
    @Bind(R.id.carout_charge_nb)
    LinearLayout caroutChargeNb;
    @Bind(R.id.textView5)
    TextView textView5;
    @Bind(R.id.carout_charge_nmon)
    TextView caroutChargeNmon;
    @Bind(R.id.carout_charge_rmon)
    TextView caroutChargeRmon;
    @Bind(R.id.carout_charge_smon)
    TextView caroutChargeSmon;
    @Bind(R.id.charge)
    LinearLayout charge;
    @Bind(R.id.carout_charge_carnum)
    TextView caroutChargeCarnum;
    @Bind(R.id.carout_charge_cartype)
    TextView caroutChargeCartype;
    @Bind(R.id.carout_charge_pztype)
    TextView caroutChargePztype;
    @Bind(R.id.carout_charge_yy)
    TextView caroutChargeYy;
    @Bind(R.id.carout_charge_ptime)
    TextView caroutChargePtime;
    @Bind(R.id.carout_charge_itime)
    TextView caroutChargeItime;
    @Bind(R.id.textView)
    TextView textView;
    @Bind(R.id.carout_charge_ctime)
    TextView caroutChargeCtime;
    @Bind(R.id.carout_charge_cancel)
    Button caroutChargeCancel;
    @Bind(R.id.carout_charge_confirm)
    Button caroutChargeConfirm;
    @Bind(R.id.carout_charge_free)
    Button caroutChargeFree;
    @Bind(R.id.carout_charge_free_lin)
    LinearLayout caroutChargeFreeLin;
    private String carnum, cartype, jfType, comfirmYy, sid, pktime, srmon, snmon, ssmon;
    private double nmom, rmon, smon;
    private int ctype, cdtp;
    private long itime, ctime;
    private ZLoadingDialog dialog1, dialog;
    private Boolean pvrefresh;
    SynthesizerListener mSynListener;

    private final int PRINTVERSION = 5;
    ProgressDialog pgdialog;
    private ProgressDialog progressDialog;
    MyHandler handler;
    private static String printVersion;
    private String Result;
    private Boolean nopaper = false;
    private final int OVERHEAT = 12;
    private final int PRINTERR = 11;
    private final int CANCELPROMPT = 10;
    private final int NOPAPER = 3;
    private Boolean printok = false;
    private String print;
    public static Activity caroutactivity;
    SlideFromBottomPopup slideFromBottomPopup;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.carout_charge);
        ButterKnife.bind(this);
        caroutactivity = this;
        //是否为免费放行
        if (Constant.enfree == 1) {
            caroutChargeFreeLin.setVisibility(View.GONE);
        }
        SpeechUtility.createUtility(CaroutChargeActivity.this, SpeechConstant.APPID + "=59df2c0c");
        Intent intent = getIntent();
        carnum = intent.getStringExtra("carnum");
        ctype = intent.getIntExtra("ctype", 0);
        jfType = intent.getStringExtra("jfType");
        itime = intent.getLongExtra("itime", 0);
        ctime = intent.getLongExtra("ctime", 0);
        comfirmYy = intent.getStringExtra("comfirmYy");
        nmom = intent.getDoubleExtra("nmon", 0);
        //rmon=(double)intent.getIntExtra("rmon",0);
        rmon = intent.getDoubleExtra("rmon", 0);
        Log.e("TAG", rmon + "");
        smon = intent.getDoubleExtra("smon", 0);
        sid = intent.getStringExtra("sid");
        cdtp = intent.getIntExtra("cdtp", 0);
        pvrefresh = intent.getBooleanExtra("pvrefresh", false);
        pktime = TimeDifferTools.getDistanceTime(itime * 1000, ctime * 1000);
        judge();
        caroutChargeCarnum.setText(carnum);
        caroutChargeCartype.setText(cartype);
        caroutChargePztype.setText(jfType);
        caroutChargeItime.setText(DateTools.getDate(itime * 1000) + "");
        caroutChargeCtime.setText(DateTools.getDate(ctime * 1000) + "");
        caroutChargePtime.setText(pktime);
        caroutChargeYy.setText(comfirmYy);
        srmon = String.format("%.2f", rmon / 100);
        snmon = String.format("%.2f", nmom / 100);
        ssmon = String.format("%.2f", smon / 100);
        caroutChargeNmon.setText(snmon);
        caroutChargeRmon.setText(srmon);
        caroutChargeSmon.setText(ssmon);

        Constant.carnum = carnum;
        Constant.ctype = ctype;
        Constant.jfType = jfType;
        Constant.itime = itime;
        Constant.ctime = ctime;
        Constant.comfirmYy = comfirmYy;
        Constant.snmon = snmon;
        Constant.ssmon = ssmon;
        Constant.srmon = srmon + "元";
        Constant.carnum = carnum;
        Constant.cdtp = cdtp;
        Constant.pvrefresh = pvrefresh;
        Constant.pktime = pktime;
        Constant.sid = sid;

        handler = new MyHandler();
        //检测打印机
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    ThermalPrinter.start(CaroutChargeActivity.this);
                    ThermalPrinter.reset();
                    printVersion = ThermalPrinter.getVersion();
                } catch (TelpoException e) {
                    e.printStackTrace();
                } finally {
                    if (printVersion != null) {
                        Message message = new Message();
                        message.what = PRINTVERSION;
                        message.obj = "1";
                        handler.sendMessage(message);
                    } else {
                        Message message = new Message();
                        message.what = PRINTVERSION;
                        message.obj = "0";
                        handler.sendMessage(message);
                    }
                    ThermalPrinter.stop(CaroutChargeActivity.this);
                }
            }
        }).start();
        slideFromBottomPopup = new SlideFromBottomPopup(this);
    }

    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case NOPAPER:
                    noPaperDlg();
                    break;
                case OVERHEAT:
                    AlertDialog.Builder overHeatDialog = new AlertDialog.Builder(CaroutChargeActivity.this);
                    overHeatDialog.setTitle("温馨提示");
                    overHeatDialog.setMessage("打印过热");
                    overHeatDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            carout_start_voice();
                            Intent zdintent = new Intent(CaroutChargeActivity.this, CaroutSuccessful.class);
                            zdintent.putExtra("carnum", carnum);
                            zdintent.putExtra("jfType", jfType);
                            zdintent.putExtra("ctype", ctype);
                            zdintent.putExtra("ctime", ctime);
                            zdintent.putExtra("itime", itime);
                            zdintent.putExtra("pvrefresh", pvrefresh);
                            startActivity(zdintent);
                            finish();
                        }
                    });
                    overHeatDialog.show();
                    break;
                case CANCELPROMPT:
                    if (progressDialog != null && !CaroutChargeActivity.this.isFinishing()) {
                        progressDialog.dismiss();
                        progressDialog = null;
                    }
                    break;
                case 0x0123:
                    carout_start_voice();
                    Intent zdintent = new Intent(CaroutChargeActivity.this, CaroutSuccessful.class);
                    zdintent.putExtra("carnum", carnum);
                    zdintent.putExtra("jfType", jfType);
                    zdintent.putExtra("ctype", ctype);
                    zdintent.putExtra("ctime", ctime);
                    zdintent.putExtra("itime", itime);
                    zdintent.putExtra("pvrefresh", pvrefresh);
                    startActivity(zdintent);
                    finish();
                    break;
                case PRINTVERSION:
                    if (msg.obj.equals("1")) {
                        Log.e("TAG", printVersion);
                    } else {
                        //Toast.makeText(InputCarnum.this, "", Toast.LENGTH_LONG).show();
                        Log.e("TAG", "未找到打印机");
                    }
                    break;
                default:
                    Toast.makeText(CaroutChargeActivity.this, "打印机异常！", Toast.LENGTH_LONG).show();
                    carout_start_voice();
                    Intent zintent = new Intent(CaroutChargeActivity.this, CaroutSuccessful.class);
                    zintent.putExtra("carnum", carnum);
                    zintent.putExtra("jfType", jfType);
                    zintent.putExtra("ctype", ctype);
                    zintent.putExtra("ctime", ctime);
                    zintent.putExtra("itime", itime);
                    zintent.putExtra("pvrefresh", pvrefresh);
                    startActivity(zintent);
                    finish();
                    break;
            }
        }
    }

    //缺纸
    private void noPaperDlg() {
        AlertDialog.Builder dlg = new AlertDialog.Builder(CaroutChargeActivity.this);
        dlg.setTitle("打印缺纸");
        dlg.setMessage("打印缺纸，请放入纸后入场打印");
        dlg.setCancelable(false);
        dlg.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ThermalPrinter.stop(CaroutChargeActivity.this);
                carout_start_voice();
                Intent zdintent = new Intent(CaroutChargeActivity.this, CaroutSuccessful.class);
                zdintent.putExtra("carnum", carnum);
                zdintent.putExtra("jfType", jfType);
                zdintent.putExtra("ctype", ctype);
                zdintent.putExtra("ctime", ctime);
                zdintent.putExtra("itime", itime);
                zdintent.putExtra("pvrefresh", pvrefresh);
                startActivity(zdintent);
                finish();
            }
        });
        dlg.show();
    }

    //获取当前时间
    private long gettime() {
        Date date = new Date();
        long time = (date.getTime() / 1000);
        //Log.e("TAG",date.getTime()/1000+"");
        return time;
    }

    //打印文本
    private class contentPrintThread extends Thread {
        @Override
        public void run() {
            super.run();
            try {
                ThermalPrinter.start(CaroutChargeActivity.this);
                ThermalPrinter.reset();
                ThermalPrinter.setAlgin(ThermalPrinter.ALGIN_LEFT);
                ThermalPrinter.setLeftIndent(0);
                ThermalPrinter.setLineSpace(32);
                ThermalPrinter.setFontSize(2);
               /* if (wordFont == 4) {
                    ThermalPrinter.setFontSize(2);
                    ThermalPrinter.enlargeFontSize(2, 2);
                } else if (wordFont == 3) {
                    ThermalPrinter.setFontSize(1);
                    ThermalPrinter.enlargeFontSize(2, 2);
                } else if (wordFont == 2) {
                    ThermalPrinter.setFontSize(2);
                } else if (wordFont == 1) {
                    ThermalPrinter.setFontSize(1);
                }*/
                String str = "\n          收费票据"
                        + "\n----------------------------"
                        + "\n车牌号：" + carnum
                        + "\n订单号：" + sid
                        + "\n日期：" + TimeUtils.getStrTime(String.valueOf(gettime()))
                        + "\n----------------------------"
                        + "\n凭证类型：" + jfType
                        + "\n入场时间：" + DateTools.getDate(itime * 1000) + ""
                        + "\n出场时间：" + DateTools.getDate(ctime * 1000) + ""
                        + "\n停车时间：" + pktime
                        + "\n应收金额：" + snmon + "元"
                        + "\n优惠金额：" + ssmon + "元"
                        + "\n实收金额：" + srmon + "元"
                        + "\n值班员：" + Constant.username
                        + "\n地址：" + Constant.adds
                        + "\n----------------------------"
                        + "\n          欢迎您下次光临";
                Bitmap bitmap = CreateCode(sid, BarcodeFormat.QR_CODE, 256, 256);
                if (bitmap != null) {
                    ThermalPrinter.printLogo(bitmap);
                }
                ThermalPrinter.setGray(4);
                ThermalPrinter.addString(str);
                ThermalPrinter.printString();
                ThermalPrinter.walkPaper(100);
                printok = true;
            } catch (Exception e) {
                e.printStackTrace();
                Result = e.toString();
                printok = false;
                if (Result.equals("com.telpo.tps550.api.printer.NoPaperException")) {
                    nopaper = true;
                } else if (Result.equals("com.telpo.tps550.api.printer.OverHeatException")) {
                    handler.sendMessage(handler.obtainMessage(OVERHEAT, 1, 0, null));
                } else {
                    handler.sendMessage(handler.obtainMessage(PRINTERR, 1, 0, null));
                }
            } finally {
                handler.sendMessage(handler.obtainMessage(CANCELPROMPT, 1, 0, null));
                if (nopaper) {
                    handler.sendMessage(handler.obtainMessage(NOPAPER, 1, 0, null));
                    nopaper = false;
                    return;
                } else {
                    handler.sendEmptyMessage(0x0123);
                }
                ThermalPrinter.stop(CaroutChargeActivity.this);
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
        //mTts.startSpeaking(carnum+"出场成功！本次收费"+srmon+"元", mSynListener);
        char[] carnumber = carnum.toCharArray();
        if (carnumber.length == 7) {
            mTts.startSpeaking(String.valueOf(carnumber[0]) + " " + String.valueOf(carnumber[1]) + " " + String.valueOf(carnumber[2])
                    + " " + String.valueOf(carnumber[3]) + " " + String.valueOf(carnumber[4]) + " " + String.valueOf(carnumber[5]) + " " + String.valueOf(carnumber[6]) + "，" + "出场成功，本次收费" + Constant.srmon + "，停车时间" + pktime, mSynListener);
        } else if (carnumber.length == 8) {
            mTts.startSpeaking(String.valueOf(carnumber[0]) + " " + String.valueOf(carnumber[1]) + " " + String.valueOf(carnumber[2])
                    + " " + String.valueOf(carnumber[3]) + " " + String.valueOf(carnumber[4]) + " " + String.valueOf(carnumber[5]) + " " + String.valueOf(carnumber[6]) + " " + String.valueOf(carnumber[7]) + "，" + "出场成功，本次收费" + Constant.srmon + "，停车时间" + pktime, mSynListener);
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

    @OnClick({R.id.carout_charge_return, R.id.carout_charge_cancel, R.id.carout_charge_confirm, R.id.carout_charge_free})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.carout_charge_return:
                finish();
                break;
            case R.id.carout_charge_cancel:
                cancelPass(App.serverurl);
                break;
            case R.id.carout_charge_confirm:
                slideFromBottomPopup.showPopupWindow();
                break;
            case R.id.carout_charge_free:
                Intent intent = new Intent(CaroutChargeActivity.this, ReleaseRemarks.class);
                intent.putExtra("relBoo", true);
                intent.putExtra("carnum", carnum);
                intent.putExtra("ctype", ctype);
                intent.putExtra("jfType", jfType);
                intent.putExtra("itime", itime);
                intent.putExtra("ctime", ctime);
                intent.putExtra("pvrefresh", pvrefresh);
                intent.putExtra("cdtp", cdtp);
                intent.putExtra("sid", sid);
                intent.putExtra("pktime", pktime);
                intent.putExtra("snmon", snmon);
                intent.putExtra("ssmon", ssmon);
                intent.putExtra("srmon", srmon);
                startActivity(intent);
                //finish();
                break;
        }
    }

    private void judge() {
        switch (ctype) {
            case 1:
                cartype = "摩托车";
                break;
            case 2:
                cartype = "小型车";
                break;
            case 3:
                cartype = "中型车";
                break;
            case 4:
                cartype = "大型车";
                break;
            case 5:
                cartype = "运输车";
                break;
            case 6:
                cartype = "备用车";
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (slideFromBottomPopup != null) {
            slideFromBottomPopup.dismiss();
        }
    }

    private void chargePass(String url) {
        dialog1 = new ZLoadingDialog(CaroutChargeActivity.this);
        dialog1.setLoadingBuilder(Z_TYPE.LEAF_ROTATE)//设置类型STAR_LOADING 五角星
                .setLoadingColor(Color.parseColor("#55BEB7"))//颜色
                .setHintText("Loading...")
                .setHintTextColor(Color.parseColor("#55BEB7"))
                .setHintTextSize(16) // 设置字体大小 dp
                .setHintTextColor(Color.GRAY)  // 设置字体颜色
                .show();
        String cpjson = "{\"cmd\":\"141\",\"type\":\"" + Constant.TYPE + "\",\"code\":\"" + Constant.CODE + "\"," +
                "\"dsv\":\"" + Constant.DSV + "\",\"sid\":\"" + sid + "\",\"io\":\"1\",\"rstat\":\"0\"," +
                "\"ftype\":\"1\",\"sale\":\"0\",\"reas\":\"000\",\"spare\":\"0\",\"sign\":\"abcd\"}";

        Log.e("TAG", cpjson);
        OkHttpUtils.postString().url(url)
                .content(cpjson)
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
                    int rstat = datajson.getInt("rstat");
                    App.wmon = datajson.getDouble("emon");
                    //String sid=datajson.getString("sid");
                    if (rstat == 0) {
                        /*if (printVersion!=null){
                            progressDialog = ProgressDialog.show(CaroutChargeActivity.this, "打印", "打印中，请稍后");
                            new contentPrintThread().start();
                        }else {*/
                        carout_start_voice();
                        Intent zdintent = new Intent(CaroutChargeActivity.this, CaroutSuccessful.class);
                        zdintent.putExtra("carnum", carnum);
                        zdintent.putExtra("jfType", jfType);
                        zdintent.putExtra("ctype", ctype);
                        zdintent.putExtra("ctime", ctime);
                        zdintent.putExtra("itime", itime);
                        zdintent.putExtra("pvrefresh", pvrefresh);
                        zdintent.putExtra("paytype", 1);
                        zdintent.putExtra("caroutprint", true);
                        startActivity(zdintent);
                        finish();

                    } else {
                        Toasty.error(CaroutChargeActivity.this, "订单无效需重新发起", Toast.LENGTH_SHORT, true).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        });
    }

    public class SlideFromBottomPopup extends BasePopupWindow implements View.OnClickListener {

        private View popupView;
        Context context;

        public SlideFromBottomPopup(Activity context) {
            super(context);
            this.context = context;
            bindEvent();
        }

        @Override
        protected Animation initShowAnimation() {
            return getTranslateAnimation(250 * 2, 0, 300);
        }

        @Override
        public View getClickToDismissView() {
            return popupView.findViewById(R.id.click_to_dismiss);
        }

        @Override
        public View onCreatePopupView() {
            popupView = LayoutInflater.from(getContext()).inflate(R.layout.popup_slide_from_bottom, null);
            return popupView;
        }

        @Override
        public View initAnimaView() {
            return popupView.findViewById(R.id.popup_anima);
        }

        private void bindEvent() {
            if (popupView != null) {
                popupView.findViewById(R.id.tx_1).setOnClickListener(this);
                popupView.findViewById(R.id.tx_2).setOnClickListener(this);
                popupView.findViewById(R.id.tx_3).setOnClickListener(this);
            }

        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.tx_1:
                    chargePass(App.serverurl);
                    break;
                case R.id.tx_2:
                    //startActivity(new Intent(CaroutChargeActivity.this,WechatPay.class));
                    Intent intent = new Intent(CaroutChargeActivity.this, WechatPay.class);
                    intent.putExtra("money", (int) (rmon));
                    startActivity(intent);
                    break;
                case R.id.tx_3:
                    Intent intent1 = new Intent(CaroutChargeActivity.this, AliPay.class);
                    intent1.putExtra("money", (int) (rmon));
                    startActivity(intent1);
                    break;
                default:
                    break;
            }

        }
    }

    private void cancelPass(String url) {
        dialog = new ZLoadingDialog(CaroutChargeActivity.this);
        dialog.setLoadingBuilder(Z_TYPE.LEAF_ROTATE)//设置类型STAR_LOADING 五角星
                .setLoadingColor(Color.parseColor("#55BEB7"))//颜色
                .setHintText("Loading...")
                .setHintTextColor(Color.parseColor("#55BEB7"))
                .setHintTextSize(16) // 设置字体大小 dp
                .setHintTextColor(Color.GRAY)  // 设置字体颜色
                .show();

        String cpjson = "{\"cmd\":\"141\",\"type\":\"" + Constant.TYPE + "\",\"code\":\"" + Constant.CODE + "\"," +
                "\"dsv\":\"" + Constant.DSV + "\",\"sid\":\"" + sid + "\",\"io\":\"1\",\"rstat\":\"1\"," +
                "\"ftype\":\"0\",\"sale\":\"0\",\"reas\":\"000\",\"spare\":\"0\",\"sign\":\"abcd\"}";

        Log.e("TAG", cpjson);
        OkHttpUtils.postString().url(url)
                .content(cpjson)
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
                dialog.dismiss();
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String reasonjson = jsonObject.getString("reason");//操作成功
                    JSONObject resultjson = jsonObject.getJSONObject("result");
                    JSONObject datajson = resultjson.getJSONObject("data");
                    int rstat = datajson.getInt("rstat");
                    //String sid=datajson.getString("sid");
                    finish();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        });
    }
}
