package com.yu.coolweather.db;

import org.litepal.crud.DataSupport;

/**
 * Created by D22436 on 2017/8/8.
 * 县
 */

public class County extends DataSupport {
    private int id;
    private String countyName;
    private String weatherId;
    private int cityId;

    public County(String countyName, String weatherId, int cityId) {
        this.countyName = countyName;
        this.weatherId = weatherId;
        this.cityId = cityId;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setCountyName(String countyName) {
        this.countyName = countyName;
    }

    public void setWeatherId(String weatherId) {
        this.weatherId = weatherId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public int getId() {

        return id;
    }

    public String getCountyName() {
        return countyName;
    }

    public String getWeatherId() {
        return weatherId;
    }

    public int getCityId() {
        return cityId;
    }
}
