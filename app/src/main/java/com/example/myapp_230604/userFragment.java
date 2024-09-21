package com.example.myapp_230604;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
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

import org.tensorflow.lite.support.audio.TensorAudio;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.task.audio.classifier.AudioClassifier;
import org.tensorflow.lite.task.audio.classifier.Classifications;
import org.tensorflow.lite.task.core.BaseOptions;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class userFragment extends Fragment {

    TextView timeText;
    Handler handler;
    Runnable runnable;

    RecyclerView recyclerView;
    UserAdapter adapter;
    ImageView main_btn;
    private DatabaseReference databaseReference;
    private List<RecycleData> recycleList = new ArrayList<>();
    private FirebaseAuth mAuth;
    MappedByteBuffer modelFile = null; // Firebase Authentication
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200; // 권한 요청 코드
    private boolean permissionToRecordAccepted = false; // 권한 상태

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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);

        // Initialize views
        recyclerView = view.findViewById(R.id.user_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        adapter = new UserAdapter();
        recyclerView.setAdapter(adapter);
        main_btn = view.findViewById(R.id.mic_icon);
        mAuth = FirebaseAuth.getInstance();  // FirebaseAuth 초기화
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // 모델 파일 로드
        try {
            String modelPath = "model.tflite"; // assets 폴더 내의 파일 이름
            modelFile = FileUtil.loadMappedFile(getActivity(), modelPath); // 모델 파일을 로드
        } catch (IOException e) {
            Log.e("TAG", "Error loading model file: " + e.getMessage());
        }

        // 현재 로그인한 사용자의 UID를 사용하여 데이터베이스 경로 설정
        if (currentUser != null) {
            String uid = currentUser.getUid();
            databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("recycle");
            loadRecycle();  // 데이터 로드 메서드 호출

            timeText = view.findViewById(R.id.time_text);
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

        // Set up the button click listener
        main_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (permissionToRecordAccepted) {
                    try {
                        startAudioClassification();
                    } catch (IOException e) {
                        Log.e("TAG", "Error starting audio classification: " + e.getMessage());
                    }
                } else {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
                }
            }
        });

        adapter.setOnItemClickListener(new UserAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(UserAdapter.Item item) {
                showDetailActivity(item);
            }
        });

        return view;
    }

    private void startAudioClassification() throws IOException {
        Log.d("TAG", "Model Started");
        // Initialization
        AudioClassifier.AudioClassifierOptions options =
                AudioClassifier.AudioClassifierOptions.builder()
                        .setBaseOptions(BaseOptions.builder().useGpu().build())
                        .setMaxResults(1)
                        .build();
        AudioClassifier classifier =
                AudioClassifier.createFromFileAndOptions(getContext(), String.valueOf(modelFile), options);

        // Start recording
        int sampleRate = 16000; // 일반적으로 사용되는 샘플 레이트
        int channelConfig = AudioFormat.CHANNEL_IN_MONO;
        int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
        int bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
            return;
        }
        AudioRecord record = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, sampleRate, channelConfig, audioFormat, bufferSize);

        if (record.getState() == AudioRecord.STATE_INITIALIZED) {
            record.startRecording();
            Log.d("TAG", "Recording started");

            // Load latest audio samples
            TensorAudio audioTensor = classifier.createInputTensorAudio();
            audioTensor.load(record);

            // Run inference
            List<Classifications> results = classifier.classify(audioTensor);
            record.stop(); // Stop recording after classification
            record.release(); // Release resources
            if (results != null) {
                Log.d("TAG", results.toString());
                // Display the results (you could show a Toast or update a TextView)
                Toast.makeText(getContext(), results.toString(), Toast.LENGTH_SHORT).show();
            } else {
                Log.e("TAG", "Classification results are null.");
            }
        } else {
            Log.e("TAG", "AudioRecord initialization failed.");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            permissionToRecordAccepted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if (permissionToRecordAccepted) {
                try {
                    startAudioClassification(); // 권한이 허용되면 오디오 분류 시작
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                Log.e("TAG", "Audio recording permission denied.");
            }
        }
    }

    private void showDetailActivity(UserAdapter.Item item) {
        Intent intent = new Intent(getContext(), DetailActivity.class);
        intent.putExtra(DetailActivity.EXTRA_TYPE_RESULT, item.wrongtype);
        intent.putExtra(DetailActivity.EXTRA_TIMESTAMP, item.wrongtime);
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
                adapter.resetItem();
                Log.d("userFragment", "Data changed, loading new data");

                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    RecycleData recycle = postSnapshot.getValue(RecycleData.class);
                    if (recycle != null) {
                        recycleList.add(recycle);
                        adapter.addItem(new UserAdapter.Item(recycle.wrongType, recycle.wrongTime));
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
}
