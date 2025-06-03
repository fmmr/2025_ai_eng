package com.vend.fmr.aieng.apis.huggingface

import com.vend.fmr.aieng.utils.Models.HuggingFace.BART_CLASSIFICATION
import com.vend.fmr.aieng.utils.Models.HuggingFace.BART_SUMMARIZATION
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.core.*
import kotlinx.serialization.json.Json

class HuggingFace(private val token: String) : Closeable {
    
    companion object {
        private const val BASE_URL = "https://router.huggingface.co/hf-inference/models"
        
        object Models {
        }

        private val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
    }

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(json)
        }
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

    override fun close() {
        client.close()
    }
}