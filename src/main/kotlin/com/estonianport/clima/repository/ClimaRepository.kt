package com.estonianport.clima.repository
import com.estonianport.clima.model.Clima
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface ClimaRepository : CrudRepository<Clima, Long> {

    fun findTopByOrderByTimestampDesc(): Clima?

    fun findByTimestampBetweenOrderByTimestampAsc(
        from: LocalDateTime,
        to: LocalDateTime
    ): List<Clima>

    fun existsByTimestamp(timestamp: LocalDateTime): Boolean
}