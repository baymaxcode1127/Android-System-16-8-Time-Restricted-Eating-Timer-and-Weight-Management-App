package com.fasting.app;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 统计页面 — 显示轻断食成功天数和体重记录
 */
public class StatsFragment extends Fragment {

    private TextView tvSuccessDays;
    private TextView tvWeightSummary;
    private WeightChartView weightChart;
    private RecyclerView recyclerView;
    private TextView tvEmptyHint;
    private FloatingActionButton fabAddWeight;

    private WeightAdapter adapter;
    private WeightDatabaseHelper dbHelper;
    private FastingManager fastingManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stats, container, false);

        // 绑定视图
        tvSuccessDays = view.findViewById(R.id.tv_success_days);
        tvWeightSummary = view.findViewById(R.id.tv_weight_summary);
        weightChart = view.findViewById(R.id.weight_chart);
        recyclerView = view.findViewById(R.id.rv_weight_records);
        tvEmptyHint = view.findViewById(R.id.tv_empty_hint);
        fabAddWeight = view.findViewById(R.id.fab_add_weight);

        // 初始化
        dbHelper = WeightDatabaseHelper.getInstance(requireContext());
        fastingManager = FastingManager.getInstance(requireContext());

        // RecyclerView 设置
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new WeightAdapter();
        adapter.setOnItemLongClickListener(record -> showDeleteDialog(record));
        recyclerView.setAdapter(adapter);

        // 添加体重按钮
        fabAddWeight.setOnClickListener(v -> showAddWeightDialog());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshAll();
    }

    /** 刷新所有数据 */
    public void refreshAll() {
        // 成功天数
        int days = fastingManager.getCompletedCycles();
        tvSuccessDays.setText(String.valueOf(days));

        // 体重记录
        List<WeightDatabaseHelper.WeightRecord> records = dbHelper.getAllRecords();
        updateWeightDisplay(records);
    }

    private void updateWeightDisplay(List<WeightDatabaseHelper.WeightRecord> records) {
        if (records.isEmpty()) {
            tvEmptyHint.setVisibility(View.VISIBLE);
            weightChart.setVisibility(View.GONE);
            tvWeightSummary.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            adapter.setData(null);
        } else {
            tvEmptyHint.setVisibility(View.GONE);
            weightChart.setVisibility(View.VISIBLE);
            tvWeightSummary.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter.setData(records);

            // 更新摘要
            float firstWeight = records.get(0).weight;
            float latestWeight = records.get(records.size() - 1).weight;
            float change = latestWeight - firstWeight;
            String summary;
            if (Math.abs(change) < 0.05f) {
                summary = String.format(Locale.getDefault(),
                        "初始: %.1f kg | 最新: %.1f kg | 体重持平", firstWeight, latestWeight);
            } else if (change < 0) {
                summary = String.format(Locale.getDefault(),
                        "初始: %.1f kg | 最新: %.1f kg | 减重 %.1f kg 🎉",
                        firstWeight, latestWeight, Math.abs(change));
            } else {
                summary = String.format(Locale.getDefault(),
                        "初始: %.1f kg | 最新: %.1f kg | 增重 %.1f kg",
                        firstWeight, latestWeight, change);
            }
            tvWeightSummary.setText(summary);

            // 更新图表
            weightChart.setData(records);
        }
    }

    /** 显示添加体重对话框 */
    private void showAddWeightDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_weight, null);
        EditText etDate = dialogView.findViewById(R.id.et_date);
        EditText etWeight = dialogView.findViewById(R.id.et_weight);

        // 默认填写今天的日期
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        etDate.setText(sdf.format(new Date()));

        new AlertDialog.Builder(requireContext())
                .setTitle("记录体重")
                .setView(dialogView)
                .setPositiveButton("确认", (dialog, which) -> {
                    String dateStr = etDate.getText().toString().trim();
                    String weightStr = etWeight.getText().toString().trim();

                    if (dateStr.isEmpty() || weightStr.isEmpty()) {
                        Toast.makeText(requireContext(), "请填写完整信息", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 验证日期格式
                    if (!dateStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
                        Toast.makeText(requireContext(), "日期格式错误，请使用 yyyy-MM-dd",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        float weight = Float.parseFloat(weightStr);
                        if (weight <= 0 || weight > 500) {
                            Toast.makeText(requireContext(), "请输入合理的体重值",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        dbHelper.insertOrUpdateWeight(dateStr, weight);
                        refreshAll();
                        Toast.makeText(requireContext(), "体重记录已保存", Toast.LENGTH_SHORT).show();
                    } catch (NumberFormatException e) {
                        Toast.makeText(requireContext(), "体重格式错误", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    /** 显示删除确认对话框 */
    private void showDeleteDialog(WeightDatabaseHelper.WeightRecord record) {
        new AlertDialog.Builder(requireContext())
                .setTitle("删除记录")
                .setMessage("确定要删除 " + record.date + " 的体重记录吗？")
                .setPositiveButton("删除", (dialog, which) -> {
                    dbHelper.deleteRecord(record.date);
                    refreshAll();
                    Toast.makeText(requireContext(), "记录已删除", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("取消", null)
                .show();
    }
}
