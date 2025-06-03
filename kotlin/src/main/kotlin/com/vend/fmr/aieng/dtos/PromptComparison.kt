package com.vend.fmr.aieng.dtos

data class PromptComparison(
    val scenario: String,
    val vague: String,
    val better: String,
    val excellent: String,
    val vagueResponse: String = "",
    val betterResponse: String = "",
    val excellentResponse: String = ""
)