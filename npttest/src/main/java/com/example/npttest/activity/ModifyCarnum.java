package com.example.npttest.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.inputmethodservice.KeyboardView;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bigkoo.pickerview.OptionsPickerView;
import com.bigkoo.pickerview.listener.CustomListener;
import com.example.npttest.App;
import com.example.npttest.R;
import com.example.npttest.constant.Constant;
import com.example.npttest.util.LicenseKeyboardUtil_cario;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.dmoral.toasty.Toasty;
import okhttp3.Call;
import okhttp3.MediaType;

/**
 * Created by liuji on 2017/9/2.
 */

public class ModifyCarnum extends Activity {

    @Bind(R.id.modify_return)
    ImageView modifyReturn;
    @Bind(R.id.modify_inputbox1)
    EditText modifyInputbox1;
    @Bind(R.id.modify_inputbox2)
    EditText modifyInputbox2;
    @Bind(R.id.modify_inputbox3)
    EditText modifyInputbox3;
    @Bind(R.id.modify_inputbox4)
    EditText modifyInputbox4;
    @Bind(R.id.modify_inputbox5)
    EditText modifyInputbox5;
    @Bind(R.id.modify_inputbox6)
    EditText modifyInputbox6;
    @Bind(R.id.modify_inputbox7)
    EditText modifyInputbox7;
    @Bind(R.id.modify_inputbox8)
    EditText modifyInputbox8;
    @Bind(R.id.modify_lin_input)
    LinearLayout modifyLinInput;
    @Bind(R.id.modify_ownername)
    EditText modifyOwnername;
    @Bind(R.id.modify_cartype)
    EditText modifyCartype;
    @Bind(R.id.modify_cardtype)
    EditText modifyCardtype;
    @Bind(R.id.modify_cardnum)
    EditText modifyCardnum;
    @Bind(R.id.keyboard_view)
    KeyboardView keyboardView;
    private LicenseKeyboardUtil_cario keyboardUtil;
    private EditText edits[];
    private String carnum,cartype,pztype,sid,carnumedit;
    private int intcartype,intpztype;
    public static int midefyposition;
    private boolean pvRefresh;
    private OptionsPickerView pickerView, cardpickview;
    private List<String> cartypes = new ArrayList<>();
    private List<String> cardtypes = new ArrayList<>();
    public static String mdcarnum;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.modifycarnum);
        ButterKnife.bind(this);
        Intent intent = getIntent();
        carnum = intent.getStringExtra("number");
        intcartype=intent.getIntExtra("cartype",0);
        intpztype=intent.getIntExtra("pztype",0);
        pvRefresh=intent.getBooleanExtra("pvrefresh",false);
        sid=intent.getStringExtra("sid");
        midefyposition=intent.getIntExtra("position",0);
        //设置每个edit的值
        char[] carnumber = carnum.toCharArray();
        if (carnumber.length == 7) {
            LicenseKeyboardUtil_cario.etsize_cario = 6;
            modifyInputbox8.setVisibility(View.GONE);
            modifyInputbox1.setText(String.valueOf(carnumber[0]));
            modifyInputbox2.setText(String.valueOf(carnumber[1]));
            modifyInputbox3.setText(String.valueOf(carnumber[2]));
            modifyInputbox4.setText(String.valueOf(carnumber[3]));
            modifyInputbox5.setText(String.valueOf(carnumber[4]));
            modifyInputbox6.setText(String.valueOf(carnumber[5]));
            modifyInputbox7.setText(String.valueOf(carnumber[6]));
        } else if (carnumber.length == 8) {
            LicenseKeyboardUtil_cario.etsize_cario = 7;
            modifyInputbox1.setText(String.valueOf(carnumber[0]));
            modifyInputbox2.setText(String.valueOf(carnumber[1]));
            modifyInputbox3.setText(String.valueOf(carnumber[2]));
            modifyInputbox4.setText(String.valueOf(carnumber[3]));
            modifyInputbox5.setText(String.valueOf(carnumber[4]));
            modifyInputbox6.setText(String.valueOf(carnumber[5]));
            modifyInputbox7.setText(String.valueOf(carnumber[6]));
            modifyInputbox8.setText(String.valueOf(carnumber[7]));
        }
        edits = new EditText[]{modifyInputbox1, modifyInputbox2, modifyInputbox3,
                modifyInputbox4, modifyInputbox5, modifyInputbox6,
                modifyInputbox7, modifyInputbox8};
        keyboardUtil = new LicenseKeyboardUtil_cario(ModifyCarnum.this, edits);
        cartypes.add("摩托车");
        cartypes.add("小型车");
        cartypes.add("中型车");
        cartypes.add("大型车");

        cardtypes.add("贵宾");
        cardtypes.add("临时");
        cardtypes.add("月票");
        cardtypes.add("储值");
        cardtypes.add("免费");
        cardtypes.add("时租");
        initCardpickview();
        initpickview();
        jfjudge();
        modifyCartype.setText(cartype);
        modifyCardtype.setText(pztype);

    }

    private void jfjudge() {
        switch (intcartype) {
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

        switch (intpztype) {
            case 1:
                pztype = "贵宾车";
                break;
            case 2:
                pztype = "月票车";
                break;
            case 3:
                pztype = "储值车";
                break;
            case 4:
                pztype = "临时车";
                break;
            case 5:
                pztype = "免费车";
                break;
            case 6:
                pztype = "车位池车";
                break;
            case 7:
                pztype = "时租车";
                break;
        }
    }

    //点击空白处隐藏键盘
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        keyboardUtil.hideKeyboard();
        return super.onTouchEvent(event);
    }

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

    @OnClick({R.id.modify_return, R.id.modify_inputbox1, R.id.modify_inputbox2, R.id.modify_inputbox3, R.id.modify_inputbox4, R.id.modify_inputbox5, R.id.modify_inputbox6, R.id.modify_inputbox7, R.id.modify_inputbox8, R.id.modify_cartype, R.id.modify_cardtype})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.modify_return:
                carnumedit = modifyInputbox1.getText().toString() +
                        modifyInputbox2.getText().toString() +
                        modifyInputbox3.getText().toString() +
                        modifyInputbox4.getText().toString() +
                        modifyInputbox5.getText().toString() +
                        modifyInputbox6.getText().toString() +
                        modifyInputbox7.getText().toString() +
                        modifyInputbox8.getText().toString();

                if (TextUtils.isEmpty(modifyInputbox1.getText().toString()) ||
                        TextUtils.isEmpty(modifyInputbox2.getText().toString()) ||
                        TextUtils.isEmpty(modifyInputbox3.getText().toString()) ||
                        TextUtils.isEmpty(modifyInputbox4.getText().toString()) ||
                        TextUtils.isEmpty(modifyInputbox5.getText().toString()) ||
                        TextUtils.isEmpty(modifyInputbox6.getText().toString()) ||
                        TextUtils.isEmpty(modifyInputbox7.getText().toString())) {
                    Toasty.error(this, "请输入正确的车牌号", Toast.LENGTH_SHORT, true).show();
                } else {
                    updatecarnum(App.serverurl);
                }
                break;
            case R.id.modify_inputbox1:
                LicenseKeyboardUtil_cario.currentEditText_cario = 0;
                initcolor1();
                edits[LicenseKeyboardUtil_cario.currentEditText_cario].setBackgroundResource(R.drawable.keyboard_bg_red);
                keyboardUtil.showKeyboard();
                break;
            case R.id.modify_inputbox2:
                LicenseKeyboardUtil_cario.currentEditText_cario = 1;
                initcolor1();
                edits[LicenseKeyboardUtil_cario.currentEditText_cario].setBackgroundResource(R.drawable.keyboard_bg_red);
                keyboardUtil.showKeyboard();
                break;
            case R.id.modify_inputbox3:
                LicenseKeyboardUtil_cario.currentEditText_cario = 2;
                initcolor1();
                edits[LicenseKeyboardUtil_cario.currentEditText_cario].setBackgroundResource(R.drawable.keyboard_bg_red);
                keyboardUtil.showKeyboard();
                break;
            case R.id.modify_inputbox4:
                LicenseKeyboardUtil_cario.currentEditText_cario = 3;
                initcolor1();
                edits[LicenseKeyboardUtil_cario.currentEditText_cario].setBackgroundResource(R.drawable.keyboard_bg_red);
                keyboardUtil.showKeyboard();
                break;
            case R.id.modify_inputbox5:
                LicenseKeyboardUtil_cario.currentEditText_cario = 4;
                initcolor1();
                edits[LicenseKeyboardUtil_cario.currentEditText_cario].setBackgroundResource(R.drawable.keyboard_bg_red);
                keyboardUtil.showKeyboard();
                break;
            case R.id.modify_inputbox6:
                LicenseKeyboardUtil_cario.currentEditText_cario = 5;
                initcolor1();
                edits[LicenseKeyboardUtil_cario.currentEditText_cario].setBackgroundResource(R.drawable.keyboard_bg_red);
                keyboardUtil.showKeyboard();
                break;
            case R.id.modify_inputbox7:
                LicenseKeyboardUtil_cario.currentEditText_cario = 6;
                initcolor1();
                edits[LicenseKeyboardUtil_cario.currentEditText_cario].setBackgroundResource(R.drawable.keyboard_bg_red);
                keyboardUtil.showKeyboard();
                break;
            case R.id.modify_inputbox8:
                LicenseKeyboardUtil_cario.currentEditText_cario = 7;
                initcolor1();
                edits[LicenseKeyboardUtil_cario.currentEditText_cario].setBackgroundResource(R.drawable.keyboard_bg_red);
                keyboardUtil.showKeyboard();
                break;
            case R.id.modify_cartype:
                //modifyCartype.setTextColor(Color.parseColor("#55BEB7"));
               // pickerView.show();
                break;
            case R.id.modify_cardtype:
                //modifyCardtype.setTextColor(Color.parseColor("#55BEB7"));
                //cardpickview.show();
                break;
        }
    }

    //修改车牌号码
    private void updatecarnum(String url){
        String s="{\"cmd\":\"173\",\"type\":\""+ Constant.TYPE+"\",\"code\":\""+ Constant.CODE+"\"," +
                "\"dsv\":\""+ Constant.DSV+"\",\"sid\":\""+sid+"\",\"plate\":\""+carnumedit+"\",\"sign\":\"abcd\"}";
        Log.e("TAG",s);
        OkHttpUtils.postString()
                .url(url).content(s)
                .mediaType(MediaType.parse("application/json; charset=utf-8"))
                .build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                Toast.makeText(ModifyCarnum.this, "修改失败，请检查网络", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onResponse(String response, int id) {
                Log.e("TAG","修改车牌返回数据"+response);
                try {
                    JSONObject jsonObject=new JSONObject(response);
                    JSONObject resultjsonobject=jsonObject.getJSONObject("result");
                    JSONObject datajsonobject=resultjsonobject.getJSONObject("data");
                    int mrs=datajsonobject.getInt("mrs");
                    if (mrs==1){
                        mdcarnum=carnumedit;
                        Toasty.success(ModifyCarnum.this,"修改成功",Toast.LENGTH_SHORT,true).show();
                        App.pvRefresh=false;
                        App.mdRefresh=true;
                        App.zcRefresh=true;
                        finish();
                    }else {
                        Toasty.error(ModifyCarnum.this,"修改失败",Toast.LENGTH_SHORT,true).show();
                        App.pvRefresh=false;
                        finish();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    private void initpickview() {
        cardpickview = new OptionsPickerView.Builder(this, new OptionsPickerView.OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int options2, int options3, View v) {
                String sex = cardtypes.get(options1);
                modifyCardtype.setText(sex);
            }
        }).setLayoutRes(R.layout.pickerview, new CustomListener() {
            @Override
            public void customLayout(View v) {
                TextView submitTv = v.findViewById(R.id.tv_finish);
                ImageView imageView = v.findViewById(R.id.iv_cancel);
                submitTv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        cardpickview.returnData();
                        cardpickview.dismiss();
                        modifyCardtype.setTextColor(Color.parseColor("#48495f"));
                    }
                });

                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        cardpickview.dismiss();
                        modifyCardtype.setTextColor(Color.parseColor("#48495f"));
                    }
                });
            }

        }).setContentTextSize(20).setSelectOptions(1).setOutSideCancelable(false).build();//setOutSideCancelable点击屏幕，点在控件外部范围时，是否取消显示

        cardpickview.setPicker(cardtypes);
    }

    private void initCardpickview() {
        pickerView = new OptionsPickerView.Builder(this, new OptionsPickerView.OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int options2, int options3, View v) {
                String sex = cartypes.get(options1);
                modifyCartype.setText(sex);
            }
        }).setLayoutRes(R.layout.pickerview, new CustomListener() {
            @Override
            public void customLayout(View v) {
                TextView submitTv = v.findViewById(R.id.tv_finish);
                ImageView imageView = v.findViewById(R.id.iv_cancel);
                submitTv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        pickerView.returnData();
                        pickerView.dismiss();
                        modifyCartype.setTextColor(Color.parseColor("#48495f"));
                    }
                });

                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        pickerView.dismiss();
                        modifyCartype.setTextColor(Color.parseColor("#48495f"));
                    }
                });
            }

        }).setContentTextSize(20).setSelectOptions(1).setOutSideCancelable(false).build();//setOutSideCancelable点击屏幕，点在控件外部范围时，是否取消显示

        pickerView.setPicker(cartypes);
    }
}
