package com.vend.fmr.aieng.dtos

data class ParameterSet(
    val name: String,
    val description: String,
    val temperature: Double,
    val topP: Double,
    val color: String,
    val response: String = ""
)