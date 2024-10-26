package com.example.myapp_230604;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DetailActivity extends AppCompatActivity {
    ImageView back_btn;
    TextView time, type;
    ImageView img;
    Button Re_recycle;
    FirebaseAuth auth; // FirebaseAuth 인스턴스 추가
    DatabaseReference databaseRef; // Realtime Database 참조 추가
    String recycleId; // 사용자 ID를 저장할 변수
    public Bitmap rotatedBitmap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Firebase 초기화
        auth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference();

        time = findViewById(R.id.time_text);
        type = findViewById(R.id.type_text);
        img = findViewById(R.id.user_img);
        recycleId = getIntent().getStringExtra("ID");
        Re_recycle = findViewById(R.id.button5);
        back_btn = findViewById(R.id.back_btn6);

        // 인텐트로부터 데이터 가져오기
        type.setText(getIntent().getStringExtra("RECYCLE_TYPE"));
        time.setText(getIntent().getStringExtra("RECYCLE_TIME"));
        String getUri = getIntent().getStringExtra("IMAGE_URI");

        // Bitmap을 가져오고 270도 회전
        Bitmap originalBitmap = BitmapFactory.decodeFile(getUri);
        if (originalBitmap != null) {
            Matrix matrix = new Matrix();
            matrix.postRotate(270);
            rotatedBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.getWidth(), originalBitmap.getHeight(), matrix, true);
            img.setImageBitmap(rotatedBitmap);
        } else {
            Toast.makeText(this, "이미지를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
        }

        img.setImageBitmap(rotatedBitmap);
        Toast.makeText(this, getUri, Toast.LENGTH_SHORT).show();

        // 뒤로 가기 버튼 클릭 리스너
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), mainActivity.class);
                startActivity(intent);
            }
        });

        // 재활용 처리 완료 버튼 클릭 리스너
        Re_recycle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateProcessStatus();
                updateManageStatus();
//                databaseRef.child(recycleId).removeValue();
            }
        });
    }

    // Realtime Database에서 'proc' 값을 '처리 완료'로 업데이트하는 메서드
    private void updateProcessStatus() {
        String userId = auth.getCurrentUser().getUid(); // 현재 사용자의 UID 가져오기

        // Realtime Database에서 경로 설정 후 'proc' 값을 '처리 완료'로 업데이트
        databaseRef.child("users").child(userId).child("recycle").child(recycleId).child("proc")
                .setValue("처리완료")
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(DetailActivity.this, "처리 완료로 업데이트되었습니다.", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(DetailActivity.this, "업데이트 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void updateManageStatus() {
        String userId = auth.getCurrentUser().getUid();
        databaseRef.child("users").child(userId).child("recycle").child(recycleId).child("uri")
                .get()
                .addOnSuccessListener(dataSnapshot -> {
                    if (dataSnapshot.exists()) {
                        String recycleUri = dataSnapshot.getValue(String.class);

                        // manage에서 해당 uri와 동일한 데이터를 찾아 proc 값을 '처리 완료'로 업데이트
                        databaseRef.child("users").child(userId).child("manage")
                                .orderByChild("uri").equalTo(recycleUri)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot manageSnapshot) {
                                        if (manageSnapshot.exists()) {
                                            for (DataSnapshot manageData : manageSnapshot.getChildren()) {
                                                manageData.getRef().child("process").setValue("처리완료")
                                                        .addOnSuccessListener(aVoid ->
                                                                Toast.makeText(DetailActivity.this, "manage의 proc이 처리 완료로 업데이트되었습니다.", Toast.LENGTH_SHORT).show()
                                                        )
                                                        .addOnFailureListener(e ->
                                                                Toast.makeText(DetailActivity.this, "manage 업데이트 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                                        );
                                            }
                                        } else {
                                            Toast.makeText(DetailActivity.this, "일치하는 manage 데이터가 없습니다.", Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Toast.makeText(DetailActivity.this, "데이터베이스 오류: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(DetailActivity.this, "recycle의 uri 값이 존재하지 않습니다.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(DetailActivity.this, "recycle 데이터 조회 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
