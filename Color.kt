package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Core Garage Pro Dark Palette (matching user's screenshots)
val GarageDarkBg = Color(0xFF14161D)
val GarageDarkSurface = Color(0xFF1E222B)
val GarageDarkCard = Color(0xFF232732)
val GarageDarkCardBorder = Color(0xFF313746)
val GarageDarkInput = Color(0xFF1A1E27)

// Light Palette
val GarageLightBg = Color(0xFFF3F5F9)
val GarageLightSurface = Color(0xFFFFFFFF)
val GarageLightCard = Color(0xFFFFFFFF)
val GarageLightCardBorder = Color(0xFFDDE3EE)
val GarageLightInput = Color(0xFFE9EEF6)

// Brand Accents
val GarageOrange = Color(0xFFFF6D00)
val GarageOrangeLight = Color(0xFFFF8F2B)
val GarageElectricBlue = Color(0xFF1E88E5)
val GarageElectricBlueBright = Color(0xFF2979FF)
val GarageEmeraldGreen = Color(0xFF2ECC71)
val GarageAmberYellow = Color(0xFFF39C12)
val GarageErrorRed = Color(0xFFE74C3C)

// Platform Brand Colors
val AvitoBlue = Color(0xFF00AAFF)
val AvitoGreen = Color(0xFF00E676)
val AvitoRed = Color(0xFFFF4081)
val AvitoPurple = Color(0xFF9C27B0)
val YoulaCyan = Color(0xFF00E5FF)
val YoulaPurple = Color(0xFFAA00FF)
val VkBlue = Color(0xFF0077FF)
val YandexRed = Color(0xFFFC3F1D)
val YandexCream = Color(0xFFFFF8E1)

enum class AppThemeMode(val key: String, val label: String) {
    DARK("dark", "Тёмная «Гараж Про»"),
    LIGHT("light", "Светлая дневная"),
    SYSTEM("system", "Системная")
}

enum class AppAccentPalette(
    val key: String,
    val label: String,
    val primary: Color,
    val secondary: Color
) {
    ORANGE_BLUE("orange_blue", "Оранжевый Драйв (Стандарт)", GarageOrange, GarageElectricBlueBright),
    NEON_BLUE("neon_blue", "Индустриальный Синий", Color(0xFF2979FF), Color(0xFF00E5FF)),
    EMERALD("emerald", "Изумрудный Склад", Color(0xFF10B981), Color(0xFF3B82F6)),
    CYBER_AMBER("cyber_amber", "Механик Голд", Color(0xFFF59E0B), Color(0xFFEC4899));

    companion object {
        fun fromKey(key: String): AppAccentPalette =
            entries.find { it.key == key } ?: ORANGE_BLUE
    }
}
