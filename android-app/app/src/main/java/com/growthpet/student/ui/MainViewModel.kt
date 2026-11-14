package com.growthpet.student.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.growthpet.student.audio.AudioRecordingManager
import com.growthpet.student.audio.SpeechRecognitionManager
import com.growthpet.student.audio.SpeechSynthesisManager
import com.growthpet.student.companion.CompanionAnimationController
import com.growthpet.student.companion.CompanionState
import com.growthpet.student.data.local.*
import com.growthpet.student.data.repository.AiConversationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

enum class CheckinStage {
    IDLE,
    CONFIRMING_HOMEWORK,
    RECORDING,
    SYNCING_CLOUD
}

data class StudentUiState(
    val prefs: StudentSessionPrefs = StudentSessionPrefs(),
    val profile: ChildProfileEntity? = null,
    val messages: List<ChatMessageEntity> = emptyList(),
    val tasks: List<TaskItemEntity> = emptyList(),
    val schedule: List<ScheduleSlotEntity> = emptyList(),
    val goals: List<GrowthGoalEntity> = emptyList(),
    val wishes: List<FamilyWishEntity> = emptyList(),
    val pointsRecords: List<PointsRecordEntity> = emptyList(),
    val rewards: List<RewardItemEntity> = emptyList(),
    val recordings: List<ReadingRecordingEntity> = emptyList(),
    val totalPoints: Int = 28,
    val isOnline: Boolean = true,
    val isSending: Boolean = false,
    val statusBanner: String? = null,
    val isIdleFullscreen: Boolean = false,
    val checkinStage: CheckinStage = CheckinStage.IDLE,
    val pendingCheckinTask: TaskItemEntity? = null,
    val checkinSeconds: Int = 0,
    val isEnrollingVoiceprint: Boolean = false,
    val simulatedSpeakerIsOwner: Boolean = true,
    val isLiveVoiceSession: Boolean = false,
    val focusTask: TaskItemEntity? = null,
    val focusSeconds: Int = 0,
    val isFocusPaused: Boolean = false
)

