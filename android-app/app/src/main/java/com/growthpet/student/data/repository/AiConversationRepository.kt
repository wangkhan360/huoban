package com.growthpet.student.data.repository

import com.growthpet.student.data.local.*
import com.growthpet.student.data.remote.AiStructuredResponse
import com.growthpet.student.data.remote.ChatRequestDto
import com.growthpet.student.data.remote.DynamicCardPayload
import com.growthpet.student.data.remote.StudentApiService
import com.growthpet.student.data.remote.VerifyBindingRequestDto
import com.growthpet.student.data.remote.VerifyBindingResponseDto
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

interface AiConversationRepository {
    val isMockMode: Boolean
    suspend fun verifyParentCode(code: String, deviceId: String): Result<VerifyBindingResponseDto>
    suspend fun seedInitialChildWorkspace(
        profile: ChildProfileEntity,
        welcomeGreeting: String
    )
    suspend fun sendStudentMessage(
        childId: String,
        gradeBand: String,
        companionName: String,
        message: String
    ): Result<AiStructuredResponse>
    fun observeMessages(childId: String): Flow<List<ChatMessageEntity>>
    fun observeTasks(childId: String): Flow<List<TaskItemEntity>>
    fun observeSchedule(childId: String): Flow<List<ScheduleSlotEntity>>
    fun observeGoals(childId: String): Flow<List<GrowthGoalEntity>>
    fun observeWishes(childId: String): Flow<List<FamilyWishEntity>>
    fun observePointsRecords(childId: String): Flow<List<PointsRecordEntity>>
    fun observeRewards(childId: String): Flow<List<RewardItemEntity>>
    fun observeRecordings(childId: String): Flow<List<ReadingRecordingEntity>>
    suspend fun updateTask(task: TaskItemEntity)
    suspend fun confirmScheduleByChild(childId: String)
    suspend fun reorderSchedule(slots: List<ScheduleSlotEntity>)
    suspend fun updateGoal(goal: GrowthGoalEntity)
    suspend fun submitWishToParent(wish: FamilyWishEntity)
    suspend fun requestRewardRedemption(childId: String, reward: RewardItemEntity, currentPoints: Int): Result<String>
    suspend fun saveRecording(recording: ReadingRecordingEntity)
    suspend fun insertConversationMessage(
        childId: String,
        role: String,
        text: String,
        intent: String? = null,
        companionState: String? = null,
        cardsJson: String? = null
    )
}

