package com.example.npttest.activity;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.npttest.R;
import com.example.npttest.util.MPermissionHelper;
import com.example.npttest.util.SPUtils;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.ice.iceplate.ActivateService;

import java.io.File;
import java.util.Hashtable;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.dmoral.toasty.Toasty;

/**
 * Created by liuji on 2017/10/17.
 */

public class GuideActivity extends BaseActivity implements MPermissionHelper.PermissionCallBack {
    @Bind(R.id.guide_img)
    ImageView guideImg;
    @Bind(R.id.guide_tv)
    TextView guideTv;
    @Bind(R.id.guide_btn)
    Button guideBtn;
    @Bind(R.id.guide_et)
    EditText guideEt;
    private MPermissionHelper permissionHelper;
    public ActivateService.ActivateBinder acBinder;
    public ServiceConnection acConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            acConnection = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            acBinder = (ActivateService.ActivateBinder) service;
        }

    };
    private String scode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guide_activity);
        ButterKnife.bind(this);
        /*Intent actiIntent = new Intent(GuideActivity.this, ActivateService.class);
        bindService(actiIntent, acConnection, Service.BIND_AUTO_CREATE);*/
        permissionHelper = new MPermissionHelper(this);
        //申请权限
        permissionHelper.requestPermission(this,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        );
        TelephonyManager TelephonyMgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
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
        scode = "PPM" + szImei;
        guideTv.setText(scode);
        SPUtils.put(GuideActivity.this, "code", scode);
        //Content.CODE= (String) SPUtils.get(GuideActivity.this,"code","");
        try {
            Bitmap bitmap = CreateCode("PPM" + szImei, BarcodeFormat.QR_CODE, 256, 256);
            guideImg.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        //Bitmap bitmap = null;
        /*try {
            bitmap = BitmapUtils.create2DCode("ppm" + szImei);//根据内容生成二维码
            guideImg.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }*/
        File file = getFilesDir();
        String path = file.getAbsolutePath();
        System.out.println("path = " + path);
        Log.i("wu", "path = " + path);

    }

    @OnClick(R.id.guide_btn)
    public void onViewClicked() {
        /*if (TextUtils.isEmpty(guideEt.getText().toString().trim())){
            Toast.makeText(this, "授权码不能为空", Toast.LENGTH_SHORT).show();
        }else {
            activateSN();
        }*/
        SPUtils.put(GuideActivity.this, "frist", false);
        SPUtils.put(GuideActivity.this, "sn", false);
        startActivity(new Intent(GuideActivity.this, SpalshActivity.class));
        finish();
    }

    //权限回调
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionHelper.handleRequestPermissionsResult(requestCode, this, grantResults);
    }

    @Override
    public void permissionRegisterSuccess(String... permissions) {
        //Toast.makeText(this, "成功", Toast.LENGTH_SHORT).show();
        Log.e("TAG", "授权成功");
    }

    @Override
    public void permissionRegisterError(String... permissions) {
        permissionHelper.showGoSettingPermissionsDialog("定位");
    }

    protected void onDestroy() {
        super.onDestroy();
        permissionHelper.destroy();
        if (acBinder != null) {
            unbindService(acConnection);
        }
    }

    private void activateSN() {
        int code = acBinder.login(guideEt.getText().toString().trim());
        if (code == 0) {
            SPUtils.put(GuideActivity.this, "frist", false);
            SPUtils.put(GuideActivity.this, "sn", false);
            //new AlertDialog.Builder(SpalshActivity.this).setMessage("授权成功!").show();
            Toasty.success(this, "相机授权成功!", Toast.LENGTH_SHORT,true).show();
            //Toast.makeText(this, "授权成功!", Toast.LENGTH_SHORT).show();

        } else if (code == 1795) {
            //new AlertDialog.Builder(SpalshActivity.this).setMessage("程序激活失败，激活的机器数量已达上限，授权码不能再更多的机器上使用").show();
            Toast.makeText(this, "激活的机器数量已达上限，授权码不能再更多的机器上使用", Toast.LENGTH_SHORT).show();
        } else if (code == 1793) {
            //new AlertDialog.Builder(SpalshActivity.this).setMessage("授权码已过期").show();
            Toast.makeText(this, "授权码已过期", Toast.LENGTH_SHORT).show();
        } else if (code == 276) {
            //new AlertDialog.Builder(SpalshActivity.this).setMessage("没有找到相应的本地授权许可数据文件").show();
            Toast.makeText(this, "没有找到相应的本地授权许可数据文件", Toast.LENGTH_SHORT).show();
        } else if (code == 284) {
            //new AlertDialog.Builder(SpalshActivity.this).setMessage("授权码输入错误").show();
            //Toast.makeText(this, "授权码输入错误", Toast.LENGTH_SHORT).show();
        } else {
            //new AlertDialog.Builder(SpalshActivity.this).setMessage("错误码：" + code).show();
            Toast.makeText(this, "错误码：" + code, Toast.LENGTH_SHORT).show();
        }
        startActivity(new Intent(GuideActivity.this, SpalshActivity.class));
        finish();
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