package com.vend.fmr.aieng.utils

enum class DemoCategory(val displayName: String, val icon: String, val emoji: String, val navbarGroup: NavbarGroup?) {
    CORE_AI("Core AI", "bi-cpu", "🧠", NavbarGroup.CORE_DATA),
    DATA("Data", "bi-database", "🗄️", NavbarGroup.CORE_DATA),
    
    OPEN_SOURCE("Open Source", "bi-hdd-stack", "🔹", NavbarGroup.MODELS_VISION),
    VISION_IMAGE("Vision & Image", "bi-eye", "👁️", NavbarGroup.MODELS_VISION),
    
    AI_AGENTS("AI Agents", "bi-robot", "🎯", NavbarGroup.AGENTS_ADVANCED),
    MCP_PROTOCOL("MCP Protocol", "bi-diagram-2", "🔗", NavbarGroup.AGENTS_ADVANCED),
    
    FRAMEWORKS("Frameworks", "bi-stack", "🔹", NavbarGroup.FRAMEWORKS),
    SOLO_PROJECTS("Solo Projects", "bi-person", "👤", NavbarGroup.FRAMEWORKS),
}