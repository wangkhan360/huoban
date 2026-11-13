import {
  ChatMessage,
  CompanionState,
  DynamicCardType,
  FamilyWish,
  GradeBand,
  GrowthGoal,
  PointsRecord,
  RewardItem,
  ScheduleSlot,
  TaskItem,
} from '../types/student';
import chineseHomeworkImg from '../assets/images/homework_chinese_reading_1794559592370.jpg';
import englishHomeworkImg from '../assets/images/homework_english_vocab_1794559592730.jpg';

export interface StudentPreset {
  childId: string;
  childName: string;
  gradeLevel: number;
  gradeBand: GradeBand;
  parentName: string;
  verificationCode: string;
  tasks: TaskItem[];
  schedule: ScheduleSlot[];
  goal: GrowthGoal;
  wish: FamilyWish;
  pointsRecords: PointsRecord[];
  rewards: RewardItem[];
  initialMessages: ChatMessage[];
}

export const COMPANION_STATE_META: Record<
  CompanionState,
  { title: string; lowerLabel: string; upperLabel: string; motionHint: string }
> = {
  idle: {
    title: 'idle · 安静等待（全屏）',
    lowerLabel: '云龙正在安静陪着你',
    upperLabel: '全屏安静待命',
    motionHint: '全屏呼吸起伏，轻触或语音即可唤醒',
  },
  listening: {
    title: 'listening · 倾听',
    lowerLabel: '云龙正在认真听你说',
    upperLabel: '正在倾听',
    motionHint: '轻微歪头倾听与声波光晕',
  },
  thinking: {
    title: 'thinking · 思考',
    lowerLabel: '云龙在动脑筋想一想',
    upperLabel: '思考中',
    motionHint: '专注微倾与节奏光环',
  },
  speaking: {
    title: 'speaking · 连续说话',
    lowerLabel: '云龙正在和你说话',
    upperLabel: '语音播报中',
    motionHint: '适合长时间循环的柔和说话节奏，不持续抬爪',
  },
  happy: {
    title: 'happy · 开心',
    lowerLabel: '云龙好开心呀',
    upperLabel: '心情愉快',
    motionHint: '轻快上浮与温和微笑',
  },
  encouraging: {
    title: 'encouraging · 鼓励',
    lowerLabel: '云龙给你加油打气',
    upperLabel: '陪伴鼓励',
    motionHint: '前倾点头给予肯定',
  },
  celebrating: {
    title: 'celebrating · 庆祝',
    lowerLabel: '太棒啦！云龙为你庆祝',
    upperLabel: '达成庆祝',
    motionHint: '欢呼跃起与星光点缀',
  },
  resting: {
    title: 'resting · 休息',
    lowerLabel: '云龙休息一会儿',
    upperLabel: '休眠休憩',
    motionHint: '低频柔和呼吸，安静休眠',
  },
};

export const PORTRAIT_QUESTIONS = [
  {
    key: 'joy',
    prompt: '平时什么事情最容易让你开心呀？',
    isSensitive: false,
    options: ['和好朋友一起玩', '拼乐高和做手工', '看科普绘本'],
  },
  {
    key: 'hobbies',
    prompt: '你最喜欢的兴趣爱好是什么？',
    isSensitive: false,
    options: ['画画涂鸦', '观察天文科学', '踢足球与跳绳'],
  },
  {
    key: 'likes_dislikes',
    prompt: '有没有特别喜欢或不太喜欢的事情？',
    isSensitive: false,
    options: ['喜欢动手实验，不喜欢抄写太多', '喜欢听故事，不喜欢太吵'],
  },
  {
    key: 'worries',
    prompt: '遇到什么事情时会有点害怕或担心？（可跳过）',
    isSensitive: true,
    options: ['怕考不好被批评', '怕黑不敢一个人睡', '暂无特别担心的'],
  },
  {
    key: 'aspiration',
    prompt: '长大以后，你想成为一个怎样的人？',
    isSensitive: false,
    options: ['探索太空的科学家', '飞机与火箭工程师', '帮助别人的医生'],
  },
  {
    key: 'role_model',
    prompt: '你最崇拜或欣赏的榜样是谁？',
    isSensitive: false,
    options: ['钱学森', '袁隆平', '中国航天员'],
  },
  {
    key: 'wish',
    prompt: '现在心里最想实现的一个小愿望是什么？',
    isSensitive: false,
    options: ['拥有一套儿童天文望远镜', '周末去科技馆参加实验课'],
  },
  {
    key: 'weekday_fun',
    prompt: '平时放学后最喜欢做什么活动？',
    isSensitive: false,
    options: ['看一会科普书', '下楼跳绳跑步', '听故事休息'],
  },
  {
    key: 'weekend_fun',
    prompt: '周末最期待全家一起做什么？',
    isSensitive: false,
    options: ['参观科学博物馆', '去郊外公园野餐', '在家一起做手工作品'],
  },
];

