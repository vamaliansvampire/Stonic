package com.example

import com.example.tools.CalculatorUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun calculator_evaluateBasicMath() {
        val result = CalculatorUtil.evaluate("2 + 3 * 4")
        assertTrue(result.isSuccess)
        assertEquals("14", result.getOrNull())
    }

    @Test
    fun calculator_evaluateScientificMath() {
        val result = CalculatorUtil.evaluate("sqrt(16) + 2^3")
        assertTrue(result.isSuccess)
        assertEquals("12", result.getOrNull())
    }

    @Test
    fun calculator_unitConversion() {
        val fahrenheit = CalculatorUtil.convertUnits(0.0, "°C", "°F")
        assertEquals(32.0, fahrenheit, 0.01)
    }
}
