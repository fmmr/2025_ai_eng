package com.vend.fmr.aieng.config

import org.springframework.core.env.PropertySource
import org.springframework.stereotype.Component

class DotEnvPropertySource : PropertySource<Map<String, String>>(
    "dotenv", 
    createProperties()
) {
    
    companion object {
        private fun createProperties(): Map<String, String> {
            // Read .env file if it exists
            val envFileContent = object {}.javaClass.getResourceAsStream("/.env")
                ?.bufferedReader()
                ?.use { it.readText() }
                ?.split("\n")
                ?.filter { it.contains("=") && !it.startsWith("#") }
                ?.associate { it.substringBefore("=").trim() to it.substringAfter("=").trim() }
                ?: emptyMap()
            
            val properties = mutableMapOf<String, String>()
            
            // Define env var → Spring property mappings
            val mappings = mapOf(
                "OPENAI_API_KEY" to listOf("spring.ai.openai.api-key", "langchain4j.open-ai.api-key"),
                "SUPABASE_URL" to listOf("supabase.url"),
                "SUPABASE_ANON_KEY" to listOf("supabase.anon-key"), 
                "POLYGON_API_KEY" to listOf("polygon.api-key"),
                "HF_TOKEN" to listOf("huggingface.token")
            )
            
            // Process each mapping
            mappings.forEach { (envVar, springProps) ->
                val value = envFileContent[envVar] ?: System.getenv(envVar)
                if (value != null) {
                    // Set original env var name (for Main.kt compatibility)
                    properties[envVar] = value
                    // Set all Spring property variants
                    springProps.forEach { springProp ->
                        properties[springProp] = value
                    }
                }
            }
            
            return properties
        }
    }
    
    override fun getProperty(name: String): Any? = source[name]
}