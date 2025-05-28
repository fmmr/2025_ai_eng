package com.vend.fmr.aieng.controller

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/demo/chat")
class ChatController {

    @GetMapping
    fun chatDemo(model: Model): String {
        model.addAttribute("pageTitle", "Interactive Chat")
        model.addAttribute("activeTab", "chat")
        return "chat-demo"
    }
}