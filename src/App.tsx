import React, { useEffect, useRef, useState } from 'react';
import {
  ArrowLeft,
  CheckCircle2,
  Cloud,
  History,
  Keyboard,
  Maximize2,
  Mic,
  Play,
  Send,
  ShieldCheck,
  SlidersHorizontal,
  Square,
  UserCheck,
  Volume2,
  VolumeX,
} from 'lucide-react';
import {
  ChatMessage,
  CheckinRecordingStage,
  CompanionState,
  FamilyWish,
  GradeBand,
  GrowthGoal,
  OwnerVoiceprintProfile,
  PrototypeView,
  ReadingRecording,
  RewardItem,
  ScheduleSlot,
  TaskItem,
} from './types/student';
import {
  COMPANION_STATE_META,
  createPresetForGrade,
  matchHomeworkTask,
  PORTRAIT_QUESTIONS,
  routeMockAiReply,
} from './data/mockStudentData';
import { YunlongStage } from './components/YunlongStage';
import { DynamicCards } from './components/DynamicCards';
import { DesignSystemShowcase } from './components/DesignSystemShowcase';
import { AndroidArchitectureView } from './components/AndroidArchitectureView';
import yunlongAppIconImg from './assets/images/yunlong_app_icon_1794558247476.jpg';

type WorkspaceTab = 'prototype' | 'design' | 'android';

const ONBOARDING_NAV: Array<{ view: PrototypeView; label: string }> = [
  { view: 'onboarding_splash', label: '1. App 启动页' },
  { view: 'onboarding_welcome', label: '2. 云龙欢迎页' },
  { view: 'onboarding_code', label: '3. 家长核验码' },
  { view: 'onboarding_identity', label: '4. 身份确认页' },
  { view: 'onboarding_reveal', label: '5. 云龙正式亮相' },
  { view: 'onboarding_name', label: '6. 给云龙起名' },
  { view: 'onboarding_portrait_chat', label: '7. 初次认识对话' },
  { view: 'onboarding_portrait_confirm', label: '8. 画像确认页' },
];

