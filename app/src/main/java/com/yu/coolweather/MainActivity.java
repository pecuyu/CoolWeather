package com.yu.coolweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        SharedPreferences sp = getSharedPreferences("weather", MODE_APPEND | MODE_PRIVATE);
        if (sp.getString("weather", null) != null) {
            Intent intent = new Intent(this, WeatherActivity.class);
            startActivity(intent);
            finish();
        } else {
            ChooseAreaFragment fragment = new ChooseAreaFragment();
            listener = fragment;
            getSupportFragmentManager().beginTransaction().replace(R.id.id_ll_main_container,fragment).commit();
        }

    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (listener!=null) listener.onKeyDown(keyCode, event);
        return false;
    }

    // 实体键单机监听
    private onKeyDownListener listener;
    public interface onKeyDownListener{
        public boolean onKeyDown(int keyCode, KeyEvent event);
    }
}
