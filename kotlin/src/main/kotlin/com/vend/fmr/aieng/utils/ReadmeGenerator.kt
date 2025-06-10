package com.vend.fmr.aieng.utils

object ReadmeGenerator {
    
    fun generateInternalDemos(): String {
        val sb = StringBuilder()
        
        // Group demos by category, same as home page
        DemoCategory.entries.forEach { category ->
            val demos = Demo.getByCategory(category).filter { !it.external() }
            if (demos.isNotEmpty()) {
                val categoryEmoji = category.emoji
                sb.appendLine("#### $categoryEmoji ${category.displayName}")
                sb.appendLine()
                
                demos.forEach { demo ->
                    val emoji = demo.emoji
                    val statusText = if (demo.status == DemoStatus.COMPLETED) null else demo.status.displayText
                    val projectType = getProjectTypeText(demo, statusText)
                    
                    // Format based on completion status
                    val titleFormat = if (demo.route != null) {
                        // Completed demo - bold with link to ai.rodland.no
                        "**[$emoji ${demo.title}](https://ai.rodland.no${demo.route})**"
                    } else {
                        // Future demo - plain text, no bold
                        "$emoji ${demo.title}"
                    }
                    
                    sb.appendLine("- $titleFormat - ${demo.shortDescription}$projectType")
                }
                sb.appendLine()
            }
        }
        
        return sb.toString()
    }
    
    fun generateExternalDemos(): String {
        val sb = StringBuilder()
        
        // Get only external demos
        val externalDemos = Demo.entries.filter { it.external() }
        
        externalDemos.forEach { demo ->
            val emoji = demo.emoji
            val projectType = getProjectTypeText(demo)
            val titleFormat = "**[$emoji ${demo.title}](${demo.route})**"
            sb.appendLine("- $titleFormat - ${demo.shortDescription}$projectType")
        }
        
        return sb.toString()
    }
    
    fun generateOperationsLinks(): String {
        val sb = StringBuilder()
        
        OperationsLink.entries.forEach { link ->
            val lockIcon = if (link.requiresLogin) " 🔒" else ""
            sb.appendLine("- **[${link.title}](${link.url})**$lockIcon - ${link.description}")
        }
        
        return sb.toString()
    }
    
    fun generateToolsList(): String {
        val sb = StringBuilder()
        
        Tools.entries.forEach { tool ->
            sb.appendLine("- **${tool.functionName}** - ${tool.readmeDescription}")
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

fun main(args: Array<String>) {
    when (args.getOrNull(0)) {
        "external" -> println(ReadmeGenerator.generateExternalDemos())
        "operations" -> println(ReadmeGenerator.generateOperationsLinks())
        "tools" -> println(ReadmeGenerator.generateToolsList())
        else -> println(ReadmeGenerator.generateInternalDemos()) // default - internal demos
    }
}