export default function App() {
  const [workspaceTab, setWorkspaceTab] = useState<WorkspaceTab>('prototype');
  const [gradeBand, setGradeBand] = useState<GradeBand>('LOWER_GRADE');
  const [companionName, setCompanionName] = useState('云龙');
  const [currentView, setCurrentView] = useState<PrototypeView>('main_chat');
  const [companionState, setCompanionState] = useState<CompanionState>('happy');
  const [isIdleFullscreen, setIsIdleFullscreen] = useState(false);

  // Onboarding state
  const [verifyCode, setVerifyCode] = useState('1012');
  const [codeError, setCodeError] = useState<string | null>(null);
  const [questionIdx, setQuestionIdx] = useState(0);
  const [portraitDraft, setPortraitDraft] = useState('');
  const [portraitAnswers, setPortraitAnswers] = useState<Record<string, string>>({});

  // Student isolated workspace state
  const [preset, setPreset] = useState(() => createPresetForGrade('LOWER_GRADE', '云龙'));
  const [messages, setMessages] = useState<ChatMessage[]>(preset.initialMessages);
  const [tasks, setTasks] = useState<TaskItem[]>(preset.tasks);
  const [schedule, setSchedule] = useState<ScheduleSlot[]>(preset.schedule);
  const [goal, setGoal] = useState<GrowthGoal>(preset.goal);
  const [wish, setWish] = useState<FamilyWish>(preset.wish);
  const [rewards, setRewards] = useState<RewardItem[]>(preset.rewards);
  const [recordings, setRecordings] = useState<ReadingRecording[]>([
    {
      id: 'rec_seed_1',
      taskId: 't_routine',
      passageTitle: '每日跳绳与阅读口头打卡',
      durationSeconds: 18,
      uploadStatus: '已同步至云端',
      cloudRecordId: 'CLD-20261112-084',
      createdAt: '昨天 19:45',
    },
  ]);

  // Voice, TTS, input, and check-in recording state machine
  const [inputText, setInputText] = useState('');
  const [isListeningVoice, setIsListeningVoice] = useState(false);
  const [isSpeakingTts, setIsSpeakingTts] = useState(false);
  const [voiceAutoPlay, setVoiceAutoPlay] = useState(true);
  const [micAuthorized, setMicAuthorized] = useState(false);
  const [isOnline, setIsOnline] = useState(true);
  const [toastMsg, setToastMsg] = useState<string | null>(null);

  const [checkinStage, setCheckinStage] = useState<CheckinRecordingStage>('IDLE');
  const [pendingCheckinTask, setPendingCheckinTask] = useState<TaskItem | null>(null);
  const [checkinSeconds, setCheckinSeconds] = useState(0);
  const [playingRecId, setPlayingRecId] = useState<string | null>(null);

  // Voice-first input & Owner Voiceprint Wake Recognition
  const [showTextInput, setShowTextInput] = useState(false);
  const [ownerVoiceprint, setOwnerVoiceprint] = useState<OwnerVoiceprintProfile>({
    enabled: false,
    enrolled: false,
    wakePhrase: '云龙你好，我是乐乐',
    ownerLabel: '乐乐',
  });
  const [isEnrollingVoiceprint, setIsEnrollingVoiceprint] = useState(false);
  const [simulatedSpeakerIsOwner, setSimulatedSpeakerIsOwner] = useState(true);

  const chatEndRef = useRef<HTMLDivElement | null>(null);
  const checkinRecorderRef = useRef<MediaRecorder | null>(null);
  const checkinChunksRef = useRef<Blob[]>([]);

  const totalPoints = preset.pointsRecords.reduce((sum, r) => sum + r.delta, 0);

  useEffect(() => {
    if (currentView === 'main_chat' && !isIdleFullscreen) {
      chatEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    }
  }, [messages.length, currentView, isIdleFullscreen]);

  useEffect(() => {
    if (checkinStage !== 'RECORDING') return;
    const timer = window.setInterval(() => setCheckinSeconds((s) => s + 1), 1000);
    return () => window.clearInterval(timer);
  }, [checkinStage]);

  const setCompanionAndFullscreen = (next: CompanionState) => {
    setCompanionState(next);
    setIsIdleFullscreen(next === 'idle');
  };

  const switchGradeAccount = (nextGrade: GradeBand) => {
    window.speechSynthesis?.cancel();
    setIsSpeakingTts(false);
    setCheckinStage('IDLE');
    setPendingCheckinTask(null);
    setIsIdleFullscreen(false);
    setGradeBand(nextGrade);
    const nextPreset = createPresetForGrade(nextGrade, companionName);
    setPreset(nextPreset);
    setVerifyCode(nextPreset.verificationCode);
    setMessages(nextPreset.initialMessages);
    setTasks(nextPreset.tasks);
    setSchedule(nextPreset.schedule);
    setGoal(nextPreset.goal);
    setWish(nextPreset.wish);
    setRewards(nextPreset.rewards);
    setOwnerVoiceprint({
      enabled: false,
      enrolled: false,
      wakePhrase: `${companionName}你好，我是${nextPreset.childName}`,
      ownerLabel: nextPreset.childName,
    });
    setCompanionState('happy');
    setToastMsg(`已切换至 ${nextPreset.childName}（${nextPreset.gradeLevel}年级）独立账号空间`);
  };

  const resetToOnboarding = () => {
    window.speechSynthesis?.cancel();
    setIsSpeakingTts(false);
    setIsIdleFullscreen(false);
    setCheckinStage('IDLE');
    setWorkspaceTab('prototype');
    setQuestionIdx(0);
    setPortraitAnswers({});
    setCurrentView('onboarding_splash');
  };

  const speakReply = (text: string) => {
    if (!voiceAutoPlay || typeof window === 'undefined' || !('speechSynthesis' in window)) return;
    window.speechSynthesis.cancel();
    const utter = new SpeechSynthesisUtterance(text);
    utter.lang = 'zh-CN';
    utter.rate = 0.96;
    utter.onstart = () => {
      setIsSpeakingTts(true);
      setCompanionState('speaking');
    };
    utter.onend = () => {
      setIsSpeakingTts(false);
      setCompanionState((cur) => (cur === 'speaking' ? 'happy' : cur));
    };
    utter.onerror = () => {
      setIsSpeakingTts(false);
    };
    window.speechSynthesis.speak(utter);
  };

  const interruptTts = () => {
    window.speechSynthesis?.cancel();
    setIsSpeakingTts(false);
    if (companionState === 'speaking') {
      setCompanionState('happy');
    }
  };

  const appendAiBubble = (
    text: string,
    nextState: CompanionState,
    cardType?: ChatMessage['cardType']
  ) => {
    const aiMsg: ChatMessage = {
      id: `a_${Date.now()}_${Math.random().toString(36).slice(2, 6)}`,
      role: 'AI',
      text,
      timestamp: '刚刚',
      companionState: nextState,
      cardType,
    };
    setMessages((prev) => [...prev, aiMsg]);
    setCompanionState(nextState);
    speakReply(text);
  };

  const promptConfirmHomeworkCheckin = (task: TaskItem) => {
    setIsIdleFullscreen(false);
    setPendingCheckinTask(task);
    setCheckinStage('CONFIRMING_HOMEWORK');
    appendAiBubble(
      `你是说确认打开【${task.title}】吗？请说“确认”或“是”，确认后我就为你开启打卡录音模式！`,
      'speaking'
    );
  };

  const startCheckinRecording = async (task: TaskItem) => {
    setMicAuthorized(true);
    setCheckinSeconds(0);
    setCheckinStage('RECORDING');
    setIsIdleFullscreen(false);
    setCompanionState('listening');
    checkinChunksRef.current = [];

    try {
      if (navigator.mediaDevices && navigator.mediaDevices.getUserMedia) {
        const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
        const recorder = new MediaRecorder(stream);
        recorder.ondataavailable = (e) => {
          if (e.data.size > 0) checkinChunksRef.current.push(e.data);
        };
        recorder.onstop = () => {
          stream.getTracks().forEach((t) => t.stop());
        };
        recorder.start();
        checkinRecorderRef.current = recorder;
      }
    } catch {
      // Seamless fallback to timed recording mode when browser mic is unavailable
    }

    appendAiBubble(
      `已进入【${task.title}】打卡录音模式！我正安静陪你完成，结束后语音说“打卡完毕”即可自动同步云端。`,
      'listening'
    );
  };

  const finishCheckinRecordingAndSync = () => {
    const targetTask = pendingCheckinTask;
    const duration = Math.max(checkinSeconds, 4);

    setCheckinStage('SYNCING_CLOUD');
    setCompanionState('thinking');
    setToastMsg('正在将作业打卡录音同步到云端...');

    const commitCloudSync = (audioUrl?: string) => {
      window.setTimeout(() => {
        const cloudId = `CLD-${new Date().toISOString().slice(0, 10).replace(/-/g, '')}-${Math.floor(100 + Math.random() * 899)}`;
        if (targetTask) {
          setTasks((prev) =>
            prev.map((t) =>
              t.id === targetTask.id
                ? { ...t, status: 'COMPLETED', feedbackNote: `语音打卡 ${duration} 秒已同步云端` }
                : t
            )
          );
        }

        setRecordings((prev) => [
          {
            id: `rec_${Date.now()}`,
            taskId: targetTask?.id,
            passageTitle: targetTask ? `${targetTask.title} · 语音打卡录音` : '作业语音打卡录音',
            durationSeconds: duration,
            uploadStatus: '已同步至云端',
            cloudRecordId: cloudId,
            createdAt: '刚刚',
            audioUrl,
          },
          ...prev,
        ]);

        setCheckinStage('IDLE');
        setPendingCheckinTask(null);
        setCheckinSeconds(0);
        setToastMsg(`打卡录音已成功同步至云端（编号 ${cloudId}）`);
        appendAiBubble(
          `太棒啦！${targetTask ? `【${targetTask.title}】` : '本次作业'}打卡完毕，${duration} 秒打卡录音已成功同步至云端，可在「打卡记录」中随时回听。`,
          'celebrating'
        );
      }, 650);
    };

    const recorder = checkinRecorderRef.current;
    if (recorder && recorder.state !== 'inactive') {
      const stream = recorder.stream;
      recorder.onstop = () => {
        stream.getTracks().forEach((t) => t.stop());
        const blob = new Blob(checkinChunksRef.current, { type: 'audio/webm' });
        const audioUrl = blob.size > 0 ? URL.createObjectURL(blob) : undefined;
        checkinRecorderRef.current = null;
        commitCloudSync(audioUrl);
      };
      recorder.stop();
    } else {
      commitCloudSync(undefined);
    }
  };

  const handleSendMessage = (rawText: string) => {
    const trimmed = rawText.trim();
    if (!trimmed) return;
    interruptTts();
    setIsIdleFullscreen(false);

    const childMsg: ChatMessage = {
      id: `c_${Date.now()}`,
      role: 'CHILD',
      text: trimmed,
      timestamp: '刚刚',
    };
    setMessages((prev) => [...prev, childMsg]);
    setInputText('');
    setCompanionState('thinking');

    window.setTimeout(() => {
      // 1. Voice check-in completion while recording
      if (
        checkinStage === 'RECORDING' &&
        (trimmed.includes('打卡完毕') || trimmed.includes('打卡完成') || trimmed.includes('完成了'))
      ) {
        finishCheckinRecordingAndSync();
        return;
      }

      // 2. Voice confirmation when confirming homework
      if (checkinStage === 'CONFIRMING_HOMEWORK' && pendingCheckinTask) {
        if (
          trimmed === '是' ||
          trimmed.includes('确认') ||
          trimmed.includes('是的') ||
          trimmed.includes('对') ||
          trimmed === '好'
        ) {
          startCheckinRecording(pendingCheckinTask);
          return;
        }
        if (trimmed.includes('不') || trimmed.includes('取消') || trimmed.includes('换')) {
          setCheckinStage('IDLE');
          setPendingCheckinTask(null);
          appendAiBubble('好的，已取消本次打卡。你想打开哪一项作业呢？', 'idle');
          return;
        }
      }

      // 3. Voice trigger: "开始打开XX作业" / "打开XX作业"
      if (
        (trimmed.includes('打开') || trimmed.includes('开始')) &&
        (trimmed.includes('作业') ||
          trimmed.includes('语文') ||
          trimmed.includes('英语') ||
          trimmed.includes('跳绳') ||
          trimmed.includes('任务'))
      ) {
        const matched = matchHomeworkTask(trimmed, tasks);
        promptConfirmHomeworkCheckin(matched);
        return;
      }

      // 4. General AI intent routing
      const ai = routeMockAiReply(trimmed, gradeBand, companionName);
      appendAiBubble(ai.replyText, ai.companionState, ai.cardType);
    }, 300);
  };

  const enrollOwnerVoiceprint = async () => {
    if (isEnrollingVoiceprint) return;
    setIsEnrollingVoiceprint(true);
    setToastMsg(`请对着麦克风说出欢迎语：“${ownerVoiceprint.wakePhrase}”`);

    const finalizeEnrollment = (audioUrl?: string, spectralSummary?: string) => {
      const nowTime = new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
      setOwnerVoiceprint((prev) => ({
        ...prev,
        enabled: true,
        enrolled: true,
        ownerLabel: preset.childName,
        enrolledAt: `今天 ${nowTime}`,
        spectralSummary: spectralSummary || '基频 238Hz · 主人声纹特征已校准',
        audioUrl,
      }));
      setIsEnrollingVoiceprint(false);
      setSimulatedSpeakerIsOwner(true);
      setToastMsg(`已录好${preset.childName}的主人欢迎声纹，并开启“仅主人可唤醒”`);
      speakReply(`声纹欢迎识别录好啦，以后只有${preset.childName}本人能唤醒我哦！`);
    };

    try {
      if (navigator.mediaDevices && navigator.mediaDevices.getUserMedia) {
        const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
        const chunks: Blob[] = [];
        const recorder = new MediaRecorder(stream);
        recorder.ondataavailable = (e) => {
          if (e.data.size > 0) chunks.push(e.data);
        };
        recorder.onstop = () => {
          stream.getTracks().forEach((t) => t.stop());
          const blob = new Blob(chunks, { type: 'audio/webm' });
          const url = blob.size > 0 ? URL.createObjectURL(blob) : undefined;
          finalizeEnrollment(url, '基频 242Hz · 实时声纹模板已锁定');
        };
        recorder.start();
        window.setTimeout(() => {
          if (recorder.state !== 'inactive') recorder.stop();
        }, 2200);
        return;
      }
    } catch {
      // Graceful fallback when browser mic is denied
    }

    window.setTimeout(() => {
      finalizeEnrollment(undefined, '基频 238Hz · 模拟声纹模板已锁定');
    }, 1500);
  };

  const verifyOwnerWakeAttempt = (speakerIsOwner: boolean, spokenWake?: string): boolean => {
    if (!ownerVoiceprint.enabled || !ownerVoiceprint.enrolled) {
      return true;
    }
    if (!speakerIsOwner) {
      setCompanionState('idle');
      setToastMsg(`声纹欢迎识别拦截：未检测到主人（${ownerVoiceprint.ownerLabel}）声纹，无法唤醒`);
      speakReply(`声纹没有认出你哦，只有小主人${ownerVoiceprint.ownerLabel}可以唤醒我。`);
      return false;
    }
    setToastMsg(
      spokenWake
        ? `主人声纹匹配通过（“${spokenWake}”），欢迎回来！`
        : `主人（${ownerVoiceprint.ownerLabel}）声纹验证通过，已唤醒${companionName}`
    );
    return true;
  };

  const handleVoiceButton = () => {
    if (isListeningVoice) {
      setIsListeningVoice(false);
      setCompanionState('happy');
      return;
    }

    if (isIdleFullscreen && ownerVoiceprint.enabled && ownerVoiceprint.enrolled) {
      const allowed = verifyOwnerWakeAttempt(simulatedSpeakerIsOwner, ownerVoiceprint.wakePhrase);
      if (!allowed) return;
    }

    interruptTts();
    setIsIdleFullscreen(false);
    setIsListeningVoice(true);
    setCompanionState('listening');

    const SpeechRec =
      (window as unknown as { webkitSpeechRecognition?: new () => unknown; SpeechRecognition?: new () => unknown })
        .SpeechRecognition ||
      (window as unknown as { webkitSpeechRecognition?: new () => unknown }).webkitSpeechRecognition;

    if (SpeechRec) {
      try {
        const rec = new SpeechRec() as {
          lang: string;
          onresult: (e: { results: ArrayLike<ArrayLike<{ transcript: string }>> }) => void;
          onerror: () => void;
          onend: () => void;
          start: () => void;
        };
        rec.lang = 'zh-CN';
        rec.onresult = (e) => {
          const transcript = e.results[0][0].transcript;
          setIsListeningVoice(false);
          handleSendMessage(transcript);
        };
        rec.onerror = () => {
          setIsListeningVoice(false);
          handleSendMessage(
            checkinStage === 'RECORDING'
              ? '打卡完毕'
              : checkinStage === 'CONFIRMING_HOMEWORK'
              ? '确认'
              : '开始打开语文作业'
          );
        };
        rec.onend = () => setIsListeningVoice(false);
        rec.start();
        return;
      } catch {
        // Fallback to simulated voice turn
      }
    }

    window.setTimeout(() => {
      setIsListeningVoice(false);
      handleSendMessage(
        checkinStage === 'RECORDING'
          ? '打卡完毕'
          : checkinStage === 'CONFIRMING_HOMEWORK'
          ? '确认'
          : '开始打开语文作业'
      );
    }, 1000);
  };

  const handleVerifyParentCode = () => {
    const code = verifyCode.trim();
    if (code === '1012' || code === '1111') {
      setCodeError(null);
      switchGradeAccount('LOWER_GRADE');
      setCurrentView('onboarding_identity');
    } else if (code === '6068' || code === '6666') {
      setCodeError(null);
      switchGradeAccount('UPPER_GRADE');
      setCurrentView('onboarding_identity');
    } else if (code.length >= 4) {
      setCodeError(null);
      setCurrentView('onboarding_identity');
    } else {
      setCodeError('请输入 4 位家长核验码（演示码：1012 一年级 / 6068 六年级）');
    }
  };

  const handleUpdateTask = (taskId: string, status: TaskItem['status'], feedback?: string) => {
    setTasks((prev) =>
      prev.map((t) => (t.id === taskId ? { ...t, status, feedbackNote: feedback ?? t.feedbackNote } : t))
    );
    if (status === 'COMPLETED') setCompanionState('celebrating');
    else if (status === 'IN_PROGRESS') setCompanionState('encouraging');
  };

  const handleMoveScheduleUp = (idx: number) => {
    if (idx <= 0) return;
    setSchedule((prev) => {
      const next = [...prev];
      const temp = next[idx - 1];
      next[idx - 1] = next[idx];
      next[idx] = temp;
      return next;
    });
  };

  const handleConfirmSchedule = () => {
    setSchedule((prev) => prev.map((s) => ({ ...s, isChildConfirmed: true })));
    setCompanionState('happy');
    setToastMsg('已保存你的计划确认（未获家长审批的条目仍标为草案）');
  };

  const handleRequestRedeem = (rewardId: string) => {
    const target = rewards.find((r) => r.id === rewardId);
    if (!target) return;
    if (totalPoints < target.costPoints) {
      setToastMsg(`当前积分 ${totalPoints} 分，还差 ${target.costPoints - totalPoints} 分`);
      return;
    }
    setRewards((prev) =>
      prev.map((r) => (r.id === rewardId ? { ...r, redeemStatus: 'PENDING_PARENT' } : r))
    );
    setCompanionState('happy');
    setToastMsg('已发送奖励兑换申请，等待家长确认');
  };

  return (
    <div className="min-h-screen bg-[#F5F8FC] text-[#13233A] flex flex-col">
      {/* Strict 1-Row 3-Zone Top Bar */}
      <header className="sticky top-0 z-30 bg-white/90 backdrop-blur-md border-b border-[#E1EAF4]">
        <div className="max-w-7xl mx-auto px-6 h-16 flex items-center justify-between gap-8">
          <a
            href="#top"
            onClick={(e) => {
              e.preventDefault();
              setWorkspaceTab('prototype');
            }}
            className="text-lg font-bold tracking-tight text-[#13233A] whitespace-nowrap shrink-0"
          >
            成长萌宠 V3.0
          </a>

          <nav className="flex items-center gap-6 text-sm font-medium text-[#475C78]">
            {[
              { id: 'prototype', label: '交互原型' },
              { id: 'design', label: '设计规范与素材' },
              { id: 'android', label: 'Android 工程结构' },
            ].map((item) => (
              <button
                key={item.id}
                type="button"
                onClick={() => setWorkspaceTab(item.id as WorkspaceTab)}
                className={`whitespace-nowrap shrink-0 pb-0.5 transition-colors border-b-2 ${
                  workspaceTab === item.id
                    ? 'text-[#13233A] border-[#2578FF]'
                    : 'border-transparent hover:text-[#13233A]'
                }`}
              >
                {item.label}
              </button>
            ))}
          </nav>

          <button
            type="button"
            onClick={resetToOnboarding}
            className="min-h-[40px] px-4 py-2 rounded-xl bg-[#2578FF] text-white text-xs font-medium hover:bg-[#1A5FD1] transition-colors whitespace-nowrap shrink-0"
          >
            重置引导流程
          </button>
        </div>
      </header>

      {workspaceTab === 'design' && <DesignSystemShowcase />}
      {workspaceTab === 'android' && <AndroidArchitectureView />}

      {workspaceTab === 'prototype' && (
        <main className="max-w-7xl w-full mx-auto px-4 sm:px-6 py-6 grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
          {/* Left Inspector & Flow Switcher */}
          <aside className="lg:col-span-4 space-y-5">
            <div className="bg-white border border-[#E1EAF4] rounded-2xl p-5 space-y-4">
              <div>
                <h1 className="text-lg font-bold text-[#13233A]">学生端交互原型控制台</h1>
                <p className="text-xs text-[#7B8FA8] mt-1">
                  本地模拟演示模式 · 独立 REST 接口设计 · Room 账号隔离
                </p>
              </div>

              <div className="space-y-1.5">
                <p className="text-xs font-medium text-[#475C78]">切换学段账号（验证风格与隔离）</p>
                <div className="grid grid-cols-2 gap-1 p-1 bg-[#F5F8FC] rounded-xl">
                  <button
                    type="button"
                    onClick={() => switchGradeAccount('LOWER_GRADE')}
                    className={`min-h-[40px] rounded-lg text-xs font-medium transition-colors whitespace-nowrap ${
                      gradeBand === 'LOWER_GRADE'
                        ? 'bg-white text-[#13233A] shadow-xs'
                        : 'text-[#475C78]'
                    }`}
                  >
                    一年级 · 乐乐
                  </button>
                  <button
                    type="button"
                    onClick={() => switchGradeAccount('UPPER_GRADE')}
                    className={`min-h-[40px] rounded-lg text-xs font-medium transition-colors whitespace-nowrap ${
                      gradeBand === 'UPPER_GRADE'
                        ? 'bg-white text-[#13233A] shadow-xs'
                        : 'text-[#475C78]'
                    }`}
                  >
                    六年级 · 思远
                  </button>
                </div>
              </div>

              <div className="space-y-1.5">
                <p className="text-xs font-medium text-[#475C78]">核心主对话与辅助页面直达</p>
                <div className="grid grid-cols-2 gap-2">
                  {[
                    { view: 'main_chat' as PrototypeView, label: '唯一 AI 主对话' },
                    { view: 'aux_history' as PrototypeView, label: '历史对话记录' },
                    { view: 'aux_recordings' as PrototypeView, label: '打卡与录音记录' },
                    { view: 'aux_settings' as PrototypeView, label: '资料与设置' },
                  ].map((item) => (
                    <button
                      key={item.view}
                      type="button"
                      onClick={() => {
                        if (item.view === 'main_chat') setIsIdleFullscreen(false);
                        setCurrentView(item.view);
                      }}
                      className={`min-h-[40px] px-3 rounded-xl text-xs font-medium border transition-colors whitespace-nowrap ${
                        currentView === item.view
                          ? 'border-[#2578FF] bg-[#EEF4FB] text-[#2578FF]'
                          : 'border-[#E1EAF4] text-[#475C78] hover:bg-[#F5F8FC]'
                      }`}
                    >
                      {item.label}
                    </button>
                  ))}
                </div>
              </div>

              <div className="space-y-1.5">
                <p className="text-xs font-medium text-[#475C78]">首次启动与绑定 8 步流程</p>
                <div className="grid grid-cols-2 gap-1.5">
                  {ONBOARDING_NAV.map((item) => (
                    <button
                      key={item.view}
                      type="button"
                      onClick={() => setCurrentView(item.view)}
                      className={`min-h-[36px] px-2.5 rounded-lg text-left text-xs border transition-colors truncate ${
                        currentView === item.view
                          ? 'border-[#2578FF] bg-[#EEF4FB] text-[#2578FF] font-medium'
                          : 'border-[#E1EAF4] text-[#475C78] hover:bg-[#F5F8FC]'
                      }`}
                    >
                      {item.label}
                    </button>
                  ))}
                </div>
              </div>

              <div className="space-y-1.5">
                <p className="text-xs font-medium text-[#475C78]">云龙 8 种动画状态演示</p>
                <div className="grid grid-cols-2 gap-1.5">
                  {(Object.keys(COMPANION_STATE_META) as CompanionState[]).map((st) => (
                    <button
                      key={st}
                      type="button"
                      onClick={() => {
                        setCurrentView('main_chat');
                        setCompanionAndFullscreen(st);
                      }}
                      className={`min-h-[36px] px-2.5 rounded-lg text-left text-xs border transition-colors truncate ${
                        companionState === st
                          ? 'border-[#2578FF] bg-[#EEF4FB] text-[#2578FF] font-medium'
                          : 'border-[#E1EAF4] text-[#475C78] hover:bg-[#F5F8FC]'
                      }`}
                    >
                      {COMPANION_STATE_META[st].title}
                    </button>
                  ))}
                </div>
              </div>
              <div className="space-y-1.5">
                <p className="text-xs font-medium text-[#475C78]">主人语音欢迎识别验证身份</p>
                <div className="grid grid-cols-2 gap-1 p-1 bg-[#F5F8FC] rounded-xl">
                  <button
                    type="button"
                    onClick={() => setSimulatedSpeakerIsOwner(true)}
                    className={`min-h-[36px] rounded-lg text-xs font-medium transition-colors ${
                      simulatedSpeakerIsOwner ? 'bg-white text-[#10A37F] shadow-xs' : 'text-[#475C78]'
                    }`}
                  >
                    主人本人声纹
                  </button>
                  <button
                    type="button"
                    onClick={() => setSimulatedSpeakerIsOwner(false)}
                    className={`min-h-[36px] rounded-lg text-xs font-medium transition-colors ${
                      !simulatedSpeakerIsOwner ? 'bg-white text-[#E04848] shadow-xs' : 'text-[#475C78]'
                    }`}
                  >
                    访客/非主人声音
                  </button>
                </div>
                <p className="text-[11px] text-[#7B8FA8]">
                  {ownerVoiceprint.enabled && ownerVoiceprint.enrolled
                    ? '已开启仅主人可唤醒：访客点击待机唤醒将被声纹拦截'
                    : '可在右侧「设置」中录制欢迎语开启声纹专属唤醒'}
                </p>
              </div>
            </div>
          </aside>

          {/* Right High-Fidelity Android Device Viewport */}
          <section className="lg:col-span-8 flex justify-center">
            <div className="w-full max-w-[430px] min-h-[780px] bg-gradient-to-b from-[#EAF3FF] via-[#F5F8FC] to-white border border-[#D4E2F4] rounded-[32px] shadow-md overflow-hidden flex flex-col">
              {/* Onboarding Views */}
              {currentView === 'onboarding_splash' && (
                <div className="flex-1 flex flex-col items-center justify-center p-8 text-center space-y-5">
                  <img
                    src={yunlongAppIconImg}
                    alt="成长萌宠图标"
                    referrerPolicy="no-referrer"
                    className="w-24 h-24 rounded-3xl object-cover shadow-sm"
                  />
                  <div>
                    <h2 className="text-2xl font-bold text-[#13233A]">成长萌宠</h2>
                    <p className="text-sm text-[#475C78] mt-1">
                      以朋友建立关系 · 以管家管理日常 · 以教练陪你成长
                    </p>
                  </div>
                  <button
                    type="button"
                    onClick={() => setCurrentView('onboarding_welcome')}
                    className="min-h-[48px] px-8 rounded-xl bg-[#2578FF] text-white text-sm font-medium hover:bg-[#1A5FD1]"
                  >
                    进入云龙欢迎页
                  </button>
                </div>
              )}

              {currentView === 'onboarding_welcome' && (
                <div className="flex-1 flex flex-col justify-between p-6">
                  <div className="pt-6 text-center space-y-5">
                    <YunlongStage
                      state="happy"
                      companionName="云龙"
                      gradeBand={gradeBand}
                      size="lg"
                    />
                    <div className="space-y-2">
                      <h2 className="text-xl font-bold text-[#13233A]">
                        你好呀！我是来自云端的伙伴云龙
                      </h2>
                      <p className="text-sm text-[#475C78] leading-relaxed">
                        每天放学后我会陪你理清任务、规划作息、记录英语朗读，还陪你一起实现长远目标。
                      </p>
                    </div>
                  </div>
                  <button
                    type="button"
                    onClick={() => setCurrentView('onboarding_code')}
                    className="w-full min-h-[52px] rounded-xl bg-[#2578FF] text-white text-sm font-medium hover:bg-[#1A5FD1]"
                  >
                    开始绑定并认识云龙
                  </button>
                </div>
              )}

              {currentView === 'onboarding_code' && (
                <div className="flex-1 flex flex-col justify-between p-6">
                  <div className="space-y-4">
                    <p className="text-xs text-[#7B8FA8]">步骤 1 / 5 · 家庭安全绑定</p>
                    <h2 className="text-xl font-bold text-[#13233A]">输入家长核验码</h2>
                    <p className="text-sm text-[#475C78]">
                      请输入爸爸妈妈手机上的 4 位核验码（演示码：1012 一年级 / 6068 六年级）。
                    </p>
                    <input
                      type="text"
                      value={verifyCode}
                      onChange={(e) => {
                        setVerifyCode(e.target.value);
                        setCodeError(null);
                      }}
                      placeholder="输入 4 位核验码"
                      className="w-full rounded-xl bg-white border border-[#E1EAF4] px-4 py-3 text-base font-mono-tabular focus:outline-none focus:border-[#2578FF]"
                    />
                    {codeError && <p className="text-xs text-[#E04848]">{codeError}</p>}
                    <div className="flex gap-2">
                      <button
                        type="button"
                        onClick={() => setVerifyCode('1012')}
                        className="min-h-[40px] px-3 rounded-xl bg-white border border-[#E1EAF4] text-xs font-medium text-[#475C78]"
                      >
                        填入一年级码 1012
                      </button>
                      <button
                        type="button"
                        onClick={() => setVerifyCode('6068')}
                        className="min-h-[40px] px-3 rounded-xl bg-white border border-[#E1EAF4] text-xs font-medium text-[#475C78]"
                      >
                        填入六年级码 6068
                      </button>
                    </div>
                  </div>
                  <div className="flex gap-3">
                    <button
                      type="button"
                      onClick={() => setCurrentView('onboarding_welcome')}
                      className="flex-1 min-h-[52px] rounded-xl bg-white border border-[#E1EAF4] text-sm font-medium text-[#475C78]"
                    >
                      返回
                    </button>
                    <button
                      type="button"
                      onClick={handleVerifyParentCode}
                      className="flex-[2] min-h-[52px] rounded-xl bg-[#2578FF] text-white text-sm font-medium"
                    >
                      验证并继续
                    </button>
                  </div>
                </div>
              )}

              {currentView === 'onboarding_identity' && (
                <div className="flex-1 flex flex-col justify-between p-6">
                  <div className="space-y-4">
                    <p className="text-xs text-[#7B8FA8]">步骤 2 / 5 · 孩子身份确认</p>
                    <h2 className="text-xl font-bold text-[#13233A]">请确认是不是你本人</h2>
                    <div className="bg-white border border-[#E1EAF4] rounded-2xl p-5 space-y-2">
                      <p className="text-xl font-bold text-[#13233A]">{preset.childName}</p>
                      <p className="text-sm text-[#475C78]">
                        {preset.gradeLevel}年级 ·{' '}
                        {gradeBand === 'LOWER_GRADE'
                          ? '低年级温和引导表达'
                          : '高年级自主规划表达'}
                      </p>
                      <div className="pt-2 border-t border-[#E1EAF4] text-xs text-[#7B8FA8] space-y-1">
                        <p>已绑定家长：{preset.parentName}</p>
                        <p>独立账号存储：禁止不同孩子设备间数据交叉访问</p>
                      </div>
                    </div>
                  </div>
                  <div className="flex gap-3">
                    <button
                      type="button"
                      onClick={() => setCurrentView('onboarding_code')}
                      className="flex-1 min-h-[52px] rounded-xl bg-white border border-[#E1EAF4] text-sm font-medium text-[#475C78]"
                    >
                      重新核验
                    </button>
                    <button
                      type="button"
                      onClick={() => setCurrentView('onboarding_reveal')}
                      className="flex-[2] min-h-[52px] rounded-xl bg-[#2578FF] text-white text-sm font-medium"
                    >
                      是我，确认身份
                    </button>
                  </div>
                </div>
              )}

              {currentView === 'onboarding_reveal' && (
                <div className="flex-1 flex flex-col justify-between p-6 text-center">
                  <p className="text-xs text-[#7B8FA8]">步骤 3 / 5 · 云龙正式亮相</p>
                  <div className="space-y-4">
                    <YunlongStage
                      state="celebrating"
                      companionName="云龙"
                      gradeBand={gradeBand}
                      size="lg"
                    />
                    <h2 className="text-xl font-bold text-[#13233A]">云龙正式与你见面啦！</h2>
                    <p className="text-sm text-[#475C78]">
                      以后无论做作业、聊心事还是探索十万个为什么，云龙都会在画面中央一直陪着你。
                    </p>
                  </div>
                  <button
                    type="button"
                    onClick={() => setCurrentView('onboarding_name')}
                    className="w-full min-h-[52px] rounded-xl bg-[#2578FF] text-white text-sm font-medium"
                  >
                    给云龙起个名字
                  </button>
                </div>
              )}

              {currentView === 'onboarding_name' && (
                <div className="flex-1 flex flex-col justify-between p-6">
                  <div className="space-y-4">
                    <p className="text-xs text-[#7B8FA8]">步骤 4 / 5 · 专属称呼</p>
                    <h2 className="text-xl font-bold text-[#13233A]">你想叫云龙什么名字？</h2>
                    <input
                      type="text"
                      value={companionName}
                      onChange={(e) => setCompanionName(e.target.value)}
                      className="w-full rounded-xl bg-white border border-[#E1EAF4] px-4 py-3 text-base focus:outline-none focus:border-[#2578FF]"
                    />
                    <div className="flex flex-wrap gap-2">
                      {['云龙', '小云朵', '蓝风', '星云'].map((presetName) => (
                        <button
                          key={presetName}
                          type="button"
                          onClick={() => setCompanionName(presetName)}
                          className="min-h-[40px] px-3.5 rounded-xl bg-white border border-[#E1EAF4] text-xs font-medium text-[#475C78]"
                        >
                          {presetName}
                        </button>
                      ))}
                    </div>
                  </div>
                  <button
                    type="button"
                    onClick={() => {
                      if (!companionName.trim()) setCompanionName('云龙');
                      setQuestionIdx(0);
                      setCurrentView('onboarding_portrait_chat');
                    }}
                    className="w-full min-h-[52px] rounded-xl bg-[#2578FF] text-white text-sm font-medium"
                  >
                    叫它「{companionName.trim() || '云龙'}」，开始初次对话
                  </button>
                </div>
              )}

              {currentView === 'onboarding_portrait_chat' && (
                <div className="flex-1 flex flex-col justify-between p-6">
                  <div className="space-y-4">
                    <p className="text-xs text-[#7B8FA8] font-mono-tabular">
                      AI 初次认识你 · {questionIdx + 1} / {PORTRAIT_QUESTIONS.length}
                    </p>
                    <div className="flex items-start gap-3">
                      <YunlongStage
                        state="listening"
                        companionName={companionName}
                        gradeBand={gradeBand}
                        size="sm"
                      />
                      <div className="flex-1 bg-white border border-[#E1EAF4] rounded-2xl p-4">
                        <p className="text-sm font-medium text-[#13233A]">
                          {PORTRAIT_QUESTIONS[questionIdx].prompt}
                        </p>
                      </div>
                    </div>

                    <div className="space-y-2 pt-2">
                      <p className="text-xs text-[#7B8FA8]">点选或自己写下想法：</p>
                      {PORTRAIT_QUESTIONS[questionIdx].options.map((opt) => (
                        <button
                          key={opt}
                          type="button"
                          onClick={() => setPortraitDraft(opt)}
                          className={`w-full min-h-[44px] px-4 rounded-xl text-left text-sm border transition-colors ${
                            portraitDraft === opt
                              ? 'border-[#2578FF] bg-[#EEF4FB] text-[#13233A] font-medium'
                              : 'border-[#E1EAF4] bg-white text-[#475C78]'
                          }`}
                        >
                          {opt}
                        </button>
                      ))}
                      <input
                        type="text"
                        value={portraitDraft}
                        onChange={(e) => setPortraitDraft(e.target.value)}
                        placeholder="也可以在这里输入自己的回答..."
                        className="w-full rounded-xl bg-white border border-[#E1EAF4] px-4 py-3 text-sm focus:outline-none focus:border-[#2578FF]"
                      />
                    </div>
                  </div>

                  <div className="flex gap-3 pt-4">
                    {PORTRAIT_QUESTIONS[questionIdx].isSensitive && (
                      <button
                        type="button"
                        onClick={() => {
                          const qKey = PORTRAIT_QUESTIONS[questionIdx].key;
                          setPortraitAnswers((prev) => ({ ...prev, [qKey]: '已跳过敏感问题' }));
                          setPortraitDraft('');
                          if (questionIdx < PORTRAIT_QUESTIONS.length - 1) {
                            setQuestionIdx((i) => i + 1);
                          } else {
                            setCurrentView('onboarding_portrait_confirm');
                          }
                        }}
                        className="flex-1 min-h-[52px] rounded-xl bg-white border border-[#E1EAF4] text-sm font-medium text-[#475C78]"
                      >
                        跳过此题
                      </button>
                    )}
                    <button
                      type="button"
                      onClick={() => {
                        const cur = PORTRAIT_QUESTIONS[questionIdx];
                        const val = portraitDraft.trim() || cur.options[0];
                        setPortraitAnswers((prev) => ({ ...prev, [cur.key]: val }));
                        setPortraitDraft('');
                        if (questionIdx < PORTRAIT_QUESTIONS.length - 1) {
                          setQuestionIdx((i) => i + 1);
                        } else {
                          setCurrentView('onboarding_portrait_confirm');
                        }
                      }}
                      className="flex-[2] min-h-[52px] rounded-xl bg-[#2578FF] text-white text-sm font-medium"
                    >
                      {questionIdx < PORTRAIT_QUESTIONS.length - 1 ? '下一题' : '生成画像确认'}
                    </button>
                  </div>
                </div>
              )}

              {currentView === 'onboarding_portrait_confirm' && (
                <div className="flex-1 flex flex-col justify-between p-6">
                  <div className="space-y-3 overflow-y-auto max-h-[640px]">
                    <p className="text-xs text-[#7B8FA8]">步骤 5 / 5 · 画像确认页</p>
                    <h2 className="text-xl font-bold text-[#13233A]">
                      {preset.childName}的成长伙伴档案
                    </h2>
                    <div className="bg-white border border-[#E1EAF4] rounded-2xl divide-y divide-[#E1EAF4] px-4">
                      {PORTRAIT_QUESTIONS.map((q) => (
                        <div key={q.key} className="py-2.5">
                          <p className="text-xs text-[#7B8FA8]">{q.prompt}</p>
                          <p className="text-sm font-medium text-[#13233A] mt-0.5">
                            {portraitAnswers[q.key] || q.options[0]}
                          </p>
                        </div>
                      ))}
                    </div>
                  </div>
                  <button
                    type="button"
                    onClick={() => {
                      setCompanionState('happy');
                      setCurrentView('main_chat');
                    }}
                    className="w-full min-h-[52px] mt-4 rounded-xl bg-[#2578FF] text-white text-sm font-medium"
                  >
                    确认画像，进入唯一 AI 对话主界面
                  </button>
                </div>
              )}

              {/* Single Core AI Main Chat View */}
              {currentView === 'main_chat' && (
                <>
                  {isIdleFullscreen ? (
                    /* Full-screen Idle Waiting Companion Mode */
                    <div className="flex-1 flex flex-col justify-between p-6 bg-gradient-to-b from-[#DCEBFF]/60 via-[#EEF6FF] to-[#F9FCFF]">
                      <div className="flex items-center justify-between">
                        <button
                          type="button"
                          onClick={() => setCurrentView('aux_recordings')}
                          className="min-h-[40px] px-3 rounded-xl bg-white/80 border border-[#E1EAF4] text-xs font-medium text-[#475C78] flex items-center gap-1.5"
                        >
                          <Cloud className="w-4 h-4 text-[#2578FF]" />
                          打卡记录 ({recordings.length})
                        </button>
                        <button
                          type="button"
                          onClick={() => setCompanionAndFullscreen('happy')}
                          className="min-h-[40px] px-3 rounded-xl bg-white/80 border border-[#E1EAF4] text-xs font-medium text-[#475C78]"
                        >
                          返回对话流
                        </button>
                      </div>

                      <div
                        onClick={() => {
                          if (!verifyOwnerWakeAttempt(simulatedSpeakerIsOwner)) return;
                          setIsIdleFullscreen(false);
                          setCompanionState('listening');
                        }}
                        className="flex-1 flex flex-col items-center justify-center text-center cursor-pointer py-6 space-y-6"
                      >
                        <div className="scale-125">
                          <YunlongStage
                            state="idle"
                            companionName={companionName}
                            gradeBand={gradeBand}
                            size="lg"
                          />
                        </div>
                        <div className="space-y-1.5 pt-4">
                          <h2 className="text-xl font-bold text-[#13233A]">
                            {companionName}正在安静等你
                          </h2>
                          <p className="text-sm text-[#475C78] max-w-[260px] mx-auto">
                            {ownerVoiceprint.enabled && ownerVoiceprint.enrolled
                              ? `已开启声纹专属守护：仅小主人 ${ownerVoiceprint.ownerLabel} 说出“${ownerVoiceprint.wakePhrase}”可唤醒`
                              : '轻触画面或点击唤醒按钮，对我说“开始打开语文作业”即可开始录音打卡'}
                          </p>
                        </div>
                      </div>

                      <div className="space-y-2.5">
                        {ownerVoiceprint.enabled && ownerVoiceprint.enrolled && (
                          <div className="flex items-center justify-center gap-2 text-xs">
                            <button
                              type="button"
                              onClick={() => verifyOwnerWakeAttempt(false)}
                              className="min-h-[34px] px-3 rounded-lg bg-white/80 border border-[#E1EAF4] text-[#E04848]"
                            >
                              测试访客唤醒（拦截）
                            </button>
                            <button
                              type="button"
                              onClick={() => {
                                if (verifyOwnerWakeAttempt(true, ownerVoiceprint.wakePhrase)) {
                                  setIsIdleFullscreen(false);
                                  setCompanionState('happy');
                                }
                              }}
                              className="min-h-[34px] px-3 rounded-lg bg-white/80 border border-[#E1EAF4] text-[#10A37F]"
                            >
                              测试主人唤醒（放行）
                            </button>
                          </div>
                        )}

                        <div className="flex gap-2 justify-center">
                          <button
                            type="button"
                            onClick={() => {
                              if (!verifyOwnerWakeAttempt(simulatedSpeakerIsOwner)) return;
                              handleSendMessage('开始打开语文作业');
                            }}
                            className="min-h-[40px] px-3.5 rounded-full bg-white border border-[#DCEBFF] text-xs font-medium text-[#2578FF] hover:bg-[#EEF4FB]"
                          >
                            唤醒说“开始打开语文作业”
                          </button>
                          <button
                            type="button"
                            onClick={() => {
                              if (!verifyOwnerWakeAttempt(simulatedSpeakerIsOwner)) return;
                              handleSendMessage('开始打开英语作业');
                            }}
                            className="min-h-[40px] px-3.5 rounded-full bg-white border border-[#DCEBFF] text-xs font-medium text-[#2578FF] hover:bg-[#EEF4FB]"
                          >
                            唤醒说“开始打开英语作业”
                          </button>
                        </div>

                        <button
                          type="button"
                          onClick={handleVoiceButton}
                          className="w-full min-h-[54px] rounded-2xl bg-[#2578FF] text-white text-sm font-semibold flex items-center justify-center gap-2 shadow-sm hover:bg-[#1A5FD1] transition-colors"
                        >
                          <Mic className="w-5 h-5" />
                          唤醒{companionName}并开始说话
                        </button>
                      </div>
                    </div>
                  ) : (
                    <>
                      {/* Top Bar inside App */}
                      <div className="px-4 py-3 flex items-center justify-between border-b border-[#E1EAF4] bg-white/70 backdrop-blur-sm">
                        <div className="flex items-center gap-1">
                          <button
                            type="button"
                            onClick={() => setCurrentView('aux_history')}
                            aria-label="历史对话"
                            className="w-10 h-10 rounded-xl flex items-center justify-center text-[#13233A] hover:bg-[#EEF4FB]"
                          >
                            <History className="w-5 h-5" />
                          </button>
                          <button
                            type="button"
                            onClick={() => setCurrentView('aux_recordings')}
                            aria-label="打卡与录音记录"
                            className="min-h-[40px] px-2 rounded-xl flex items-center gap-1 text-xs font-medium text-[#13233A] hover:bg-[#EEF4FB]"
                          >
                            <Cloud className="w-4 h-4 text-[#2578FF]" />
                            <span className="font-mono-tabular">{recordings.length}</span>
                          </button>
                        </div>

                        <div className="text-center">
                          <p className="text-sm font-semibold text-[#13233A]">{companionName}</p>
                          <button
                            type="button"
                            onClick={() => setIsOnline((o) => !o)}
                            className="text-[11px] text-[#7B8FA8]"
                          >
                            {isOnline ? '模拟演示模式 · 网络正常' : '离线缓存模式（点击恢复）'}
                          </button>
                        </div>

                        <div className="flex items-center gap-1">
                          <button
                            type="button"
                            onClick={() => setCompanionAndFullscreen('idle')}
                            aria-label="进入安静等待全屏"
                            title="安静等待全屏"
                            className="w-10 h-10 rounded-xl flex items-center justify-center text-[#475C78] hover:bg-[#EEF4FB]"
                          >
                            <Maximize2 className="w-4 h-4" />
                          </button>
                          <button
                            type="button"
                            onClick={() => handleSendMessage('我现在有多少积分？')}
                            className="min-h-[40px] px-2 rounded-xl text-xs font-semibold text-[#2578FF] hover:bg-[#EEF4FB] font-mono-tabular whitespace-nowrap"
                          >
                            {totalPoints}分
                          </button>
                          <button
                            type="button"
                            onClick={() => setCurrentView('aux_settings')}
                            aria-label="个人资料与设置"
                            className="w-10 h-10 rounded-xl flex items-center justify-center text-[#13233A] hover:bg-[#EEF4FB]"
                          >
                            <SlidersHorizontal className="w-4 h-4" />
                          </button>
                        </div>
                      </div>

                      {toastMsg && (
                        <div className="mx-4 mt-2 px-3.5 py-2 rounded-xl bg-[#DCEBFF] text-xs text-[#13233A] flex items-center justify-between gap-2 z-20">
                          <span>{toastMsg}</span>
                          <button
                            type="button"
                            onClick={() => setToastMsg(null)}
                            className="text-[#2578FF] font-medium shrink-0"
                          >
                            关闭
                          </button>
                        </div>
                      )}

                      {/* Unified Canvas: Yunlong floats over the scrollable conversation text with zero hard split */}
                      <div className="relative flex-1 overflow-hidden">
                        {/* Conversation Stream filling the entire stage behind/below Yunlong */}
                        <div className="h-full overflow-y-auto px-4 pt-[172px] pb-6 space-y-3">
                          {messages.map((msg) => {
                            const isChild = msg.role === 'CHILD';
                            return (
                              <div
                                key={msg.id}
                                className={`flex flex-col ${isChild ? 'items-end' : 'items-start'} space-y-2`}
                              >
                                <div
                                  className={`max-w-[85%] px-4 py-2.5 text-sm leading-relaxed ${
                                    isChild
                                      ? 'bg-[#2578FF] text-white rounded-2xl rounded-br-md shadow-xs'
                                      : 'bg-white/95 border border-[#E1EAF4] text-[#13233A] rounded-2xl rounded-bl-md shadow-xs'
                                  }`}
                                >
                                  {msg.text}
                                </div>

                                {!isChild && msg.cardType && (
                                  <div className="w-full">
                                    <DynamicCards
                                      cardType={msg.cardType}
                                      gradeBand={gradeBand}
                                      tasks={tasks}
                                      onUpdateTask={handleUpdateTask}
                                      onStartVoiceCheckin={(task) => promptConfirmHomeworkCheckin(task)}
                                      schedule={schedule}
                                      onMoveScheduleUp={handleMoveScheduleUp}
                                      onConfirmSchedule={handleConfirmSchedule}
                                      goal={goal}
                                      onUpdateGoal={(nextGoal) => {
                                        setGoal(nextGoal);
                                        setCompanionState('happy');
                                        setToastMsg('已更新本周成长行动与复盘');
                                      }}
                                      wish={wish}
                                      onSubmitWish={(nextWish) => {
                                        setWish(nextWish);
                                        setCompanionState('encouraging');
                                        setToastMsg('愿望协商单已提交家长确认');
                                      }}
                                      totalPoints={totalPoints}
                                      pointsRecords={preset.pointsRecords}
                                      rewards={rewards}
                                      onRequestRedeem={handleRequestRedeem}
                                      micAuthorized={micAuthorized}
                                      onAuthorizeMic={() => setMicAuthorized(true)}
                                      onSaveRecording={(title, dur, url) => {
                                        setRecordings((prev) => [
                                          {
                                            id: `rec_${Date.now()}`,
                                            passageTitle: title,
                                            durationSeconds: dur,
                                            uploadStatus: '已同步至云端',
                                            cloudRecordId: `CLD-${Math.floor(1000 + Math.random() * 8999)}`,
                                            createdAt: '刚刚',
                                            audioUrl: url,
                                          },
                                          ...prev,
                                        ]);
                                        setCompanionState('celebrating');
                                        setToastMsg('英语朗读录音已同步至云端记录');
                                      }}
                                    />
                                  </div>
                                )}
                              </div>
                            );
                          })}
                          <div ref={chatEndRef} />
                        </div>

                        {/* Soft top atmospheric fade so text glides gently under floating Yunlong */}
                        <div
                          aria-hidden="true"
                          className="pointer-events-none absolute top-0 inset-x-0 h-24 bg-gradient-to-b from-[#EAF3FF]/90 via-[#EAF3FF]/40 to-transparent"
                        />

                        {/* Floating Yunlong Companion Overlay */}
                        <div className="pointer-events-none absolute top-1.5 inset-x-0 z-10 flex justify-center">
                          <div
                            onClick={() => setCompanionAndFullscreen('idle')}
                            title="点击进入安静等待全屏显示"
                            className="pointer-events-auto cursor-pointer"
                          >
                            <YunlongStage
                              state={isListeningVoice ? 'listening' : companionState}
                              companionName={companionName}
                              gradeBand={gradeBand}
                              size="md"
                              floating
                              isSpeakingTts={isSpeakingTts}
                              onInterruptTts={interruptTts}
                            />
                          </div>
                        </div>
                      </div>

                      {/* Active Check-in Confirmation / Live Recording Banner */}
                      {checkinStage === 'CONFIRMING_HOMEWORK' && pendingCheckinTask && (
                        <div className="mx-3 mb-2 p-3 rounded-2xl bg-[#EEF4FB] border border-[#2578FF]/30 space-y-2">
                          <p className="text-xs font-semibold text-[#13233A]">
                            确认打开「{pendingCheckinTask.title}」并开启录音打卡吗？
                          </p>
                          <div className="flex gap-2">
                            <button
                              type="button"
                              onClick={() => handleSendMessage('确认')}
                              className="flex-1 min-h-[40px] rounded-xl bg-[#2578FF] text-white text-xs font-medium"
                            >
                              语音/点击确认，开始录音
                            </button>
                            <button
                              type="button"
                              onClick={() => handleSendMessage('换一个作业')}
                              className="min-h-[40px] px-3 rounded-xl bg-white border border-[#E1EAF4] text-xs text-[#475C78]"
                            >
                              取消
                            </button>
                          </div>
                        </div>
                      )}

                      {checkinStage === 'RECORDING' && (
                        <div className="mx-3 mb-2 p-3 rounded-2xl bg-[#FFECEC] border border-[#E04848]/30 space-y-2">
                          <div className="flex items-center justify-between">
                            <p className="text-xs font-semibold text-[#B92B2B]">
                              正在录音打卡：{pendingCheckinTask?.title}
                            </p>
                            <span className="text-sm font-bold text-[#E04848] font-mono-tabular">
                              {String(Math.floor(checkinSeconds / 60)).padStart(2, '0')}:
                              {String(checkinSeconds % 60).padStart(2, '0')}
                            </span>
                          </div>
                          <div className="flex items-center justify-between gap-2">
                            <p className="text-[11px] text-[#7D2424]">
                              完成后请说“打卡完毕”，录音将自动同步到云端
                            </p>
                            <button
                              type="button"
                              onClick={() => handleSendMessage('打卡完毕')}
                              className="min-h-[38px] px-3.5 rounded-xl bg-[#E04848] text-white text-xs font-medium shrink-0"
                            >
                              说“打卡完毕”
                            </button>
                          </div>
                        </div>
                      )}

                      {checkinStage === 'SYNCING_CLOUD' && (
                        <div className="mx-3 mb-2 p-3 rounded-2xl bg-[#EEF4FB] border border-[#DCEBFF] text-xs font-medium text-[#2578FF]">
                          正在上传打卡录音并同步至云端...
                        </div>
                      )}

                      {/* Quick Conversational Triggers */}
                      <div className="px-4 py-2 flex items-center gap-2 overflow-x-auto border-t border-[#E1EAF4]/60 bg-white/75">
                        {[
                          '开始打开语文作业',
                          '开始打开英语作业',
                          '我今天有什么作业？',
                          '帮我看看今日时间计划',
                          '我想成为一名科学家',
                          '我现在有多少积分？',
                        ].map((prompt) => (
                          <button
                            key={prompt}
                            type="button"
                            onClick={() => handleSendMessage(prompt)}
                            className="min-h-[36px] px-3 rounded-full bg-white border border-[#E1EAF4] text-xs text-[#475C78] hover:border-[#2578FF] hover:text-[#2578FF] transition-colors whitespace-nowrap shrink-0"
                          >
                            {prompt}
                          </button>
                        ))}
                      </div>

                      {/* Voice-First Bottom Control Bar (Defaults to Tap-to-Speak; Keyboard toggle reveals text input) */}
                      <div className="p-3 bg-white border-t border-[#E1EAF4] flex items-center gap-2">
                        <button
                          type="button"
                          onClick={() => {
                            if (voiceAutoPlay) interruptTts();
                            setVoiceAutoPlay((v) => !v);
                          }}
                          aria-label={voiceAutoPlay ? '关闭自动语音播报' : '开启自动语音播报'}
                          className="w-11 h-11 rounded-xl border border-[#E1EAF4] flex items-center justify-center text-[#475C78] shrink-0"
                        >
                          {voiceAutoPlay ? <Volume2 className="w-5 h-5" /> : <VolumeX className="w-5 h-5" />}
                        </button>

                        {!showTextInput ? (
                          <>
                            <button
                              type="button"
                              onClick={handleVoiceButton}
                              className={`flex-1 min-h-[50px] px-4 rounded-2xl text-white text-sm font-semibold flex items-center justify-center gap-2 shadow-sm transition-colors ${
                                isListeningVoice
                                  ? 'bg-[#E04848] animate-pulse'
                                  : 'bg-[#2578FF] hover:bg-[#1A5FD1]'
                              }`}
                            >
                              {isListeningVoice ? (
                                <>
                                  <Square className="w-4 h-4" />
                                  <span>正在听你说... 点击结束</span>
                                </>
                              ) : (
                                <>
                                  <Mic className="w-5 h-5" />
                                  <span>
                                    {checkinStage === 'RECORDING'
                                      ? '点击语音说“打卡完毕”'
                                      : `点击和${companionName}语音对话`}
                                  </span>
                                </>
                              )}
                            </button>

                            <button
                              type="button"
                              onClick={() => setShowTextInput(true)}
                              aria-label="切换到键盘文字输入"
                              title="键盘输入"
                              className="w-11 h-11 rounded-xl border border-[#E1EAF4] flex items-center justify-center text-[#475C78] hover:bg-[#EEF4FB] shrink-0"
                            >
                              <Keyboard className="w-5 h-5" />
                            </button>
                          </>
                        ) : (
                          <>
                            <input
                              type="text"
                              value={inputText}
                              onChange={(e) => setInputText(e.target.value)}
                              onKeyDown={(e) => {
                                if (e.key === 'Enter') handleSendMessage(inputText);
                              }}
                              placeholder={
                                checkinStage === 'RECORDING'
                                  ? '输入“打卡完毕”结束录音...'
                                  : `输入你想对${companionName}说的话...`
                              }
                              className="flex-1 min-h-[46px] px-4 rounded-2xl bg-[#F5F8FC] border border-[#E1EAF4] text-sm focus:outline-none focus:border-[#2578FF]"
                            />

                            {inputText.trim() && (
                              <button
                                type="button"
                                onClick={() => handleSendMessage(inputText)}
                                aria-label="发送消息"
                                className="w-11 h-11 rounded-full bg-[#2578FF] text-white flex items-center justify-center shrink-0"
                              >
                                <Send className="w-4 h-4" />
                              </button>
                            )}

                            <button
                              type="button"
                              onClick={() => setShowTextInput(false)}
                              aria-label="切换回语音主模式"
                              title="切回语音输入"
                              className="w-11 h-11 rounded-xl bg-[#EEF4FB] text-[#2578FF] flex items-center justify-center shrink-0"
                            >
                              <Mic className="w-5 h-5" />
                            </button>
                          </>
                        )}
                      </div>
                    </>
                  )}
                </>
              )}

              {/* Auxiliary Screens */}
              {currentView === 'aux_history' && (
                <div className="flex-1 flex flex-col">
                  <div className="px-4 h-14 flex items-center gap-3 border-b border-[#E1EAF4] bg-white">
                    <button
                      type="button"
                      onClick={() => setCurrentView('main_chat')}
                      className="w-11 h-11 rounded-xl flex items-center justify-center hover:bg-[#EEF4FB]"
                    >
                      <ArrowLeft className="w-5 h-5" />
                    </button>
                    <h2 className="text-base font-semibold">历史对话记录</h2>
                  </div>
                  <div className="flex-1 overflow-y-auto p-4 space-y-3">
                    {messages.map((m) => (
                      <div key={m.id} className="bg-white border border-[#E1EAF4] rounded-2xl p-3.5 space-y-1">
                        <p className="text-xs text-[#7B8FA8]">
                          {m.role === 'CHILD' ? preset.childName : companionName} · {m.timestamp}
                        </p>
                        <p className="text-sm text-[#13233A]">{m.text}</p>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {currentView === 'aux_recordings' && (
                <div className="flex-1 flex flex-col">
                  <div className="px-4 h-14 flex items-center gap-3 border-b border-[#E1EAF4] bg-white">
                    <button
                      type="button"
                      onClick={() => setCurrentView('main_chat')}
                      className="w-11 h-11 rounded-xl flex items-center justify-center hover:bg-[#EEF4FB]"
                    >
                      <ArrowLeft className="w-5 h-5" />
                    </button>
                    <h2 className="text-base font-semibold">打开打卡与云端录音记录</h2>
                  </div>
                  <div className="flex-1 overflow-y-auto p-4 space-y-3">
                    {recordings.map((rec) => (
                      <div key={rec.id} className="bg-white border border-[#E1EAF4] rounded-2xl p-4 space-y-2">
                        <div className="flex items-start justify-between gap-2">
                          <div>
                            <p className="text-sm font-semibold text-[#13233A]">{rec.passageTitle}</p>
                            <p className="text-xs text-[#7B8FA8] font-mono-tabular mt-0.5">
                              时长 {rec.durationSeconds} 秒 · {rec.createdAt}
                              {rec.cloudRecordId ? ` · 云端 ID ${rec.cloudRecordId}` : ''}
                            </p>
                          </div>
                          <span className="inline-flex items-center gap-1 text-xs font-medium text-[#10A37F] shrink-0">
                            <CheckCircle2 className="w-3.5 h-3.5" />
                            {rec.uploadStatus}
                          </span>
                        </div>
                        <button
                          type="button"
                          onClick={() => {
                            if (rec.audioUrl) {
                              const audio = new Audio(rec.audioUrl);
                              setPlayingRecId(rec.id);
                              audio.onended = () => setPlayingRecId(null);
                              audio.play().catch(() => setPlayingRecId(null));
                            } else {
                              setPlayingRecId(rec.id);
                              window.setTimeout(() => setPlayingRecId(null), 1500);
                            }
                          }}
                          className="min-h-[38px] px-3.5 rounded-xl bg-[#F5F8FC] border border-[#E1EAF4] text-xs font-medium text-[#13233A] inline-flex items-center gap-1.5 hover:bg-[#EEF4FB]"
                        >
                          <Play className="w-3.5 h-3.5 text-[#2578FF]" />
                          {playingRecId === rec.id ? '正在试听播放...' : '播放打卡录音'}
                        </button>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {currentView === 'aux_settings' && (
                <div className="flex-1 flex flex-col">
                  <div className="px-4 h-14 flex items-center gap-3 border-b border-[#E1EAF4] bg-white">
                    <button
                      type="button"
                      onClick={() => setCurrentView('main_chat')}
                      className="w-11 h-11 rounded-xl flex items-center justify-center hover:bg-[#EEF4FB]"
                    >
                      <ArrowLeft className="w-5 h-5" />
                    </button>
                    <h2 className="text-base font-semibold">资料、云龙与隐私设置</h2>
                  </div>
                  <div className="flex-1 overflow-y-auto p-4 space-y-4">
                    <div className="bg-white border border-[#E1EAF4] rounded-2xl p-4 space-y-1.5">
                      <p className="text-sm font-semibold text-[#13233A]">个人资料与设备绑定状态</p>
                      <p className="text-xs text-[#475C78]">
                        学生姓名：{preset.childName}（{preset.gradeLevel}年级）
                      </p>
                      <p className="text-xs text-[#475C78]">绑定家长：{preset.parentName}</p>
                      <p className="text-xs text-[#7B8FA8] font-mono-tabular">
                        账号隔离 ID：{preset.childId} · 设备已绑定
                      </p>
                    </div>

                    <div className="bg-white border border-[#E1EAF4] rounded-2xl p-4 space-y-3">
                      <p className="text-sm font-semibold text-[#13233A]">云龙设置</p>
                      <label className="block text-xs text-[#7B8FA8]">
                        伙伴昵称
                        <input
                          type="text"
                          value={companionName}
                          onChange={(e) => setCompanionName(e.target.value)}
                          className="mt-1 w-full rounded-xl border border-[#E1EAF4] px-3 py-2 text-sm text-[#13233A]"
                        />
                      </label>
                      <div className="flex items-center justify-between text-sm">
                        <span>自动语音播报回复</span>
                        <button
                          type="button"
                          onClick={() => setVoiceAutoPlay((v) => !v)}
                          className={`min-h-[36px] px-3 rounded-lg text-xs font-medium ${
                            voiceAutoPlay
                              ? 'bg-[#2578FF] text-white'
                              : 'bg-[#EEF4FB] text-[#475C78]'
                          }`}
                        >
                          {voiceAutoPlay ? '已开启' : '已关闭'}
                        </button>
                      </div>
                    </div>

                    <div className="bg-white border border-[#E1EAF4] rounded-2xl p-4 space-y-3">
                      <div className="flex items-center justify-between">
                        <div className="flex items-center gap-2">
                          <ShieldCheck className="w-4 h-4 text-[#2578FF]" />
                          <p className="text-sm font-semibold text-[#13233A]">主人语音欢迎识别（仅主人可唤醒）</p>
                        </div>
                        <button
                          type="button"
                          onClick={() => {
                            if (!ownerVoiceprint.enrolled) {
                              setToastMsg('请先点击下方“录制主人欢迎声纹”完成声纹注册');
                              return;
                            }
                            setOwnerVoiceprint((prev) => ({ ...prev, enabled: !prev.enabled }));
                          }}
                          className={`min-h-[34px] px-3 rounded-lg text-xs font-medium ${
                            ownerVoiceprint.enabled && ownerVoiceprint.enrolled
                              ? 'bg-[#2578FF] text-white'
                              : 'bg-[#EEF4FB] text-[#475C78]'
                          }`}
                        >
                          {ownerVoiceprint.enabled && ownerVoiceprint.enrolled ? '已开启守护' : '未开启'}
                        </button>
                      </div>

                      <p className="text-xs text-[#475C78]">
                        录入小主人（{preset.childName}）的专属欢迎语音后，待机唤醒时将校验声纹特征，只有用户主人可以唤醒{companionName}。
                      </p>

                      <label className="block text-xs text-[#7B8FA8]">
                        专属欢迎唤醒语
                        <input
                          type="text"
                          value={ownerVoiceprint.wakePhrase}
                          onChange={(e) =>
                            setOwnerVoiceprint((prev) => ({ ...prev, wakePhrase: e.target.value }))
                          }
                          className="mt-1 w-full rounded-xl border border-[#E1EAF4] px-3 py-2 text-sm text-[#13233A]"
                        />
                      </label>

                      <div className="flex flex-wrap gap-2">
                        <button
                          type="button"
                          onClick={enrollOwnerVoiceprint}
                          disabled={isEnrollingVoiceprint}
                          className={`flex-1 min-h-[42px] px-3 rounded-xl text-xs font-semibold text-white flex items-center justify-center gap-1.5 ${
                            isEnrollingVoiceprint ? 'bg-[#E04848]' : 'bg-[#2578FF] hover:bg-[#1A5FD1]'
                          }`}
                        >
                          <Mic className="w-4 h-4" />
                          {isEnrollingVoiceprint
                            ? '正在采集主人声纹...'
                            : ownerVoiceprint.enrolled
                            ? '重新录制主人欢迎声纹'
                            : '录制主人欢迎声纹'}
                        </button>

                        {ownerVoiceprint.enrolled && ownerVoiceprint.audioUrl && (
                          <button
                            type="button"
                            onClick={() => new Audio(ownerVoiceprint.audioUrl).play().catch(() => {})}
                            className="min-h-[42px] px-3 rounded-xl bg-[#F5F8FC] border border-[#E1EAF4] text-xs font-medium text-[#13233A] flex items-center gap-1"
                          >
                            <Play className="w-3.5 h-3.5 text-[#2578FF]" />
                            试听声纹
                          </button>
                        )}
                      </div>

                      {ownerVoiceprint.enrolled && (
                        <div className="p-3 rounded-xl bg-[#F5F8FC] border border-[#E1EAF4] space-y-2">
                          <div className="flex items-center gap-1.5 text-xs font-medium text-[#10A37F]">
                            <UserCheck className="w-4 h-4" />
                            <span>
                              已注册主人声纹：{ownerVoiceprint.ownerLabel} · {ownerVoiceprint.enrolledAt}
                            </span>
                          </div>
                          {ownerVoiceprint.spectralSummary && (
                            <p className="text-[11px] text-[#7B8FA8] font-mono-tabular">
                              {ownerVoiceprint.spectralSummary}
                            </p>
                          )}
                          <div className="flex gap-2 pt-1">
                            <button
                              type="button"
                              onClick={() => {
                                setSimulatedSpeakerIsOwner(true);
                                verifyOwnerWakeAttempt(true, ownerVoiceprint.wakePhrase);
                              }}
                              className="flex-1 min-h-[34px] rounded-lg bg-white border border-[#E1EAF4] text-[11px] font-medium text-[#10A37F]"
                            >
                              模拟主人唤醒（通过）
                            </button>
                            <button
                              type="button"
                              onClick={() => {
                                setSimulatedSpeakerIsOwner(false);
                                verifyOwnerWakeAttempt(false);
                              }}
                              className="flex-1 min-h-[34px] rounded-lg bg-white border border-[#E1EAF4] text-[11px] font-medium text-[#E04848]"
                            >
                              模拟访客唤醒（拦截）
                            </button>
                          </div>
                        </div>
                      )}
                    </div>

                    <div className="bg-white border border-[#E1EAF4] rounded-2xl p-4 space-y-2">
                      <p className="text-sm font-semibold text-[#13233A]">隐私设置</p>
                      <div className="flex items-center justify-between text-sm">
                        <span>英语朗读麦克风授权</span>
                        <button
                          type="button"
                          onClick={() => setMicAuthorized((m) => !m)}
                          className={`min-h-[36px] px-3 rounded-lg text-xs font-medium ${
                            micAuthorized
                              ? 'bg-[#10A37F] text-white'
                              : 'bg-[#EEF4FB] text-[#475C78]'
                          }`}
                        >
                          {micAuthorized ? '已授权' : '未授权'}
                        </button>
                      </div>
                      <p className="text-xs text-[#7B8FA8]">
                        所有录音仅在孩子主动授权后启动；当前运行于本地模拟演示模式。
                      </p>
                    </div>

                    <button
                      type="button"
                      onClick={resetToOnboarding}
                      className="w-full min-h-[48px] rounded-xl bg-[#E04848] text-white text-sm font-medium"
                    >
                      退出登录并切换孩子账号
                    </button>
                  </div>
                </div>
              )}
            </div>
          </section>
        </main>
      )}
    </div>
  );
}
