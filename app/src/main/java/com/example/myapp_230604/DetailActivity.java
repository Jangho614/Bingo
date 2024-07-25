package com.example.myapp_230604;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class DetailActivity extends AppCompatActivity {

    public static final String EXTRA_TYPE_RESULT = "type_result";
    public static final String EXTRA_TIMESTAMP = "timestamp";

    private String typeResult;
    private String timestamp;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        if (intent != null) {
            typeResult = intent.getStringExtra(EXTRA_TYPE_RESULT);
            timestamp = intent.getStringExtra(EXTRA_TIMESTAMP);
        }

        ImageView backBtn = findViewById(R.id.backbtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        TextView typeResultText = findViewById(R.id.type_result_text);
        TextView timestampText = findViewById(R.id.timestamp_text);
        VideoView videoView = findViewById(R.id.videoView);

        typeResultText.setText(typeResult);
        timestampText.setText(timestamp);

        // VideoView 설정 (필요 시)
        // videoView.setVideoURI(...);
    }
}
