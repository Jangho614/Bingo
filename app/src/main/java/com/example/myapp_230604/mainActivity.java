package com.example.myapp_230604;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.navigation.NavigationBarView;

public class mainActivity extends AppCompatActivity {
    com.example.myapp_230604.chartFragment chartFragment;
    userFragment recycleFragment;
    noticeFragment noticeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_bottom_nevigation);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

        chartFragment = new chartFragment();
        recycleFragment = new userFragment();
        noticeFragment = new noticeFragment();

        getSupportFragmentManager().beginTransaction().replace(R.id.containers,recycleFragment).commit();

        NavigationBarView navigationBarView = findViewById(R.id.bottom_navigation_view);
        navigationBarView.setSelectedItemId(R.id.recycle);
        navigationBarView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.chart) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.containers, chartFragment).commit();
                    return true;
                } else if (id == R.id.recycle) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.containers, recycleFragment).commit();
                    return true;
                } else if (id == R.id.board) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.containers, noticeFragment).commit();
                    return true;
                }
                return false;
            }
        });
    }

};
