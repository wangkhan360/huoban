package com.growthpet.student.ui.cards

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.growthpet.student.R
import com.growthpet.student.data.local.*
import com.growthpet.student.ui.theme.*
import kotlinx.coroutines.delay

import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun TaskManagementCard(
    tasks: List<TaskItemEntity>,
    isLowerGrade: Boolean,
    onUpdateTask: (TaskItemEntity, String, String?) -> Unit,
    onStartVoiceCheckin: (TaskItemEntity) -> Unit = {}
) {
    var activeTaskId by remember { mutableStateOf<String?>(null) }
    var elapsedSec by remember { mutableIntStateOf(0) }
    var selectedDetailTask by remember { mutableStateOf<TaskItemEntity?>(null) }

    LaunchedEffect(activeTaskId) {
        while (activeTaskId != null) {
            delay(1000)
            elapsedSec++
        }
    }

    val completedCount = tasks.count { it.status == "COMPLETED" }

    Surface(
        shape = CardStyle.shape,
        border = CardStyle.border,
        color = ColorTokens.SurfaceCard,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(CardStyle.innerPadding)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(if (isLowerGrade) "今天的小任务" else "今日任务清单", style = MaterialTheme.typography.titleMedium)
                Text(
                    "已完成 $completedCount/${tasks.size}",
                    style = MaterialTheme.typography.labelMedium,
                    color = ColorTokens.SuccessMint
                )
            }
            Spacer(modifier = Modifier.height(Spacing.sm))

            tasks.forEach { task ->
                val isDone = task.status == "COMPLETED"
                val isRunning = activeTaskId == task.id
                val sourceLabel = when (task.source) {
                    "SCHOOL" -> "学校"
                    "PARENT" -> "家长"
                    else -> "家庭日常"
                }
                val drawableId = when (task.imageResName) {
                    "img_homework_chinese" -> R.drawable.img_homework_chinese
                    "img_homework_english" -> R.drawable.img_homework_english
                    else -> null
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Radius.sm))
                        .clickable { selectedDetailTask = task }
                        .padding(vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (drawableId != null) {
                            Image(
                                painter = painterResource(id = drawableId),
                                contentDescription = task.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(Radius.sm))
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(task.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                            Text(
                                "${task.subject} · $sourceLabel · 预计 ${task.estimatedMinutes} 分钟 · +${task.pointsReward}分",
                                style = MaterialTheme.typography.labelMedium
                            )
                            Text(
                                "点击查看作业内容与图片",
                                style = MaterialTheme.typography.labelSmall,
                                color = ColorTokens.TechBluePrimary
                            )
                        }
                        if (isRunning) {
                            Text(
                                String.format("%02d:%02d", elapsedSec / 60, elapsedSec % 60),
                                style = MaterialTheme.typography.titleMedium,
                                color = ColorTokens.TechBluePrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                        if (!isDone) {
                            Button(
                                onClick = {
                                    if (isRunning) {
                                        activeTaskId = null
                                        onUpdateTask(task, "COMPLETED", "专注完成用时 ${elapsedSec}秒")
                                        elapsedSec = 0
                                    } else {
                                        activeTaskId = task.id
                                        elapsedSec = 0
                                        onUpdateTask(task, "IN_PROGRESS", null)
                                    }
                                },
                                modifier = Modifier.height(40.dp),
                                shape = RoundedCornerShape(Radius.sm)
                            ) {
                                Text(if (isRunning) "完成任务" else "开始计时")
                            }
                            OutlinedButton(
                                onClick = { onStartVoiceCheckin(task) },
                                modifier = Modifier.height(40.dp),
                                shape = RoundedCornerShape(Radius.sm)
                            ) { Text("语音打卡") }
                            OutlinedButton(
                                onClick = { selectedDetailTask = task },
                                modifier = Modifier.height(40.dp),
                                shape = RoundedCornerShape(Radius.sm)
                            ) { Text("查看详情") }
                        } else {
                            Text("已完成 · ${task.feedbackNote ?: "表现很棒"}", color = ColorTokens.SuccessMint, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    HorizontalDivider(color = ColorTokens.BorderHairline)
                }
            }

            if (completedCount == tasks.size && tasks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(Spacing.xs))
                Text(
                    "今日任务全部完成！今日总结：专注力稳定，共获得 ${tasks.sumOf { it.pointsReward }} 分记录。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ColorTokens.SuccessMint
                )
            }
        }
    }

    selectedDetailTask?.let { task ->
        val detailDrawableId = when (task.imageResName) {
            "img_homework_chinese" -> R.drawable.img_homework_chinese
            "img_homework_english" -> R.drawable.img_homework_english
            else -> null
        }
        AlertDialog(
            onDismissRequest = { selectedDetailTask = null },
            title = { Text("${task.subject} · ${task.title}") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    Text(
                        "预计 ${task.estimatedMinutes} 分钟 · +${task.pointsReward}分",
                        style = MaterialTheme.typography.labelMedium,
                        color = ColorTokens.InkSecondary
                    )
                    if (task.detailContent.isNotBlank()) {
                        Surface(
                            color = ColorTokens.CanvasIce,
                            shape = RoundedCornerShape(Radius.sm),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = task.detailContent,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(Spacing.sm)
                            )
                        }
                    }
                    if (!task.teacherReminder.isNullOrBlank()) {
                        Surface(
                            color = ColorTokens.SkyHaloLight,
                            shape = RoundedCornerShape(Radius.sm),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "老师小提醒：${task.teacherReminder}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = ColorTokens.InkPrimary,
                                modifier = Modifier.padding(Spacing.sm)
                            )
                        }
                    }
                    if (detailDrawableId != null) {
                        Text("作业参考图片：", style = MaterialTheme.typography.labelMedium)
                        Image(
                            painter = painterResource(id = detailDrawableId),
                            contentDescription = task.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(Radius.md))
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                        OutlinedButton(
                            onClick = {
                                onUpdateTask(task, "HELP_REQUESTED", "孩子发起任务求助")
                                selectedDetailTask = null
                            }
                        ) { Text("发起求助") }
                        OutlinedButton(
                            onClick = {
                                onUpdateTask(task, "TIME_ADJUST_REQUESTED", "申请延后至 19:30 完成")
                                selectedDetailTask = null
                            }
                        ) { Text("申请调整时间") }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    selectedDetailTask = null
                    onStartVoiceCheckin(task)
                }) { Text("开启语音打卡") }
            },
            dismissButton = {
                TextButton(onClick = { selectedDetailTask = null }) { Text("关闭") }
            }
        )
    }
}

