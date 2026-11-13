package com.growthpet.student.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        ChildProfileEntity::class,
        ChatMessageEntity::class,
        TaskItemEntity::class,
        ScheduleSlotEntity::class,
        GrowthGoalEntity::class,
        FamilyWishEntity::class,
        PointsRecordEntity::class,
        RewardItemEntity::class,
        ReadingRecordingEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao
}
