package com.vend.fmr.aieng

import com.vend.fmr.aieng.utils.createHttpClient
import com.vend.fmr.aieng.utils.createJson
import com.vend.fmr.aieng.utils.isValidApiKey
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.chat.DisabledChatModel
import dev.langchain4j.model.openai.OpenAiChatModel
import io.ktor.client.*
import kotlinx.serialization.json.Json
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication(exclude = [R2dbcAutoConfiguration::class])
class Application {

    /**
     * Manual LangChain4j ChatModel bean creation to avoid conflicts with Spring AI auto-configuration.
     * Both frameworks create beans named "ChatModel", causing Spring startup failures.
     * 
     * Alternative: Remove LangChain4j entirely and focus on Spring AI (recommended for future).
     * Spring AI is Spring's official AI framework with better long-term support.
     */
    @Bean("langchain4jChatModel")
    fun langchain4jChatModel(): ChatModel {
        val apiKey = System.getenv("OPENAI_API_KEY") ?: "dummy-key-for-auto-config"
        return if (apiKey.isValidApiKey()) {
            OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gpt-4o-mini")
                .temperature(0.7)
                .build()
        } else {
            DisabledChatModel()
        }
    }

    @Bean
    fun json(): Json = createJson()

    @Bean
    fun httpClient(json: Json): HttpClient = createHttpClient(json)
}

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}