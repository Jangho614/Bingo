package com.example.myapp_230604;

import android.net.Uri;

public class RecycleData {
    private Uri uri;
    private String type;  // 설명
    private String time;  // 날짜

    // 기본 생성자 (Firebase에서 데이터베이스로부터 객체를 생성할 때 필요)
    public RecycleData(String string, String recycleType, String recordedTime) {}

    public RecycleData(Uri uri, String type, String time) {
        this.uri = uri;
        this.type = type;
        this.time = time;
    }

    public Uri getUri() {
        return uri;
    }

    public String getType() {
        return type;
    }

    public String getTime() {
        return time;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }
}
