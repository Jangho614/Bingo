package com.example.myapp_230604;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
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

import java.util.ArrayList;
import java.util.List;

public class ManagerFragment extends Fragment {

    RecyclerView recyclerView;
    EditText editTextSearch;
    public static ManagerAdapter adapter;

    CheckBox NeedBox;
    CheckBox CompleteBox;
    ImageView imageViewSearch;
    String[] checkSelect = {"", "처리완료", "처리필요"};
    String searchText = "";
    int idx;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private List<RecycleData_m> recycleList = new ArrayList<RecycleData_m>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manager, container, false);
        idx = 0;
        recyclerView = view.findViewById(R.id.video_recycler);
        editTextSearch = view.findViewById(R.id.search_video);
        imageViewSearch = view.findViewById(R.id.imageView2);
        NeedBox = view.findViewById(R.id.NeedCheck);
        CompleteBox = view.findViewById(R.id.CompleteCheck);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String uid = currentUser.getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("manage");


        loadRecycle();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new ManagerAdapter(); // Adapter 인스턴스 생성
        recyclerView.setAdapter(adapter);

        imageViewSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchText = editTextSearch.getText().toString().trim();
                adapter.getFilter().filter(checkSelect[idx] + " " + searchText); // 어댑터에 검색어 전달하여 필터링
            }
        });
        NeedBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    CompleteBox.setChecked(false);
                    adapter.getFilter().filter(searchText + " 처리필요");
                } else {
                    adapter.getFilter().filter(searchText);
                }
            }
        });

        CompleteBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    NeedBox.setChecked(false);
                    adapter.getFilter().filter(searchText + " 처리완료");
                } else {
                    adapter.getFilter().filter(searchText);
                }
            }
        });

        adapter.notifyDataSetChanged();

        adapter.setOnItemClickListener(new ManagerAdapter.OnItemClickListener() {
            @Override
            public void onItemClickV(ManagerAdapter.Item item) {
                showDetailActivitym(item);
            }
        });
        return view;
    }
    private void showDetailActivitym(ManagerAdapter.Item item) {
        Intent intent = new Intent(getContext(), DetailActivity_m.class);
        intent.putExtra(DetailActivity_m.EXTRA_ID, item.id);
        intent.putExtra(DetailActivity_m.EXTRA_MID, item.mid);
        intent.putExtra(DetailActivity_m.EXTRA_TIME, item.time);
        intent.putExtra(DetailActivity_m.EXTRA_PROCESS, item.processing);
        intent.putExtra(DetailActivity_m.EXTRA_TYPE, item.type);
        intent.putExtra(DetailActivity_m.EXTRA_URI, item.uri);
        startActivity(intent);
    }
    private void loadRecycle() {
        if (databaseReference == null) {
            Log.e("VideoFragment", "DatabaseReference is null");
            return;
        }

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                recycleList.clear();
                adapter.resetItem();
                System.out.println("dataChanged");
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    RecycleData_m recycle = postSnapshot.getValue(RecycleData_m.class);
                    System.out.println("Recycle");
                    if (recycle != null) {
                        recycleList.add(recycle);
                        adapter.addItem(new ManagerAdapter.Item(recycle.id,recycle.wrongTime,recycle.process,recycle.wrongType,recycle.uri,recycle.mid));
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

