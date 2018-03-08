package com.example.npttest.activity;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.npttest.R;
import com.example.npttest.constant.Constant;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.jaeger.library.StatusBarUtil;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;
import com.zyao89.view.zloading.ZLoadingDialog;
import com.zyao89.view.zloading.Z_TYPE;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Hashtable;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;

/**
 * Created by liuji on 2017/11/3.
 */

public class WechatPay extends Activity {
    @Bind(R.id.wechatpay_return)
    ImageView wechatpayReturn;
    @Bind(R.id.wechatpay_qr)
    ImageView wechatpayQr;
    @Bind(R.id.wechatpay_rmon)
    TextView wechatpayRmon;
    private ZLoadingDialog dialog;
    private int money;
    private String qrurl;
    public static Context wechatcontext;
    public static Activity Wechatactivity;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wechatpay);
        StatusBarUtil.setColor(WechatPay.this, Color.parseColor("#429056"));
        ButterKnife.bind(this);
        Wechatactivity = this;
        if (Constant.wxUrl!=null&&!Constant.wxUrl.equals("null")&&!Constant.wxUrl.equals("")){
            getqr();
        }
        setwechatcontext(this);
        wechatpayRmon.setText("本次支付金额："+ Constant.srmon);
    }

    public static void setwechatcontext(Context wechatcontext) {
        WechatPay.wechatcontext = wechatcontext;
    }

    @OnClick(R.id.wechatpay_return)
    public void onViewClicked() {
        finish();
    }

    //获取二维码
    private void getqr() {
        money=getIntent().getIntExtra("money",0);
       // money = 1;
        Log.e("TAG",  "微信收款金额："+money );
        if (!isFinishing()) {
            dialog = new ZLoadingDialog(WechatPay.this);
            dialog.setLoadingBuilder(Z_TYPE.LEAF_ROTATE)//设置类型STAR_LOADING 五角星
                    .setLoadingColor(Color.parseColor("#55BEB7"))//颜色
                    .setHintText("Loading...")
                    .setHintTextColor(Color.parseColor("#55BEB7"))
                    .setHintTextSize(16) // 设置字体大小 dp
                    .setHintTextColor(Color.GRAY) // 设置字体颜色
                    //.setCanceledOnTouchOutside(false)
                    .show();
        }
        //String url = "http://192.168.8.111/nptpay/barcode/wxPay";
        String url = Constant.wxUrl;
        //String url1 = "http://192.168.8.111/nptpay/barcode/aliPay";
        Log.e("TAG","微信支付—— code:"+Constant.CODE+"pname:"+Constant.pname+"sid:"+Constant.sid);
        // String appcode = "1d737752e21e44598c9eb8acf7ae2165";
        OkHttpUtils.post().url(url)
                .addParams("code", Constant.CODE)
                .addParams("money", String.valueOf(money))
                .addParams("pname", Constant.pname)
                .addParams("sid", Constant.sid)
                .build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                Toast.makeText(WechatPay.this, "获取支付二维码超时", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(String response, int id) {
                dialog.dismiss();
                Log.e("TAG", response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONObject resultJson = jsonObject.getJSONObject("result");
                    qrurl = resultJson.getString("url");
                    Log.e("TAG", qrurl);
                    try {
                        Bitmap bitmap = CreateCode(qrurl, BarcodeFormat.QR_CODE, 256, 256);
                        wechatpayQr.setImageBitmap(bitmap);
                    } catch (WriterException e) {
                        e.printStackTrace();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
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
}
