package com.vend.fmr.aieng.utils

enum class NavbarGroup(val displayName: String, icon: String, val dropdownId: String) {
    CORE_DATA("Core & Data", "bi-cpu-fill", "coreDropdown"),
    MODELS_VISION("Models & Vision", "/images/huggingface.svg", "modelsDropdown"),
    AGENTS_ADVANCED("Agents & Advanced", "bi-robot", "agentsDropdown"),
    FRAMEWORKS("Frameworks", "/images/spring.svg", "frameworksDropdown");

    val iconHtml: String = iconHtml(icon, displayName)
}