package com.vend.fmr.aieng.utils

const val DUMMY_API_KEY = "dummy-key-for-auto-config"

fun String.env(): String = System.getenv(this) ?: "UNKNOWN_ENV_VAR"

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

fun String.isValidApiKey(): Boolean = this.isNotBlank() && this != DUMMY_API_KEY