package com.yu.coolweather;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.yu.coolweather.entity.Forecast;
import com.yu.coolweather.entity.Weather;
import com.yu.coolweather.utils.HttpUtil;
import com.yu.coolweather.utils.Utility;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by D22436 on 2017/8/10.
 */

public class WeatherFragment extends Fragment {
    private TextView tvDegreeNow;
    private TextView tvDescNow;
    private TextView tvAqi;
    private TextView tvPM25;
    private TextView tvComfort;
    private TextView tvWashCar;
    private TextView tvSport;
    private TextView tvTitle;
    private ImageView ivLocation;

    private LinearLayout forecastContainer;

    String weatherCache;
    String mCityName;
    String mWeatherId;

    private TextView tvDateForecast;
    private TextView tvDescForecast;
    private TextView tvMaxForecast;
    private TextView tvMinForecast;

    ImageView bingImg;
    SwipeRefreshLayout swipeRefresh;

    ScrollView mViewWeather;

    public WeatherFragment() {

    }

    public static WeatherFragment newInstance(Bundle args) {
        WeatherFragment fragment = new WeatherFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_weather, container, false);
        tvDegreeNow = (TextView) view.findViewById(R.id.id_tv_degree_now_weather);
        tvDescNow = (TextView) view.findViewById(R.id.id_tv_desc_now_weather);
        tvAqi = (TextView) view.findViewById(R.id.id_tv_aqi_info);
        tvPM25 = (TextView) view.findViewById(R.id.id_tv_air_pm25);
        tvComfort = (TextView) view.findViewById(R.id.id_tv_comfort_suggestion);
        tvWashCar = (TextView) view.findViewById(R.id.id_tv_wash_car_suggestion);
        tvSport = (TextView) view.findViewById(R.id.id_tv_sport_suggestion);
        swipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.id_refresh_weather);
        swipeRefresh.setColorSchemeColors(getResources().getColor(R.color.colorAccent));
        tvTitle = (TextView) view.findViewById(R.id.id_tv_title_weather);
        ivLocation = (ImageView) view.findViewById(R.id.id_iv_choose_area);
        mViewWeather = (ScrollView) view.findViewById(R.id.id_weather_scroll_view);

        // 设置显示位置
        swipeRefresh.setProgressViewOffset(true, 0,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200, getResources().getDisplayMetrics()));

        // 预报容器
        forecastContainer = (LinearLayout) view.findViewById(R.id.id_ll_forecast);

        initEvents();
        initDatas();
        return view;
    }

    private void initDatas() {
        Bundle arguments = getArguments();
        String weatherId = (String) arguments.get("weatherId");
        mCityName = (String) arguments.get("cityName");
        weatherCache = (String) arguments.get("weatherCache");

        mViewWeather.setVisibility(View.INVISIBLE);
        if (!TextUtils.isEmpty(weatherCache)) {    // 有缓存
            Weather weather = Utility.handleWeatherResponse(weatherCache);
            if (weather.basic != null) {
                mWeatherId = weather.basic.weatherId;
            } else {
                mWeatherId = weatherId;
            }
            showWeatherInfo(weather);
            dismissSwipeRefresh(true);
        } else {
            requestWeather(weatherId);
        }
    }

    private void initEvents() {
        tvTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() instanceof WeatherActivity) {
                    ((WeatherActivity) getActivity()).openDrawer();
                }
            }
        });
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(mWeatherId);
            }
        });
        ivLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() instanceof WeatherActivity) {
                    ((WeatherActivity) getActivity()).openDrawer();
                }
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
//        if (getActivity() instanceof WeatherActivity) {
//            ((WeatherActivity )getActivity()).notifyWeatherDataChange(); // 更新数据
//        }
    }

    public void requestWeather(final String weatherId) {
        showSwipeRefresh();
//        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + mWeatherId + "&key=bc0418b57b2d4918819d3974ac1285d9";
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=5d5bdbd790a24dc495d3ed56d9a68a16";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(new Runnable() {
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
                if (getActivity() == null) return;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)) {
                            weather.basic.cityName = mCityName;
                            mWeatherId = weather.basic.weatherId;
                            SharedPreferences sp = getActivity().getSharedPreferences("weather", Context.MODE_APPEND);
                            String cityCache = sp.getString("citiesName", null);
                            if (TextUtils.isEmpty(cityCache) || !cityCache.contains(mCityName)) {
                                sp.edit().putString("citiesName", cityCache == null ? mCityName : cityCache + "#" + mCityName).apply();
                            }
                            sp.edit().putString(mCityName, mWeatherId)
                                    .putString(mWeatherId, weatherCache)
                                    .apply();
                            showWeatherInfo(weather);
                            dismissSwipeRefresh(true);
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
        String cityName = mCityName;
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

        tvTitle.setText(mCityName);
        tvDegreeNow.setText(temperature + "°C");
        tvDescNow.setText(nowInfo);
        tvAqi.setText(aqi);
        tvPM25.setText(pm25);
        tvComfort.setText(comfortInfo);
        tvWashCar.setText(carWashInfo);
        tvSport.setText(sportInfo);

        forecastContainer.removeAllViews();
        for (Forecast forecast : forecastList) {
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.forecast_item, null);
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
        mViewWeather.setVisibility(View.VISIBLE);

    }

    // 设置刷新显示
    public void showSwipeRefresh() {
        if (swipeRefresh == null || swipeRefresh.isRefreshing()) {
            return;
        }
        swipeRefresh.setRefreshing(true);
    }

    // 取消刷新显示
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
}
