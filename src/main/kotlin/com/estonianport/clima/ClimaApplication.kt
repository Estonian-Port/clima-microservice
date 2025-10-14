package com.estonianport.clima

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class ClimaApplication

fun main(args: Array<String>) {
	runApplication<ClimaApplication>(*args)
}
