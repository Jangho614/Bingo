package com.example.myapp_230604;

public class RecycleData {
    public String id;
    public String wrongType;
    public String wrongTime;
    public String process;

    public RecycleData() {
        // Firebase Realtime Database 사용을 위한 기본 생성자
    }
    public RecycleData(String id, String wrongType, String wrongTime, String process) {
        this.id = id;
        this.wrongTime = wrongTime;
        this.wrongType = wrongType;
        this.process = process;
    }
}
