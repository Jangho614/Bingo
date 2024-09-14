package com.example.myapp_230604;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
    private DatabaseReference databaseReference;
    private List<RecycleData> recycleList = new ArrayList<>();

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

        recyclerView = view.findViewById(R.id.user_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        adapter = new UserAdapter();
        recyclerView.setAdapter(adapter);
        databaseReference = FirebaseDatabase.getInstance().getReference().child("WrongRecycle");
        loadRecycle();

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

        Random rand = new Random();
        String[] trash = {"플라스틱", "유리", "캔", "종이"};
        int once = 0;
        int process;
        String prc;
        for (int i = 0; i < 6; i++) {
            int rnum1 = rand.nextInt(4);
            String put = trash[rnum1];
            process = rand.nextInt(2);

            int rnum2 = rand.nextInt(4);
            while (rnum1 == rnum2) {
                rnum2 = rand.nextInt(4);
            }
            String bin = trash[rnum2];

            String typeResult = String.format("%s -> %s", put, bin);
            String id = databaseReference.push().getKey();
            if (process == 1) {
                prc = "처리완료";
            } else {
                prc = "처리필요";
            }
            RecycleData data = new RecycleData(id, typeResult, "2024-07-25 19:28:30", prc);
            if (id != null) {
                databaseReference.child(id).setValue(data);
            }
        }

        adapter.notifyDataSetChanged();

        adapter.setOnItemClickListener(new UserAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(UserAdapter.Item item) {
                showDetailActivity(item);
            }
        });

        return view;
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
                System.out.println("dataChanged");
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    RecycleData recycle = postSnapshot.getValue(RecycleData.class);
                    System.out.println("Recycle");
                    if (recycle != null) {
                        recycleList.add(recycle);
                        adapter.addItem(new UserAdapter.Item(recycle.wrongType, recycle.wrongTime));
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("MainActivity", error.toException().toString());
            }
        });
    }
}
