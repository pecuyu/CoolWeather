package com.yu.coolweather.entity;

import com.google.gson.annotations.SerializedName;

/**
 * Created by D22436 on 2017/8/9.
 */

public class Basic {
    @SerializedName("name")
    public String cityName;  // 建立映射 cityName --> name

    @SerializedName("id")
    public String weatherId;

    public Update update;

    public class Update {
        @SerializedName("loc")
        public String updateTime;
    }
}