@Composable
fun TimePlanningCard(
    slots: List<ScheduleSlotEntity>,
    onMoveUp: (Int) -> Unit,
    onConfirmPlan: () -> Unit
) {
    val hasDraft = slots.any { !it.isParentConfirmed }
    val hasConflict = slots.any { it.hasConflict }

    Surface(
        shape = CardStyle.shape,
        border = CardStyle.border,
        color = ColorTokens.SurfaceCard,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(CardStyle.innerPadding)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("今日时间规划表", style = MaterialTheme.typography.titleMedium)
                if (hasDraft) {
                    Text(
                        "含家长待确认草案",
                        style = MaterialTheme.typography.labelMedium,
                        color = ColorTokens.WarningAmber
                    )
                }
            }

            if (hasConflict) {
                Spacer(modifier = Modifier.height(Spacing.xs))
                Text(
                    "时间冲突提示：晚间朗读与阅读安排较紧凑，建议提前或顺延10分钟。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ColorTokens.DangerCoral
                )
            }

            Spacer(modifier = Modifier.height(Spacing.sm))
            slots.forEachIndexed { index, slot ->
                val periodText = when (slot.period) {
                    "MORNING" -> "早晨计划"
                    "HOMEWORK" -> "作业时间"
                    "REST" -> "休息时间"
                    else -> "晚间计划"
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${slot.startTime}-${slot.endTime} · $periodText", style = MaterialTheme.typography.labelMedium)
                        Text(slot.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        Text(
                            if (slot.isParentConfirmed) "家长已确认" else "草案（家长尚未最终确认）",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (slot.isParentConfirmed) ColorTokens.SuccessMint else ColorTokens.DraftSlate
                        )
                    }
                    if (index > 0) {
                        OutlinedButton(onClick = { onMoveUp(index) }, modifier = Modifier.height(38.dp)) {
                            Text("上移")
                        }
                    }
                }
                HorizontalDivider(color = ColorTokens.BorderHairline)
            }

            Spacer(modifier = Modifier.height(Spacing.sm))
            Button(
                onClick = onConfirmPlan,
                modifier = Modifier.fillMaxWidth().height(ButtonStyle.heightCompact),
                shape = ButtonStyle.shape
            ) {
                Text("我确认这份今日时间表")
            }
        }
    }
}

