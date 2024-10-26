package com.example.myapp_230604;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DetailActivity_m extends AppCompatActivity {
    Button rmBtn;
    String wasteType1;
    String wasteType2;

    public static final String EXTRA_ID = "id";
    public static final String EXTRA_MID = "mid";
    public static final String EXTRA_TIME = "time";
    public static final String EXTRA_PROCESS = "process";
    public static final String EXTRA_TYPE = "TYPE";
    public static final String EXTRA_URI = "URI";

    DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_m);

        Intent intent = getIntent();
        String id = intent.getStringExtra(EXTRA_ID);
        String mid = intent.getStringExtra(EXTRA_MID);
        String time = intent.getStringExtra(EXTRA_TIME);
        String process = intent.getStringExtra(EXTRA_PROCESS);
        String type = intent.getStringExtra(EXTRA_TYPE);
        String uri = intent.getStringExtra(EXTRA_URI);

        String userId = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        // wasteType1과 wasteType2 추출
        String[] types = type.replaceAll("[0-9]", "").split("->");
        if (types.length == 2) {
            wasteType1 = types[0].trim();
            wasteType2 = types[1].trim();
        }

        rmBtn = findViewById(R.id.videoRM_btn);
        rmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (id != null) {
//                    databaseRef.child(mid).removeValue();
                    finish();
                }
            }
        });

        ImageView backBtn = findViewById(R.id.backbtn);
        ImageView img = findViewById(R.id.imageView);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Bitmap originalBitmap = BitmapFactory.decodeFile(uri);
        Matrix matrix = new Matrix();
        matrix.postRotate(270);
        Bitmap rotatedBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.getWidth(), originalBitmap.getHeight(), matrix, true);
        img.setImageBitmap(rotatedBitmap);

        TextView result;
        result = findViewById(R.id.result_text);
        result.setText("잘못 분리수거 경고 및 재분리수거 지시\n\n"
                + "수신자: " + userId + " (아이디: " + userId + ")\n"
                + "발신자: [OO아파트 관리사무소]\n"
                + "발신일: " + time + "\n\n"
                + "제목: 잘못된 분리수거에 대한 경고 및 재분리수거 지시\n\n"
                + userId + "님께,\n\n"
                + "안녕하세요,\n\n"
                + time + "에 " + wasteType1 + "을(를) " + wasteType2 + " 쓰레기통에 잘못 분리수거한 사실이 확인되었습니다. "
                + "올바른 분리수거는 환경 보호와 자원 재활용을 위해 매우 중요합니다. 따라서 잘못된 분리수거는 즉시 수정되어야 합니다.\n\n"
                + "다음 사항을 참고하여 잘못된 분리수거를 수정해 주시기 바랍니다:\n\n"
                + wasteType1 + ": " + wasteType1 + "은(는) 지정된 " + wasteType1 + " 쓰레기통에 버려야 합니다.\n"
                + wasteType2 + ": " + wasteType2 + "은(는) 지정된 " + wasteType2 + " 쓰레기통에 버려야 합니다.\n\n"
                + "이에 따라, 현재 " + wasteType2 + " 쓰레기통에 버린 " + wasteType1 + "을(를) 제거하고 지정된 "
                + wasteType1 + " 쓰레기통에 재분리수거해 주시기 바랍니다.\n\n"
                + "만약 잘못된 분리수거가 반복될 경우, 추가적인 조치가 취해질 수 있습니다. 앞으로는 올바른 분리수거 방법을 준수하여 "
                + "환경 보호에 기여해 주시기를 부탁드립니다.\n\n"
                + "궁금한 사항이나 도움이 필요하신 경우, 언제든지 저희에게 연락해 주십시오.\n\n"
                + "감사합니다.\n\n"
                + "[OO아파트 관리사무소]\n\n"
                + "[010-1234-5678]\n\n"
                + "이 문서는 " + userId + "님께 올바른 분리수거를 안내하기 위한 것입니다. "
                + "올바른 분리수거를 통해 우리의 환경을 보호하는 데 함께해 주시기를 부탁드립니다.");
    }
}
