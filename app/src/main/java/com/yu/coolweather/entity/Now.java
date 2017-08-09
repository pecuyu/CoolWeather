package com.yu.coolweather.entity;

import com.google.gson.annotations.SerializedName;

/**
 * Created by D22436 on 2017/8/9.
 */

public class Now {
    @SerializedName("tmp")
    public String temperature;

    @SerializedName("cond")
    public More more;


    public class More{
        @SerializedName("txt")
        public String info;
    }

}
