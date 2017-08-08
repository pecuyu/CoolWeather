package com.yu.coolweather;


import android.app.ProgressDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

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
public class ChooseAreaFragment extends Fragment {
    Button btnBack;
    TextView tvTitle;
    ListView listView;
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
    private Province selectProvince;

    private City selectCity;


    public ChooseAreaFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_choose_area, container, false);

        btnBack = (Button) view.findViewById(R.id.id_btn_back_choose_area);
        tvTitle = (TextView) view.findViewById(R.id.id_tv_title_choose_area);
        listView = (ListView) view.findViewById(R.id.id_lv_choose_area);
        adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (currentLevel) {
                    case LEVEL_PROVINCE:
                        selectProvince = provinceList.get(position);
                        queryCities();
                        break;
                    case LEVEL_CITY:
                        selectCity = cityList.get(position);
                        queryCounties();
                        break;
                    case LEVEL_COUNTY:
                        //
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
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
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
        tvTitle.setText(selectProvince.getProvinceName());
        btnBack.setVisibility(View.VISIBLE);
        Cursor cursor = DataSupport.findBySQL("select * from city where provinceid = ?" ,String.valueOf(selectProvince.getId()));
        if (cursor!=null) {
            while (cursor.moveToNext()) {
                String cityName = cursor.getString(cursor.getColumnIndex("cityname"));
                int cityCode = cursor.getInt(cursor.getColumnIndex("citycode"));
                int provinceId = cursor.getInt(cursor.getColumnIndex("provinceid"));
                City city = new City(cityName,cityCode, provinceId);
                cityList.add(city);
            }
            cursor.close();
        }
        if (cityList.size() > 0) {
            dataList.clear();
            for (City city : cityList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;

        } else {
            int provinceCode = selectProvince.getProvinceCode();
            String url = "http://guolin.tech/api/china" + "/"+provinceCode;
            queryFromSever(url, "city");
        }
    }

    private void queryCounties() {
        tvTitle.setText(selectCity.getCityName());
        btnBack.setVisibility(View.VISIBLE);
        List<County> countyList = DataSupport.where("cityid=?", String.valueOf(selectCity.getId())).find(County.class);
        if (countyList.size() > 0) {
            dataList.clear();
            for (County county : countyList) {
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        } else {
            int provinceCode = selectProvince.getProvinceCode();
            int cityCode = selectCity.getCityCode();
            String url = "http://guolin.tech/api/china" + provinceCode + "/" + cityCode;
            queryFromSever(url, "county");
        }
    }

    private void queryFromSever(String url, final String type) {
        showProgressDialog();
        if (!TextUtils.isEmpty(url) && !TextUtils.isEmpty(type)) {
            HttpUtil.sendOkHttpRequest(url, new Callback() {
                @Override // 线程池中调用
                public void onFailure(Call call, IOException e) {
                    dismissProgressDialog();

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
                            b = Utility.handleCityResponse(resp, selectProvince.getId());
                            break;
                        case "county":
                            b = Utility.handleCountyResponse(resp, selectCity.getId());
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
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("正在加载...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();

    }

    private void dismissProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}
