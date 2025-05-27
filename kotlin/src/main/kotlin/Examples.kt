@file:Suppress("unused")

import chunker.Chunker
import supabase.Document
import supabase.DocumentMatch
import utils.read

suspend fun enrichedMovieChat(query: String, debug: Boolean = false): String {
    val fromDb = queryVectorDbForMovies(query, 5).joinToString("\n") { it.content }
    val system = "You are an enthusiastic movie expert who loves recommending movies to people. " +
        "You will be given two pieces of information - some context about movies and a question. These are separated by #####." +
        "Your main job is to formulate an answer to the question using the provided context. " +
        "If you can recommend 2 or 3 movies - this is always better than one." +
        "If you are unsure and cannot find the answer in the context, say, \"Sorry, I don't know the answer.\" " +
        "Please do not make up the answer."
    val chatGPT = openAI.createChatCompletion("Context: ${fromDb}\n#####\nQuestion: ${query}", system)
    val response = chatGPT.text()
    if (debug) {
        println("Found $fromDb")
        println(response)
    }
    return response
}

suspend fun clearDbAndInsertDocs(file: String, debug: Boolean = false): List<Document> {
    val embeddingData = embeddings(file)
    supabase.clearDocuments()
    val docs = supabase.insertDocumentsFromPair(embeddingData)
    if (debug) {
        println("Inserted ${docs.size} documents into Supabase.")
        docs.forEach { println(it) }
    }
    return docs
}


suspend fun embeddings(file: String, debug: Boolean = false): List<Pair<String, List<Double>>> {
    val texts = splitText(file)
    val embeddings = openAI.createEmbeddings(texts).mapIndexed { i, it ->
        texts[i] to it
    }
    if (debug) {
        embeddings.forEachIndexed { index, pair ->
            println("Text $index: ${pair.first} (${pair.second.take(10)}... ${pair.second.size} dimensions)")
        }
    }
    return embeddings
}

fun splitText(file: String, debug: Boolean = false): List<String> {
    val text = file.read()
    val splits = Chunker().split(text)
    if (debug) {
        splits.forEachIndexed { index, split ->
            println("Chunk $index: $split (${split.length} characters)")
        }
    }
    return splits
}


suspend fun chatGPT() {
    val chatGPT = openAI.createChatCompletion(
        "What is the capital of France?",
        "You are a helpful assistant. Please answer the user's questions in rhymes. max length 100 tokens."
    )
    print(chatGPT.text())
    print(chatGPT.usage())
}

suspend fun queryVectorDbForMovies(query: String, matches: Int = 5, debug: Boolean = false): List<DocumentMatch> {
    val response = supabase.matchDocuments(openAI.createEmbedding(query), matches)
    if (debug) {
        response.forEach { match ->
            println("ID: ${match.id}, Content: ${match.content}, Similarity: ${match.similarity}")
        }
    }
    return response
}