export function createPresetForGrade(gradeBand: GradeBand, companionName = '云龙'): StudentPreset {
  const isLower = gradeBand === 'LOWER_GRADE';
  return {
    childId: isLower ? 'child_grade1_lele' : 'child_grade6_siyuan',
    childName: isLower ? '乐乐' : '思远',
    gradeLevel: isLower ? 1 : 6,
    gradeBand,
    parentName: isLower ? '乐乐爸爸' : '思远妈妈',
    verificationCode: isLower ? '1012' : '6068',
    tasks: [
      {
        id: 't_chinese',
        title: isLower ? '语文周末背诵与听写作业' : '语文课文精读、背诵与词语听写',
        subject: '语文',
        source: '学校',
        estimatedMinutes: 20,
        status: 'TODO',
        pointsReward: 6,
        detailContent:
          '@所有人 周末提示：\n语文\n💜《秋天》《江南》背熟\n💜64页《雪地里的小画家》读3遍\n💜听写以下词语（带拼音）：可口、叶子、东西、大火、人手、禾田\n\n🔅每天练习跳绳 · 坚持每日读课外书 · 练习魔方（自愿）',
        checklistItems: [
          '背熟课文：《秋天》《江南》',
          '第 64 页《雪地里的小画家》大声朗读 3 遍',
          '带拼音听写词语：可口、叶子、东西、大火、人手、禾田',
          '课外延伸：每天练习跳绳、每日坚持阅读课外书、练习魔方（自愿）',
        ],
        teacherReminder: '建议先朗读再背诵，听写时把拼音声调标清楚。',
        images: [
          {
            url: chineseHomeworkImg,
            caption: '语文课文朗读与带拼音田字格听写示例',
          },
        ],
      },
      {
        id: 't_english',
        title: '今日英语学习任务：课堂词汇与不漏页读书',
        subject: '英语',
        source: '学校',
        estimatedMinutes: 15,
        status: 'TODO',
        pointsReward: 6,
        detailContent:
          '📖今日英语学习任务\n1. 复习课堂重点内容，认真看同学的示范视频练习记忆：\nclassroom 教室 · blackboard 黑板\nopen the window 打开窗户 · close the window 关闭窗户\nopen the door 打开门 · close the door 关门\n2. 熟读复习书 P1-18，P31-36，P47，强调不漏页读书。',
        checklistItems: [
          '重点词汇句型记忆：classroom 教室 / blackboard 黑板',
          '动作指令跟读：open/close the window · open/close the door',
          '熟读复习课本 P1-18、P31-36、P47（逐页指读，不漏页）',
        ],
        teacherReminder:
          '小提醒：在本周做习题和读课文过程中，发现孩子对已学习知识有遗忘现象，请家长务必提醒孩子不漏页读书，战胜遗忘。',
        images: [
          {
            url: englishHomeworkImg,
            caption: '英语课堂词汇图解与课本页码复习范围',
          },
        ],
      },
      {
        id: 't_routine',
        title: isLower ? '每天跳绳练习与整理小书包' : '每日跳绳体能打卡与书桌整理',
        subject: '体育与习惯',
        source: '家庭日常',
        estimatedMinutes: 15,
        status: 'COMPLETED',
        pointsReward: 3,
        feedbackNote: '已完成 3 组跳绳并整理好书包',
        detailContent:
          '🔅每天练习跳绳（计时 3 组）\n🔅坚持每日读课外书 15 分钟\n🔅练习魔方（自愿加分项）并整理明天书包。',
        checklistItems: [
          '完成每日跳绳练习',
          '睡前坚持阅读课外书',
          '把课本和铅笔盒放回书包',
        ],
        images: [],
      },
    ],
    schedule: [
      {
        id: 's1',
        period: '早晨计划',
        startTime: '07:20',
        endTime: '07:45',
        title: '起床洗漱与营养早餐',
        isParentConfirmed: true,
        isChildConfirmed: true,
      },
      {
        id: 's2',
        period: '作业时间',
        startTime: '16:40',
        endTime: '17:15',
        title: '语文《秋天》《江南》背诵与词语听写',
        isParentConfirmed: true,
        isChildConfirmed: false,
      },
      {
        id: 's3',
        period: '休息时间',
        startTime: '17:15',
        endTime: '17:45',
        title: '每天跳绳练习与眼部放松',
        isParentConfirmed: false,
        isChildConfirmed: false,
      },
      {
        id: 's4',
        period: '晚间计划',
        startTime: '19:30',
        endTime: '19:50',
        title: '英语不漏页读书（P1-18 / P31-36 / P47）',
        isParentConfirmed: false,
        isChildConfirmed: false,
        hasConflict: true,
      },
    ],
    goal: {
      id: 'g1',
      aspiration: isLower ? '成为一名探索宇宙的科学家' : '成为一名航空系统设计师',
      roleModel: '钱学森',
      admiredQualities: '专注钻研、坚持到底、实事求是',
      targetSkills: '科学观察力、数学逻辑推理、清晰表达',
      steps: [
        '每天阅读 15 分钟科学绘本或航空资料',
        '每周记录 1 个生活中的“为什么”并尝试解释',
        '周末和爸爸妈妈完成 1 次光学或力学小实验',
      ],
      weeklyAction: '完成一次三棱镜折射小实验并讲给云龙听',
      reflectionNote: '昨天自己独立检查出了一道粗心题，专注时间比上周更长。',
      progressPercent: 45,
    },
    wish: {
      id: 'w1',
      wishTitle: isLower ? '想要一套儿童天文望远镜入门盒' : '希望周末去科技馆参加航天模型工作坊',
      parentSupportNeeded: '希望爸爸妈妈周末抽 2 小时陪同体验，并在家庭预算范围内支持',
      structuredSummary: '成长动机：拓展光学与天文兴趣；自律承诺：本周连续 5 天按时开启作业',
      budgetNote: '约 ¥120 - ¥180（申请用已有积分协同鼓励）',
      timelinePlan: '本周末周六下午 14:00 - 16:00',
      approvalStatus: 'PENDING_PARENT',
      parentFeedback: '家长已收到协商单，正在核对周六家庭日程',
    },
    pointsRecords: [
      { id: 'p1', delta: 15, reason: '本周连续三天按时开始作业', date: '昨天 19:20' },
      { id: 'p2', delta: 8, reason: '主动整理书包与书桌', date: '前天 20:05' },
      { id: 'p3', delta: 5, reason: '主动向云龙提出科学探究问题', date: '11-11 18:10' },
    ],
    rewards: [
      {
        id: 'r1',
        title: '和爸爸妈妈夜间观星陪伴 40 分钟',
        category: '陪伴',
        costPoints: 15,
        description: '晚饭后全家一起看星星、聊宇宙故事',
        redeemStatus: 'AVAILABLE',
      },
      {
        id: 'r2',
        title: '周六自主挑选家庭晚餐菜单特权',
        category: '玩乐特权',
        costPoints: 20,
        description: '担任一天小主厨搭配健康晚餐',
        redeemStatus: 'AVAILABLE',
      },
      {
        id: 'r3',
        title: '周末科技馆互动实验体验日',
        category: '兴趣体验',
        costPoints: 35,
        description: '需家长确认周末时间与同行安排',
        redeemStatus: 'PENDING_PARENT',
      },
      {
        id: 'r4',
        title: '儿童光学三棱镜与迷你望远镜套装',
        category: '实物',
        costPoints: 25,
        description: '含三棱镜、观测笔记本与便携入门望远镜',
        redeemStatus: 'AVAILABLE',
        hasImage: true,
      },
    ],
    initialMessages: [
      {
        id: 'm_init',
        role: 'AI',
        text: isLower
          ? `嗨 乐乐！我是你的成长伙伴${companionName}~ 你可以点下面看今天的语文和英语作业详情，或者直接唤醒我说“开始打开语文作业”来录音打卡哦！`
          : `你好，思远。我是你的成长伙伴${companionName}。你可以查看今日作业图文详情、切换全屏安静陪伴，或直接语音说“开始打开英语作业”启动录音打卡。`,
        timestamp: '刚刚',
        companionState: 'happy',
      },
      {
        id: 'm_tasks_intro',
        role: 'AI',
        text: '今天的语文和英语作业我都整理好啦，点开任意一条就能看到老师原话、练习清单和配套课本图片！',
        timestamp: '刚刚',
        companionState: 'idle',
        cardType: 'TASK_LIST',
      },
    ],
  };
}

