package com.estonianport.clima.dto

import java.math.BigDecimal
import java.time.LocalDateTime

data class ClimaResponse(
    val timestamp: LocalDateTime,
    val temperature: BigDecimal,
    val humidity: Int,
    val isRaining: Boolean
)