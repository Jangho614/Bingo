package com.example.myapp_230604;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private ArrayList<UserAdapter.Item> items = new ArrayList<>();
    private UserAdapter.OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClickN(UserAdapter.Item item);
    }
    public void setOnItemClickListener(UserAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.user_item, viewGroup, false);
        return new ViewHolder(view,listener);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        Item item = items.get(position);
        viewHolder.setItem(item);
        viewHolder.itemView.setTag(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void addItem(UserAdapter.Item item) {
        items.add(item);
        notifyDataSetChanged();
    }

    public void resetItem() {
        items = new ArrayList<>();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView type,time;

        public ViewHolder(View view, final OnItemClickListener listener) {
            super(view);
            type = view.findViewById(R.id.wrongType_text);
            time = view.findViewById(R.id.wrong_time);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (listener != null && position != RecyclerView.NO_POSITION) {
                        listener.onItemClickN((UserAdapter.Item) view.getTag());
                    }
                }
            });
        }
        public void setItem(Item item) {
            type.setText(item.type);
            time.setText(item.time);
        }
    }
    public static class Item {
        String uri,type,time,proc,id;

        public Item(String uri, String type, String time,String proc, String id){
            this.uri = uri;
            this.type = type;
            this.time = time;
            this.proc = proc;
            this.id = id;
        }
    }
}
