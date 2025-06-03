package com.vend.fmr.aieng.utils


private val envContent = object {}.javaClass.getResourceAsStream("/.env")
    ?.bufferedReader()
    ?.use { it.readText() }
    ?.split("\n")
    ?.associate { it.substringBefore("=") to it.substringAfter("=") }


fun String.env(): String = envContent?.get(this) ?: System.getenv(this) ?: "UNKNOWN_ENV_VAR"

fun String.read(): String = (if (this.startsWith("/")) this else "/$this").let { string ->
    object {}.javaClass.getResourceAsStream(string)
        ?.bufferedReader()
        ?.use { reader -> reader.readText() }!!
}

operator fun String.times(count: Int): String = this.repeat(count)

fun String.truncate(l: Int = 200): String {
    return if (length > l) {
        this.take(l - 1) + "…"
    } else {
        this
    }
}