package com.example.myapp_230604;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class DetailActivity extends AppCompatActivity {
    ImageView back_btn;
    TextView time,type;

    private MediaPlayer mediaPlayer;
    private Button playButton;
    private Uri audioUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        time = findViewById(R.id.time_text);
        type = findViewById(R.id.type_text);
        back_btn = findViewById(R.id.back_btn6);

        type.setText(getIntent().getStringExtra("RECYCLE_TYPE"));
        time.setText(getIntent().getStringExtra("RECYCLE_TIME"));

        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),mainActivity.class);
                startActivity(intent);
            }
        });

        // Retrieve the audio URI from the intent
        String audioUriString = getIntent().getStringExtra("AUDIO_URI");
        if (audioUriString != null) {
            audioUri = Uri.parse(audioUriString);
        } else {
            Toast.makeText(this, "오디오 파일을 찾을 수 없습니다", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void playAudio() {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(this, audioUri);
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mediaPlayer.start();
        Toast.makeText(this, "오디오 재생 중", Toast.LENGTH_SHORT).show();

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                stopAudio();
            }
        });
    }

    private void stopAudio() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
            Toast.makeText(this, "오디오 중지", Toast.LENGTH_SHORT).show();
        }
    }
}