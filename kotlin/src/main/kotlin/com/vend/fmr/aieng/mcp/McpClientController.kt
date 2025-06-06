package com.vend.fmr.aieng.mcp

import com.vend.fmr.aieng.utils.Demo
import com.vend.fmr.aieng.web.BaseController
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class McpClientController : BaseController(Demo.MCP_PROTOCOL_DEMO) {

    @GetMapping("/demo/mcp-protocol")
    fun mcpProtocolDemo(model: Model): String {
        model.addAttribute("pageTitle", "MCP Protocol Demo")
        model.addAttribute("activeTab", "mcp-protocol")
        return "mcp-client-demo"
    }

}