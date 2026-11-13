export type GradeBand = 'LOWER_GRADE' | 'UPPER_GRADE';

export type CompanionState =
  | 'idle'
  | 'listening'
  | 'thinking'
  | 'speaking'
  | 'happy'
  | 'encouraging'
  | 'celebrating'
  | 'resting';

export type DynamicCardType =
  | 'TASK_LIST'
  | 'TIME_PLAN'
  | 'GROWTH_GOAL'
  | 'WISH_NEGOTIATION'
  | 'POINTS_REWARDS'
  | 'ENGLISH_READING';

export type PrototypeView =
  | 'onboarding_splash'
  | 'onboarding_welcome'
  | 'onboarding_code'
  | 'onboarding_identity'
  | 'onboarding_reveal'
  | 'onboarding_name'
  | 'onboarding_portrait_chat'
  | 'onboarding_portrait_confirm'
  | 'main_chat'
  | 'aux_history'
  | 'aux_recordings'
  | 'aux_settings';

export type CheckinRecordingStage =
  | 'IDLE'
  | 'CONFIRMING_HOMEWORK'
  | 'RECORDING'
  | 'SYNCING_CLOUD';

export interface ChatMessage {
  id: string;
  role: 'CHILD' | 'AI';
  text: string;
  timestamp: string;
  companionState?: CompanionState;
  cardType?: DynamicCardType;
}

export interface TaskImage {
  url: string;
  caption: string;
}

export interface TaskItem {
  id: string;
  title: string;
  subject: '语文' | '英语' | '数学' | '体育与习惯';
  source: '学校' | '家长' | '家庭日常';
  estimatedMinutes: number;
  status: 'TODO' | 'IN_PROGRESS' | 'COMPLETED' | 'HELP_REQUESTED' | 'TIME_ADJUST_REQUESTED';
  pointsReward: number;
  feedbackNote?: string;
  detailContent: string;
  checklistItems: string[];
  teacherReminder?: string;
  images: TaskImage[];
}

export interface ScheduleSlot {
  id: string;
  period: '早晨计划' | '作业时间' | '休息时间' | '晚间计划';
  startTime: string;
  endTime: string;
  title: string;
  isParentConfirmed: boolean;
  isChildConfirmed: boolean;
  hasConflict?: boolean;
}

export interface GrowthGoal {
  id: string;
  aspiration: string;
  roleModel: string;
  admiredQualities: string;
  targetSkills: string;
  steps: string[];
  weeklyAction: string;
  reflectionNote: string;
  progressPercent: number;
}

export interface FamilyWish {
  id: string;
  wishTitle: string;
  parentSupportNeeded: string;
  structuredSummary: string;
  budgetNote: string;
  timelinePlan: string;
  approvalStatus: 'DRAFT' | 'PENDING_PARENT' | 'APPROVED' | 'REJECTED';
  parentFeedback?: string;
}

export interface RewardItem {
  id: string;
  title: string;
  category: '陪伴' | '玩乐特权' | '兴趣体验' | '实物';
  costPoints: number;
  description: string;
  redeemStatus: 'AVAILABLE' | 'PENDING_PARENT' | 'APPROVED' | 'REJECTED';
  rejectReason?: string;
  hasImage?: boolean;
}

export interface PointsRecord {
  id: string;
  delta: number;
  reason: string;
  date: string;
}

export interface ReadingRecording {
  id: string;
  taskId?: string;
  passageTitle: string;
  durationSeconds: number;
  uploadStatus: '已同步至云端' | '正在同步云端' | '待重传';
  cloudRecordId?: string;
  createdAt: string;
  audioUrl?: string;
}

export interface OwnerVoiceprintProfile {
  enabled: boolean;
  enrolled: boolean;
  wakePhrase: string;
  ownerLabel: string;
  enrolledAt?: string;
  spectralSummary?: string;
  audioUrl?: string;
}
