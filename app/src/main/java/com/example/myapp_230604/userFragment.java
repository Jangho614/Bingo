package com.example.myapp_230604;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;

public class userFragment extends Fragment {

    TextView timeText;
    Handler handler;
    Runnable runnable;

    RecyclerView recyclerView;
    UserAdapter adapter;
    ImageView main_btn;

    private String recordPermission = Manifest.permission.RECORD_AUDIO;
    private int PERMISSION_CODE = 21;

    private MediaRecorder mediaRecorder;
    private String audioFileName; // 오디오 녹음 생성 파일 이름
    private boolean isRecording = false;    // 현재 녹음 상태를 확인하기 위함.
    private Uri audioUri = null;

    private MediaPlayer mediaPlayer = null;
    private Boolean isPlaying = false;
    ImageView playIcon;

    private DatabaseReference databaseReference;
    private List<RecycleData> recycleList = new ArrayList<>();
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);

        // Initialize views
        recyclerView = view.findViewById(R.id.user_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        adapter = new UserAdapter(recycleList);
        recyclerView.setAdapter(adapter);
        main_btn = view.findViewById(R.id.mic_icon);
        mAuth = FirebaseAuth.getInstance();  // FirebaseAuth 초기화
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // 현재 로그인한 사용자의 UID를 사용하여 데이터베이스 경로 설정
        if (currentUser != null) {
            String uid = currentUser.getUid();
            databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("recycle");
            loadRecycle();
        }
        timeText = view.findViewById(R.id.time_text);
        if (timeText == null) {
            Log.e("userFragment", "TextView is null");
        } else {
            handler = new Handler();
            runnable = new Runnable() {
                @Override
                public void run() {
                    updateTime();
                    handler.postDelayed(this, 1000);
                }
            };
            handler.post(runnable);
        }

        adapter.setOnItemClickListener(new UserAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(RecycleData item) {
                showDetailActivity(item);
            }
        });
        main_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isRecording) {
                    stopRecording();
                } else {
                    if(checkAudioPermission()) {
                        startRecording();
                    }
                }
            }
        });
        return view;
    }

    private void showDetailActivity(RecycleData item) {
        Intent intent = new Intent(getContext(), recycle_video.class);
        intent.putExtra("RECYCLE_TIME",item.getTime());
        intent.putExtra("RECYCLE_TYPE",item.getType());
        intent.putExtra("AUDIO_URI", item.getUri().toString());
        startActivity(intent);
    }
    private void loadRecycle() {
        if (databaseReference == null) {
            Log.e("userFragment", "DatabaseReference is null");
            return;
        }

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                recycleList.clear();
                adapter.resetItem(recycleList);
                Log.d("userFragment", "Data changed, loading new data");

                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    RecycleData recycle = postSnapshot.getValue(RecycleData.class);
                    if (recycle != null) {
                        recycleList.add(recycle);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("userFragment", "Database error: " + error.toException());
            }
        });
    }
    private void updateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
        String currentTime = sdf.format(new Date());
        timeText.setText(currentTime);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }
    //리사이클러 뷰
    private boolean checkAudioPermission() {
        if (ActivityCompat.checkSelfPermission(getContext(), recordPermission) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{recordPermission}, PERMISSION_CODE);
            return false;
        }
    }
    private void startRecording() {
        String recordPath = getContext().getExternalFilesDir("/").getAbsolutePath();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        audioFileName = recordPath + "/" +"RecordExample_" + timeStamp + "_"+"audio.mp4";

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFile(audioFileName);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecording = true;
            Toast.makeText(getContext(), "녹음 시작", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Stop recording and add it to RecyclerView
    private void stopRecording() {
        try {
            mediaRecorder.stop();
        } catch (RuntimeException e) {
            // Handle the exception if the recording has not been started
            e.printStackTrace();
        } finally {
            mediaRecorder.release();
            mediaRecorder = null;
            isRecording = false;
        }

        Toast.makeText(getContext(), "녹음 중지", Toast.LENGTH_SHORT).show();

        audioUri = Uri.parse(audioFileName);
        String recordedTime = (timeText != null) ? timeText.getText().toString() : "Unknown Time";

        // Create a new RecycleData object
        RecycleData newRecycleData = new RecycleData(audioUri.toString(), getRecycleType(), recordedTime);

        // Add to local list and update Firebase
        recycleList.add(newRecycleData);
        adapter.notifyDataSetChanged();


        saveRecycleDataToFirebase(newRecycleData);
    }

    private void saveRecycleDataToFirebase(RecycleData recycleData) {
        String key = databaseReference.push().getKey(); // Generate a new key for the new item
        if (key != null) {
            databaseReference.child(key).setValue(recycleData)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("userFragment", "RecycleData successfully saved to Firebase.");
                    })
                    .addOnFailureListener(e -> {
                        Log.e("userFragment", "Failed to save RecycleData: " + e.getMessage());
                    });
        } else {
            Log.e("userFragment", "Failed to generate key for RecycleData.");
        }
    }

    private String getRecycleType() {
        Random rand = new Random();
        String[] trash1 = new String[]{"플라스틱", "종이", "유리", "고철"};
        String[] trash2 = new String[]{"플라스틱", "종이", "유리", "고철"};

        int a, b;
        do {
            a = rand.nextInt(4);
            b = rand.nextInt(4);
        } while (a == b);

        String result = trash1[a] + " -> " + trash2[b];

        return result;
    }
}