@Singleton
class MockAiConversationRepository @Inject constructor(
    private val dao: StudentDao,
    @Suppress("unused") private val remoteApi: StudentApiService
) : AiConversationRepository {

    override val isMockMode: Boolean = true

    override suspend fun verifyParentCode(code: String, deviceId: String): Result<VerifyBindingResponseDto> {
        delay(450)
        val trimmed = code.trim()
        return when {
            trimmed == "1012" || trimmed == "1111" -> Result.success(
                VerifyBindingResponseDto(
                    childId = "child_grade1_linlin",
                    childName = "乐乐",
                    gradeLevel = 1,
                    gradeBand = "LOWER_GRADE",
                    parentName = "乐乐爸爸"
                )
            )
            trimmed == "6068" || trimmed == "6666" -> Result.success(
                VerifyBindingResponseDto(
                    childId = "child_grade6_ziyuan",
                    childName = "思远",
                    gradeLevel = 6,
                    gradeBand = "UPPER_GRADE",
                    parentName = "思远妈妈"
                )
            )
            trimmed.length >= 4 -> Result.success(
                VerifyBindingResponseDto(
                    childId = "child_custom_$trimmed",
                    childName = "小宇",
                    gradeLevel = 3,
                    gradeBand = "LOWER_GRADE",
                    parentName = "家长"
                )
            )
            else -> Result.failure(IllegalArgumentException("请输入4位以上有效家长核验码（演示码：1012 一年级 / 6068 六年级）"))
        }
    }

    override suspend fun seedInitialChildWorkspace(profile: ChildProfileEntity, welcomeGreeting: String) {
        val cid = profile.childId
        val now = System.currentTimeMillis()
        dao.upsertProfile(profile)
        dao.clearMessages(cid)
        dao.insertMessage(
            ChatMessageEntity(
                id = UUID.randomUUID().toString(),
                childId = cid,
                role = "AI",
                text = welcomeGreeting,
                intent = "GREETING",
                companionState = "happy",
                cardsJson = null,
                timestamp = now
            )
        )

        val isLower = profile.gradeBand == "LOWER_GRADE"
        dao.upsertTasks(
            listOf(
                TaskItemEntity(
                    id = "task_chinese_$cid",
                    childId = cid,
                    title = "语文周末背诵与听写作业",
                    subject = "语文",
                    source = "SCHOOL",
                    estimatedMinutes = 20,
                    elapsedSeconds = 0,
                    status = "TODO",
                    pointsReward = 6,
                    feedbackNote = null,
                    detailContent = "@所有人 周末提示：\n语文\n《秋天》《江南》背熟\n64页《雪地里的小画家》读3遍\n听写以下词语（带拼音）：可口、叶子、东西、大火、人手、禾田\n每天练习跳绳 · 坚持每日读课外书 · 练习魔方（自愿）",
                    teacherReminder = "先大声朗读再背诵，听写时注意声调准确。",
                    imageResName = "img_homework_chinese",
                    updatedAt = now
                ),
                TaskItemEntity(
                    id = "task_english_$cid",
                    childId = cid,
                    title = "今日英语学习任务：课堂词汇与不漏页读书",
                    subject = "英语",
                    source = "SCHOOL",
                    estimatedMinutes = 15,
                    elapsedSeconds = 0,
                    status = "TODO",
                    pointsReward = 6,
                    feedbackNote = null,
                    detailContent = "今日英语学习任务\n1. 复习课堂重点内容，认真看同学的示范视频练习记忆：\nclassroom 教室 · blackboard 黑板 · open the window 打开窗户 · close the window 关闭窗户 · open the door 打开门 · close the door 关门\n2. 熟读复习书 P1-18，P31-36，P47，强调不漏页读书。",
                    teacherReminder = "小提醒：在本周做习题和读课文过程中，发现孩子对已学习知识有遗忘现象，请家长务必提醒孩子不漏页读书，战胜遗忘。",
                    imageResName = "img_homework_english",
                    updatedAt = now + 1
                ),
                TaskItemEntity(
                    id = "task_home_$cid",
                    childId = cid,
                    title = if (isLower) "每天跳绳练习与整理小书包" else "每日跳绳打卡与书桌整理",
                    subject = "体育与习惯",
                    source = "HOME_ROUTINE",
                    estimatedMinutes = 15,
                    elapsedSeconds = 0,
                    status = "COMPLETED",
                    pointsReward = 3,
                    feedbackNote = "已经按要求完成",
                    detailContent = "每天练习跳绳 · 坚持每日读课外书 · 练习魔方（自愿）并整理书包。",
                    teacherReminder = null,
                    imageResName = null,
                    updatedAt = now + 2
                )
            )
        )

        dao.upsertSchedule(
            listOf(
                ScheduleSlotEntity("slot_1_$cid", cid, "MORNING", "07:20", "07:45", "起床整理与营养早餐", isParentConfirmed = true, isChildConfirmed = true, hasConflict = false, sortOrder = 1),
                ScheduleSlotEntity("slot_2_$cid", cid, "HOMEWORK", "16:40", "17:15", "专心完成数学与语文练习", isParentConfirmed = true, isChildConfirmed = false, hasConflict = false, sortOrder = 2),
                ScheduleSlotEntity("slot_3_$cid", cid, "REST", "17:15", "17:45", "户外跳绳与眼部放松", isParentConfirmed = false, isChildConfirmed = false, hasConflict = false, sortOrder = 3),
                ScheduleSlotEntity("slot_4_$cid", cid, "EVENING", "19:30", "19:50", "英语朗读录音 + 自由阅读", isParentConfirmed = false, isChildConfirmed = false, hasConflict = true, sortOrder = 4)
            )
        )

        dao.upsertGoal(
            GrowthGoalEntity(
                id = "goal_main_$cid",
                childId = cid,
                aspiration = if (isLower) "成为一名探索太空的航天科学家" else "成为一名懂航空工程的系统设计师",
                roleModel = "钱学森",
                admiredQualities = "专注钻研、坚持到底、热爱祖国",
                targetSkills = "观察实验、数学推理、清晰表达",
                stepsJson = "每天阅读15分钟科普绘本/资料|每周记录1个生活中的科学为什么|周末和家长做1次小实验",
                weeklyAction = "本周完成一次光的折射小实验并讲给云龙听",
                reflectionNote = "周二晚上自己拆解了一道难题，比上周更快了。",
                progressPercent = 45
            )
        )

        dao.upsertWish(
            FamilyWishEntity(
                id = "wish_1_$cid",
                childId = cid,
                wishTitle = if (isLower) "想要一套儿童天文望远镜入门盒" else "希望周末去科技馆参加航天模型工作坊",
                parentSupportNeeded = "需要爸爸妈妈周末抽2小时陪同，并在预算范围内安排体验",
                structuredSummary = "愿望诉求：拓展光学与航天兴趣；自律约定：连续完成5天按时作业",
                budgetNote = "约 ¥120 - ¥180（可先用积分抵扣部分）",
                timelinePlan = "本周末周六下午 14:00 - 16:00",
                approvalStatus = "PENDING_PARENT",
                parentFeedback = "妈妈已收到申请，正在查看周六日程"
            )
        )

        dao.upsertPointsRecords(
            listOf(
                PointsRecordEntity("pts_1_$cid", cid, 15, "本周连续三天按时开始作业", now - 86400000),
                PointsRecordEntity("pts_2_$cid", cid, 8, "主动完成家庭整理任务", now - 43200000),
                PointsRecordEntity("pts_3_$cid", cid, 5, "科学问题探索积极提问", now - 3600000)
            )
        )

        dao.upsertRewards(
            listOf(
                RewardItemEntity("rw_1_$cid", cid, "和爸爸妈妈一起夜间观星 40 分钟", "COMPANIONSHIP", 15, "周末晚饭后一起去楼顶或公园看星星", "AVAILABLE", null),
                RewardItemEntity("rw_2_$cid", cid, "周六自主选择晚饭菜单特权", "PLAY_PRIVILEGE", 20, "自己当家庭小主厨搭配一顿晚餐", "AVAILABLE", null),
                RewardItemEntity("rw_3_$cid", cid, "周末科普实践 / 科技体验门票", "EXPERIENCE", 35, "需家长确认周末时间后安排出行", "PENDING_PARENT", null),
                RewardItemEntity("rw_4_$cid", cid, "儿童光学棱镜与天文实验盒", "PHYSICAL", 25, "包含三棱镜、观测笔记本与迷你望远镜", "AVAILABLE", null)
            )
        )
    }

    override suspend fun sendStudentMessage(
        childId: String,
        gradeBand: String,
        companionName: String,
        message: String
    ): Result<AiStructuredResponse> {
        val now = System.currentTimeMillis()
        dao.insertMessage(
            ChatMessageEntity(
                id = UUID.randomUUID().toString(),
                childId = childId,
                role = "CHILD",
                text = message,
                intent = null,
                companionState = null,
                cardsJson = null,
                timestamp = now
            )
        )

        delay(420)
        val isLower = gradeBand == "LOWER_GRADE"
        val lowerMsg = message.lowercase()

        val response = when {
            lowerMsg.contains("作业") || lowerMsg.contains("任务") || lowerMsg.contains("做什么") -> AiStructuredResponse(
                replyText = if (isLower)
                    "今天还有数学口算和英语跟读两项小任务哦！我们挑一个最想先开始的吧，做完可以攒小星星~"
                else
                    "今天待完成有 2 项任务：数学分数应用题和英语 Unit 4 朗读提交。你打算先攻克哪一项？我可以帮你计时。",
                intent = "QUERY_TASKS",
                companionState = "encouraging",
                cards = listOf(DynamicCardPayload("TASK_LIST", "今日任务清单")),
                suggestedActions = listOf("开始数学任务", "去英语朗读录音", "看看今日时间表")
            )

            lowerMsg.contains("时间") || lowerMsg.contains("计划") || lowerMsg.contains("安排") -> AiStructuredResponse(
                replyText = if (isLower)
                    "这是今天的作息小计划！注意晚上有一小段安排还没和爸爸妈妈最终确认，标记成草案啦，我们一起看看顺不顺手。"
                else
                    "已为你调出今日时间规划。其中傍晚休息与晚间阅读时段为“家长待确认草案”，你可以先调整顺序或确认你的建议版本。",
                intent = "PLAN_TIME",
                companionState = "thinking",
                cards = listOf(DynamicCardPayload("TIME_PLAN", "今日时间规划")),
                suggestedActions = listOf("确认我的建议计划", "把英语放到17:45", "查看今日任务")
            )

            lowerMsg.contains("目标") || lowerMsg.contains("科学家") || lowerMsg.contains("想成为") || lowerMsg.contains("榜样") -> AiStructuredResponse(
                replyText = if (isLower)
                    "哇，想当科学家太酷啦！我们可以从每天看一小页科学书、做个小实验开始，我陪你一步一步实现！"
                else
                    "很棒的长远志向！把大目标拆解成可执行的每周小行动，最容易看到进步。这是你的成长目标卡和本周复盘。",
                intent = "GROWTH_GOAL",
                companionState = "happy",
                cards = listOf(DynamicCardPayload("GROWTH_GOAL", "我的成长目标卡")),
                suggestedActions = listOf("更新本周行动", "记录今天复盘", "聊聊我的榜样")
            )

            lowerMsg.contains("愿望") || lowerMsg.contains("协商") || lowerMsg.contains("爸爸") || lowerMsg.contains("妈妈") -> AiStructuredResponse(
                replyText = "我已经帮你把愿望整理成了条理清晰的家庭协商单。记住哦，提交给爸爸妈妈批准后才算正式生效。",
                intent = "WISH_NEGOTIATION",
                companionState = "speaking",
                cards = listOf(DynamicCardPayload("WISH_NEGOTIATION", "愿望与家庭协商")),
                suggestedActions = listOf("提交家长确认", "修改预算说明", "看看我的积分")
            )

            lowerMsg.contains("积分") || lowerMsg.contains("奖励") || lowerMsg.contains("兑换") -> AiStructuredResponse(
                replyText = "你目前一共有 28 分！积分由系统统一记录，我们可以先浏览奖励商店，挑中心仪的再向家长发起兑换申请。",
                intent = "QUERY_POINTS_REWARDS",
                companionState = "celebrating",
                cards = listOf(DynamicCardPayload("POINTS_REWARDS", "积分与奖励商店")),
                suggestedActions = listOf("兑换天文实验盒", "查看积分明细", "继续做任务攒分")
            )

            lowerMsg.contains("朗读") || lowerMsg.contains("英语") || lowerMsg.contains("录音") -> AiStructuredResponse(
                replyText = "准备好英语朗读了吗？点击下方录音卡片里的“授权并开始”，我会安静听你读完，还能先试听再提交！",
                intent = "ENGLISH_READING",
                companionState = "listening",
                cards = listOf(DynamicCardPayload("ENGLISH_READING", "英语朗读录音")),
                suggestedActions = listOf("开始朗读录音", "查看已交录音")
            )

            else -> AiStructuredResponse(
                replyText = if (isLower)
                    "嗯嗯，$companionName 听到了！“$message”真有意思，你想聊聊作业、规划时间，还是问我一个《十万个为什么》的问题呀？"
                else
                    "收到你的想法！关于“$message”，我们可以继续深聊背后的原理，或者把它和你的目标、今日计划结合起来。",
                intent = "GENERAL_CHAT",
                companionState = "speaking",
                cards = emptyList(),
                suggestedActions = listOf("我今天有什么作业", "查看时间规划", "我有多少积分", "英语朗读录音")
            )
        }

        val cardsSerialized = response.cards.joinToString(",") { "${it.cardType}:${it.title}" }
        dao.insertMessage(
            ChatMessageEntity(
                id = UUID.randomUUID().toString(),
                childId = childId,
                role = "AI",
                text = response.replyText,
                intent = response.intent,
                companionState = response.companionState,
                cardsJson = cardsSerialized.ifBlank { null },
                timestamp = System.currentTimeMillis()
            )
        )

        return Result.success(response)
    }

    override fun observeMessages(childId: String) = dao.observeMessages(childId)
    override fun observeTasks(childId: String) = dao.observeTasks(childId)
    override fun observeSchedule(childId: String) = dao.observeSchedule(childId)
    override fun observeGoals(childId: String) = dao.observeGoals(childId)
    override fun observeWishes(childId: String) = dao.observeWishes(childId)
    override fun observePointsRecords(childId: String) = dao.observePointsRecords(childId)
    override fun observeRewards(childId: String) = dao.observeRewards(childId)
    override fun observeRecordings(childId: String) = dao.observeRecordings(childId)

    override suspend fun updateTask(task: TaskItemEntity) {
        dao.upsertTask(task)
    }

    override suspend fun confirmScheduleByChild(childId: String) {
        // Handled via ViewModel snapshot update
    }

    override suspend fun reorderSchedule(slots: List<ScheduleSlotEntity>) {
        dao.upsertSchedule(slots)
    }

    override suspend fun updateGoal(goal: GrowthGoalEntity) {
        dao.upsertGoal(goal)
    }

    override suspend fun submitWishToParent(wish: FamilyWishEntity) {
        dao.upsertWish(wish.copy(approvalStatus = "PENDING_PARENT"))
    }

    override suspend fun requestRewardRedemption(
        childId: String,
        reward: RewardItemEntity,
        currentPoints: Int
    ): Result<String> {
        if (currentPoints < reward.costPoints) {
            return Result.failure(IllegalStateException("积分还差 ${reward.costPoints - currentPoints} 分，完成后再来申请吧"))
        }
        dao.upsertReward(reward.copy(redeemStatus = "PENDING_PARENT"))
        return Result.success("已提交家长确认，审核通过后生效")
    }

    override suspend fun saveRecording(recording: ReadingRecordingEntity) {
        dao.upsertRecording(recording)
    }

    override suspend fun insertConversationMessage(
        childId: String,
        role: String,
        text: String,
        intent: String?,
        companionState: String?,
        cardsJson: String?
    ) {
        dao.insertMessage(
            ChatMessageEntity(
                id = UUID.randomUUID().toString(),
                childId = childId,
                role = role,
                text = text,
                intent = intent,
                companionState = companionState,
                cardsJson = cardsJson,
                timestamp = System.currentTimeMillis()
            )
        )
    }
}
