package com.vend.fmr.aieng.utils

@Suppress("unused")
enum class OperationsLink(
    val id: String,
    val title: String,
    val url: String,
    val icon: String,
    val description: String,
    val requiresLogin: Boolean = false
) {

    GITHUB_SOURCE(
        id = "github-source",
        title = "GitHub Project",
        url = "https://github.com/fmmr/2025_ai_eng/",
        icon = "/images/github.svg",
        description = "View project on GitHub"
    ),

    GITHUB_KOTLIN(
        id = "github-source",
        title = "GitHub Kotlin",
        url = "https://github.com/fmmr/2025_ai_eng/tree/main/kotlin",
        icon = "/images/kotlin.svg",
        description = "View kotlin code on GitHub"
    ),

    GITHUB_ACTIONS(
        id = "github-actions",
        title = "GitHub Actions",
        url = "https://github.com/fmmr/2025_ai_eng/actions",
        icon = "/images/github.svg",
        description = "CI/CD pipeline status"
    ),

    DOCKER_HUB(
        id = "docker-hub",
        title = "Docker Hub",
        url = "https://hub.docker.com/repository/docker/fmmr/aieng",
        icon = "/images/docker.svg",
        description = "Container registry"
    ),

    SUPABASE(
        id = "supabase",
        title = "Supabase Dashboard",
        url = "https://supabase.com/dashboard/project/qplquwknowszjlzqxmha/database/schemas",
        icon = "/images/supabase.svg",
        description = "Supabase DB dashboard"
    ),

    NORTHFLANK(
        id = "northflank",
        title = "Kotlin APP (northflank)",
        url = "https://app.northflank.com/t/fmrs-team/project/aieng/services/aieng",
        icon = "bi-cloud",
        description = "Production deployment",
        requiresLogin = true
    ),

    RENDER_KOTLIN(
        id = "render-kotlin",
        title = "Kotlin APP (render)",
        url = "https://dashboard.render.com/web/srv-d0sal4emcj7s73advd5g",
        icon = "/images/render.svg",
        description = "Backup deployment dashboard",
        requiresLogin = true
    ),

    CLOUDFLARE_DODGY_DAVE(
        id = "cloudflare-dodgy-dave",
        title = "Dodgy Dave: (cloudflare)",
        url = "https://dash.cloudflare.com/da5ff37dc3a56b099d74be433465c853/pages/view/2025-ai-eng",
        icon = "/images/cloudflare.svg",
        description = "Edge deployment dashboard",
        requiresLogin = true
    ),

    RENDER_POLYGLOT(
        id = "render-polyglot",
        title = "Polyglot (render)",
        url = "https://dashboard.render.com/web/srv-d11dtore5dus738lm9mg",
        icon = "/images/render.svg",
        description = "Node.js app deployment dashboard",
        requiresLogin = true
    );

    val iconHtml: String = iconHtml(icon, title)
}