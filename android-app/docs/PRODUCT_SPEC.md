# 成长萌宠 V3.0｜Android 原生学生端产品功能描述文档（Agent 交接与理解基线）

> **文档目的**：完整定义「成长萌宠 V3.0」Android 原生学生端的产品定位、交互范式、语音状态机、业务模块、数据隔离与 Mock/真实边界，供后续研发、设计或 AI Agent 准确理解并延续开发。

---

## 1. 产品定位与核心原则

- **产品名称**：成长萌宠（GrowthPet Student）
- **产品定位**：儿童 AI 成长伙伴 Android 原生应用。
- **核心理念**：**以朋友的方式建立关系，以管家的方式管理日常，以教练的方式帮助孩子实现目标。**
- **目标用户与双学段适配**（同一套架构，差异化语言风格、任务复杂度与提示）：
  - **低年级模式（`LOWER_GRADE`，1–3 年级）**：句式简短温和（1–3 句、少说教），多鼓励与具体动作引导，情绪安抚优先（演示账号：一年级「乐乐」，核验码 `1012`）。
  - **高年级模式（`UPPER_GRADE`，4–6 年级）**：平等尊重、去低幼化，支持自主拆解时间块、优先级权衡、成长复盘与理性家庭协商（演示账号：六年级「思远」，核验码 `6068`）。
- **唯一虚拟伙伴**：**云龙**（本期仅开发云龙，不包含其他角色）。云龙不是静态装饰物，而是贯穿全流程的 AI 实体，通过 8 种动画状态实时反馈情绪与行为。
- **核心交互原则**：**单一 AI 对话主界面**（类似 ChatGPT / GPT Voice Mode），不采用传统多 Tab 任务管理软件结构；所有任务、日程、目标、愿望、积分、朗读均在对话流中以动态原生卡片（Dynamic Cards）形式按需唤起。

---

## 2. 技术路线与架构约束

- **开发技术栈**（纯 Android 原生，禁止 uni-app / Vue / WebView 主实现）：
  - **语言与 UI**：Kotlin + Jetpack Compose + Material 3（`minSdk = 26`，`targetSdk = 35`）
  - **架构与状态**：MVVM + Repository（`MainViewModel` + `StateFlow` / Kotlin Coroutines + Hilt 依赖注入）
  - **导航**：Navigation Compose（单 Activity `MainActivity`）
  - **本地存储**：Room（9 张业务表，按 `childId` 严格隔离） + Jetpack DataStore（会话与设置持久化）
  - **音频与语音**：`SpeechRecognizer`（中文连续语音识别）+ `TextToSpeech`（中文语音播报）+ `MediaRecorder`（AAC/M4A 录音）+ Media3 `ExoPlayer`（录音回放试听）
  - **动画与图像**：Compose Animation + Coil `ImageDecoderDecoder`（原生播放透明背景 GIF）
  - **网络层**：Retrofit + OkHttp（已预留 `StudentApiService` REST 接口定义）
- **安全与红线约束**：
  1. **零大模型密钥暴露**：APK 客户端严禁内置 LLM API Key，Prompt 与模型调用仅运行在受控服务端；客户端仅接收结构化 JSON 意图与卡片载荷并由原生 Compose 渲染。
  2. **服务端权威边界**：客户端不可篡改积分余额；家庭愿望、奖励兑换、未确认时间表在家长审批前必须显示为“草案”或“等待家长确认中”，严禁伪造“已批准”。
  3. **录音隐私保护**：任何打卡或朗读录音必须先经过孩子主动点击或语音授权（`micAuthorizedByChild`）。

---

## 3. 页面路由与信息架构

应用共包含 5 个核心路由（无底部导航栏）：

| 路由 | 页面组件 | 定位与职责 |
|---|---|---|
| `onboarding` | `OnboardingFlowScreen` | 首次启动 8 步安全绑定、云龙命名与初次成长画像采集 |
| `main_chat` | `MainChatScreen` | **唯一核心主界面**：含「安静等待全屏态」与「云龙悬浮主对话流」两种显示状态 |
| `history` | `ConversationHistoryScreen` | 历史对话记录列表（顶部左上角入口，按当前 `childId` 展示） |
| `recordings` | `SubmittedRecordingsScreen` | 打卡与云端录音记录页（展示云端同步状态、同步编号 `CLD-*` 与本地回听） |
| `settings` | `SettingsAndProfileScreen` | 个人资料、绑定状态、云龙昵称、自动播报、主人声纹欢迎识别、隐私权限与退出切换账号 |

