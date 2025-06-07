package com.vend.fmr.aieng.utils

@Suppress("CanBeParameter")
enum class DemoContentType(icon: String, val tooltip: String) {
    COURSE_CONTENT("bi-mortarboard", "AI Engineering Course Topic"),
    COURSE_CONTENT_SOLO("bi-mortarboard", "AI Engineering Course Solo Project"),
    PERSONAL_EXPLORATION("bi-compass", "Personal Exploration"),
    HACKDAY("bi-lightning-charge", "Hackday June 5th 2025"),
    EXTERNAL_JS("/images/javascript.svg", "External JavaScript Demo");

    val iconHtml: String = iconHtml(icon, tooltip)
}