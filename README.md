# 应用隐藏大师 (AppHider)

一款帮助用户在 Android 手机上隐藏指定应用图标、保护隐私的工具类 APP。

## 功能说明

- **应用隐藏**：隐藏社交、金融、游戏等不希望他人看到的第三方应用，图标从桌面消失
- **伪装界面**：APP 主界面伪装成计算器，不暴露真实用途
- **三种入口**：
  - 伪装计算器中输入密码按 `=` 进入隐藏空间
  - 连续点击计算器标题栏 5 次，弹出密码输入框
  - 系统拨号盘输入 `*#*#1234#*#*`（需设备支持）
- **隐藏空间**：深色主题网格界面，显示所有已隐藏应用，点击即可启动
- **密码保护**：4-6 位数字密码，Android Keystore 加密存储
- **安全机制**：连续 5 次输错密码锁定 30 秒，支持指纹解锁
- **设置功能**：修改密码、切换伪装主题、管理隐藏应用

## 使用方法

### 首次使用
1. 安装后打开 APP，显示为计算器界面
2. 首次使用需设置 4-6 位数字密码
3. 设置完成后进入计算器伪装界面

### 隐藏应用
1. 进入隐藏空间（通过密码或 5 次点击标题）
2. 点击右下角 "+" 按钮
3. 在应用列表中选择要隐藏的应用（支持搜索、多选）
4. 点击底部"隐藏选中"按钮

### 启动隐藏应用
1. 进入隐藏空间
2. 点击应用图标即可启动

### 取消隐藏
1. 在隐藏空间长按应用图标或通过设置页管理
2. 应用图标将恢复显示在桌面

## 隐藏机制原理解析

### Activity Alias 动态启停

本 APP 采用 Android 的 `activity-alias`（活动别名）机制实现应用图标隐藏：

1. **预注册别名槽位**：在 AndroidManifest.xml 中预注册 20 个 `activity-alias`，每个 alias 都声明了 `MAIN`/`LAUNCHER` intent-filter，使其可以作为独立的桌面图标存在。

2. **动态启停控制**：使用 `PackageManager.setComponentEnabledSetting()` 方法控制每个 alias 的启用状态：
   - 启用状态（`COMPONENT_ENABLED_STATE_ENABLED`）：alias 在桌面显示图标
   - 禁用状态（`COMPONENT_ENABLED_STATE_DISABLED`）：alias 从桌面消失

3. **隐藏流程**：
   - 将目标应用包名与某个 alias 槽位关联
   - 禁用该 alias → 图标从桌面消失
   - 记录关联关系到 Room 数据库持久化

4. **取消隐藏流程**：
   - 从数据库读取关联的 alias 槽位
   - 重新启用该 alias → 图标恢复

5. **持久化**：`PackageManager.setComponentEnabledSetting()` 的设置在设备重启后仍然保持，APP 在 `BOOT_COMPLETED` 广播接收器中验证状态一致性。

### 为什么选择 Activity Alias 方案

- **无需 root 权限**：相比于修改系统文件或使用 Xposed 框架
- **无需设备管理员权限**：相比于使用 `disableComponent()` 禁止目标应用自身
- **效果可靠**：alias 被禁用后，在所有支持 Activity Alias 的 Android 版本上都能隐藏图标
- **可逆性**：随时可以重新启用，不影响应用数据

## 技术架构

```
AppHider/
├── app/
│   └── src/main/java/com/apphider/
│       ├── AppHiderApp.kt          # Application 类 (Hilt 入口)
│       ├── MainActivity.kt         # 单 Activity 入口
│       ├── LauncherActivity.kt     # 透明目标 Activity
│       ├── DialerEntryActivity.kt  # 拨号入口
│       ├── di/                     # 依赖注入 (Hilt)
│       ├── data/                   # 数据层
│       │   ├── local/
│       │   │   ├── db/             # Room 数据库
│       │   │   ├── datastore/      # DataStore 偏好设置
│       │   │   └── crypt/          # Keystore 加密
│       │   ├── repository/         # 仓库实现
│       │   └── model/              # 数据模型
│       ├── domain/                 # 领域层
│       │   ├── repository/         # 仓库接口
│       │   ├── usecase/            # 用例
│       │   └── model/              # 领域模型
│       ├── ui/                     # UI 层 (Compose)
│       │   ├── theme/              # 主题/颜色/排版
│       │   ├── navigation/         # 导航图
│       │   ├── calculator/         # 计算器伪装界面
│       │   ├── hidden/             # 隐藏空间
│       │   ├── applist/            # 应用列表选择
│       │   ├── settings/           # 设置页
│       │   ├── setup/              # 初始设置
│       │   └── components/         # 共用组件
│       └── service/                # 系统服务 (Boot Receiver)
```

### 技术栈

| 组件 | 技术选型 |
|------|---------|
| 语言 | Kotlin 100% |
| UI | Jetpack Compose + Material 3 |
| 架构 | MVVM + Clean Architecture |
| DI | Hilt |
| 异步 | Kotlin Coroutines + Flow |
| 数据库 | Room (隐藏应用列表) |
| 偏好设置 | DataStore |
| 加密 | Android Keystore (AES/GCM) |
| 生物识别 | BiometricPrompt |
| 最低 SDK | API 24 (Android 7.0) |
| 目标 SDK | API 34 |

## 已知限制

### 1. Android 10+ 厂商 ROM 限制
部分厂商（华为、小米、OPPO、vivo 等）的 Android 10+ ROM 可能限制 `PackageManager.setComponentEnabledSetting()` 对 activity-alias 的效果。具体表现：
- 图标隐藏后，桌面启动器可能仍然缓存图标
- 需要手动刷新桌面或重启设备
- 部分桌面启动器（如 MIUI 桌面）可能忽略 alias 的状态变化

### 2. 系统设置中仍可见
本 APP 只能隐藏应用在桌面的启动图标，无法隐藏应用在系统设置中的显示，包括：
- 设置 → 应用管理 → 所有应用列表
- 设置 → 应用权限管理
- 设置 → 默认应用设置

### 3. 隐藏应用仍可被其他方式启动
隐藏后的应用仍可通过以下方式启动：
- 其他应用调用 `PackageManager.getLaunchIntentForPackage()`
- ADB 命令 `am start`
- 桌面小部件（如果已添加）

### 4. 别名槽位数量限制
当前预注册 20 个 activity-alias 槽位，最多同时隐藏 20 个应用。如需更多槽位，需增加 AndroidManifest.xml 中的 alias 声明。

### 5. 隐私合规
- 本 APP **不收集任何用户数据**
- **不联网**，无网络权限
- **不含广告 SDK**
- 仅为个人隐私保护设计，请勿用于非法目的

## 安全说明

- 密码使用 Android Keystore 硬件加密存储，密钥不可提取
- 加密算法：AES-256-GCM，带随机 IV
- 数据存储使用 Room + DataStore，无网络传输
- 日志中不打印敏感信息（仅包名）

## 构建要求

- Android Studio Hedgehog (2023.1.1) 或更高版本
- JDK 17
- Gradle 8.5
- Android SDK 34

## 构建步骤

```bash
git clone <repo-url>
cd AppHider
./gradlew assembleDebug
```

APK 输出路径：`app/build/outputs/apk/debug/app-debug.apk`

## 免责声明

本应用仅供个人隐私保护使用。用户应对使用本应用的行为负全部责任。开发者不对因使用本应用产生的任何直接或间接损失承担责任。请遵守当地法律法规。