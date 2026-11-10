package com.example.domain.formatting

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs

object NumberFormatter {

    fun format(value: Double, maxDecimals: Int = 4): String {
        if (value.isNaN()) return "Indéfini (NaN)"
        if (value.isInfinite()) return if (value > 0) "+∞" else "-∞"

        val absVal = abs(value)
        if (absVal != 0.0 && (absVal >= 1e7 || absVal < 1e-4)) {
            val symbols = DecimalFormatSymbols(Locale.US)
            val sciFormat = DecimalFormat("0.####E0", symbols)
            return sciFormat.format(value)
        }

        val pattern = buildString {
            append("0.")
            repeat(maxDecimals) { append("#") }
        }
        val symbols = DecimalFormatSymbols(Locale.US)
        val df = DecimalFormat(pattern, symbols)
        val formatted = df.format(value)
        return if (formatted == "-0") "0" else formatted
    }
}