---

## 4. 首次使用与绑定引导流程（8 步）

当 `StudentSessionPrefs.isBoundAndOnboarded == false` 时进入 `OnboardingFlowScreen`：

1. **启动页（`SPLASH`）**：展示云龙 App 图标与产品定位标语，1.3s 后自动流转。
2. **云龙欢迎页（`WELCOME`）**：云龙以 `happy` 状态出场，介绍“朋友 / 管家 / 教练”陪伴方式。
3. **家长核验码绑定（`PARENT_CODE`）**：输入 4 位以上家庭核验码绑定孩子账号（演示快捷码：`1012` 绑定一年级乐乐，`6068` 绑定六年级思远）。
4. **孩子身份确认（`IDENTITY_CONFIRM`）**：核对姓名、年级、学段模式（低/高年级）与绑定家长姓名，明确同设备多账号隔离。
5. **云龙正式亮相（`YUNLONG_REVEAL`）**：云龙以 `celebrating` 状态欢迎加入。
6. **给云龙起名字（`NAME_YUNLONG`）**：支持自定义昵称或点选预设（云龙、小云朵、蓝风、星云）。
7. **AI 初次认识对话（`FIRST_PORTRAIT_CHAT`）**：9 维成长画像问答（开心事、兴趣爱好、喜恶、害怕/担心的事、未来志向、崇拜榜样、小愿望、放学放松、周末期待）。其中敏感问题（`worries`）支持一键「跳过此题」。
8. **成长画像确认（`PORTRAIT_CONFIRM`）**：汇总 9 维档案并写入 Room 与 DataStore，生成对应学段首条欢迎语进入 `main_chat`。

---

## 5. 虚拟伙伴「云龙」状态机与视觉规范

### 5.1 八种动画状态（`CompanionState`）
由全局单例 `CompanionAnimationController` 统一管理，支持前端本地交互切换与服务端 `companion_state` 字段驱动：

| 状态枚举 | wireKey | 适用场景与行为特征 |
|---|---|---|
| `IDLE` | `idle` | 安静等待，轻柔呼吸缩放（支持全屏陪伴待机态） |
| `LISTENING` | `listening` | 倾听孩子说话或打卡录音中，轻微歪头摆动 |
| `THINKING` | `thinking` | 思考问题、上传同步云端，专注微倾 |
| `SPEAKING` | `speaking` | 语音播报与连续对话回复，节律循环轻浮（不持续抬爪） |
| `HAPPY` | `happy` | 唤醒成功、完成日程或目标保存 |
| `ENCOURAGING` | `encouraging` | 引导任务开始、鼓励孩子提交愿望 |
| `CELEBRATING` | `celebrating` | 任务完成、打卡同步成功、积分查询庆祝 |
| `RESTING` | `resting` | 休眠休憩状态 |

### 5.2 资源加载与视觉融合机制
- **GIF 优先加载**：`YunlongCompanionStage` 自动检查 `res/raw/yunlong_<state>.gif`（如 `yunlong_idle.gif`、`yunlong_listening.gif` 等 8 个文件）；存在时由 Coil 直接解码播放。
- **降级渲染**：未放置 raw GIF 时，自动回退至 `res/drawable/img_yunlong_*` 高清插画 + Compose `graphicsLayer` 动画。
- **无白底硬边框**：云龙背景采用浅天蓝光晕（`SkyHaloLight` / `SkyGradientTop`）径向渐变融合，不出现白色矩形框。
- **悬浮对话排版**：在主对话页中，云龙以透明渐变遮罩**悬浮在可滚动对话流上方**（`Box` 叠层 + 顶部 92dp 渐隐过渡），对话文字从云龙下方自然滚动流过，无生硬水平分割线。
- **安静等待全屏模式（`isIdleFullscreen`）**：点击顶栏全屏图标或悬浮云龙进入全屏待机；云龙放大居中显示，支持轻触画面、测试声纹唤醒、快捷指令唤醒或一键开启实时语音对话。

---

## 6. 语音优先交互与实时连续对话系统

### 6.1 语音为主、键盘为辅
- 主对话底部默认展示**语音主交互栏**，不默认弹出文本输入框；点击右侧键盘图标可切换为文字输入框（`OutlinedTextField`），随时可切回语音模式。
- 顶部提供语音自动播报开关、“安静等待全屏”与直达「积分奖励活动」入口；悬浮云龙正下方与全屏待机页统一仅保留单个「今天小任务」轻量入口，去除多余短语胶囊，保持界面简洁。

