package com.yu.coolweather;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.yu.coolweather.entity.Forecast;
import com.yu.coolweather.entity.Weather;
import com.yu.coolweather.utils.HttpUtil;
import com.yu.coolweather.utils.Utility;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity implements ChooseAreaFragment.OnRefreshLayoutListener {
    public static final String ACTION_UPDATE_WEATHER = "com.yu.coolweather.UPDATE_WEATHER";
    TextView tvTitle, tvUpdateTime, tvDegreeNow, tvDescNow,
            tvAqi, tvPM25, tvComfort, tvWashCar, tvSport;

    // forecast item
    TextView tvDateForecast, tvDescForecast, tvMaxForecast, tvMinForecast;
    LinearLayout forecastContainer;
    String cityName = "未知";

    ImageView ivLocation;
    ImageView bingImg;

    private String mWeatherId;

    SwipeRefreshLayout swipeRefresh;

    ScrollView scrollView;

    DrawerLayout drawer;

    WeatherUpdateReceiver updateReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
        initDatas();
        initEvents();

        startUpdateService();
        registerUpdateReceiver();

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
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(mWeatherId);
            }
        });

        ivLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.openDrawer(Gravity.LEFT);
            }
        });

        tvTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.openDrawer(Gravity.LEFT);
            }
        });
    }

    public void dismissSwipeRefresh(boolean success) {
        if (swipeRefresh != null && swipeRefresh.isRefreshing()) {
            swipeRefresh.setRefreshing(false);
            if (success) {
                Toast.makeText(GlobalContextApplication.getContext(), "refresh success", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(GlobalContextApplication.getContext(), "refresh failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initDatas() {
        swipeRefresh.setRefreshing(true);
        SharedPreferences sp = getSharedPreferences("weather", Context.MODE_APPEND | Context.MODE_PRIVATE);
        String weatherCache = sp.getString("weather", null);
        cityName = sp.getString("cityName", "未知");
        if (!TextUtils.isEmpty(weatherCache)) {    // 有缓存
            Weather weather = Utility.handleWeatherResponse(weatherCache);
            if (weather.basic != null) {
                weather.basic.cityName = cityName;
                mWeatherId = weather.basic.weatherId;
            } else {
                mWeatherId = sp.getString("weatherId", null);
            }
            showWeatherInfo(weather);
            swipeRefresh.setRefreshing(false);
        } else {
            Intent intent = getIntent();
            mWeatherId = intent.getStringExtra("weatherId");
            cityName = intent.getStringExtra("city");
            requestWeather(mWeatherId);
            loadBingPic();
        }
    }

    private void initViews() {
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        setContentView(R.layout.activity_weather);

        tvTitle = (TextView) findViewById(R.id.id_tv_title_weather);
        tvUpdateTime = (TextView) findViewById(R.id.id_tv_update_time_weather);
        tvDegreeNow = (TextView) findViewById(R.id.id_tv_degree_now_weather);
        tvDescNow = (TextView) findViewById(R.id.id_tv_desc_now_weather);
        tvAqi = (TextView) findViewById(R.id.id_tv_aqi_info);
        tvPM25 = (TextView) findViewById(R.id.id_tv_air_pm25);
        tvComfort = (TextView) findViewById(R.id.id_tv_comfort_suggestion);
        tvWashCar = (TextView) findViewById(R.id.id_tv_wash_car_suggestion);
        tvSport = (TextView) findViewById(R.id.id_tv_sport_suggestion);
        ivLocation = (ImageView) findViewById(R.id.id_iv_choose_area);
        bingImg = (ImageView) findViewById(R.id.id_iv_bg_weather);
        drawer = (DrawerLayout) findViewById(R.id.id_drawer_weather);

        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.id_refresh_weather);
        swipeRefresh.setColorSchemeColors(getResources().getColor(R.color.colorAccent));
        // 设置显示位置
        swipeRefresh.setProgressViewOffset(true, 0,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200, getResources().getDisplayMetrics()));
        scrollView = (ScrollView) findViewById(R.id.id_weather_scroll_view);
        scrollView.setVisibility(View.INVISIBLE);

        forecastContainer = (LinearLayout) findViewById(R.id.id_ll_forecast);

        ChooseAreaFragment fragment = new ChooseAreaFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.id_fl_container_left, fragment).commit();
        fragment.setOnRefreshLayoutListener(this);
    }

    public void requestWeather(final String weatherId) {
//        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=bc0418b57b2d4918819d3974ac1285d9";
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=5d5bdbd790a24dc495d3ed56d9a68a16";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dismissSwipeRefresh(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                Log.e("TAG", "responseText=" + responseText);

                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)) {
                            weather.basic.cityName = cityName;
                            mWeatherId = weather.basic.weatherId;
                            getSharedPreferences("weather", Context.MODE_APPEND)
                                    .edit()
                                    .putString("weather", responseText)
                                    .putString("weatherId", mWeatherId)
                                    .putString("cityName", cityName).apply();
                            dismissSwipeRefresh(true);
                            showWeatherInfo(weather);
                        } else {
                            dismissSwipeRefresh(false);
                            Toast.makeText(GlobalContextApplication.getContext(), "加载天气信息失败", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            }
        });
    }

    private void showWeatherInfo(Weather weather) {
        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime;
        String temperature = weather.now.temperature;
        String nowInfo = weather.now.more.info;
        List<Forecast> forecastList = weather.daily_forecast;
        String aqi = null;
        String pm25 = null;
        if (weather.aqi != null) {
            aqi = weather.aqi.city.aqi;
            pm25 = weather.aqi.city.pm25;
        }
        String comfortInfo = "舒适度：" + weather.suggestion.comfort.info;
        String carWashInfo = "洗车指数：" + weather.suggestion.carWash.info;
        String sportInfo = "运行建议：" + weather.suggestion.sport.info;

        tvTitle.setText(cityName);
        tvUpdateTime.setText(updateTime.split(" ")[1]);
        tvDegreeNow.setText(temperature + "°C");
        tvDescNow.setText(nowInfo);
        tvAqi.setText(aqi);
        tvPM25.setText(pm25);
        tvComfort.setText(comfortInfo);
        tvWashCar.setText(carWashInfo);
        tvSport.setText(sportInfo);

        forecastContainer.removeAllViews();
        for (Forecast forecast : forecastList) {
            View view = View.inflate(WeatherActivity.this, R.layout.forecast_item, null);
            tvDateForecast = (TextView) view.findViewById(R.id.id_tv_date_forecast_item);
            tvDescForecast = (TextView) view.findViewById(R.id.id_tv_info_forecast_item);
            tvMaxForecast = (TextView) view.findViewById(R.id.id_tv_max_forecast_item);
            tvMinForecast = (TextView) view.findViewById(R.id.id_tv_min_forecast_item);

            tvDateForecast.setText(forecast.date);
            tvDescForecast.setText(forecast.more.info);
            tvMaxForecast.setText(forecast.temperature.max);
            tvMinForecast.setText(forecast.temperature.min);

            forecastContainer.addView(view);
        }
        // 显示
        if (scrollView.getVisibility() == View.INVISIBLE) {
            scrollView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onRefresh(String weatherId, String cityName) {
        if (drawer != null) drawer.closeDrawer(Gravity.LEFT);
        Toast.makeText(GlobalContextApplication.getContext(), "onRefresh", Toast.LENGTH_SHORT).show();
        this.cityName = cityName;
        drawer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onGlobalLayout() {
                swipeRefresh.setRefreshing(true);
                drawer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
        requestWeather(weatherId);
    }

    public class WeatherUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case ACTION_UPDATE_WEATHER:
                    swipeRefresh.setRefreshing(true);
                    requestWeather(mWeatherId);
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
                getSharedPreferences("weather",MODE_APPEND).edit()
                        .putString("bing_pic", bingPic)
                        .apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        RequestBuilder<TranscodeType> thumbnailRequest
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
}
