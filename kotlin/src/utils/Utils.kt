package utils

import java.io.File

fun getEnv(key: String): String {
    var file = File(".env")
    if (!file.exists()) {
        file = File("kotlin/.env")
        if (!file.exists()) {
            error(".env file not found at .env")
        }
    }
    return file.readLines()
        .find { it.startsWith("$key=") }
        ?.substringAfter("=")
        ?: error("$key not found in .env file")
}