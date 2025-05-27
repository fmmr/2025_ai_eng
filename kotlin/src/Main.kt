import openai.OpenAIClient

suspend fun main() {
    OpenAIClient().use { it.simple("Write a short poem about Kotlin programming") }
}
