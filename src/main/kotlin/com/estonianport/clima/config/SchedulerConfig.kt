package com.estonianport.clima.config

import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.SchedulingConfigurer
import org.springframework.scheduling.config.ScheduledTaskRegistrar
import java.util.TimeZone
import java.util.concurrent.Executor
import java.util.concurrent.Executors

@Configuration
class SchedulerConfig : SchedulingConfigurer {

    override fun configureTasks(taskRegistrar: ScheduledTaskRegistrar) {
        taskRegistrar.setScheduler(taskExecutor())
    }

    fun taskExecutor(): Executor {
        TimeZone.setDefault(TimeZone.getTimeZone("America/Argentina/Buenos_Aires"))
        return Executors.newScheduledThreadPool(5)
    }
}