package com.example.ui.screens

import android.app.TimePickerDialog
import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ReminderItem
import com.example.tools.CalculatorUtil
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
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Reminders & Tasks", "Calculator & Converter", "Weather Station", "Diagnostics HUD")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StonicDarkBg)
    ) {
        TopAppBar(
            title = {
                Column {
                    Text("TACTICAL TOOLS & TELEMETRY", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text("Stonic Operational Utilities", color = StonicCyan, fontSize = 11.sp)
                }
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = StonicCyan)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = StonicDarkSurface)
        )

        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = StonicDarkSurface,
            contentColor = StonicCyan,
            edgePadding = 12.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = StonicCyan
                )
            }
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == index) StonicCyanBright else StonicTextSecondary,
                            fontSize = 13.sp
                        )
                    }
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            when (selectedTab) {
                0 -> RemindersTab(viewModel)
                1 -> CalculatorTab()
                2 -> WeatherTab(viewModel)
                3 -> DiagnosticsTab(viewModel)
            }
        }
    }
}

@Composable
private fun RemindersTab(viewModel: MainViewModel) {
    val context = LocalContext.current
    val reminders by viewModel.reminders.collectAsState()
    var newTitle by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf("MEDIUM") }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Quick System Intent Triggers
        item {
            HudCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("SYSTEM ALARM & TIMER SHORTCUTS", color = StonicCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                val c = Calendar.getInstance()
                                TimePickerDialog(
                                    context,
                                    { _, hour, min ->
                                        viewModel.setSystemAlarm(hour, min, "Stonic Scheduled Protocol")
                                    },
                                    c.get(Calendar.HOUR_OF_DAY),
                                    c.get(Calendar.MINUTE),
                                    true
                                ).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StonicDarkSurfaceVariant),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Alarm, contentDescription = null, tint = StonicCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Set Alarm", color = StonicTextPrimary, fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                viewModel.setSystemTimer(300, "Stonic 5-Min Timer")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StonicDarkSurfaceVariant),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.HourglassBottom, contentDescription = null, tint = StonicCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("5-Min Timer", color = StonicTextPrimary, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Add Reminder Input
        item {
            HudCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("CREATE NEW DIRECTIVE / TASK", color = StonicCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = newTitle,
                            onValueChange = { newTitle = it },
                            placeholder = { Text("Task description...", color = StonicTextSecondary) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("reminder_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = StonicCyan,
                                unfocusedBorderColor = StonicCardBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (newTitle.isNotBlank()) {
                                    viewModel.addReminder(newTitle.trim(), "", System.currentTimeMillis() + 3600000L, selectedPriority)
                                    newTitle = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StonicCyan, contentColor = StonicDarkBg),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Add", fontWeight = FontWeight.Bold)
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("HIGH", "MEDIUM", "LOW").forEach { p ->
                            FilterChip(
                                selected = selectedPriority == p,
                                onClick = { selectedPriority = p },
                                label = { Text(p, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = if (p == "HIGH") StonicAlert else StonicCyan,
                                    selectedLabelColor = StonicDarkBg,
                                    containerColor = StonicDarkSurfaceVariant,
                                    labelColor = StonicTextSecondary
                                )
                            )
                        }
                    }
                }
            }
        }

        // List
        if (reminders.isEmpty()) {
            item {
                Text(
                    text = "No pending tasks or directives logged, Sir.",
                    color = StonicTextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            items(reminders, key = { it.id }) { reminder ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = StonicDarkSurfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = reminder.isCompleted,
                            onCheckedChange = { viewModel.toggleReminderCompleted(reminder) },
                            colors = CheckboxDefaults.colors(checkedColor = StonicSuccess)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = reminder.title,
                                color = if (reminder.isCompleted) StonicTextSecondary else StonicTextPrimary,
                                fontSize = 14.sp,
                                textDecoration = if (reminder.isCompleted) TextDecoration.LineThrough else null
                            )
                            Text(
                                text = "Priority: ${reminder.priority}",
                                color = if (reminder.priority == "HIGH") StonicAlert else StonicCyan,
                                fontSize = 11.sp
                            )
                        }
                        IconButton(onClick = { viewModel.deleteReminder(reminder) }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = StonicAlert.copy(alpha = 0.7f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalculatorTab() {
    var expr by remember { mutableStateOf("") }
    var calcResult by remember { mutableStateOf("0") }

    // Unit Converter state
    var convertVal by remember { mutableStateOf("100") }
    var fromUnit by remember { mutableStateOf("°C") }
    var toUnit by remember { mutableStateOf("°F") }
    var convertedResult by remember { mutableStateOf("") }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Calculator HUD Card
        item {
            HudCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("SCIENTIFIC COMPUTATION HUD", color = StonicCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                    // Display Screen
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(StonicDarkBg)
                            .border(1.dp, StonicCardBorder, RoundedCornerShape(10.dp))
                            .padding(14.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = expr.ifBlank { "0" },
                                color = StonicTextSecondary,
                                fontSize = 15.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "= $calcResult",
                                color = StonicCyanBright,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Keypad
                    val buttons = listOf(
                        listOf("C", "(", ")", "÷"),
                        listOf("sin", "cos", "sqrt", "×"),
                        listOf("7", "8", "9", "-"),
                        listOf("4", "5", "6", "+"),
                        listOf("1", "2", "3", "^"),
                        listOf("0", ".", "π", "=")
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (row in buttons) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                for (btn in row) {
                                    val isOp = btn in listOf("÷", "×", "-", "+", "=", "^")
                                    val isClear = btn == "C"

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(44.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (btn == "=") StonicCyan
                                                else if (isClear) StonicAlert.copy(alpha = 0.2f)
                                                else if (isOp) StonicDarkSurfaceVariant
                                                else StonicDarkSurface
                                            )
                                            .border(
                                                1.dp,
                                                if (btn == "=") StonicCyan else StonicCardBorder,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable {
                                                when (btn) {
                                                    "C" -> {
                                                        expr = ""
                                                        calcResult = "0"
                                                    }
                                                    "=" -> {
                                                        val res = CalculatorUtil.evaluate(expr)
                                                        calcResult = res.getOrElse { "Syntax Error" }
                                                    }
                                                    "sin" -> expr += "sin("
                                                    "cos" -> expr += "cos("
                                                    "sqrt" -> expr += "sqrt("
                                                    else -> expr += btn
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = btn,
                                            color = if (btn == "=") StonicDarkBg
                                            else if (isClear) StonicAlert
                                            else if (isOp) StonicCyanBright
                                            else StonicTextPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Unit Converter Card
        item {
            HudCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("DIMENSIONAL CONVERTER", color = StonicCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = convertVal,
                            onValueChange = {
                                convertVal = it
                                val num = it.toDoubleOrNull() ?: 0.0
                                val converted = CalculatorUtil.convertUnits(num, fromUnit, toUnit)
                                convertedResult = "%.2f".format(converted)
                            },
                            label = { Text("Magnitude") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = StonicCyan,
                                unfocusedBorderColor = StonicCardBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("°C to °F", "km to miles", "kg to lbs", "MB to GB").forEach { pair ->
                            val parts = pair.split(" to ")
                            FilterChip(
                                selected = fromUnit == parts[0] && toUnit == parts[1],
                                onClick = {
                                    fromUnit = parts[0]
                                    toUnit = parts[1]
                                    val num = convertVal.toDoubleOrNull() ?: 0.0
                                    val converted = CalculatorUtil.convertUnits(num, fromUnit, toUnit)
                                    convertedResult = "%.2f".format(converted)
                                },
                                label = { Text(pair, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = StonicCyan,
                                    selectedLabelColor = StonicDarkBg,
                                    containerColor = StonicDarkSurfaceVariant,
                                    labelColor = StonicTextSecondary
                                )
                            )
                        }
                    }

                    if (convertedResult.isNotBlank()) {
                        Text(
                            text = "Result: $convertedResult $toUnit",
                            color = StonicCyanBright,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WeatherTab(viewModel: MainViewModel) {
    val weather by viewModel.weatherInfo.collectAsState()
    val isLoading by viewModel.isWeatherLoading.collectAsState()
    val selectedCity by viewModel.selectedCity.collectAsState()
    val supportedCities = listOf("New York", "London", "Tokyo", "Paris", "San Francisco", "Dubai", "Sydney", "Singapore")

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text("SELECT GLOBAL OBSERVATION SECTOR", color = StonicCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                items(supportedCities) { city ->
                    FilterChip(
                        selected = selectedCity == city,
                        onClick = { viewModel.fetchWeather(city) },
                        label = { Text(city, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StonicCyan,
                            selectedLabelColor = StonicDarkBg,
                            containerColor = StonicDarkSurfaceVariant,
                            labelColor = StonicTextPrimary
                        )
                    )
                }
            }
        }

        if (isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = StonicCyan)
                }
            }
        } else if (weather != null) {
            val w = weather!!
            item {
                HudCard(modifier = Modifier.fillMaxWidth(), isGlow = true) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(w.cityName.uppercase(), color = StonicCyanBright, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                Text(w.weatherCondition, color = StonicTextSecondary, fontSize = 13.sp)
                            }
                            Text(text = w.weatherIconSymbol, fontSize = 42.sp)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = "%.1f°C".format(w.temperature),
                                color = Color.White,
                                fontSize = 38.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Feels like %.1f°C".format(w.apparentTemp),
                                color = StonicTextSecondary,
                                fontSize = 13.sp
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("HUMIDITY", color = StonicTextSecondary, fontSize = 10.sp)
                                Text("${w.humidity}%", color = StonicCyanBright, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("WIND VELOCITY", color = StonicTextSecondary, fontSize = 10.sp)
                                Text("${w.windSpeed} km/h", color = StonicCyanBright, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Stonic Tactical Voice Advisory
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(StonicDarkSurfaceVariant)
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "Stonic Advisory: ${w.stonicAdvisory}",
                                color = StonicCyan,
                                fontSize = 12.sp,
                                lineHeight = 17.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DiagnosticsTab(viewModel: MainViewModel) {
    val status by viewModel.systemStatus.collectAsState()

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            HudCard(modifier = Modifier.fillMaxWidth(), isGlow = true) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("SYSTEM TELEMETRY DIAGNOSTICS", color = StonicCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                    DiagnosticRow("CORE ASSISTANT STATUS", status.assistantStatus, StonicSuccess)
                    DiagnosticRow("HARDWARE TERMINAL", status.deviceModel, StonicTextPrimary)
                    DiagnosticRow("OPERATING SYSTEM", status.androidVersion, StonicCyanBright)
                    DiagnosticRow("POWER SUBSYSTEM", "${status.batteryPercent}% (${if (status.isCharging) "Charging" else "On Battery"})", StonicCyan)
                    DiagnosticRow("SYSTEM MEMORY", "${status.ramAvailableMb} MB Free / ${status.ramTotalMb} MB Total", StonicTextPrimary)
                    DiagnosticRow("NETWORK BANDWIDTH", status.networkType, StonicCyan)

                    Button(
                        onClick = { viewModel.refreshSystemStatus() },
                        colors = ButtonDefaults.buttonColors(containerColor = StonicCyan, contentColor = StonicDarkBg),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Refresh Telemetry Sensors", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun DiagnosticRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = StonicTextSecondary, fontSize = 11.sp, letterSpacing = 0.8.sp)
        Text(text = value, color = valueColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}
