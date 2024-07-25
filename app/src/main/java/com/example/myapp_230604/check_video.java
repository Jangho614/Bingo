package com.example.myapp_230604;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;


public class check_video extends AppCompatActivity {
    Button id_record;
    Button video_rm;
    ImageView back_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_video);
        id_record = findViewById(R.id.button7);
        id_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),"아이디 전적 확인",Toast.LENGTH_SHORT).show();
            }
        });
        video_rm = findViewById(R.id.button6);
        video_rm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),"영상 기록 삭제",Toast.LENGTH_SHORT).show();
            }
        });
        back_btn = findViewById(R.id.back_btn2);
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), videoFragment_m.class);
                startActivity(intent);
            }

        });
    }
}