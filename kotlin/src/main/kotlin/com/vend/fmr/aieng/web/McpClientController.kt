package com.vend.fmr.aieng.web

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseBody

@Controller
class McpClientController {

    @GetMapping("/demo/mcp-client")
    fun mcpClientDemo(model: Model): String {
        model.addAttribute("pageTitle", "MCP Client Demo")
        model.addAttribute("activeTab", "mcp-client")
        return "mcp-client-demo"
    }

    @PostMapping("/demo/mcp-client/connect")
    @ResponseBody
    fun connectToMcpServer(@RequestBody request: Map<String, String>): Map<String, Any> {
        val serverUrl = request["serverUrl"] ?: "https://ai.rodland.no/mcp/"
        
        // For now, return a mock response
        // We'll implement the actual MCP client logic step by step
        return mapOf(
            "status" to "success",
            "message" to "Connected to MCP server at $serverUrl",
            "serverInfo" to mapOf(
                "name" to "Kotlin AI Engineering MCP Server",
                "version" to "1.0.0"
            )
        )
    }
}