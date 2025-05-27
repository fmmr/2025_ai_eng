import openai.OpenAI
import supabase.Supabase
import utils.getEnv

val openAiKey = getEnv("OPENAI_API_KEY")
val supabaseUrl = getEnv("SUPABASE_URL")
val supabaseAnonKey = getEnv("SUPABASE_ANON_KEY")

suspend fun main() {

    val query = "I want to watch movie set in a rural environment."

    val response = OpenAI(openAiKey).use { openAI ->
        Supabase(supabaseUrl, supabaseAnonKey).use { client ->
            client.matchDocuments(openAI.createEmbedding(query))
        }
    }
    response.forEach { match ->
        println("ID: ${match.id}, Content: ${match.content}, Similarity: ${match.similarity}")
    }

}
