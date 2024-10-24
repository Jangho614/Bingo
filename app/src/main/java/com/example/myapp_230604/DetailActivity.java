package com.example.myapp_230604;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class DetailActivity extends AppCompatActivity {
    ImageView back_btn;
    TextView time,type;
    ImageView img;

    private MediaPlayer mediaPlayer;
    private Button playButton;
    private Uri audioUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        time = findViewById(R.id.time_text);
        type = findViewById(R.id.type_text);
        img = findViewById(R.id.user_img);

        back_btn = findViewById(R.id.back_btn6);

        type.setText(getIntent().getStringExtra("RECYCLE_TYPE"));
        time.setText(getIntent().getStringExtra("RECYCLE_TIME"));
        img.setImageBitmap(BitmapFactory.decodeFile(getIntent().getStringExtra("IMAGE_URI")));

        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), mainActivity.class);
                startActivity(intent);
            }
        });

    }
}