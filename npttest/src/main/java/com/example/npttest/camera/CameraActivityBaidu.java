package com.example.npttest.camera;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.text.format.Time;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.npttest.R;
import com.example.npttest.tool.RecognizeTool;
import com.example.npttest.util.CameraUtils;
import com.example.npttest.util.DeleteFileUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class CameraActivityBaidu extends Activity implements SurfaceHolder.Callback, Animation.AnimationListener, Camera.PreviewCallback {
    private Camera camera;
    private SurfaceView surfaceView;
    private static final String PATH = Environment
            .getExternalStorageDirectory().toString() + "/DCIM/PlatePic/";
    private ImageButton back_btn, flash_btn, back, take_pic;
    //private ViewfinderView myview;
    private int width, height;
    private TimerTask timer;
    private int preWidth = 0;
    private int preHeight = 0;
    private String number = "", color = "";
    private boolean isFatty = false;
    private SurfaceHolder holder;
    private int iInitPlateIDSDK = -1;
    private String[] fieldvalue = new String[10];
    private int rotation = 90;
    private static int tempUiRot = 0;
    private Bitmap bitmap1;
    private Vibrator mVibrator;
    private byte[] tempData;
    private boolean isSuccess = false;
    private Animation scaleAnimation;
    private boolean isAnimationEnd = false;
    private static int EXPIRED_SECONDS = 2592000;
    private static final int REQUEST_CODE_LICENSE_PLATE = 122;
    public static final String KEY_OUTPUT_FILE_PATH = "outputFilePath";
    public static final String KEY_CONTENT_TYPE = "contentType";
    public static final String KEY_NATIVE_TOKEN = "nativeToken";
    public static final String KEY_NATIVE_ENABLE = "nativeEnable";

    public static final String CONTENT_TYPE_GENERAL = "general";
    public static final String CONTENT_TYPE_ID_CARD_FRONT = "IDCardFront";
    public static final String CONTENT_TYPE_ID_CARD_BACK = "IDCardBack";
    public static final String CONTENT_TYPE_BANK_CARD = "bankCard";

    private boolean isCamera = true;// ???????????????????? true:?????? false:???????
    private boolean recogType = true;// ????????????????????????????? true:?????? false:???????
    private boolean isFirstPic = true;// ???????????????????

    private boolean isClick = false;
    int nums = 1;
    private String path;
    private String carnum;
    private boolean isgo = true;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // DisplayMetrics ?????????????????????????????????????????????
            DisplayMetrics metric = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metric);
            width = metric.widthPixels; // ????????????
            height = metric.heightPixels; // ????????????
            switch (msg.what) {
                case 0:
                    rotation = 90;
                    break;
                case 1:
                    rotation = 0;
                    break;
                case 2:
                    rotation = 270;
                    break;
                case 3:
                    rotation = 180;
                    break;

            }
            setButton();
            initCamera(holder, rotation);
            super.handleMessage(msg);

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        int uiRot = getWindowManager().getDefaultDisplay().getRotation();// ?????????????

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_camera);

        /*isCamera = getIntent().getBooleanExtra("camera", false);
        recogType = getIntent().getBooleanExtra("camera", false);*/

        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        width = metric.widthPixels; // ????????????
        height = metric.heightPixels; // ????????????

        switch (uiRot) {
            case 0:// ?????????
                tempUiRot = 0;
                rotation = 90;
                break;
            case 1:// ????90??
                tempUiRot = 1;
                rotation = 0;
                break;
            case 2:// ????180??
                tempUiRot = 2;
                rotation = 270;
                break;
            case 3:// ????270??
                tempUiRot = 3;
                rotation = 180;
                break;
        }
        findView();
        if (width * 3 == height * 4) {
            isFatty = true;
        }
    }

    private void findView() {
        surfaceView = (SurfaceView) findViewById(R.id.surfaceViwe_video);
        back_btn = (ImageButton) findViewById(R.id.back_camera);
        flash_btn = (ImageButton) findViewById(R.id.flash_camera);
        back = (ImageButton) findViewById(R.id.back);
        //myview = (ViewfinderView) findViewById(R.id.myview);
        take_pic = (ImageButton) findViewById(R.id.take_pic_btn);
        setButton();
        holder = surfaceView.getHolder();
        holder.addCallback(CameraActivityBaidu.this);
        back_btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Log.e("TAG", "点击了横向返回");
                Intent intent = new Intent();
                intent.putExtra("number", "null");
                //intent.putExtra("color", "null");
                intent.putExtra("path", "null");
                setResult(0x128, intent);
                Log.e("TAG", "0x128");
                //startActivity(intent);
                CameraActivityBaidu.this.finish();
            }
        });
        back.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Log.e("TAG", "点击了返回");
                //finish();
                Intent intent = new Intent();
                intent.putExtra("number", "null");
                // intent.putExtra("color", "null");
                intent.putExtra("path", "null");
                setResult(0x127, intent);
                Log.e("TAG", "0x127");
                //startActivity(intent);
                CameraActivityBaidu.this.finish();
            }
        });
        flash_btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Log.e("TAG", "点击了闪光灯");
                // Toast.makeText(CameraActivityBaidu.this, "点击了闪光灯", Toast.LENGTH_SHORT).show();
                if (!getPackageManager().hasSystemFeature(
                        PackageManager.FEATURE_CAMERA_FLASH)) {
                    Toast.makeText(getApplicationContext(), "?????????????!",
                            Toast.LENGTH_SHORT).show();
                } else {
                    if (camera != null) {
                        Camera.Parameters parameters = camera.getParameters();
                        String flashMode = parameters.getFlashMode();
                        if (flashMode
                                .equals(Camera.Parameters.FLASH_MODE_TORCH)) {
                            parameters
                                    .setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                            parameters.setExposureCompensation(0);
                        } else {
                            parameters
                                    .setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);// ????????
                            parameters.setExposureCompensation(-1);

                        }
                        try {
                            camera.setParameters(parameters);
                        } catch (Exception e) {
                            Toast.makeText(
                                    getApplicationContext(),
                                    getResources().getString(
                                            getResources().getIdentifier(
                                                    "no_flash", "string",
                                                    getPackageName())),
                                    Toast.LENGTH_SHORT).show();
                        }
                        camera.startPreview();
                    }
                }
            }

        });
        take_pic.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                Log.e("TAG", "点击了拍照");
                isClick = true;
                if (isClick) {
                    scaleAnimation = new ScaleAnimation(1.0f, 1.2f, 1.0f, 1.2f,
                            width / 2, height / 2);
                    scaleAnimation.setDuration(300);
                    scaleAnimation.setRepeatMode(Animation.REVERSE);
                    scaleAnimation.setRepeatCount(1);
                    //myview.startAnimation(scaleAnimation);
                    scaleAnimation.setAnimationListener(CameraActivityBaidu.this);

                    camera.autoFocus(new Camera.AutoFocusCallback() {
                        public void onAutoFocus(boolean success, Camera camera) {
                            isSuccess = success;
                            //??????????????(??:?????????????success?????false),??????????????????if???;
                            //???????????????????????????????
                            if (!isSuccess) {
                                isSuccess = true;
                            }
                        }
                    });

                    if (timer != null) {
                        timer.cancel();
                    }

                    if (!isSuccess || !isAnimationEnd) {
                        MyThread myThread = new MyThread();
                        myThread.start();
                    } else {
                        isCamera = true;
                    }

                    isClick = false;
                }


            }
        });

    }


    private void setButton() {

        int back_w;
        int back_h;
        int flash_w;
        int flash_h;
        int take_h;
        int take_w;
        RelativeLayout.LayoutParams layoutParams;
        switch (rotation) {
            case 90:
            case 270:

                back.setVisibility(View.VISIBLE);
                back_btn.setVisibility(View.GONE);
                back_h = (int) (height * 0.067);
                back_w = (int) (back_h * 1);
                layoutParams = new RelativeLayout.LayoutParams(back_w, back_h);
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT,
                        RelativeLayout.TRUE);
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP,
                        RelativeLayout.TRUE);

                layoutParams.topMargin = (int) (((height - width * 0.95) / 2 - back_h) / 2);
                layoutParams.leftMargin = (int) (width * 0.105);
                back.setLayoutParams(layoutParams);

                flash_h = (int) (height * 0.067);
                flash_w = (int) (flash_h * 1);
                layoutParams = new RelativeLayout.LayoutParams(flash_w, flash_h);
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,
                        RelativeLayout.TRUE);
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP,
                        RelativeLayout.TRUE);

                layoutParams.topMargin = (int) (((height - width * 0.95) / 2 - flash_h) / 2);
                layoutParams.rightMargin = (int) (width * 0.105);
                flash_btn.setLayoutParams(layoutParams);

                take_h = (int) (height * 0.105);
                take_w = (int) (take_h * 1);
                layoutParams = new RelativeLayout.LayoutParams(take_w, take_h);
                layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL,
                        RelativeLayout.TRUE);
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,
                        RelativeLayout.TRUE);

                layoutParams.bottomMargin = (int) (width * 0.105);
                take_pic.setLayoutParams(layoutParams);
                break;
            case 0:
            case 180:

                back_btn.setVisibility(View.VISIBLE);
                back.setVisibility(View.GONE);
                back_w = (int) (width * 0.0675);
                back_h = (int) (back_w * 1);
                layoutParams = new RelativeLayout.LayoutParams(back_w, back_h);
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT,
                        RelativeLayout.TRUE);
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,
                        RelativeLayout.TRUE);

                layoutParams.leftMargin = (int) (((width - height * 0.95) / 2 - back_h) / 2);
                layoutParams.bottomMargin = (int) (height * 0.105);
                back_btn.setLayoutParams(layoutParams);

                flash_w = (int) (width * 0.067);
                flash_h = (int) (flash_w * 1);
                layoutParams = new RelativeLayout.LayoutParams(flash_w, flash_h);
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT,
                        RelativeLayout.TRUE);
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP,
                        RelativeLayout.TRUE);

                layoutParams.leftMargin = (int) (((width - height * 0.95) / 2 - back_h) / 2);
                layoutParams.topMargin = (int) (height * 0.105);
                flash_btn.setLayoutParams(layoutParams);

                take_h = (int) (width * 0.105);
                take_w = (int) (take_h * 1);
                layoutParams = new RelativeLayout.LayoutParams(take_w, take_h);
                layoutParams.addRule(RelativeLayout.CENTER_VERTICAL,
                        RelativeLayout.TRUE);
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,
                        RelativeLayout.TRUE);

                layoutParams.rightMargin = (int) (height * 0.105);
                take_pic.setLayoutParams(layoutParams);
                break;
        }
        if (isCamera) {
            take_pic.setVisibility(View.GONE);
        } else {
            take_pic.setVisibility(View.VISIBLE);
        }
    }


    class MyThread extends Thread {

        @Override
        public void run() {
            super.run();
            while (!isSuccess || !isAnimationEnd) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            isCamera = true;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (camera == null) {
            try {
                camera = Camera.open();
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
        try {
            camera.setPreviewDisplay(holder);
            initCamera(holder, rotation);
            Timer time = new Timer();
            if (timer == null) {
                timer = new TimerTask() {
                    public void run() {
                        isSuccess = false;
                        if (camera != null) {
                            try {
                                camera.autoFocus(new Camera.AutoFocusCallback() {
                                    public void onAutoFocus(boolean success,
                                                            Camera camera) {
                                        isSuccess = success;
                                        //??????????????(??:?????????????success?????false),??????????????????if???;
                                        //???????????????????????????????
                                        if (!isSuccess) {
                                            isSuccess = true;
                                        }
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    ;
                };
            }
            time.schedule(timer, 500, 2500);
        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    /**
     * @param @param holder
     * @param @param r ??????????
     * @return void ????????
     * @throws
     * @Title: initCamera
     * @Description: (????????)
     */
    @TargetApi(14)
    private void initCamera(SurfaceHolder holder, int r) {

        if (camera != null) {
            // ???????????
            Camera.Parameters parameters = camera.getParameters();
            // ??????????????
            List<Camera.Size> list = parameters.getSupportedPreviewSizes();
            Camera.Size size;
            int length = list.size();
            int previewWidth = 640;
            int previewheight = 480;
            int second_previewWidth = 0;
            int second_previewheight = 0;
            if (length == 1) {
                size = list.get(0);
                previewWidth = size.width;
                previewheight = size.height;

            } else {
                for (int i = 0; i < length; i++) {
                    size = list.get(i);

                    if (isFatty) {
                        if (size.height <= 960 || size.width <= 1280) {

                            second_previewWidth = size.width;
                            second_previewheight = size.height;

                            if (previewWidth <= second_previewWidth
                                    && second_previewWidth * 3 == second_previewheight * 4) {

                            }
                            previewWidth = second_previewWidth;
                            previewheight = second_previewheight;
                        }
                    } else {

                        if (size.height <= 960 || size.width <= 1280) {
                            second_previewWidth = size.width;
                            second_previewheight = size.height;
                            if (previewWidth <= second_previewWidth) {
                                previewWidth = second_previewWidth;
                                previewheight = second_previewheight;
                            }
                        }
                    }
                }
            }

            preWidth = previewWidth;
            preHeight = previewheight;
            Log.e("TAG", "宽" + preWidth + "高" + preHeight);
            parameters.setPictureFormat(ImageFormat.JPEG);
            parameters.setPreviewSize(preWidth, preHeight);
            if (getPackageManager().hasSystemFeature(
                    PackageManager.FEATURE_CAMERA_AUTOFOCUS)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            }
            camera.setPreviewCallback(CameraActivityBaidu.this);
            camera.setParameters(parameters);

            if (rotation == 90 || rotation == 270) {
                if (width < 1080) {
                    camera.stopPreview();
                }
            } else {
                if (height < 1080) {
                    camera.stopPreview();
                }
            }

            camera.setDisplayOrientation(r);

            try {
                camera.setPreviewDisplay(holder);
            } catch (IOException e) {
                e.printStackTrace();
            }
            camera.startPreview();

            if (rotation == 90 || rotation == 270) {
                if (width < 1080) {
                    camera.setPreviewCallback(CameraActivityBaidu.this);
                }
            } else {
                if (height < 1080) {
                    camera.setPreviewCallback(CameraActivityBaidu.this);
                }
            }
            camera.cancelAutoFocus();
        }
    }


    @Override
    public void surfaceChanged(final SurfaceHolder holder, int format, int width, int height) {
        if (camera != null) {
            camera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, final Camera camera) {
                    if (success) {
                        synchronized (camera) {
                            new Thread() {
                                public void run() {
                                    initCamera(holder, rotation);
                                    super.run();
                                }
                            }.start();
                        }
                    }
                }
            });
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        try {
            if (camera != null) {
                camera.setPreviewCallback(null);
                camera.stopPreview();
                camera.release();
                camera = null;
            }
        } catch (Exception e) {
        }
    }


    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        //Log.e("TAG","data:"+data);

        if (data == null || isSuccess == false) {
//			Log.i("wu", "??????????");
            return;
        }
        // ???????????????
        int uiRot = getWindowManager().getDefaultDisplay().getRotation();// ?????????????

        if (uiRot != tempUiRot) {
            Log.e("TAG", "uiRot:" + uiRot);
            Message mesg = new Message();
            mesg.what = uiRot;
            handler.sendMessage(mesg);
            tempUiRot = uiRot;
        }

        if (!take_pic.isEnabled()) {
            take_pic.setEnabled(true);
        }

        if (isgo) {
            nums++;
            if (nums == 3) {
                tempData = data;
                Log.e("TAG", "tempData" + tempData);
                int[] datas = CameraUtils.convertYUV420_NV21toARGB8888(tempData, preWidth, preHeight);
                bitmap1 = Bitmap.createBitmap(datas, preWidth, preHeight, Bitmap.Config.ARGB_8888);
                Matrix matrix = new Matrix();
                matrix.reset();
                if (rotation == 90) {
                    matrix.setRotate(90);
                } else if (rotation == 180) {
                    matrix.setRotate(180);
                } else if (rotation == 270) {
                    matrix.setRotate(270);
                }
                bitmap1 = Bitmap.createBitmap(bitmap1, 0, 0,
                        bitmap1.getWidth(),
                        bitmap1.getHeight(), matrix, true);
                Log.e("TAG", "是同时的吗");
                path = savePicture(bitmap1, "ppm");
                Log.e("TAG", "path:" + path);
                isgo = false;
                RecognizeTool.recLicensePlate(path, new RecognizeTool.ServiceListener() {
                    @Override
                    public void onResult(String result) {
                        //infoPopText(result);
                        if (result.length() > 50) {
                            try {
                                JSONObject jsonObject = new JSONObject(result);
                                JSONObject jsonObject1 = jsonObject.getJSONObject("words_result");
                                String carnum = jsonObject1.getString("number");
                                Intent intent = new Intent();
                                intent.putExtra("path", path);
                                intent.putExtra("number", carnum);
                                setResult(0x123, intent);
                                finish();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            DeleteFileUtil.mydeleteFile(path);
                            isgo = true;
                        }
                        //Toast.makeText(CameraActivityBaidu.this, "result:"+result, Toast.LENGTH_SHORT).show();
                        Log.e("TAG", "result:" + result);
                    }
                });

                nums = 1;
            }
        }

    }

   /* private void getdata(Bitmap bitmap) throws IOException, JSONException, NoSuchAlgorithmException, KeyManagementException {
        Log.e("TAG","开始识别车牌");
        String imageData = bitmapToBase64(bitmap);
        String url = "https://api.youtu.qq.com/youtu/ocrapi/plateocr"; //车牌识别接口
        //String url = "https://api.youtu.qq.com/youtu/carapi/carclassify";//车辆属性识别接口
        JSONObject postData = new JSONObject();
        postData.put("image",imageData);
        postData.put("app_id","10115192");
        StringBuffer mySign = new StringBuffer("");
        YoutuSign.appSign("10115192", "AKID1wjQyLcjkaEgx3Q8B4qcEi1LUSFqfUaV", "jPuDf5PY1KSbhdOzOdholW6jCZmogq12",
                System.currentTimeMillis() / 1000 + EXPIRED_SECONDS,
                "", mySign);
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, new TrustManager[]{new TrustAnyTrustManager()}, new java.security.SecureRandom());
        OkHttpClient okHttpClient = new OkHttpClient.Builder().sslSocketFactory(sc.getSocketFactory(),new TrustAnyTrustManager()).build();
        OkHttpUtils.initClient(okHttpClient);
        Log.e("TAG", "请求参数：" +postData.toString());
//        Log.e("TAG", "签名：" + mySign);
//        Log.e("TAG", "base64图片：" + imageData.getBytes("UTF-8"));
        OkHttpUtils.postString().url(url)
                .content(postData.toString())
                .mediaType(MediaType.parse("text/json;charset=utf-8"))
                .addHeader("Authorization", mySign.toString())
                .build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                Log.e("TAG", "错误"+e.toString());
                Toast.makeText(CameraActivity.this, "错误："+e.toString(), Toast.LENGTH_SHORT).show();
                //tv1.setText(e.toString());
            }

            @Override
            public void onResponse(String response, int id) {
                //tv1.setText(response);
                Log.e("TAG", "返回："+response);
                //Toast.makeText(CameraActivity.this, "返回："+response, Toast.LENGTH_SHORT).show();
                try {
                    JSONObject jsonObject=new JSONObject(response);
                    int errorcode=jsonObject.getInt("errorcode");
                    JSONArray jsonArray=jsonObject.getJSONArray("items");
                    Log.e("TAG","识别的大小"+jsonArray.length());
                    if (errorcode==0&&jsonArray.length()>0){
                        JSONObject CarnumJsonObject = jsonArray.getJSONObject(0);
                        carnum=CarnumJsonObject.getString("itemstring");
                        path = savePicture(bitmap1, "ppm");
                        Intent intent=new Intent();
                        intent.putExtra("path", path);
                        intent.putExtra("carnum", carnum);

                        setResult(0x123,intent);
                        Log.e("TAG","path:"+path);
                        Log.e("TAG","结束识别车牌off");
                        finish();
                    }else {
                        isgo=true;
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //isgo=true;
            }
        });
    }*/

    /**
     * bitmap转为base64
     *
     * @param bitmap
     * @return
     */
    public static String bitmapToBase64(Bitmap bitmap) throws IOException {

        String result = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

                baos.flush();
                baos.close();

                byte[] bitmapBytes = baos.toByteArray();
                result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
            }
        } catch (IOException e) {
            throw e;
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (IOException e) {
                throw e;
            }
        }
        return result;
    }

    /**
     * base64转为bitmap
     *
     * @param base64Data
     * @return
     */
    public static Bitmap base64ToBitmap(String base64Data) {
        byte[] bytes = Base64.decode(base64Data, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {
        isAnimationEnd = true;
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }


    public static String savePicture(Bitmap bitmap, String a) {
        String strCaptureFilePath = PATH + "plateID_" + pictureName() + a
                + ".jpg";
        File dir = new File(PATH);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(strCaptureFilePath);
        if (file.exists()) {
            file.delete();
        }
        try {
            file.createNewFile();
            BufferedOutputStream bos = new BufferedOutputStream(
                    new FileOutputStream(file));

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return strCaptureFilePath;
    }

    public static String pictureName() {
        String str = "";
        Time t = new Time();
        t.setToNow(); // ????????
        int year = t.year;
        int month = t.month + 1;
        int date = t.monthDay;
        int hour = t.hour; // 0-23
        int minute = t.minute;
        int second = t.second;
        if (month < 10)
            str = String.valueOf(year) + "0" + String.valueOf(month);
        else {
            str = String.valueOf(year) + String.valueOf(month);
        }
        if (date < 10)
            str = str + "0" + String.valueOf(date + "_");
        else {
            str = str + String.valueOf(date + "_");
        }
        if (hour < 10)
            str = str + "0" + String.valueOf(hour);
        else {
            str = str + String.valueOf(hour);
        }
        if (minute < 10)
            str = str + "0" + String.valueOf(minute);
        else {
            str = str + String.valueOf(minute);
        }
        if (second < 10)
            str = str + "0" + String.valueOf(second);
        else {
            str = str + String.valueOf(second);
        }
        return str;
    }
}