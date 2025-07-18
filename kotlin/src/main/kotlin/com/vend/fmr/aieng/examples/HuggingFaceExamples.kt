@file:Suppress("unused")

package com.vend.fmr.aieng.examples

import com.vend.fmr.aieng.apis.huggingface.HuggingFace
import com.vend.fmr.aieng.apis.openai.OpenAI

suspend fun huggingFaceClassificationDemo(huggingface: HuggingFace, debug: Boolean = false) {
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

suspend fun huggingFaceSummarizationDemo(huggingface: HuggingFace, debug: Boolean = false) {
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

suspend fun huggingFaceComparisonDemo(huggingface: HuggingFace, openAI: OpenAI, debug: Boolean = false) {
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
        val openAiResponse = openAI.createChatCompletion(
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

suspend fun huggingFaceObjectDetectionDemo(huggingface: HuggingFace, debug: Boolean = false) {
    println("\n=== HUGGINGFACE OBJECT DETECTION DEMO ===")
    
    // Use the kitchen.png image from resources
    val imageResource = object {}.javaClass.classLoader.getResourceAsStream("static/images/kitchen.png")
    if (imageResource == null) {
        println("Error: kitchen.png not found in resources")
        return
    }
    
    val imageBytes = imageResource.readBytes()
    println("Image loaded: ${imageBytes.size} bytes")
    
    val result = huggingface.detectObjects(
        imageBytes = imageBytes,
        contentType = "image/png",
        debug = debug
    )
    
    // Filter objects with confidence > 0.7
    val highConfidenceObjects = result.filter { it.score > 0.9 }.sortedByDescending { it.score }
    
    println("\nDetected Objects (confidence > 70%):")
    if (highConfidenceObjects.isEmpty()) {
        println("  No objects detected with high confidence")
    } else {
        highConfidenceObjects.forEach { detection ->
            println("  ${detection.label}: ${(detection.score * 100).toInt()}% confidence")
            println("    Box: (${detection.box.xmin}, ${detection.box.ymin}) to (${detection.box.xmax}, ${detection.box.ymax})")
        }
    }
    
    println("\nAll detected objects:")
    result.sortedByDescending { it.score }.forEach { detection ->
        println("  ${detection.label}: ${(detection.score * 100).toInt()}% confidence")
    }
    
    println("\nModel: facebook/detr-resnet-50 (DETR - Detection Transformer)")
    println("Total objects found: ${result.size}")
    println("High confidence objects (>70%): ${highConfidenceObjects.size}")
}

suspend fun huggingFaceDemo(huggingface: HuggingFace, openAI: OpenAI, debug: Boolean = false) {
    huggingFaceClassificationDemo(huggingface, debug)
    huggingFaceSummarizationDemo(huggingface, debug)
    huggingFaceObjectDetectionDemo(huggingface, debug)
    huggingFaceComparisonDemo(huggingface, openAI, debug)
}