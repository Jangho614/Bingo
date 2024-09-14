package com.example.myapp_230604;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.security.spec.ECField;
import java.util.regex.Pattern;

public class signup_user extends AppCompatActivity {

    Button Signup_btn;
    EditText id;
    EditText pw;
    EditText pw_chk;
    EditText phone;
    EditText birth;
    EditText add;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_user);

        id = findViewById(R.id.id);
        pw = findViewById(R.id.password);
        pw_chk = findViewById(R.id.chk_pw);
        phone = findViewById(R.id.phone);
        birth = findViewById(R.id.birth);
        add = findViewById(R.id.address);
        mAuth = FirebaseAuth.getInstance();

        Signup_btn = findViewById(R.id.signUp);
        Signup_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(SignupCheck(id.getText().toString(), pw.getText().toString(), pw_chk.getText().toString())){
                    createAccount();
                }
            }
        });
    }
    private void createAccount() {
        mAuth.createUserWithEmailAndPassword(id.getText().toString(), pw.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "CreateAccount : SUCCESS");
                            FirebaseUser user = mAuth.getCurrentUser();
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(id.getText().toString())
                                    .build();
                            if (user != null){
                                user.updateProfile(profileUpdates)
                                        .addOnCompleteListener(_task ->{
                                           if(_task.isSuccessful()){
                                               Log.d(TAG,"User profile updated");
                                           }
                                        });
                            }else{
                                Log.d(TAG, "User Not Found");
                            }
                            Intent intent = new Intent(getApplicationContext(), Login_User.class);
                            startActivity(intent);
                        }else{
                            Log.w(TAG,"CreateAccount : FAILURE",task.getException());
                            Toast.makeText(signup_user.this, "오류가 발생하였습니다.",Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }
    private boolean SignupCheck(String email, String pw, String pwChk){
        //이메일 검증
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        if(!Pattern.matches(emailRegex, email)){
            Toast.makeText(this, "이메일 형식이 올바르지 않습니다.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(pw.length() < 6){
            Toast.makeText(this, "비밀번호는 6자 이상이어야합니다.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(!pw.matches(pwChk)){
            Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}