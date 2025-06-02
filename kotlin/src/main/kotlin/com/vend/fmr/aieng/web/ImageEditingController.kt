package com.vend.fmr.aieng.web

import com.vend.fmr.aieng.openAI
import com.vend.fmr.aieng.utils.Models
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class ImageEditingController {

    companion object {
        val AVAILABLE_SIZES = listOf(
            "256x256" to "256×256",
            "512x512" to "512×512", 
            "1024x1024" to "1024×1024"
        )
        
        val DEMO_IMAGES = mapOf(
            "ski.png" to "ski_mask.png"
        )
        
        const val DEFAULT_PROMPT = "A hot air balloon floating in the sky"
    }

    @GetMapping("/demo/image-editing")
    fun imageEditingDemo(model: Model): String {
        model.addAttribute("pageTitle", "AI Image Editing")
        model.addAttribute("activeTab", "image-editing")
        
        model.addAttribute("availableSizes", AVAILABLE_SIZES)
        model.addAttribute("demoImages", DEMO_IMAGES)
        model.addAttribute("defaultPrompt", DEFAULT_PROMPT)
        
        // Ensure boolean attributes are set for Thymeleaf
        model.addAttribute("success", false)
        model.addAttribute("error", null)
        
        return "image-editing-demo"
    }

    @PostMapping("/demo/image-editing")
    suspend fun editImage(
        @RequestParam("prompt") prompt: String,
        @RequestParam("size", defaultValue = "1024x1024") size: String,
        model: Model
    ): String {
        model.addAttribute("pageTitle", "AI Image Editing")
        model.addAttribute("activeTab", "image-editing")
        
        // Preserve form data
        val formData = mutableMapOf<String, String>()
        formData["prompt"] = prompt
        formData["size"] = size
        model.addAttribute("formData", formData)
        
        // Add all dropdown options back
        model.addAttribute("availableSizes", AVAILABLE_SIZES)
        model.addAttribute("demoImages", DEMO_IMAGES)
        model.addAttribute("defaultPrompt", DEFAULT_PROMPT)
        
        // Initialize boolean attributes
        model.addAttribute("success", false)
        model.addAttribute("error", null)

        try {
            // Load the demo images from classpath
            val originalImageResource = ClassPathResource("static/images/ski.png")
            val maskImageResource = ClassPathResource("static/images/ski_mask.png")
            
            val originalImageBytes = originalImageResource.inputStream.readBytes()
            val maskImageBytes = maskImageResource.inputStream.readBytes()
            
            val editResponse = openAI.editImage(
                prompt = prompt,
                imageFile = originalImageBytes,
                maskFile = maskImageBytes,
                model = Models.ImageGeneration.DALL_E_2,
                size = size,
                debug = false
            )

            val editedImageUrl = editResponse.data.firstOrNull()?.url
            
            if (editedImageUrl != null) {
                model.addAttribute("editedImageUrl", editedImageUrl)
                model.addAttribute("originalImageUrl", "/images/ski.png")
                model.addAttribute("maskImageUrl", "/images/ski_mask.png")
                model.addAttribute("usedPrompt", prompt)
                model.addAttribute("usedSize", size)
                model.addAttribute("success", true)
            } else {
                model.addAttribute("error", "No edited image was generated. Please try again.")
            }

        } catch (e: Exception) {
            model.addAttribute("error", "Failed to edit image: ${e.message}")
        }

        return "image-editing-demo"
    }
}