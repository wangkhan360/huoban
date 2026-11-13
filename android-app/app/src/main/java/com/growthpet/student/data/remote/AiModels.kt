package com.growthpet.student.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AiStructuredResponse(
    @SerialName("reply_text") val replyText: String,
    @SerialName("intent") val intent: String,
    @SerialName("companion_state") val companionState: String,
    @SerialName("cards") val cards: List<DynamicCardPayload> = emptyList(),
    @SerialName("suggested_actions") val suggestedActions: List<String> = emptyList(),
    @SerialName("requires_confirmation") val requiresConfirmation: Boolean = false,
    @SerialName("is_mock_response") val isMockResponse: Boolean = true
)

@Serializable
data class DynamicCardPayload(
    @SerialName("card_type") val cardType: String, // TASK_LIST, TIME_PLAN, GROWTH_GOAL, WISH_NEGOTIATION, POINTS_REWARDS, ENGLISH_READING
    @SerialName("title") val title: String,
    @SerialName("focus_id") val focusId: String? = null
)

@Serializable
data class ChatRequestDto(
    @SerialName("child_id") val childId: String,
    @SerialName("grade_band") val gradeBand: String,
    @SerialName("message") val message: String,
    @SerialName("companion_name") val companionName: String
)

@Serializable
data class VerifyBindingRequestDto(
    @SerialName("verification_code") val verificationCode: String,
    @SerialName("device_id") val deviceId: String
)

@Serializable
data class VerifyBindingResponseDto(
    @SerialName("child_id") val childId: String,
    @SerialName("child_name") val childName: String,
    @SerialName("grade_level") val gradeLevel: Int,
    @SerialName("grade_band") val gradeBand: String,
    @SerialName("parent_name") val parentName: String
)

@Serializable
data class PointsSummaryDto(
    @SerialName("child_id") val childId: String,
    @SerialName("total_points") val totalPoints: Int,
    @SerialName("server_verified") val serverVerified: Boolean
)
