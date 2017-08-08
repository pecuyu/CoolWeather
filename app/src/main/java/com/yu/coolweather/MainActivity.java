package com.yu.coolweather;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.FrameLayout;

public class MainActivity extends AppCompatActivity {

    FrameLayout leftContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        leftContainer = (FrameLayout) findViewById(R.id.id_fl_container_left);
        ChooseAreaFragment fragment = new ChooseAreaFragment();
        getSupportFragmentManager().beginTransaction().replace(leftContainer.getId(),fragment).commit();
    }
}
