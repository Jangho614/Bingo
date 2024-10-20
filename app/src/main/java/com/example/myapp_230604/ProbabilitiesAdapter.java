package com.example.myapp_230604;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.tensorflow.lite.support.label.Category;

import java.util.List;

class ProbabilitiesAdapter extends RecyclerView.Adapter<ProbabilitiesAdapter.ViewHolder> {
    private List<Category> categoryList; // 카테고리 리스트

    public ProbabilitiesAdapter(List<Category> categoryList) {
        this.categoryList = categoryList; // 카테고리 리스트 초기화
    }

    // 카테고리 목록 설정 메소드
    public void setCategoryList(List<Category> categories) {
        this.categoryList = categories; // 카테고리 리스트 업데이트
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_probability, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category category = categoryList.get(position);
        holder.bind(category.getLabel(), category.getScore(), category.getIndex());
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    // ViewHolder 클래스 정의
    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView labelTextView;
        private ProgressBar progressBar;
        private int[] primaryProgressColorList;
        private int[] backgroundProgressColorList;

        public ViewHolder(View itemView) {
            super(itemView);
            labelTextView = itemView.findViewById(R.id.label_text_view);
            progressBar = itemView.findViewById(R.id.progress_bar);
            Context context = itemView.getContext();
            primaryProgressColorList = context.getResources().getIntArray(R.array.colors_progress_primary);
            backgroundProgressColorList = context.getResources().getIntArray(R.array.colors_progress_background);
        }

        public void bind(String label, float score, int index) {
            labelTextView.setText(label);
            progressBar.setProgressBackgroundTintList(ColorStateList.valueOf(
                    backgroundProgressColorList[index % backgroundProgressColorList.length]));
            progressBar.setProgressTintList(ColorStateList.valueOf(
                    primaryProgressColorList[index % primaryProgressColorList.length]));
            int newValue = (int) (score * 100);
            progressBar.setProgress(newValue);
        }
    }
}
