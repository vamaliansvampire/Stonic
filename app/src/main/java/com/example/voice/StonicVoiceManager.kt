package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class StonicVoiceManager(private val context: Context) : TextToSpeech.OnInitListener, RecognitionListener {

    private var tts: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null

    private val _isTtsReady = MutableStateFlow(false)
    val isTtsReady: StateFlow<Boolean> = _isTtsReady.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _audioWaveLevel = MutableStateFlow(0f)
    val audioWaveLevel: StateFlow<Float> = _audioWaveLevel.asStateFlow()

    private val _lastSpokenText = MutableStateFlow("")
    val lastSpokenText: StateFlow<String> = _lastSpokenText.asStateFlow()

    private var currentPitch: Float = 0.85f // Deeper, confident JARVIS / Pocheed tone
    private var currentSpeed: Float = 1.02f
    private var currentPreset: String = "Male Stonic (Deep & Confident)"

    private var onSpeechResultCallback: ((String) -> Unit)? = null
    private var onSpeechErrorCallback: ((String) -> Unit)? = null

    init {
        tts = TextToSpeech(context.applicationContext, this)
        initSpeechRecognizer()
    }

    private fun initSpeechRecognizer() {
        try {
            if (SpeechRecognizer.isRecognitionAvailable(context)) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(this@StonicVoiceManager)
                }
            }
        } catch (e: Exception) {
            Log.e("StonicVoice", "Error initializing speech recognizer: ${e.message}")
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            configureVoice(currentPreset, currentPitch, currentSpeed)
            _isTtsReady.value = true

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                    _audioWaveLevel.value = 0.65f
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                    _audioWaveLevel.value = 0f
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                    _audioWaveLevel.value = 0f
                }
            })
        }
    }

    fun configureVoice(preset: String, pitch: Float, speed: Float) {
        currentPreset = preset
        currentPitch = pitch
        currentSpeed = speed

        tts?.let { engine ->
            engine.language = Locale.US

            // Apply pitch and speed
            val finalPitch = when (preset) {
                "Male Stonic (Deep & Confident)" -> 0.80f.coerceIn(0.5f, 2.0f)
                "Male Tech (Smooth)" -> 0.92f.coerceIn(0.5f, 2.0f)
                "System Male" -> 0.88f
                else -> pitch.coerceIn(0.5f, 2.0f)
            }
            engine.setPitch(finalPitch)
            engine.setSpeechRate(speed.coerceIn(0.5f, 2.0f))

            // Attempt to select an English male voice from installed TTS voices
            try {
                val voices = engine.voices
                if (voices != null && voices.isNotEmpty()) {
                    val maleVoice = voices.firstOrNull { voice ->
                        voice.locale.language == "en" &&
                                (voice.name.contains("male", ignoreCase = true) ||
                                        voice.name.contains("en-gb-x-rjs", ignoreCase = true) ||
                                        voice.name.contains("en-us-x-sfg", ignoreCase = true) ||
                                        voice.name.contains("en-us-x-iom", ignoreCase = true)) &&
                                !voice.name.contains("female", ignoreCase = true)
                    } ?: voices.firstOrNull { voice ->
                        voice.locale.language == "en" && !voice.name.contains("female", ignoreCase = true)
                    }

                    if (maleVoice != null) {
                        engine.voice = maleVoice
                    }
                }
            } catch (e: Exception) {
                Log.w("StonicVoice", "Could not set custom voice: ${e.message}")
            }
        }
    }

    fun speak(text: String, onDone: (() -> Unit)? = null) {
        val cleanText = sanitizeTextForSpeech(text)
        if (cleanText.isBlank()) return

        _lastSpokenText.value = cleanText
        stopListening() // stop listening while speaking to avoid feedback

        val utteranceId = "stonic_speech_${System.currentTimeMillis()}"
        val params = Bundle()
        tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
    }

    fun replay() {
        if (_lastSpokenText.value.isNotBlank()) {
            speak(_lastSpokenText.value)
        }
    }

    fun stopSpeaking() {
        tts?.stop()
        _isSpeaking.value = false
        _audioWaveLevel.value = 0f
    }

    fun testVoice(preset: String, pitch: Float, speed: Float) {
        configureVoice(preset, pitch, speed)
        speak("Stonic online. All systems are fully operational and awaiting your command, Sir.")
    }

    fun startListening(
        onResult: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        stopSpeaking() // interrupt speech when user starts talking
        onSpeechResultCallback = onResult
        onSpeechErrorCallback = onError

        if (speechRecognizer == null) {
            initSpeechRecognizer()
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Stonic is listening...")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        try {
            speechRecognizer?.startListening(intent)
            _isListening.value = true
        } catch (e: Exception) {
            _isListening.value = false
            onError("Microphone error: ${e.message}")
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (_: Exception) {}
        _isListening.value = false
        _audioWaveLevel.value = 0f
    }

    private fun sanitizeTextForSpeech(text: String): String {
        return text
            // Remove code blocks
            .replace(Regex("```[\\s\\S]*?```"), "Code block omitted from speech.")
            // Remove inline code
            .replace(Regex("`([^`]+)`"), "$1")
            // Remove markdown bold/italics
            .replace(Regex("[*_~]"), "")
            // Remove markdown headers
            .replace(Regex("#+\\s*"), "")
            // Remove markdown links [title](url) -> title
            .replace(Regex("\\[([^\\]]+)\\]\\([^\\)]+\\)"), "$1")
            // Clean emojis / symbols
            .replace(Regex("[⚠️💡✨🚀🤖⚡]"), "")
            .trim()
    }

    // RecognitionListener Callbacks
    override fun onReadyForSpeech(params: Bundle?) {
        _isListening.value = true
    }

    override fun onBeginningOfSpeech() {
        _isListening.value = true
    }

    override fun onRmsChanged(rmsdB: Float) {
        // Normalize RMS dB (typically -2 to 10 dB) to 0f - 1f
        val level = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
        _audioWaveLevel.value = level
    }

    override fun onBufferReceived(buffer: ByteArray?) {}

    override fun onEndOfSpeech() {
        _isListening.value = false
        _audioWaveLevel.value = 0f
    }

    override fun onError(error: Int) {
        _isListening.value = false
        _audioWaveLevel.value = 0f
        val message = when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required"
            SpeechRecognizer.ERROR_NETWORK -> "Network connection error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Please try again."
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Voice recognizer is busy"
            SpeechRecognizer.ERROR_SERVER -> "Server error"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected"
            else -> "Voice recognition error ($error)"
        }
        onSpeechErrorCallback?.invoke(message)
    }

    override fun onResults(results: Bundle?) {
        _isListening.value = false
        _audioWaveLevel.value = 0f
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val text = matches?.firstOrNull()?.trim() ?: ""
        if (text.isNotBlank()) {
            onSpeechResultCallback?.invoke(text)
        }
    }

    override fun onPartialResults(partialResults: Bundle?) {
        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val partialText = matches?.firstOrNull()
        if (partialText != null) {
            _audioWaveLevel.value = 0.5f
        }
    }

    override fun onEvent(eventType: Int, params: Bundle?) {}

    fun destroy() {
        try {
            tts?.stop()
            tts?.shutdown()
            speechRecognizer?.destroy()
        } catch (_: Exception) {}
    }
}
