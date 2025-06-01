@file:Suppress("UnusedImport")

package com.vend.fmr.aieng

import com.vend.fmr.aieng.impl.openai.OpenAI
import com.vend.fmr.aieng.impl.polygon.Polygon
import com.vend.fmr.aieng.impl.supabase.Supabase
import com.vend.fmr.aieng.utils.env
import com.vend.fmr.aieng.utils.FunctionCallingExamples
import com.vend.fmr.aieng.utils.promptEngineeringDemo
import com.vend.fmr.aieng.utils.chatParametersDemo
import com.vend.fmr.aieng.utils.temperatureDemo

val OPEN_AI_KEY = "OPENAI_API_KEY".env()
val SUPABASE_URL = "SUPABASE_URL".env()
val SUPABASE_KEY = "SUPABASE_ANON_KEY".env()
val POLYGON_API_KEY = "POLYGON_API_KEY".env()

const val OPEN_AI_MODEL = "gpt-4"
const val EMBEDDING_MODEL = "text-embedding-ada-002"

val openAI = OpenAI(OPEN_AI_KEY)
val supabase = Supabase(SUPABASE_URL, SUPABASE_KEY)
val polygon = Polygon(POLYGON_API_KEY)

@Suppress("RedundantSuspendModifier", "RedundantSuppression")
suspend fun main() {
    Runtime.getRuntime().addShutdownHook(Thread { close() })

//    chatParametersDemo(debug = true)

//    promptEngineeringDemo(debug = true)

//    val conversation = multiMessageChat(debug = true)
//    println(conversation)
    // ReAct Agent Examples (manual step-by-step reasoning)
//    println("\n" + "=".repeat(60))
//    println("REACT AGENT EXAMPLES")
//    println("=".repeat(60))
//
//    val result1 = reactAgent("Do you have any ideas for activities I can do at my location?", debug = true)
//    println("Final ReAct Result: $result1")
//
//    val result2 = reactAgent("Can you tell me the current stock price of Apple and Microsoft, and what time it is?", debug = true)
//    println("Final ReAct Result: $result2")
    
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
}


fun close() {
    println("Closing stuff")
    openAI.close()
    supabase.close()
    polygon.close()
}
