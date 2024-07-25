package com.example.myapp_230604;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

public class noticeDetail extends AppCompatActivity {
    public static final String EXTRA_TITLE= "title";
    public static final String EXTRA_CONTENT = "content";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice_detail);

        String title = getIntent().getStringExtra(EXTRA_TITLE);
        String content = getIntent().getStringExtra(EXTRA_CONTENT);

        ImageView backBtn = findViewById(R.id.backbtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView titleText = findViewById(R.id.titleText);
        TextView contentText = findViewById(R.id.contentText);

        titleText.setText(title);
        contentText.setText(content);
    }
}
