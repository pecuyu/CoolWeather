package com.yu.coolweather;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.yu.coolweather.utils.HttpUtil;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by D22436 on 2017/8/10.
 */

public class AutoUpdateService extends Service {
    Timer timer;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String weatherId = intent.getStringExtra("weatherId");
//        final String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=bc0418b57b2d4918819d3974ac1285d9";
        final String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=5d5bdbd790a24dc495d3ed56d9a68a16";
        updateWeather(weatherUrl);

        if (timer == null) {
            timer = new Timer();
            TimerTask timerTask=new TimerTask() {
                @Override
                public void run() {
                    updateWeather(weatherUrl);
                    loadBingPic();
                }
            };
            timer.schedule(timerTask,60*1000,10*60*1000);  // 定时更新
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void updateWeather(String weatherUrl) {
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("TAG", "failed to update");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String weatherResponse = response.body().string();
                getSharedPreferences("weather", MODE_APPEND | MODE_PRIVATE)
                        .edit()
                        .putString("weather", weatherResponse)
                        .apply();
                Intent intent = new Intent(WeatherActivity.ACTION_UPDATE_WEATHER);
                sendBroadcast(intent);  // 发送更新广播
            }
        });
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
                getSharedPreferences("weather",MODE_APPEND|MODE_PRIVATE).edit()
                .putString("bing_pic", bingPic)
                .apply();
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
    }
}
