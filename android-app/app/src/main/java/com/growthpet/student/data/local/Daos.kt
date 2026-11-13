package com.growthpet.student.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Query("SELECT * FROM child_profiles WHERE childId = :childId LIMIT 1")
    fun observeProfile(childId: String): Flow<ChildProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: ChildProfileEntity)

    @Query("SELECT * FROM chat_messages WHERE childId = :childId ORDER BY timestamp ASC")
    fun observeMessages(childId: String): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Query("DELETE FROM chat_messages WHERE childId = :childId")
    suspend fun clearMessages(childId: String)

    @Query("SELECT * FROM task_items WHERE childId = :childId ORDER BY updatedAt ASC")
    fun observeTasks(childId: String): Flow<List<TaskItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTasks(tasks: List<TaskItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTask(task: TaskItemEntity)

    @Query("SELECT * FROM schedule_slots WHERE childId = :childId ORDER BY sortOrder ASC")
    fun observeSchedule(childId: String): Flow<List<ScheduleSlotEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSchedule(slots: List<ScheduleSlotEntity>)

    @Query("SELECT * FROM growth_goals WHERE childId = :childId")
    fun observeGoals(childId: String): Flow<List<GrowthGoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertGoal(goal: GrowthGoalEntity)

    @Query("SELECT * FROM family_wishes WHERE childId = :childId")
    fun observeWishes(childId: String): Flow<List<FamilyWishEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWish(wish: FamilyWishEntity)

    @Query("SELECT * FROM points_records WHERE childId = :childId ORDER BY createdAt DESC")
    fun observePointsRecords(childId: String): Flow<List<PointsRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPointsRecords(records: List<PointsRecordEntity>)

    @Query("SELECT * FROM reward_items WHERE childId = :childId")
    fun observeRewards(childId: String): Flow<List<RewardItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRewards(rewards: List<RewardItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertReward(reward: RewardItemEntity)

    @Query("SELECT * FROM reading_recordings WHERE childId = :childId ORDER BY createdAt DESC")
    fun observeRecordings(childId: String): Flow<List<ReadingRecordingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRecording(recording: ReadingRecordingEntity)
}
