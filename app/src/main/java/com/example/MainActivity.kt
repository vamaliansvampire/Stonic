package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ImagesScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.ToolsScreen
import com.example.ui.screens.VoiceScreen
import com.example.ui.theme.StonicCardBorder
import com.example.ui.theme.StonicCyan
import com.example.ui.theme.StonicCyanBright
import com.example.ui.theme.StonicDarkBg
import com.example.ui.theme.StonicDarkSurface
import com.example.ui.theme.StonicDarkSurfaceVariant
import com.example.ui.theme.StonicNeonPurple
import com.example.ui.theme.StonicTextPrimary
import com.example.ui.theme.StonicTextSecondary
import com.example.ui.theme.StonicTheme
import com.example.viewmodel.MainViewModel

enum class StonicScreen(val title: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    CHAT("Chat", Icons.Default.Chat),
    VOICE("Voice", Icons.Default.Mic),
    IMAGES("Images", Icons.Default.Image),
    TOOLS("Tools", Icons.Default.Build),
    HISTORY("History", Icons.Default.History),
    SETTINGS("Settings", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StonicTheme {
                val viewModel: MainViewModel = viewModel()
                StonicAppRoot(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun StonicAppRoot(viewModel: MainViewModel) {
    var currentScreen by remember { mutableStateOf(StonicScreen.HOME) }

    // Handle back button to return to HOME when on sub-screens
    if (currentScreen != StonicScreen.HOME) {
        BackHandler {
            currentScreen = StonicScreen.HOME
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = StonicDarkBg,
        bottomBar = {
            // Display Bottom Bar on main navigation destinations
            if (currentScreen != StonicScreen.VOICE) {
                StonicBottomNavigation(
                    currentScreen = currentScreen,
                    onNavigate = { currentScreen = it }
                )
            }
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "screen_transition",
            modifier = Modifier.padding(innerPadding)
        ) { screen ->
            when (screen) {
                StonicScreen.HOME -> {
                    HomeScreen(
                        viewModel = viewModel,
                        onNavigateToChat = { currentScreen = StonicScreen.CHAT },
                        onNavigateToVoice = { currentScreen = StonicScreen.VOICE },
                        onNavigateToImages = { currentScreen = StonicScreen.IMAGES },
                        onNavigateToTools = { currentScreen = StonicScreen.TOOLS },
                        onNavigateToSettings = { currentScreen = StonicScreen.SETTINGS }
                    )
                }
                StonicScreen.CHAT -> {
                    ChatScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = StonicScreen.HOME }
                    )
                }
                StonicScreen.VOICE -> {
                    VoiceScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = StonicScreen.HOME }
                    )
                }
                StonicScreen.IMAGES -> {
                    ImagesScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = StonicScreen.HOME },
                        onNavigateToChat = { currentScreen = StonicScreen.CHAT }
                    )
                }
                StonicScreen.TOOLS -> {
                    ToolsScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = StonicScreen.HOME }
                    )
                }
                StonicScreen.HISTORY -> {
                    HistoryScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = StonicScreen.HOME },
                        onSelectConversation = { convId ->
                            viewModel.selectConversation(convId)
                            currentScreen = StonicScreen.CHAT
                        }
                    )
                }
                StonicScreen.SETTINGS -> {
                    SettingsScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = StonicScreen.HOME }
                    )
                }
            }
        }
    }
}

@Composable
fun StonicBottomNavigation(
    currentScreen: StonicScreen,
    onNavigate: (StonicScreen) -> Unit
) {
    val items = listOf(
        StonicScreen.HOME,
        StonicScreen.CHAT,
        StonicScreen.VOICE,
        StonicScreen.IMAGES,
        StonicScreen.TOOLS,
        StonicScreen.HISTORY
    )

    NavigationBar(
        containerColor = StonicDarkSurface,
        tonalElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = StonicCardBorder,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            )
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .testTag("stonic_bottom_nav")
    ) {
        items.forEach { screen ->
            val isSelected = currentScreen == screen
            val isVoice = screen == StonicScreen.VOICE

            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(screen) },
                icon = {
                    if (isVoice) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) StonicCyan else StonicDarkSurfaceVariant),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title,
                                tint = if (isSelected) StonicDarkBg else StonicCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = screen.icon,
                            contentDescription = screen.title,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                },
                label = {
                    Text(
                        text = screen.title,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = StonicCyan,
                    selectedTextColor = StonicCyanBright,
                    unselectedIconColor = StonicTextSecondary,
                    unselectedTextColor = StonicTextSecondary,
                    indicatorColor = StonicDarkSurfaceVariant
                ),
                modifier = Modifier.testTag("nav_${screen.name.lowercase()}")
            )
        }
    }
}
