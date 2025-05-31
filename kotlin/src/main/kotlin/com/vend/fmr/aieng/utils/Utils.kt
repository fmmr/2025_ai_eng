package com.vend.fmr.aieng.utils


private val envContent = object {}.javaClass.getResourceAsStream("/.env")
    ?.bufferedReader()
    ?.use { it.readText() }
    ?.split("\n")
    ?.associate { it.substringBefore("=") to it.substringAfter("=") }


fun String.env(): String = envContent?.get(this) ?: System.getenv(this) ?: error("Environment variable '$this' not found")

fun String.read(): String = object {}.javaClass.getResourceAsStream(this)
    ?.bufferedReader()
    ?.use { it.readText() }!!

/**
 * Helper extension for string repetition (for debug formatting)
 */
 operator fun String.times(count: Int): String = this.repeat(count)