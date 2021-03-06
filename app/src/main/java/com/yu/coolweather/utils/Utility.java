package com.yu.coolweather.utils;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.yu.coolweather.db.City;
import com.yu.coolweather.db.County;
import com.yu.coolweather.db.Province;
import com.yu.coolweather.entity.Weather;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by D22436 on 2017/8/8.
 */

public class Utility {
    /**
     * 解析返回的省级数据
     * @param response
     * @return
     */
    public static boolean handleProvinceResponse(String response) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray array = new JSONArray(response);
               for (int i=0;i<array.length();i++) {
                   JSONObject obj = array.getJSONObject(i);
                   Province province = new Province(obj.getString("name"), obj.getInt("id"));
                   province.save();
               }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return false;
    }
    /**
     * 解析返回的市级数据
     * @param response
     * @return
     */
    public static boolean handleCityResponse(String response,int provinceId) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray array = new JSONArray(response);
               for (int i=0;i<array.length();i++) {
                   JSONObject obj = array.getJSONObject(i);
                   City city = new City(obj.getString("name"), obj.getInt("id"),i,provinceId);
                   city.save();
               }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    /**
     * 解析返回的县级数据
     * @param response
     * @return
     */
    public static boolean handleCountyResponse(String response,int cityId) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray array = new JSONArray(response);
                for (int i=0;i<array.length();i++) {
                    JSONObject obj = array.getJSONObject(i);
                    County county = new County(obj.getString("name"), obj.getString("weather_id"),cityId);
                    county.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    /**
     * 解析天气的json数据
     * @param response
     * @return
     */
    public static Weather handleWeatherResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray array = jsonObject.getJSONArray("HeWeather");
            String content = array.getJSONObject(0).toString();
            return new Gson().fromJson(content, Weather.class);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
