import React from 'react';

const PROJECT_MODULES = [
  {
    layer: 'Gradle 与工程配置',
    path: 'android-app/gradle/libs.versions.toml · app/build.gradle.kts',
    details: 'Kotlin 2.0.21 · AGP 8.7.2 · minSdk 26 · targetSdk 35 · Jetpack Compose + Material 3 + Hilt + Room + DataStore + Retrofit + Coil GIF + Media3',
  },
  {
    layer: '设计系统 Tokens',
    path: 'ui/theme/ColorTokens.kt · DesignTokens.kt · Theme.kt',
    details: '统一定义 ColorTokens、AppTypography、Spacing、Radius、Elevation、IconStyle、AnimationDuration、CardStyle、ButtonStyle',
  },
  {
    layer: '云龙 8 状态动效控制器',
    path: 'companion/CompanionState.kt · CompanionAnimationController.kt',
    details: '统一管理 idle / listening / thinking / speaking / happy / encouraging / celebrating / resting，支持 Coil GIF 直接接入与透明安全渲染',
  },
  {
    layer: 'AI、语音与音频框架',
    path: 'audio/SpeechRecognitionManager.kt · SpeechSynthesisManager.kt · AudioRecordingManager.kt',
    details: '语音识别、TTS 播报与随时打断、英语朗读主动授权录音（MediaRecorder）与 Media3 试听',
  },
  {
    layer: 'Room + DataStore + REST API',
    path: 'data/local/AppDatabase.kt · data/remote/StudentApiService.kt · AiConversationRepository.kt',
    details: '按 childId 严格隔离对话/任务/日程/目标/愿望/积分/录音缓存，Mock 与真实 REST 接口分离且不把 AI 密钥写入 APK',
  },
  {
    layer: '全部原生界面与动态卡片',
    path: 'ui/onboarding/OnboardingScreens.kt · ui/chat/MainChatScreen.kt · ui/cards/BusinessCards.kt · ui/auxiliary/AuxiliaryScreens.kt',
    details: '8 步首次启动与画像采集 + 唯一 AI 对话主界面 + 6 类原生动态卡片 + 历史/录音/资料/隐私/绑定/退出辅助页',
  },
];

export const AndroidArchitectureView: React.FC = () => {
  return (
    <div className="max-w-6xl mx-auto px-6 py-8 space-y-8">
      <div className="space-y-2">
        <h2 className="text-2xl font-bold text-[#13233A]">Android Studio 原生工程结构与真实编译说明</h2>
        <p className="text-sm text-[#475C78] max-w-3xl">
          完整的 Kotlin + Jetpack Compose 源码已生成在工作区
          <code className="mx-1 px-1.5 py-0.5 bg-[#EEF4FB] rounded text-xs font-mono-tabular">/android-app</code>
          目录中，可直接用 Android Studio Ladybug（JDK 17 + Android SDK API 35）打开同步。
        </p>
      </div>

      <div className="bg-white border border-[#E1EAF4] rounded-2xl p-5 space-y-2">
        <h3 className="text-base font-semibold text-[#13233A]">当前环境编译状态诚实声明</h3>
        <p className="text-sm text-[#475C78]">
          由于当前在线构建容器为 Node.js Web 运行环境（未安装 JDK、Gradle 与 Android SDK），本环境无法直接编译生成
          <code className="mx-1 px-1.5 py-0.5 bg-[#EEF4FB] rounded text-xs font-mono-tabular">app-debug.apk</code>
          二进制安装包或运行 Android 模拟器截图。您可将
          <code className="mx-1 px-1.5 py-0.5 bg-[#EEF4FB] rounded text-xs font-mono-tabular">/android-app</code>
          下载到本地执行 <code className="px-1.5 py-0.5 bg-[#EEF4FB] rounded text-xs font-mono-tabular">./gradlew assembleDebug</code> 打包安装。
        </p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {PROJECT_MODULES.map((m, i) => (
          <div key={m.layer} className="bg-white border border-[#E1EAF4] rounded-2xl p-5 space-y-2">
            <p className="text-xs font-medium text-[#2578FF] font-mono-tabular">0{i + 1}. {m.layer}</p>
            <p className="text-xs text-[#7B8FA8] font-mono-tabular break-all">{m.path}</p>
            <p className="text-sm text-[#475C78]">{m.details}</p>
          </div>
        ))}
      </div>

      <div className="bg-white border border-[#E1EAF4] rounded-2xl p-5 space-y-2">
        <h3 className="text-base font-semibold text-[#13233A]">替换官方云龙 GIF 素材方法</h3>
        <p className="text-sm text-[#475C78]">
          如需接入官方制作的 8 状态透明背景 GIF，只需将其命名为
          <code className="mx-1 px-1.5 py-0.5 bg-[#EEF4FB] rounded text-xs font-mono-tabular">
            yunlong_idle.gif / yunlong_listening.gif / yunlong_thinking.gif / yunlong_speaking.gif / yunlong_happy.gif / yunlong_encouraging.gif / yunlong_celebrating.gif / yunlong_resting.gif
          </code>
          并放入 <code className="px-1.5 py-0.5 bg-[#EEF4FB] rounded text-xs font-mono-tabular">android-app/app/src/main/res/raw/</code>，
          <code className="mx-1 px-1.5 py-0.5 bg-[#EEF4FB] rounded text-xs font-mono-tabular">CompanionAnimationController</code> 会自动使用 Coil GIF 解码播放。
        </p>
      </div>
    </div>
  );
};
