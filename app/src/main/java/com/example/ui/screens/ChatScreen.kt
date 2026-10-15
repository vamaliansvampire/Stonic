package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.Message
import com.example.ui.components.ArcReactorAvatar
import com.example.ui.components.MarkdownText
import com.example.ui.theme.StonicAlert
import com.example.ui.theme.StonicCardBorder
import com.example.ui.theme.StonicCyan
import com.example.ui.theme.StonicCyanBright
import com.example.ui.theme.StonicDarkBg
import com.example.ui.theme.StonicDarkSurface
import com.example.ui.theme.StonicDarkSurfaceVariant
import com.example.ui.theme.StonicNeonPurple
import com.example.ui.theme.StonicTextPrimary
import com.example.ui.theme.StonicTextSecondary
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentMessages by viewModel.currentMessages.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val streamingContent by viewModel.currentStreamingContent.collectAsState()
    val currentTitle by viewModel.currentConversationTitle.collectAsState()
    val convId by viewModel.currentConversationId.collectAsState()
    val selectedModel by viewModel.settingsRepo.selectedModelFlow.collectAsState(initial = "gemini-2.5-flash")
    val attachedUri by viewModel.attachedImageUri.collectAsState()
    val isListening by viewModel.voiceManager.isListening.collectAsState()

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    var showRenameDialog by remember { mutableStateOf(false) }
    var renameText by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        viewModel.attachImage(uri)
    }

    // Scroll to bottom when messages change or streaming updates
    LaunchedEffect(currentMessages.size, streamingContent) {
        val totalCount = currentMessages.size + (if (isGenerating) 1 else 0)
        if (totalCount > 0) {
            listState.animateScrollToItem(totalCount - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StonicDarkBg)
    ) {
        // Futuristic Top Bar
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = currentTitle,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = "Stonic • $selectedModel",
                        color = StonicCyan,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp
                    )
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
                // Rename button
                if (convId != null) {
                    IconButton(
                        onClick = {
                            renameText = currentTitle
                            showRenameDialog = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Rename Chat",
                            tint = StonicTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = { showDeleteDialog = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Chat",
                            tint = StonicAlert,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // New chat button
                IconButton(
                    onClick = {
                        viewModel.startNewConversation()
                        inputText = ""
                    },
                    modifier = Modifier.testTag("chat_new_session_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Session",
                        tint = StonicCyan
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = StonicDarkSurface
            )
        )

        // Chat messages
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (currentMessages.isEmpty() && !isGenerating) {
                // Empty state with prompts
                EmptyChatPrompts(
                    onSelectPrompt = { prompt ->
                        inputText = prompt
                        viewModel.sendMessage(prompt)
                        inputText = ""
                    }
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(currentMessages, key = { it.id }) { msg ->
                        ChatMessageBubble(
                            message = msg,
                            onSpeak = { viewModel.voiceManager.speak(msg.content) },
                            onCopy = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Stonic Response", msg.content)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            onShare = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, msg.content)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share Stonic Intel"))
                            }
                        )
                    }

                    // Live streaming bubble
                    if (isGenerating) {
                        item(key = "streaming_bubble") {
                            StreamingAiBubble(
                                text = streamingContent,
                                onStop = { viewModel.stopGeneration() }
                            )
                        }
                    }
                }
            }
        }

        // Attached image preview if any
        if (attachedUri != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StonicDarkSurfaceVariant)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = attachedUri,
                    contentDescription = "Attached visual",
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Visual Payload Loaded",
                        color = StonicCyanBright,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Stonic vision ready to analyze image",
                        color = StonicTextSecondary,
                        fontSize = 11.sp
                    )
                }
                IconButton(onClick = { viewModel.attachImage(null) }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove attachment",
                        tint = StonicAlert
                    )
                }
            }
        }

        // Bottom Input Bar
        InputControlBar(
            inputText = inputText,
            onTextChanged = { inputText = it },
            isGenerating = isGenerating,
            isListening = isListening,
            onSend = {
                viewModel.sendMessage(inputText)
                inputText = ""
            },
            onStop = { viewModel.stopGeneration() },
            onMicClick = {
                viewModel.startVoiceCommand()
            },
            onAttachImage = {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }
        )
    }

    // Rename Dialog
    if (showRenameDialog && convId != null) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Session", color = Color.White) },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    label = { Text("Session Name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StonicCyan,
                        focusedLabelColor = StonicCyan
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.renameConversation(convId!!, renameText)
                        showRenameDialog = false
                    }
                ) {
                    Text("Save", color = StonicCyan)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel", color = StonicTextSecondary)
                }
            },
            containerColor = StonicDarkSurface
        )
    }

    // Delete Dialog
    if (showDeleteDialog && convId != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Session?", color = Color.White) },
            text = {
                Text(
                    "Are you sure you wish to delete this session and purge all telemetry data?",
                    color = StonicTextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteConversation(convId!!)
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete", color = StonicAlert)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = StonicTextSecondary)
                }
            },
            containerColor = StonicDarkSurface
        )
    }
}

