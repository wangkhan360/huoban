package com.growthpet.student.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.growthpet.student.R
import com.growthpet.student.companion.CompanionState
import com.growthpet.student.companion.YunlongCompanionStage
import com.growthpet.student.ui.CheckinStage
import com.growthpet.student.ui.MainViewModel
import com.growthpet.student.ui.StudentUiState
import com.growthpet.student.ui.cards.DynamicCardRenderer
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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                    ) {
                        OutlinedButton(
                            onClick = {
                                if (viewModel.verifyOwnerWakeAttempt()) viewModel.sendMessage("开始打开语文作业")
                            },
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(Radius.pill)
                        ) { Text("开始打开语文作业", maxLines = 1) }
                        OutlinedButton(
                            onClick = {
                                if (viewModel.verifyOwnerWakeAttempt()) viewModel.sendMessage("开始打开英语作业")
                            },
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(Radius.pill)
                        ) { Text("开始打开英语作业", maxLines = 1) }
                    }
                    Button(
                        onClick = { viewModel.toggleVoiceInput() },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape = RoundedCornerShape(Radius.lg)
                    ) {
                        Icon(Icons.Rounded.Mic, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("唤醒${uiState.prefs.companionName}并开始说话")
                    }
                }
            }
            return@Column
        }

        // Top Utility Bar (No multi-tab bottom bar — single AI chat interface)
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

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(uiState.prefs.companionName, style = MaterialTheme.typography.titleMedium)
                Text(
                    if (uiState.prefs.isMockMode) "模拟演示模式 · 网络正常" else "在线",
                    style = MaterialTheme.typography.labelMedium,
                    color = ColorTokens.InkMuted
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.enterIdleFullscreen() }, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Rounded.OpenInFull, contentDescription = "安静等待全屏", tint = ColorTokens.InkSecondary)
                }
                TextButton(
                    onClick = { viewModel.sendMessage("我现在有多少积分？") },
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

        // Unified Canvas: Yunlong floats over the scrollable conversation text with zero hard split
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = Spacing.md, end = Spacing.md, top = 176.dp, bottom = Spacing.md),
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

            // Soft top gradient so messages gently fade underneath floating Yunlong
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(92.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(ColorTokens.SkyGradientTop.copy(alpha = 0.88f), Color.Transparent)
                        )
                    )
            )

            // Floating Yunlong Companion Overlay
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 4.dp)
                    .clickable { viewModel.enterIdleFullscreen() },
                contentAlignment = Alignment.Center
            ) {
                YunlongCompanionStage(
                    state = if (isListening) CompanionState.LISTENING else if (isSpeaking) CompanionState.SPEAKING else companionState,
                    companionName = uiState.prefs.companionName,
                    isLowerGrade = isLowerGrade,
                    size = 148.dp,
                    floating = true
                )
                if (isSpeaking) {
                    OutlinedButton(
                        onClick = { viewModel.interruptAiSpeech() },
                        modifier = Modifier.align(Alignment.BottomEnd).padding(start = 132.dp).height(38.dp),
                        shape = RoundedCornerShape(Radius.pill)
                    ) {
                        Text("打断播报")
                    }
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
                    color = ColorTokens.DangerCoral.copy(alpha = 0.12f),
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
                                "正在录音打卡：${uiState.pendingCheckinTask?.title ?: "当前作业"}",
                                style = MaterialTheme.typography.labelLarge,
                                color = ColorTokens.DangerCoral
                            )
                            Text(
                                "计时 ${String.format("%02d:%02d", uiState.checkinSeconds / 60, uiState.checkinSeconds % 60)} · 完成后请说“打卡完毕”",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Button(
                            onClick = { viewModel.sendMessage("打卡完毕") },
                            colors = ButtonDefaults.buttonColors(containerColor = ColorTokens.DangerCoral)
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

        // Quick Conversational Triggers
        LazyRow(
            contentPadding = PaddingValues(horizontal = Spacing.md, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            val prompts = listOf(
                "开始打开语文作业",
                "开始打开英语作业",
                "我今天有什么作业？",
                "帮我看看今日时间计划",
                "我想成为一名科学家",
                "我现在有多少积分？"
            )
            items(prompts) { prompt ->
                OutlinedButton(
                    onClick = { viewModel.sendMessage(prompt) },
                    modifier = Modifier.height(40.dp),
                    shape = RoundedCornerShape(Radius.pill)
                ) {
                    Text(prompt, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        // Voice-First Bottom Control Bar (Defaults to Tap-to-Speak; Keyboard icon switches to text input)
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
                    Button(
                        onClick = { viewModel.toggleVoiceInput() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isListening) ColorTokens.DangerCoral else ColorTokens.TechBluePrimary
                        ),
                        shape = RoundedCornerShape(Radius.lg),
                        modifier = Modifier.weight(1f).height(52.dp)
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Rounded.Stop else Icons.Rounded.Mic,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (isListening) "正在听你说... 点击结束"
                            else if (uiState.checkinStage == CheckinStage.RECORDING) "点击语音说“打卡完毕”"
                            else "点击和${uiState.prefs.companionName}语音对话"
                        )
                    }
                    OutlinedIconButton(
                        onClick = { showTextInput = true },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Rounded.Keyboard, contentDescription = "切换到键盘输入")
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
}
