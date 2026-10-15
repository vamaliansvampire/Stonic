package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.AlarmClock
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.Conversation
import com.example.data.Message
import com.example.data.ReminderItem
import com.example.data.SettingsRepository
import com.example.service.GeminiService
import com.example.tools.CalculatorUtil
import com.example.tools.SystemDiagnosticsHelper
import com.example.tools.SystemStatus
import com.example.tools.WeatherInfo
import com.example.tools.WeatherService
import com.example.voice.StonicVoiceManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val context: Context get() = getApplication<Application>().applicationContext
    private val database = AppDatabase.getDatabase(context)
    private val chatDao = database.chatDao()
    private val reminderDao = database.reminderDao()
    val settingsRepo = SettingsRepository(context)
    private val geminiService = GeminiService()
    val voiceManager = StonicVoiceManager(context)
    private val weatherService = WeatherService()

    // Active conversation
    private val _currentConversationId = MutableStateFlow<String?>(null)
    val currentConversationId: StateFlow<String?> = _currentConversationId.asStateFlow()

    private val _currentConversationTitle = MutableStateFlow("New Session")
    val currentConversationTitle: StateFlow<String> = _currentConversationTitle.asStateFlow()

    val conversations: StateFlow<List<Conversation>> = chatDao.getAllConversations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentMessages = MutableStateFlow<List<Message>>(emptyList())
    val currentMessages: StateFlow<List<Message>> = _currentMessages.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val filteredConversations: StateFlow<List<Conversation>> = combine(conversations, _searchQuery) { list, query ->
        if (query.isBlank()) list
        else list.filter { it.title.contains(query, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Generation / Streaming state
    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _currentStreamingContent = MutableStateFlow("")
    val currentStreamingContent: StateFlow<String> = _currentStreamingContent.asStateFlow()

    private var activeGenerationJob: Job? = null

    // Attached Image for Vision
    private val _attachedImageUri = MutableStateFlow<Uri?>(null)
    val attachedImageUri: StateFlow<Uri?> = _attachedImageUri.asStateFlow()

    private val _attachedImageBitmap = MutableStateFlow<Bitmap?>(null)
    val attachedImageBitmap: StateFlow<Bitmap?> = _attachedImageBitmap.asStateFlow()

    // API Connection Test
    private val _isTestingConnection = MutableStateFlow(false)
    val isTestingConnection: StateFlow<Boolean> = _isTestingConnection.asStateFlow()

    private val _connectionTestResult = MutableStateFlow<String?>(null)
    val connectionTestResult: StateFlow<String?> = _connectionTestResult.asStateFlow()

    // Reminders
    val reminders: StateFlow<List<ReminderItem>> = reminderDao.getAllReminders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Weather
    private val _selectedCity = MutableStateFlow("New York")
    val selectedCity: StateFlow<String> = _selectedCity.asStateFlow()

    private val _weatherInfo = MutableStateFlow<WeatherInfo?>(null)
    val weatherInfo: StateFlow<WeatherInfo?> = _weatherInfo.asStateFlow()

    private val _isWeatherLoading = MutableStateFlow(false)
    val isWeatherLoading: StateFlow<Boolean> = _isWeatherLoading.asStateFlow()

    // Image Generation Studio
    private val _imageGenPrompt = MutableStateFlow("")
    val imageGenPrompt: StateFlow<String> = _imageGenPrompt.asStateFlow()

    private val _generatedImageUrl = MutableStateFlow<String?>(null)
    val generatedImageUrl: StateFlow<String?> = _generatedImageUrl.asStateFlow()

    private val _isGeneratingImage = MutableStateFlow(false)
    val isGeneratingImage: StateFlow<Boolean> = _isGeneratingImage.asStateFlow()

    // System Diagnostics
    private val _systemStatus = MutableStateFlow(SystemDiagnosticsHelper.getSystemStatus(context))
    val systemStatus: StateFlow<SystemStatus> = _systemStatus.asStateFlow()

    init {
        // Load initial weather
        fetchWeather("New York")

        // Sync voice settings with VoiceManager
        viewModelScope.launch {
            combine(
                settingsRepo.voicePresetFlow,
                settingsRepo.voicePitchFlow,
                settingsRepo.voiceSpeedFlow
            ) { preset, pitch, speed ->
                Triple(preset, pitch, speed)
            }.collect { (preset, pitch, speed) ->
                voiceManager.configureVoice(preset, pitch, speed)
            }
        }
    }

    fun refreshSystemStatus() {
        _systemStatus.value = SystemDiagnosticsHelper.getSystemStatus(context)
    }

    // Conversation management
    fun selectConversation(conversationId: String) {
        _currentConversationId.value = conversationId
        viewModelScope.launch {
            val conv = chatDao.getConversationById(conversationId)
            _currentConversationTitle.value = conv?.title ?: "Chat"
            chatDao.getMessagesForConversation(conversationId).collect {
                _currentMessages.value = it
            }
        }
    }

    fun startNewConversation(title: String = "New Session") {
        val newConv = Conversation(title = title)
        viewModelScope.launch {
            chatDao.insertConversation(newConv)
            selectConversation(newConv.id)
        }
    }

    fun renameConversation(id: String, newTitle: String) {
        viewModelScope.launch {
            val conv = chatDao.getConversationById(id)
            if (conv != null) {
                chatDao.updateConversation(conv.copy(title = newTitle.trim(), updatedAt = System.currentTimeMillis()))
                if (_currentConversationId.value == id) {
                    _currentConversationTitle.value = newTitle.trim()
                }
            }
        }
    }

    fun deleteConversation(id: String) {
        viewModelScope.launch {
            chatDao.deleteConversationById(id)
            if (_currentConversationId.value == id) {
                _currentConversationId.value = null
                _currentMessages.value = emptyList()
            }
        }
    }

    fun clearAllConversations() {
        viewModelScope.launch {
            chatDao.clearAllConversations()
            _currentConversationId.value = null
            _currentMessages.value = emptyList()
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun attachImage(uri: Uri?) {
        _attachedImageUri.value = uri
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                _attachedImageBitmap.value = bitmap
                inputStream?.close()
            } catch (e: Exception) {
                Toast.makeText(context, "Could not load image: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            _attachedImageBitmap.value = null
        }
    }

    // Message generation
    fun sendMessage(userText: String) {
        val prompt = userText.trim()
        if (prompt.isBlank() && _attachedImageBitmap.value == null) return

        viewModelScope.launch {
            // Ensure we have an active conversation
            var convId = _currentConversationId.value
            if (convId == null) {
                val autoTitle = if (prompt.length > 30) prompt.take(30) + "..." else prompt.ifBlank { "Visual Analysis" }
                val newConv = Conversation(title = autoTitle)
                chatDao.insertConversation(newConv)
                convId = newConv.id
                _currentConversationId.value = convId
                _currentConversationTitle.value = autoTitle
            }

            // Save User message
            val userMsg = Message(
                conversationId = convId,
                sender = "USER",
                content = prompt,
                imageUri = _attachedImageUri.value?.toString()
            )
            chatDao.insertMessage(userMsg)

            // Update conversation updatedAt
            val conv = chatDao.getConversationById(convId)
            if (conv != null) {
                chatDao.updateConversation(conv.copy(updatedAt = System.currentTimeMillis()))
            }

            // Clear attached image for next turn
            val imageToAnalyze = _attachedImageBitmap.value
            _attachedImageBitmap.value = null
            _attachedImageUri.value = null

            // Stream response from Gemini
            executeGeminiQuery(convId, prompt, imageToAnalyze)
        }
    }

    private fun executeGeminiQuery(convId: String, prompt: String, imageBitmap: Bitmap?) {
        activeGenerationJob?.cancel()
        _isGenerating.value = true
        _currentStreamingContent.value = ""

        activeGenerationJob = viewModelScope.launch {
            val apiKey = settingsRepo.getEffectiveApiKey()
            val model = settingsRepo.selectedModelFlow.stateIn(viewModelScope).value
            val personality = settingsRepo.personalityFlow.stateIn(viewModelScope).value
            val responseLength = settingsRepo.responseLengthFlow.stateIn(viewModelScope).value
            val autoSpeak = settingsRepo.autoSpeakFlow.stateIn(viewModelScope).value

            val history = _currentMessages.value.map { Pair(it.sender, it.content) }

            val responseBuilder = StringBuilder()

            geminiService.streamGenerateContent(
                apiKey = apiKey,
                model = model,
                history = history,
                userPrompt = prompt,
                imageBitmap = imageBitmap,
                personality = personality,
                responseLength = responseLength
            ).collect { token ->
                responseBuilder.append(token)
                _currentStreamingContent.value = responseBuilder.toString()
            }

            val finalContent = responseBuilder.toString().ifBlank {
                "Stonic received no output for this command, Sir."
            }

            // Save AI message to database
            val aiMsg = Message(
                conversationId = convId,
                sender = "STONIC",
                content = finalContent
            )
            chatDao.insertMessage(aiMsg)

            _isGenerating.value = false
            _currentStreamingContent.value = ""

            // Speak response if autoSpeak is enabled
            if (autoSpeak) {
                voiceManager.speak(finalContent)
            }
        }
    }

    fun stopGeneration() {
        activeGenerationJob?.cancel()
        _isGenerating.value = false
        if (_currentStreamingContent.value.isNotBlank() && _currentConversationId.value != null) {
            val convId = _currentConversationId.value!!
            val partialContent = _currentStreamingContent.value + "\n\n*(Generation halted by user)*"
            viewModelScope.launch {
                chatDao.insertMessage(
                    Message(conversationId = convId, sender = "STONIC", content = partialContent)
                )
                _currentStreamingContent.value = ""
            }
        }
    }

    fun regenerateLastResponse() {
        val messages = _currentMessages.value
        val lastUserMsg = messages.lastOrNull { it.sender == "USER" } ?: return
        val lastAiMsg = messages.lastOrNull { it.sender == "STONIC" }

        viewModelScope.launch {
            if (lastAiMsg != null) {
                chatDao.deleteMessage(lastAiMsg)
            }
            executeGeminiQuery(lastUserMsg.conversationId, lastUserMsg.content, null)
        }
    }

    // Voice command handler
    fun startVoiceCommand() {
        voiceManager.startListening(
            onResult = { recognizedText ->
                sendMessage(recognizedText)
            },
            onError = { err ->
                Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Connection testing
    fun testApiConnection() {
        _isTestingConnection.value = true
        _connectionTestResult.value = null

        viewModelScope.launch {
            val apiKey = settingsRepo.getEffectiveApiKey()
            val model = settingsRepo.selectedModelFlow.stateIn(viewModelScope).value

            val result = geminiService.testConnection(apiKey, model)
            result.fold(
                onSuccess = { latency ->
                    _connectionTestResult.value = "✅ Connected to Gemini ($latency ms)\nModel: $model • Status: Operational"
                },
                onFailure = { error ->
                    _connectionTestResult.value = "❌ Connection Failed:\n${error.message}"
                }
            )
            _isTestingConnection.value = false
        }
    }

    // Reminders
    fun addReminder(title: String, notes: String, dueTime: Long, priority: String) {
        viewModelScope.launch {
            reminderDao.insertReminder(
                ReminderItem(title = title, notes = notes, dueTime = dueTime, priority = priority)
            )
        }
    }

    fun toggleReminderCompleted(reminder: ReminderItem) {
        viewModelScope.launch {
            reminderDao.updateReminder(reminder.copy(isCompleted = !reminder.isCompleted))
        }
    }

    fun deleteReminder(reminder: ReminderItem) {
        viewModelScope.launch {
            reminderDao.deleteReminder(reminder)
        }
    }

    fun setSystemAlarm(hour: Int, minute: Int, message: String) {
        val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_MESSAGE, message)
            putExtra(AlarmClock.EXTRA_HOUR, hour)
            putExtra(AlarmClock.EXTRA_MINUTES, minute)
            putExtra(AlarmClock.EXTRA_SKIP_UI, false)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "No alarm application available on device", Toast.LENGTH_SHORT).show()
        }
    }

    fun setSystemTimer(seconds: Int, message: String) {
        val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
            putExtra(AlarmClock.EXTRA_MESSAGE, message)
            putExtra(AlarmClock.EXTRA_LENGTH, seconds)
            putExtra(AlarmClock.EXTRA_SKIP_UI, false)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "No timer application available on device", Toast.LENGTH_SHORT).show()
        }
    }

    // Weather
    fun fetchWeather(city: String) {
        _selectedCity.value = city
        _isWeatherLoading.value = true
        viewModelScope.launch {
            val result = weatherService.fetchWeather(city)
            result.fold(
                onSuccess = { info ->
                    _weatherInfo.value = info
                    _isWeatherLoading.value = false
                },
                onFailure = {
                    _isWeatherLoading.value = false
                }
            )
        }
    }

    // Image Generation
    fun generateImage(prompt: String, style: String = "Cyberpunk Sci-Fi") {
        if (prompt.isBlank()) return
        _imageGenPrompt.value = prompt
        _isGeneratingImage.value = true
        _generatedImageUrl.value = null

        viewModelScope.launch {
            try {
                val enhancedPrompt = "$prompt, $style, ultra-detailed, cinematic lighting, 8k resolution, futuristic concept art"
                val encoded = URLEncoder.encode(enhancedPrompt, StandardCharsets.UTF_8.toString())
                val seed = (1000..99999).random()
                val url = "https://image.pollinations.ai/prompt/$encoded?seed=$seed&width=1024&height=1024&nologo=true"
                _generatedImageUrl.value = url
            } catch (_: Exception) {
                Toast.makeText(context, "Failed to initiate image generation", Toast.LENGTH_SHORT).show()
            } finally {
                _isGeneratingImage.value = false
            }
        }
    }

    fun setHandsFree(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepo.setHandsFree(enabled)
        }
    }

    fun setAutoSpeak(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepo.setAutoSpeak(enabled)
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.destroy()
    }
}