@Composable
fun GrowthGoalCard(
    goals: List<GrowthGoalEntity>,
    onUpdateGoal: (GrowthGoalEntity, String, String) -> Unit
) {
    val goal = goals.firstOrNull() ?: return
    var weeklyInput by remember(goal.id) { mutableStateOf(goal.weeklyAction) }
    var reflectionInput by remember(goal.id) { mutableStateOf(goal.reflectionNote) }

    Surface(
        shape = CardStyle.shape,
        border = CardStyle.border,
        color = ColorTokens.SurfaceCard,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(CardStyle.innerPadding), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("成长目标与榜样拆解", style = MaterialTheme.typography.titleMedium)
                Text("${goal.progressPercent}%", color = ColorTokens.TechBluePrimary, fontWeight = FontWeight.Bold)
            }
            LinearProgressIndicator(
                progress = { goal.progressPercent / 100f },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(Radius.pill))
            )
            Text("我想成为：${goal.aspiration}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text("我的榜样：${goal.roleModel}（欣赏品质：${goal.admiredQualities}）", style = MaterialTheme.typography.bodyMedium)
            Text("想学习的能力：${goal.targetSkills}", style = MaterialTheme.typography.bodyMedium)
            HorizontalDivider(color = ColorTokens.BorderHairline)
            Text("AI 拆解行动步骤：", style = MaterialTheme.typography.labelMedium)
            goal.stepsJson.split("|").forEachIndexed { idx, step ->
                Text("${idx + 1}. $step", style = MaterialTheme.typography.bodyMedium)
            }
            OutlinedTextField(
                value = weeklyInput,
                onValueChange = { weeklyInput = it },
                label = { Text("本周行动") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = reflectionInput,
                onValueChange = { reflectionInput = it },
                label = { Text("成长复盘") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = { onUpdateGoal(goal, reflectionInput, weeklyInput) },
                modifier = Modifier.fillMaxWidth().height(ButtonStyle.heightCompact),
                shape = ButtonStyle.shape
            ) {
                Text("保存本周进展与复盘")
            }
        }
    }
}

