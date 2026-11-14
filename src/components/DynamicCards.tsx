import React, { useEffect, useRef, useState } from 'react';
import { AnimatePresence, motion, useReducedMotion } from 'motion/react';
import {
  DynamicCardType,
  FamilyWish,
  GradeBand,
  GrowthGoal,
  PointsRecord,
  RewardItem,
  ScheduleSlot,
  TaskItem,
} from '../types/student';
import rewardScienceKitImg from '../assets/images/reward_science_kit_1794558247850.jpg';
import yunlongCelebratingImg from '../assets/images/yunlong_celebrating_pose_1794558248617.jpg';

const CONFETTI_PIECES = Array.from({ length: 28 }, (_, i) => ({
  id: i,
  leftPct: (i * 37) % 100,
  delay: (i % 7) * 0.06,
  duration: 1.5 + (i % 5) * 0.22,
  driftX: ((i % 2 === 0 ? 1 : -1) * (18 + (i % 4) * 14)),
  rotate: (i % 2 === 0 ? 1 : -1) * (180 + (i % 3) * 90),
  size: 7 + (i % 4) * 2,
  color: ['#2578FF', '#4FB8FF', '#10A37F', '#F59F00', '#7CC4FF'][i % 5],
  isCircle: i % 3 === 0,
}));

export interface CelebrationOverlayData {
  taskTitle: string;
  pointsReward: number;
  allCompleted: boolean;
}

export const TaskCelebrationModal: React.FC<{
  data: CelebrationOverlayData | null;
  gradeBand: GradeBand;
  onClose: () => void;
}> = ({ data, gradeBand, onClose }) => {
  const isLower = gradeBand === 'LOWER_GRADE';
  const prefersReducedMotion = useReducedMotion();
  const [celebrateImgFailed, setCelebrateImgFailed] = useState(false);

  return (
    <AnimatePresence>
      {data && (
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          exit={{ opacity: 0 }}
          transition={{ duration: 0.22 }}
          onClick={onClose}
          role="status"
          aria-live="polite"
          className="fixed inset-0 z-50 flex items-center justify-center bg-[#13233A]/20 backdrop-blur-[2px] overflow-hidden cursor-pointer px-4"
        >
          {!prefersReducedMotion && (
            <div className="absolute inset-0 pointer-events-none overflow-hidden">
              {CONFETTI_PIECES.map((piece) => (
                <motion.span
                  key={piece.id}
                  initial={{ y: -24, x: 0, opacity: 1, rotate: 0, scale: 0.9 }}
                  animate={{
                    y: '105vh',
                    x: piece.driftX,
                    opacity: [1, 1, 0],
                    rotate: piece.rotate,
                    scale: 1,
                  }}
                  transition={{
                    duration: piece.duration,
                    delay: piece.delay,
                    ease: 'easeOut',
                  }}
                  style={{
                    position: 'absolute',
                    top: 0,
                    left: `${piece.leftPct}%`,
                    width: piece.size,
                    height: piece.isCircle ? piece.size : piece.size * 0.55,
                    borderRadius: piece.isCircle ? '9999px' : '2px',
                    backgroundColor: piece.color,
                  }}
                />
              ))}
            </div>
          )}

          <motion.div
            initial={prefersReducedMotion ? { opacity: 0 } : { scale: 0.86, y: 18, opacity: 0 }}
            animate={prefersReducedMotion ? { opacity: 1 } : { scale: 1, y: 0, opacity: 1 }}
            exit={prefersReducedMotion ? { opacity: 0 } : { scale: 0.92, y: 10, opacity: 0 }}
            transition={{ duration: 0.28, ease: [0.16, 1, 0.3, 1] }}
            className="relative bg-white border border-[#DCEBFF] rounded-3xl px-7 py-6 max-w-xs w-full text-center shadow-lg space-y-3"
          >
            <div className="mx-auto w-32 h-32 rounded-full overflow-hidden bg-gradient-to-b from-[#EAF3FF] to-[#F9FCFF] ring-4 ring-[#10A37F]/25 flex items-center justify-center animate-yunlong-celebrate">
              {!celebrateImgFailed ? (
                <img
                  src={yunlongCelebratingImg}
                  alt="云龙庆祝任务完成"
                  referrerPolicy="no-referrer"
                  onError={() => setCelebrateImgFailed(true)}
                  className="w-full h-full object-cover mix-blend-multiply scale-105"
                />
              ) : (
                <span className="text-sm font-semibold text-[#10A37F]">云龙庆祝中</span>
              )}
            </div>

            <div className="space-y-1">
              <p className="text-xs font-semibold text-[#10A37F]">
                {data.allCompleted
                  ? '今日全部任务达成'
                  : isLower
                  ? '太棒啦！云龙为你庆祝'
                  : '任务达成 · 专注表现很稳'}
              </p>
              <h5 className="text-base font-bold text-[#13233A]">{data.taskTitle}</h5>
              <p className="text-xs text-[#475C78] font-mono-tabular">
                +{data.pointsReward} 分 · 轻触关闭
              </p>
            </div>
          </motion.div>
        </motion.div>
      )}
    </AnimatePresence>
  );
};

