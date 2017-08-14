package com.yu.coolweather;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.yu.coolweather.utils.HttpUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity implements ChooseAreaFragment.OnAddAreaListener {
    public static final String ACTION_UPDATE_WEATHER = "com.yu.coolweather.UPDATE_WEATHER";

    LinearLayout forecastContainer;
    String cityName = "未知";

    private String mWeatherId;

    ScrollView scrollView;

    DrawerLayout drawer;

    ViewPager viewPager;
    WeatherViewPagerAdapter adapter;
    WeatherUpdateReceiver updateReceiver;
    ImageView bingImg;

    ArrayList<WeatherFragment> weatherFragments = new ArrayList<>();
//    List<OnRefreshWeatherFragmentUIListener> listenerList = new ArrayList<>();

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
        initDatas();
        initEvents();

        //  startUpdateService();
        //  registerUpdateReceiver();

    }

    private void startUpdateService() {
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }

    private void registerUpdateReceiver() {
        updateReceiver = new WeatherUpdateReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_UPDATE_WEATHER);
        registerReceiver(updateReceiver, filter);
    }

    private void initEvents() {


    }


    private void initViews() {
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        setContentView(R.layout.activity_weather);

        viewPager = (ViewPager) findViewById(R.id.id_pager_weather);
        bingImg = (ImageView) findViewById(R.id.id_iv_bg_weather);
        drawer = (DrawerLayout) findViewById(R.id.id_drawer_weather);

        ChooseAreaFragment areaFragment = new ChooseAreaFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.id_fl_container_left, areaFragment).commit();
        areaFragment.setOnAddAreaListener(this);
    }

    private void initDatas() {
        List<String> weatherIds = new ArrayList<>();
        List<String> weatherCaches = new ArrayList<>();
        SharedPreferences sp = getSharedPreferences("weather", MODE_APPEND);

        Intent intent = getIntent();
        if (intent.getStringExtra("cityName") != null) {
            String cityName = intent.getStringExtra("cityName");
            String weatherId = intent.getStringExtra("weatherId");
            Bundle args = new Bundle();
            args.putString("weatherId", weatherId);
            args.putString("cityName", cityName);
            String weatherCache = sp.getString(weatherId, null);
            args.putString("weatherCache", weatherCache);
            WeatherFragment fragment = WeatherFragment.newInstance(args);
            weatherFragments.add(fragment);

        } else {
            String cityArray = sp.getString("citiesName", null);
            if (TextUtils.isEmpty(cityArray)) {
                Toast.makeText(GlobalContextApplication.getContext(), "请选择城市", Toast.LENGTH_SHORT).show();
                openDrawer();
            }

            String[] cities = cityArray.split("#");
            // 通过city来获取weatherId
            for (String city : cities) {
                if (TextUtils.isEmpty(city)) continue;
                String weatherId = sp.getString(city, null);
                weatherIds.add(weatherId);
            }
            // 通过weatherId来获取weatherCache
            for (String weatherId : weatherIds) {
                if (TextUtils.isEmpty(weatherId)) continue;
                String weatherCache = sp.getString(weatherId, null);
                weatherCaches.add(weatherCache);
            }

            if (weatherIds.size() <= 0 || cities.length <= 0) {
                Toast.makeText(GlobalContextApplication.getContext(), "请选择城市", Toast.LENGTH_SHORT).show();
                openDrawer();
            }

            // 初始化fragments
            for (int i = 0; i < cities.length; i++) {
                if (TextUtils.isEmpty(cities[i]) || TextUtils.isEmpty(weatherIds.get(i))) {
                    continue;
                }
                Bundle args = new Bundle();
                args.putString("weatherId", weatherIds.get(i));
                args.putString("cityName", cities[i]);
                args.putString("weatherCache", weatherCaches.get(i));
                WeatherFragment fragment = WeatherFragment.newInstance(args);
                weatherFragments.add(0,fragment);
            }
        }

        if (weatherFragments.size() == 0) {
            viewPager.setVisibility(View.GONE);
        } else {
            viewPager.setVisibility(View.VISIBLE);
        }

        adapter = new WeatherViewPagerAdapter(getSupportFragmentManager(), weatherFragments);
        viewPager.setAdapter(adapter);
    }


    @Override
    public void onRefreshAreaAdd(String weatherId, String cityName) {
        if (drawer != null) drawer.closeDrawer(Gravity.LEFT);
        this.cityName = cityName;

        Bundle args = new Bundle();
        args.putString("weatherId", weatherId);
        args.putString("cityName", cityName);
        args.putString("weatherCache", null);
        WeatherFragment fragment = WeatherFragment.newInstance(args);
        weatherFragments.add(fragment);
//        adapter = new WeatherViewPagerAdapter(getSupportFragmentManager(), weatherFragments);
//        viewPager.setAdapter(adapter);
        notifyWeatherDataChange();
    }

    public void notifyWeatherDataChange() {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
            viewPager.setCurrentItem(weatherFragments.size()-1);
        }
    }

    public class WeatherUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case ACTION_UPDATE_WEATHER:
                    String bingPic = getSharedPreferences("weather", MODE_APPEND).getString("bing_pic", null);
                    Glide.with(GlobalContextApplication.getContext()).load(bingPic).placeholder(R.mipmap.ic_bg).into(bingImg);
                    break;
            }
        }
    }

    /**
     * 加载必应每日一图
     */
    private void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                getSharedPreferences("weather", MODE_APPEND).edit()
                        .putString("bing_pic", bingPic)
                        .apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // placeholder设置等待时的图片
                        Glide.with(WeatherActivity.this).load(bingPic).placeholder(R.mipmap.ic_bg).into(bingImg);
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (updateReceiver != null) unregisterReceiver(updateReceiver);
    }

    public void openDrawer() {
        if (drawer != null && !drawer.isDrawerOpen(Gravity.LEFT)) {
            drawer.openDrawer(Gravity.LEFT);
        }
    }

    public void closeDrawer() {
        if (drawer != null && drawer.isDrawerOpen(Gravity.LEFT)) {
            drawer.closeDrawer(Gravity.LEFT);
        }
    }


}
