package com.vend.fmr.aieng.apis.huggingface

import com.vend.fmr.aieng.utils.Models.HuggingFace.BART_CLASSIFICATION
import com.vend.fmr.aieng.utils.Models.HuggingFace.BART_SUMMARIZATION
import com.vend.fmr.aieng.utils.Models.HuggingFace.DETR_OBJECT_DETECTION
import com.vend.fmr.aieng.utils.env
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.core.*
import kotlinx.serialization.json.Json
import org.springframework.stereotype.Service

@Service
class HuggingFace(val client: HttpClient, val json: Json) : Closeable {
    private val token = "HF_TOKEN".env()
    
    companion object {
        private const val BASE_URL = "https://router.huggingface.co/hf-inference/models"
    }

    suspend fun classify(
        text: String,
        labels: List<String>,
        model: String = BART_CLASSIFICATION,
        debug: Boolean = false
    ): HuggingFaceClassificationResponse {
        val request = HuggingFaceRequest(
            inputs = text,
            parameters = HuggingFaceParameters(candidateLabels = labels)
        )

        val response = client.post("$BASE_URL/$model") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
            setBody(request)
        }

        val responseText = response.bodyAsText()
        if (debug) {
            println("HuggingFace Classification API Status: ${response.status}")
            println("Model: $model")
            println("Input: $text")
            println("Labels: $labels")
            println("Response: $responseText")
        }
        return json.decodeFromString<HuggingFaceClassificationResponse>(responseText)
    }

    suspend fun summarize(
        text: String,
        maxLength: Int? = 130,
        minLength: Int? = 30,
        doSample: Boolean? = true,
        model: String = BART_SUMMARIZATION,
        debug: Boolean = false
    ): HuggingFaceSummarizationResponse {
        val request = HuggingFaceRequest(
            inputs = text,
            parameters = HuggingFaceParameters(
                maxLength = maxLength,
                minLength = minLength,
                doSample = doSample
            )
        )

        val response = client.post("$BASE_URL/$model") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
            setBody(request)
        }

        val responseText = response.bodyAsText()
        if (debug) {
            println("HuggingFace Summarization API Status: ${response.status}")
            println("Model: $model")
            println("Input length: ${text.length} characters")
            println("Max length: $maxLength, Min length: $minLength, Do sample: $doSample")
            println("Response: $responseText")
        }
        val responseList = json.decodeFromString<List<HuggingFaceSummarizationResponse>>(responseText)
        return responseList.first()
    }

    suspend fun detectObjects(
        imageBytes: ByteArray,
        contentType: String = "image/jpeg",
        model: String = DETR_OBJECT_DETECTION,
        debug: Boolean = false
    ): List<HuggingFaceObjectDetectionResponse> {
        val response = client.post("$BASE_URL/$model") {
            contentType(ContentType.parse(contentType))
            header("Authorization", "Bearer $token")
            setBody(imageBytes)
        }

        val responseText = response.bodyAsText()
        if (debug) {
            println("HuggingFace Object Detection API Status: ${response.status}")
            println("Model: $model")
            println("Image size: ${imageBytes.size} bytes")
            println("Content-Type: $contentType")
            println("Response: $responseText")
        }
        return json.decodeFromString<List<HuggingFaceObjectDetectionResponse>>(responseText)
    }

    override fun close() {
        client.close()
    }
}