package com.vend.fmr.aieng.utils


private val envContent = object {}.javaClass.getResourceAsStream("/.env")
    ?.bufferedReader()
    ?.use { it.readText() }
    ?.split("\n")
    ?.associate { it.substringBefore("=") to it.substringAfter("=") }
    ?: error(".env file not found in classpath")

fun String.env(): String = envContent[this] ?: System.getenv(this) ?: error("Environment variable '$this' not found")

fun String.read(): String = object {}.javaClass.getResourceAsStream(this)
    ?.bufferedReader()
    ?.use { it.readText() }!!