### 6.2 GPT-Voice 风格实时在线语音对话（`isLiveVoiceSession`）
- **温和配色**：录音与连续语音连线全流程使用科技蓝/青绿色系（`SkyHaloLight`、`TechBluePrimary`、`SuccessMint`），**杜绝蓝变红的突兀刺激感**。
- **一按即通，无需长按**：点击“和云龙语音聊天”或全屏唤醒按钮后，`isLiveVoiceSession = true`，立即开启连续语音识别。
- **自动多轮循环**：
  1. 云龙进入 `LISTENING` 态持续等待孩子开口。
  2. 识别到完整语音后自动发送并进入 `THINKING` -> `SPEAKING` 回复与 TTS 播报。
  3. `MainViewModel` 监听 `speechSynthesizer.isSpeaking` 从 `true -> false` 的完成事件：若 `isLiveVoiceSession` 仍为开启状态，延时 220ms 后**自动重启下一轮倾听**（`beginLiveListeningPass()`），孩子无需再次点击任何按钮即可继续对话。
  4. **随时打断与暂停**：云龙说话时点击实时连线栏或“打断”可立即中断 TTS 并切回倾听；点击“暂停”按钮退出连续语音对话。

### 6.3 主人语音欢迎识别（声纹守护）
- 在设置页支持开启“主人语音欢迎识别（仅主人可唤醒）”：
  - 孩子可自定义唤醒语（如“云龙你好，我是乐乐”）并录制 2 秒主人欢迎声纹样本（`ownerVoiceprintSamplePath`）。
  - 开启守护后，处于安静等待全屏态唤醒时触发 `verifyOwnerWakeAttempt()`：
    - 若识别为非主人访客，云龙保持 `IDLE` 状态并语音提示拦截：“只有小主人本人可以唤醒我”。
    - 页面提供“主人唤醒（通过）”与“访客唤醒（拦截）”验证入口便于演示校验。

---

## 7. 语音驱动业务闭环：作业打开打卡与云端同步

支持孩子通过自然语音完成「点名作业 → 二次确认 → 静默陪伴录音 → 语音结束 → 同步云端」全闭环（由 `CheckinStage` 状态机驱动）：

1. **语音触发（`IDLE` -> `CONFIRMING_HOMEWORK`）**：
   - 孩子说出“开始打开语文作业” / “开始打开英语作业”等指令（或点击任务卡上的“语音打卡”）。
   - `matchHomeworkTask()` 自动匹配对应科目任务，云龙追问确认：“你是说确认打开【XXX】吗？请说‘确认’或‘是’，确认后我就为你开启打卡录音模式！”
2. **确认开启录音（`CONFIRMING_HOMEWORK` -> `RECORDING`）**：
   - 孩子回答“确认”/“是”/“好”（或点击确认横幅按钮），自动授权并启动 `AudioRecordingManager` 录制本地 `.m4a` 音频 + 实时秒表计时。
   - 底部出现浅蓝柔光提示条（无需长按），云龙保持 `LISTENING` 安静陪伴。
3. **语音结束与云端同步（`RECORDING` -> `SYNCING_CLOUD` -> `IDLE`）**：
   - 孩子说出“打卡完毕”（或“完成打卡”/“结束了”），自动停止录音、生成云端同步编号（格式 `CLD-yyyyMMdd-XXX`）。
   - 对应作业自动更新为 `COMPLETED` 并附带语音打卡时长备注；录音写入 `reading_recordings`（`uploadStatus = "SYNCED_CLOUD"`）。
   - 云龙切换为 `CELEBRATING` 状态庆祝，孩子可随时在「打卡与云端录音记录」页回听。

---

## 8. 六类原生动态业务卡片与业务规则

AI 回复可通过 `DynamicCardPayload` 在对话流中内嵌以下 6 类原生 Jetpack Compose 卡片：

1. **今日任务管理卡（`TASK_LIST` / `TaskManagementCard`）**：
   - 展示作业标题、学科、预估耗时与奖励分值。
   - **图文详情弹窗**：点击任意任务行或“详情”可打开完整作业内容（如语文周末背诵与听写词语、英语课堂词汇与不漏页读书要求）、老师小提醒（`teacherReminder`）及高清作业参考图（`img_homework_chinese` / `img_homework_english`）。
   - **全屏专注计时模式（点击“开始”）**：点击任务卡「开始」进入全屏专注计时界面，云龙切换 `THINKING` 陪伴专注；支持「暂停休息 / 继续」（联动 `RESTING` / `THINKING`）、右上角随时查看「作业内容」、以及点击「完成」标记任务完成并触发 `CELEBRATING` 庆祝。此外仍支持“语音打卡”、发起求助（`HELP_REQUESTED`）、申请调整时间（`TIME_ADJUST_REQUESTED`）。
