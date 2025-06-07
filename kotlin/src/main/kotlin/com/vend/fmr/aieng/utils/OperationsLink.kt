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
        icon = "bi-github",
        description = "View project on GitHub"
    ),

    GITHUB_KOTLIN(
        id = "github-source",
        title = "GitHub Kotlin",
        url = "https://github.com/fmmr/2025_ai_eng/tree/main/kotlin",
        icon = "bi-github",
        description = "View kotlin code on GitHub"
    ),

    GITHUB_ACTIONS(
        id = "github-actions",
        title = "GitHub Actions",
        url = "https://github.com/fmmr/2025_ai_eng/actions",
        icon = "bi-gear-wide-connected",
        description = "CI/CD pipeline status"
    ),

    DOCKER_HUB(
        id = "docker-hub",
        title = "Docker Hub",
        url = "https://hub.docker.com/repository/docker/fmmr/aieng",
        icon = "bi-box",
        description = "Container registry"
    ),

    NORTHFLANK(
        id = "northflank",
        title = "Northflank",
        url = "https://app.northflank.com/t/fmrs-team/project/aieng/services/aieng",
        icon = "bi-cloud",
        description = "Production deployment",
        requiresLogin = true
    ),

    CLOUDFLARE_DODGY_DAVE(
        id = "cloudflare-dodgy-dave",
        title = "Cloudflare (dodgy dave)",
        url = "https://dash.cloudflare.com/da5ff37dc3a56b099d74be433465c853/pages/view/2025-ai-eng",
        icon = "bi-lightning-charge",
        description = "Edge deployment dashboard",
        requiresLogin = true
    ),

    RENDER_KOTLIN(
        id = "render-kotlin",
        title = "Render (kotlin)",
        url = "https://dashboard.render.com/web/srv-d0sal4emcj7s73advd5g",
        icon = "bi-server",
        description = "Backup deployment dashboard",
        requiresLogin = true
    ),

    RENDER_POLYGLOT(
        id = "render-polyglot",
        title = "Render (polyglot)",
        url = "https://dashboard.render.com/web/srv-d11dtore5dus738lm9mg",
        icon = "bi-chat-dots",
        description = "Node.js app deployment dashboard",
        requiresLogin = true
    )
}