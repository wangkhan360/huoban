package com.growthpet.student

import com.growthpet.student.companion.CompanionState
import org.junit.Assert.assertEquals
import org.junit.Test

class MockConversationLogicTest {
    @Test
    fun companionStateFromWireKey_mapsAllEightStatesCorrectly() {
        assertEquals(CompanionState.IDLE, CompanionState.fromWireKey("idle"))
        assertEquals(CompanionState.LISTENING, CompanionState.fromWireKey("listening"))
        assertEquals(CompanionState.THINKING, CompanionState.fromWireKey("thinking"))
        assertEquals(CompanionState.SPEAKING, CompanionState.fromWireKey("speaking"))
        assertEquals(CompanionState.HAPPY, CompanionState.fromWireKey("happy"))
        assertEquals(CompanionState.ENCOURAGING, CompanionState.fromWireKey("encouraging"))
        assertEquals(CompanionState.CELEBRATING, CompanionState.fromWireKey("celebrating"))
        assertEquals(CompanionState.RESTING, CompanionState.fromWireKey("resting"))
        assertEquals(CompanionState.IDLE, CompanionState.fromWireKey("unknown_state"))
    }
}
