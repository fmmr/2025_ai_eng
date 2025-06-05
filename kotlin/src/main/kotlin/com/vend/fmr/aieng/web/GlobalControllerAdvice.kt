package com.vend.fmr.aieng.web

import com.vend.fmr.aieng.utils.Demo
import com.vend.fmr.aieng.utils.NavbarGroup
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ModelAttribute

@ControllerAdvice
class GlobalControllerAdvice {
    
    @ModelAttribute
    fun addNavbarData(model: Model) {
        val navbarData = NavbarGroup.entries.map { group ->
            NavbarData(
                displayName = group.displayName,
                icon = group.icon,
                dropdownId = group.dropdownId,
                demos = Demo.getByNavbarGroup(group)
            )
        }
        model.addAttribute("navbarGroups", navbarData)
    }
}