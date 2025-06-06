package com.vend.fmr.aieng.web

import com.vend.fmr.aieng.utils.BuildInfo
import com.vend.fmr.aieng.utils.Demo
import com.vend.fmr.aieng.utils.DemoCategory
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
        
        val categoryData = DemoCategory.entries.map { category ->
            CategoryData(
                displayName = category.displayName,
                icon = category.icon,
                demos = Demo.getByCategory(category)
            )
        }.filterNot { it.demos.isEmpty() }

        model.addAttribute("demoCategories", categoryData)
        return "home"
    }
}