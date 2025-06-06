package com.vend.fmr.aieng.utils

object ReadmeGenerator {
    
    fun generateDemoSection(): String {
        val sb = StringBuilder()
        
        // Group demos by category, same as home page
        DemoCategory.entries.forEach { category ->
            val demos = Demo.getByCategory(category)
            if (demos.isNotEmpty()) {
                val categoryEmoji = category.emoji
                sb.appendLine("#### $categoryEmoji ${category.displayName}")
                sb.appendLine()
                
                demos.forEach { demo ->
                    val emoji = demo.emoji
                    val statusText = if (demo.status == DemoStatus.COMPLETED) null else demo.status.displayText
                    val projectType = getProjectTypeText(demo, statusText)
                    sb.appendLine("- **$emoji ${demo.title}** - ${demo.shortDescription}$projectType")
                }
                sb.appendLine()
            }
        }
        
        return sb.toString()
    }
    
    private fun getProjectTypeText(demo: Demo, statusText: String? = null): String {
        return when {
            demo.soloProject -> " (Solo Project${statusText?.let { " - $it" } ?: ""})"
            demo.contentType == DemoContentType.HACKDAY -> " (Hackday${statusText?.let { " - $it" } ?: ""})"
            demo.contentType == DemoContentType.PERSONAL_EXPLORATION -> " (Personal Exploration${statusText?.let { " - $it" } ?: ""})"
            statusText != null -> " ($statusText)"
            else -> ""
        }
    }
}

fun main() {
    println(ReadmeGenerator.generateDemoSection())
}