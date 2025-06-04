package com.vend.fmr.aieng.config

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent
import org.springframework.context.ApplicationListener
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.stereotype.Component

@Component
class PropertySourceConfiguration : ApplicationListener<ApplicationEnvironmentPreparedEvent> {
    
    override fun onApplicationEvent(event: ApplicationEnvironmentPreparedEvent) {
        val environment = event.environment as ConfigurableEnvironment
        val dotEnvPropertySource = DotEnvPropertySource()
        
        // Add our custom PropertySource with high priority
        environment.propertySources.addFirst(dotEnvPropertySource)
    }
}