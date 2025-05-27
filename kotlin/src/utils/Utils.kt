package utils

import java.io.File

fun getEnv(key: String): String {
   return (File("./kotlin/.env").readLines()
       .find { it.startsWith("$key=") }
       ?.substringAfter("=")
       ?: error("$key not found in .env file"))
}