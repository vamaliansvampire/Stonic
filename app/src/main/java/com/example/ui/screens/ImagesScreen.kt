package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.components.HudCard
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
fun ImagesScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Synthesizer, 1: Vision

    // Image Generator state
    var promptText by remember { mutableStateOf("Futuristic holographic Iron Man suit lab with cyan neon lights") }
    var selectedStyle by remember { mutableStateOf("Cyberpunk Sci-Fi") }
    val generatedUrl by viewModel.generatedImageUrl.collectAsState()
    val isGeneratingImage by viewModel.isGeneratingImage.collectAsState()

    // Vision state
    val attachedUri by viewModel.attachedImageUri.collectAsState()
    var visionPrompt by remember { mutableStateOf("Analyze this visual in high detail and extract all key data, Sir.") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        viewModel.attachImage(uri)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StonicDarkBg)
    ) {
        TopAppBar(
            title = {
                Column {
                    Text("STONIC IMAGE LAB", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("Neural Synthesis & Multimodal Vision", color = StonicCyan, fontSize = 11.sp)
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
            colors = TopAppBarDefaults.topAppBarColors(containerColor = StonicDarkSurface)
        )

        // Tab Selector
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = StonicDarkSurface,
            contentColor = StonicCyan,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = StonicCyan
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Text(
                        "AI Synthesizer",
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTab == 0) StonicCyanBright else StonicTextSecondary
                    )
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Text(
                        "Multimodal Vision",
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTab == 1) StonicCyanBright else StonicTextSecondary
                    )
                }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (selectedTab == 0) {
                // Synthesizer Section
                HudCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "IMAGE PROMPT SPECIFICATION",
                            color = StonicCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        OutlinedTextField(
                            value = promptText,
                            onValueChange = { promptText = it },
                            placeholder = { Text("Describe the visual entity you wish to generate...", color = StonicTextSecondary) },
                            maxLines = 4,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("image_gen_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = StonicCyan,
                                unfocusedBorderColor = StonicCardBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Text(
                            text = "AESTHETIC PRESET",
                            color = StonicTextSecondary,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        )

                        val styles = listOf("Cyberpunk Sci-Fi", "Photorealistic 8K", "JARVIS Blueprint HUD", "Anime Mech", "Digital Concept Art")
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(styles) { style ->
                                FilterChip(
                                    selected = selectedStyle == style,
                                    onClick = { selectedStyle = style },
                                    label = { Text(style, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = StonicCyan,
                                        selectedLabelColor = StonicDarkBg,
                                        containerColor = StonicDarkSurfaceVariant,
                                        labelColor = StonicTextPrimary
                                    )
                                )
                            }
                        }

                        Button(
                            onClick = { viewModel.generateImage(promptText, selectedStyle) },
                            enabled = !isGeneratingImage && promptText.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = StonicCyan,
                                contentColor = StonicDarkBg
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("generate_image_button")
                        ) {
                            if (isGeneratingImage) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = StonicDarkBg,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Synthesizing Image...", fontWeight = FontWeight.Bold)
                            } else {
                                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Generate Visual", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Image Output Card
                if (generatedUrl != null) {
                    HudCard(modifier = Modifier.fillMaxWidth(), isGlow = true) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "SYNTHESIZED ARTIFACT",
                                    color = StonicCyanBright,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                IconButton(
                                    onClick = {
                                        val shareIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_TEXT, "Generated by Stonic AI Assistant:\n$generatedUrl")
                                            type = "text/plain"
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Share Image Link"))
                                    }
                                ) {
                                    Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = StonicCyan)
                                }
                            }

                            AsyncImage(
                                model = generatedUrl,
                                contentDescription = "Synthesized AI visual",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, StonicCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            )
                        }
                    }
                }
            } else {
                // Multimodal Vision Section
                HudCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(
                            text = "IMAGE UNDERSTANDING & OCR",
                            color = StonicCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Text(
                            text = "Upload any diagram, photograph, mathematical equation, or code screenshot. Stonic will inspect and analyze it.",
                            color = StonicTextSecondary,
                            fontSize = 13.sp
                        )

                        if (attachedUri != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, StonicCyan, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = attachedUri,
                                    contentDescription = "Selected payload",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        Button(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = StonicDarkSurfaceVariant,
                                contentColor = StonicCyanBright
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (attachedUri != null) "Choose Different Image" else "Select Image From Device")
                        }

                        OutlinedTextField(
                            value = visionPrompt,
                            onValueChange = { visionPrompt = it },
                            label = { Text("Instructions for Stonic") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = StonicCyan,
                                unfocusedBorderColor = StonicCardBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                if (attachedUri == null) {
                                    Toast.makeText(context, "Please select an image first, Sir.", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.sendMessage(visionPrompt)
                                    onNavigateToChat()
                                }
                            },
                            enabled = attachedUri != null,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = StonicCyan,
                                contentColor = StonicDarkBg
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Inspect with Stonic Vision", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
