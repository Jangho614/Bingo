package com.example.myapp_230604;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class noticeFragment_m extends Fragment {

    RecyclerView recyclerView;
    EditText editTextSearch;
    NoticeAdapter adapter;
    Button write_btn;
    Button search_btn;

    private DatabaseReference databaseReference;
    private List<noticeData> postList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notice_m, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        editTextSearch = view.findViewById(R.id.editTextSearch);
        write_btn = view.findViewById(R.id.buttonnn);
        search_btn = view.findViewById(R.id.search_button_n);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        adapter = new NoticeAdapter(); // Adapter 인스턴스 생성
        recyclerView.setAdapter(adapter);

        // Firebase Database 초기화
        databaseReference = FirebaseDatabase.getInstance().getReference().child("posts");

        write_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), board_writing.class);
                startActivity(intent);
            }
        });

        loadPost();

        // 검색
        editTextSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || (event != null && event.getAction() == KeyEvent.ACTION_DOWN
                        && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    String searchText = editTextSearch.getText().toString().trim();
                    adapter.getFilter().filter(searchText); // 어댑터에 검색어 전달하여 필터링
                    return true;
                }
                return false;
            }
        });
        adapter.setOnItemClickListener(new NoticeAdapter.OnItemClickListener() {
            @Override
            public void onItemClickN(NoticeAdapter.Item item) {
                showDetailActivity(item);
            }
        });

        return view;
    }

    private void showDetailActivity(NoticeAdapter.Item item) {
        Intent intent = new Intent(getContext(), noticeDetail.class);
        intent.putExtra(noticeDetail.EXTRA_TITLE, item.title);
        intent.putExtra(noticeDetail.EXTRA_CONTENT, item.contents);
        startActivity(intent);
    }
    private void loadPost() {
        if (databaseReference == null) {
            Log.e("noticeFragment_m", "DatabaseReference is null");
            return;
        }

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                adapter.resetItem();

                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    noticeData post = postSnapshot.getValue(noticeData.class);
                    System.out.println(post.title);
                    if (post != null) {
                        postList.add(post);
                        adapter.addItem(new NoticeAdapter.Item(post.title, post.content));
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
