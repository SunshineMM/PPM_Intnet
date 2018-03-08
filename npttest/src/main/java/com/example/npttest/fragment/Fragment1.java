package com.example.npttest.fragment;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.AccessToken;
import com.example.npttest.App;
import com.example.npttest.R;
import com.example.npttest.activity.Admission;
import com.example.npttest.activity.Appearance;
import com.example.npttest.activity.InputCarnum;
import com.example.npttest.activity.PresenceVehicle;
import com.example.npttest.activity.SpalshActivity;
import com.example.npttest.activity.SurplusCarParkingLot;
import com.example.npttest.camera.CameraActivityBaidu;
import com.example.npttest.constant.Constant;
import com.example.npttest.linearlayout.MyLayout;
import com.example.npttest.linearlayout.MyLayoutIn;
import com.example.npttest.loader.GlideImageLoader;
import com.youth.banner.Banner;
import com.youth.banner.Transformer;
import com.youth.banner.listener.OnBannerListener;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.MediaType;

/**
 * Created by liuji on 2017/7/28.
 */

public class Fragment1 extends Fragment implements OnBannerListener {
    @Bind(R.id.fg_banner)
    Banner fgBanner;
    @Bind(R.id.textView6)
    TextView textView6;
    @Bind(R.id.fg_zaicar)
    TextView fgZaicar;
    @Bind(R.id.fg_prece)
    LinearLayout fgPrece;
    @Bind(R.id.fg_shouf)
    TextView fgShouf;
    @Bind(R.id.fg_shengy)
    TextView fgShengy;
    @Bind(R.id.fg_Surplus_car)
    LinearLayout fgSurplusCar;
    @Bind(R.id.fg_car_into)
    MyLayoutIn fgCarInto;
    @Bind(R.id.fg_car_out)
    MyLayout fgCarOut;
    @Bind(R.id.fg_inputnum)
    Button fgInputnum;
    private List<View> list = new ArrayList<View>();
    private List<ImageView> imageViews = new ArrayList<ImageView>();
    private int select_index = 0;//当前选中item 的下标
    public static final String INPUT_LICENSE_COMPLETE = "me.kevingo.licensekeyboard.input.comp";
    public static final String INPUT_LICENSE_KEY = "LICENSE";
    private String number, color, bitmapPath;
    private int type;
    public static TextView shengyTv;
    public static TextView pvcarTv;
    List<Integer> images = new ArrayList<>();
    private boolean hasGotToken = false;//是否获取token

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        images.add(R.mipmap.img_1);
        images.add(R.mipmap.img_2);
        images.add(R.mipmap.img_3);
        images.add(R.mipmap.img_4);
        images.add(R.mipmap.img_5);
        initAccessTokenWithAkSk();
    }

    private void initAccessTokenWithAkSk() {
        OCR.getInstance().initAccessTokenWithAkSk(new OnResultListener<AccessToken>() {
            @Override
            public void onResult(AccessToken result) {
                String token = result.getAccessToken();
                hasGotToken = true;
            }

            @Override
            public void onError(OCRError error) {
                error.printStackTrace();
                //alertText("AK，SK方式获取token失败", error.getMessage());
                Log.e("TAG","AK，SK方式获取token失败");
            }
        }, getContext(), "d34eA1GpPiuGgCK8GcrXedxU", "k6DvClDyCpmVWY9CdUt1arq7a3AeQGGS");
    }

    private boolean checkTokenStatus() {
        if (!hasGotToken) {
            Toast.makeText(getContext(), "token还未成功获取", Toast.LENGTH_LONG).show();
        }
        return hasGotToken;
    }
    private void jump() {
        TranslateAnimation down = new TranslateAnimation(0, 0, -300, 0);//位移动画，从button的上方300像素位置开始
        down.setFillAfter(true);
        down.setInterpolator(new BounceInterpolator());//弹跳动画,要其它效果的当然也可以设置为其它的值
        down.setDuration(2000);//持续时间
        fgCarInto.startAnimation(down);//设置按钮运行该动画效果
        fgCarOut.startAnimation(down);//设置按钮运行该动画效果
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment1, null);
        ButterKnife.bind(this, v);
        jump();
        shengyTv = fgShengy;
        pvcarTv=fgZaicar;
        if (App.serverurl != null) {
            get_quantity(App.serverurl);
            //get_quantity(App.serverurl);
        }
        fgBanner.setImages(images).setImageLoader(new GlideImageLoader())
                .setBannerAnimation(Transformer.Default).isAutoPlay(true)
                .setOnBannerListener(this).setDelayTime(3000).start();
        return v;
    }

    static final String[] PERMISSION = new String[]{Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,// 写入权限
            Manifest.permission.READ_EXTERNAL_STORAGE, // 读取权限
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.VIBRATE, Manifest.permission.INTERNET};


    @Override
    public void onStart() {
        super.onStart();
        //开始轮播
        if (App.surpluscar != null) {
            fgShengy.setText(App.surpluscar);
        }
        if (Constant.pvcar!=null){
            fgZaicar.setText(Constant.pvcar);
        }
        fgBanner.startAutoPlay();
        String wmon = String.format("%.2f", App.wmon / 100);
        fgShouf.setText(wmon);
    }

    @Override
    public void onStop() {
        super.onStop();
        //结束轮播
        fgBanner.stopAutoPlay();
    }

    @OnClick({R.id.fg_car_into, R.id.fg_car_out, R.id.fg_inputnum, R.id.fg_prece, R.id.fg_zaicar, R.id.fg_Surplus_car})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.fg_car_into:
                if (!checkTokenStatus()) {
                    return;
                }
                type = 1;
                //jumpVideoRecog();
                Intent intent = new Intent(getActivity(), CameraActivityBaidu.class);
                //intent.putExtra("camera", true);
                //startActivity(intent);
                startActivityForResult(intent, 0x11);
                break;
            case R.id.fg_car_out:
                type = 2;
                Intent intent1 = new Intent(getActivity(), CameraActivityBaidu.class);
                //intent1.putExtra("camera", true);
                //startActivity(intent);
                startActivityForResult(intent1, 0x11);
                //jumpVideoRecog();
                break;
            case R.id.fg_inputnum:
                startActivity(new Intent(getActivity(), InputCarnum.class));
                break;
            case R.id.fg_prece:
                startActivity(new Intent(getActivity(), PresenceVehicle.class));
                break;
            case R.id.fg_Surplus_car:
                startActivity(new Intent(getActivity(), SurplusCarParkingLot.class));
                break;
        }
    }

    //获取在场车数量和空车位的数量
    private void get_quantity(String url) {
        String jsons = "{\"cmd\":\"176\",\"type\":\""+ Constant.TYPE+"\",\"code\":\""+ Constant.CODE+"\",\"dsv\":\""+ Constant.DSV+"\",\"sign\":\"abcd\"}";
        Log.e("TAG", jsons);
        OkHttpUtils.postString().url(url)
                .content(jsons)
                .mediaType(MediaType.parse("application/json; charset=utf-8"))
                .build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {

            }

            @Override
            public void onResponse(String response, int id) {
                Log.e("TAG", response);
                try {
                    JSONObject rpjson = new JSONObject(response);
                    String reasonjson = rpjson.getString("reason");
                    JSONObject resultjson = rpjson.getJSONObject("result");
                    JSONObject datajsonObject = resultjson.getJSONObject("data");
                    // Log.e("TAG", datajsonObject.toString());
                    if (reasonjson.equals("操作成功")) {
                        int elot=datajsonObject.getInt("elot");
                        int number=datajsonObject.getInt("number");
                        int tlot=datajsonObject.getInt("tlot");
                        Constant.pvcar= String.valueOf(number);
                        fgShengy.setText(String.valueOf(elot+tlot));
                        fgZaicar.setText(number+"");
                    } else {
                        Log.e("TAG", "获取失败");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    //跳转到视频扫描识别界面
  /*  public void jumpVideoRecog() {
        Intent video_intent = new Intent();
        video_intent.putExtra("camera", true);
        RecogService.recogModel = true;//true  精准模式 多帧识别  false:快速模式  单帧识别
        video_intent = new Intent(getActivity(), MemoryCameraActivity.class);
        if (Build.VERSION.SDK_INT >= 23) {
            CheckPermission checkPermission = new CheckPermission(getActivity());
            if (checkPermission.permissionSet(PERMISSION)) {
                PermissionActivity.startActivityForResult(getActivity(), 0, "true", PERMISSION);
                startActivityForResult(video_intent, 0x11);
            } else {
                video_intent.setClass(getActivity().getApplication(), MemoryCameraActivity.class);
                video_intent.putExtra("camera", true);
                //startActivity(video_intent);
                startActivityForResult(video_intent, 0x11);
            }
        } else {
            video_intent.setClass(getActivity().getApplication(), MemoryCameraActivity.class);
            video_intent.putExtra("camera", true);
            startActivityForResult(video_intent, 0x11);
        }
        //finish();
    }*/

    //跳转到拍照识别界面
   /* public void jumpTakePic() {
        Intent cameraintent = new Intent(getActivity(), MemoryCameraActivity.class);
        if (Build.VERSION.SDK_INT >= 23) {
            CheckPermission checkPermission = new CheckPermission(getActivity());
            if (checkPermission.permissionSet(PERMISSION)) {
                PermissionActivity.startActivityForResult(getActivity(), 0, "false", PERMISSION);
            } else {
                cameraintent.putExtra("camera", false);
                startActivity(cameraintent);
            }
        } else {
            cameraintent.putExtra("camera", false);
            startActivity(cameraintent);
        }
    }*/

    public void test() {
        startActivity(new Intent(getActivity(), SpalshActivity.class));
        Log.e("TAG", getActivity() + "");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data!=null){
            number = data.getStringExtra("number").toString();
            //color = data.getStringExtra("color").toString();
            bitmapPath = data.getStringExtra("path").toString();
            if (number.equals("null")) {
                Log.e("TAG","未识别");
            } else if (type == 1) {
                Log.e("TAG","入场");
                Intent intent = new Intent(getActivity(), Admission.class);
                intent.putExtra("number", number);
                intent.putExtra("color", color);
                intent.putExtra("path", bitmapPath);
                startActivity(intent);
            } else if (type == 2) {
                Log.e("TAG","出场");
                Intent intent = new Intent(getActivity(), Appearance.class);
                intent.putExtra("number", number);
                intent.putExtra("color", color);
                intent.putExtra("path", bitmapPath);
                startActivity(intent);
            }
        }
        //Toast.makeText(getActivity(), bitmapPath, Toast.LENGTH_SHORT).show();
        //Toast.makeText(getActivity(),number, Toast.LENGTH_LONG).show();
    }

    @Override
    public void OnBannerClick(int position) {
        //Toast.makeText(getActivity(), position + "", Toast.LENGTH_SHORT).show();
        switch (position) {
            case 0:
                break;
            case 1:
                break;
            case 2:
                break;
            case 3:
                break;
            case 4:
                /*Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("http://www.baidu.com"));
                startActivity(intent);*/
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
