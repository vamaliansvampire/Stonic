package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.components.ArcReactorAvatar
import com.example.ui.components.HudCard
import com.example.ui.components.VoiceWaveform
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isListening by viewModel.voiceManager.isListening.collectAsState()
    val isSpeaking by viewModel.voiceManager.isSpeaking.collectAsState()
    val audioLevel by viewModel.voiceManager.audioWaveLevel.collectAsState()
    val lastSpokenText by viewModel.voiceManager.lastSpokenText.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val streamingContent by viewModel.currentStreamingContent.collectAsState()
    val voicePreset by viewModel.settingsRepo.voicePresetFlow.collectAsState(initial = "Male Stonic (Deep & Confident)")
    val handsFree by viewModel.settingsRepo.handsFreeFlow.collectAsState(initial = false)

    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasAudioPermission = granted
        if (granted) {
            viewModel.startVoiceCommand()
        } else {
            Toast.makeText(context, "Microphone permission is needed for voice assistant", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StonicDarkBg)
    ) {
        TopAppBar(
            title = {
                Column {
                    Text("STONIC NEURAL VOICE SPHERE", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text("Pocheed Deep Male Engine Active", color = StonicCyan, fontSize = 11.sp)
                }
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = StonicCyan
                    )
                }
            },
            actions = {
                // Preset badge
                Box(
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(StonicDarkSurfaceVariant)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isListening) "LISTENING" else if (isSpeaking) "SPEAKING" else "STANDBY",
                        color = if (isListening) StonicCyanBright else if (isSpeaking) StonicNeonPurple else StonicTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = StonicDarkSurface)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Central Massive Holographic Arc Reactor
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                ArcReactorAvatar(
                    size = 220.dp,
                    isActive = isListening || isSpeaking || isGenerating,
                    audioLevel = audioLevel,
                    onClick = {
                        if (!hasAudioPermission) {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        } else {
                            if (isListening) {
                                viewModel.voiceManager.stopListening()
                            } else {
                                viewModel.startVoiceCommand()
                            }
                        }
                    }
                )
            }

            // Real-time Audio Waveform
            VoiceWaveform(
                height = 54.dp,
                isActive = isListening || isSpeaking,
                audioLevel = audioLevel,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            // Transcript / Response readout card
            HudCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                isGlow = isListening || isSpeaking
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = when {
                            isListening -> "Listening to your instructions, Sir..."
                            isGenerating -> "Synthesizing answer with Gemini..."
                            isSpeaking -> "Voice Transmission in Progress"
                            else -> "Tap the microphone to speak with Stonic"
                        },
                        color = StonicCyan,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val displayText = when {
                        streamingContent.isNotBlank() -> streamingContent
                        lastSpokenText.isNotBlank() -> lastSpokenText
                        else -> "Stonic is initialized and tuned to a deep, confident vocal resonance. Speak naturally or give operational instructions."
                    }

                    Text(
                        text = displayText,
                        color = StonicTextPrimary,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 6
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Voice Control Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Replay button
                IconButton(
                    onClick = { viewModel.voiceManager.replay() },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(StonicDarkSurfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.Replay,
                        contentDescription = "Replay speech",
                        tint = StonicCyanBright
                    )
                }

                // Primary Large Glowing Mic Button
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    if (isListening) StonicCyanBright else StonicCyan,
                                    StonicNeonPurple
                                )
                            )
                        )
                        .testTag("voice_main_mic_button")
                ) {
                    IconButton(
                        onClick = {
                            if (!hasAudioPermission) {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            } else {
                                if (isListening) {
                                    viewModel.voiceManager.stopListening()
                                } else {
                                    viewModel.startVoiceCommand()
                                }
                            }
                        },
                        modifier = Modifier.size(76.dp)
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Microphone",
                            tint = StonicDarkBg,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                // Stop / Interrupt speech button
                IconButton(
                    onClick = {
                        viewModel.voiceManager.stopSpeaking()
                        viewModel.stopGeneration()
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(StonicDarkSurfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Interrupt speech",
                        tint = StonicAlert
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Hands-Free Mode Toggle & Info
            Card(
                colors = CardDefaults.cardColors(containerColor = StonicDarkSurfaceVariant),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Hands-Free Auto Response",
                            color = StonicTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Automatically read out full intelligence responses",
                            color = StonicTextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Switch(
                        checked = handsFree,
                        onCheckedChange = { viewModel.setHandsFree(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = StonicDarkBg,
                            checkedTrackColor = StonicCyan,
                            uncheckedThumbColor = StonicTextSecondary,
                            uncheckedTrackColor = StonicDarkSurface
                        )
                    )
                }
            }
        }
    }
}
