package com.yu.coolweather;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.List;

/**
 * Created by D22436 on 2017/8/10.
 */

public class WeatherViewPager extends FragmentStatePagerAdapter {

    List<WeatherFragment> weatherFragmentList;

    public WeatherViewPager(FragmentManager fm,List<WeatherFragment> weatherFragmentList) {
        super(fm);
        this.weatherFragmentList = weatherFragmentList;
    }

    @Override
    public Fragment getItem(int position) {
        return weatherFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return weatherFragmentList.size();
    }
}
