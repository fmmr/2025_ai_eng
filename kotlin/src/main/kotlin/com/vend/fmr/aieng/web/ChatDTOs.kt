package com.vend.fmr.aieng.web

data class ChatResult(
    val userPrompt: String,
    val systemMessage: String,
    val response: String,
    val usage: String,
    val model: String
)

data class ChatFormData(
    val userPrompt: String,
    val systemMessage: String,
    val model: String,
    val maxTokens: Int,
    val temperature: Double
)