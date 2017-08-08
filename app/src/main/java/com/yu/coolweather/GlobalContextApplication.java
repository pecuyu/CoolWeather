package com.yu.coolweather;

import android.app.Application;
import android.content.Context;

import org.litepal.LitePalApplication;

/**
 * Created by D22436 on 2017/8/8.
 */

public class GlobalContextApplication extends Application {
    private static Context context;
    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        LitePalApplication.initialize(context);  // 初始化litepal

    }


    /**
     * 获取全局上下文对象
     * @return
     */
    public  static Context getGlobalContext() {
        return context;
    }
}
