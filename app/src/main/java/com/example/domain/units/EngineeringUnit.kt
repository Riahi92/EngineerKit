package com.example.domain.units

import androidx.compose.runtime.Immutable

enum class UnitDimension(val displayName: String) {
    LENGTH("Longueur"),
    AREA("Surface"),
    VOLUME("Volume"),
    MASS("Masse"),
    FORCE("Force"),
    PRESSURE("Pression & Contrainte"),
    TORQUE("Couple mécanique"),
    POWER("Puissance"),
    ENERGY("Énergie & Travail"),
    TEMPERATURE("Température"),
    TIME("Temps"),
    ANGULAR_VELOCITY("Vitesse angulaire")
}

@Immutable
data class EngineeringUnit(
    val symbol: String,
    val name: String,
    val dimension: UnitDimension,
    val toBaseFactor: Double = 1.0 // Multiplier to SI Base unit
)

object UnitRegistry {

    // LENGTH (Base: m)
    val MM = EngineeringUnit("mm", "Millimètre", UnitDimension.LENGTH, 0.001)
    val CM = EngineeringUnit("cm", "Centimètre", UnitDimension.LENGTH, 0.01)
    val M = EngineeringUnit("m", "Mètre", UnitDimension.LENGTH, 1.0)
    val KM = EngineeringUnit("km", "Kilomètre", UnitDimension.LENGTH, 1000.0)
    val INCH = EngineeringUnit("inch", "Pouce", UnitDimension.LENGTH, 0.0254)
    val FT = EngineeringUnit("ft", "Pied", UnitDimension.LENGTH, 0.3048)

    // AREA (Base: m²)
    val MM2 = EngineeringUnit("mm²", "Millimètre carré", UnitDimension.AREA, 1e-6)
    val CM2 = EngineeringUnit("cm²", "Centimètre carré", UnitDimension.AREA, 1e-4)
    val M2 = EngineeringUnit("m²", "Mètre carré", UnitDimension.AREA, 1.0)

    // VOLUME (Base: m³)
    val MM3 = EngineeringUnit("mm³", "Millimètre cube", UnitDimension.VOLUME, 1e-9)
    val CM3 = EngineeringUnit("cm³", "Centimètre cube", UnitDimension.VOLUME, 1e-6)
    val ML = EngineeringUnit("mL", "Millilitre", UnitDimension.VOLUME, 1e-6)
    val L = EngineeringUnit("L", "Litre", UnitDimension.VOLUME, 0.001)
    val M3 = EngineeringUnit("m³", "Mètre cube", UnitDimension.VOLUME, 1.0)

    // MASS (Base: kg)
    val MG = EngineeringUnit("mg", "Milligramme", UnitDimension.MASS, 1e-6)
    val G = EngineeringUnit("g", "Gramme", UnitDimension.MASS, 1e-3)
    val KG = EngineeringUnit("kg", "Kilogramme", UnitDimension.MASS, 1.0)
    val TONNE = EngineeringUnit("tonne", "Tonne métrique", UnitDimension.MASS, 1000.0)
    val LB = EngineeringUnit("lb", "Livre (Pound)", UnitDimension.MASS, 0.45359237)

    // FORCE (Base: N)
    val N = EngineeringUnit("N", "Newton", UnitDimension.FORCE, 1.0)
    val KN = EngineeringUnit("kN", "Kilonewton", UnitDimension.FORCE, 1000.0)
    val LBF = EngineeringUnit("lbf", "Livre-force", UnitDimension.FORCE, 4.4482216)

    // PRESSURE & STRESS (Base: Pa)
    val PA = EngineeringUnit("Pa", "Pascal", UnitDimension.PRESSURE, 1.0)
    val KPA = EngineeringUnit("kPa", "Kilopascal", UnitDimension.PRESSURE, 1000.0)
    val MPA = EngineeringUnit("MPa", "Mégapascal", UnitDimension.PRESSURE, 1e6)
    val BAR = EngineeringUnit("bar", "Bar", UnitDimension.PRESSURE, 1e5)
    val PSI = EngineeringUnit("psi", "Pound per square inch", UnitDimension.PRESSURE, 6894.757)

    // TORQUE (Base: N·m)
    val NM = EngineeringUnit("N·m", "Newton mètre", UnitDimension.TORQUE, 1.0)
    val KNM = EngineeringUnit("kN·m", "Kilonewton mètre", UnitDimension.TORQUE, 1000.0)
    val LBF_FT = EngineeringUnit("lbf·ft", "Pound-foot", UnitDimension.TORQUE, 1.355818)

    // POWER (Base: W)
    val W = EngineeringUnit("W", "Watt", UnitDimension.POWER, 1.0)
    val KW = EngineeringUnit("kW", "Kilowatt", UnitDimension.POWER, 1000.0)
    val MW = EngineeringUnit("MW", "Mégawatt", UnitDimension.POWER, 1e6)
    val HP = EngineeringUnit("hp", "Cheval-vapeur mécanique", UnitDimension.POWER, 745.69987)

    // ENERGY (Base: J)
    val J = EngineeringUnit("J", "Joule", UnitDimension.ENERGY, 1.0)
    val KJ = EngineeringUnit("kJ", "Kilojoule", UnitDimension.ENERGY, 1000.0)
    val KWH = EngineeringUnit("kWh", "Kilowattheure", UnitDimension.ENERGY, 3.6e6)

    // TEMPERATURE (Affine: Base Kelvin)
    val CELSIUS = EngineeringUnit("°C", "Degré Celsius", UnitDimension.TEMPERATURE, 1.0)
    val KELVIN = EngineeringUnit("K", "Kelvin", UnitDimension.TEMPERATURE, 1.0)
    val FAHRENHEIT = EngineeringUnit("°F", "Degré Fahrenheit", UnitDimension.TEMPERATURE, 1.0)

    // TIME (Base: s)
    val SEC = EngineeringUnit("s", "Seconde", UnitDimension.TIME, 1.0)
    val MIN = EngineeringUnit("min", "Minute", UnitDimension.TIME, 60.0)
    val HOUR = EngineeringUnit("h", "Heure", UnitDimension.TIME, 3600.0)

    // ANGULAR VELOCITY (Base: rad/s)
    val RAD_S = EngineeringUnit("rad/s", "Radian par seconde", UnitDimension.ANGULAR_VELOCITY, 1.0)
    val RPM = EngineeringUnit("rpm", "Tours par minute", UnitDimension.ANGULAR_VELOCITY, Math.PI / 30.0)

    val allUnits: List<EngineeringUnit> = listOf(
        MM, CM, M, KM, INCH, FT,
        MM2, CM2, M2,
        MM3, CM3, ML, L, M3,
        MG, G, KG, TONNE, LB,
        N, KN, LBF,
        PA, KPA, MPA, BAR, PSI,
        NM, KNM, LBF_FT,
        W, KW, MW, HP,
        J, KJ, KWH,
        CELSIUS, KELVIN, FAHRENHEIT,
        SEC, MIN, HOUR,
        RAD_S, RPM
    )

    fun getUnitsForDimension(dimension: UnitDimension): List<EngineeringUnit> =
        allUnits.filter { it.dimension == dimension }

    fun byDimension(dimension: UnitDimension): List<EngineeringUnit> =
        getUnitsForDimension(dimension)

    fun findUnitBySymbol(symbol: String): EngineeringUnit? =
        allUnits.find { it.symbol.equals(symbol, ignoreCase = true) }
}
