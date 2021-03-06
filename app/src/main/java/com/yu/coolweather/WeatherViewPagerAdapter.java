package com.yu.coolweather;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * Created by D22436 on 2017/8/10.
 */

public class WeatherViewPagerAdapter extends FragmentPagerAdapter {

    List<WeatherFragment> weatherFragmentList;

    public WeatherViewPagerAdapter(FragmentManager fm, List<WeatherFragment> weatherFragmentList) {
        super(fm);
        this.weatherFragmentList = weatherFragmentList;
    }

    @Override
    public Fragment getItem(int position) {
        return weatherFragmentList.get(position);
    }

    @Override
    public int getItemPosition(Object object) {
        return weatherFragmentList.indexOf(object);
    }

    @Override
    public int getCount() {
        return weatherFragmentList.size();
    }
}
