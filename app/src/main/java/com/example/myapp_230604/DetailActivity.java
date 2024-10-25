package com.example.myapp_230604;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class DetailActivity extends AppCompatActivity {
    ImageView back_btn;
    TextView time,type;
    ImageView img;
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
        String getUri = getIntent().getStringExtra("IMAGE_URI");

        // Bitmap을 가져오고 90도 회전
        Bitmap originalBitmap = BitmapFactory.decodeFile(getUri);
        Matrix matrix = new Matrix();
        matrix.postRotate(270); // 90도 회전
        Bitmap rotatedBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.getWidth(), originalBitmap.getHeight(), matrix, true);

        img.setImageBitmap(rotatedBitmap); // 회전된 이미지를 ImageView에 설정
        Toast.makeText(this, getUri, Toast.LENGTH_SHORT).show();

        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), mainActivity.class);
                startActivity(intent);
            }
        });
    }

}