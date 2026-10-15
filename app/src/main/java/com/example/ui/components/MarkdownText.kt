package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.StonicCardBorder
import com.example.ui.theme.StonicCyan
import com.example.ui.theme.StonicCyanBright
import com.example.ui.theme.StonicDarkBg
import com.example.ui.theme.StonicDarkSurfaceVariant
import com.example.ui.theme.StonicNeonPurple
import com.example.ui.theme.StonicTextPrimary
import com.example.ui.theme.StonicTextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    textColor: Color = StonicTextPrimary
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Parse blocks: code fences vs regular text
    val blocks = remember(text) { parseMarkdownBlocks(text) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (block in blocks) {
            when (block) {
                is MarkdownBlock.CodeBlock -> {
                    CodeBlockCard(
                        language = block.language,
                        code = block.code,
                        onCopy = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Stonic Code Snippet", block.code)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Code copied to clipboard", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
                is MarkdownBlock.TextBlock -> {
                    val annotated = remember(block.content, textColor) {
                        renderFormattedText(block.content, textColor)
                    }
                    Text(
                        text = annotated,
                        lineHeight = 22.sp,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun CodeBlockCard(
    language: String,
    code: String,
    onCopy: () -> Unit
) {
    var isCopied by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(StonicDarkBg)
            .border(1.dp, StonicCardBorder, RoundedCornerShape(12.dp))
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(StonicDarkSurfaceVariant.copy(alpha = 0.9f))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(StonicCyan)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = language.ifBlank { "CODE" }.uppercase(),
                    color = StonicCyanBright,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }

            IconButton(
                onClick = {
                    onCopy()
                    isCopied = true
                    coroutineScope.launch {
                        delay(2000)
                        isCopied = false
                    }
                },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = if (isCopied) Icons.Default.Done else Icons.Default.ContentCopy,
                    contentDescription = "Copy code",
                    tint = if (isCopied) StonicCyan else StonicTextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Code content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(12.dp)
        ) {
            Text(
                text = code,
                color = StonicTextPrimary,
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                lineHeight = 19.sp
            )
        }
    }
}

sealed class MarkdownBlock {
    data class TextBlock(val content: String) : MarkdownBlock()
    data class CodeBlock(val language: String, val code: String) : MarkdownBlock()
}

private fun parseMarkdownBlocks(text: String): List<MarkdownBlock> {
    val blocks = mutableListOf<MarkdownBlock>()
    val lines = text.lines()
    var inCodeBlock = false
    var currentLang = ""
    val codeBuilder = StringBuilder()
    val textBuilder = StringBuilder()

    for (line in lines) {
        if (line.trimStart().startsWith("```")) {
            if (inCodeBlock) {
                // End of code block
                blocks.add(MarkdownBlock.CodeBlock(currentLang, codeBuilder.toString().trimEnd()))
                codeBuilder.clear()
                currentLang = ""
                inCodeBlock = false
            } else {
                // Start of code block
                if (textBuilder.isNotBlank()) {
                    blocks.add(MarkdownBlock.TextBlock(textBuilder.toString().trimEnd()))
                    textBuilder.clear()
                }
                currentLang = line.trim().removePrefix("```").trim()
                inCodeBlock = true
            }
        } else {
            if (inCodeBlock) {
                codeBuilder.append(line).append("\n")
            } else {
                textBuilder.append(line).append("\n")
            }
        }
    }

    if (inCodeBlock && codeBuilder.isNotEmpty()) {
        blocks.add(MarkdownBlock.CodeBlock(currentLang, codeBuilder.toString().trimEnd()))
    } else if (textBuilder.isNotBlank()) {
        blocks.add(MarkdownBlock.TextBlock(textBuilder.toString().trimEnd()))
    }

    return if (blocks.isEmpty()) listOf(MarkdownBlock.TextBlock(text)) else blocks
}

private fun renderFormattedText(content: String, defaultColor: Color) = buildAnnotatedString {
    val lines = content.lines()
    for ((index, line) in lines.withIndex()) {
        val trimmed = line.trim()
        when {
            trimmed.startsWith("### ") -> {
                withStyle(
                    SpanStyle(
                        color = StonicCyanBright,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                ) {
                    append(trimmed.removePrefix("### "))
                }
            }
            trimmed.startsWith("## ") -> {
                withStyle(
                    SpanStyle(
                        color = StonicCyan,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                ) {
                    append(trimmed.removePrefix("## "))
                }
            }
            trimmed.startsWith("# ") -> {
                withStyle(
                    SpanStyle(
                        color = StonicCyan,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                ) {
                    append(trimmed.removePrefix("# "))
                }
            }
            trimmed.startsWith("* ") || trimmed.startsWith("- ") -> {
                withStyle(SpanStyle(color = StonicCyan, fontWeight = FontWeight.Bold)) {
                    append("  ▸ ")
                }
                parseInlineStyles(trimmed.substring(2), defaultColor)
            }
            trimmed.startsWith("1. ") || trimmed.startsWith("2. ") || trimmed.startsWith("3. ") || trimmed.startsWith("4. ") || trimmed.startsWith("5. ") -> {
                val prefix = trimmed.substringBefore(". ") + ". "
                withStyle(SpanStyle(color = StonicNeonPurple, fontWeight = FontWeight.Bold)) {
                    append(prefix)
                }
                parseInlineStyles(trimmed.substringAfter(". "), defaultColor)
            }
            else -> {
                parseInlineStyles(line, defaultColor)
            }
        }

        if (index < lines.size - 1) {
            append("\n")
        }
    }
}

private fun androidx.compose.ui.text.AnnotatedString.Builder.parseInlineStyles(
    text: String,
    defaultColor: Color
) {
    var i = 0
    while (i < text.length) {
        if (text.startsWith("**", i)) {
            val end = text.indexOf("**", i + 2)
            if (end != -1) {
                withStyle(SpanStyle(color = Color.White, fontWeight = FontWeight.Bold)) {
                    append(text.substring(i + 2, end))
                }
                i = end + 2
                continue
            }
        }

        if (text.startsWith("`", i)) {
            val end = text.indexOf("`", i + 1)
            if (end != -1) {
                withStyle(
                    SpanStyle(
                        color = StonicCyanBright,
                        background = StonicDarkSurfaceVariant,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp
                    )
                ) {
                    append(text.substring(i + 1, end))
                }
                i = end + 1
                continue
            }
        }

        withStyle(SpanStyle(color = defaultColor)) {
            append(text[i].toString())
        }
        i++
    }
}