@Composable
private fun ChatMessageBubble(
    message: Message,
    onSpeak: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit
) {
    val isUser = message.sender.equals("USER", ignoreCase = true)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            ArcReactorAvatar(
                size = 32.dp,
                isActive = false,
                modifier = Modifier.padding(top = 4.dp, end = 8.dp)
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(0.88f),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isUser) StonicDarkSurfaceVariant else StonicDarkSurface
                ),
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 16.dp
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = if (isUser) StonicCyan.copy(alpha = 0.4f) else StonicCardBorder
                ),
                modifier = Modifier.testTag("chat_bubble_${message.id}")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Display image if attached to message
                    if (message.imageUri != null) {
                        AsyncImage(
                            model = message.imageUri,
                            contentDescription = "Message visual payload",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .padding(bottom = 8.dp)
                        )
                    }

                    MarkdownText(
                        text = message.content,
                        textColor = if (isUser) Color.White else StonicTextPrimary
                    )
                }
            }

            // Action strip for AI message
            if (!isUser) {
                Row(
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(onClick = onSpeak, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Speak response",
                            tint = StonicTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(onClick = onCopy, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy text",
                            tint = StonicTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(onClick = onShare, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share text",
                            tint = StonicTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StreamingAiBubble(
    text: String,
    onStop: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cursor_blink")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursor"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        ArcReactorAvatar(
            size = 32.dp,
            isActive = true,
            modifier = Modifier.padding(top = 4.dp, end = 8.dp)
        )

        Column(modifier = Modifier.fillMaxWidth(0.88f)) {
            Card(
                colors = CardDefaults.cardColors(containerColor = StonicDarkSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, StonicCyan.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    if (text.isBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = StonicCyan,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Synthesizing intelligence...",
                                color = StonicCyanBright,
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        MarkdownText(text = text)
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .size(width = 8.dp, height = 14.dp)
                                .background(StonicCyan.copy(alpha = alpha))
                        )
                    }
                }
            }

            // Stop generation button
            TextButton(
                onClick = onStop,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = "Halt generation",
                    tint = StonicAlert,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Halt Generation", color = StonicAlert, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun EmptyChatPrompts(onSelectPrompt: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        ArcReactorAvatar(size = 90.dp, isActive = false)
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "STONIC INTELLIGENCE READY",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Text(
            text = "Ask a complex question, request code analysis, or select a command below, Sir.",
            color = StonicTextSecondary,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
        )

        val samplePrompts = listOf(
            "Explain quantum encryption protocols in simple terms",
            "Write a Kotlin Jetpack Compose animated button",
            "What are the best habits for peak mental productivity?",
            "Summarize the latest advancements in Fusion energy"
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            for (p in samplePrompts) {
                Card(
                    onClick = { onSelectPrompt(p) },
                    colors = CardDefaults.cardColors(containerColor = StonicDarkSurfaceVariant),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "▸  $p",
                        color = StonicTextPrimary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun InputControlBar(
    inputText: String,
    onTextChanged: (String) -> Unit,
    isGenerating: Boolean,
    isListening: Boolean,
    onSend: () -> Unit,
    onStop: () -> Unit,
    onMicClick: () -> Unit,
    onAttachImage: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = StonicDarkSurface),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, StonicCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Image attach button
            IconButton(
                onClick = onAttachImage,
                modifier = Modifier.testTag("attach_image_button")
            ) {
                Icon(
                    imageVector = Icons.Default.AttachFile,
                    contentDescription = "Attach visual",
                    tint = StonicTextSecondary
                )
            }

            // Text input
            OutlinedTextField(
                value = inputText,
                onValueChange = onTextChanged,
                placeholder = {
                    Text(
                        text = if (isListening) "Listening to audio..." else "Enter command for Stonic...",
                        color = StonicTextSecondary,
                        fontSize = 14.sp
                    )
                },
                maxLines = 4,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 6.dp)
                    .testTag("chat_input_field"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = StonicCyan,
                    unfocusedBorderColor = StonicCardBorder,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(20.dp)
            )

            // Voice Mic Button (Glowing)
            IconButton(
                onClick = onMicClick,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isListening) StonicCyanBright else StonicDarkSurfaceVariant)
                    .testTag("chat_mic_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Voice command",
                    tint = if (isListening) StonicDarkBg else StonicCyan
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Send / Stop button
            IconButton(
                onClick = if (isGenerating) onStop else onSend,
                enabled = isGenerating || inputText.isNotBlank(),
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isGenerating) StonicAlert else StonicCyan)
                    .testTag("chat_send_button")
            ) {
                Icon(
                    imageVector = if (isGenerating) Icons.Default.Stop else Icons.Default.Send,
                    contentDescription = if (isGenerating) "Stop" else "Send",
                    tint = StonicDarkBg
                )
            }
        }
    }
}
