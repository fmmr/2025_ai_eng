package com.vend.fmr.aieng.controller

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class HomeController {

    @GetMapping("/")
    fun home(model: Model): String {
        model.addAttribute("pageTitle", "Home")
        model.addAttribute("activeTab", "home")
        return "home"
    }


    @GetMapping("/demo/supabase")
    fun supabaseDemo(model: Model): String {
        model.addAttribute("pageTitle", "Supabase Vector Database")
        model.addAttribute("activeTab", "supabase")
        return "supabase-demo"
    }

    @GetMapping("/demo/polygon")
    fun polygonDemo(model: Model): String {
        model.addAttribute("pageTitle", "Polygon Stock Data")
        model.addAttribute("activeTab", "polygon")
        return "polygon-demo"
    }


    @GetMapping("/demo/rag")
    fun ragDemo(model: Model): String {
        model.addAttribute("pageTitle", "RAG Implementation")
        model.addAttribute("activeTab", "rag")
        return "rag-demo"
    }


}