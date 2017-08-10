package com.yu.coolweather;


import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.yu.coolweather.db.City;
import com.yu.coolweather.db.County;
import com.yu.coolweather.db.Province;
import com.yu.coolweather.utils.HttpUtil;
import com.yu.coolweather.utils.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChooseAreaFragment extends Fragment implements MainActivity.onKeyDownListener {
    Button btnBack;
    TextView tvTitle;
    ListView listView;
    SwipeRefreshLayout refresh;

    ArrayAdapter<String> adapter;

    private List<String> dataList = new ArrayList<>();
    /*  省市县集合 */
    private List<Province> provinceList = new ArrayList<Province>();
    private List<City> cityList = new ArrayList<>();

    private List<County> countyList = new ArrayList<>();

    private static final int LEVEL_PROVINCE = 0;  // 城市级别
    private static final int LEVEL_CITY = 1;  // 城市级别
    private static final int LEVEL_COUNTY = 2; // 县级别

    private int currentLevel;  // 当前级别
    /* 选中的省市 */
    private Province selectedProvince;

    private City selectedCity;

    /* 当前第一个可见的省市，用于返回时的定位 */
    private int mProvinceCurFirstPos = 0;
    private int mCityCurFirstPos = 0;


    public ChooseAreaFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_choose_area, container, false);

        refresh = (SwipeRefreshLayout) view.findViewById(R.id.id_refresh_choose_area);
        btnBack = (Button) view.findViewById(R.id.id_btn_back_choose_area);
        tvTitle = (TextView) view.findViewById(R.id.id_tv_title_choose_area);
        listView = (ListView) view.findViewById(R.id.id_lv_choose_area);
        adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);

        return view;
    }


    /**
     * 强制5秒结束
     */
    private void stopRefreshing() {
        new Thread(){
            @Override
            public void run() {
                super.run();
                SystemClock.sleep(5000);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dismissRefreshing();
                    }
                });
            }
        }.start();

    }

    /**
     * 结束刷新
     */
    private void dismissRefreshing() {
        if (refresh != null && refresh.isRefreshing()) {
            refresh.setRefreshing(false);
        }
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (currentLevel) {
                    case LEVEL_PROVINCE:
                        mProvinceCurFirstPos = parent.getFirstVisiblePosition();
                        selectedProvince = provinceList.get(position);
                        queryCities();
                        break;
                    case LEVEL_CITY:
                        mCityCurFirstPos = parent.getFirstVisiblePosition();
                        selectedCity = cityList.get(position);
                        queryCounties();
                        break;
                    case LEVEL_COUNTY:
                        // 启动WeatherActivity
                        String weatherId = countyList.get(position).getWeatherId();
                        String countyName = countyList.get(position).getCountyName();
                        if (getActivity() instanceof MainActivity) {
                            Intent intent = new Intent(getActivity(), WeatherActivity.class);
                            intent.putExtra("city", countyName);
                            intent.putExtra("weatherId", weatherId);
                            getActivity().startActivity(intent);
                            getActivity().finish();
                        } else if (getActivity() instanceof WeatherActivity) {
                            if (listener != null) {
                                listener.onRefresh(weatherId, countyName);
                            } else {
                                throw new RuntimeException("you need implement and set the OnRefreshLayoutListener");
                            }
                        }

                        break;
                }
            }

        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLevel == LEVEL_CITY) {
                    queryProvinces();
                } else if (currentLevel == LEVEL_COUNTY) {
                    queryCities();
                }
            }
        });

        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                stopRefreshing();
                switch (currentLevel) {
                    case LEVEL_PROVINCE:
                        queryProvinces();
                        break;
                    case LEVEL_CITY:
                        queryCities();
                        break;
                    case LEVEL_COUNTY:
                        queryCounties();
                        break;
                }

            }
        });

        queryProvinces(); // 初始化
    }

    /**
     * 查找省信息,优先查本地数据库，没有则从网络拉取
     */
    private void queryProvinces() {
        tvTitle.setText("中国");
        btnBack.setVisibility(View.GONE);
        provinceList = DataSupport.findAll(Province.class);
        if (provinceList.size() > 0) {
            dataList.clear();
            for (Province province : provinceList) {
                dataList.add(province.getProvinceName());
            }
            dismissRefreshing();
            adapter.notifyDataSetChanged();
            listView.setSelection(selectedProvince == null ? 0 : mProvinceCurFirstPos);
            currentLevel = LEVEL_PROVINCE;
        } else {
            String url = "http://guolin.tech/api/china";
            queryFromSever(url, "province");
        }
    }

    /**
     * 查找省信息,优先查本地数据库，没有则从网络拉取
     */
    private void queryCities() {
        Cursor cursor = DataSupport.findBySQL("select * from city where provinceid = ?", String.valueOf(selectedProvince.getId()));
        if (cursor != null) {
            cityList.clear();
            while (cursor.moveToNext()) {
                String cityName = cursor.getString(cursor.getColumnIndex("cityname"));
                int cityCode = cursor.getInt(cursor.getColumnIndex("citycode"));
                int provinceId = cursor.getInt(cursor.getColumnIndex("provinceid"));
                int id = cursor.getInt(cursor.getColumnIndex("id"));
                City city = new City(cityName, cityCode, id, provinceId);
                cityList.add(city);
            }
            cursor.close();
        }
//        cityList = DataSupport.where("provinceid = ?", String.valueOf(selectedProvince.getId())).find(City.class);
        if (this.cityList.size() > 0) {
            btnBack.setVisibility(View.VISIBLE);
            tvTitle.setText(selectedProvince.getProvinceName());
            dataList.clear();
            for (City city : this.cityList) {
                dataList.add(city.getCityName());
            }
            dismissRefreshing();
            adapter.notifyDataSetChanged();
            listView.setSelection(selectedCity == null ? 0 : mCityCurFirstPos);
            currentLevel = LEVEL_CITY;

        } else {
            int provinceCode = selectedProvince.getProvinceCode();
            String url = "http://guolin.tech/api/china" + "/" + provinceCode;
            queryFromSever(url, "city");
        }
    }

    private void queryCounties() {
        countyList.clear();
        Log.e("TAG", "cityId=" + selectedCity.getId());
        countyList = DataSupport.where("cityid = ?", String.valueOf(selectedCity.getId())).find(County.class);
//        Cursor cursor = DataSupport.findBySQL("select * from county where cityid = ?" ,String.valueOf(selectedCity.getId()));
//        if (cursor!=null) {
//            countyList.clear();
//            while (cursor.moveToNext()) {
//                String countyName = cursor.getString(cursor.getColumnIndex("countyname"));
//                String weatherId = cursor.getString(cursor.getColumnIndex("weatherid"));
//                int cityId = cursor.getInt(cursor.getColumnIndex("cityid"));
//                County county = new County(countyName, weatherId, cityId);
//                countyList.add(county);
//            }
//            cursor.close();
//        }
        if (countyList.size() > 0) {
            btnBack.setVisibility(View.VISIBLE);
            tvTitle.setText(selectedCity.getCityName());
            dataList.clear();
            for (County county : countyList) {
                dataList.add(county.getCountyName());
            }
            dismissRefreshing();
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        } else {
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String url = "http://guolin.tech/api/china" + "/" + provinceCode + "/" + cityCode;
            queryFromSever(url, "county");
        }
    }

    private void queryFromSever(String url, final String type) {
        showProgressDialog();
        if (!TextUtils.isEmpty(url) && !TextUtils.isEmpty(type)) {
            HttpUtil.sendOkHttpRequest(url, new Callback() {
                @Override // 线程池中调用
                public void onFailure(Call call, IOException e) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dismissProgressDialog();
                            Toast.makeText(GlobalContextApplication.getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override  // 线程池中调用
                public void onResponse(Call call, Response response) throws IOException {
                    String resp = response.body().string();
                    boolean b = false;
                    switch (type) {
                        case "province":
                            b = Utility.handleProvinceResponse(resp);
                            break;
                        case "city":
                            b = Utility.handleCityResponse(resp, selectedProvince.getId());
                            break;
                        case "county":
                            b = Utility.handleCountyResponse(resp, selectedCity.getId());
                            break;
                    }

                    if (b) {   // 处理响应成功
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dismissProgressDialog();
                                switch (type) {
                                    case "province":
                                        queryProvinces();
                                        break;
                                    case "city":
                                        queryCities();
                                        break;
                                    case "county":
                                        queryCounties();
                                        break;
                                }
                            }
                        });
                    }
                }
            });

        }

    }

    ProgressDialog progressDialog;

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setTitle("正在加载...");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.show();
        }
    }

    private void dismissProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        dismissRefreshing();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            rollbackList();
            return true;
        }

        return false;
    }

    /**
     * 处理返回键，尽可能回滚
     */
    private void rollbackList() {
        if (currentLevel == LEVEL_PROVINCE) {
            getActivity().finish();
        } else if (currentLevel == LEVEL_CITY) {
            queryProvinces();
        } else if (currentLevel == LEVEL_COUNTY) {
            queryCities();
        }
    }


    public interface OnRefreshLayoutListener {
        void onRefresh(String weatherId, String cityName);
    }

    OnRefreshLayoutListener listener;

    public void setOnRefreshLayoutListener(OnRefreshLayoutListener listener) {
        this.listener = listener;
    }
}
