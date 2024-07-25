package com.example.myapp_230604;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class noticeFragment extends Fragment {
    RecyclerView userRV;
    NoticeAdapter adapter;
    EditText editTextSearch;

    private DatabaseReference databaseReference;
    private List<noticeData> postList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notice, container, false);

        editTextSearch = view.findViewById(R.id.editTextText3);
        userRV = view.findViewById(R.id.recyclerView_user);
        databaseReference = FirebaseDatabase.getInstance().getReference().child("posts");

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        userRV.setLayoutManager(layoutManager);

        adapter = new NoticeAdapter();
        userRV.setAdapter(adapter);

        loadPost();

        editTextSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || (event != null && event.getAction() == KeyEvent.ACTION_DOWN
                        && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    String searchText = editTextSearch.getText().toString().trim();
                    adapter.getFilter().filter(searchText);
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
            Log.e("noticeFragment", "DatabaseReference is null");
            return;
        }

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                adapter.resetItem();
                System.out.println("dataChanged");
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    noticeData post = postSnapshot.getValue(noticeData.class);
                    System.out.println("userItem");
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
