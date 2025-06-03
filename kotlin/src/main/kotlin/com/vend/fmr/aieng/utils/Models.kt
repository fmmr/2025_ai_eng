@file:Suppress("unused")

package com.vend.fmr.aieng.utils

/**
 * Centralized AI model constants for the entire application.
 * All model strings are defined here to avoid magic strings throughout the codebase.
 */
object Models {
    
    // OpenAI Chat Completion Models
    object Chat {
        const val GPT_4 = "gpt-4"
        const val GPT_4_TURBO = "gpt-4-turbo"
        const val GPT_4O = "gpt-4o"
        const val GPT_4O_MINI = "gpt-4o-mini"
        const val GPT_3_5_TURBO = "gpt-3.5-turbo"
    }
    
    // OpenAI Embedding Models
    object Embeddings {
        const val TEXT_EMBEDDING_ADA_002 = "text-embedding-ada-002"
        const val TEXT_EMBEDDING_3_SMALL = "text-embedding-3-small"
        const val TEXT_EMBEDDING_3_LARGE = "text-embedding-3-large"
    }
    
    // OpenAI Image Generation Models
    object ImageGeneration {
        const val DALL_E_2 = "dall-e-2"
        const val DALL_E_3 = "dall-e-3"
    }
    
    // OpenAI Vision Models (GPT-4 with Vision capabilities)
    object Vision {
        const val GPT_4_VISION_PREVIEW = "gpt-4-vision-preview" // Deprecated
        const val GPT_4O = "gpt-4o"
        const val GPT_4O_MINI = "gpt-4o-mini"
    }
    
    // Default models for specific use cases
    object Defaults {
        const val CHAT_COMPLETION = Chat.GPT_4
        const val EMBEDDING = Embeddings.TEXT_EMBEDDING_ADA_002
        const val IMAGE_GENERATION = ImageGeneration.DALL_E_3
        const val VISION_ANALYSIS = Vision.GPT_4O
        const val FUNCTION_CALLING = Chat.GPT_4
        const val REACT_AGENT = Chat.GPT_4
    }

    object HuggingFace{
        const val BART_CLASSIFICATION = "facebook/bart-large-mnli"
        const val BART_SUMMARIZATION = "facebook/bart-large-cnn"
        const val DETR_OBJECT_DETECTION = "facebook/detr-resnet-50"
    }

    // Model display names for UI
    object DisplayNames {
        val CHAT_MODELS = mapOf(
            Chat.GPT_4 to "GPT-4",
            Chat.GPT_4_TURBO to "GPT-4 Turbo",
            Chat.GPT_4O to "GPT-4o (Omni)",
            Chat.GPT_4O_MINI to "GPT-4o Mini",
            Chat.GPT_3_5_TURBO to "GPT-3.5 Turbo"
        )
        
        val VISION_MODELS = mapOf(
            Vision.GPT_4O to "GPT-4o (Omni)",
            Vision.GPT_4O_MINI to "GPT-4o Mini"
        )
        
        val IMAGE_GENERATION_MODELS = mapOf(
            ImageGeneration.DALL_E_2 to "DALL-E 2",
            ImageGeneration.DALL_E_3 to "DALL-E 3"
        )
    }
}

