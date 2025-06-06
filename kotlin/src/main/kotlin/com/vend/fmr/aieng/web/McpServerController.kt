package com.vend.fmr.aieng.web

import com.vend.fmr.aieng.utils.Demo
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class McpServerController : BaseController(Demo.MCP_SERVER) {

    @GetMapping("/demo/mcp-server")
    fun mcpServerDemo(model: Model): String {
        model.addAttribute("pageTitle", "MCP Server Demo")
        model.addAttribute("activeTab", "mcp-server")
        return "mcp-server-demo"
    }
}