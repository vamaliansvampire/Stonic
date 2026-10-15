package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.HudCard
import com.example.ui.theme.StonicAlert
import com.example.ui.theme.StonicCardBorder
import com.example.ui.theme.StonicCyan
import com.example.ui.theme.StonicCyanBright
import com.example.ui.theme.StonicDarkBg
import com.example.ui.theme.StonicDarkSurface
import com.example.ui.theme.StonicDarkSurfaceVariant
import com.example.ui.theme.StonicNeonPurple
import com.example.ui.theme.StonicSuccess
import com.example.ui.theme.StonicTextPrimary
import com.example.ui.theme.StonicTextSecondary
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val rawApiKey by viewModel.settingsRepo.rawSavedApiKeyFlow.collectAsState(initial = "")
    val effectiveApiKey by viewModel.settingsRepo.apiKeyFlow.collectAsState(initial = "")
    val selectedModel by viewModel.settingsRepo.selectedModelFlow.collectAsState(initial = "gemini-2.5-flash")
    val personality by viewModel.settingsRepo.personalityFlow.collectAsState(initial = "JARVIS Stonic (Sophisticated & Confident)")
    val responseLength by viewModel.settingsRepo.responseLengthFlow.collectAsState(initial = "Balanced")
    val voicePreset by viewModel.settingsRepo.voicePresetFlow.collectAsState(initial = "Male Stonic (Deep & Confident)")
    val voicePitch by viewModel.settingsRepo.voicePitchFlow.collectAsState(initial = 0.85f)
    val voiceSpeed by viewModel.settingsRepo.voiceSpeedFlow.collectAsState(initial = 1.02f)
    val autoSpeak by viewModel.settingsRepo.autoSpeakFlow.collectAsState(initial = true)
    val handsFree by viewModel.settingsRepo.handsFreeFlow.collectAsState(initial = false)

    val isTesting by viewModel.isTestingConnection.collectAsState()
    val testResult by viewModel.connectionTestResult.collectAsState()

    var apiKeyInput by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    var sliderPitch by remember { mutableFloatStateOf(voicePitch) }
    var sliderSpeed by remember { mutableFloatStateOf(voiceSpeed) }

    LaunchedEffect(rawApiKey) {
        apiKeyInput = rawApiKey
    }

    LaunchedEffect(voicePitch, voiceSpeed) {
        sliderPitch = voicePitch
        sliderSpeed = voiceSpeed
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StonicDarkBg)
    ) {
        TopAppBar(
            title = {
                Column {
                    Text("SYSTEM CONFIGURATION", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("API Credentials & Neural Matrix", color = StonicCyan, fontSize = 11.sp)
                }
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = StonicCyan)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = StonicDarkSurface)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. API Credentials Card
            HudCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = StonicCyan, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("GEMINI API CREDENTIALS", color = StonicCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Text(
                        text = "Enter your personal Gemini API key. Stonic encrypts and stores credentials strictly inside your device's protected DataStore.",
                        color = StonicTextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )

                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = { apiKeyInput = it },
                        label = { Text("Gemini API Key") },
                        placeholder = { Text("AIzaSy...") },
                        singleLine = true,
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle visibility",
                                    tint = StonicTextSecondary
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StonicCyan,
                            unfocusedBorderColor = StonicCardBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = StonicCyan
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("api_key_input")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    viewModel.settingsRepo.setApiKey(apiKeyInput)
                                    Toast.makeText(context, "API Key saved securely", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StonicCyan, contentColor = StonicDarkBg),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("save_api_key_button")
                        ) {
                            Text("Save Key", fontWeight = FontWeight.Bold)
                        }

                        if (apiKeyInput.isNotBlank()) {
                            OutlinedButton(
                                onClick = {
                                    coroutineScope.launch {
                                        viewModel.settingsRepo.clearApiKey()
                                        apiKeyInput = ""
                                        Toast.makeText(context, "API Key cleared", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(0.7f)
                            ) {
                                Text("Clear", color = StonicAlert)
                            }
                        }
                    }

                    // Test Connection Button
                    Button(
                        onClick = { viewModel.testApiConnection() },
                        enabled = !isTesting,
                        colors = ButtonDefaults.buttonColors(containerColor = StonicDarkSurfaceVariant),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("test_connection_button")
                    ) {
                        if (isTesting) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = StonicCyan, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Pinging Gemini Neural Link...", color = StonicCyanBright)
                        } else {
                            Icon(imageVector = Icons.Default.NetworkCheck, contentDescription = null, tint = StonicCyan, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Test API Connection", color = StonicCyanBright, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    if (testResult != null) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (testResult!!.startsWith("✅")) StonicDarkBg else StonicAlert.copy(alpha = 0.15f)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (testResult!!.startsWith("✅")) StonicSuccess else StonicAlert
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = testResult!!,
                                color = if (testResult!!.startsWith("✅")) StonicSuccess else StonicAlert,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            }

            // 2. Model Selection Card
            HudCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("ACTIVE GEMINI INTELLIGENCE MODEL", color = StonicCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                    val models = listOf("gemini-2.5-flash", "gemini-2.5-pro", "gemini-2.0-flash", "gemini-1.5-flash")
                    models.forEach { modelName ->
                        Card(
                            onClick = {
                                coroutineScope.launch {
                                    viewModel.settingsRepo.setModel(modelName)
                                }
                            },
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedModel == modelName) StonicDarkSurfaceVariant else StonicDarkBg
                            ),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (selectedModel == modelName) StonicCyan else StonicCardBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("model_select_$modelName")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = modelName,
                                        color = if (selectedModel == modelName) StonicCyanBright else StonicTextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = when (modelName) {
                                            "gemini-2.5-flash" -> "Optimal speed, high multimodal reasoning"
                                            "gemini-2.5-pro" -> "Maximum analytical depth and coding intelligence"
                                            "gemini-2.0-flash" -> "Next-gen low-latency multimodal agent"
                                            else -> "Fast conversational performance"
                                        },
                                        color = StonicTextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                                if (selectedModel == modelName) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = "Active", tint = StonicCyan)
                                }
                            }
                        }
                    }
                }
            }

            // 3. Male Pocheed-Style Voice Synthesizer
            HudCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.RecordVoiceOver, contentDescription = null, tint = StonicCyan, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("MALE POCHEED-STYLE VOICE ENGINE", color = StonicCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Text("VOICE PRESET", color = StonicTextSecondary, fontSize = 11.sp)
                    val presets = listOf(
                        "Male Stonic (Deep & Confident)",
                        "Male Tech (Smooth)",
                        "System Male",
                        "Custom"
                    )
                    presets.forEach { preset ->
                        FilterChip(
                            selected = voicePreset == preset,
                            onClick = {
                                coroutineScope.launch {
                                    val newPitch = when (preset) {
                                        "Male Stonic (Deep & Confident)" -> 0.80f
                                        "Male Tech (Smooth)" -> 0.92f
                                        else -> sliderPitch
                                    }
                                    val newSpeed = 1.02f
                                    sliderPitch = newPitch
                                    sliderSpeed = newSpeed
                                    viewModel.settingsRepo.setVoiceSettings(preset, newPitch, newSpeed)
                                }
                            },
                            label = { Text(preset, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = StonicCyan,
                                selectedLabelColor = StonicDarkBg,
                                containerColor = StonicDarkSurfaceVariant,
                                labelColor = StonicTextPrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Pitch Slider
                    Text(
                        text = "Vocal Pitch Resonance: %.2fx (Deeper <-> Higher)".format(sliderPitch),
                        color = StonicTextSecondary,
                        fontSize = 12.sp
                    )
                    Slider(
                        value = sliderPitch,
                        onValueChange = { sliderPitch = it },
                        onValueChangeFinished = {
                            coroutineScope.launch {
                                viewModel.settingsRepo.setVoiceSettings("Custom", sliderPitch, sliderSpeed)
                            }
                        },
                        valueRange = 0.5f..1.5f,
                        colors = SliderDefaults.colors(
                            thumbColor = StonicCyan,
                            activeTrackColor = StonicCyan,
                            inactiveTrackColor = StonicCardBorder
                        )
                    )

                    // Speed Slider
                    Text(
                        text = "Vocal Tempo Rate: %.2fx".format(sliderSpeed),
                        color = StonicTextSecondary,
                        fontSize = 12.sp
                    )
                    Slider(
                        value = sliderSpeed,
                        onValueChange = { sliderSpeed = it },
                        onValueChangeFinished = {
                            coroutineScope.launch {
                                viewModel.settingsRepo.setVoiceSettings("Custom", sliderPitch, sliderSpeed)
                            }
                        },
                        valueRange = 0.6f..1.8f,
                        colors = SliderDefaults.colors(
                            thumbColor = StonicCyan,
                            activeTrackColor = StonicCyan,
                            inactiveTrackColor = StonicCardBorder
                        )
                    )

                    // Test Voice Button
                    Button(
                        onClick = {
                            viewModel.voiceManager.testVoice(voicePreset, sliderPitch, sliderSpeed)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StonicDarkSurfaceVariant),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🔊 Test Vocal Transmission", color = StonicCyanBright, fontWeight = FontWeight.Bold)
                    }

                    // Auto Speak Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Auto-Speak AI Responses", color = StonicTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text("Stonic vocalizes all generated intelligence", color = StonicTextSecondary, fontSize = 11.sp)
                        }
                        Switch(
                            checked = autoSpeak,
                            onCheckedChange = { viewModel.setAutoSpeak(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = StonicDarkBg,
                                checkedTrackColor = StonicCyan
                            )
                        )
                    }
                }
            }

            // 4. Assistant Personality & Length
            HudCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Tune, contentDescription = null, tint = StonicCyan, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("TACTICAL PERSONALITY MATRIX", color = StonicCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    listOf(
                        "JARVIS Stonic (Sophisticated & Confident)",
                        "Concise Tech Specialist",
                        "Creative Synthesizer",
                        "Direct Executive"
                    ).forEach { p ->
                        FilterChip(
                            selected = personality == p,
                            onClick = { coroutineScope.launch { viewModel.settingsRepo.setPersonality(p) } },
                            label = { Text(p, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = StonicNeonPurple,
                                selectedLabelColor = Color.White,
                                containerColor = StonicDarkSurfaceVariant,
                                labelColor = StonicTextPrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("RESPONSE VERBOSITY", color = StonicTextSecondary, fontSize = 11.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Concise", "Balanced", "Detailed").forEach { len ->
                            FilterChip(
                                selected = responseLength == len,
                                onClick = { coroutineScope.launch { viewModel.settingsRepo.setResponseLength(len) } },
                                label = { Text(len, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = StonicCyan,
                                    selectedLabelColor = StonicDarkBg,
                                    containerColor = StonicDarkSurfaceVariant,
                                    labelColor = StonicTextSecondary
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
