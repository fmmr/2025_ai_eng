package com.vend.fmr.aieng

import com.vend.fmr.aieng.apis.chunker.Chunker
import com.vend.fmr.aieng.apis.geolocation.Geolocation
import com.vend.fmr.aieng.apis.huggingface.HuggingFace
import com.vend.fmr.aieng.apis.nasa.Nasa
import com.vend.fmr.aieng.apis.news.News
import com.vend.fmr.aieng.apis.openai.OpenAI
import com.vend.fmr.aieng.apis.openai.OpenAIAssistant
import com.vend.fmr.aieng.apis.polygon.Polygon
import com.vend.fmr.aieng.apis.supabase.Supabase
import com.vend.fmr.aieng.apis.weather.Weather
import com.vend.fmr.aieng.utils.createHttpClient
import com.vend.fmr.aieng.utils.createJson
import java.io.Closeable

@Suppress("RedundantSuspendModifier", "RedundantSuppression", "UnusedVariable", "unused")
suspend fun main() {
    val json = createJson()
    val httpClient = createHttpClient(json)
    val openAI = OpenAI(httpClient, json)
    val assistant = OpenAIAssistant(httpClient, json)
    val supabase = Supabase(httpClient, json)
    val polygon = Polygon(httpClient, json)
    val weather = Weather(httpClient, json)
    val geolocation = Geolocation(httpClient, json)
    val nasa = Nasa(httpClient, json)
    val news = News(httpClient, json)
    val chunker = Chunker()
    val huggingface = HuggingFace(httpClient, json)
    Runtime.getRuntime().addShutdownHook(Thread { close(listOf(openAI, assistant, supabase, polygon, weather, geolocation, huggingface)) })

//    chatParametersDemo(openAI, debug = true)

//    promptEngineeringDemo(openAI, debug = true)

    // Uncomment to generate README content
    // println(ReadmeGenerator.generateInternalDemos())

//    val conversation = multiMessageChat(openAI, debug = true)
//    println(conversation)

//    val result3 = FunctionCallingExamples.functionCallingAgent(openAI, "Do you have any ideas for activities I can do at my location?", debug = true)
//    println("Final Function Call Result: $result3")
//
//    val result4 = FunctionCallingExamples.functionCallingAgent(openAI, "Can you tell me the current stock price of Apple and Microsoft, and what time it is?", debug = true)
//    println("Final Function Call Result: $result4")

//    tickers(openAI, polygon, "APPL", "NHYDY")
//    embeddings(openAI, chunker, "/movies.txt", debug = true)
//    clearDbAndInsertDocs(openAI, supabase, chunker, "/movies.txt")
//    splitText(chunker, "/movies.txt", debug = true)
//    queryVectorDbForMovies(openAI, supabase, "action packed thriller")
//    chatGPT(openAI)
//    enrichedMovieChat(openAI, supabase, "The movie with that actor from Castaway", debug = true)

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
//    val fileId = uploadMovieFile(assistant, debug = true)
//    // Step 2: Create vector store using the FILE ID from step 1 (copy the VECTOR STORE ID from output)
//    val vectorDbId = createMovieVectorStore(assistant, fileId, debug = true)
//    // Step 3: Create assistant using the VECTOR STORE ID from step 2 (copy the ASSISTANT ID from output)
//    val assistantId = createMovieAssistant(assistant, vectorDbId, debug = true)

    // === ASSISTANTS API USAGE (reuse with persisted IDs) ===
    // Step 4: Chat using the ASSISTANT ID from step 3
//    movieRecommendationChat(assistant, "asst_tuqM1BDGX5jyRmi9szATXuck", "Any french films on the menu?", debug = false)
//    listAllAssistantResources(assistant)
//    deleteAllAssistantResources(assistant, true)
    // === CLEANUP (optional) ===
//    deleteAllAssistantResources(assistant, true)

//    huggingFaceDemo(huggingface, openAI, debug = true)
//    huggingFaceObjectDetectionDemo(huggingface)

    // === NEWS API TEST ===
    val newsUSResult = news.getHeadlinesSummary("us", debug = true)
    println("News US Test:")
    println(newsUSResult)
    println()
    
    val newsNorwayResult = news.getHeadlinesSummary("no", debug = true)
    println("News Norway Test:")
    println(newsNorwayResult)
    println()
    
    val newsFranceResult = news.getHeadlinesSummary("fr", debug = true)
    println("News France Test:")
    println(newsFranceResult)
    println()

    // === NASA API TEST ===
    val apodResult = nasa.getApodSummary(debug = true)
    println("NASA APOD Test:")
    println(apodResult)
    println()
    
    val neoResult = nasa.getNearEarthObjectsSummary(debug = true)
    println("NASA NEO Test:")
    println(neoResult)
}


fun close(apis: List<Closeable>) {
    println("Closing stuff")
    apis.forEach { it.close() }
}