export function matchHomeworkTask(input: string, tasks: TaskItem[]): TaskItem {
  const q = input.toLowerCase();
  if (q.includes('语文') || q.includes('秋天') || q.includes('江南') || q.includes('听写')) {
    return tasks.find((t) => t.subject === '语文') || tasks[0];
  }
  if (q.includes('英语') || q.includes('blackboard') || q.includes('朗读')) {
    return tasks.find((t) => t.subject === '英语') || tasks[0];
  }
  if (q.includes('跳绳') || q.includes('书包') || q.includes('习惯') || q.includes('体育')) {
    return tasks.find((t) => t.subject === '体育与习惯') || tasks[0];
  }
  return tasks.find((t) => t.status !== 'COMPLETED') || tasks[0];
}

export function routeMockAiReply(
  input: string,
  gradeBand: GradeBand,
  companionName: string
): {
  replyText: string;
  companionState: CompanionState;
  cardType?: DynamicCardType;
} {
  const q = input.trim();
  const isLower = gradeBand === 'LOWER_GRADE';

  if (q.includes('作业') || q.includes('任务')) {
    return {
      replyText: isLower
        ? '今天有语文背诵听写和英语课堂词汇读书任务！点开任何一条就能看老师发的全部内容和图片，也可以直接对我说“开始打开语文作业”开启录音打卡~'
        : '已为你展开今日任务卡片。点击任一作业即可查看完整原文、复习页码提醒和课本配图，或说“开始打开XX作业”启动语音打卡。',
      companionState: 'encouraging',
      cardType: 'TASK_LIST',
    };
  }
  if (q.includes('时间') || q.includes('计划') || q.includes('安排')) {
    return {
      replyText: isLower
        ? '这是今天的作息时间表！黄色标注的是“家长待确认草案”，我们也可以调整顺序哦。'
        : '已为你打开今日时间规划表。标有“草案”的时段还需家长最终确认，检测到晚间时段有一处紧凑冲突提示。',
      companionState: 'thinking',
      cardType: 'TIME_PLAN',
    };
  }
  if (q.includes('目标') || q.includes('科学家') || q.includes('榜样') || q.includes('成为')) {
    return {
      replyText: isLower
        ? '想成为科学家真是太棒的目标啦！我已经帮你拆成每天 15 分钟的小行动，我们一起来打卡！'
        : '这是一个非常扎实的目标。我已经根据你的榜样与兴趣拆解了可执行步骤，随时可以更新本周行动和复盘。',
      companionState: 'happy',
      cardType: 'GROWTH_GOAL',
    };
  }
  if (q.includes('愿望') || q.includes('协商') || q.includes('支持')) {
    return {
      replyText: '我帮你把愿望整理成了清楚的家庭协商单，包含需要家长支持的事项和预算计划。提交家长审批通过后才会生效哦。',
      companionState: 'speaking',
      cardType: 'WISH_NEGOTIATION',
    };
  }
  if (q.includes('积分') || q.includes('奖励') || q.includes('兑换')) {
    return {
      replyText: '你现在一共有 28 分！积分由服务端记录，不能在手机上随意改动。下面是可以向家长申请兑换的奖励清单：',
      companionState: 'celebrating',
      cardType: 'POINTS_REWARDS',
    };
  }
  if (q.includes('朗读') || q.includes('录音')) {
    return {
      replyText: '准备好朗读录音了吗？既可以点击下方卡片直接录制，也可以对我说“开始打开英语作业”，确认后进入打卡录音模式，说完说“打卡完毕”自动同步云端！',
      companionState: 'listening',
      cardType: 'ENGLISH_READING',
    };
  }
  return {
    replyText: isLower
      ? `嗯嗯，${companionName}认真听完啦！关于“${q}”，你想让我用十万个为什么的方式讲给你听，还是先看看今天的语文英语作业呀？`
      : `收到，“${q}”是一个很值得思考的话题。我们可以先梳理核心原因，也可以随时说“开始打开XX作业”开启语音打卡。`,
    companionState: 'speaking',
  };
}
