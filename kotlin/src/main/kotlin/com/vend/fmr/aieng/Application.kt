package com.vend.fmr.aieng

import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.chat.DisabledChatModel
import dev.langchain4j.model.openai.OpenAiChatModel
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication(exclude = [R2dbcAutoConfiguration::class])
class Application{

    @Bean("langchain4jChatModel")
    fun langchain4jChatModel(): ChatModel {
        return if (OPEN_AI_KEY.isNotBlank()) {
            OpenAiChatModel.builder()
                .apiKey(OPEN_AI_KEY)
                .modelName("gpt-4o-mini")
                .temperature(0.7)
                .build()
        } else {
            DisabledChatModel()
        }
    }
}

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}