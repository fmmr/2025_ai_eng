package com.vend.fmr.aieng.web

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



    @GetMapping("/demo/polygon")
    fun polygonDemo(model: Model): String {
        model.addAttribute("pageTitle", "Polygon Stock Data")
        model.addAttribute("activeTab", "polygon")
        return "polygon-demo"
    }




}