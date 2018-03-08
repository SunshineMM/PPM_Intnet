package com.example.npttest.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.inputmethodservice.KeyboardView;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.OSSLogToFileUtils;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSStsTokenCredentialProvider;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;
import com.example.npttest.App;
import com.example.npttest.R;
import com.example.npttest.camera.CameraActivity;
import com.example.npttest.constant.Constant;
import com.example.npttest.manager.ActivityManager;
import com.example.npttest.util.FileUtils;
import com.example.npttest.util.LicenseKeyboardUtil_input;
import com.example.npttest.util.MD5Util;
import com.example.npttest.util.PictureUtil;
import com.example.npttest.util.SPUtils;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;
import com.kyleduo.switchbutton.SwitchButton;
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

import static com.example.npttest.util.FileUtils.SDPATH;
import static com.example.npttest.util.LicenseKeyboardUtil_input.currentEditText_input;

/**
 * Created by liuji on 2017/8/22.
 */

public class InputCarnum extends Activity implements CompoundButton.OnCheckedChangeListener {

    @Bind(R.id.inputcar_return)
    ImageView inputcarReturn;
    @Bind(R.id.textView7)
    TextView textView7;
    @Bind(R.id.inputcar_camera)
    ImageView inputcarCamera;
    @Bind(R.id.inputcar_inputbox1)
    EditText inputcarInputbox1;
    @Bind(R.id.inputcar_inputbox2)
    EditText inputcarInputbox2;
    @Bind(R.id.inputcar_inputbox3)
    EditText inputcarInputbox3;
    @Bind(R.id.inputcar_inputbox4)
    EditText inputcarInputbox4;
    @Bind(R.id.inputcar_inputbox5)
    EditText inputcarInputbox5;
    @Bind(R.id.inputcar_inputbox6)
    EditText inputcarInputbox6;
    @Bind(R.id.inputcar_inputbox7)
    EditText inputcarInputbox7;
    @Bind(R.id.inputcar_inputbox8)
    EditText inputcarInputbox8;
    @Bind(R.id.inputcar_lin_input)
    LinearLayout inputcarLinInput;
    @Bind(R.id.input_sbtn)
    SwitchButton inputSbtn;
    @Bind(R.id.inputcar_img)
    ImageView inputcarImg;
    @Bind(R.id.inputcar_motoimg)
    ImageView inputcarMotoimg;
    @Bind(R.id.inputcar_mototext)
    TextView inputcarMototext;
    @Bind(R.id.inputcar_moto)
    LinearLayout inputcarMoto;
    @Bind(R.id.inputcar_smallcarimg)
    ImageView inputcarSmallcarimg;
    @Bind(R.id.inputcar_smallcartext)
    TextView inputcarSmallcartext;
    @Bind(R.id.inputcar_smallcar)
    LinearLayout inputcarSmallcar;
    @Bind(R.id.inputcar_middleimg)
    ImageView inputcarMiddleimg;
    @Bind(R.id.inputcar_middletext)
    TextView inputcarMiddletext;
    @Bind(R.id.inputcar_middle)
    LinearLayout inputcarMiddle;
    @Bind(R.id.inputcar_bigcarimg)
    ImageView inputcarBigcarimg;
    @Bind(R.id.inputcar_bigcartextt)
    TextView inputcarBigcartextt;
    @Bind(R.id.inputcar_bigcar)
    LinearLayout inputcarBigcar;
    @Bind(R.id.inputcarinto_btn)
    Button inputcarintoBtn;
    @Bind(R.id.inputcarout_btn)
    Button inputcaroutBtn;
    @Bind(R.id.keyboard_view)
    KeyboardView keyboardView;
    private String carnumb, color, bitmapPath;
    private LicenseKeyboardUtil_input keyboardUtil;
    private EditText edits[];
    private String editS;
    private int carType = 2;
    private Bitmap bitmap = null;
    private long carintotime;
    private String bitPath;
    private ZLoadingDialog dialog1, dialog;
    private String comCity;
    private String car_type;
    private int rstat, ptype, ctype, cdtp, preson;
    private String jfType, carnum;//计费类型
    private String comfirmYy, putbitmappath;//确认原因
    SynthesizerListener mSynListener;
    private OSS oss;
    MyHandler handler;
    private long itime;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inputcar);
        ButterKnife.bind(this);
        if (App.serverurl != null) {
            getosskey(App.serverurl);
        }
        SpeechUtility.createUtility(InputCarnum.this, SpeechConstant.APPID + "=59df2c0c");
        edits = new EditText[]{inputcarInputbox1, inputcarInputbox2, inputcarInputbox3,
                inputcarInputbox4, inputcarInputbox5, inputcarInputbox6,
                inputcarInputbox7, inputcarInputbox8};
        keyboardUtil = new LicenseKeyboardUtil_input(InputCarnum.this, edits);
        inputcarSmallcar.setBackgroundColor(Color.parseColor("#1e7db4"));
        inputcarSmallcarimg.setImageResource(R.mipmap.ic_small_car_w);
        inputcarSmallcartext.setTextColor(Color.parseColor("#FFFFFF"));
        comCity = (String) SPUtils.get(InputCarnum.this, Constant.COM_CITY, "");
        char[] chars = comCity.toCharArray();
        if (TextUtils.isEmpty(comCity)) {

        } else {
            inputcarInputbox1.setText(String.valueOf(chars[0]));
            inputcarInputbox2.setText(String.valueOf(chars[1]));
        }
        Boolean aBoolean = (Boolean) SPUtils.get(InputCarnum.this, "open_new_car", false);
        inputSbtn.setCheckedImmediately(aBoolean);
        inputSbtn.setOnCheckedChangeListener(this);
        if (aBoolean) {
            inputcarInputbox8.setVisibility(View.VISIBLE);
            LicenseKeyboardUtil_input.etsize = 7;
        } else {
            inputcarInputbox8.setVisibility(View.GONE);
            LicenseKeyboardUtil_input.etsize = 6;
        }
        ActivityManager.getInstance().addActivity(this);
        handler=new MyHandler();
    }

    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0x0123:
                    Log.e("TAG", "上传成功");
                    break;
                case 0x0124:
                    Log.e("TAG", "网络异常，上传失败");
                    break;
                case 0x0125:
                    Log.e("TAG", "服务异常，上传失败");
                    break;
            }
        }
    }

    private void initoss() {
        /**
         * 初始化oss
         */
        String endpoint = Constant.EndPoint;
        // 在移动端建议使用STS方式初始化OSSClient。
        // 更多信息可查看sample 中 sts 使用方式(https://github.com/aliyun/aliyun-oss-android-sdk/tree/master/app/src/main/java/com/alibaba/sdk/android/oss/app)
        OSSCredentialProvider credentialProvider = new OSSStsTokenCredentialProvider(Constant.AccessKeyId, Constant.AccessKeySecret, Constant.SecurityToken);
        //该配置类如果不设置，会有默认配置，具体可看该类
        ClientConfiguration conf = new ClientConfiguration();
        conf.setConnectionTimeout(15 * 1000); // 连接超时，默认15秒
        conf.setSocketTimeout(15 * 1000); // socket超时，默认15秒
        conf.setMaxConcurrentRequest(5); // 最大并发请求数，默认5个
        conf.setMaxErrorRetry(2); // 失败后最大重试次数，默认2次
        //开启可以在控制台看到日志，并且会支持写入手机sd卡中的一份日志文件位置在SDCard_path\OSSLog\logs.csv  默认不开启
        //日志会记录oss操作行为中的请求数据，返回数据，异常信息
        //例如requestId,response header等
        //android_version：5.1  android版本
        //mobile_model：XT1085  android手机型号
        //network_state：connected  网络状况
        //network_type：WIFI 网络连接类型
        //具体的操作行为信息:
        //[2017-09-05 16:54:52] - Encounter local execpiton: //java.lang.IllegalArgumentException: The bucket name is invalid.
        //A bucket name must:
        //1) be comprised of lower-case characters, numbers or dash(-);
        //2) start with lower case or numbers;
        //3) be between 3-63 characters long.
        //------>end of log
        OSSLog.enableLog();
        oss = new OSSClient(getApplicationContext(), endpoint, credentialProvider);
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
                Toast.makeText(InputCarnum.this, "请检查网络", Toast.LENGTH_SHORT).show();
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
                        Constant.AccessKeyId = datajson.getString("accessKeyId");
                        Constant.AccessKeySecret = datajson.getString("accessKeySecret");
                        Constant.SecurityToken = datajson.getString("securityToken");
                        Log.e("TAG", "AccessKeyId:" + Constant.AccessKeyId + "\n" + "AccessKeySecret:" + Constant.AccessKeySecret + "\n" + "SecurityToken:" + Constant.SecurityToken);
                        initoss();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void putdate(String bucket, String imgurl, String fileurl) {
        ///storage/emulated/0/DCIM/PlatePic/plateID_20171018_204458null.jpg
        OSSLogToFileUtils.reset();
        PutObjectRequest put = new PutObjectRequest(bucket, imgurl, fileurl);
        //Toast.makeText(this, put.getBucketName(), Toast.LENGTH_SHORT).show();
        // 异步上传时可以设置进度回调
        put.setProgressCallback(new OSSProgressCallback<PutObjectRequest>() {
            @Override
            public void onProgress(PutObjectRequest request, long currentSize, long totalSize) {
                //Log.e("TAG", "currentSize: " + currentSize + " totalSize: " + totalSize);
            }
        });
        OSSAsyncTask task = oss.asyncPutObject(put, new OSSCompletedCallback<PutObjectRequest, PutObjectResult>() {
            @Override
            public void onSuccess(PutObjectRequest request, PutObjectResult result) {
                Log.e("TAG", "UploadSuccess");
                handler.sendEmptyMessage(0x123);
            }

            @Override
            public void onFailure(PutObjectRequest request, ClientException clientExcepion, ServiceException serviceException) {
                // 请求异常
                if (clientExcepion != null) {
                    // 本地异常如网络异常等
                    clientExcepion.printStackTrace();
                    handler.sendEmptyMessage(0x0124);
                }
                if (serviceException != null) {
                    // 服务异常
                    Log.e("TAG", serviceException.getErrorCode());
                    Log.e("TAG", serviceException.getRequestId());
                    Log.e("TAG", serviceException.getHostId());
                    Log.e("TAG", serviceException.getRawMessage());
                    handler.sendEmptyMessage(0x0125);
                }
            }
        });
        // task.cancel(); // 可以取消任务
        //task.waitUntilFinished(); // 可以等待直到任务完成
    }

    private void carin_start_voice() {
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
                    + " " + String.valueOf(carnumber[3]) + " " + String.valueOf(carnumber[4]) + " " + String.valueOf(carnumber[5]) + " " + String.valueOf(carnumber[6]) + "，" + "入场成功。", mSynListener);
            Log.e("TAG", String.valueOf(carnumber[0]) + " " + String.valueOf(carnumber[1]) + " " + String.valueOf(carnumber[2])
                    + " " + String.valueOf(carnumber[3]) + " " + String.valueOf(carnumber[4]) + " " + String.valueOf(carnumber[5]) + " " + String.valueOf(carnumber[6]) + "，" + "入场成功。");
        } else if (carnumber.length == 8) {
            mTts.startSpeaking(String.valueOf(carnumber[0]) + " " + String.valueOf(carnumber[1]) + " " + String.valueOf(carnumber[2])
                    + " " + String.valueOf(carnumber[3]) + " " + String.valueOf(carnumber[4]) + " " + String.valueOf(carnumber[5]) + " " + String.valueOf(carnumber[6]) + " " + String.valueOf(carnumber[7]) + "，" + "入场成功。", mSynListener);
        }

        Log.e("TAG", String.valueOf(carnumber[0]) + String.valueOf(carnumber[1]) + String.valueOf(carnumber[2])
                + String.valueOf(carnumber[3]) + String.valueOf(carnumber[4]) + String.valueOf(carnumber[5]) + String.valueOf(carnumber[6]) + "");
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

    //初始化 按键框的背景
    private void initcolor1() {
        edits[0].setBackgroundResource(R.drawable.keyboard_bg_white);
        edits[1].setBackgroundResource(R.drawable.keyboard_bg_white);
        edits[2].setBackgroundResource(R.drawable.keyboard_bg_white);
        edits[3].setBackgroundResource(R.drawable.keyboard_bg_white);
        edits[4].setBackgroundResource(R.drawable.keyboard_bg_white);
        edits[5].setBackgroundResource(R.drawable.keyboard_bg_white);
        edits[6].setBackgroundResource(R.drawable.keyboard_bg_white);
        edits[7].setBackgroundResource(R.drawable.keyboard_bg_white);

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @OnClick({R.id.inputcar_return, R.id.inputcar_camera, R.id.inputcar_inputbox1, R.id.inputcar_inputbox2,
            R.id.inputcar_inputbox3, R.id.inputcar_inputbox4, R.id.inputcar_inputbox5, R.id.inputcar_inputbox6,
            R.id.inputcar_inputbox7, R.id.inputcar_inputbox8, R.id.inputcarinto_btn, R.id.inputcarout_btn,
            R.id.inputcar_moto, R.id.inputcar_smallcar, R.id.inputcar_middle, R.id.inputcar_bigcar})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.inputcar_return:
                finish();
                break;
            case R.id.inputcar_camera:
                //jumpVideoRecog();
                Intent intent = new Intent(InputCarnum.this, CameraActivity.class);
                intent.putExtra("camera", false);
                //startActivity(intent);
                startActivityForResult(intent, 0x11);
                break;
            case R.id.inputcar_inputbox1:
                currentEditText_input = 0;
                initcolor1();
                edits[LicenseKeyboardUtil_input.currentEditText_input].setBackgroundResource(R.drawable.keyboard_bg_red);
                keyboardUtil.showKeyboard();
                break;
            case R.id.inputcar_inputbox2:
                currentEditText_input = 1;
                initcolor1();
                edits[LicenseKeyboardUtil_input.currentEditText_input].setBackgroundResource(R.drawable.keyboard_bg_red);
                keyboardUtil.showKeyboard();
                break;
            case R.id.inputcar_inputbox3:
                currentEditText_input = 2;
                initcolor1();
                edits[LicenseKeyboardUtil_input.currentEditText_input].setBackgroundResource(R.drawable.keyboard_bg_red);
                keyboardUtil.showKeyboard();

                break;
            case R.id.inputcar_inputbox4:
                currentEditText_input = 3;
                initcolor1();
                edits[LicenseKeyboardUtil_input.currentEditText_input].setBackgroundResource(R.drawable.keyboard_bg_red);
                keyboardUtil.showKeyboard();

                break;
            case R.id.inputcar_inputbox5:
                currentEditText_input = 4;
                initcolor1();
                edits[LicenseKeyboardUtil_input.currentEditText_input].setBackgroundResource(R.drawable.keyboard_bg_red);
                keyboardUtil.showKeyboard();

                break;
            case R.id.inputcar_inputbox6:
                currentEditText_input = 5;
                initcolor1();
                edits[LicenseKeyboardUtil_input.currentEditText_input].setBackgroundResource(R.drawable.keyboard_bg_red);
                keyboardUtil.showKeyboard();

                break;
            case R.id.inputcar_inputbox7:
                currentEditText_input = 6;
                initcolor1();
                edits[LicenseKeyboardUtil_input.currentEditText_input].setBackgroundResource(R.drawable.keyboard_bg_red);
                keyboardUtil.showKeyboard();

                break;
            case R.id.inputcar_inputbox8:
                currentEditText_input = 7;
                initcolor1();
                edits[LicenseKeyboardUtil_input.currentEditText_input].setBackgroundResource(R.drawable.keyboard_bg_red);
                keyboardUtil.showKeyboard();
                break;
            //车辆入场
            case R.id.inputcarinto_btn:
                carintotime = gettime();
                Log.e("TAG", carintotime + "");
                switch (carType) {
                    case 1:
                        car_type = "摩托车";
                        break;
                    case 2:
                        car_type = "小型车";
                        break;
                    case 3:
                        car_type = "中型车";
                        break;
                    case 4:
                        car_type = "大型车";
                        break;
                }
                editS = inputcarInputbox1.getText().toString() +
                        inputcarInputbox2.getText().toString() +
                        inputcarInputbox3.getText().toString() +
                        inputcarInputbox4.getText().toString() +
                        inputcarInputbox5.getText().toString() +
                        inputcarInputbox6.getText().toString() +
                        inputcarInputbox7.getText().toString() +
                        inputcarInputbox8.getText().toString();

                if (TextUtils.isEmpty(inputcarInputbox1.getText().toString()) ||
                        TextUtils.isEmpty(inputcarInputbox2.getText().toString()) ||
                        TextUtils.isEmpty(inputcarInputbox3.getText().toString()) ||
                        TextUtils.isEmpty(inputcarInputbox4.getText().toString()) ||
                        TextUtils.isEmpty(inputcarInputbox5.getText().toString()) ||
                        TextUtils.isEmpty(inputcarInputbox6.getText().toString()) ||
                        TextUtils.isEmpty(inputcarInputbox7.getText().toString())) {
                    Toasty.error(this, "请输入正确的车牌号", Toast.LENGTH_SHORT, true).show();
                } else {
                    carinto(App.serverurl);
                }
                break;
            //车辆出场
            case R.id.inputcarout_btn:
                switch (carType) {
                    case 1:
                        car_type = "摩托车";
                        break;
                    case 2:
                        car_type = "小型车";
                        break;
                    case 3:
                        car_type = "中型车";
                        break;
                    case 4:
                        car_type = "大型车";
                        break;
                }
                editS = inputcarInputbox1.getText().toString() +
                        inputcarInputbox2.getText().toString() +
                        inputcarInputbox3.getText().toString() +
                        inputcarInputbox4.getText().toString() +
                        inputcarInputbox5.getText().toString() +
                        inputcarInputbox6.getText().toString() +
                        inputcarInputbox7.getText().toString() +
                        inputcarInputbox8.getText().toString();

                if (TextUtils.isEmpty(inputcarInputbox1.getText().toString()) ||
                        TextUtils.isEmpty(inputcarInputbox2.getText().toString()) ||
                        TextUtils.isEmpty(inputcarInputbox3.getText().toString()) ||
                        TextUtils.isEmpty(inputcarInputbox4.getText().toString()) ||
                        TextUtils.isEmpty(inputcarInputbox5.getText().toString()) ||
                        TextUtils.isEmpty(inputcarInputbox6.getText().toString()) ||
                        TextUtils.isEmpty(inputcarInputbox7.getText().toString())) {
                    Toasty.error(this, "请输入正确的车牌号", Toast.LENGTH_SHORT, true).show();
                } else {
                    carout(App.serverurl);

                }
                break;
            case R.id.inputcar_moto:
                initcolor();
                carType = 1;
                inputcarMoto.setBackgroundColor(Color.parseColor("#1e7db4"));
                inputcarMotoimg.setImageResource(R.mipmap.ic_moto_bike_w);
                inputcarMototext.setTextColor(Color.parseColor("#FFFFFF"));
                break;
            case R.id.inputcar_smallcar:
                carType = 2;
                initcolor();
                inputcarSmallcar.setBackgroundColor(Color.parseColor("#1e7db4"));
                inputcarSmallcarimg.setImageResource(R.mipmap.ic_small_car_w);
                inputcarSmallcartext.setTextColor(Color.parseColor("#FFFFFF"));
                break;
            case R.id.inputcar_middle:
                carType = 3;
                initcolor();
                inputcarMiddle.setBackgroundColor(Color.parseColor("#1e7db4"));
                inputcarMiddleimg.setImageResource(R.mipmap.ic_mid_truck_w);
                inputcarMiddletext.setTextColor(Color.parseColor("#FFFFFF"));
                break;
            case R.id.inputcar_bigcar:
                carType = 4;
                initcolor();
                inputcarBigcar.setBackgroundColor(Color.parseColor("#1e7db4"));
                inputcarBigcarimg.setImageResource(R.mipmap.ic_big_truck_w);
                inputcarBigcartextt.setTextColor(Color.parseColor("#FFFFFF"));
                break;
        }
    }

    static final String[] PERMISSION = new String[]{Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,// 写入权限
            Manifest.permission.READ_EXTERNAL_STORAGE, // 读取权限
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.VIBRATE, Manifest.permission.INTERNET};

    //跳转到视频扫描识别界面
  /*  public void jumpVideoRecog() {
        Intent video_intent = new Intent();
        video_intent.putExtra("camera", true);
        RecogService.recogModel = false;//true  精准模式 多帧识别  false:快速模式  单帧识别
        video_intent = new Intent(InputCarnum.this, MemoryCameraActivity.class);
        if (Build.VERSION.SDK_INT >= 23) {
            CheckPermission checkPermission = new CheckPermission(InputCarnum.this);
            if (checkPermission.permissionSet(PERMISSION)) {
                PermissionActivity.startActivityForResult(InputCarnum.this, 0, "true", PERMISSION);
                startActivityForResult(video_intent, 0x11);
            } else {
                video_intent.setClass(InputCarnum.this.getApplication(), MemoryCameraActivity.class);
                video_intent.putExtra("camera", true);
                startActivityForResult(video_intent, 0x11);
            }
        } else {
            video_intent.setClass(InputCarnum.this, MemoryCameraActivity.class);
            video_intent.putExtra("camera", true);
            startActivityForResult(video_intent, 0x11);
        }
        //finish();
    }*/

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        carnumb = data.getStringExtra("number").toString();
        color = data.getStringExtra("color").toString();
        bitmapPath = data.getStringExtra("path").toString();
        if (carnumb.equals("null")) {

        } else {
            //设置每个edit的值
            char[] carnumber = carnumb.toCharArray();
            if (carnumber.length == 7) {
                inputcarInputbox8.setVisibility(View.GONE);
                inputcarInputbox1.setText(String.valueOf(carnumber[0]));
                inputcarInputbox2.setText(String.valueOf(carnumber[1]));
                inputcarInputbox3.setText(String.valueOf(carnumber[2]));
                inputcarInputbox4.setText(String.valueOf(carnumber[3]));
                inputcarInputbox5.setText(String.valueOf(carnumber[4]));
                inputcarInputbox6.setText(String.valueOf(carnumber[5]));
                inputcarInputbox7.setText(String.valueOf(carnumber[6]));
            } else if (carnumber.length == 8) {
                inputcarInputbox1.setText(String.valueOf(carnumber[0]));
                inputcarInputbox2.setText(String.valueOf(carnumber[1]));
                inputcarInputbox3.setText(String.valueOf(carnumber[2]));
                inputcarInputbox4.setText(String.valueOf(carnumber[3]));
                inputcarInputbox5.setText(String.valueOf(carnumber[4]));
                inputcarInputbox6.setText(String.valueOf(carnumber[5]));
                inputcarInputbox7.setText(String.valueOf(carnumber[6]));
                inputcarInputbox8.setText(String.valueOf(carnumber[7]));
            }
            if (bitmapPath != null && !bitmapPath.equals("")) {
               /* bitmap = BitmapFactory.decodeFile(bitmapPath);
                //在使用图片路径识别模式跳入本界面时   请将下面这行代码注释
            *//*bitmap = Bitmap.createBitmap(bitmap, left, top, w, h);
            if (bitmap != null) {
                admissionImg.setImageBitmap(bitmap);
            }*//*
                inputcarImg.setVisibility(View.VISIBLE);
                Bitmap bitmap1 = ImageCrop(bitmap, 125, 300, true);
                inputcarImg.setImageBitmap(bitmap1);*/
                Bitmap newbitmap = PictureUtil.getSmallBitmap(bitmapPath, 480, 800);
                FileUtils.saveBitmap(newbitmap, pictureName());
                putbitmappath = SDPATH + pictureName() + ".JPEG";
                Log.e("TAG", putbitmappath);
                bitmap = BitmapFactory.decodeFile(putbitmappath);
                inputcarImg.setVisibility(View.VISIBLE);
                Bitmap bitmap1 = ImageCrop(bitmap, 125, 300, true);
                inputcarImg.setImageBitmap(bitmap1);
            }
        }

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

    //固定长宽剪切图片
    public static Bitmap ImageCrop(Bitmap bitmap, int num1, int num2, boolean isRecycled) {
        if (bitmap == null) {
            return null;
        }
        int w = bitmap.getWidth(); // 得到图片的宽，高
        int h = bitmap.getHeight();
        int retX, retY;
        int nw, nh;
        if (w > h) {
            if (h > w * num2 / num1) {
                nw = w;
                nh = w * num2 / num1;
                retX = 0;
                retY = (h - nh) / 2;
            } else {
                nw = h * num1 / num2;
                nh = h;
                retX = (w - nw) / 2;
                retY = 0;
            }
        } else {
            if (w > h * num2 / num1) {
                nh = h;
                nw = h * num2 / num1;
                retY = 0;
                retX = (w - nw) / 2;
            } else {
                nh = w * num1 / num2;
                nw = w;
                retY = (h - nh) / 2;
                retX = 0;
            }
        }
        Bitmap bmp = Bitmap.createBitmap(bitmap, retX, retY, nw, nh, null,
                false);
        if (isRecycled && bitmap != null && !bitmap.equals(bmp)
                && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
        }
        return bmp;// Bitmap.createBitmap(bitmap, retX, retY, nw, nh, null,
        // false);
    }

    //点击空白处隐藏键盘
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        keyboardUtil.hideKeyboard();
        return super.onTouchEvent(event);
    }

    //车辆入场
    private void carinto(String url) {
        dialog1 = new ZLoadingDialog(InputCarnum.this);
        dialog1.setLoadingBuilder(Z_TYPE.LEAF_ROTATE)//设置类型STAR_LOADING 五角星
                .setLoadingColor(Color.parseColor("#55BEB7"))//颜色
                .setHintText("Loading...")
                .setHintTextColor(Color.parseColor("#55BEB7"))
                .setHintTextSize(16) // 设置字体大小 dp
                .setHintTextColor(Color.GRAY)  // 设置字体颜色
                .show();
        //{"cmd":"140","type":"2","code":"17083B3DE","dsv":"110","ptype":"0","io":"0",
        // "num":"京B1FL39","ctype":"2","spare":"0","sign":"abcd"}
       /* String intocar_jS = "{\"cmd\":\"140\",\"type\":\"" + Constant.TYPE + "\",\"code\":\"" + Constant.CODE + "\"," +
                "\"dsv\":\"" + Constant.DSV + "\",\"ptype\":\"0\",\"io\":\"0\",\"num\":\"" + editS + "\"," +
                "\"ctype\":\"" + carType + "\",\"spare\":\"0\",\"sign\":\"abcd\"}";*/
        String intocar_jS ="{\"cmd\":\"140\",\"type\":\""+ Constant.TYPE+"\",\"code\":\""+ Constant.CODE+"\",\"dsv\":\""
                + Constant.DSV+"\",\"ptype\":\"0\",\"io\":\"0\",\"num\":\""+editS+"\",\"ctype\":\""+carType+
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
                    carnum = datajson.getString("num");//车牌号
                    rstat = datajson.getInt("rstat");//放行状态
                    //rstat=1;
                    ptype = datajson.getInt("ptype");//凭证类型（卡或车牌）
                    ctype = datajson.getInt("ctype");//车辆类型
                    cdtp = datajson.getInt("cdtp");//计费类型（月票车）
                    long ctime = datajson.getLong("ctime");//计费时间
                    preson = datajson.getInt("preson");//原因
                    itime = datajson.getLong("itime");//入场时间
                    double nmon = datajson.getDouble("nmon");//应收金额
                    double rmon = datajson.getDouble("rmon");//实收金额
                    double smon = datajson.getDouble("smon");//折扣金额
                    String sid = datajson.getString("sid");
                    if (sid != null && putbitmappath != null) {
                        Log.e("TAG", putbitmappath);
                        String ppmbucket = Constant.ppmBucket;
                        //String ppmimgurl=Constant.CODE+"/"+sid+".jpg";
                        String ppmimgurl = MD5Util.MD5Encode(Constant.CODE +"0"+ sid) + ".jpg";
                        String ppmfileurl = putbitmappath;
                        Log.e("TAG", sid);
                        putdate(ppmbucket, ppmimgurl, ppmfileurl);
                    }
                    //判断
                    jfjudge();
                    if (rstat == 0) {
                        //自动放行
                            carin_start_voice();
                            Intent zdintent = new Intent(InputCarnum.this, CarintoSuccessful.class);
                            zdintent.putExtra("carnum", carnum);
                            zdintent.putExtra("jfType", jfType);
                            zdintent.putExtra("ctype", ctype);
                            zdintent.putExtra("itime", itime);
                            zdintent.putExtra("cdtp", cdtp);
                            startActivity(zdintent);
                        //Toast.makeText(InputCarnum.this, "自动放行", Toast.LENGTH_SHORT).show();
                    } else if (rstat == 1) {
                        //确认放行
                        confirmjudge();
                        Intent qrintent = new Intent(InputCarnum.this, CarinConfirmPass.class);
                        qrintent.putExtra("carnum", carnum);
                        qrintent.putExtra("jfType", jfType);
                        qrintent.putExtra("ctype", ctype);
                        qrintent.putExtra("itime", itime);
                        qrintent.putExtra("comfirmYy", comfirmYy);
                        qrintent.putExtra("sid", sid);
                        qrintent.putExtra("cdtp", cdtp);
                        startActivity(qrintent);

                    } else if (rstat == 2) {
                        //收费放行
                        chargejudge();
                        Intent sfintent = new Intent(InputCarnum.this, CarinChargeActivity.class);
                        sfintent.putExtra("carnum", carnum);
                        sfintent.putExtra("jfType", jfType);
                        sfintent.putExtra("ctype", ctype);
                        sfintent.putExtra("itime", itime);
                        sfintent.putExtra("comfirmYy", comfirmYy);
                        sfintent.putExtra("nmon", nmon);
                        sfintent.putExtra("rmon", rmon);
                        sfintent.putExtra("smon", smon);
                        sfintent.putExtra("sid", sid);
                        sfintent.putExtra("cdtp", cdtp);
                        startActivity(sfintent);
                    } else if (rstat == 3) {
                        //禁止通行
                        prohibitjudge();
                        Intent jzintent = new Intent(InputCarnum.this, ProhibitPass.class);
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

    //车辆出场
    private void carout(String url) {
        dialog = new ZLoadingDialog(InputCarnum.this);
        dialog.setLoadingBuilder(Z_TYPE.LEAF_ROTATE)//设置类型STAR_LOADING 五角星
                .setLoadingColor(Color.parseColor("#55BEB7"))//颜色
                .setHintText("Loading...")
                .setHintTextColor(Color.parseColor("#55BEB7"))
                .setHintTextSize(16) // 设置字体大小 dp
                .setHintTextColor(Color.GRAY)  // 设置字体颜色
                .show();
        //{"cmd":"140","type":"2","code":"17083B3DE","dsv":"110","ptype":"0","io":"0",
        // "num":"京B1FL39","ctype":"2","spare":"0","sign":"abcd"}
        /*String intocar_jS = "{\"cmd\":\"140\",\"type\":\"" + Constant.TYPE + "\",\"code\":\"" + Constant.CODE + "\"," +
                "\"dsv\":\"" + Constant.DSV + "\",\"ptype\":\"0\",\"io\":\"1\",\"num\":\"" + editS + "\"," +
                "\"ctype\":\"" + carType + "\",\"spare\":\"0\",\"sign\":\"abcd\"}";*/

        String intocar_jS ="{\"cmd\":\"140\",\"type\":\""+ Constant.TYPE+"\",\"code\":\""+ Constant.CODE+"\",\"dsv\":\""
                + Constant.DSV+"\",\"ptype\":\"0\",\"io\":\"1\",\"num\":\""+editS+"\",\"ctype\":\""+carType+
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
                dialog.dismiss();
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String reasonjson = jsonObject.getString("reason");//操作成功
                    JSONObject resultjson = jsonObject.getJSONObject("result");
                    JSONObject datajson = resultjson.getJSONObject("data");
                    carnum = datajson.getString("num");//车牌号
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
                    if (sid != null && bitmapPath != null) {
                        String ppmbucket = Constant.ppmBucket;
                        //String ppmimgurl=Constant.CODE+"/"+sid+".jpg";
                        String ppmimgurl = MD5Util.MD5Encode(Constant.CODE +"1"+ sid) + ".jpg";
                        String ppmfileurl = bitmapPath;
                        Log.e("TAG", sid);
                        putdate(ppmbucket, ppmimgurl, ppmfileurl);
                    }
                    //判断
                    jfjudge();
                    if (rstat == 0) {
                        //自动放行
                        carout_start_voice();
                        Intent zdintent = new Intent(InputCarnum.this, CaroutSuccessful.class);
                        zdintent.putExtra("carnum", carnum);
                        zdintent.putExtra("jfType", jfType);
                        zdintent.putExtra("ctype", ctype);
                        zdintent.putExtra("ctime", ctime);
                        zdintent.putExtra("itime", itime);
                        startActivity(zdintent);
                        //Toast.makeText(InputCarnum.this, "自动放行", Toast.LENGTH_SHORT).show();
                    } else if (rstat == 1) {
                        //确认放行
                        confirmjudge();
                        Intent qrintent = new Intent(InputCarnum.this, CaroutConfirmPass.class);
                        qrintent.putExtra("carnum", carnum);
                        qrintent.putExtra("jfType", jfType);
                        qrintent.putExtra("ctype", ctype);
                        qrintent.putExtra("itime", itime);
                        qrintent.putExtra("ctime", ctime);
                        qrintent.putExtra("comfirmYy", comfirmYy);
                        qrintent.putExtra("sid", sid);
                        qrintent.putExtra("cdtp", cdtp);
                        qrintent.putExtra("pvrefresh", false);
                        startActivity(qrintent);
                    } else if (rstat == 2) {
                        //收费放行
                        chargejudge();
                        Intent sfintent = new Intent(InputCarnum.this, CaroutChargeActivity.class);
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
                        sfintent.putExtra("pvrefresh", false);
                        startActivity(sfintent);
                    } else if (rstat == 3) {
                        //禁止通行
                        prohibitjudge();
                        Intent jzintent = new Intent(InputCarnum.this, ProhibitPass.class);
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

    //判断分类
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

    //获取当前时间
    private long gettime() {
        Date date = new Date();
        long time = (date.getTime() / 1000);
        //Log.e("TAG",date.getTime()/1000+"");
        return time;
    }

    //初始化颜色
    private void initcolor() {
        inputcarMoto.setBackgroundColor(Color.parseColor("#FFFFFF"));
        inputcarMotoimg.setImageResource(R.mipmap.ic_moto_bike);
        inputcarMototext.setTextColor(Color.parseColor("#48495f"));
        inputcarSmallcar.setBackgroundColor(Color.parseColor("#FFFFFF"));
        inputcarSmallcarimg.setImageResource(R.mipmap.ic_small_car);
        inputcarSmallcartext.setTextColor(Color.parseColor("#48495f"));
        inputcarMiddle.setBackgroundColor(Color.parseColor("#FFFFFF"));
        inputcarMiddleimg.setImageResource(R.mipmap.ic_mid_truck);
        inputcarMiddletext.setTextColor(Color.parseColor("#48495f"));
        inputcarBigcar.setBackgroundColor(Color.parseColor("#FFFFFF"));
        inputcarBigcarimg.setImageResource(R.mipmap.ic_big_truck);
        inputcarBigcartextt.setTextColor(Color.parseColor("#48495f"));
    }

    //开启新能源车牌识别
    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (b) {
            SPUtils.put(InputCarnum.this, "open_new_car", true);
            inputcarInputbox8.setVisibility(View.VISIBLE);
            LicenseKeyboardUtil_input.etsize = 7;
            if (!TextUtils.isEmpty(inputcarInputbox7.getText())) {
                currentEditText_input = 7;
                initcolor1();
                edits[LicenseKeyboardUtil_input.currentEditText_input].setBackgroundResource(R.drawable.keyboard_bg_red);
            }

        } else {
            SPUtils.put(InputCarnum.this, "open_new_car", false);
            inputcarInputbox8.setVisibility(View.GONE);
            LicenseKeyboardUtil_input.etsize = 6;
            inputcarInputbox8.setText("");
            if (!TextUtils.isEmpty(inputcarInputbox6.getText())) {
                currentEditText_input = 6;
                initcolor1();
                edits[LicenseKeyboardUtil_input.currentEditText_input].setBackgroundResource(R.drawable.keyboard_bg_red);
            }

        }
    }

}
