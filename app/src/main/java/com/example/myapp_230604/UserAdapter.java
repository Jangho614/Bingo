package com.example.myapp_230604;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder>{

    public ArrayList<Item> items = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Item item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public TextView wrongtype, wrongtime;
        public Button videobtn;

        public ViewHolder(View view, final OnItemClickListener listener){
            super(view);
            wrongtime = view.findViewById(R.id.wrong_time);
            wrongtype = view.findViewById(R.id.wrongType_text);
            videobtn = view.findViewById(R.id.video_btn);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (listener != null && position != RecyclerView.NO_POSITION) {
                        listener.onItemClick((Item) v.getTag());
                    }
                }
            });
        }

        public void setItem(Item item){
            wrongtype.setText(item.wrongtype);
            wrongtime.setText(item.wrongtime);
            itemView.setTag(item);
        }
    }
    public void resetItem() {
        items = new ArrayList<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.user_item, viewGroup, false);

        return new ViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        Item item = items.get(position);
        viewHolder.setItem(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void addItem(Item item){
        items.add(item);
    }

    public static class Item{
        String wrongtime, wrongtype;
        public Item(String wrongtype, String wrongtime){
            this.wrongtype = wrongtype;
            this.wrongtime = wrongtime;
        }
    }
}