export const PointsRewardsCard: React.FC<{
  totalPoints: number;
  pointsRecords: PointsRecord[];
  rewards: RewardItem[];
  onRequestRedeem: (rewardId: string) => void;
}> = ({ totalPoints, pointsRecords, rewards, onRequestRedeem }) => {
  const [rewardFilter, setRewardFilter] = useState<'ALL' | RewardItem['category']>('ALL');
  const categories: Array<'ALL' | RewardItem['category']> = [
    'ALL',
    '陪伴',
    '玩乐特权',
    '兴趣体验',
    '实物',
  ];
  const visibleRewards =
    rewardFilter === 'ALL'
      ? rewards
      : rewards.filter((r) => r.category === rewardFilter);
  const redeemableCount = rewards.filter(
    (r) => r.redeemStatus === 'AVAILABLE' && totalPoints >= r.costPoints
  ).length;

  return (
    <div className="bg-white border border-[#E1EAF4] rounded-2xl p-4 shadow-xs space-y-3">
      {/* Points Summary Banner */}
      <div className="rounded-xl bg-gradient-to-r from-[#EAF3FF] via-[#F2F7FF] to-[#E8F8F2] border border-[#DCEBFF] p-3.5 flex items-center justify-between gap-3">
        <div>
          <p className="text-[11px] font-medium text-[#475C78]">当前可用积分（服务端记录）</p>
          <p className="text-2xl font-bold text-[#13233A] font-mono-tabular mt-0.5">
            {totalPoints} <span className="text-xs font-medium text-[#2578FF]">分</span>
          </p>
        </div>
        <div className="text-right">
          <span className="inline-block px-2.5 py-1 rounded-full bg-white/90 border border-[#DCEBFF] text-xs font-semibold text-[#10A37F]">
            可兑换 {redeemableCount} 项活动
          </span>
        </div>
      </div>

      {/* Recent Points Records */}
      <div className="text-xs text-[#475C78] space-y-1">
        <p className="text-[#7B8FA8]">最近积分明细</p>
        {pointsRecords.slice(0, 2).map((rec) => (
          <p key={rec.id} className="font-mono-tabular flex items-center justify-between gap-2">
            <span className="truncate">+{rec.delta} 分 · {rec.reason}</span>
            <span className="text-[#7B8FA8] shrink-0">{rec.date}</span>
          </p>
        ))}
      </div>

      {/* Category Filter Tabs */}
      <div className="flex items-center gap-1 p-1 bg-[#F5F8FC] rounded-xl overflow-x-auto">
        {categories.map((cat) => (
          <button
            key={cat}
            type="button"
            onClick={() => setRewardFilter(cat)}
            className={`px-3 py-1.5 text-xs font-medium rounded-lg transition-colors whitespace-nowrap shrink-0 ${
              rewardFilter === cat
                ? 'bg-white text-[#13233A] shadow-xs'
                : 'text-[#475C78] hover:text-[#13233A]'
            }`}
          >
            {cat === 'ALL' ? '全部活动' : cat}
          </button>
        ))}
      </div>

      {/* Redeemable Reward Activities */}
      <div className="divide-y divide-[#E1EAF4]">
        {visibleRewards.map((rw) => {
          const canRedeemNow = rw.redeemStatus === 'AVAILABLE' && totalPoints >= rw.costPoints;
          const pointsNeeded = Math.max(0, rw.costPoints - totalPoints);

          return (
            <div key={rw.id} className="py-3 first:pt-1 last:pb-1 flex items-center justify-between gap-3">
              <div className="flex items-center gap-3 min-w-0">
                {rw.hasImage && (
                  <img
                    src={rewardScienceKitImg}
                    alt={rw.title}
                    referrerPolicy="no-referrer"
                    className="w-14 h-12 rounded-xl object-cover shrink-0 border border-[#E1EAF4]"
                  />
                )}
                <div className="min-w-0">
                  <div className="flex items-center gap-1.5 flex-wrap">
                    <p className="text-sm font-medium text-[#13233A]">{rw.title}</p>
                    {canRedeemNow && (
                      <span className="px-1.5 py-0.5 rounded-md bg-[#E8F8F2] text-[10px] font-semibold text-[#10A37F] shrink-0">
                        可兑换
                      </span>
                    )}
                  </div>
                  <p className="text-xs text-[#7B8FA8] mt-0.5">
                    {rw.category}
                    <span className="mx-1.5" aria-hidden="true">·</span>
                    <span className="font-mono-tabular font-medium text-[#2578FF]">{rw.costPoints} 分</span>
                  </p>
                  <p className="text-xs text-[#475C78] mt-0.5">{rw.description}</p>
                </div>
              </div>

              {rw.redeemStatus === 'AVAILABLE' ? (
                canRedeemNow ? (
                  <button
                    type="button"
                    onClick={() => onRequestRedeem(rw.id)}
                    className="min-h-[38px] px-3 py-1.5 rounded-xl bg-[#2578FF] text-white text-xs font-medium hover:bg-[#1A5FD1] shrink-0 whitespace-nowrap transition-colors"
                  >
                    申请兑换
                  </button>
                ) : (
                  <span className="min-h-[36px] px-2.5 py-1.5 rounded-xl bg-[#F5F8FC] border border-[#E1EAF4] text-xs font-medium text-[#7B8FA8] shrink-0 whitespace-nowrap flex items-center">
                    还差 {pointsNeeded} 分
                  </span>
                )
              ) : (
                <span className="text-xs font-medium text-[#D9820B] shrink-0 whitespace-nowrap">
                  {rw.redeemStatus === 'PENDING_PARENT'
                    ? '待家长确认'
                    : rw.redeemStatus === 'APPROVED'
                    ? '兑换成功'
                    : '未通过'}
                </span>
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
};

interface DynamicCardsProps {
  cardType: DynamicCardType;
  gradeBand: GradeBand;
  tasks: TaskItem[];
  onUpdateTask: (taskId: string, status: TaskItem['status'], feedback?: string) => void;
  onStartFocusTask?: (task: TaskItem) => void;
  onStartVoiceCheckin?: (task: TaskItem) => void;
  schedule: ScheduleSlot[];
  onMoveScheduleUp: (index: number) => void;
  onConfirmSchedule: () => void;
  goal: GrowthGoal;
  onUpdateGoal: (next: GrowthGoal) => void;
  wish: FamilyWish;
  onSubmitWish: (next: FamilyWish) => void;
  totalPoints: number;
  pointsRecords: PointsRecord[];
  rewards: RewardItem[];
  onRequestRedeem: (rewardId: string) => void;
  micAuthorized: boolean;
  onAuthorizeMic: () => void;
  onSaveRecording: (title: string, durationSec: number, audioUrl?: string) => void;
}

export const DynamicCards: React.FC<DynamicCardsProps> = ({
  cardType,
  gradeBand,
  tasks,
  onUpdateTask,
  onStartFocusTask,
  onStartVoiceCheckin,
  schedule,
  onMoveScheduleUp,
  onConfirmSchedule,
  goal,
  onUpdateGoal,
  wish,
  onSubmitWish,
  totalPoints,
  pointsRecords,
  rewards,
  onRequestRedeem,
  micAuthorized,
  onAuthorizeMic,
  onSaveRecording,
}) => {
  const [detailTask, setDetailTask] = useState<TaskItem | null>(null);
  const [zoomedImage, setZoomedImage] = useState<{ url: string; caption: string } | null>(null);
  const [celebrationOverlay, setCelebrationOverlay] = useState<CelebrationOverlayData | null>(null);

  useEffect(() => {
    if (!celebrationOverlay) return;
    const timeout = window.setTimeout(() => setCelebrationOverlay(null), 2600);
    return () => window.clearTimeout(timeout);
  }, [celebrationOverlay]);

  const handleTaskUpdate = (
    task: TaskItem,
    nextStatus: TaskItem['status'],
    feedback?: string
  ) => {
    onUpdateTask(task.id, nextStatus, feedback);
    if (nextStatus === 'COMPLETED') {
      const remainingTodo = tasks.filter(
        (t) => t.id !== task.id && t.status !== 'COMPLETED'
      ).length;
      setCelebrationOverlay({
        taskTitle: task.title,
        pointsReward: task.pointsReward,
        allCompleted: remainingTodo === 0,
      });
    }
  };

  // Goal local edit state
  const [weeklyAction, setWeeklyAction] = useState(goal.weeklyAction);
  const [reflectionNote, setReflectionNote] = useState(goal.reflectionNote);

  // Wish local edit state
  const [wishTitle, setWishTitle] = useState(wish.wishTitle);
  const [budgetNote, setBudgetNote] = useState(wish.budgetNote);

  // English reading recorder state
  const [isRecording, setIsRecording] = useState(false);
  const [recordSeconds, setRecordSeconds] = useState(0);
  const [recordedUrl, setRecordedUrl] = useState<string | null>(null);
  const [hasRecorded, setHasRecorded] = useState(false);
  const [uploadState, setUploadState] = useState<'IDLE' | 'UPLOADING' | 'SUBMITTED' | 'FAILED'>('IDLE');
  const [isPlayingPreview, setIsPlayingPreview] = useState(false);
  const mediaRecorderRef = useRef<MediaRecorder | null>(null);
  const chunksRef = useRef<Blob[]>([]);
  const audioElRef = useRef<HTMLAudioElement | null>(null);

  useEffect(() => {
    if (!isRecording) return;
    const t = window.setInterval(() => setRecordSeconds((s) => s + 1), 1000);
    return () => window.clearInterval(t);
  }, [isRecording]);

  const startReadingRecording = async () => {
    if (!micAuthorized) return;
    setRecordSeconds(0);
    setHasRecorded(false);
    setUploadState('IDLE');
    setRecordedUrl(null);
    chunksRef.current = [];
    try {
      if (navigator.mediaDevices && navigator.mediaDevices.getUserMedia) {
        const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
        const recorder = new MediaRecorder(stream);
        recorder.ondataavailable = (e) => {
          if (e.data.size > 0) chunksRef.current.push(e.data);
        };
        recorder.onstop = () => {
          const blob = new Blob(chunksRef.current, { type: 'audio/webm' });
          const url = URL.createObjectURL(blob);
          setRecordedUrl(url);
          stream.getTracks().forEach((track) => track.stop());
        };
        recorder.start();
        mediaRecorderRef.current = recorder;
      }
    } catch {
      // Safe fallback to timed simulated clip when browser blocks or lacks mic hardware
    }
    setIsRecording(true);
  };

  const stopReadingRecording = () => {
    if (mediaRecorderRef.current && mediaRecorderRef.current.state !== 'inactive') {
      mediaRecorderRef.current.stop();
    }
    setIsRecording(false);
    setHasRecorded(true);
  };

  const playReadingPreview = () => {
    if (recordedUrl) {
      const audio = new Audio(recordedUrl);
      audioElRef.current = audio;
      setIsPlayingPreview(true);
      audio.onended = () => setIsPlayingPreview(false);
      audio.play().catch(() => setIsPlayingPreview(false));
    } else {
      setIsPlayingPreview(true);
      window.setTimeout(() => setIsPlayingPreview(false), 1600);
    }
  };

  const submitReading = (simulateError = false) => {
    setUploadState('UPLOADING');
    window.setTimeout(() => {
      if (simulateError) {
        setUploadState('FAILED');
      } else {
        setUploadState('SUBMITTED');
        onSaveRecording('Unit 4 - My Sunny Day', Math.max(recordSeconds, 3), recordedUrl || undefined);
      }
    }, 450);
  };

  if (cardType === 'TASK_LIST') {
    const doneCount = tasks.filter((t) => t.status === 'COMPLETED').length;
    return (
      <div className="bg-white border border-[#E1EAF4] rounded-2xl p-4 shadow-xs space-y-3">
        <div className="flex items-center justify-between gap-4">
          <h4 className="text-base font-semibold text-[#13233A]">今天小任务</h4>
          <span className="text-xs font-medium text-[#10A37F] font-mono-tabular">
            {doneCount}/{tasks.length} 完成
          </span>
        </div>

        <div className="divide-y divide-[#E1EAF4]">
          {tasks.map((task) => {
            const isDone = task.status === 'COMPLETED';
            return (
              <div key={task.id} className="py-3 first:pt-1 last:pb-1 space-y-2">
                <button
                  type="button"
                  onClick={() => setDetailTask(task)}
                  className="w-full text-left flex items-center justify-between gap-3 group rounded-xl p-1.5 -mx-1.5 hover:bg-[#F5F8FC] transition-colors"
                >
                  <div className="flex items-center gap-2.5 min-w-0">
                    {task.images.length > 0 && (
                      <img
                        src={task.images[0].url}
                        alt={task.title}
                        referrerPolicy="no-referrer"
                        className="w-11 h-11 rounded-lg object-cover border border-[#E1EAF4] shrink-0"
                      />
                    )}
                    <div className="min-w-0">
                      <p className="text-sm font-medium text-[#13233A] group-hover:text-[#2578FF] transition-colors truncate">
                        {task.title}
                      </p>
                      <p className="text-xs text-[#7B8FA8] mt-0.5">
                        {task.subject} · {task.estimatedMinutes} 分钟 · +{task.pointsReward} 分
                      </p>
                    </div>
                  </div>
                  <span className="text-xs text-[#2578FF] font-medium shrink-0">详情</span>
                </button>

                {!isDone ? (
                  <div className="flex flex-wrap items-center gap-2">
                    <button
                      type="button"
                      onClick={() => {
                        if (onStartFocusTask) {
                          onStartFocusTask(task);
                        } else {
                          handleTaskUpdate(task, 'IN_PROGRESS');
                        }
                      }}
                      className="min-h-[38px] px-4 py-1.5 rounded-xl bg-[#2578FF] text-white text-xs font-medium hover:bg-[#1A5FD1] transition-colors whitespace-nowrap"
                    >
                      开始
                    </button>
                    {onStartVoiceCheckin && (
                      <button
                        type="button"
                        onClick={() => onStartVoiceCheckin(task)}
                        className="min-h-[38px] px-3.5 py-1.5 rounded-xl bg-[#EEF4FB] text-[#2578FF] text-xs font-medium hover:bg-[#DCEBFF] transition-colors whitespace-nowrap"
                      >
                        语音打卡
                      </button>
                    )}
                    {task.status === 'HELP_REQUESTED' && (
                      <span className="text-xs text-[#D9820B] font-medium">已求助</span>
                    )}
                    {task.status === 'TIME_ADJUST_REQUESTED' && (
                      <span className="text-xs text-[#2578FF] font-medium">已申请调时</span>
                    )}
                  </div>
                ) : (
                  <p className="text-xs font-medium text-[#10A37F]">
                    已完成 · {task.feedbackNote || '表现很棒'}
                  </p>
                )}
              </div>
            );
          })}
        </div>

        {doneCount === tasks.length && (
          <div className="pt-2 border-t border-[#E1EAF4] text-xs text-[#10A37F] font-medium">
            今天小任务全部完成啦！
          </div>
        )}

        {/* Full Homework Detail Modal */}
        {detailTask && (
          <div
            className="fixed inset-0 z-40 flex items-end sm:items-center justify-center bg-[#13233A]/35 backdrop-blur-[2px] p-0 sm:p-4"
            onClick={() => setDetailTask(null)}
          >
            <div
              onClick={(e) => e.stopPropagation()}
              className="w-full max-w-md max-h-[85vh] overflow-y-auto bg-white border border-[#E1EAF4] rounded-t-3xl sm:rounded-3xl p-5 space-y-4 shadow-lg"
            >
              <div className="flex items-start justify-between gap-3">
                <div>
                  <p className="text-xs text-[#7B8FA8]">
                    {detailTask.subject}
                    <span className="mx-1.5" aria-hidden="true">·</span>
                    {detailTask.source}
                    <span className="mx-1.5" aria-hidden="true">·</span>
                    预计 {detailTask.estimatedMinutes} 分钟
                  </p>
                  <h5 className="text-lg font-bold text-[#13233A] mt-0.5">{detailTask.title}</h5>
                </div>
                <button
                  type="button"
                  onClick={() => setDetailTask(null)}
                  className="min-h-[40px] px-3 rounded-xl text-xs font-medium text-[#475C78] hover:bg-[#F5F8FC] shrink-0"
                >
                  关闭
                </button>
              </div>

              {/* Exact homework text */}
              <div className="bg-[#F5F8FC] border border-[#E1EAF4] rounded-2xl p-3.5 whitespace-pre-line text-sm text-[#13233A] leading-relaxed">
                {detailTask.detailContent}
              </div>

              {/* Structured checklist */}
              <div className="space-y-1.5">
                <p className="text-xs font-semibold text-[#475C78]">作业分解要点</p>
                {detailTask.checklistItems.map((item, idx) => (
                  <p key={item} className="text-xs text-[#13233A] leading-relaxed">
                    {idx + 1}. {item}
                  </p>
                ))}
              </div>

              {/* Teacher reminder */}
              {detailTask.teacherReminder && (
                <div className="bg-[#FFF5E6] border border-[#F3DEC0] rounded-2xl p-3 text-xs text-[#8A5300] leading-relaxed">
                  {detailTask.teacherReminder}
                </div>
              )}

              {/* Attached homework images */}
              {detailTask.images.length > 0 && (
                <div className="space-y-2">
                  <p className="text-xs font-semibold text-[#475C78]">作业图片附件（点击查看大图）</p>
                  <div className="grid grid-cols-1 gap-2.5">
                    {detailTask.images.map((img) => (
                      <button
                        key={img.caption}
                        type="button"
                        onClick={() => setZoomedImage(img)}
                        className="text-left bg-[#F5F8FC] border border-[#E1EAF4] rounded-2xl overflow-hidden hover:border-[#2578FF] transition-colors"
                      >
                        <img
                          src={img.url}
                          alt={img.caption}
                          referrerPolicy="no-referrer"
                          className="w-full h-44 object-cover"
                        />
                        <p className="px-3 py-2 text-xs text-[#475C78]">{img.caption}</p>
                      </button>
                    ))}
                  </div>
                </div>
              )}

              {/* Detail actions */}
              <div className="flex flex-wrap gap-2 pt-2 border-t border-[#E1EAF4]">
                {detailTask.status !== 'COMPLETED' && onStartVoiceCheckin && (
                  <button
                    type="button"
                    onClick={() => {
                      const target = detailTask;
                      setDetailTask(null);
                      onStartVoiceCheckin(target);
                    }}
                    className="flex-1 min-h-[44px] px-4 rounded-xl bg-[#2578FF] text-white text-xs font-medium hover:bg-[#1A5FD1]"
                  >
                    开启语音打卡录音
                  </button>
                )}
                {detailTask.status !== 'COMPLETED' && (
                  <button
                    type="button"
                    onClick={() => {
                      const target = detailTask;
                      setDetailTask(null);
                      handleTaskUpdate(target, 'COMPLETED', '查看详情后完成打卡');
                    }}
                    className="min-h-[44px] px-4 rounded-xl bg-[#E8F8F2] text-[#10A37F] text-xs font-medium"
                  >
                    直接完成
                  </button>
                )}
                <button
                  type="button"
                  onClick={() => {
                    handleTaskUpdate(detailTask, 'HELP_REQUESTED', '请求思路提示');
                    setDetailTask(null);
                  }}
                  className="min-h-[44px] px-3 rounded-xl border border-[#E1EAF4] text-xs font-medium text-[#13233A]"
                >
                  求助
                </button>
                <button
                  type="button"
                  onClick={() => {
                    handleTaskUpdate(detailTask, 'TIME_ADJUST_REQUESTED', '申请延后至 19:30');
                    setDetailTask(null);
                  }}
                  className="min-h-[44px] px-3 rounded-xl border border-[#E1EAF4] text-xs font-medium text-[#2578FF]"
                >
                  调时
                </button>
              </div>
            </div>
          </div>
        )}

        {/* Zoomed Image Lightbox */}
        {zoomedImage && (
          <div
            className="fixed inset-0 z-50 flex flex-col items-center justify-center bg-[#13233A]/85 p-4"
            onClick={() => setZoomedImage(null)}
          >
            <img
              src={zoomedImage.url}
              alt={zoomedImage.caption}
              referrerPolicy="no-referrer"
              className="max-w-full max-h-[80vh] rounded-2xl object-contain"
            />
            <p className="mt-3 text-sm text-white">{zoomedImage.caption}（轻触关闭）</p>
          </div>
        )}

        {/* Transient Full-Screen Confetti & Yunlong Celebrating Overlay */}
        <TaskCelebrationModal
          data={celebrationOverlay}
          gradeBand={gradeBand}
          onClose={() => setCelebrationOverlay(null)}
        />
      </div>
    );
  }

  if (cardType === 'TIME_PLAN') {
    const hasDraft = schedule.some((s) => !s.isParentConfirmed);
    const hasConflict = schedule.some((s) => s.hasConflict);
    return (
      <div className="bg-white border border-[#E1EAF4] rounded-2xl p-4 shadow-xs space-y-3">
        <div className="flex items-center justify-between gap-4">
          <h4 className="text-base font-semibold text-[#13233A]">今日时间规划表</h4>
          {hasDraft && (
            <span className="text-xs font-medium text-[#D9820B]">含家长待确认草案</span>
          )}
        </div>

        {hasConflict && (
          <p className="text-xs text-[#E04848] bg-[#FFECEC] px-3 py-2 rounded-xl">
            时间冲突提示：晚间英语朗读与自由阅读衔接偏紧，可先上移顺序或微调 10 分钟休息。
          </p>
        )}

        <div className="divide-y divide-[#E1EAF4]">
          {schedule.map((slot, idx) => (
            <div key={slot.id} className="py-2.5 flex items-center justify-between gap-3">
              <div>
                <p className="text-xs text-[#7B8FA8] font-mono-tabular">
                  {slot.startTime} - {slot.endTime}
                  <span className="mx-1.5" aria-hidden="true">·</span>
                  {slot.period}
                </p>
                <p className="text-sm font-medium text-[#13233A] mt-0.5">{slot.title}</p>
                <p
                  className={`text-xs mt-0.5 ${
                    slot.isParentConfirmed ? 'text-[#10A37F]' : 'text-[#5D6F88]'
                  }`}
                >
                  {slot.isParentConfirmed ? '家长已确认' : '草案（家长尚未最终确认）'}
                  {slot.isChildConfirmed ? ' · 我已确认' : ''}
                </p>
              </div>
              {idx > 0 && (
                <button
                  type="button"
                  onClick={() => onMoveScheduleUp(idx)}
                  className="min-h-[38px] px-3 py-1 rounded-xl border border-[#E1EAF4] text-xs font-medium text-[#475C78] hover:bg-[#EEF4FB] shrink-0"
                >
                  上移顺序
                </button>
              )}
            </div>
          ))}
        </div>

        <button
          type="button"
          onClick={onConfirmSchedule}
          className="w-full min-h-[44px] rounded-xl bg-[#2578FF] text-white text-sm font-medium hover:bg-[#1A5FD1] transition-colors"
        >
          确认我的今日时间计划
        </button>
      </div>
    );
  }

  if (cardType === 'GROWTH_GOAL') {
    return (
      <div className="bg-white border border-[#E1EAF4] rounded-2xl p-4 shadow-xs space-y-3">
        <div className="flex items-center justify-between">
          <h4 className="text-base font-semibold text-[#13233A]">成长目标与榜样拆解</h4>
          <span className="text-sm font-semibold text-[#2578FF] font-mono-tabular">
            {goal.progressPercent}%
          </span>
        </div>

        <div className="w-full h-2 rounded-full bg-[#EEF4FB] overflow-hidden">
          <div
            className="h-full bg-[#2578FF] transition-all duration-300"
            style={{ width: `${goal.progressPercent}%` }}
          />
        </div>

        <div className="text-sm space-y-1.5 text-[#13233A]">
          <p>
            <span className="text-[#7B8FA8]">我想成为：</span>
            <span className="font-medium">{goal.aspiration}</span>
          </p>
          <p>
            <span className="text-[#7B8FA8]">我的榜样：</span>
            {goal.roleModel}（欣赏品质：{goal.admiredQualities}）
          </p>
          <p>
            <span className="text-[#7B8FA8]">想学习的能力：</span>
            {goal.targetSkills}
          </p>
        </div>

        <div className="pt-2 border-t border-[#E1EAF4] space-y-1">
          <p className="text-xs font-medium text-[#7B8FA8]">AI 拆解行动步骤</p>
          {goal.steps.map((step, i) => (
            <p key={step} className="text-xs text-[#475C78]">
              {i + 1}. {step}
            </p>
          ))}
        </div>

        <div className="space-y-2 pt-1">
          <label className="block text-xs text-[#7B8FA8]">
            本周行动
            <input
              type="text"
              value={weeklyAction}
              onChange={(e) => setWeeklyAction(e.target.value)}
              className="mt-1 w-full rounded-xl border border-[#E1EAF4] px-3 py-2 text-sm text-[#13233A] focus:outline-none focus:border-[#2578FF]"
            />
          </label>
          <label className="block text-xs text-[#7B8FA8]">
            成长复盘
            <input
              type="text"
              value={reflectionNote}
              onChange={(e) => setReflectionNote(e.target.value)}
              className="mt-1 w-full rounded-xl border border-[#E1EAF4] px-3 py-2 text-sm text-[#13233A] focus:outline-none focus:border-[#2578FF]"
            />
          </label>
        </div>

        <button
          type="button"
          onClick={() =>
            onUpdateGoal({
              ...goal,
              weeklyAction,
              reflectionNote,
              progressPercent: Math.min(100, goal.progressPercent + 10),
            })
          }
          className="w-full min-h-[44px] rounded-xl bg-[#2578FF] text-white text-sm font-medium hover:bg-[#1A5FD1] transition-colors"
        >
          保存本周行动与成长复盘
        </button>
      </div>
    );
  }

  if (cardType === 'WISH_NEGOTIATION') {
    const statusLabel =
      wish.approvalStatus === 'APPROVED'
        ? '家长已批准'
        : wish.approvalStatus === 'PENDING_PARENT'
        ? '等待家长确认中（未批准前不展示为已获得）'
        : '整理草案中';
    return (
      <div className="bg-white border border-[#E1EAF4] rounded-2xl p-4 shadow-xs space-y-3">
        <div className="flex items-center justify-between gap-2">
          <h4 className="text-base font-semibold text-[#13233A]">愿望与家庭协商单</h4>
          <span className="text-xs font-medium text-[#D9820B]">{statusLabel}</span>
        </div>

        <div className="space-y-2 text-xs text-[#475C78]">
          <label className="block text-[#7B8FA8]">
            我的愿望
            <input
              type="text"
              value={wishTitle}
              onChange={(e) => setWishTitle(e.target.value)}
              className="mt-1 w-full rounded-xl border border-[#E1EAF4] px-3 py-2 text-sm text-[#13233A] focus:outline-none focus:border-[#2578FF]"
            />
          </label>
          <p>
            <span className="text-[#7B8FA8]">希望家长支持：</span>
            {wish.parentSupportNeeded}
          </p>
          <p>
            <span className="text-[#7B8FA8]">AI 整理需求：</span>
            {wish.structuredSummary}
          </p>
          <label className="block text-[#7B8FA8]">
            资源与预算计划
            <input
              type="text"
              value={budgetNote}
              onChange={(e) => setBudgetNote(e.target.value)}
              className="mt-1 w-full rounded-xl border border-[#E1EAF4] px-3 py-2 text-sm text-[#13233A] focus:outline-none focus:border-[#2578FF]"
            />
          </label>
          <p>
            <span className="text-[#7B8FA8]">时间计划：</span>
            {wish.timelinePlan}
          </p>
          {wish.parentFeedback && (
            <p className="text-[#2578FF] font-medium">家长反馈：{wish.parentFeedback}</p>
          )}
        </div>

        <button
          type="button"
          onClick={() =>
            onSubmitWish({
              ...wish,
              wishTitle,
              budgetNote,
              approvalStatus: 'PENDING_PARENT',
            })
          }
          className="w-full min-h-[44px] rounded-xl bg-[#2578FF] text-white text-sm font-medium hover:bg-[#1A5FD1] transition-colors"
        >
          提交家长确认
        </button>
      </div>
    );
  }

  if (cardType === 'POINTS_REWARDS') {
    return (
      <PointsRewardsCard
        totalPoints={totalPoints}
        pointsRecords={pointsRecords}
        rewards={rewards}
        onRequestRedeem={onRequestRedeem}
      />
    );
  }

  // ENGLISH_READING
  return (
    <div className="bg-white border border-[#E1EAF4] rounded-2xl p-4 shadow-xs space-y-3">
      <div className="flex items-center justify-between">
        <h4 className="text-base font-semibold text-[#13233A]">英语朗读录音 · My Sunny Day</h4>
        <span className="text-sm font-semibold text-[#2578FF] font-mono-tabular">
          {String(Math.floor(recordSeconds / 60)).padStart(2, '0')}:
          {String(recordSeconds % 60).padStart(2, '0')}
        </span>
      </div>

      <p className="text-sm text-[#475C78] bg-[#F5F8FC] rounded-xl p-3">
        “The sun is bright today. I walk to school with my friend and look at the blue sky.”
      </p>

      {!micAuthorized ? (
        <div className="space-y-2">
          <p className="text-xs text-[#D9820B]">
            录音只能在孩子主动点击授权后开始。准备好了再开启麦克风哦。
          </p>
          <button
            type="button"
            onClick={onAuthorizeMic}
            className="w-full min-h-[44px] rounded-xl bg-[#2578FF] text-white text-sm font-medium hover:bg-[#1A5FD1]"
          >
            我已准备好，授权开启录音
          </button>
        </div>
      ) : (
        <div className="space-y-2.5">
          <div className="flex flex-wrap gap-2">
            {!isRecording ? (
              <button
                type="button"
                onClick={startReadingRecording}
                className="flex-1 min-h-[44px] px-4 rounded-xl bg-[#2578FF] text-white text-sm font-medium hover:bg-[#1A5FD1] whitespace-nowrap"
              >
                {hasRecorded ? '重新录制' : '开始录音'}
              </button>
            ) : (
              <button
                type="button"
                onClick={stopReadingRecording}
                className="flex-1 min-h-[44px] px-4 rounded-xl bg-[#0EA5A4] hover:bg-[#0D9488] text-white text-sm font-medium whitespace-nowrap transition-colors"
              >
                完成录音
              </button>
            )}

            {hasRecorded && !isRecording && (
              <button
                type="button"
                onClick={playReadingPreview}
                className="flex-1 min-h-[44px] px-4 rounded-xl border border-[#E1EAF4] text-sm font-medium text-[#13233A] hover:bg-[#EEF4FB] whitespace-nowrap"
              >
                {isPlayingPreview ? '试听播放中...' : '播放试听'}
              </button>
            )}
          </div>

          {hasRecorded && !isRecording && (
            <div className="flex items-center gap-2">
              <button
                type="button"
                onClick={() => submitReading(false)}
                className="flex-1 min-h-[44px] px-4 rounded-xl bg-[#10A37F] text-white text-sm font-medium whitespace-nowrap"
              >
                {uploadState === 'UPLOADING'
                  ? '正在上传...'
                  : uploadState === 'SUBMITTED'
                  ? '提交成功（已存入记录）'
                  : '确认提交录音'}
              </button>
              {uploadState === 'FAILED' && (
                <button
                  type="button"
                  onClick={() => submitReading(false)}
                  className="min-h-[44px] px-3 rounded-xl border border-[#E04848] text-[#E04848] text-xs font-medium"
                >
                  上传失败，点击重试
                </button>
              )}
            </div>
          )}
        </div>
      )}
    </div>
  );
};
