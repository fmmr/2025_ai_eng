package com.vend.fmr.aieng.web

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class McpServerController {

    @GetMapping("/demo/mcp-server")
    fun mcpServerDemo(model: Model): String {
        model.addAttribute("pageTitle", "MCP Server Demo")
        model.addAttribute("activeTab", "mcp-server")
        return "mcp-server-demo"
    }
}