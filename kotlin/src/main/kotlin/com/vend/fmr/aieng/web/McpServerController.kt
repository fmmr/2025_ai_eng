package com.vend.fmr.aieng.web

import com.vend.fmr.aieng.utils.AgentTool
import com.vend.fmr.aieng.utils.Demo
import jakarta.servlet.http.HttpSession
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/demo/mcp-server")
class McpServerController : BaseController(Demo.MCP_SERVER) {
    override fun addDefaultModel(model: Model, session: HttpSession) {
        model.addAttribute("availableTools", AgentTool.entries)
    }
}