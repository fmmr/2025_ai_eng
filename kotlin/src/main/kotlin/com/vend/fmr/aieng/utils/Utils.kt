package com.vend.fmr.aieng.utils

import jakarta.servlet.http.HttpServletRequest

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

/**
 * Extract the real client IP address from an HTTP request
 * Handles proxy headers (X-Forwarded-For, X-Real-IP) and custom forwarding (X-Original-Client-IP)
 * Falls back to remoteAddr
 */
fun getClientIpAddress(request: HttpServletRequest): String {
    // Check for forwarded original client IP (from internal MCP client calls)
    val originalClientIp = request.getHeader("X-Original-Client-IP")
    if (!originalClientIp.isNullOrBlank()) {
        return originalClientIp
    }
    
    val xForwardedFor = request.getHeader("X-Forwarded-For")
    if (!xForwardedFor.isNullOrBlank()) {
        return xForwardedFor.split(",")[0].trim()
    }
    
    val xRealIp = request.getHeader("X-Real-IP")
    if (!xRealIp.isNullOrBlank()) {
        return xRealIp
    }
    
    return request.remoteAddr ?: "unknown"
}

fun iconHtml(icon: String, alt: String = ""): String = 
    if (icon.startsWith("/images/")) {
        """<img src="$icon" alt="$alt" width="16" height="16" class="me-1">"""
    } else {
        """<i class="$icon"></i>"""
    }