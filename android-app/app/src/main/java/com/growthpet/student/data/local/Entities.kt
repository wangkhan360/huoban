package com.growthpet.student.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "child_profiles")
data class ChildProfileEntity(
    @PrimaryKey val childId: String,
    val name: String,
    val gradeLevel: Int, // 1 for lower grade, 6 for upper grade
    val gradeBand: String, // "LOWER_GRADE" or "UPPER_GRADE"
    val parentBoundName: String,
    val companionName: String,
    val portraitSummaryJson: String,
    val deviceId: String,
    val updatedAt: Long
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val childId: String,
    val role: String, // "CHILD" or "AI"
    val text: String,
    val intent: String?,
    val companionState: String?,
    val cardsJson: String?,
    val timestamp: Long
)

@Entity(tableName = "task_items")
data class TaskItemEntity(
    @PrimaryKey val id: String,
    val childId: String,
    val title: String,
    val subject: String = "语文",
    val source: String, // "SCHOOL", "PARENT", "HOME_ROUTINE"
    val estimatedMinutes: Int,
    val elapsedSeconds: Int,
    val status: String, // "TODO", "IN_PROGRESS", "COMPLETED", "HELP_REQUESTED", "TIME_ADJUST_REQUESTED"
    val pointsReward: Int,
    val feedbackNote: String?,
    val detailContent: String = "",
    val teacherReminder: String? = null,
    val imageResName: String? = null,
    val updatedAt: Long
)

@Entity(tableName = "schedule_slots")
data class ScheduleSlotEntity(
    @PrimaryKey val id: String,
    val childId: String,
    val period: String, // "MORNING", "HOMEWORK", "REST", "EVENING"
    val startTime: String,
    val endTime: String,
    val title: String,
    val isParentConfirmed: Boolean,
    val isChildConfirmed: Boolean,
    val hasConflict: Boolean,
    val sortOrder: Int
)

@Entity(tableName = "growth_goals")
data class GrowthGoalEntity(
    @PrimaryKey val id: String,
    val childId: String,
    val aspiration: String,
    val roleModel: String,
    val admiredQualities: String,
    val targetSkills: String,
    val stepsJson: String,
    val weeklyAction: String,
    val reflectionNote: String,
    val progressPercent: Int
)

@Entity(tableName = "family_wishes")
data class FamilyWishEntity(
    @PrimaryKey val id: String,
    val childId: String,
    val wishTitle: String,
    val parentSupportNeeded: String,
    val structuredSummary: String,
    val budgetNote: String,
    val timelinePlan: String,
    val approvalStatus: String, // "DRAFT", "PENDING_PARENT", "APPROVED", "REJECTED"
    val parentFeedback: String?
)

@Entity(tableName = "points_records")
data class PointsRecordEntity(
    @PrimaryKey val id: String,
    val childId: String,
    val delta: Int,
    val reason: String,
    val createdAt: Long
)

@Entity(tableName = "reward_items")
data class RewardItemEntity(
    @PrimaryKey val id: String,
    val childId: String,
    val title: String,
    val category: String, // "COMPANIONSHIP", "PLAY_PRIVILEGE", "EXPERIENCE", "PHYSICAL"
    val costPoints: Int,
    val description: String,
    val redeemStatus: String, // "AVAILABLE", "PENDING_PARENT", "APPROVED", "REJECTED"
    val rejectReason: String?
)

@Entity(tableName = "reading_recordings")
data class ReadingRecordingEntity(
    @PrimaryKey val id: String,
    val childId: String,
    val passageTitle: String,
    val filePath: String,
    val durationSeconds: Int,
    val uploadStatus: String, // "LOCAL", "UPLOADING", "SYNCED_CLOUD", "FAILED"
    val cloudRecordId: String? = null,
    val createdAt: Long
)
