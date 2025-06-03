@file:Suppress("unused")

package com.vend.fmr.aieng.examples

import com.vend.fmr.aieng.huggingface

suspend fun huggingFaceClassificationDemo(debug: Boolean = false) {
    println("=== HUGGINGFACE CLASSIFICATION DEMO ===")
    
    val texts = listOf(
        "I love this new product, it works amazing!",
        "This is the worst experience I've ever had",
        "The weather is okay today, nothing special"
    )
    
    texts.forEach { text ->
        println("\nText: \"$text\"")
        val result = huggingface.classify(
            text = text,
            labels = listOf("positive", "negative", "neutral"),
            debug = debug
        )
        
        println("Classification Results:")
        result.labels.zip(result.scores).forEach { (label, score) ->
            println("  $label: ${(score * 100).toInt()}%")
        }
    }
}

suspend fun huggingFaceSummarizationDemo(debug: Boolean = false) {
    println("\n=== HUGGINGFACE SUMMARIZATION DEMO ===")
    
    val longText = """
        The emergence of artificial intelligence has transformed numerous industries and continues to shape our daily lives. 
        From healthcare to transportation, AI technologies are being integrated into systems that improve efficiency and decision-making. 
        Machine learning algorithms can analyze vast amounts of data to identify patterns and make predictions. 
        Natural language processing enables computers to understand and generate human language. 
        Computer vision allows machines to interpret and analyze visual information. 
        These advancements are creating new opportunities while also raising important questions about ethics, privacy, and the future of work.
        As AI continues to evolve, it's crucial for organizations and individuals to understand both its potential benefits and limitations.
        The technology requires careful consideration of bias, fairness, and transparency in its implementation.
        Educational institutions are adapting their curricula to prepare students for an AI-driven future.
        Governments worldwide are developing regulations and policies to ensure responsible AI development and deployment.
    """.trimIndent()
    
    println("Original text (${longText.length} characters):")
    println("\"${longText.take(100)}...\"")
    
    // Test different summary lengths
    val shortSummary = huggingface.summarize(text = longText, maxLength = 50, minLength = 20, debug = debug)
    val mediumSummary = huggingface.summarize(text = longText, maxLength = 100, minLength = 40, debug = debug)
    val longSummary = huggingface.summarize(text = longText, maxLength = 200, minLength = 80, debug = debug)
    
    println("\nShort Summary (max 50, min 20):")
    println("\"${shortSummary.summaryText}\" (${shortSummary.summaryText.length} chars)")
    
    println("\nMedium Summary (max 100, min 40):")
    println("\"${mediumSummary.summaryText}\" (${mediumSummary.summaryText.length} chars)")
    
    println("\nLong Summary (max 200, min 80):")
    println("\"${longSummary.summaryText}\" (${longSummary.summaryText.length} chars)")
    
    val shortCompression = ((longText.length - shortSummary.summaryText.length).toDouble() / longText.length * 100).toInt()
    println("\nCompression ratios:")
    println("  Short: $shortCompression% reduction")
    println("  Medium: ${((longText.length - mediumSummary.summaryText.length).toDouble() / longText.length * 100).toInt()}% reduction")
    println("  Long: ${((longText.length - longSummary.summaryText.length).toDouble() / longText.length * 100).toInt()}% reduction")
}

suspend fun huggingFaceComparisonDemo(debug: Boolean = false) {
    println("\n=== HUGGINGFACE vs OPENAI COMPARISON ===")
    
    val testText = "This product exceeded my expectations! Great quality and fast delivery."
    
    println("Test text: \"$testText\"\n")
    
    // HuggingFace Classification
    println("🤗 HUGGINGFACE (Open Source):")
    val hfResult = huggingface.classify(
        text = testText,
        labels = listOf("positive", "negative", "neutral"),
        debug = debug
    )
    hfResult.labels.zip(hfResult.scores).forEach { (label, score) ->
        println("  $label: ${(score * 100).toInt()}%")
    }
    
    // OpenAI Classification
    println("\n🤖 OPENAI (Commercial):")
    try {
        val openAiResponse = com.vend.fmr.aieng.openAI.createChatCompletion(
            prompt = "Classify this text as positive, negative, or neutral: \"$testText\"",
            systemMessage = "You are a sentiment classifier. Respond with only: positive, negative, or neutral.",
            maxTokens = 10
        )
        println("  Classification: ${openAiResponse.text()}")
    } catch (e: Exception) {
        println("  Error: ${e.message}")
    }
    
    println("\n💰 Cost Comparison:")
    println("  HuggingFace: Free (open source model)")
    println("  OpenAI: Pay-per-token (commercial API)")
    
    println("\n🔒 Privacy Comparison:")
    println("  HuggingFace: Data processed on their infrastructure")
    println("  OpenAI: Data sent to OpenAI servers")
}

suspend fun huggingFaceDemo(debug: Boolean = false) {
    huggingFaceClassificationDemo(debug)
    huggingFaceSummarizationDemo(debug)
    huggingFaceComparisonDemo(debug)
}