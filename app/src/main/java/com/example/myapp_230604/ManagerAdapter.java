package com.example.myapp_230604;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ManagerAdapter extends RecyclerView.Adapter<ManagerAdapter.ViewHolder> implements Filterable {

    private ArrayList<Item> items;
    private ArrayList<Item> itemsFull;
    private OnItemClickListener listener;

    public ManagerAdapter() {
        this.items = new ArrayList<>();
        this.itemsFull = new ArrayList<>();
    }
    public interface OnItemClickListener{
        void onItemClickV(Item item);
    }
    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView id, time, processing;

        public ViewHolder(View view, final OnItemClickListener listener) {
            super(view);
            id = view.findViewById(R.id.idText);
            time = view.findViewById(R.id.timeText);
            processing = view.findViewById(R.id.prcText);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if(listener != null && position !=RecyclerView.NO_POSITION){
                        listener.onItemClickV((ManagerAdapter.Item) v.getTag());
                    }
                }
            });
        }

        public void setItem(Item item) {
            id.setText(item.id);
            time.setText(item.time);
            if(item.processing.equals("처리완료")) {
                processing.setTextColor(Color.rgb(60, 60, 255));
            }
            if(item.processing.equals("처리필요")){
                processing.setTextColor(Color.rgb(255, 60, 60));
            }
            processing.setText(item.processing);
        }

    }
    public void resetItem() {
        items = new ArrayList<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.manager_item, viewGroup, false);

        return new ViewHolder(view, listener);
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

    public void addItem(Item item) {
        items.add(item);
        itemsFull.add(item);
        notifyDataSetChanged();
    }

    public void delItem(Item item) {
        items.remove(item);
        itemsFull.remove(item);
        notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        return itemFilter;
    }

    private Filter itemFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Item> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(itemsFull);
            } else {
                String[] keywords = constraint.toString().toLowerCase().split("\\s+");

                for (Item item : itemsFull) {
                    boolean containsAllKeywords = true;
                    for (String keyword : keywords) {
                        if (!item.printItem().toLowerCase().contains(keyword)) {
                            containsAllKeywords = false;
                            break;
                        }
                    }
                    if (containsAllKeywords) {
                        filteredList.add(item);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            items.clear();
            items.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

    public static class Item {
        String id, time, processing, type;

        public Item(String id, String time, String processing,String type) {
            this.id = id;
            this.time = time;
            this.processing = processing;
            this.type = type;
        }

        public String printItem() {
            return id + ", " + time + ", " + processing;
        }
    }
}
