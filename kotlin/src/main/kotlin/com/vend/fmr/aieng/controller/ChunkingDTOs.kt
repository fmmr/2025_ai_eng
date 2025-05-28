package com.vend.fmr.aieng.controller

data class ChunkingResult(
    val totalChunks: Int,
    val totalCharacters: Int,
    val chunks: List<ChunkWithOverlap>
)
data class ChunkWithOverlap(
    val index: Int,
    val text: String,
    val length: Int,
    val prevOverlap: String,
    val nextOverlap: String
)

data class ChunkingFormData(
    val chunkSize: Int,
    val chunkOverlap: Int,
    val textInput: String
)