@Composable
fun WishNegotiationCard(
    wishes: List<FamilyWishEntity>,
    onSubmitWish: (FamilyWishEntity) -> Unit
) {
    val wish = wishes.firstOrNull() ?: return
    var wishTitle by remember(wish.id) { mutableStateOf(wish.wishTitle) }
    var budget by remember(wish.id) { mutableStateOf(wish.budgetNote) }

    val statusText = when (wish.approvalStatus) {
        "APPROVED" -> "家长已批准"
        "REJECTED" -> "暂未通过（待调整）"
        "PENDING_PARENT" -> "等待家长确认中（未批准前不视为已获得）"
        else -> "整理草案中"
    }

    Surface(
        shape = CardStyle.shape,
        border = CardStyle.border,
        color = ColorTokens.SurfaceCard,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(CardStyle.innerPadding), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            Text("愿望与家庭协商单", style = MaterialTheme.typography.titleMedium)
            Text("状态：$statusText", style = MaterialTheme.typography.labelMedium, color = ColorTokens.WarningAmber)
            OutlinedTextField(
                value = wishTitle,
                onValueChange = { wishTitle = it },
                label = { Text("我的愿望") },
                modifier = Modifier.fillMaxWidth()
            )
            Text("希望家长支持：${wish.parentSupportNeeded}", style = MaterialTheme.typography.bodyMedium)
            Text("AI 整理需求：${wish.structuredSummary}", style = MaterialTheme.typography.bodyMedium)
            OutlinedTextField(
                value = budget,
                onValueChange = { budget = it },
                label = { Text("资源与预算说明") },
                modifier = Modifier.fillMaxWidth()
            )
            Text("时间计划：${wish.timelinePlan}", style = MaterialTheme.typography.bodyMedium)
            if (!wish.parentFeedback.isNullOrBlank()) {
                Text("家长反馈：${wish.parentFeedback}", style = MaterialTheme.typography.bodyMedium, color = ColorTokens.TechBluePrimary)
            }
            Button(
                onClick = { onSubmitWish(wish.copy(wishTitle = wishTitle, budgetNote = budget)) },
                modifier = Modifier.fillMaxWidth().height(ButtonStyle.heightCompact),
                shape = ButtonStyle.shape
            ) {
                Text("提交家长确认")
            }
        }
    }
}

@Composable
fun PointsRewardsCard(
    totalPoints: Int,
    records: List<PointsRecordEntity>,
    rewards: List<RewardItemEntity>,
    onRequestRedeem: (RewardItemEntity) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("ALL") }
    val categories = listOf("ALL" to "全部", "COMPANIONSHIP" to "陪伴", "PLAY_PRIVILEGE" to "玩乐特权", "EXPERIENCE" to "兴趣体验", "PHYSICAL" to "实物")
    val filtered = if (selectedCategory == "ALL") rewards else rewards.filter { it.category == selectedCategory }

    Surface(
        shape = CardStyle.shape,
        border = CardStyle.border,
        color = ColorTokens.SurfaceCard,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(CardStyle.innerPadding), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("积分与奖励商店", style = MaterialTheme.typography.titleMedium)
                Text("当前 $totalPoints 分（服务端校验）", color = ColorTokens.TechBluePrimary, fontWeight = FontWeight.Bold)
            }
            Text("最近积分记录：", style = MaterialTheme.typography.labelMedium)
            records.take(2).forEach { rec ->
                Text("+${rec.delta} 分 · ${rec.reason}", style = MaterialTheme.typography.bodyMedium)
            }

            HorizontalDivider(color = ColorTokens.BorderHairline)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                categories.take(3).forEach { (key, label) ->
                    OutlinedButton(
                        onClick = { selectedCategory = key },
                        modifier = Modifier.height(36.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp)
                    ) { Text(label, style = MaterialTheme.typography.labelMedium) }
                }
            }

            filtered.forEach { reward ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    if (reward.category == "PHYSICAL") {
                        Image(
                            painter = painterResource(id = R.drawable.img_reward_science_kit),
                            contentDescription = reward.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(56.dp).clip(RoundedCornerShape(Radius.sm))
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(reward.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        Text("${reward.costPoints} 分 · ${reward.description}", style = MaterialTheme.typography.labelMedium)
                        val st = when (reward.redeemStatus) {
                            "PENDING_PARENT" -> "等待家长确认中"
                            "APPROVED" -> "兑换成功"
                            "REJECTED" -> "未通过：${reward.rejectReason ?: "暂不适合"}"
                            else -> "可申请兑换"
                        }
                        Text(st, style = MaterialTheme.typography.labelMedium, color = ColorTokens.InkSecondary)
                    }
                    if (reward.redeemStatus == "AVAILABLE") {
                        Button(
                            onClick = { onRequestRedeem(reward) },
                            modifier = Modifier.height(38.dp),
                            shape = RoundedCornerShape(Radius.sm)
                        ) { Text("申请兑换") }
                    }
                }
                HorizontalDivider(color = ColorTokens.BorderHairline)
            }
        }
    }
}

