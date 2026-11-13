package com.growthpet.student.ui.auxiliary

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.growthpet.student.ui.MainViewModel
import com.growthpet.student.ui.StudentUiState
import com.growthpet.student.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationHistoryScreen(
    uiState: StudentUiState,
    onBack: () -> Unit
) {
    val formatter = remember { SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("历史对话记录") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            items(uiState.messages, key = { it.id }) { msg ->
                Surface(shape = CardStyle.shape, border = CardStyle.border, color = ColorTokens.SurfaceCard, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(Spacing.md)) {
                        Text(
                            "${if (msg.role == "CHILD") "我" else uiState.prefs.companionName} · ${formatter.format(Date(msg.timestamp))}",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(msg.text, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmittedRecordingsScreen(
    uiState: StudentUiState,
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val formatter = remember { SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()) }
    val isPlayingPreview by viewModel.audioRecorder.isPlayingPreview.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("打卡与云端录音记录") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.recordings.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding).padding(Spacing.xl)) {
                Text("还没有打卡录音记录，回到主对话说“开始打开语文作业”即可语音打卡并同步云端。", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(Spacing.md),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                items(uiState.recordings, key = { it.id }) { rec ->
                    val statusText = when (rec.uploadStatus) {
                        "SYNCED_CLOUD" -> "已同步至云端"
                        "UPLOADING" -> "正在同步云端"
                        else -> rec.uploadStatus
                    }
                    Surface(shape = CardStyle.shape, border = CardStyle.border, color = ColorTokens.SurfaceCard, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(Spacing.md), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(rec.passageTitle, style = MaterialTheme.typography.titleMedium)
                            Text(
                                "时长 ${rec.durationSeconds} 秒 · $statusText · ${formatter.format(Date(rec.createdAt))}",
                                style = MaterialTheme.typography.labelMedium,
                                color = ColorTokens.SuccessMint
                            )
                            if (!rec.cloudRecordId.isNullOrBlank()) {
                                Text(
                                    "云端同步编号：${rec.cloudRecordId}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = ColorTokens.TechBluePrimary
                                )
                            }
                            if (!rec.filePath.startsWith("mock://")) {
                                OutlinedButton(
                                    onClick = {
                                        if (isPlayingPreview) viewModel.audioRecorder.stopPreview()
                                        else viewModel.audioRecorder.playPreview(rec.filePath)
                                    },
                                    modifier = Modifier.height(38.dp)
                                ) {
                                    Text(if (isPlayingPreview) "停止播放" else "回听打卡录音")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsAndProfileScreen(
    uiState: StudentUiState,
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onLoggedOut: () -> Unit
) {
    var companionNameInput by remember(uiState.prefs.companionName) { mutableStateOf(uiState.prefs.companionName) }
    var wakePhraseInput by remember(uiState.prefs.ownerWakePhrase) { mutableStateOf(uiState.prefs.ownerWakePhrase) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("资料、云龙与隐私设置") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            item {
                Surface(shape = CardStyle.shape, border = CardStyle.border, color = ColorTokens.SurfaceCard, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(Spacing.md), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                        Text("个人资料与设备绑定", style = MaterialTheme.typography.titleMedium)
                        Text("学生账号 ID：${uiState.prefs.activeChildId}", style = MaterialTheme.typography.bodyMedium)
                        Text("绑定状态：已绑定家长账号（同设备账号严格隔离）", style = MaterialTheme.typography.bodyMedium)
                        Text("服务运行模式：${if (uiState.prefs.isMockMode) "Mock 模拟演示模式（未配置真实 AI 密钥）" else "真实服务端模式"}", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            item {
                Surface(shape = CardStyle.shape, border = CardStyle.border, color = ColorTokens.SurfaceCard, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(Spacing.md), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        Text("云龙设置", style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(
                            value = companionNameInput,
                            onValueChange = { companionNameInput = it },
                            label = { Text("云龙昵称") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(onClick = { viewModel.updateCompanionName(companionNameInput) }) {
                            Text("保存伙伴名字")
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("自动语音播报回复", style = MaterialTheme.typography.bodyLarge)
                            Switch(
                                checked = uiState.prefs.voiceAutoPlayEnabled,
                                onCheckedChange = { viewModel.setVoiceAutoPlay(it) }
                            )
                        }
                    }
                }
            }

            item {
                Surface(shape = CardStyle.shape, border = CardStyle.border, color = ColorTokens.SurfaceCard, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(Spacing.md), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("主人语音欢迎识别（仅主人可唤醒）", style = MaterialTheme.typography.titleMedium)
                            Switch(
                                checked = uiState.prefs.ownerWakeOnlyEnabled && uiState.prefs.ownerVoiceprintEnrolled,
                                onCheckedChange = { viewModel.setOwnerWakeOnly(it) }
                            )
                        }
                        Text(
                            "录制小主人的专属欢迎语音后，待机唤醒时将校验声纹特征，只有用户主人可以唤醒${uiState.prefs.companionName}。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ColorTokens.InkSecondary
                        )
                        OutlinedTextField(
                            value = wakePhraseInput,
                            onValueChange = { wakePhraseInput = it },
                            label = { Text("专属欢迎唤醒语") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = { viewModel.enrollOwnerVoiceprint(wakePhraseInput) },
                            enabled = !uiState.isEnrollingVoiceprint,
                            modifier = Modifier.fillMaxWidth().height(ButtonStyle.heightCompact)
                        ) {
                            Text(
                                when {
                                    uiState.isEnrollingVoiceprint -> "正在采集主人声纹..."
                                    uiState.prefs.ownerVoiceprintEnrolled -> "重新录制主人欢迎声纹"
                                    else -> "录制主人欢迎声纹"
                                }
                            )
                        }
                        if (uiState.prefs.ownerVoiceprintEnrolled) {
                            Text(
                                "已录入主人声纹，可在下方快速校验：",
                                style = MaterialTheme.typography.labelMedium,
                                color = ColorTokens.SuccessMint
                            )
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                                OutlinedButton(
                                    onClick = { viewModel.verifyOwnerWakeAttempt(true) },
                                    modifier = Modifier.weight(1f)
                                ) { Text("主人唤醒（通过）") }
                                OutlinedButton(
                                    onClick = { viewModel.verifyOwnerWakeAttempt(false) },
                                    modifier = Modifier.weight(1f)
                                ) { Text("访客唤醒（拦截）") }
                            }
                        }
                    }
                }
            }

            item {
                Surface(shape = CardStyle.shape, border = CardStyle.border, color = ColorTokens.SurfaceCard, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(Spacing.md), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        Text("隐私与录音权限", style = MaterialTheme.typography.titleMedium)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("允许英语朗读主动授权录音", style = MaterialTheme.typography.bodyLarge)
                            Switch(
                                checked = uiState.prefs.micAuthorizedByChild,
                                onCheckedChange = { viewModel.authorizeMicrophone(it) }
                            )
                        }
                        Text("所有录音仅在孩子主动点击授权后开始，本地缓存严格按账号隔离。", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        viewModel.logout()
                        onLoggedOut()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ColorTokens.DangerCoral),
                    modifier = Modifier.fillMaxWidth().height(ButtonStyle.heightPrimary),
                    shape = ButtonStyle.shape
                ) {
                    Text("退出登录并切换孩子账号")
                }
            }
        }
    }
}
