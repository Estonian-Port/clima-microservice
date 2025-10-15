package com.estonianport.clima.model.enum

enum class EstadoClimaType() {
    SOLEADO,
    PARCIAL,
    NUBLADO,
    LLUVIA,
    TORMENTA;

    companion object {
        fun fromWeatherCondition(weatherMain: String, description: String): EstadoClimaType {
            return when {
                weatherMain.equals("Thunderstorm", ignoreCase = true) -> TORMENTA
                weatherMain.equals("Rain", ignoreCase = true) ||
                        weatherMain.equals("Drizzle", ignoreCase = true) -> LLUVIA
                weatherMain.equals("Clouds", ignoreCase = true) -> {
                    // Si la descripción contiene "few" o "scattered" = parcial
                    when {
                        description.contains("few", ignoreCase = true) -> PARCIAL
                        description.contains("scattered", ignoreCase = true) -> PARCIAL
                        description.contains("broken", ignoreCase = true) -> NUBLADO
                        description.contains("overcast", ignoreCase = true) -> NUBLADO
                        else -> NUBLADO
                    }
                }
                weatherMain.equals("Clear", ignoreCase = true) -> SOLEADO
                weatherMain.equals("Sunny", ignoreCase = true) -> SOLEADO
                else -> NUBLADO
            }
        }
    }
}
