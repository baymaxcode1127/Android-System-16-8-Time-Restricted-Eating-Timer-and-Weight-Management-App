# 16:8 轻断食助手 · Intermittent Fasting Timer

[![Android](https://img.shields.io/badge/Android-7.0%2B-green)](https://developer.android.com)
[![Language](https://img.shields.io/badge/Language-Java-orange)](https://www.java.com)

一个简洁优雅的 Android 应用，帮助你践行 **16:8 间歇性断食法**——每天 16 小时断食 + 8 小时进食窗口，自动循环计时，并记录体重变化趋势。

A simple and elegant Android app to help you practice **16:8 intermittent fasting** — 16h fasting + 8h eating window daily, with automatic cycle timer and weight tracking.

---

## 📱 截图预览 · Screenshots

| 计时页 · Timer | 统计页 · Stats |
|:---:|:---:|
| 圆形倒计时 · Circular Countdown | 体重趋势图 · Weight Chart |

---

## 📥 下载安装 · Download

👉 **[点击下载 APK · Download APK](./16-8-fasting-app.apk)**

直接传到手机安装即可。首次安装需在手机设置中允许「安装未知来源应用」。

Transfer to your phone and install. You may need to allow "Install unknown apps" in your phone settings.

---

## ✨ 功能特性 · Features

### ⏱️ 断食计时 · Fasting Timer
- **圆形倒计时** —— 大圆形进度环，剩余时间一目了然
- **颜色区分** —— 🟠 橙色 = 断食中（16小时）｜ 🟢 绿色 = 进食窗口（8小时）
- **自动循环** —— 断食结束自动切换进食，进食结束自动开始新断食，无需手动操作
- **一键重置** —— 随时恢复到初始状态

- **Circular countdown** — Large circular progress ring, remaining time at a glance
- **Color coded** — 🟠 Orange = Fasting (16h) ｜ 🟢 Green = Eating window (8h)
- **Auto cycling** — Automatically switches between fasting and eating, hands-free
- **One-tap reset** — Return to initial state anytime

### 📊 数据统计 · Statistics
- **成功天数** —— 每完成一轮 16h+8h 计为一天，累计显示
- **体重记录** —— 按日期记录体重，自动计算与初始体重的变化
- **可视化图表** —— 自定义折线图，直观展示体重趋势，初始体重参考线
- **长按删除** —— 长按任意记录可删除

- **Success days** — Each complete 16h+8h cycle counts as one day
- **Weight logging** — Record weight by date, auto-calculate changes from initial weight
- **Visual chart** — Custom line chart with initial weight reference line
- **Long-press to delete** — Remove any record with a long press

### 🔔 智能通知 · Smart Notifications
- 断食结束 → 📢 「断食结束！可以开始进食了」
- 进食结束 → 📢 「进食窗口结束！请开始新的断食」
- 设备重启后自动恢复闹钟

- Fasting ends → 📢 "Fasting complete, you can eat now!"
- Eating ends → 📢 "Eating window closed, start fasting!"
- Alarms auto-restore after device reboot

---

## 🛠️ 技术栈 · Tech Stack

| 技术 · Technology | 说明 · Description |
|:---|:---|
| **Language** | Java 8 |
| **UI Framework** | Material Design Components |
| **Navigation** | ViewPager2 + TabLayout |
| **Custom Views** | CircularProgressView（圆形进度）, WeightChartView（体重图表） |
| **Database** | SQLite (SQLiteOpenHelper) |
| **Persistence** | SharedPreferences |
| **Notifications** | NotificationCompat + AlarmManager |
| **Min SDK** | API 24 (Android 7.0) |
| **Target SDK** | API 34 (Android 14) |

---

## 🏗️ 项目结构 · Project Structure

```
IntermittentFasting/
├── app/src/main/java/com/fasting/app/
│   ├── MainActivity.java              # 主 Activity
│   ├── TimerFragment.java             # 计时页面（断食/进食）
│   ├── StatsFragment.java             # 统计页面（天数 + 体重）
│   ├── FastingManager.java            # 核心状态管理（单例）
│   ├── CircularProgressView.java      # 圆形倒计时自定义 View
│   ├── WeightChartView.java           # 体重折线图自定义 View
│   ├── WeightDatabaseHelper.java      # SQLite 数据库
│   ├── WeightAdapter.java             # 体重列表适配器
│   ├── NotificationHelper.java        # 通知 + 闹钟管理
│   ├── FastingAlarmReceiver.java      # 闹钟广播接收器
│   ├── BootReceiver.java              # 开机恢复广播
│   └── ViewPagerAdapter.java          # 页面适配器
├── app/src/main/res/
│   ├── layout/                        # 布局 XML
│   ├── values/                        # 字符串、颜色、主题
│   └── drawable/                      # 图标资源
└── 16-8-fasting-app.apk               # 👈 直接下载安装
```

---

## 🔨 本地构建 · Build Locally

```bash
# 1. 安装 Android SDK（或将 local.properties 中的 sdk.dir 指向你的 SDK）
# 2. 构建
./gradlew assembleDebug

# APK 输出路径：
# app/build/outputs/apk/debug/app-debug.apk
```

需要 JDK 8+ 和 Android SDK 34。

Requires JDK 8+ and Android SDK 34.

---

## 📄 许可证 · License

MIT License — 自由使用、修改和分发。
