package com.growthpet.student.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "growth_pet_prefs")

data class StudentSessionPrefs(
    val isBoundAndOnboarded: Boolean = false,
    val activeChildId: String = "",
    val companionName: String = "云龙",
    val voiceAutoPlayEnabled: Boolean = true,
    val micAuthorizedByChild: Boolean = false,
    val isMockMode: Boolean = true,
    val ownerWakeOnlyEnabled: Boolean = false,
    val ownerVoiceprintEnrolled: Boolean = false,
    val ownerWakePhrase: String = "云龙你好，我是乐乐",
    val ownerVoiceprintSamplePath: String = ""
)

@Singleton
class UserPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val IS_ONBOARDED = booleanPreferencesKey("is_onboarded")
        val ACTIVE_CHILD_ID = stringPreferencesKey("active_child_id")
        val COMPANION_NAME = stringPreferencesKey("companion_name")
        val VOICE_AUTO_PLAY = booleanPreferencesKey("voice_auto_play")
        val MIC_AUTHORIZED = booleanPreferencesKey("mic_authorized")
        val IS_MOCK_MODE = booleanPreferencesKey("is_mock_mode")
        val OWNER_WAKE_ONLY = booleanPreferencesKey("owner_wake_only")
        val OWNER_VOICEPRINT_ENROLLED = booleanPreferencesKey("owner_voiceprint_enrolled")
        val OWNER_WAKE_PHRASE = stringPreferencesKey("owner_wake_phrase")
        val OWNER_VOICEPRINT_SAMPLE_PATH = stringPreferencesKey("owner_voiceprint_sample_path")
    }

    val sessionPrefs: Flow<StudentSessionPrefs> = context.dataStore.data.map { prefs ->
        StudentSessionPrefs(
            isBoundAndOnboarded = prefs[Keys.IS_ONBOARDED] ?: false,
            activeChildId = prefs[Keys.ACTIVE_CHILD_ID] ?: "",
            companionName = prefs[Keys.COMPANION_NAME] ?: "云龙",
            voiceAutoPlayEnabled = prefs[Keys.VOICE_AUTO_PLAY] ?: true,
            micAuthorizedByChild = prefs[Keys.MIC_AUTHORIZED] ?: false,
            isMockMode = prefs[Keys.IS_MOCK_MODE] ?: true,
            ownerWakeOnlyEnabled = prefs[Keys.OWNER_WAKE_ONLY] ?: false,
            ownerVoiceprintEnrolled = prefs[Keys.OWNER_VOICEPRINT_ENROLLED] ?: false,
            ownerWakePhrase = prefs[Keys.OWNER_WAKE_PHRASE] ?: "云龙你好，我是乐乐",
            ownerVoiceprintSamplePath = prefs[Keys.OWNER_VOICEPRINT_SAMPLE_PATH] ?: ""
        )
    }

    suspend fun completeOnboarding(childId: String, companionName: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_ONBOARDED] = true
            prefs[Keys.ACTIVE_CHILD_ID] = childId
            prefs[Keys.COMPANION_NAME] = companionName
        }
    }

    suspend fun updateCompanionName(name: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.COMPANION_NAME] = name
        }
    }

    suspend fun setVoiceAutoPlay(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.VOICE_AUTO_PLAY] = enabled
        }
    }

    suspend fun setMicAuthorized(authorized: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.MIC_AUTHORIZED] = authorized
        }
    }

    suspend fun setOwnerWakeOnly(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.OWNER_WAKE_ONLY] = enabled
        }
    }

    suspend fun saveOwnerVoiceprint(wakePhrase: String, samplePath: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.OWNER_WAKE_PHRASE] = wakePhrase
            prefs[Keys.OWNER_VOICEPRINT_SAMPLE_PATH] = samplePath
            prefs[Keys.OWNER_VOICEPRINT_ENROLLED] = true
            prefs[Keys.OWNER_WAKE_ONLY] = true
        }
    }

    suspend fun logout() {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_ONBOARDED] = false
            prefs[Keys.ACTIVE_CHILD_ID] = ""
            prefs[Keys.MIC_AUTHORIZED] = false
            prefs[Keys.OWNER_WAKE_ONLY] = false
            prefs[Keys.OWNER_VOICEPRINT_ENROLLED] = false
            prefs[Keys.OWNER_VOICEPRINT_SAMPLE_PATH] = ""
        }
    }
}
