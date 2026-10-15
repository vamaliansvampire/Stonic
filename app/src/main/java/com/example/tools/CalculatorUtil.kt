package com.example.tools

import java.text.DecimalFormat
import kotlin.math.*

object CalculatorUtil {

    private val df = DecimalFormat("#.######")

    fun evaluate(expression: String): Result<String> {
        return try {
            val clean = expression.replace(" ", "")
                .replace("×", "*")
                .replace("÷", "/")
                .replace("π", Math.PI.toString())
                .replace("e", Math.E.toString())

            val parser = ExpressionParser(clean)
            val result = parser.parse()
            if (result.isNaN() || result.isInfinite()) {
                Result.failure(ArithmeticException("Undefined or infinite result"))
            } else {
                Result.success(df.format(result))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private class ExpressionParser(private val str: String) {
        private var pos = -1
        private var ch = ' '

        private fun nextChar() {
            pos++
            ch = if (pos < str.length) str[pos] else '\u0000'
        }

        private fun eat(charToEat: Char): Boolean {
            while (ch == ' ') nextChar()
            if (ch == charToEat) {
                nextChar()
                return true
            }
            return false
        }

        fun parse(): Double {
            nextChar()
            val x = parseExpression()
            if (pos < str.length) throw RuntimeException("Unexpected character: $ch")
            return x
        }

        private fun parseExpression(): Double {
            var x = parseTerm()
            while (true) {
                when {
                    eat('+') -> x += parseTerm()
                    eat('-') -> x -= parseTerm()
                    else -> return x
                }
            }
        }

        private fun parseTerm(): Double {
            var x = parseFactor()
            while (true) {
                when {
                    eat('*') -> x *= parseFactor()
                    eat('/') -> x /= parseFactor()
                    eat('%') -> x %= parseFactor()
                    else -> return x
                }
            }
        }

        private fun parseFactor(): Double {
            if (eat('+')) return +parseFactor()
            if (eat('-')) return -parseFactor()

            var x: Double
            val startPos = pos
            if (eat('(')) {
                x = parseExpression()
                eat(')')
            } else if ((ch in '0'..'9') || ch == '.') {
                while ((ch in '0'..'9') || ch == '.') nextChar()
                x = str.substring(startPos, pos).toDouble()
            } else if (ch in 'a'..'z') {
                while (ch in 'a'..'z') nextChar()
                val func = str.substring(startPos, pos)
                x = parseFactor()
                x = when (func) {
                    "sqrt" -> sqrt(x)
                    "sin" -> sin(Math.toRadians(x))
                    "cos" -> cos(Math.toRadians(x))
                    "tan" -> tan(Math.toRadians(x))
                    "log" -> log10(x)
                    "ln" -> ln(x)
                    "abs" -> abs(x)
                    else -> throw RuntimeException("Unknown function: $func")
                }
            } else {
                throw RuntimeException("Unexpected token: $ch")
            }

            if (eat('^')) x = x.pow(parseFactor())

            return x
        }
    }

    // Unit Converter
    fun convertUnits(value: Double, fromUnit: String, toUnit: String): Double {
        return when {
            // Temperature
            fromUnit == "°C" && toUnit == "°F" -> (value * 9 / 5) + 32
            fromUnit == "°F" && toUnit == "°C" -> (value - 32) * 5 / 9
            fromUnit == "°C" && toUnit == "K" -> value + 273.15
            fromUnit == "K" && toUnit == "°C" -> value - 273.15
            fromUnit == "°F" && toUnit == "K" -> (value - 32) * 5 / 9 + 273.15
            fromUnit == "K" && toUnit == "°F" -> (value - 273.15) * 9 / 5 + 32

            // Length
            fromUnit == "km" && toUnit == "miles" -> value * 0.621371
            fromUnit == "miles" && toUnit == "km" -> value / 0.621371
            fromUnit == "meters" && toUnit == "feet" -> value * 3.28084
            fromUnit == "feet" && toUnit == "meters" -> value / 3.28084
            fromUnit == "cm" && toUnit == "inches" -> value * 0.393701
            fromUnit == "inches" && toUnit == "cm" -> value / 0.393701

            // Weight
            fromUnit == "kg" && toUnit == "lbs" -> value * 2.20462
            fromUnit == "lbs" && toUnit == "kg" -> value / 2.20462
            fromUnit == "grams" && toUnit == "ounces" -> value * 0.035274
            fromUnit == "ounces" && toUnit == "grams" -> value / 0.035274

            // Data
            fromUnit == "MB" && toUnit == "GB" -> value / 1024.0
            fromUnit == "GB" && toUnit == "MB" -> value * 1024.0
            fromUnit == "GB" && toUnit == "TB" -> value / 1024.0
            fromUnit == "TB" && toUnit == "GB" -> value * 1024.0

            else -> value
        }
    }
}
