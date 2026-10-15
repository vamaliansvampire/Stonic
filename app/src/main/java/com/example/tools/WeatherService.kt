package com.example.tools

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class WeatherInfo(
    val cityName: String,
    val temperature: Double,
    val apparentTemp: Double,
    val humidity: Int,
    val windSpeed: Double,
    val weatherCondition: String,
    val weatherIconSymbol: String,
    val stonicAdvisory: String
)

class WeatherService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val cityCoordinates = mapOf(
        "New York" to Pair(40.7128, -74.0060),
        "London" to Pair(51.5074, -0.1278),
        "Tokyo" to Pair(35.6762, 139.6503),
        "Paris" to Pair(48.8566, 2.3522),
        "San Francisco" to Pair(37.7749, -122.4194),
        "Dubai" to Pair(25.2048, 55.2708),
        "Sydney" to Pair(-33.8688, 151.2093),
        "Berlin" to Pair(52.5200, 13.4050),
        "Toronto" to Pair(43.6532, -79.3832),
        "Singapore" to Pair(1.3521, 103.8198),
        "Mumbai" to Pair(19.0760, 72.8777)
    )

    fun getSupportedCities(): List<String> = cityCoordinates.keys.toList()

    suspend fun fetchWeather(cityName: String): Result<WeatherInfo> = withContext(Dispatchers.IO) {
        val coords = cityCoordinates[cityName] ?: Pair(40.7128, -74.0060)
        val lat = coords.first
        val lon = coords.second

        val url = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current=temperature_2m,relative_humidity_2m,apparent_temperature,precipitation,weather_code,wind_speed_10m"

        try {
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Weather service error: ${response.code}"))
            }

            val body = response.body?.string() ?: ""
            val json = JSONObject(body)
            val current = json.getJSONObject("current")

            val temp = current.getDouble("temperature_2m")
            val apparent = current.getDouble("apparent_temperature")
            val humidity = current.getInt("relative_humidity_2m")
            val wind = current.getDouble("wind_speed_10m")
            val code = current.getInt("weather_code")

            val (condition, symbol, advisory) = interpretWeatherCode(code, temp)

            val info = WeatherInfo(
                cityName = cityName,
                temperature = temp,
                apparentTemp = apparent,
                humidity = humidity,
                windSpeed = wind,
                weatherCondition = condition,
                weatherIconSymbol = symbol,
                stonicAdvisory = advisory
            )

            Result.success(info)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun interpretWeatherCode(code: Int, temp: Double): Triple<String, String, String> {
        val condition = when (code) {
            0 -> "Clear Sky"
            1, 2, 3 -> "Mainly Clear / Partly Cloudy"
            45, 48 -> "Foggy / Atmospheric Haze"
            51, 53, 55 -> "Drizzle"
            61, 63, 65 -> "Rain Showers"
            71, 73, 75 -> "Snowfall"
            80, 81, 82 -> "Heavy Rain"
            95, 96, 99 -> "Thunderstorm Alert"
            else -> "Variable Clouds"
        }

        val symbol = when (code) {
            0 -> "☀️"
            1, 2, 3 -> "⛅"
            45, 48 -> "🌫️"
            51, 53, 55, 61, 63, 65, 80, 81, 82 -> "🌧️"
            71, 73, 75 -> "❄️"
            95, 96, 99 -> "⚡"
            else -> "🌤️"
        }

        val advisory = when {
            code >= 95 -> "Caution, Sir. Atmospheric electrical discharge detected. Recommend indoor operations."
            code in 61..82 -> "Precipitation inbound. Ensure weatherproofing gear before transit."
            temp < 0 -> "Sub-zero conditions detected. Thermal compensation recommended."
            temp > 32 -> "Elevated thermal readings. Hydration protocol recommended."
            else -> "Atmospheric parameters optimal for standard operations, Sir."
        }

        return Triple(condition, symbol, advisory)
    }
}
