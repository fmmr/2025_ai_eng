package com.vend.fmr.aieng.web

import com.vend.fmr.aieng.openAI
import com.vend.fmr.aieng.utils.Models
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class FilmFusionController {

    companion object {
        val MOVIE_TITLES = listOf(
            "Casablanca", "The Godfather", "Pulp Fiction", "The Matrix", "Star Wars",
            "Blade Runner", "Citizen Kane", "The Shawshank Redemption", "Goodfellas",
            "Apocalypse Now", "2001: A Space Odyssey", "Vertigo", "Psycho",
            "The Dark Knight", "Inception", "Fight Club", "The Lord of the Rings",
            "Jaws", "E.T.", "Back to the Future", "Alien", "Terminator",
            "Taxi Driver", "Raging Bull", "The Departed", "Heat", "Scarface",
            "The Big Lebowski", "No Country for Old Men", "There Will Be Blood",
            "Mad Max: Fury Road", "Interstellar", "La La Land", "Parasite",
            "Moonlight", "The Shape of Water", "Get Out", "Dunkirk", "1917",
            "Joker", "Once Upon a Time in Hollywood", "Avengers: Endgame",
            "Black Panther", "Spider-Man: Into the Spider-Verse", "Dune"
        ).sorted()

        val ART_STYLES = listOf(
            "Renaissance painting" to "Classical Renaissance art style with rich colors and detailed composition",
            "Art Nouveau poster" to "Flowing organic lines and decorative elements in vintage poster style", 
            "Film noir style" to "High contrast black and white with dramatic shadows and lighting",
            "Minimalist design" to "Clean, simple geometric shapes with limited color palette",
            "Impressionist painting" to "Soft brushstrokes and light-focused artistic interpretation",
            "Comic book style" to "Bold colors, dynamic action lines and comic book aesthetics",
            "Cyberpunk aesthetic" to "Neon colors, futuristic technology and dystopian atmosphere",
            "Art Deco poster" to "Geometric patterns, bold typography and luxurious 1920s style",
            "Surrealist artwork" to "Dreamlike, fantastical imagery with unexpected combinations",
            "Pop art style" to "Bright colors, bold graphics inspired by popular culture",
            "Gothic cathedral art" to "Dark, ornate medieval styling with religious symbolism",
            "Japanese woodblock print" to "Traditional ukiyo-e style with flat colors and flowing lines",
            "Abstract expressionism" to "Bold, emotional brushstrokes and non-representational forms",
            "Steampunk design" to "Victorian-era industrial machinery and brass aesthetic",
            "Vintage travel poster" to "Classic 1950s travel advertisement styling",
            "Graffiti street art" to "Urban spray paint style with bold colors and street culture",
            "Watercolor painting" to "Soft, translucent colors with organic paint flow",
            "Soviet propaganda poster" to "Bold red colors, strong worker imagery and socialist realism"
        )

        val DALL_E_MODELS = Models.DisplayNames.IMAGE_GENERATION_MODELS.toList()

        val DALL_E_2_SIZES = listOf(
            "256x256" to "256×256",
            "512x512" to "512×512", 
            "1024x1024" to "1024×1024"
        )

        val DALL_E_3_SIZES = listOf(
            "1024x1024" to "Square (1024×1024)",
            "1024x1792" to "Portrait (1024×1792)",
            "1792x1024" to "Landscape (1792×1024)"
        )

        val DALL_E_3_STYLES = listOf(
            "vivid" to "Vivid (dramatic, colorful)",
            "natural" to "Natural (more realistic)"
        )

        val QUALITY_OPTIONS = listOf(
            "standard" to "Standard quality",
            "hd" to "HD quality"
        )
    }

    @GetMapping("/demo/film-fusion")
    fun filmFusionDemo(model: Model): String {
        model.addAttribute("pageTitle", "Film Fusion")
        model.addAttribute("activeTab", "film-fusion")
        
        model.addAttribute("movieTitles", MOVIE_TITLES)
        model.addAttribute("artStyles", ART_STYLES)
        model.addAttribute("dalleModels", DALL_E_MODELS)
        model.addAttribute("dalle2Sizes", DALL_E_2_SIZES)
        model.addAttribute("dalle3Sizes", DALL_E_3_SIZES)
        model.addAttribute("dalle3Styles", DALL_E_3_STYLES)
        model.addAttribute("qualityOptions", QUALITY_OPTIONS)
        
        return "film-fusion-demo"
    }

    @PostMapping("/demo/film-fusion")
    suspend fun generateFilmFusionPoster(
        @RequestParam("movie") movie: String,
        @RequestParam("artStyle") artStyle: String,
        @RequestParam("model") dalleModel: String,
        @RequestParam("size") size: String,
        @RequestParam("style", required = false) style: String?,
        @RequestParam("quality", required = false) quality: String?,
        model: Model
    ): String {
        model.addAttribute("pageTitle", "Film Fusion")
        model.addAttribute("activeTab", "film-fusion")
        
        val formData = mutableMapOf<String, String>()
        formData["movie"] = movie
        formData["artStyle"] = artStyle
        formData["model"] = dalleModel
        formData["size"] = size
        style?.let { formData["style"] = it }
        quality?.let { formData["quality"] = it }
        model.addAttribute("formData", formData)
        model.addAttribute("movieTitles", MOVIE_TITLES)
        model.addAttribute("artStyles", ART_STYLES)
        model.addAttribute("dalleModels", DALL_E_MODELS)
        model.addAttribute("dalle2Sizes", DALL_E_2_SIZES)
        model.addAttribute("dalle3Sizes", DALL_E_3_SIZES)
        model.addAttribute("dalle3Styles", DALL_E_3_STYLES)
        model.addAttribute("qualityOptions", QUALITY_OPTIONS)

        try {
            val artStyleDescription = ART_STYLES.find { it.first == artStyle }?.second ?: artStyle
            val prompt = "Create a movie poster for '$movie' in the style of $artStyleDescription. " +
                    "The poster should capture the essence of the film while incorporating the distinctive visual elements of this artistic style. " +
                    "Focus on compelling composition, appropriate typography space, and visual storytelling."

            val imageResponse = openAI.generateImage(
                prompt = prompt,
                model = dalleModel,
                size = size,
                style = if (dalleModel == Models.ImageGeneration.DALL_E_3) style else null,
                quality = if (dalleModel == Models.ImageGeneration.DALL_E_3) quality else null,
                debug = true
            )

            // Get the first image URL
            val imageUrl = imageResponse.data.firstOrNull()?.url
            val revisedPrompt = imageResponse.data.firstOrNull()?.revisedPrompt

            if (imageUrl != null) {
                model.addAttribute("generatedImageUrl", imageUrl)
                model.addAttribute("originalPrompt", prompt)
                model.addAttribute("revisedPrompt", revisedPrompt)
                model.addAttribute("selectedMovie", movie)
                model.addAttribute("selectedArtStyle", artStyle)
                model.addAttribute("success", true)
            } else {
                model.addAttribute("error", "No image was generated. Please try again.")
            }

        } catch (e: Exception) {
            model.addAttribute("error", "Failed to generate image: ${e.message}")
        }

        return "film-fusion-demo"
    }
}