2. **时间规划表卡（`TIME_PLAN` / `TimePlanningCard`）**：
   - 按早/作业/休息/晚间展示时段安排；支持点击“上移”微调顺序及“我确认这份今日时间表”。
   - 明确区分“家长已确认”与“草案（家长尚未最终确认）”，存在紧凑冲突时段时展示冲突提示。
3. **成长目标与榜样拆解卡（`GROWTH_GOAL` / `GrowthGoalCard`）**：
   - 展示长期志向、榜样人物及品质、目标能力、进度条、AI 拆解的行动步骤，支持编辑“本周行动”与“成长复盘”并更新进度。
4. **愿望与家庭协商单卡（`WISH_NEGOTIATION` / `WishNegotiationCard`）**：
   - 帮助孩子将愿望整理为结构化需求（目标、所需家长支持、预算说明、时间计划），支持提交家长确认（状态流转为 `PENDING_PARENT`）。
5. **积分与奖励商店卡（`POINTS_REWARDS` / `PointsRewardsCard`）**：
   - 支持**点击顶栏积分直接弹窗展示**或对话流内嵌展示；顶部横幅只读展示服务端可用积分及“可兑换 X 项活动”计数、近期加分明细；下方支持全部分类（全部活动、陪伴、玩乐特权、兴趣体验、实物）横向切换，清晰区分「可兑换 / 申请兑换」与「还差 X 分」，扣分与最终审批依赖家长端/服务端确认。
6. **英语朗读录音卡（`ENGLISH_READING` / `EnglishReadingCard`）**：
   - 强制先由孩子主动点击“我已准备好，授权开启录音”，随后支持录制、计时、Media3 `ExoPlayer` 本地试听与提交云端记录。

---

## 9. 本地数据模型与多账号隔离

Room 数据库（`AppDatabase`）包含 9 张表，所有业务查询均以 `childId` 作为强制隔离键：
- `child_profiles`（孩子档案与画像摘要）
- `chat_messages`（对话历史、意图与挂靠卡片 token）
- `task_items`（作业任务、图文详情、老师提醒、打卡反馈）
- `schedule_slots`（日程时段、冲突与家长确认标记）
- `growth_goals`（长期志向、榜样、拆解步骤与复盘）
- `family_wishes`（家庭愿望协商单与家长反馈）
- `points_records`（积分流水）
- `reward_items`（可兑换奖励与审批状态）
- `reading_recordings`（朗读与作业打卡音频路径、时长、云端编号与同步状态）

退出登录时（`logout()`）重置 DataStore 绑定状态与声纹激活态，重新输入另一年级核验码即可切换独立数据空间。

---

## 10. 当前工程 Mock 与真实能力边界（Agent 必读）

| 模块 | 当前实现状态 | 说明与后续接入方式 |
|---|---|---|
| **UI / 状态流 / 本地数据库 / 音频硬件** | **真实原生实现** | Compose UI、Room、DataStore、MediaRecorder、ExoPlayer、Android TTS 与 SpeechRecognizer 均已按真实 API 编写。 |
| **AI 对话与任务/积分数据源** | **Mock Repository（明确标识）** | 当前 Hilt 注入 `MockAiConversationRepository`（`isMockMode = true`），顶栏明确提示“模拟演示模式”。接入真实后端时，按 `docs/REST_API_SPEC.md` 实现 `RemoteAiConversationRepository` 替换绑定即可。 |
| **声纹校验算法** | **端侧模拟鉴权闭环** | 当前完成欢迎语配置、真实音频样本录制保存及“仅主人唤醒”拦截流程；实际声纹特征比对默认通过 `simulatedSpeakerIsOwner` 标志演示，正式上线需对接云端声纹识别 SDK/接口。 |
| **云端音频上传** | **本地落盘 + 模拟云端编号** | 打卡或朗读录音真实录制保存至应用私有目录并可回放，云端同步状态与 `CLD-*` 编号目前由本地逻辑模拟，待对接 `POST student/{childId}/recordings/upload`。 |
