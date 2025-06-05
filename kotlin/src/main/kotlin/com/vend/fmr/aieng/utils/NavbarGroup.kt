package com.vend.fmr.aieng.utils

enum class NavbarGroup(
    val displayName: String,
    val icon: String,
    val dropdownId: String
) {
    CORE_DATA("Core & Data", "bi-cpu-fill", "coreDropdown"),
    MODELS_VISION("Models & Vision", "bi-eye-fill", "modelsDropdown"),
    AGENTS_ADVANCED("Agents & Advanced", "bi-robot", "agentsDropdown"),
    FRAMEWORKS_MORE("Frameworks & More", "bi-stack", "frameworksDropdown");
}