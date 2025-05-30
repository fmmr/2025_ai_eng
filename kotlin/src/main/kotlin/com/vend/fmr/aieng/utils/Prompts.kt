package com.vend.fmr.aieng.utils

object Prompts {
    
    /**
     * System message for financial analysis using Polygon.io stock data
     */
    const val FINANCIAL_ANALYSIS = """You handle financial information. The input will be data in JSON fetched from the polygon.io api - more specifically https://api.polygon.io/v2/aggs/. The format will be in json. It may contain data for multiple companies/tickers. Your task is to create a short report based on these data to be shown to the end user. The inputs are known to the user, so no need to repeat this. If multiple companies are in the input focus on the main differences and what sets them apart. Try to use fewer than 100 words."""
    
    /**
     * System message for movie/content recommendation using RAG
     */
    const val MOVIE_EXPERT_RAG = """You are an enthusiastic movie expert who loves recommending movies to people. You will be given two pieces of information - some context about movies and a question. These are separated by #####. Your main job is to formulate an answer to the question using the provided context. If you can recommend 2 or 3 movies - this is always better than one. If you are unsure and cannot find the answer in the context, say, "Sorry, I don't know the answer." Please do not make up the answer."""
    
    /**
     * System message for general helpful assistant with rhyming responses
     */
    const val RHYMING_ASSISTANT = """You are a helpful assistant. Please answer the user's questions in rhymes. max length 100 tokens."""
    
    // Default user prompts for demo forms
    @Suppress("unused")
    object Defaults {
        const val RAG_QUERY = "I want to watch a movie with the actor from castaway."
        const val STOCK_TICKERS = "AAPL,MSFT,GOOGL"
        const val CHAT_COMPLETION_USER_PROMPT = "What is the capital of France?"
        const val CHAT_COMPLETION_SYSTEM_MESSAGE = RHYMING_ASSISTANT
        const val EMBEDDINGS_TEXT = "The quick brown fox jumps over the lazy dog."
        const val CHUNKING_TEXT = "Einstein's theory of relativity revolutionized our understanding of space, time, and gravity. The special theory of relativity, published in 1905, introduced the concept that space and time are interwoven into a single continuum called spacetime. It established that the speed of light in a vacuum is constant for all observers, regardless of their motion or the motion of the light source. This led to the famous equation E=mc², showing that mass and energy are interchangeable. The general theory of relativity, completed in 1915, extended these ideas to include gravity, describing it not as a force but as the curvature of spacetime caused by mass and energy. This theory has been confirmed by numerous experiments and observations, from the bending of light around massive objects to the detection of gravitational waves."
    }
    
    /**
     * Format a RAG query with context and question
     */
    fun formatRagQuery(context: String, question: String): String {
        return "Context: $context\n#####\nQuestion: $question"
    }
}