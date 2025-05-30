package com.vend.fmr.aieng.web

data class EmbeddingResult(
    val text: String,
    val embedding: List<Double>,
    val dimensions: Int,
    val showVector: Boolean,
    val first10Values: List<Double>,
    val minValue: Double,
    val maxValue: Double,
    val avgValue: Double
)

data class EmbeddingFormData(
    val inputText: String,
    val showVector: Boolean
)