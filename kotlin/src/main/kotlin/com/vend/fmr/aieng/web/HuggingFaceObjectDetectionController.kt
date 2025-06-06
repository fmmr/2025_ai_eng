package com.vend.fmr.aieng.web

import com.vend.fmr.aieng.huggingface
import com.vend.fmr.aieng.utils.Demo
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile

@Controller
class HuggingFaceObjectDetectionController : BaseController(Demo.OBJECT_DETECTION) {

    @GetMapping("/demo/huggingface-object-detection")
    fun objectDetectionDemo(model: Model): String {
        model.addAttribute("pageTitle", "HuggingFace Object Detection")
        model.addAttribute("activeTab", "huggingface-object-detection")
        
        model.addAttribute("defaultImage", "kitchen.png")
        model.addAttribute("explanation", "Detect objects in images using DETR (Detection Transformer) model")
        
        // Show kitchen.png as the "before" image on page load
        model.addAttribute("showBefore", true)
        model.addAttribute("beforeImageSrc", "/images/kitchen.png")
        model.addAttribute("beforeImageName", "kitchen.png")
        
        return "huggingface-object-detection-demo"
    }

    @PostMapping("/demo/huggingface-object-detection")
    suspend fun processObjectDetection(
        @RequestParam("image", required = false) imageFile: MultipartFile?,
        @RequestParam("useDefault", required = false) useDefault: String?,
        model: Model
    ): String {
        model.addAttribute("pageTitle", "HuggingFace Object Detection")
        model.addAttribute("activeTab", "huggingface-object-detection")
        
        try {
            val imageBytes: ByteArray
            val imageName: String
            val contentType: String
            
            if (useDefault == "true" || imageFile == null || imageFile.isEmpty) {
                // Use default kitchen.png image
                val resource = ClassPathResource("static/images/kitchen.png")
                imageBytes = resource.inputStream.readBytes()
                imageName = "kitchen.png"
                contentType = "image/png"
                model.addAttribute("usingDefault", true)
            } else {
                // Use uploaded image
                imageBytes = imageFile.bytes
                imageName = imageFile.originalFilename ?: "uploaded_image"
                contentType = imageFile.contentType ?: "image/jpeg"
                model.addAttribute("usingDefault", false)
                model.addAttribute("uploadedFileName", imageName)
            }
            
            val result = huggingface.detectObjects(
                imageBytes = imageBytes,
                contentType = contentType,
                debug = false
            )
            
            // Filter objects with confidence > 0.7 and sort by confidence
            val detectedObjects = result
                .filter { it.score > 0.7 }
                .sortedByDescending { it.score }
                .map { detection ->
                    mapOf(
                        "label" to detection.label,
                        "score" to detection.score,
                        "percentage" to (detection.score * 100).toInt(),
                        "box" to detection.box
                    )
                }
            
            model.addAttribute("imageName", imageName)
            model.addAttribute("imageSize", "${imageBytes.size} bytes")
            model.addAttribute("detectedObjects", detectedObjects)
            model.addAttribute("objectCount", detectedObjects.size)
            model.addAttribute("modelUsed", "facebook/detr-resnet-50")
            model.addAttribute("results", true)
            model.addAttribute("success", true)
            
            // Keep showing the before image section
            model.addAttribute("showBefore", true)
            if (useDefault == "true" || imageFile == null || imageFile.isEmpty) {
                model.addAttribute("beforeImageSrc", "/images/kitchen.png")
                model.addAttribute("beforeImageName", "kitchen.png")
            } else {
                model.addAttribute("beforeImageSrc", "data:${imageFile.contentType};base64," + java.util.Base64.getEncoder().encodeToString(imageBytes))
                model.addAttribute("beforeImageName", imageName)
            }
            
        } catch (e: Exception) {
            model.addAttribute("error", "Object detection failed: ${e.message}")
            // Still show before image on error
            model.addAttribute("showBefore", true)
            model.addAttribute("beforeImageSrc", "/images/kitchen.png")
            model.addAttribute("beforeImageName", "kitchen.png")
        }
        
        model.addAttribute("defaultImage", "kitchen.png")
        model.addAttribute("explanation", "Detect objects in images using DETR (Detection Transformer) model")
        
        return "huggingface-object-detection-demo"
    }
}