package com.example.myapp_230604;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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

import org.tensorflow.lite.support.label.Category;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
    ManagerAdapter Madapter;
    ImageView main_btn;

    private String recordPermission = Manifest.permission.RECORD_AUDIO;
    private int PERMISSION_CODE = 21;
    private AudioClassificationHelper audioHelper;

    private MediaRecorder mediaRecorder;
    private String audioFileName; // 오디오 녹음 생성 파일 이름
    private boolean isRecording = false;    // 현재 녹음 상태를 확인하기 위함.
    private Uri audioUri = null;

    private MediaPlayer mediaPlayer = null;
    private Boolean isPlaying = false;
    private TextView guideBtn;

    private DatabaseReference databaseReference;
    private DatabaseReference MdatabaseReference;

    private List<RecycleData> recycleList = new ArrayList<>();
    private FirebaseAuth mAuth;

    private CameraDevice cameraDevice;
    private ImageReader imageReader;
    private String frontCameraId; // 정면 카메라 ID
    private String imageFilePath; // 저장할 이미지 파일 경로
    private static final int CAMERA_PERMISSION_CODE = 100;

    public String recycleId,manageId;
    private String userEmail;

    private CameraCaptureSession cameraCaptureSession;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);

        // Initialize views
        recyclerView = view.findViewById(R.id.user_recyclerView);
        guideBtn = view.findViewById(R.id.guide_text);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        adapter = new UserAdapter();
        Madapter = new ManagerAdapter();
        recyclerView.setAdapter(adapter);
        main_btn = view.findViewById(R.id.mic_icon);
        mAuth = FirebaseAuth.getInstance();  // FirebaseAuth 초기화
        FirebaseUser currentUser = mAuth.getCurrentUser();

        mediaPlayer = MediaPlayer.create(getContext(), R.raw.alert_sound);

        // 현재 로그인한 사용자의 UID를 사용하여 데이터베이스 경로 설정
        if (currentUser != null) {
            String uid = currentUser.getUid();
            databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("recycle");
            MdatabaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("manage");

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
            public void onItemClickN(UserAdapter.Item item) {
                showDetailActivity(item);
            }
        });

        main_btn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onClick(View view) {
                if (isRecording) {
                    main_btn.setImageDrawable(getResources().getDrawable(R.drawable.user_mic));
                    isRecording = false;
                    guideBtn.setText("마이크를 눌러 분리배출을 시작");
                    audioHelper.stopAudioClassification();
                } else {
                    if (checkAudioPermission()) {
                        main_btn.setImageDrawable(getResources().getDrawable(R.drawable.user_stop));
                        isRecording = true;
                        guideBtn.setText("잘못된 분리배출 내역");
                        checkAudioPermission();
                        audioHelper.startAudioClassification();
                    }
                }
            }
        });

        // AudioClassificationHelper 초기화
        audioHelper = new AudioClassificationHelper(
                requireContext(),
                audioClassificationListener,
                AudioClassificationHelper.YAMNET_MODEL,
                AudioClassificationHelper.DISPLAY_THRESHOLD,
                AudioClassificationHelper.DEFAULT_OVERLAP_VALUE,
                AudioClassificationHelper.DEFAULT_NUM_OF_RESULTS,
                AudioClassificationHelper.DELEGATE_CPU,
                2, // numThreads
                adapter // UserAdapter
        );
        audioHelper.stopAudioClassification();
        findFrontCameraId();
        return view;
    }

    // AudioClassificationListener 구현 (Java 버전)
    private AudioClassificationListener audioClassificationListener = new AudioClassificationListener() {
        @Override
        public void onResult(final List<Category> results, final long inferenceTime) {
            requireActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for (Category category : results) {
                        String label = category.getLabel();
                        float score = category.getScore();
                        String currentTime = timeText.getText().toString();
                        String process = "처리필요";
                        FirebaseUser currentUser = mAuth.getCurrentUser();
                        userEmail = currentUser.getEmail();
                        if (RightRecycle(label)) {
                            label = label.substring(label.indexOf(" ") + 1);
                            takePicture();
                            Handler handler = new Handler();
                            String finalLabel = label;
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (mediaPlayer != null) {
                                        mediaPlayer.start(); // 소리 재생
                                    }
                                    putDatabase(imageFilePath, finalLabel, currentTime, process);
                                    adapter.addItem(new UserAdapter.Item(imageFilePath, finalLabel, currentTime, process, recycleId));
                                    putMDatabase(userEmail, currentTime, process, finalLabel,imageFilePath,manageId);
                                }
                            }, 500); // 딜레이 타임 조절
                        }
                    }
                }
            });
        }

        @Override
        public void onError(String message) {
            requireActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                }
            });
        }
    };

    private boolean RightRecycle(String label){
        int recycleType;
        // 1자리 또는 2자리 숫자 처
        if (Character.isDigit(label.charAt(1))) {
            recycleType = Integer.parseInt(label.substring(0, 2));  // 두 자리 숫자
        } else {
            recycleType = Integer.parseInt(label.substring(0, 1));  // 한 자리 숫자
        }
        if(recycleType % 5 == 1 || recycleType == 0){
            return false;
        }else{
            return true;
        }
    }

    private String getRecycleType(String label) {
        int recycleType;
        // 1자리 또는 2자리 숫자 처
        if (Character.isDigit(label.charAt(1))) {
            recycleType = Integer.parseInt(label.substring(0, 2));  // 두 자리 숫자
        } else {
            recycleType = Integer.parseInt(label.substring(0, 1));  // 한 자리 숫자
        }
        String[] trash = new String[]{"유리", "종이", "고철", "플라스틱"};
        switch (recycleType) {
            case 1: case 2: case 3: case 4:
                return trash[0];
            case 5: case 6: case 7: case 8:
                return trash[1];
            case 9: case 10: case 11: case 12:
                return trash[2];
            case 13: case 14: case 15: case 16:
                return trash[3];
            default:
                throw new IllegalStateException("Unexpected value: " + recycleType);
        }
    }


    private void showDetailActivity(UserAdapter.Item item) {
        Intent intent = new Intent(getContext(), DetailActivity.class);
        intent.putExtra("ID",item.id);
        intent.putExtra("RECYCLE_TIME", item.time);
        intent.putExtra("RECYCLE_TYPE", item.type);
        intent.putExtra("IMAGE_URI", item.uri);
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
                        adapter.addItem(new UserAdapter.Item(recycle.getUri(), recycle.getType(), recycle.getTime(), recycle.getProc(),recycle.getId()));
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

    private boolean checkAudioPermission() {
        if (ActivityCompat.checkSelfPermission(getContext(), recordPermission) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{recordPermission}, PERMISSION_CODE);
            return false;
        }
    }
    private void putDatabase(String uri, String type, String time, String proc) {
        recycleId = databaseReference.push().getKey();
        manageId = MdatabaseReference.push().getKey();
        if (databaseReference == null) {
            Log.e("userFragment", "DatabaseReference is null");
            return;
        }
        RecycleData recycle = new RecycleData(uri, type, time, proc, recycleId);
        databaseReference.child(recycleId).setValue(recycle);
    }

    private void putMDatabase(String user, String time, String proc, String type, String uri, String mid) {
        if (MdatabaseReference == null) {
            Log.e("userFragment", "DatabaseReference is null");
            return;
        }
        RecycleData_m recycle = new RecycleData_m(user,time,proc,type,uri,mid);
        MdatabaseReference.child(manageId).setValue(recycle);
    }
    private void findFrontCameraId() {
        CameraManager manager = (CameraManager) requireActivity().getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                int lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING);

                // 정면 카메라(LENS_FACING_FRONT)인 경우 cameraId 저장
                if (lensFacing == CameraCharacteristics.LENS_FACING_FRONT) {
                    frontCameraId = cameraId;
                    openCamera();  // 정면 카메라 열기
                    return;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    private void openCamera() {
        CameraManager manager = (CameraManager) requireActivity().getSystemService(Context.CAMERA_SERVICE);
        try {
            if (frontCameraId != null) {
                if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                manager.openCamera(frontCameraId, new CameraDevice.StateCallback() {
                    @Override
                    public void onOpened(@NonNull CameraDevice camera) {
                        cameraDevice = camera;
                        setupImageReader();
                        createCameraCaptureSession();
                    }

                    @Override
                    public void onDisconnected(@NonNull CameraDevice camera) {
                        camera.close();
                        cameraDevice = null;
                    }

                    @Override
                    public void onError(@NonNull CameraDevice camera, int error) {
                        camera.close();
                        cameraDevice = null;
                    }
                }, null);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    private void createCameraCaptureSession() {
        try {
            cameraDevice.createCaptureSession(Arrays.asList(imageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    cameraCaptureSession = session; // Save session reference
                    Log.d("Camera", "Camera session configured");
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Log.e("Camera", "카메라 세션 구성 실패");
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    private void setupImageReader() {
        imageReader = ImageReader.newInstance(640, 480, ImageFormat.JPEG, 1);
        imageReader.setOnImageAvailableListener(reader -> {
            Image image = reader.acquireNextImage();
            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            image.close();
            saveImageToFile(bytes);  // 파일로 저장
        }, null);
    }
    private void takePicture() {
        if (cameraDevice == null || cameraCaptureSession == null) return;

        // Check if camera permissions are granted
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
            return;
        }

        try {
            CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(imageReader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);

            cameraCaptureSession.capture(captureBuilder.build(), new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    Log.d("Camera", "사진 촬영 완료");
                    imageReader.acquireLatestImage();
                }
            }, null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void saveImageToFile(byte[] bytes) {
        // 현재 시간으로 고유한 파일 이름 생성
        String fileName = "captured_image_" + System.currentTimeMillis() + ".jpg";

        // 사진을 저장할 디렉토리 설정
        File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File file = new File(directory, fileName);

        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(bytes);
            imageFilePath = file.getAbsolutePath();  // 저장된 파일 경로 저장
            Log.d("Camera", "이미지 저장 완료: " + imageFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        checkAudioPermission();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (audioHelper != null) {
            audioHelper.stopAudioClassification(); // 오디오 분류 중지
            Log.d("AudioTF", "TFStop");
        }
    }
}
