package com.yu.coolweather;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChooseAreaFragment extends Fragment {
    Button btnBack;
    TextView tvTitle;
    ListView listView;
    ArrayAdapter<String> adapter;

    private List<String> dataList;
    /*  省市县集合 */
    private List<Province> provinceList = new ArrayList<Province>();
    private List<City> cityList = new ArrayList<>();

    private List<County> countyList = new ArrayList<>();

    private int LEVEL_PROVINCE = 0;  // 城市级别
    private int LEVEL_CITY = 1;  // 城市级别
    private int LEVEL_COUNTY = 2; // 县级别

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

            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    /**
     * 查找省信息
     */
    private void queryProvinces() {
        tvTitle.setText("中国");
        btnBack.setVisibility(View.GONE);
        provinceList = DataSupport.findAll(Province.class);
        if (provinceList.size() > 0) {
            dataList.clear();
            for (Province province:provinceList) {
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LE
        } else {

        }
    }
}
