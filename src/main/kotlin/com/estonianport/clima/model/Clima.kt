package com.estonianport.clima.model
import com.estonianport.clima.model.enum.EstadoClimaType
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
class Clima(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true)
    val timestamp: LocalDateTime,

    @Column(nullable = false, precision = 5, scale = 2)
    val temperatura: BigDecimal,

    @Column(nullable = false)
    val humedad: Int,

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_clima", nullable = false)
    val estadoClima: EstadoClimaType
)
