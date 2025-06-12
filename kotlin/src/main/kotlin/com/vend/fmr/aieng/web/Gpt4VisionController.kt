package com.vend.fmr.aieng.web

import com.vend.fmr.aieng.apis.openai.OpenAI
import com.vend.fmr.aieng.utils.Demo
import com.vend.fmr.aieng.utils.Models
import jakarta.servlet.http.HttpSession
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@RequestMapping("/demo/gpt4-vision")
class Gpt4VisionController(
    private val openAI: OpenAI
) : BaseController(Demo.GPT4_VISION) {

    companion object {
        val DEMO_IMAGES = listOf(
            DemoImage(
                filename = "magnus.png",
                title = "Chess Tournament",
                description = "Magnus Carlsen in a chess tournament setting",
                suggestedPrompts = listOf(
                    "Analyze the chess position on the board",
                    "Describe the tournament setting and atmosphere", 
                    "What can you tell me about the players and their setup?",
                    "Identify any sponsors or tournament details visible"
                )
            ),
            DemoImage(
                filename = "sushi.png", 
                title = "Sashimi Platter",
                description = "Beautiful Japanese sashimi and sushi arrangement",
                suggestedPrompts = listOf(
                    "Identify each type of fish and seafood in this platter",
                    "Assess the freshness and quality of the sashimi",
                    "Describe the presentation and traditional elements",
                    "What would you recommend trying first?"
                )
            ),
            DemoImage(
                filename = "briller.png",
                title = "Prescription Form", 
                description = "Glasses prescription document",
                suggestedPrompts = listOf(
                    "Extract all the prescription details from this form",
                    "Read and transcribe all visible text",
                    "What type of vision correction is prescribed?",
                    "Identify the optometrist or clinic information"
                )
            ),
            DemoImage(
                filename = "kompis.png",
                title = "Winter Dog",
                description = "Dog playing in winter snow scene",
                suggestedPrompts = listOf(
                    "What breed is this dog and describe its characteristics?",
                    "Describe the winter scene and weather conditions",
                    "What is the dog's mood and behavior in this photo?",
                    "Analyze the photographic composition and lighting"
                )
            )
        )

        val VISION_MODELS = Models.DisplayNames.VISION_MODELS.toList()

        val DETAIL_LEVELS = listOf(
            "auto" to "Auto (balanced cost/detail)",
            "low" to "Low (faster, cheaper)",
            "high" to "High (more detailed)"
        )
    }

    data class DemoImage(
        val filename: String,
        val title: String, 
        val description: String,
        val suggestedPrompts: List<String>
    )

    override fun addDefaultModel(model: Model, session: HttpSession) {
        model.addAttribute("demoImages", DEMO_IMAGES)
        model.addAttribute("visionModels", VISION_MODELS)
        model.addAttribute("detailLevels", DETAIL_LEVELS)
    }

    @PostMapping
    suspend fun analyzeImage(
        @RequestParam("selectedImage") selectedImage: String,
        @RequestParam("prompt") prompt: String,
        @RequestParam("model", defaultValue = Models.Defaults.VISION_ANALYSIS) visionModel: String,
        @RequestParam("detail", defaultValue = "auto") detail: String,
        model: Model
    ): String {
        // Preserve form data
        val formData = mutableMapOf<String, String>()
        formData["selectedImage"] = selectedImage
        formData["prompt"] = prompt
        formData["model"] = visionModel
        formData["detail"] = detail
        model.addAttribute("formData", formData)
        
        // Add all dropdown options back
        model.addAttribute("demoImages", DEMO_IMAGES)
        model.addAttribute("visionModels", VISION_MODELS)
        model.addAttribute("detailLevels", DETAIL_LEVELS)

        try {
            // Construct the full image URL
            val imageUrl = "https://ai.rodland.no/images/$selectedImage"
            
            // Get image details for context
            val selectedImageData = DEMO_IMAGES.find { it.filename == selectedImage }
            
            // Analyze the image with GPT-4 Vision
            val visionResponse = openAI.createVisionCompletion(
                prompt = prompt,
                imageUrl = imageUrl,
                model = visionModel,
                maxTokens = 500,
                detail = detail,
                debug = true
            )

            val analysis = visionResponse.text()

            if (analysis.isNotBlank()) {
                model.addAttribute("analysisResult", analysis)
                model.addAttribute("selectedImageData", selectedImageData)
                model.addAttribute("selectedImageUrl", "/images/$selectedImage")
                model.addAttribute("usedPrompt", prompt)
                model.addAttribute("usedModel", visionModel)
                model.addAttribute("success", true)
            } else {
                model.addAttribute("error", "No analysis was generated. Please try again.")
            }

        } catch (e: Exception) {
            model.addAttribute("error", "Failed to analyze image: ${e.message}")
        }

        return demo.id
    }
}