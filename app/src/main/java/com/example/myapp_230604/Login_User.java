package com.example.myapp_230604;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class Login_User extends AppCompatActivity {
    Button login_btn;
    Button Signup_btn;
    TextView user_btn;
    TextView manage_btn;
    String mode="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_user);

        user_btn = findViewById(R.id.text_user);
        manage_btn = findViewById(R.id.text_manager);

        user_btn.setTextColor(Color.parseColor("#1ab833"));
        manage_btn.setTextColor(Color.parseColor("#000000"));
        mode="user";

        login_btn = findViewById(R.id.button_login);
        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                if (mode.equals("user")) {
                    intent = new Intent(getApplicationContext(), mainActivity.class);
                } else {
                    intent = new Intent(getApplicationContext(), mainActivity_m.class);
                }

                startActivity(intent);
            }
        });
        Signup_btn = findViewById(R.id.button_signup);
        Signup_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), signup_user.class);
                startActivity(intent);
            }
        });

        user_btn = findViewById(R.id.text_user);
        manage_btn = findViewById(R.id.text_manager);
        user_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user_btn.setTextColor(Color.parseColor("#1ab833"));
                manage_btn.setTextColor(Color.parseColor("#000000"));
                mode="user";
            }
        });
        manage_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manage_btn.setTextColor(Color.parseColor("#1ab833"));
                user_btn.setTextColor(Color.parseColor("#000000"));
                mode="manage";
            }
        });
    }
}