@Composable
fun EnglishReadingCard(
    micAuthorized: Boolean,
    isRecording: Boolean,
    isPlayingPreview: Boolean,
    onAuthorizeMic: () -> Unit,
    onStartRecord: () -> Unit,
    onStopRecord: () -> Unit,
    onPlayPreview: () -> Unit,
    onSubmitRecording: (title: String, durationSeconds: Int) -> Unit
) {
    var seconds by remember { mutableIntStateOf(0) }
    var hasRecordedClip by remember { mutableStateOf(false) }
    var uploadState by remember { mutableStateOf("IDLE") } // IDLE, UPLOADING, SUCCESS, FAILED

    LaunchedEffect(isRecording) {
        while (isRecording) {
            delay(1000)
            seconds++
        }
    }

    Surface(
        shape = CardStyle.shape,
        border = CardStyle.border,
        color = ColorTokens.SurfaceCard,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(CardStyle.innerPadding), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            Text("英语朗读录音 · My Sunny Day", style = MaterialTheme.typography.titleMedium)
            Text(
                "朗读提示：\"The sun is bright today. I walk to school with my friend and look at the blue sky.\"",
                style = MaterialTheme.typography.bodyMedium
            )
            if (!micAuthorized) {
                Text("为保护隐私，录音前必须先由你主动点击授权麦克风。", style = MaterialTheme.typography.labelMedium, color = ColorTokens.WarningAmber)
                Button(
                    onClick = onAuthorizeMic,
                    modifier = Modifier.fillMaxWidth().height(ButtonStyle.heightCompact),
                    shape = ButtonStyle.shape
                ) { Text("我已准备好，授权开启录音") }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        when {
                            isRecording -> "正在录音中..."
                            hasRecordedClip -> "已完成录制，可先试听"
                            else -> "已授权，点击开始朗读"
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        String.format("%02d:%02d", seconds / 60, seconds % 60),
                        style = MaterialTheme.typography.titleMedium,
                        color = ColorTokens.TechBluePrimary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                    if (!isRecording) {
                        Button(
                            onClick = {
                                seconds = 0
                                hasRecordedClip = false
                                uploadState = "IDLE"
                                onStartRecord()
                            },
                            modifier = Modifier.weight(1f).height(44.dp)
                        ) { Text(if (hasRecordedClip) "重新录制" else "开始录音") }
                    } else {
                        Button(
                            onClick = {
                                onStopRecord()
                                hasRecordedClip = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ColorTokens.DangerCoral),
                            modifier = Modifier.weight(1f).height(44.dp)
                        ) { Text("停止录音") }
                    }

                    if (hasRecordedClip && !isRecording) {
                        OutlinedButton(
                            onClick = onPlayPreview,
                            modifier = Modifier.weight(1f).height(44.dp)
                        ) { Text(if (isPlayingPreview) "试听中..." else "播放试听") }
                    }
                }

                if (hasRecordedClip && !isRecording) {
                    Button(
                        onClick = {
                            uploadState = "SUCCESS"
                            onSubmitRecording("Unit 4 - My Sunny Day", seconds.coerceAtLeast(3))
                        },
                        modifier = Modifier.fillMaxWidth().height(ButtonStyle.heightCompact),
                        shape = ButtonStyle.shape
                    ) {
                        Text(if (uploadState == "SUCCESS") "已确认提交（可查看历史记录）" else "确认并提交录音")
                    }
                }
            }
        }
    }
}
