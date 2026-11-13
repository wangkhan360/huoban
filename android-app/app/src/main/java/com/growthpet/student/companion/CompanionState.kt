package com.growthpet.student.companion

enum class CompanionState(
    val wireKey: String,
    val labelLowerGrade: String,
    val labelUpperGrade: String
) {
    IDLE("idle", "云龙正在陪着你", "待命中"),
    LISTENING("listening", "云龙正在认真听你说", "正在倾听"),
    THINKING("thinking", "云龙在动脑筋想一想", "思考中"),
    SPEAKING("speaking", "云龙正在和你说话", "语音播报中"),
    HAPPY("happy", "云龙好开心呀", "心情愉快"),
    ENCOURAGING("encouraging", "云龙给你加油打气", "陪伴鼓励"),
    CELEBRATING("celebrating", "太棒啦！云龙为你庆祝", "达成庆祝"),
    RESTING("resting", "云龙休息一会儿", "休眠休憩");

    companion object {
        fun fromWireKey(key: String?): CompanionState =
            entries.firstOrNull { it.wireKey.equals(key, ignoreCase = true) } ?: IDLE
    }
}
