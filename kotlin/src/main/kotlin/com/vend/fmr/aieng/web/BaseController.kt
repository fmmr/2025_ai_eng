package com.vend.fmr.aieng.web

import com.vend.fmr.aieng.utils.Demo
import org.springframework.web.bind.annotation.ModelAttribute

abstract class BaseController(protected val demo: Demo) {

    @ModelAttribute("demo")
    fun demo(): Demo = demo
}