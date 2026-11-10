package com.example.core.model

enum class ThemeMode(val title: String) {
    DARK("Sombre (Recommandé)"),
    LIGHT("Clair"),
    SYSTEM("Système")
}

enum class UnitSystem(val title: String, val subtitle: String) {
    METRIC_SI("Système International (SI)", "Mètres, Kilogrammes, Pascals, Joules"),
    IMPERIAL_US("Système Impérial (US)", "Pieds, Livres, PSI, BTU")
}

enum class DecimalPrecision(val decimals: Int, val label: String) {
    TWO(2, "2 décimales (ex: 3.14)"),
    FOUR(4, "4 décimales (ex: 3.1416)"),
    SIX(6, "6 décimales (ex: 3.141592)")
}
