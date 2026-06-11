package com.fasting.app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

/**
 * 主 Activity — 使用 ViewPager2 + TabLayout 管理两个页面
 */
public class MainActivity extends AppCompatActivity {

    private static final int NOTIFICATION_PERMISSION_REQUEST = 100;

    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private TimerFragment timerFragment;
    private StatsFragment statsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 创建通知渠道
        NotificationHelper.createChannel(this);

        // 请求通知权限 (Android 13+)
        requestNotificationPermission();

        // 初始化视图
        viewPager = findViewById(R.id.view_pager);
        tabLayout = findViewById(R.id.tab_layout);

        // 创建 Fragment
        timerFragment = new TimerFragment();
        statsFragment = new StatsFragment();

        // 设置 ViewPager2 适配器
        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        adapter.addFragment(timerFragment, getString(R.string.tab_timer));
        adapter.addFragment(statsFragment, getString(R.string.tab_stats));
        viewPager.setAdapter(adapter);

        // 绑定 TabLayout
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(position == 0
                    ? getString(R.string.tab_timer)
                    : getString(R.string.tab_stats));
        }).attach();
    }

    /** 当断食/进食周期完成时，通知统计页面刷新 */
    public void onCycleCompleted() {
        if (statsFragment != null) {
            statsFragment.refreshAll();
        }
    }

    /** 请求通知权限（Android 13+） */
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_REQUEST
                );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST) {
            // 用户拒绝也继续运行，不影响核心功能
        }
    }
}
