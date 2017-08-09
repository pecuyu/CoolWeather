package com.yu.coolweather.entity;

import java.util.List;

/**
 * Created by D22436 on 2017/8/9.
 */

public class Weather {
    public String status;
    public Basic basic;
    public AQI aqi;
    public Now now;
    public Suggestion suggestion;
    public List<Forecast> daily_forecast;
}
