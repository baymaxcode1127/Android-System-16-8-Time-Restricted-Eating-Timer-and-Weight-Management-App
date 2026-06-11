package com.fasting.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * 体重记录列表适配器
 */
public class WeightAdapter extends RecyclerView.Adapter<WeightAdapter.ViewHolder> {

    private final List<WeightDatabaseHelper.WeightRecord> records = new ArrayList<>();
    private Float firstWeight = null;
    private OnItemLongClickListener longClickListener;

    public interface OnItemLongClickListener {
        void onItemLongClick(WeightDatabaseHelper.WeightRecord record);
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    public void setData(List<WeightDatabaseHelper.WeightRecord> records) {
        this.records.clear();
        if (records != null) {
            this.records.addAll(records);
        }
        // 计算初始体重
        if (!this.records.isEmpty()) {
            this.firstWeight = this.records.get(0).weight;
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_weight, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WeightDatabaseHelper.WeightRecord record = records.get(position);
        holder.dateText.setText(record.date);
        holder.weightText.setText(String.format("%.1f kg", record.weight));

        // 与初始体重的变化
        if (firstWeight != null && position > 0) {
            float change = record.weight - firstWeight;
            String changeStr;
            int colorRes;
            if (Math.abs(change) < 0.05f) {
                changeStr = "持平";
                colorRes = R.color.text_secondary;
            } else if (change < 0) {
                changeStr = String.format("↓ %.1f kg", Math.abs(change));
                colorRes = R.color.eating_color; // 绿色，体重下降
            } else {
                changeStr = String.format("↑ %.1f kg", change);
                colorRes = R.color.fasting_color; // 橙色，体重上升
            }
            holder.changeText.setText(changeStr);
            holder.changeText.setTextColor(
                    holder.itemView.getContext().getResources().getColor(colorRes));
            holder.changeText.setVisibility(View.VISIBLE);
        } else if (position == 0) {
            holder.changeText.setText("初始体重");
            holder.changeText.setTextColor(
                    holder.itemView.getContext().getResources().getColor(R.color.text_secondary));
            holder.changeText.setVisibility(View.VISIBLE);
        }

        // 长按删除
        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onItemLongClick(record);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView dateText, weightText, changeText;

        ViewHolder(View itemView) {
            super(itemView);
            dateText = itemView.findViewById(R.id.tv_date);
            weightText = itemView.findViewById(R.id.tv_weight);
            changeText = itemView.findViewById(R.id.tv_change);
        }
    }
}
