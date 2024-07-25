package com.example.myapp_230604;

import android.app.Application;

import com.google.firebase.FirebaseApp;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Firebase 초기화
        FirebaseApp.initializeApp(this);
    }
}
