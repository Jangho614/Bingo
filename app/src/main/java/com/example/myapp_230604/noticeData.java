package com.example.myapp_230604;

public class noticeData {
    public String id;
    public String title;
    public String content;

    public noticeData() {
        // Firebase Realtime Database 사용을 위한 기본 생성자
    }

    public noticeData(String id, String title, String content) {
        this.id = id;
        this.title = title;
        this.content = content;
    }
}

