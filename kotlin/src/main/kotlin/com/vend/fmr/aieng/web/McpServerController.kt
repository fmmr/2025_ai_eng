package com.vend.fmr.aieng.web

import com.vend.fmr.aieng.utils.Demo
import com.vend.fmr.aieng.utils.AgentTool
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/demo/mcp-server")
class McpServerController : BaseController(Demo.MCP_SERVER) {

    @GetMapping
    fun mcpServerDemo(model: Model): String {
        model.addAttribute("availableTools", AgentTool.entries)
        return "mcp-server-demo"
    }
}