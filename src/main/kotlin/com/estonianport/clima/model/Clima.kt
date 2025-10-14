package com.estonianport.clima.model
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
    val temperature: BigDecimal,

    @Column(nullable = false)
    val humidity: Int,

    @Column(name = "is_raining", nullable = false)
    val isRaining: Boolean
)