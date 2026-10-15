package com.example.tools

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build

data class SystemStatus(
    val batteryPercent: Int,
    val isCharging: Boolean,
    val ramAvailableMb: Long,
    val ramTotalMb: Long,
    val networkType: String,
    val deviceModel: String,
    val androidVersion: String,
    val assistantStatus: String
)

object SystemDiagnosticsHelper {

    fun getSystemStatus(context: Context): SystemStatus {
        // Battery
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct = if (level != -1 && scale != -1) (level * 100 / scale.toFloat()).toInt() else 100
        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

        // Memory
        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager?.getMemoryInfo(memInfo)
        val availMb = memInfo.availMem / (1024 * 1024)
        val totalMb = memInfo.totalMem / (1024 * 1024)

        // Network
        val connManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val activeNet = connManager?.activeNetwork
        val caps = connManager?.getNetworkCapabilities(activeNet)
        val netType = when {
            caps == null -> "Offline"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi (High Bandwidth)"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular (5G/LTE)"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
            else -> "Connected"
        }

        val model = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}"
        val version = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"

        return SystemStatus(
            batteryPercent = batteryPct,
            isCharging = isCharging,
            ramAvailableMb = availMb,
            ramTotalMb = totalMb,
            networkType = netType,
            deviceModel = model,
            androidVersion = version,
            assistantStatus = "Optimal • Neural Core Active"
        )
    }
}
