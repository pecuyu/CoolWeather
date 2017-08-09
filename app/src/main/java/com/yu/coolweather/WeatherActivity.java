package com.yu.coolweather;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yu.coolweather.entity.Forecast;
import com.yu.coolweather.entity.Weather;
import com.yu.coolweather.utils.HttpUtil;
import com.yu.coolweather.utils.Utility;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {
    TextView tvTitle,tvUpdateTime,tvDegreeNow,tvDescNow,
            tvAqi,tvPM25,tvComfort,tvWashCar,tvSport;

    // forecast item
    TextView tvDateForecast,tvDescForecast,tvMaxForecast, tvMinForecast;
    LinearLayout forecastContainer;
    private String mWeatherId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);

        initViews();
        initDatas();
    }

    private void initDatas() {
        String weatherCache = getSharedPreferences("weather", Context.MODE_APPEND | Context.MODE_PRIVATE).getString("weather", null);
        if (!TextUtils.isEmpty(weatherCache)) {    // 有缓存
            Weather weather = Utility.handleWeatherResponse(weatherCache);
            mWeatherId = weather.basic.weatherId;
            showWeatherInfo(weather);
        } else {
            mWeatherId = getIntent().getStringExtra("weatherId");
            requestWeather(mWeatherId);
        }
    }

    private void initViews() {
        tvTitle = (TextView) findViewById(R.id.id_tv_title_weather);
        tvUpdateTime = (TextView) findViewById(R.id.id_tv_update_time_weather);
        tvDegreeNow = (TextView) findViewById(R.id.id_tv_degree_now_weather);
        tvDescNow = (TextView) findViewById(R.id.id_tv_desc_now_weather);
        tvAqi = (TextView) findViewById(R.id.id_tv_aqi_info);
        tvPM25 = (TextView) findViewById(R.id.id_tv_air_pm25);
        tvComfort = (TextView) findViewById(R.id.id_tv_comfort_suggestion);
        tvWashCar = (TextView) findViewById(R.id.id_tv_wash_car_suggestion);
        tvSport = (TextView) findViewById(R.id.id_tv_sport_suggestion);

        forecastContainer = (LinearLayout) findViewById(R.id.id_ll_forecast);

    }

    public void requestWeather(final String weatherId) {
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=bc0418b57b2d4918819d3974ac1285d9";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

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
                            mWeatherId = weather.basic.weatherId;
                            getSharedPreferences("weather", Context.MODE_APPEND | Context.MODE_PRIVATE)
                                    .edit().putString("weather",responseText).commit();
                            showWeatherInfo(weather);
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
        String aqi=null;
        String pm25=null;
        if (weather.aqi != null) {
            aqi = weather.aqi.city.aqi;
            pm25 = weather.aqi.city.pm25;
        }
        String comfortInfo = "舒适度：" + weather.suggestion.comfort.info;
        String carWashInfo = "洗车指数：" + weather.suggestion.carWash.info;
        String sportInfo = "运行建议：" + weather.suggestion.sport.info;

        tvTitle.setText(cityName);
        tvUpdateTime.setText(updateTime.split(" ")[1]);
        tvDegreeNow.setText(temperature+"°C");
        tvDescNow.setText(nowInfo);
        tvAqi.setText(aqi);
        tvPM25.setText(pm25);
        tvComfort.setText(comfortInfo);
        tvWashCar.setText(carWashInfo);
        tvSport.setText(sportInfo);

        for (Forecast forecast : forecastList) {
            View view = View.inflate(WeatherActivity.this, R.layout.forecast_item, null);
            tvDateForecast= (TextView) view.findViewById(R.id.id_tv_date_forecast_item);
            tvDescForecast= (TextView) view.findViewById(R.id.id_tv_info_forecast_item);
            tvMaxForecast = (TextView) view.findViewById(R.id.id_tv_max_forecast_item);
            tvMinForecast = (TextView) view.findViewById(R.id.id_tv_min_forecast_item);

            tvDateForecast.setText(forecast.date);
            tvDescForecast.setText(forecast.more.info);
            tvMaxForecast.setText(forecast.temperature.max);
            tvMinForecast.setText(forecast.temperature.min);

            forecastContainer.addView(view);
        }
    }
}
