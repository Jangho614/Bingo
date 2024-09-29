package com.example.myapp_230604;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Objects;

// Modify your AudioAdapter class to open a new activity when an item is clicked
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private List<RecycleData> recycleList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(RecycleData item);
    }

    public UserAdapter(List<RecycleData> recycleList) {
        this.recycleList = recycleList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView title, wrongType, wrongTime;
        public View playIcon;

        public ViewHolder(View view, final OnItemClickListener listener) {
            super(view);
            title = view.findViewById(R.id.titleText);
            playIcon = view.findViewById(R.id.video_btn);
            wrongType = view.findViewById(R.id.wrongType_text);  // 설명 텍스트뷰
            wrongTime = view.findViewById(R.id.wrong_time);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (listener != null && position != RecyclerView.NO_POSITION) {
                        listener.onItemClick((RecycleData) v.getTag());
                    }
                }
            });
        }


        @SuppressLint("SetTextI18n")
        public void bind(RecycleData item) {
            Log.d("item",item.getType());
            Log.d("item",item.getTime());
            Log.d("item", Objects.requireNonNull(item.getUri().getLastPathSegment()));
            wrongType.setText( item.getType());  // 설명 텍스트 설정
            wrongTime.setText(item.getTime());
            title.setText("녹음 파일: " + item.getUri().getLastPathSegment());
            itemView.setTag(item);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false);
        return new ViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecycleData item = recycleList.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return recycleList.size();
    }
    public void resetItem(List<RecycleData> newData) {
        recycleList.clear();
        recycleList.addAll(newData);
        notifyDataSetChanged();
    }
}


