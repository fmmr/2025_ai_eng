package com.vend.fmr.aieng.utils

enum class DemoCategory(
    val displayName: String,
    val icon: String,
    val navbarGroup: NavbarGroup?
) {
    // Core AI & Data - appears in navbar dropdown
    CORE_AI("Core AI", "bi-cpu", NavbarGroup.CORE_DATA),
    DATA("Data", "bi-database", NavbarGroup.CORE_DATA),
    
    // Models & Vision - appears in navbar dropdown  
    OPEN_SOURCE("Open Source", "bi-hdd-stack", NavbarGroup.MODELS_VISION),
    VISION_IMAGE("Vision & Image", "bi-eye", NavbarGroup.MODELS_VISION),
    
    // Agents & Advanced - appears in navbar dropdown
    AI_AGENTS("AI Agents", "bi-robot", NavbarGroup.AGENTS_ADVANCED),
    MCP_PROTOCOL("MCP Protocol", "bi-diagram-2", NavbarGroup.AGENTS_ADVANCED),
    
    // Frameworks - appears in navbar dropdown
    FRAMEWORKS("Frameworks", "bi-stack", NavbarGroup.FRAMEWORKS_MORE),
    SOLO_PROJECTS("Solo Projects", "bi-person", NavbarGroup.FRAMEWORKS_MORE),
    
    // Home page only sections (not in navbar)
    FUTURE_COURSE("Future Course", "bi-hourglass", null),
    ADDITIONAL_EXPLORATIONS("Additional Explorations", "bi-plus-circle", null)
}