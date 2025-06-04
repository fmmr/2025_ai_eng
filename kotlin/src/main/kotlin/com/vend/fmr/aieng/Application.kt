package com.vend.fmr.aieng

import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.chat.DisabledChatModel
import dev.langchain4j.model.openai.OpenAiChatModel
import org.springframework.ai.chat.model.ChatModel as SpringAiChatModel
import org.springframework.ai.chat.model.ChatResponse
import org.springframework.ai.chat.model.Generation
import org.springframework.ai.chat.messages.AssistantMessage
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.openai.OpenAiChatModel as SpringAiOpenAiChatModel
import org.springframework.ai.openai.api.OpenAiApi
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration
import org.springframework.ai.autoconfigure.openai.OpenAiAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

// Disabled implementation for Spring AI ChatModel
class DisabledSpringAiChatModel : SpringAiChatModel {
    override fun call(prompt: Prompt): ChatResponse {
        val message = AssistantMessage("Spring AI is not available - OPEN_AI_KEY environment variable is required")
        val generation = Generation(message)
        return ChatResponse(listOf(generation))
    }
}

@SpringBootApplication(exclude = [R2dbcAutoConfiguration::class, OpenAiAutoConfiguration::class])
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
    
    @Bean("springAiChatModel")
    fun springAiChatModel(): SpringAiChatModel {
        return if (OPEN_AI_KEY.isNotBlank()) {
            SpringAiOpenAiChatModel(
                OpenAiApi(OPEN_AI_KEY)
            )
        } else {
            DisabledSpringAiChatModel()
        }
    }
}

fun main(args: Array<String>) {
    println(OPEN_AI_KEY)
    runApplication<Application>(*args)
}