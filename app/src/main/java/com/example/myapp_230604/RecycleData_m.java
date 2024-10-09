package com.example.myapp_230604;

public class RecycleData_m {
    public String id;
    public String wrongTime;
    public String process;
    public String wrongType;

    // 기본 생성자 (Firebase에서 데이터를 읽어올 때 필요)
    public RecycleData_m() {}

    // 모든 필드를 포함한 생성자
    public RecycleData_m(String id, String wrongTime, String process, String wrongType) {
        this.id = id;
        this.wrongTime = wrongTime;
        this.process = process;
        this.wrongType = wrongType;
    }

    // Getter 및 Setter 메서드
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getWrongTime() {
        return wrongTime;
    }

    public void setWrongTime(String wrongTime) {
        this.wrongTime = wrongTime;
    }

    public String getProcess() {
        return process;
    }

    public void setProcess(String process) {
        this.process = process;
    }

    public String getWrongType() {
        return wrongType;
    }

    public void setWrongType(String wrongType) {
        this.wrongType = wrongType;
    }
}
