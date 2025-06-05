package com.vend.fmr.aieng.mcp

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class McpClientController {

    @GetMapping("/demo/mcp-protocol")
    fun mcpProtocolDemo(model: Model): String {
        model.addAttribute("pageTitle", "MCP Protocol Demo")
        model.addAttribute("activeTab", "mcp-protocol")
        return "mcp-client-demo"
    }

}