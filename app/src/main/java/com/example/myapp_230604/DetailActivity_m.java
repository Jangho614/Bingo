package com.example.myapp_230604;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DetailActivity_m extends AppCompatActivity {
    Button rmBtn;

    public static final String EXTRA_ID= "id";
    public static final String EXTRA_TIME = "time";
    public static final String EXTRA_PROCESS = "process";
    public static final String EXTRA_TYPE = "TYPE";

    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("WrongRecycle");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_m);

        Intent intent = getIntent();
        String id = getIntent().getStringExtra(EXTRA_ID);
        String time = getIntent().getStringExtra(EXTRA_TIME);
        String process = getIntent().getStringExtra(EXTRA_PROCESS);
        String type = getIntent().getStringExtra(EXTRA_TYPE);

        rmBtn = findViewById(R.id.videoRM_btn);
        rmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (id != null) {
                    databaseReference.child(id).removeValue();
                    finish();
                }
            }
        });

        ImageView backBtn = findViewById(R.id.backbtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView idText = findViewById(R.id.id_text);
        TextView processText = findViewById(R.id.prc_text);
        TextView typeResultText = findViewById(R.id.type_text);
        TextView timestampText = findViewById(R.id.time_text);
        VideoView videoView = findViewById(R.id.videoView);

        timestampText.setText(time);
        idText.setText(id.substring(0,8));
        processText.setText(process);
        typeResultText.setText(type);

        // VideoView 설정 (필요 시)
        // videoView.setVideoURI(...);
    }
}
