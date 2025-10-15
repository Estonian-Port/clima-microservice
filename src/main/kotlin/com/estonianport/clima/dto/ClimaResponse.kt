package com.estonianport.clima.dto

import com.estonianport.clima.model.enum.EstadoClimaType
import java.math.BigDecimal
import java.time.LocalDateTime

data class ClimaResponse(
    val timestamp: LocalDateTime,
    val temperatura: BigDecimal,
    val humedad: Int,
    val estadoClima: EstadoClimaType,
)