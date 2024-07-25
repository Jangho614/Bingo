package com.example.myapp_230604;


import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class board_writing extends AppCompatActivity {

    private DatabaseReference databaseReference;

    EditText titleText;
    EditText contentText;

    Button register_btn;
    ImageView back_btn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board_writing);
        titleText = findViewById(R.id.editTextTitle);
        contentText = findViewById(R.id.editTextContent);

        databaseReference = FirebaseDatabase.getInstance().getReference("posts");

        //게시물 등록
        register_btn = findViewById(R.id.button3);
        register_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePost();
            }
        });
        //뒤로 가기
        back_btn = findViewById(R.id.back_btn4);
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    private void savePost(){
        String id = databaseReference.push().getKey();
        String title = titleText.getText().toString();
        String content = contentText.getText().toString();

        noticeData data = new noticeData(id,title,content);
        if(id != null){
            databaseReference.child(id).setValue(data);
            Toast.makeText(this, "Notice Upload", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}