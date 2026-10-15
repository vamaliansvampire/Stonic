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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Conversation
import com.example.ui.components.HudCard
import com.example.ui.theme.StonicAlert
import com.example.ui.theme.StonicCardBorder
import com.example.ui.theme.StonicCyan
import com.example.ui.theme.StonicCyanBright
import com.example.ui.theme.StonicDarkBg
import com.example.ui.theme.StonicDarkSurface
import com.example.ui.theme.StonicDarkSurfaceVariant
import com.example.ui.theme.StonicTextPrimary
import com.example.ui.theme.StonicTextSecondary
import com.example.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onSelectConversation: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val conversations by viewModel.filteredConversations.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var showClearAllDialog by remember { mutableStateOf(false) }
    var conversationToRename by remember { mutableStateOf<Conversation?>(null) }
    var renameInput by remember { mutableStateOf("") }
    var conversationToDelete by remember { mutableStateOf<Conversation?>(null) }

    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StonicDarkBg)
    ) {
        TopAppBar(
            title = {
                Column {
                    Text("INTELLIGENCE ARCHIVES", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("${conversations.size} Recorded Sessions", color = StonicCyan, fontSize = 11.sp)
                }
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = StonicCyan)
                }
            },
            actions = {
                if (conversations.isNotEmpty()) {
                    IconButton(
                        onClick = { showClearAllDialog = true }
                    ) {
                        Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = "Clear all archives", tint = StonicAlert)
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = StonicDarkSurface)
        )

        // Search Field
        Box(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Filter intelligence archives...", color = StonicTextSecondary) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = StonicCyan)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = StonicTextSecondary)
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("history_search_field"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = StonicCyan,
                    unfocusedBorderColor = StonicCardBorder,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }

        if (conversations.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (searchQuery.isBlank()) "No archived sessions in memory database."
                    else "No sessions found matching '$searchQuery'.",
                    color = StonicTextSecondary,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(conversations, key = { it.id }) { conv ->
                    Card(
                        onClick = { onSelectConversation(conv.id) },
                        colors = CardDefaults.cardColors(containerColor = StonicDarkSurfaceVariant),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("history_item_${conv.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Chat,
                                contentDescription = null,
                                tint = StonicCyan,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = conv.title,
                                    color = StonicTextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = dateFormat.format(Date(conv.updatedAt)) + " • " + conv.modelUsed,
                                    color = StonicTextSecondary,
                                    fontSize = 11.sp
                                )
                            }

                            // Actions
                            IconButton(
                                onClick = {
                                    conversationToRename = conv
                                    renameInput = conv.title
                                }
                            ) {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = "Rename", tint = StonicTextSecondary, modifier = Modifier.size(18.dp))
                            }

                            IconButton(
                                onClick = { conversationToDelete = conv }
                            ) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = StonicAlert, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Rename Dialog
    if (conversationToRename != null) {
        val target = conversationToRename!!
        AlertDialog(
            onDismissRequest = { conversationToRename = null },
            title = { Text("Rename Archive", color = Color.White) },
            text = {
                OutlinedTextField(
                    value = renameInput,
                    onValueChange = { renameInput = it },
                    label = { Text("Session Title") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StonicCyan,
                        focusedLabelColor = StonicCyan
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.renameConversation(target.id, renameInput)
                        conversationToRename = null
                    }
                ) {
                    Text("Save", color = StonicCyan)
                }
            },
            dismissButton = {
                TextButton(onClick = { conversationToRename = null }) {
                    Text("Cancel", color = StonicTextSecondary)
                }
            },
            containerColor = StonicDarkSurface
        )
    }

    // Delete Single Dialog
    if (conversationToDelete != null) {
        val target = conversationToDelete!!
        AlertDialog(
            onDismissRequest = { conversationToDelete = null },
            title = { Text("Delete Archive?", color = Color.White) },
            text = {
                Text("Purge '${target.title}' from local storage?", color = StonicTextSecondary)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteConversation(target.id)
                        conversationToDelete = null
                    }
                ) {
                    Text("Purge", color = StonicAlert)
                }
            },
            dismissButton = {
                TextButton(onClick = { conversationToDelete = null }) {
                    Text("Cancel", color = StonicTextSecondary)
                }
            },
            containerColor = StonicDarkSurface
        )
    }

    // Clear All Dialog
    if (showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllDialog = false },
            title = { Text("Purge All Archives?", color = Color.White) },
            text = {
                Text("This action will erase all past conversation records permanently.", color = StonicTextSecondary)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllConversations()
                        showClearAllDialog = false
                    }
                ) {
                    Text("Purge All", color = StonicAlert)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllDialog = false }) {
                    Text("Cancel", color = StonicTextSecondary)
                }
            },
            containerColor = StonicDarkSurface
        )
    }
}
