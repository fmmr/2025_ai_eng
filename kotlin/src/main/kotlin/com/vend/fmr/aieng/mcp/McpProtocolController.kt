package com.vend.fmr.aieng.mcp

import com.vend.fmr.aieng.utils.AgentTool
import com.vend.fmr.aieng.utils.Demo
import com.vend.fmr.aieng.web.BaseController
import jakarta.servlet.http.HttpSession
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/demo/mcp-protocol")
class McpProtocolController : BaseController(Demo.MCP_PROTOCOL_DEMO) {

    override fun addDefaultModel(model: Model, session: HttpSession) {
        model.addAttribute("pageTitle", "MCP Protocol Demo")
        model.addAttribute("activeTab", "mcp-protocol")

        // Provide testParams from Tools enum for default values
        val toolDefaults = AgentTool.entries.associate { it.functionName to it.testParams }
        model.addAttribute("toolDefaults", toolDefaults)
    }
}