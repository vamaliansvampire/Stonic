package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.BuildConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "stonic_settings")

class SettingsRepository(private val context: Context) {

    private object PreferencesKeys {
        val API_KEY = stringPreferencesKey("gemini_api_key")
        val SELECTED_MODEL = stringPreferencesKey("selected_model")
        val PERSONALITY = stringPreferencesKey("personality")
        val RESPONSE_LENGTH = stringPreferencesKey("response_length")
        val VOICE_PRESET = stringPreferencesKey("voice_preset")
        val VOICE_PITCH = floatPreferencesKey("voice_pitch")
        val VOICE_SPEED = floatPreferencesKey("voice_speed")
        val AUTO_SPEAK = booleanPreferencesKey("auto_speak")
        val HANDS_FREE = booleanPreferencesKey("hands_free")
    }

    private val buildConfigFallbackKey: String
        get() {
            return try {
                val field = BuildConfig::class.java.getField("GEMINI_API_KEY")
                (field.get(null) as? String)?.takeIf { it.isNotBlank() && !it.contains("MY_GEMINI_API_KEY") } ?: ""
            } catch (_: Exception) {
                ""
            }
        }

    val apiKeyFlow: Flow<String> = context.dataStore.data.map { preferences ->
        val saved = preferences[PreferencesKeys.API_KEY] ?: ""
        if (saved.isNotBlank()) saved else buildConfigFallbackKey
    }

    val rawSavedApiKeyFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.API_KEY] ?: ""
    }

    val selectedModelFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SELECTED_MODEL] ?: "gemini-2.5-flash"
    }

    val personalityFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.PERSONALITY] ?: "JARVIS Stonic (Sophisticated & Confident)"
    }

    val responseLengthFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.RESPONSE_LENGTH] ?: "Balanced"
    }

    val voicePresetFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.VOICE_PRESET] ?: "Male Stonic (Deep & Confident)"
    }

    val voicePitchFlow: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.VOICE_PITCH] ?: 0.85f
    }

    val voiceSpeedFlow: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.VOICE_SPEED] ?: 1.02f
    }

    val autoSpeakFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.AUTO_SPEAK] ?: true
    }

    val handsFreeFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.HANDS_FREE] ?: false
    }

    suspend fun getEffectiveApiKey(): String {
        return apiKeyFlow.first()
    }

    suspend fun setApiKey(key: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.API_KEY] = key.trim()
        }
    }

    suspend fun clearApiKey() {
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.API_KEY)
        }
    }

    suspend fun setModel(model: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_MODEL] = model
        }
    }

    suspend fun setPersonality(personality: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PERSONALITY] = personality
        }
    }

    suspend fun setResponseLength(length: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.RESPONSE_LENGTH] = length
        }
    }

    suspend fun setVoiceSettings(preset: String, pitch: Float, speed: Float) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.VOICE_PRESET] = preset
            preferences[PreferencesKeys.VOICE_PITCH] = pitch
            preferences[PreferencesKeys.VOICE_SPEED] = speed
        }
    }

    suspend fun setAutoSpeak(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_SPEAK] = enabled
        }
    }

    suspend fun setHandsFree(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HANDS_FREE] = enabled
        }
    }
}
