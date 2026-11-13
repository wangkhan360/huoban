# 构建、运行与交付说明（成长萌宠 V3.0 Android 学生端）

## 1. 环境与真实编译状态说明
- 本工程位于 `/android-app/`，采用标准 Android Studio Gradle Kotlin DSL 工程结构（`minSdk = 26`, `targetSdk = 35`, Kotlin 2.0.21, Jetpack Compose BOM 2024.11.00, Hilt, Room, DataStore, Retrofit, Coil GIF, Media3）。
- **重要提示**：由于生成本工程的沙箱容器为 Node.js Web 环境（未安装 JDK 17、Gradle 及 Android SDK），**未在容器内直接执行 `./gradlew assembleDebug` 编译生成 `.apk` 二进制文件，也未运行 Android 模拟器截图**。请在本地 Android Studio 中打开 `/android-app` 目录完成 Gradle Sync 与 Debug APK 编译。

## 2. 本地构建 Debug APK 步骤
1. 安装 **Android Studio Ladybug (2024.2+)** 与 **JDK 17**，确保 SDK Manager 已安装 Android API 35。
2. 在 Android Studio 中选择 **Open** 并指向本项目的 `/android-app` 目录。
3. 等待 Gradle 同步完成（依赖清单见 `gradle/libs.versions.toml`）。
4. （可选）如有官方云龙 8 状态透明背景 GIF 素材，请直接放入 `app/src/main/res/raw/`，命名为：
   - `yunlong_idle.gif`
   - `yunlong_listening.gif`
   - `yunlong_thinking.gif`
   - `yunlong_speaking.gif`
   - `yunlong_happy.gif`
   - `yunlong_encouraging.gif`
   - `yunlong_celebrating.gif`
   - `yunlong_resting.gif`
   `CompanionAnimationController` 会自动优先使用 Coil `ImageDecoderDecoder` 播放原始 GIF；未放入时自动回退至配套高清视觉素材与 Compose 状态动效。
5. 运行菜单 **Build -> Build Bundle(s) / APK(s) -> Build APK(s)** 或在终端执行 `./gradlew assembleDebug` 生成 `app/build/outputs/apk/debug/app-debug.apk`。

## 3. 演示账号与验证清单
- **一年级低年级演示核验码**：`1012`（学生：乐乐）
- **六年级高年级演示核验码**：`6068`（学生：思远）
- **核心验证路径**：
  1. 完成 8 步首次启动、核验码绑定、身份确认、云龙命名与 9 维画像采集（含敏感问题跳过）。
  2. 进入唯一 AI 主对话页，通过语音按钮或快捷话题触发 6 类动态卡片（任务、时间规划、成长目标、愿望协商、积分奖励、英语朗读录音）。
  3. 体验任务开始计时/完成/求助/调时、时间表顺序调整与草案提示、愿望提交家长审批、奖励申请兑换、英语朗读主动授权与试听提交。
  4. 进入历史对话、已提交录音、资料/云龙/隐私设置，并测试退出登录切换另一年级账号，确认 Room 数据按 `childId` 隔离。
