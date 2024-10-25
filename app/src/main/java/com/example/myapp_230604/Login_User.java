package com.example.myapp_230604;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;


public class Login_User extends AppCompatActivity {
    Button login_btn;
    Button Signup_btn;
    TextView user_btn;
    TextView manage_btn;

    EditText id;
    EditText pw;
    String mode = "user";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_user);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            if (mode == "user") {
                startActivity(new Intent(this, mainActivity.class));
                finish();
            } else {
                startActivity(new Intent(this, mainActivity_m.class));
                finish();
            }
        }

        user_btn = findViewById(R.id.text_user);
        manage_btn = findViewById(R.id.text_manager);
        id = findViewById(R.id.input_id);
        pw = findViewById(R.id.input_pw);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        user_btn.setTextColor(Color.parseColor("#1ab833"));
        manage_btn.setTextColor(Color.parseColor("#000000"));

        login_btn = findViewById(R.id.button_login);
        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
        Signup_btn = findViewById(R.id.button_signup);
        Signup_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                if (mode == "user") {
                    intent = new Intent(getApplicationContext(), signup_user.class);
                } else {
                    intent = new Intent(getApplicationContext(), signup_manager.class);
                }
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
                mode = "user";
            }
        });
        manage_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manage_btn.setTextColor(Color.parseColor("#1ab833"));
                user_btn.setTextColor(Color.parseColor("#000000"));
                mode = "manage";
            }
        });
    }

    private void login() {
        mAuth.signInWithEmailAndPassword(id.getText().toString(), pw.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            Intent intent;
                            if (mode.equals("user")) {
                                intent = new Intent(getApplicationContext(), mainActivity.class);
                            } else {
                                intent = new Intent(getApplicationContext(), mainActivity_m.class);
                            }
                            startActivity(intent);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(Login_User.this, "이메일 혹은 비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}