import openai.OpenAI
import supabase.Supabase
import utils.env

val openAiKey = "OPENAI_API_KEY".env()
val supabaseUrl = "SUPABASE_URL".env()
val supabaseAnonKey = "SUPABASE_ANON_KEY".env()

const val OPEN_AI_MODEL = "gpt-4o"
const val EMBEDDING_MODEL = "text-embedding-ada-002"

val openAI = OpenAI(openAiKey)
val supabase = Supabase(supabaseUrl, supabaseAnonKey)

@Suppress("RedundantSuspendModifier")
suspend fun main() {
//    embeddings("/movies.txt", true)
//    clearDbAndInsertDocs(file = "/movies.txt")
//    splitText("/movies.txt", debug = true)
//    queryVectorDbForMovies("action packed thriller")
//    chatGPT()
//    enrichedMovieChat("The movie with that actor from Castaway", debug = true)
    close()
}



fun close() {
    openAI.close()
    supabase.close()
}
