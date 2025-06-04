@file:Suppress("UnusedImport")

package com.vend.fmr.aieng

import com.vend.fmr.aieng.apis.geolocation.Geolocation
import com.vend.fmr.aieng.apis.huggingface.HuggingFace
import com.vend.fmr.aieng.apis.openai.OpenAI
import com.vend.fmr.aieng.apis.openai.OpenAIAssistant
import com.vend.fmr.aieng.apis.polygon.Polygon
import com.vend.fmr.aieng.apis.supabase.Supabase
import com.vend.fmr.aieng.apis.weather.Weather
import com.vend.fmr.aieng.examples.multiMessageChat
import com.vend.fmr.aieng.utils.*

val OPEN_AI_KEY = "OPENAI_API_KEY".env()
val SUPABASE_URL = "SUPABASE_URL".env()
val SUPABASE_KEY = "SUPABASE_ANON_KEY".env()
val POLYGON_API_KEY = "POLYGON_API_KEY".env()
val HF_TOKEN = "HF_TOKEN".env()

const val OPEN_AI_MODEL = Models.Defaults.CHAT_COMPLETION
const val EMBEDDING_MODEL = Models.Defaults.EMBEDDING

val openAI = OpenAI(OPEN_AI_KEY)
val assistant = OpenAIAssistant(OPEN_AI_KEY)
val supabase = Supabase(SUPABASE_URL, SUPABASE_KEY)
val polygon = Polygon(POLYGON_API_KEY)
val weather = Weather()
val geolocation = Geolocation()
val huggingface = HuggingFace(HF_TOKEN)

val apis = listOf(openAI, assistant, supabase, polygon, weather, geolocation, huggingface)

@Suppress("RedundantSuspendModifier", "RedundantSuppression")
suspend fun main() {
    Runtime.getRuntime().addShutdownHook(Thread { close() })

//    chatParametersDemo(debug = true)

//    promptEngineeringDemo(debug = true)

    val conversation = multiMessageChat(debug = true)
    println(conversation)

//    val result3 = FunctionCallingExamples.functionCallingAgent("Do you have any ideas for activities I can do at my location?", debug = true)
//    println("Final Function Call Result: $result3")
//
//    val result4 = FunctionCallingExamples.functionCallingAgent("Can you tell me the current stock price of Apple and Microsoft, and what time it is?", debug = true)
//    println("Final Function Call Result: $result4")

//    tickers("APPL", "NHYDY")
//    embeddings("/movies.txt", true)
//    clearDbAndInsertDocs(file = "/movies.txt")
//    splitText("/movies.txt", debug = true)
//    queryVectorDbForMovies("action packed thriller")
//    chatGPT()
//    enrichedMovieChat("The movie with that actor from Castaway", debug = true)

    // === LOCATION & WEATHER API EXAMPLES ===
    // Get location from IP and weather for that location
//    val location = geolocation.getLocationByIp("51.175.221.12", debug = true)
//    println(geolocation.formatLocationSummary(location))
//    val weatherData = weather.getNowcast(location.latitude, location.longitude, debug = true)
//    val currentWeather = weather.getCurrentWeather(weatherData)
//    if (currentWeather != null) {
//        println(weather.formatWeatherSummary(currentWeather))
//    }

    // === ASSISTANTS API SETUP (run once, then comment out) ===
    // Step 1: Upload movie file (copy the FILE ID from output)
//    val fileId = uploadMovieFile(debug = true)
//    // Step 2: Create vector store using the FILE ID from step 1 (copy the VECTOR STORE ID from output)
//    val vectorDbId = createMovieVectorStore(fileId, debug = true)
//    // Step 3: Create assistant using the VECTOR STORE ID from step 2 (copy the ASSISTANT ID from output)
//    val assistantId = createMovieAssistant(vectorDbId, debug = true)

    // === ASSISTANTS API USAGE (reuse with persisted IDs) ===
    // Step 4: Chat using the ASSISTANT ID from step 3
//    movieRecommendationChat("asst_tuqM1BDGX5jyRmi9szATXuck", "Any french films on the menu?", debug = false)
//    listAllAssistantResources()
//    deleteAllAssistantResources(true)
    // === CLEANUP (optional) ===
//    deleteAllAssistantResources(true)

//    huggingFaceDemo(debug = true)
//    huggingFaceObjectDetectionDemo()
}


fun close() {
    println("Closing stuff")
    apis.forEach { it.close() }
}
