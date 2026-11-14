package com.growthpet.student.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.growthpet.student.R
import com.growthpet.student.companion.CompanionState
import com.growthpet.student.companion.YunlongCompanionStage
import com.growthpet.student.ui.CheckinStage
import com.growthpet.student.ui.MainViewModel
import com.growthpet.student.ui.StudentUiState
import com.growthpet.student.ui.cards.DynamicCardRenderer
import com.growthpet.student.ui.cards.PointsRewardsCard
import com.growthpet.student.ui.theme.*

@Composable
fun MainChatScreen(
    uiState: StudentUiState,
    companionState: CompanionState,
    viewModel: MainViewModel,
    onOpenHistory: () -> Unit,
    onOpenRecordings: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val isSpeaking by viewModel.speechSynthesizer.isSpeaking.collectAsState()
    val isListening by viewModel.speechRecognizer.isListening.collectAsState()
    val isRecording by viewModel.audioRecorder.isRecording.collectAsState()
    val isPlayingPreview by viewModel.audioRecorder.isPlayingPreview.collectAsState()

    var inputText by remember { mutableStateOf("") }
    var showTextInput by remember { mutableStateOf(false) }
    var showPointsSheet by remember { mutableStateOf(false) }
    var lastRecordedPath by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val isLowerGrade = !uiState.prefs.activeChildId.contains("grade6")

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.lastIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(ColorTokens.SkyGradientTop, ColorTokens.CanvasIce, ColorTokens.SurfaceCard)
                )
            )
            .systemBarsPadding()
    ) {
        if (uiState.isIdleFullscreen) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Spacing.lg),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onOpenRecordings,
                        shape = RoundedCornerShape(Radius.md)
                    ) {
                        Icon(Icons.Rounded.CloudDone, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("打卡记录 (${uiState.recordings.size})")
                    }
                    OutlinedButton(
                        onClick = { viewModel.wakeFromIdleFullscreen() },
                        shape = RoundedCornerShape(Radius.md)
                    ) {
                        Text("返回对话流")
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clickable { viewModel.wakeFromIdleFullscreen() },
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    YunlongCompanionStage(
                        state = CompanionState.IDLE,
                        companionName = uiState.prefs.companionName,
                        isLowerGrade = isLowerGrade,
                        size = 240.dp
                    )
                    Spacer(modifier = Modifier.height(Spacing.lg))
                    Text(
                        "${uiState.prefs.companionName}正在安静等你",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(Spacing.xs))
                    Text(
                        if (uiState.prefs.ownerWakeOnlyEnabled && uiState.prefs.ownerVoiceprintEnrolled)
                            "已开启声纹专属守护：仅小主人说出“${uiState.prefs.ownerWakePhrase}”可唤醒"
                        else
                            "轻触画面或点击下方唤醒按钮，对我说“开始打开语文作业”即可开始录音打卡",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ColorTokens.InkSecondary,
                        textAlign = TextAlign.Center
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    if (uiState.prefs.ownerWakeOnlyEnabled && uiState.prefs.ownerVoiceprintEnrolled) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.verifyOwnerWakeAttempt(false) },
                                modifier = Modifier.weight(1f).height(40.dp)
                            ) { Text("测试访客拦截", color = ColorTokens.DangerCoral) }
                            OutlinedButton(
                                onClick = {
                                    if (viewModel.verifyOwnerWakeAttempt(true)) {
                                        viewModel.wakeFromIdleFullscreen()
                                    }
                                },
                                modifier = Modifier.weight(1f).height(40.dp)
                            ) { Text("测试主人唤醒", color = ColorTokens.SuccessMint) }
                        }
                    }

                    Button(
                        onClick = {
                            if (viewModel.verifyOwnerWakeAttempt()) {
                                viewModel.wakeFromIdleFullscreen()
                                viewModel.sendMessage("今天小任务")
                            }
                        },
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = ColorTokens.SurfaceCard),
                        border = CardStyle.border,
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        shape = RoundedCornerShape(Radius.lg)
                    ) {
                        Icon(Icons.Rounded.Checklist, contentDescription = null, tint = ColorTokens.TechBluePrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("今天小任务", color = ColorTokens.TechBluePrimary)
                    }
                    Button(
                        onClick = { viewModel.startLiveVoiceSession() },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape = RoundedCornerShape(Radius.lg)
                    ) {
                        Icon(Icons.Rounded.Mic, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("唤醒${uiState.prefs.companionName}，开始实时语音")
                    }
                }
            }
            return@Column
        }

        // Fullscreen Focus Timer Mode
        val activeFocusTask = uiState.focusTask
        if (activeFocusTask != null) {
            var showFocusDetailDialog by remember { mutableStateOf(false) }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Spacing.lg),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { viewModel.exitFocusScreen() },
                        shape = RoundedCornerShape(Radius.md)
                    ) { Text("收起") }

                    Surface(
                        color = ColorTokens.SkyHaloLight,
                        shape = RoundedCornerShape(Radius.pill)
                    ) {
                        Text(
                            text = "${activeFocusTask.subject} · ${if (uiState.isFocusPaused) "休息中" else "专注计时"}",
                            style = MaterialTheme.typography.labelMedium,
                            color = ColorTokens.TechBluePrimary,
                            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = 4.dp)
                        )
                    }

                    OutlinedButton(
                        onClick = { showFocusDetailDialog = true },
                        shape = RoundedCornerShape(Radius.md)
                    ) { Text("作业内容") }
                }

                Column(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = activeFocusTask.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = Spacing.md)
                    )
                    Spacer(modifier = Modifier.height(Spacing.lg))
                    YunlongCompanionStage(
                        state = if (uiState.isFocusPaused) CompanionState.RESTING else CompanionState.THINKING,
                        companionName = uiState.prefs.companionName,
                        isLowerGrade = isLowerGrade,
                        size = 220.dp,
                        statusLabelOverride = if (uiState.isFocusPaused)
                            "暂停休息中 · ${uiState.prefs.companionName}陪你歇会"
                        else
                            "${uiState.prefs.companionName}正在陪你专注"
                    )
                    Spacer(modifier = Modifier.height(Spacing.lg))
                    Text(
                        text = String.format("%02d:%02d", uiState.focusSeconds / 60, uiState.focusSeconds % 60),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = ColorTokens.InkPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "预计 ${activeFocusTask.estimatedMinutes} 分钟 · +${activeFocusTask.pointsReward}分",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ColorTokens.InkMuted
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.toggleFocusPause() },
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(Radius.lg)
                    ) {
                        Icon(
                            imageVector = if (uiState.isFocusPaused) Icons.Rounded.PlayArrow else Icons.Rounded.Pause,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (uiState.isFocusPaused) "继续" else "暂停休息")
                    }
                    Button(
                        onClick = { viewModel.completeFocusTask() },
                        colors = ButtonDefaults.buttonColors(containerColor = ColorTokens.SuccessMint),
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(Radius.lg)
                    ) {
                        Icon(Icons.Rounded.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("完成")
                    }
                }
            }

            if (showFocusDetailDialog) {
                AlertDialog(
                    onDismissRequest = { showFocusDetailDialog = false },
                    title = { Text(activeFocusTask.title) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                            if (activeFocusTask.detailContent.isNotBlank()) {
                                Text(activeFocusTask.detailContent, style = MaterialTheme.typography.bodyMedium)
                            }
                            if (!activeFocusTask.teacherReminder.isNullOrBlank()) {
                                Text(
                                    "老师小提醒：${activeFocusTask.teacherReminder}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ColorTokens.TechBluePrimary
                                )
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showFocusDetailDialog = false }) { Text("关闭") }
                    }
                )
            }
            return@Column
        }

        // Minimal Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md, vertical = Spacing.xs),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onOpenHistory, modifier = Modifier.size(44.dp)) {
                    Icon(Icons.Rounded.History, contentDescription = "历史对话", tint = ColorTokens.InkPrimary)
                }
                IconButton(onClick = onOpenRecordings, modifier = Modifier.size(44.dp)) {
                    Icon(Icons.Rounded.CloudDone, contentDescription = "打卡与云端录音记录", tint = ColorTokens.InkPrimary)
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            if (uiState.isOnline) ColorTokens.SuccessMint else ColorTokens.InkMuted,
                            CircleShape
                        )
                )
                Text(uiState.prefs.companionName, style = MaterialTheme.typography.titleMedium)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.enterIdleFullscreen() }, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Rounded.OpenInFull, contentDescription = "安静等待全屏", tint = ColorTokens.InkSecondary)
                }
                TextButton(
                    onClick = { showPointsSheet = true },
                    modifier = Modifier.height(44.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_yunlong_points),
                        contentDescription = null,
                        tint = ColorTokens.TechBluePrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${uiState.totalPoints}分", fontWeight = FontWeight.Bold, color = ColorTokens.TechBluePrimary)
                }
                IconButton(onClick = onOpenSettings, modifier = Modifier.size(44.dp)) {
                    Icon(Icons.Rounded.Tune, contentDescription = "设置与资料", tint = ColorTokens.InkPrimary)
                }
            }
        }

        if (uiState.statusBanner != null) {
            Surface(
                color = ColorTokens.SkyHaloLight,
                shape = RoundedCornerShape(Radius.md),
                modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.md, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.xs),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(uiState.statusBanner, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    TextButton(onClick = { viewModel.clearBanner() }) { Text("知道了") }
                }
            }
        }

        // Unified Canvas: Yunlong floats near the bottom voice dock for immersive voice dialogue
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = Spacing.md, end = Spacing.md, top = Spacing.md, bottom = 212.dp),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                items(uiState.messages, key = { it.id }) { msg ->
                    val isChild = msg.role == "CHILD"
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = if (isChild) Alignment.End else Alignment.Start
                    ) {
                        Surface(
                            shape = RoundedCornerShape(
                                topStart = Radius.lg,
                                topEnd = Radius.lg,
                                bottomStart = if (isChild) Radius.lg else 6.dp,
                                bottomEnd = if (isChild) 6.dp else Radius.lg
                            ),
                            color = if (isChild) ColorTokens.BubbleChild else ColorTokens.BubbleAi.copy(alpha = 0.96f),
                            border = if (isChild) null else CardStyle.border,
                            modifier = Modifier.widthIn(max = 310.dp)
                        ) {
                            Text(
                                text = msg.text,
                                color = if (isChild) ColorTokens.SurfaceCard else ColorTokens.InkPrimary,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm)
                            )
                        }

                        if (!isChild && !msg.cardsJson.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(Spacing.xs))
                            DynamicCardRenderer(
                                cardsToken = msg.cardsJson,
                                uiState = uiState,
                                isLowerGrade = isLowerGrade,
                                isRecording = isRecording,
                                isPlayingPreview = isPlayingPreview,
                                onUpdateTask = viewModel::updateTaskStatus,
                                onStartFocusTask = viewModel::startFocusTask,
                                onStartVoiceCheckin = viewModel::promptConfirmHomeworkCheckin,
                                onMoveScheduleUp = viewModel::swapScheduleUp,
                                onConfirmSchedule = viewModel::confirmSchedulePlan,
                                onUpdateGoal = viewModel::updateGoalReflection,
                                onSubmitWish = viewModel::submitWish,
                                onRedeemReward = viewModel::requestRewardRedemption,
                                onAuthorizeMic = { viewModel.authorizeMicrophone(true) },
                                onStartRecord = {
                                    viewModel.audioRecorder.startAuthorizedRecording(uiState.prefs.micAuthorizedByChild)
                                        .onSuccess { file -> lastRecordedPath = file.absolutePath }
                                },
                                onStopRecord = {
                                    val file = viewModel.audioRecorder.stopRecording()
                                    if (file != null) lastRecordedPath = file.absolutePath
                                },
                                onPlayPreview = {
                                    if (lastRecordedPath.isNotBlank()) {
                                        viewModel.audioRecorder.playPreview(lastRecordedPath)
                                    }
                                },
                                onSubmitRecording = { title, duration ->
                                    viewModel.saveSubmittedRecording(
                                        title = title,
                                        filePath = lastRecordedPath.ifBlank { "mock://reading_clip.m4a" },
                                        durationSeconds = duration
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // Soft bottom gradient so conversation stream blends gently behind bottom-docked Yunlong
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(128.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, ColorTokens.CanvasIce.copy(alpha = 0.92f))
                        )
                    )
            )

            // Bottom Floating Yunlong Companion docked close to the voice button + single "今天小任务" pill
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier.clickable { viewModel.enterIdleFullscreen() },
                    contentAlignment = Alignment.Center
                ) {
                    YunlongCompanionStage(
                        state = if (isListening) CompanionState.LISTENING else if (isSpeaking) CompanionState.SPEAKING else companionState,
                        companionName = uiState.prefs.companionName,
                        isLowerGrade = isLowerGrade,
                        size = 138.dp,
                        floating = true
                    )
                    if (isSpeaking) {
                        OutlinedButton(
                            onClick = { viewModel.interruptAiSpeech() },
                            modifier = Modifier.align(Alignment.BottomEnd).padding(start = 128.dp).height(34.dp),
                            shape = RoundedCornerShape(Radius.pill),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Text("打断")
                        }
                    }
                }

                OutlinedButton(
                    onClick = { viewModel.sendMessage("今天小任务") },
                    shape = RoundedCornerShape(Radius.pill),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = ColorTokens.SurfaceCard),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(
                        Icons.Rounded.Checklist,
                        contentDescription = null,
                        tint = ColorTokens.TechBluePrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    val pendingCount = uiState.tasks.count { it.status != "COMPLETED" }
                    Text(
                        text = if (pendingCount > 0) "今天小任务 · $pendingCount" else "今天小任务",
                        style = MaterialTheme.typography.labelMedium,
                        color = ColorTokens.TechBluePrimary
                    )
                }
            }
        }

        // Active Voice Check-in Confirmation / Recording / Cloud Sync Banners
        when (uiState.checkinStage) {
            CheckinStage.CONFIRMING_HOMEWORK -> {
                uiState.pendingCheckinTask?.let { pending ->
                    Surface(
                        color = ColorTokens.SkyHaloLight,
                        shape = RoundedCornerShape(Radius.md),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.md, vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(Spacing.sm), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("确认打开「${pending.title}」并开启录音打卡吗？", style = MaterialTheme.typography.labelLarge)
                            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                                Button(
                                    onClick = { viewModel.sendMessage("确认") },
                                    modifier = Modifier.weight(1f).height(40.dp)
                                ) { Text("确认，开始录音") }
                                OutlinedButton(
                                    onClick = { viewModel.sendMessage("取消") },
                                    modifier = Modifier.height(40.dp)
                                ) { Text("取消") }
                            }
                        }
                    }
                }
            }
            CheckinStage.RECORDING -> {
                Surface(
                    color = ColorTokens.SkyHaloLight,
                    shape = RoundedCornerShape(Radius.md),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.md, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(Spacing.sm),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "正在安心录音打卡：${uiState.pendingCheckinTask?.title ?: "当前作业"}",
                                style = MaterialTheme.typography.labelLarge,
                                color = ColorTokens.TechBluePrimary
                            )
                            Text(
                                "计时 ${String.format("%02d:%02d", uiState.checkinSeconds / 60, uiState.checkinSeconds % 60)} · 无需按住，完成后说“打卡完毕”",
                                style = MaterialTheme.typography.bodySmall,
                                color = ColorTokens.InkSecondary
                            )
                        }
                        Button(
                            onClick = { viewModel.sendMessage("打卡完毕") },
                            colors = ButtonDefaults.buttonColors(containerColor = ColorTokens.SuccessMint)
                        ) { Text("说“打卡完毕”") }
                    }
                }
            }
            CheckinStage.SYNCING_CLOUD -> {
                Surface(
                    color = ColorTokens.SkyHaloLight,
                    shape = RoundedCornerShape(Radius.md),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.md, vertical = 4.dp)
                ) {
                    Text(
                        "正在上传打卡录音并同步至云端...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ColorTokens.TechBluePrimary,
                        modifier = Modifier.padding(Spacing.sm)
                    )
                }
            }
            CheckinStage.IDLE -> Unit
        }

        // Voice-First Live Call Bottom Bar directly beneath Yunlong
        Surface(
            color = ColorTokens.SurfaceCard,
            border = CardStyle.border,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                if (!showTextInput) {
                    if (uiState.isLiveVoiceSession) {
                        Surface(
                            color = ColorTokens.SkyHaloLight,
                            shape = RoundedCornerShape(Radius.lg),
                            modifier = Modifier.weight(1f).height(52.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable { if (isSpeaking) viewModel.interruptAiSpeech() }
                                    .padding(horizontal = Spacing.md),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.GraphicEq,
                                        contentDescription = null,
                                        tint = ColorTokens.TechBluePrimary
                                    )
                                    Text(
                                        when {
                                            isSpeaking -> "${uiState.prefs.companionName}正在说 · 点此打断"
                                            uiState.checkinStage == CheckinStage.RECORDING -> "收音中 · 说“打卡完毕”"
                                            else -> "实时对话中 · 正在听你说"
                                        },
                                        style = MaterialTheme.typography.labelLarge,
                                        color = ColorTokens.InkPrimary,
                                        maxLines = 1
                                    )
                                }
                                OutlinedButton(
                                    onClick = { viewModel.stopLiveVoiceSession() },
                                    contentPadding = PaddingValues(horizontal = 12.dp),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text("暂停")
                                }
                            }
                        }
                    } else {
                        Button(
                            onClick = { viewModel.toggleVoiceInput() },
                            colors = ButtonDefaults.buttonColors(containerColor = ColorTokens.TechBluePrimary),
                            shape = RoundedCornerShape(Radius.lg),
                            modifier = Modifier.weight(1f).height(52.dp)
                        ) {
                            Icon(imageVector = Icons.Rounded.Mic, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (uiState.checkinStage == CheckinStage.RECORDING) "点击说“打卡完毕”"
                                else "和${uiState.prefs.companionName}语音聊天"
                            )
                        }
                        OutlinedIconButton(
                            onClick = { showTextInput = true },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Rounded.Keyboard, contentDescription = "切换到键盘输入")
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("输入你想对${uiState.prefs.companionName}说的话...") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(Radius.xl)
                    )
                    if (inputText.isNotBlank()) {
                        Button(
                            onClick = {
                                viewModel.sendMessage(inputText)
                                inputText = ""
                            },
                            modifier = Modifier.height(48.dp),
                            shape = CircleShape
                        ) {
                            Icon(Icons.Rounded.Send, contentDescription = "发送")
                        }
                    }
                    FilledTonalIconButton(
                        onClick = { showTextInput = false },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Rounded.Mic, contentDescription = "切回语音主模式")
                    }
                }
            }
        }
    }

    if (showPointsSheet) {
        Dialog(onDismissRequest = { showPointsSheet = false }) {
            Surface(
                shape = RoundedCornerShape(Radius.xl),
                color = ColorTokens.CanvasIce,
                border = CardStyle.border,
                modifier = Modifier.fillMaxWidth().heightIn(max = 620.dp)
            ) {
                Column(modifier = Modifier.padding(Spacing.md)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_yunlong_points),
                                contentDescription = null,
                                tint = ColorTokens.TechBluePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text("积分奖励活动", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        TextButton(onClick = { showPointsSheet = false }) {
                            Text("关闭")
                        }
                    }
                    Spacer(modifier = Modifier.height(Spacing.xs))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        PointsRewardsCard(
                            totalPoints = uiState.totalPoints,
                            records = uiState.pointsRecords,
                            rewards = uiState.rewards,
                            onRequestRedeem = viewModel::requestRewardRedemption
                        )
                    }
                }
            }
        }
    }
}
