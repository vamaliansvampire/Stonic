package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import java.util.Calendar

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToChat: () -> Unit,
    onNavigateToVoice: () -> Unit,
    onNavigateToImages: () -> Unit,
    onNavigateToTools: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val conversations by viewModel.conversations.collectAsState()
    val isListening by viewModel.voiceManager.isListening.collectAsState()
    val isSpeaking by viewModel.voiceManager.isSpeaking.collectAsState()
    val audioLevel by viewModel.voiceManager.audioWaveLevel.collectAsState()
    val systemStatus by viewModel.systemStatus.collectAsState()
    val apiKey by viewModel.settingsRepo.apiKeyFlow.collectAsState(initial = "")
    val selectedModel by viewModel.settingsRepo.selectedModelFlow.collectAsState(initial = "gemini-2.5-flash")

    val greeting = getJarvisGreeting()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(StonicDarkBg)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Top HUD Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (apiKey.isNotBlank()) StonicSuccess else StonicAlert)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "STONIC // OS 4.8",
                            color = StonicCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                    }
                    Text(
                        text = "JARVIS PROTOCOL ACTIVE",
                        color = StonicTextSecondary,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )
                }

                IconButton(
                    onClick = onNavigateToSettings,
                    modifier = Modifier.testTag("home_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = StonicCyan
                    )
                }
            }
        }

        // Hero Arc Reactor & Greeting
        item {
            HudCard(
                modifier = Modifier.fillMaxWidth(),
                isGlow = isListening || isSpeaking
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ArcReactorAvatar(
                        size = 130.dp,
                        isActive = isListening || isSpeaking,
                        audioLevel = audioLevel,
                        onClick = onNavigateToVoice
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = greeting,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = if (isListening) "Stonic is analyzing your voice..."
                        else if (isSpeaking) "Stonic voice output engaged..."
                        else "All systems operational. How may I assist you today, Sir?",
                        color = StonicTextSecondary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )

                    if (isListening || isSpeaking) {
                        VoiceWaveform(
                            modifier = Modifier.padding(vertical = 4.dp),
                            height = 36.dp,
                            isActive = true,
                            audioLevel = audioLevel
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onNavigateToVoice,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = StonicCyan,
                                contentColor = StonicDarkBg
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("hero_voice_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Voice Mode", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        Button(
                            onClick = {
                                viewModel.startNewConversation("Stonic Session")
                                onNavigateToChat()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = StonicDarkSurfaceVariant,
                                contentColor = StonicCyanBright
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("hero_chat_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Chat,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("New Chat", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        // Quick Capabilities Grid
        item {
            Text(
                text = "SYSTEM CAPABILITIES",
                color = StonicCyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    title = "AI Chat",
                    subtitle = "Gemini $selectedModel",
                    icon = Icons.Default.Chat,
                    accentColor = StonicCyan,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToChat
                )
                QuickActionCard(
                    title = "Image Lab",
                    subtitle = "Generate & Vision",
                    icon = Icons.Default.Image,
                    accentColor = StonicNeonPurple,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToImages
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    title = "Tools & Tasks",
                    subtitle = "Reminders & Math",
                    icon = Icons.Default.Build,
                    accentColor = StonicCyanBright,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToTools
                )
                QuickActionCard(
                    title = "Voice Sphere",
                    subtitle = "Hands-free Mode",
                    icon = Icons.Default.Mic,
                    accentColor = StonicSuccess,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToVoice
                )
            }
        }

        // Recent Conversations
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECENT SESSIONS",
                    color = StonicCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
                if (conversations.isNotEmpty()) {
                    Text(
                        text = "View All (${conversations.size})",
                        color = StonicTextSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier.clickable { onNavigateToChat() }
                    )
                }
            }
        }

        if (conversations.isEmpty()) {
            item {
                HudCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "No previous sessions found, Sir. Initiate a new session above to begin.",
                        color = StonicTextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(conversations.take(5)) { conv ->
                        Card(
                            onClick = {
                                viewModel.selectConversation(conv.id)
                                onNavigateToChat()
                            },
                            colors = CardDefaults.cardColors(
                                containerColor = StonicDarkSurfaceVariant
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .width(200.dp)
                                .testTag("recent_chat_${conv.id}")
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = conv.title,
                                    color = StonicTextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Model: ${conv.modelUsed}",
                                    color = StonicTextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Telemetry & Diagnostics Strip
        item {
            Text(
                text = "TELEMETRY STATUS",
                color = StonicCyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
        }

        item {
            HudCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    TelemetryItem(
                        label = "POWER",
                        value = "${systemStatus.batteryPercent}%",
                        sub = if (systemStatus.isCharging) "Charging" else "Discharging"
                    )
                    TelemetryItem(
                        label = "RAM",
                        value = "${systemStatus.ramAvailableMb} MB",
                        sub = "Available"
                    )
                    TelemetryItem(
                        label = "NETWORK",
                        value = systemStatus.networkType.take(8),
                        sub = "Status"
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    HudCard(
        modifier = modifier.clickable { onClick() },
        borderColor = accentColor.copy(alpha = 0.4f)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                color = StonicTextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                color = StonicTextSecondary,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun TelemetryItem(label: String, value: String, sub: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = StonicTextSecondary, fontSize = 10.sp, letterSpacing = 1.sp)
        Text(text = value, color = StonicCyanBright, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Text(text = sub, color = StonicTextSecondary, fontSize = 10.sp)
    }
}

private fun getJarvisGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 5..11 -> "Good morning, Sir."
        in 12..16 -> "Good afternoon, Sir."
        in 17..22 -> "Good evening, Sir."
        else -> "Good night, Sir. Systems active."
    }
}
