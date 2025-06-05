package com.vend.fmr.aieng.web

import com.vend.fmr.aieng.utils.BuildInfo
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class HomeController(private val buildInfo: BuildInfo) {

    @GetMapping("/")
    fun home(model: Model): String {
        model.addAttribute("pageTitle", "Home")
        model.addAttribute("activeTab", "home")
        model.addAttribute("buildInfo", buildInfo)
        return "home"
    }



    @GetMapping("/demo/polygon")
    fun polygonDemo(model: Model): String {
        model.addAttribute("pageTitle", "Polygon Stock Data")
        model.addAttribute("activeTab", "polygon")
        return "polygon-demo"
    }




}