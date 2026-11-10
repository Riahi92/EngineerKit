package com.example.domain.units

object UnitEngine {

    /**
     * Converts a numerical value from a source unit to a target unit.
     * Enforces strict dimensional compatibility and affine temperature conversions.
     */
    fun convert(value: Double, from: EngineeringUnit, to: EngineeringUnit): Double {
        if (from == to) return value

        require(from.dimension == to.dimension) {
            "Incompatibilité dimensionnelle : impossible de convertir ${from.dimension} en ${to.dimension}."
        }

        return if (from.dimension == UnitDimension.TEMPERATURE) {
            convertTemperature(value, from.symbol, to.symbol)
        } else {
            val baseValue = value * from.toBaseFactor
            baseValue / to.toBaseFactor
        }
    }

    /**
     * Affine temperature conversion handling Celsius, Kelvin, and Fahrenheit.
     */
    fun convertTemperature(value: Double, fromSymbol: String, toSymbol: String): Double {
        if (fromSymbol == toSymbol) return value

        // 1. Convert source to Kelvin
        val kelvin = when (fromSymbol) {
            "°C" -> value + 273.15
            "K" -> value
            "°F" -> (value - 32.0) * 5.0 / 9.0 + 273.15
            else -> throw IllegalArgumentException("Unité de température inconnue : $fromSymbol")
        }

        // 2. Convert Kelvin to target
        return when (toSymbol) {
            "°C" -> kelvin - 273.15
            "K" -> kelvin
            "°F" -> (kelvin - 273.15) * 9.0 / 5.0 + 32.0
            else -> throw IllegalArgumentException("Unité de température inconnue : $toSymbol")
        }
    }
}