private data class InteractionControlState(
    val isSending: Boolean = false,
    val statusBanner: String? = null,
    val isIdleFullscreen: Boolean = false,
    val checkinStage: CheckinStage = CheckinStage.IDLE,
    val pendingCheckinTask: TaskItemEntity? = null,
    val checkinSeconds: Int = 0,
    val isEnrollingVoiceprint: Boolean = false,
    val simulatedSpeakerIsOwner: Boolean = true,
    val isLiveVoiceSession: Boolean = false,
    val focusTask: TaskItemEntity? = null,
    val focusSeconds: Int = 0,
    val isFocusPaused: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: AiConversationRepository,
    private val prefsStore: UserPreferencesDataStore,
    val companionController: CompanionAnimationController,
    val speechRecognizer: SpeechRecognitionManager,
    val speechSynthesizer: SpeechSynthesisManager,
    val audioRecorder: AudioRecordingManager
) : ViewModel() {

    private val _control = MutableStateFlow(InteractionControlState())
    private var checkinTimerJob: Job? = null
    private var focusTimerJob: Job? = null
    private var activeCheckinFilePath: String = ""

    val companionState: StateFlow<CompanionState> = companionController.state

    init {
        viewModelScope.launch {
            var wasSpeaking = false
            speechSynthesizer.isSpeaking.collect { speaking ->
                if (wasSpeaking && !speaking && _control.value.isLiveVoiceSession) {
                    delay(220)
                    if (_control.value.isLiveVoiceSession && !speechRecognizer.isListening.value) {
                        beginLiveListeningPass()
                    }
                }
                wasSpeaking = speaking
            }
        }
    }

    val uiState: StateFlow<StudentUiState> = prefsStore.sessionPrefs
        .flatMapLatest { prefs ->
            val cid = prefs.activeChildId
            if (cid.isBlank()) {
                flowOf(StudentUiState(prefs = prefs))
            } else {
                combine(
                    repository.observeMessages(cid),
                    repository.observeTasks(cid),
                    repository.observeSchedule(cid),
                    repository.observeGoals(cid),
                    repository.observeWishes(cid),
                    repository.observePointsRecords(cid),
                    repository.observeRewards(cid),
                    repository.observeRecordings(cid),
                    _control
                ) { array ->
                    @Suppress("UNCHECKED_CAST")
                    val msgs = array[0] as List<ChatMessageEntity>
                    @Suppress("UNCHECKED_CAST")
                    val tasks = array[1] as List<TaskItemEntity>
                    @Suppress("UNCHECKED_CAST")
                    val schedule = array[2] as List<ScheduleSlotEntity>
                    @Suppress("UNCHECKED_CAST")
                    val goals = array[3] as List<GrowthGoalEntity>
                    @Suppress("UNCHECKED_CAST")
                    val wishes = array[4] as List<FamilyWishEntity>
                    @Suppress("UNCHECKED_CAST")
                    val pts = array[5] as List<PointsRecordEntity>
                    @Suppress("UNCHECKED_CAST")
                    val rewards = array[6] as List<RewardItemEntity>
                    @Suppress("UNCHECKED_CAST")
                    val recs = array[7] as List<ReadingRecordingEntity>
                    val ctrl = array[8] as InteractionControlState

                    StudentUiState(
                        prefs = prefs,
                        messages = msgs,
                        tasks = tasks,
                        schedule = schedule,
                        goals = goals,
                        wishes = wishes,
                        pointsRecords = pts,
                        rewards = rewards,
                        recordings = recs,
                        totalPoints = pts.sumOf { it.delta },
                        isOnline = true,
                        isSending = ctrl.isSending,
                        statusBanner = ctrl.statusBanner,
                        isIdleFullscreen = ctrl.isIdleFullscreen,
                        checkinStage = ctrl.checkinStage,
                        pendingCheckinTask = ctrl.pendingCheckinTask,
                        checkinSeconds = ctrl.checkinSeconds,
                        isEnrollingVoiceprint = ctrl.isEnrollingVoiceprint,
                        simulatedSpeakerIsOwner = ctrl.simulatedSpeakerIsOwner,
                        isLiveVoiceSession = ctrl.isLiveVoiceSession,
                        focusTask = ctrl.focusTask,
                        focusSeconds = ctrl.focusSeconds,
                        isFocusPaused = ctrl.isFocusPaused
                    )
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StudentUiState())

    fun verifyCodeAndBind(
        code: String,
        onVerified: (childId: String, childName: String, gradeLevel: Int, gradeBand: String, parentName: String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            repository.verifyParentCode(code, "android_device_local")
                .onSuccess { dto ->
                    onVerified(dto.childId, dto.childName, dto.gradeLevel, dto.gradeBand, dto.parentName)
                }
                .onFailure { err ->
                    onError(err.message ?: "核验码无效")
                }
        }
    }

    fun finishOnboarding(
        childId: String,
        childName: String,
        gradeLevel: Int,
        gradeBand: String,
        parentName: String,
        companionName: String,
        portraitSummaryJson: String
    ) {
        viewModelScope.launch {
            val profile = ChildProfileEntity(
                childId = childId,
                name = childName,
                gradeLevel = gradeLevel,
                gradeBand = gradeBand,
                parentBoundName = parentName,
                companionName = companionName,
                portraitSummaryJson = portraitSummaryJson,
                deviceId = "android_device_local",
                updatedAt = System.currentTimeMillis()
            )
            val greeting = if (gradeBand == "LOWER_GRADE") {
                "嗨 $childName！我是你的好朋友$companionName~ 今天想先看看有什么小任务，还是跟我聊聊你的奇思妙想呀？"
            } else {
                "你好，$childName。我是你的成长伙伴$companionName。你可以直接对我说今天的作业、时间规划、目标拆解或积分查询。"
            }
            repository.seedInitialChildWorkspace(profile, greeting)
            prefsStore.completeOnboarding(childId, companionName)
            companionController.transitionTo(CompanionState.HAPPY)
        }
    }

    fun enterIdleFullscreen() {
        speechSynthesizer.interrupt()
        companionController.transitionTo(CompanionState.IDLE)
        _control.update { it.copy(isIdleFullscreen = true) }
    }

    fun setOwnerWakeOnly(enabled: Boolean) {
        viewModelScope.launch {
            if (enabled && !uiState.value.prefs.ownerVoiceprintEnrolled) {
                _control.update { it.copy(statusBanner = "请先录制主人欢迎声纹再开启守护") }
                return@launch
            }
            prefsStore.setOwnerWakeOnly(enabled)
        }
    }

    fun setSimulatedSpeakerIsOwner(isOwner: Boolean) {
        _control.update { it.copy(simulatedSpeakerIsOwner = isOwner) }
    }

    fun enrollOwnerVoiceprint(wakePhrase: String) {
        if (_control.value.isEnrollingVoiceprint) return
        val phrase = wakePhrase.trim().ifBlank { "云龙你好，我是小主人" }
        viewModelScope.launch {
            _control.update {
                it.copy(
                    isEnrollingVoiceprint = true,
                    statusBanner = "请对着麦克风说出欢迎语：“$phrase”"
                )
            }
            prefsStore.setMicAuthorized(true)
            var samplePath = "mock://owner_voiceprint_${System.currentTimeMillis()}.m4a"
            val recStarted = audioRecorder.startAuthorizedRecording(true)
            delay(2000)
            if (recStarted.isSuccess) {
                samplePath = audioRecorder.stopRecording()?.absolutePath ?: samplePath
            }
            prefsStore.saveOwnerVoiceprint(phrase, samplePath)
            _control.update {
                it.copy(
                    isEnrollingVoiceprint = false,
                    simulatedSpeakerIsOwner = true,
                    statusBanner = "主人欢迎声纹已注册，已开启“仅主人可唤醒”守护"
                )
            }
            if (uiState.value.prefs.voiceAutoPlayEnabled) {
                speechSynthesizer.speak("声纹欢迎识别录好啦，以后只有小主人本人能唤醒我哦！")
            }
        }
    }

    fun verifyOwnerWakeAttempt(speakerIsOwner: Boolean = _control.value.simulatedSpeakerIsOwner): Boolean {
        val prefs = uiState.value.prefs
        if (!prefs.ownerWakeOnlyEnabled || !prefs.ownerVoiceprintEnrolled) return true
        if (!speakerIsOwner) {
            companionController.transitionTo(CompanionState.IDLE)
            _control.update {
                it.copy(statusBanner = "声纹欢迎识别拦截：未检测到主人声纹，只有用户主人可以唤醒")
            }
            if (prefs.voiceAutoPlayEnabled) {
                speechSynthesizer.speak("声纹没有认出你哦，只有小主人本人可以唤醒我。")
            }
            return false
        }
        _control.update {
            it.copy(statusBanner = "主人声纹识别通过（“${prefs.ownerWakePhrase}”），欢迎回来！")
        }
        return true
    }

    fun wakeFromIdleFullscreen() {
        if (!verifyOwnerWakeAttempt()) return
        companionController.transitionTo(CompanionState.HAPPY)
        _control.update { it.copy(isIdleFullscreen = false) }
    }

    fun promptConfirmHomeworkCheckin(task: TaskItemEntity) {
        val current = uiState.value
        val cid = current.prefs.activeChildId
        if (cid.isBlank()) return
        viewModelScope.launch {
            _control.update {
                it.copy(
                    isIdleFullscreen = false,
                    checkinStage = CheckinStage.CONFIRMING_HOMEWORK,
                    pendingCheckinTask = task
                )
            }
            val reply = "你是说确认打开【${task.title}】吗？请说“确认”或“是”，确认后我就为你开启打卡录音模式！"
            repository.insertConversationMessage(cid, "AI", reply, "CONFIRM_CHECKIN", "speaking")
            companionController.transitionTo(CompanionState.SPEAKING)
            if (current.prefs.voiceAutoPlayEnabled) speechSynthesizer.speak(reply)
        }
    }

    private fun matchHomeworkTask(input: String, tasks: List<TaskItemEntity>): TaskItemEntity? {
        val normalized = input.lowercase()
        return when {
            normalized.contains("语文") || normalized.contains("背诵") || normalized.contains("秋天") || normalized.contains("听写") ->
                tasks.firstOrNull { it.subject == "语文" || it.title.contains("语文") }
            normalized.contains("英语") || normalized.contains("单词") || normalized.contains("读书") ->
                tasks.firstOrNull { it.subject == "英语" || it.title.contains("英语") }
            normalized.contains("跳绳") || normalized.contains("体育") ->
                tasks.firstOrNull { it.title.contains("跳绳") || it.subject.contains("体育") }
            else -> tasks.firstOrNull { it.status != "COMPLETED" } ?: tasks.firstOrNull()
        }
    }

    private suspend fun startHomeworkCheckinRecording(cid: String, task: TaskItemEntity, voiceAutoPlay: Boolean) {
        prefsStore.setMicAuthorized(true)
        activeCheckinFilePath = ""
        audioRecorder.startAuthorizedRecording(true)
            .onSuccess { file -> activeCheckinFilePath = file.absolutePath }

        checkinTimerJob?.cancel()
        _control.update {
            it.copy(
                isIdleFullscreen = false,
                checkinStage = CheckinStage.RECORDING,
                pendingCheckinTask = task,
                checkinSeconds = 0
            )
        }
        checkinTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _control.update { it.copy(checkinSeconds = it.checkinSeconds + 1) }
            }
        }
        companionController.transitionTo(CompanionState.LISTENING)
        val reply = "已进入【${task.title}】打卡录音模式！我正安静陪你完成，结束后语音说“打卡完毕”即可自动同步云端。"
        repository.insertConversationMessage(cid, "AI", reply, "RECORDING_STARTED", "listening")
        if (voiceAutoPlay) speechSynthesizer.speak(reply)
    }

    private suspend fun finishHomeworkCheckinAndSync(cid: String, voiceAutoPlay: Boolean) {
        val currentCtrl = _control.value
        val targetTask = currentCtrl.pendingCheckinTask
        val duration = currentCtrl.checkinSeconds.coerceAtLeast(4)
        checkinTimerJob?.cancel()
        val recordedFile = audioRecorder.stopRecording()
        val finalPath = recordedFile?.absolutePath ?: activeCheckinFilePath.ifBlank { "mock://checkin_${System.currentTimeMillis()}.m4a" }

        _control.update {
            it.copy(
                checkinStage = CheckinStage.SYNCING_CLOUD,
                statusBanner = "正在将作业打卡录音同步到云端..."
            )
        }
        companionController.transitionTo(CompanionState.THINKING)
        delay(650)

        val datePart = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val cloudId = "CLD-$datePart-${(100..999).random()}"

        if (targetTask != null) {
            repository.updateTask(
                targetTask.copy(
                    status = "COMPLETED",
                    feedbackNote = "语音打卡 $duration 秒已同步云端",
                    updatedAt = System.currentTimeMillis()
                )
            )
        }

        repository.saveRecording(
            ReadingRecordingEntity(
                id = UUID.randomUUID().toString(),
                childId = cid,
                passageTitle = if (targetTask != null) "${targetTask.title} · 语音打卡录音" else "作业语音打卡录音",
                filePath = finalPath,
                durationSeconds = duration,
                uploadStatus = "SYNCED_CLOUD",
                cloudRecordId = cloudId,
                createdAt = System.currentTimeMillis()
            )
        )

        _control.update {
            it.copy(
                checkinStage = CheckinStage.IDLE,
                pendingCheckinTask = null,
                checkinSeconds = 0,
                statusBanner = "打卡录音已成功同步至云端（编号 $cloudId）"
            )
        }
        companionController.transitionTo(CompanionState.CELEBRATING)
        val reply = "太棒啦！${targetTask?.let { "【${it.title}】" } ?: "本次作业"}打卡完毕，$duration 秒打卡录音已成功同步至云端，可在「打卡记录」中随时回听。"
        repository.insertConversationMessage(cid, "AI", reply, "CHECKIN_SYNCED", "celebrating")
        if (voiceAutoPlay) speechSynthesizer.speak(reply)
    }

    fun sendMessage(text: String) {
        val current = uiState.value
        val cid = current.prefs.activeChildId
        val trimmed = text.trim()
        if (cid.isBlank() || trimmed.isBlank()) return

        viewModelScope.launch {
            speechSynthesizer.interrupt()
            _control.update { it.copy(isIdleFullscreen = false, isSending = true) }

            val ctrl = _control.value
            val normalized = trimmed.lowercase()

            // 1. Recording in progress -> check for "打卡完毕"
            if (ctrl.checkinStage == CheckinStage.RECORDING) {
                repository.insertConversationMessage(cid, "CHILD", trimmed)
                if (normalized.contains("打卡完毕") || normalized.contains("完成打卡") || normalized.contains("录完了") || normalized.contains("结束了")) {
                    finishHomeworkCheckinAndSync(cid, current.prefs.voiceAutoPlayEnabled)
                } else {
                    val tip = "正在为你录制【${ctrl.pendingCheckinTask?.title ?: "当前作业"}】打卡音频，结束后对我说“打卡完毕”就会自动同步到云端哦。"
                    repository.insertConversationMessage(cid, "AI", tip, "RECORDING_HINT", "listening")
                    companionController.transitionTo(CompanionState.LISTENING)
                    if (current.prefs.voiceAutoPlayEnabled) speechSynthesizer.speak(tip)
                }
                _control.update { it.copy(isSending = false) }
                return@launch
            }

            // 2. Confirming homework before recording
            if (ctrl.checkinStage == CheckinStage.CONFIRMING_HOMEWORK && ctrl.pendingCheckinTask != null) {
                repository.insertConversationMessage(cid, "CHILD", trimmed)
                when {
                    normalized == "是" || normalized.contains("确认") || normalized.contains("好") || normalized.contains("没错") || normalized.contains("开始") -> {
                        startHomeworkCheckinRecording(cid, ctrl.pendingCheckinTask, current.prefs.voiceAutoPlayEnabled)
                    }
                    normalized.contains("取消") || normalized.contains("不是") || normalized.contains("换") -> {
                        _control.update { it.copy(checkinStage = CheckinStage.IDLE, pendingCheckinTask = null) }
                        val cancelMsg = "好的，已取消本次打卡。你想打开哪一项作业，随时告诉我！"
                        repository.insertConversationMessage(cid, "AI", cancelMsg, "CHECKIN_CANCELLED", "happy")
                        companionController.transitionTo(CompanionState.HAPPY)
                    }
                    else -> {
                        val rematched = matchHomeworkTask(trimmed, current.tasks) ?: ctrl.pendingCheckinTask
                        _control.update { it.copy(pendingCheckinTask = rematched) }
                        val confirmMsg = "你是说确认打开【${rematched.title}】吗？请说“确认”或“是”，我马上开启录音打卡！"
                        repository.insertConversationMessage(cid, "AI", confirmMsg, "CONFIRM_CHECKIN", "speaking")
                        companionController.transitionTo(CompanionState.SPEAKING)
                        if (current.prefs.voiceAutoPlayEnabled) speechSynthesizer.speak(confirmMsg)
                    }
                }
                _control.update { it.copy(isSending = false) }
                return@launch
            }

            // 3. Voice trigger: "开始打开XX作业"
            if (normalized.contains("开始打开") || (normalized.contains("打开") && normalized.contains("作业"))) {
                repository.insertConversationMessage(cid, "CHILD", trimmed)
                val matched = matchHomeworkTask(trimmed, current.tasks)
                if (matched != null) {
                    _control.update {
                        it.copy(
                            checkinStage = CheckinStage.CONFIRMING_HOMEWORK,
                            pendingCheckinTask = matched
                        )
                    }
                    val confirmMsg = "你是说确认打开【${matched.title}】吗？请说“确认”或“是”，确认后我就为你开启打卡录音模式！"
                    repository.insertConversationMessage(cid, "AI", confirmMsg, "CONFIRM_CHECKIN", "speaking")
                    companionController.transitionTo(CompanionState.SPEAKING)
                    if (current.prefs.voiceAutoPlayEnabled) speechSynthesizer.speak(confirmMsg)
                }
                _control.update { it.copy(isSending = false) }
                return@launch
            }

            // 4. Standard AI conversation turn
            companionController.transitionTo(CompanionState.THINKING)
            val gradeBand = if (cid.contains("grade6")) "UPPER_GRADE" else "LOWER_GRADE"
            repository.sendStudentMessage(cid, gradeBand, current.prefs.companionName, trimmed)
                .onSuccess { res ->
                    companionController.transitionFromWire(res.companionState)
                    if (current.prefs.voiceAutoPlayEnabled) {
                        speechSynthesizer.speak(res.replyText)
                    }
                }
                .onFailure {
                    companionController.transitionTo(CompanionState.HAPPY)
                    _control.update { it.copy(statusBanner = "网络或服务暂时不可用，请重试") }
                }
            _control.update { it.copy(isSending = false) }
        }
    }

    private fun beginLiveListeningPass() {
        if (!_control.value.isLiveVoiceSession) return
        companionController.transitionTo(CompanionState.LISTENING)
        speechRecognizer.startListening(
            onFinalResult = { recognized -> sendMessage(recognized) },
            onError = {
                if (_control.value.isLiveVoiceSession) {
                    // Stay in calm continuous listening mode waiting for child voice
                    companionController.transitionTo(CompanionState.LISTENING)
                }
            }
        )
    }

    fun startLiveVoiceSession() {
        if (_control.value.isIdleFullscreen && !verifyOwnerWakeAttempt()) {
            return
        }
        speechSynthesizer.interrupt()
        _control.update { it.copy(isIdleFullscreen = false, isLiveVoiceSession = true) }
        beginLiveListeningPass()
    }

    fun stopLiveVoiceSession() {
        speechRecognizer.stopListening()
        speechSynthesizer.interrupt()
        _control.update { it.copy(isLiveVoiceSession = false) }
        companionController.transitionTo(CompanionState.HAPPY)
    }

    fun toggleVoiceInput() {
        if (_control.value.isLiveVoiceSession) {
            stopLiveVoiceSession()
        } else {
            startLiveVoiceSession()
        }
    }

    fun interruptAiSpeech() {
        speechSynthesizer.interrupt()
        if (_control.value.isLiveVoiceSession) {
            beginLiveListeningPass()
        } else {
            companionController.transitionTo(CompanionState.HAPPY)
        }
    }

    fun updateTaskStatus(task: TaskItemEntity, nextStatus: String, feedback: String? = null) {
        viewModelScope.launch {
            repository.updateTask(
                task.copy(
                    status = nextStatus,
                    feedbackNote = feedback ?: task.feedbackNote,
                    updatedAt = System.currentTimeMillis()
                )
            )
            if (nextStatus == "COMPLETED") {
                companionController.transitionTo(CompanionState.CELEBRATING)
            } else if (nextStatus == "IN_PROGRESS") {
                companionController.transitionTo(CompanionState.ENCOURAGING)
            }
        }
    }

    private fun startFocusTicker() {
        focusTimerJob?.cancel()
        focusTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val cur = _control.value
                if (cur.focusTask != null && !cur.isFocusPaused) {
                    _control.update { it.copy(focusSeconds = it.focusSeconds + 1) }
                }
            }
        }
    }

    fun startFocusTask(task: TaskItemEntity) {
        if (_control.value.isLiveVoiceSession) {
            stopLiveVoiceSession()
        }
        speechSynthesizer.interrupt()
        viewModelScope.launch {
            repository.updateTask(
                task.copy(status = "IN_PROGRESS", updatedAt = System.currentTimeMillis())
            )
        }
        _control.update {
            it.copy(
                isIdleFullscreen = false,
                focusTask = task,
                focusSeconds = 0,
                isFocusPaused = false
            )
        }
        companionController.transitionTo(CompanionState.THINKING)
        startFocusTicker()
    }

    fun toggleFocusPause() {
        val nextPaused = !_control.value.isFocusPaused
        _control.update { it.copy(isFocusPaused = nextPaused) }
        companionController.transitionTo(if (nextPaused) CompanionState.RESTING else CompanionState.THINKING)
    }

    fun exitFocusScreen() {
        focusTimerJob?.cancel()
        _control.update {
            it.copy(focusTask = null, focusSeconds = 0, isFocusPaused = false)
        }
        companionController.transitionTo(CompanionState.HAPPY)
    }

    fun completeFocusTask() {
        val ctrl = _control.value
        val target = ctrl.focusTask ?: return
        val cid = uiState.value.prefs.activeChildId
        val mins = ctrl.focusSeconds / 60
        val secs = ctrl.focusSeconds % 60
        val durationLabel = if (mins > 0) "${mins}分${secs}秒" else "${secs}秒"

        focusTimerJob?.cancel()
        _control.update {
            it.copy(focusTask = null, focusSeconds = 0, isFocusPaused = false)
        }
        viewModelScope.launch {
            repository.updateTask(
                target.copy(
                    status = "COMPLETED",
                    feedbackNote = "专注用时 $durationLabel 完成",
                    updatedAt = System.currentTimeMillis()
                )
            )
            if (cid.isNotBlank()) {
                val reply = "太棒啦！你专注完成了【${target.title}】，一共坚持了 $durationLabel！"
                repository.insertConversationMessage(cid, "AI", reply, "FOCUS_COMPLETED", "celebrating")
                if (uiState.value.prefs.voiceAutoPlayEnabled) {
                    speechSynthesizer.speak(reply)
                }
            }
            companionController.transitionTo(CompanionState.CELEBRATING)
        }
    }

    fun confirmSchedulePlan() {
        val cid = uiState.value.prefs.activeChildId
        if (cid.isBlank()) return
        viewModelScope.launch {
            val updated = uiState.value.schedule.map { it.copy(isChildConfirmed = true) }
            repository.reorderSchedule(updated)
            companionController.transitionTo(CompanionState.HAPPY)
            _control.update { it.copy(statusBanner = "已保存你的计划确认，家长审批部分保留草案标识") }
        }
    }

    fun swapScheduleUp(index: Int) {
        val list = uiState.value.schedule.toMutableList()
        if (index <= 0 || index >= list.size) return
        val a = list[index - 1]
        val b = list[index]
        list[index - 1] = b.copy(sortOrder = a.sortOrder)
        list[index] = a.copy(sortOrder = b.sortOrder)
        viewModelScope.launch { repository.reorderSchedule(list) }
    }

    fun updateGoalReflection(goal: GrowthGoalEntity, reflection: String, weeklyAction: String) {
        viewModelScope.launch {
            repository.updateGoal(
                goal.copy(
                    reflectionNote = reflection,
                    weeklyAction = weeklyAction,
                    progressPercent = (goal.progressPercent + 10).coerceAtMost(100)
                )
            )
            companionController.transitionTo(CompanionState.HAPPY)
        }
    }

    fun submitWish(wish: FamilyWishEntity) {
        viewModelScope.launch {
            repository.submitWishToParent(wish)
            companionController.transitionTo(CompanionState.ENCOURAGING)
            _control.update { it.copy(statusBanner = "愿望协商单已发送给家长，审批通过后显示") }
        }
    }

    fun requestRewardRedemption(reward: RewardItemEntity) {
        val state = uiState.value
        viewModelScope.launch {
            repository.requestRewardRedemption(state.prefs.activeChildId, reward, state.totalPoints)
                .onSuccess { msg ->
                    companionController.transitionTo(CompanionState.HAPPY)
                    _control.update { it.copy(statusBanner = msg) }
                }
                .onFailure { err ->
                    _control.update { it.copy(statusBanner = err.message) }
                }
        }
    }

    fun authorizeMicrophone(authorized: Boolean) {
        viewModelScope.launch { prefsStore.setMicAuthorized(authorized) }
    }

    fun saveSubmittedRecording(title: String, filePath: String, durationSeconds: Int) {
        val cid = uiState.value.prefs.activeChildId
        if (cid.isBlank()) return
        viewModelScope.launch {
            val datePart = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
            val cloudId = "CLD-$datePart-${(100..999).random()}"
            repository.saveRecording(
                ReadingRecordingEntity(
                    id = UUID.randomUUID().toString(),
                    childId = cid,
                    passageTitle = title,
                    filePath = filePath,
                    durationSeconds = durationSeconds,
                    uploadStatus = "SYNCED_CLOUD",
                    cloudRecordId = cloudId,
                    createdAt = System.currentTimeMillis()
                )
            )
            companionController.transitionTo(CompanionState.CELEBRATING)
            _control.update { it.copy(statusBanner = "录音已同步至云端（编号 $cloudId）") }
        }
    }

    fun updateCompanionName(name: String) {
        viewModelScope.launch { prefsStore.updateCompanionName(name) }
    }

    fun setVoiceAutoPlay(enabled: Boolean) {
        viewModelScope.launch { prefsStore.setVoiceAutoPlay(enabled) }
    }

    fun clearBanner() {
        _control.update { it.copy(statusBanner = null) }
    }

    fun logout() {
        viewModelScope.launch {
            focusTimerJob?.cancel()
            _control.update { it.copy(focusTask = null, focusSeconds = 0, isFocusPaused = false, isLiveVoiceSession = false) }
            speechSynthesizer.interrupt()
            prefsStore.logout()
            companionController.transitionTo(CompanionState.IDLE)
        }
    }
}
