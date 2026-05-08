# FitCore - AI 驱动的 Android 健身追踪应用

> **100% AI 生成** | 22 个 Java 源文件 | 5 大功能模块 | 完全本地 SQLite 存储

FitCore 是一款轻量级、隐私优先的 Android 健身记录应用，全程由 AI 编程助手（Claude Code + Cursor + Codex）从零构建。不上传、不追踪、无广告——你的数据全部留在手机本地。

---

## 功能特性

### 首页仪表盘
- 根据时段自动问候（早上好 / 下午好 / 晚上好）
- 当前健身计划卡片，点击可切换计划
- 运动次数、累计时长、连续打卡天数统计
- 每日目标进度可视化

### 运动记录
- 100+ 种运动项目，覆盖 5 大类别：有氧、力量、柔韧、球类、其他
- 内置运动计时器，支持开始/暂停/恢复
- 基于 MET 值估算卡路里消耗
- 体感评分（1-5 星）
- 保存前展示详细运动摘要确认弹窗

### 数据分析
- 总运动次数和累计时长概览
- 7 日打卡热力图
- MPAndroidChart 柱状图展示每周运动时长趋势
- 支持周切换和日期选择器

### 智能提醒
- 自定义每日提醒时间
- 单次提醒 / 按星期选择（周一至周日）
- 一键测试通知
- 内置 22 条中文励志语录

### 个人中心与指南
- 头像自定义
- 编辑资料（性别、年龄、身高、体重）
- BMI 计算器与分级参考标准
- 热量消耗百科参考

---

## 技术栈

| 层级 | 技术 |
|------|------|
| 语言 | Java 17 |
| 最低 SDK | 24 (Android 7.0) |
| 目标 SDK | 36 |
| UI | Material Design Components, ConstraintLayout |
| 导航 | BottomNavigationView（5 个标签页） |
| 图表 | MPAndroidChart v3.1.0 |
| 数据库 | SQLite + SQLiteOpenHelper |
| 加密 | SHA-256 密码哈希 |
| 会话 | SharedPreferences |
| 通知 | NotificationManager + BroadcastReceiver |

---

## 项目结构

```
app/src/main/java/com/example/fitcore/
├── LoginActivity.java           # 登录/注册页
├── MainActivity.java            # 主页容器（5 个标签页）
├── activity/
│   ├── AccountInfoActivity.java     # 账号信息
│   ├── BMIGuideActivity.java        # BMI 指南
│   ├── CalorieGuideActivity.java    # 热量指南
│   ├── EditProfileActivity.java     # 编辑资料
│   ├── PlanListActivity.java        # 健身计划列表
│   ├── ReminderSettingsActivity.java # 提醒设置
│   ├── WorkoutDetailActivity.java   # 运动详情
│   ├── WorkoutHistoryActivity.java  # 历史记录
│   └── WorkoutTimerActivity.java    # 运动计时器
├── fragment/
│   ├── HomeFragment.java        # 首页
│   ├── RecordFragment.java      # 运动记录
│   ├── AnalyticsFragment.java   # 数据分析
│   ├── ReminderFragment.java    # 提醒设置
│   └── ProfileFragment.java     # 个人中心
├── adapter/
│   ├── PlanAdapter.java         # 计划列表适配器
│   └── WorkoutAdapter.java      # 运动记录适配器
├── model/
│   ├── User.java                # 用户模型
│   ├── FitnessPlan.java         # 健身计划模型
│   ├── WorkoutRecord.java       # 运动记录模型
│   └── ReminderSettings.java    # 提醒设置模型
├── database/
│   └── DatabaseHelper.java      # 数据库（5 张表，v2 迁移）
└── utils/
    ├── SessionManager.java      # 会话管理
    └── NotificationHelper.java  # 通知辅助
```

---

## 数据库设计

| 表名 | 说明 |
|------|------|
| `users` | 用户账户（用户名、密码哈希、个人资料） |
| `fitness_plans` | 4 个预设计划（推拉腿分化、HIIT、柔韧训练、耐力跑） |
| `user_plans` | 用户计划选择与进度追踪 |
| `workout_records` | 运动类型、时长、体感、卡路里、时间戳 |
| `reminder_settings` | 时间、模式（每日/自定义）、生效星期 |

---

## 构建运行

```bash
git clone https://github.com/MouriShinichi/FitCore.git
cd FitCore
./gradlew assembleDebug
```

使用 Android Studio Hedgehog (2023.1.1) 或更高版本打开即可。

---

## AI 开发历程

本项目是 AI 驱动移动端全栈开发的实践验证：

- **Claude Code**：负责整体架构设计、数据库 Schema、复杂逻辑实现（MET 估算、图表渲染、提醒调度）
- **Codex**：辅助 UI 布局生成和 Material Design 组件适配
- **Cursor**：代码审查、Bug 修复和细节打磨

三个 AI Agent 协同工作，**3 天内完成从需求到完整交付**，共产出 141 个文件、9346 行代码。

---

## 许可证

MIT License — 随意使用，无需署名的自由软件许可。

---

*由 Claude Code、Cursor 和 Codex 联合构建。作为 Xiaomi MiMo Orbit 创造者激励计划的申请项目提交。*
