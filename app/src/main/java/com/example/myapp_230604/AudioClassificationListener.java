package com.example.myapp_230604;

import org.tensorflow.lite.support.label.Category;

import java.util.List;

public interface AudioClassificationListener {
    // 오류 발생 시 호출되는 메소드
    void onError(String error);
    // 결과를 반환하는 메소드, 처리 시간도 포함
    void onResult(List<Category> results, long inferenceTime);
}
