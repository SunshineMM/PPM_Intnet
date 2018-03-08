package com.example.npttest.fragment;


import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.SimpleClickListener;
import com.example.npttest.App;
import com.example.npttest.R;
import com.example.npttest.activity.CarinDetailedInfo;
import com.example.npttest.adapter.CarintoAdapter;
import com.example.npttest.constant.Constant;
import com.example.npttest.entity.Carinto;
import com.example.npttest.manager.LinearLayoutManagerWrapper;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.MediaType;

/**
 * Created by liuji on 2017/7/28.
 */

public class Fg2_Fragment1 extends Fragment implements SwipeRefreshLayout.OnRefreshListener, BaseQuickAdapter.RequestLoadMoreListener {


    @Bind(R.id.rv_list_fg1)
    RecyclerView rvListFg1;
    @Bind(R.id.swipeLayout)
    SwipeRefreshLayout swipeLayout;
    @Bind(R.id.ffg1_nulltv)
    TextView ffg1Nulltv;
    private List<Carinto> list = new ArrayList<>();
    private CarintoAdapter carintoAdapter;
    private static final int TOTAL_COUNTER = 0;
    private static final int PAGE_SIZE = 6;
    private int delayMillis = 1000;
    private int mCurrentCounter = 0;
    private boolean isErr;
    private boolean mLoadMoreEndGone = false;
    /*private ZLoadingDialog dialog;*/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fg2_fragment1, null);
        ButterKnife.bind(this, v);
        list.clear();
        if (App.serverurl != null) {
            getrecord(App.serverurl);
            initAdapter();
        }
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorSchemeResources(R.color.colorPrimaryDark);
        //initAdapter();
        //rvListFg1.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvListFg1.setLayoutManager(new LinearLayoutManagerWrapper(getActivity(), LinearLayoutManager.VERTICAL, false));
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.e("TAG", "httpupdate入场");
        if (!App.goRefresh) {
            onRefresh();
            App.goRefresh = true;
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        /*list.clear();
        getrecord(App.serverurl);*/
        Log.e("TAG", "fg1onResume");
    }

    //获取入场车辆记录
    public void getrecord(String url) {
        /*dialog = new ZLoadingDialog(getActivity());
        dialog.setLoadingBuilder(Z_TYPE.LEAF_ROTATE)//设置类型STAR_LOADING 五角星
                .setLoadingColor(Color.parseColor("#55BEB7"))//颜色
                .setHintText("Loading...")
                .setHintTextColor(Color.parseColor("#55BEB7"))
                .setHintTextSize(16) // 设置字体大小 dp
                .setHintTextColor(Color.GRAY)  // 设置字体颜色
                .show();*/

        String getrcord = "{\"cmd\":\"155\",\"type\":\"" + Constant.TYPE + "\",\"code\":\"" + Constant.CODE +
                "\",\"dsv\":\"" + Constant.DSV + "\",\"qtype\":\"0\",\"num\":\"\",\"stime\":\"\"," +
                "\"etime\":\"\",\"spare\":\" \",\"sign\":\"abcd\"}";
        Log.e("TAG", getrcord);
        OkHttpUtils.postString().url(url)
                .content(getrcord)
                .mediaType(MediaType.parse("application/json; charset=utf-8"))
                .build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                Toast.makeText(getActivity(), "无网络", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(String response, int id) {
                //dialog.dismiss();
                try {
                    JSONObject rjsonObject = new JSONObject(response);
                    JSONObject resultjsOb = rjsonObject.getJSONObject("result");
                    JSONArray listjsAr = resultjsOb.getJSONArray("list");
                    for (int i = 0; i < listjsAr.length(); i++) {
                        JSONObject jsonObject = listjsAr.getJSONObject(i);
                        Carinto carinto = new Carinto();
                        carinto.setCnum(jsonObject.getString("cnum"));
                        carinto.setItime(jsonObject.getInt("itime"));
                        carinto.setPnum(jsonObject.getString("pnum"));
                        carinto.setIurl(jsonObject.getString("iurl"));
                        carinto.setSid(jsonObject.getString("sid"));
                        carinto.setCtype(jsonObject.getString("ctype"));
                        carinto.setCdtp(jsonObject.getString("cdtp"));
                        list.add(carinto);
                    }
                    Log.e("TAG", "入场车辆：" + list.size());
                    handler.sendEmptyMessage(0x123);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0x123) {
                if (carintoAdapter != null && rvListFg1 != null) {
                    carintoAdapter.notifyDataSetChanged();
                    rvListFg1.setAdapter(carintoAdapter);
                }
            }
        }
    };

    /**
     * 刷新
     */
    @Override
    public void onRefresh() {
        carintoAdapter.setEnableLoadMore(false);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                list.clear();
                if (App.serverurl != null) {
                    getrecord(App.serverurl);
                }
                //rvListFg1.setLayoutManager(new LinearLayoutManager(getActivity()));
                //carintoAdapter.setNewData(list);
                carintoAdapter.notifyDataSetChanged();
                isErr = false;
                mCurrentCounter = PAGE_SIZE;
                if (swipeLayout != null) {
                    swipeLayout.setRefreshing(false);
                }
                carintoAdapter.setEnableLoadMore(true);
            }
        }, delayMillis);
    }


    //设置适配器
    private void initAdapter() {
        carintoAdapter = new CarintoAdapter(list);
        carintoAdapter.setOnLoadMoreListener(this, rvListFg1);
        carintoAdapter.openLoadAnimation(BaseQuickAdapter.SLIDEIN_LEFT);
        carintoAdapter.isFirstOnly(false);
        rvListFg1.setAdapter(carintoAdapter);
        mCurrentCounter = carintoAdapter.getData().size();

       /* rvListFg1.addOnItemTouchListener(new OnItemClickListener() {
            @Override
            public void onSimpleItemClick(final BaseQuickAdapter adapter, final View view, final int position) {
                Toast.makeText(getActivity(), Integer.toString(position), Toast.LENGTH_LONG).show();
            }
        } );*/

        /**
         * 注册监听事件
         */
        rvListFg1.addOnItemTouchListener(new SimpleClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                //Toast.makeText(getActivity(), Integer.toString(position), Toast.LENGTH_SHORT).show();
                Intent carintoInfo_intent = new Intent(getActivity(), CarinDetailedInfo.class);
                carintoInfo_intent.putExtra("carnum", carintoAdapter.getData().get(position).getPnum());
                carintoInfo_intent.putExtra("cartype", carintoAdapter.getData().get(position).getCtype());
                carintoInfo_intent.putExtra("pztype", carintoAdapter.getData().get(position).getCdtp());
                carintoInfo_intent.putExtra("itime", carintoAdapter.getData().get(position).getItime());
                startActivity(carintoInfo_intent);
            }

            @Override
            public void onItemLongClick(BaseQuickAdapter adapter, View view, int position) {

            }

            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                switch (view.getId()) {
                    case R.id.item_img:
                        final AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.show();
                        ImageView imageView = new ImageView(getActivity());
                        Glide.with(getActivity()).load(carintoAdapter.getData().get(position).getIurl())
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


    /**
     * 加载
     */
    @Override
    public void onLoadMoreRequested() {
        swipeLayout.setEnabled(false);
        if (carintoAdapter.getData().size() < PAGE_SIZE) {
            carintoAdapter.loadMoreEnd(true);//加载完成不再加载
        } else {
            if (mCurrentCounter >= TOTAL_COUNTER) {
                carintoAdapter.loadMoreEnd();//default visible
                //数据全部加载完毕
                carintoAdapter.loadMoreEnd(mLoadMoreEndGone);//true is gone,false is visible
            } else {
                if (isErr) {
                    carintoAdapter.addData(list);
                    mCurrentCounter = carintoAdapter.getData().size();
                    carintoAdapter.loadMoreComplete();
                } else {
                    isErr = true;
                    carintoAdapter.loadMoreFail();
                }
            }
            swipeLayout.setEnabled(true);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
