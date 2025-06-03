package com.vend.fmr.aieng.apis.huggingface

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HuggingFaceRequest(
    val inputs: String,
    val parameters: HuggingFaceParameters? = null
)

@Serializable
data class HuggingFaceParameters(
    @SerialName("candidate_labels") val candidateLabels: List<String>? = null,
    @SerialName("max_length") val maxLength: Int? = null,
    @SerialName("min_length") val minLength: Int? = null,
    @SerialName("do_sample") val doSample: Boolean? = null,
    val threshold: Double? = null,
    val percentage: Boolean? = null
)

@Serializable
data class HuggingFaceClassificationResponse(
    val sequence: String,
    val labels: List<String>,
    val scores: List<Double>
)

@Serializable
data class HuggingFaceSummarizationResponse(
    @SerialName("summary_text") val summaryText: String
)

@Serializable
data class HuggingFaceObjectDetectionResponse(
    val label: String,
    val score: Double,
    val box: BoundingBox
)

@Serializable
data class BoundingBox(
    val xmin: Int,
    val ymin: Int,
    val xmax: Int,
    val ymax: Int
)