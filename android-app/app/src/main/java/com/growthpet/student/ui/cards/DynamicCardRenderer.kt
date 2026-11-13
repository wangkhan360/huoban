package com.growthpet.student.ui.cards

import androidx.compose.runtime.Composable
import com.growthpet.student.ui.StudentUiState

@Composable
fun DynamicCardRenderer(
    cardsToken: String?,
    uiState: StudentUiState,
    isLowerGrade: Boolean,
    isRecording: Boolean,
    isPlayingPreview: Boolean,
    onUpdateTask: (com.growthpet.student.data.local.TaskItemEntity, String, String?) -> Unit,
    onStartVoiceCheckin: (com.growthpet.student.data.local.TaskItemEntity) -> Unit,
    onMoveScheduleUp: (Int) -> Unit,
    onConfirmSchedule: () -> Unit,
    onUpdateGoal: (com.growthpet.student.data.local.GrowthGoalEntity, String, String) -> Unit,
    onSubmitWish: (com.growthpet.student.data.local.FamilyWishEntity) -> Unit,
    onRedeemReward: (com.growthpet.student.data.local.RewardItemEntity) -> Unit,
    onAuthorizeMic: () -> Unit,
    onStartRecord: () -> Unit,
    onStopRecord: () -> Unit,
    onPlayPreview: () -> Unit,
    onSubmitRecording: (String, Int) -> Unit
) {
    if (cardsToken.isNullOrBlank()) return
    val types = cardsToken.split(",").mapNotNull { it.substringBefore(":").takeIf(String::isNotBlank) }

    types.forEach { type ->
        when (type) {
            "TASK_LIST" -> TaskManagementCard(
                tasks = uiState.tasks,
                isLowerGrade = isLowerGrade,
                onUpdateTask = onUpdateTask,
                onStartVoiceCheckin = onStartVoiceCheckin
            )
            "TIME_PLAN" -> TimePlanningCard(
                slots = uiState.schedule,
                onMoveUp = onMoveScheduleUp,
                onConfirmPlan = onConfirmSchedule
            )
            "GROWTH_GOAL" -> GrowthGoalCard(
                goals = uiState.goals,
                onUpdateGoal = onUpdateGoal
            )
            "WISH_NEGOTIATION" -> WishNegotiationCard(
                wishes = uiState.wishes,
                onSubmitWish = onSubmitWish
            )
            "POINTS_REWARDS" -> PointsRewardsCard(
                totalPoints = uiState.totalPoints,
                records = uiState.pointsRecords,
                rewards = uiState.rewards,
                onRequestRedeem = onRedeemReward
            )
            "ENGLISH_READING" -> EnglishReadingCard(
                micAuthorized = uiState.prefs.micAuthorizedByChild,
                isRecording = isRecording,
                isPlayingPreview = isPlayingPreview,
                onAuthorizeMic = onAuthorizeMic,
                onStartRecord = onStartRecord,
                onStopRecord = onStopRecord,
                onPlayPreview = onPlayPreview,
                onSubmitRecording = onSubmitRecording
            )
        }
    }
}
