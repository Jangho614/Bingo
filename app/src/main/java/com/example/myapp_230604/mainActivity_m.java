package com.example.myapp_230604;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.FirebaseApp;

public class mainActivity_m extends AppCompatActivity {

    chartFragment chartFragment;
    noticeFragment_m noticeFragment;
    videoFragment_m videoFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseApp.initializeApp(this);
        setContentView(R.layout.manager_bottom_nevigation);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

        chartFragment = new chartFragment();
        videoFragment = new videoFragment_m();
        noticeFragment = new noticeFragment_m();

        getSupportFragmentManager().beginTransaction().replace(R.id.containers,videoFragment).commit();

        NavigationBarView navigationBarView = findViewById(R.id.bottom_navigation_view_m);
        navigationBarView.setSelectedItemId(R.id.video);
        navigationBarView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.chart) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.containers, chartFragment).commit();
                    return true;
                } else if (id == R.id.video) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.containers, videoFragment).commit();
                    return true;
                } else if (id == R.id.notice) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.containers, noticeFragment).commit();
                    return true;
                }
                return false;
            }
        });
    }
}
