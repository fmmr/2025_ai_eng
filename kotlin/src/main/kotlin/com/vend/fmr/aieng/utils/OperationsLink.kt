package com.vend.fmr.aieng.utils

enum class DropdownGroup(val displayName: String) {
    BUILD("Build"),
    SERVICES("Services"), 
    DEPLOYMENT("Deployment"),
    APPS("Apps")
}

@Suppress("unused")
enum class OperationsLink(
    val id: String,
    val title: String,
    val url: String,
    val icon: String,
    val description: String,
    val dropdownGroup: DropdownGroup,
    val requiresLogin: Boolean = false
) {

    GITHUB_SOURCE(
        id = "github-source",
        title = "GitHub Project",
        url = "https://github.com/fmmr/2025_ai_eng/",
        icon = "/images/github.svg",
        description = "View project on GitHub",
        dropdownGroup = DropdownGroup.BUILD
    ),

    GITHUB_KOTLIN(
        id = "github-source",
        title = "GitHub Kotlin",
        url = "https://github.com/fmmr/2025_ai_eng/tree/main/kotlin",
        icon = "/images/kotlin.svg",
        description = "View kotlin code on GitHub",
        dropdownGroup = DropdownGroup.BUILD
    ),

    GITHUB_ACTIONS(
        id = "github-actions",
        title = "GitHub Actions",
        url = "https://github.com/fmmr/2025_ai_eng/actions",
        icon = "/images/github.svg",
        description = "CI/CD pipeline status",
        dropdownGroup = DropdownGroup.BUILD
    ),

    DOCKER_HUB(
        id = "docker-hub",
        title = "Docker Hub",
        url = "https://hub.docker.com/repository/docker/fmmr/aieng",
        icon = "/images/docker.svg",
        description = "Container registry",
        dropdownGroup = DropdownGroup.BUILD
    ),

    SUPABASE(
        id = "supabase",
        title = "Supabase Dashboard",
        url = "https://supabase.com/dashboard/project/qplquwknowszjlzqxmha/database/schemas",
        icon = "/images/supabase.svg",
        description = "Supabase DB dashboard",
        dropdownGroup = DropdownGroup.SERVICES,
        requiresLogin = true
    ),
    POLYGON(
        id = "polygon",
        title = "Polygon Dashboard",
        url = "https://polygon.io/dashboard",
        icon = "/images/polygon.svg",
        description = "Polygon dashboard",
        dropdownGroup = DropdownGroup.SERVICES,
        requiresLogin = true
    ),
    OPEN_AI_PLATFORM(
        id = "open_ai",
        title = "OpenAi Platform",
        url = "https://platform.openai.com/api-keys",
        icon = "/images/openai.svg",
        description = "OpenAi Platform",
        dropdownGroup = DropdownGroup.SERVICES,
        requiresLogin = true
    ),

    NORTHFLANK(
        id = "northflank",
        title = "Northflank Dashboard (Kotlin APP)",
        url = "https://app.northflank.com/t/fmrs-team/project/aieng/services/aieng",
        icon = "/images/northflank.svg",
        description = "Production deployment",
        dropdownGroup = DropdownGroup.DEPLOYMENT,
        requiresLogin = true
    ),

    RENDER_KOTLIN(
        id = "render-kotlin",
        title = "Render (Kotlin APP)",
        url = "https://dashboard.render.com/web/srv-d0sal4emcj7s73advd5g",
        icon = "/images/render.svg",
        description = "Backup deployment dashboard",
        dropdownGroup = DropdownGroup.DEPLOYMENT,
        requiresLogin = true
    ),

    CLOUDFLARE_DODGY_DAVE(
        id = "cloudflare-dodgy-dave",
        title = "Cloudflare (Dodgy Dave)",
        url = "https://dash.cloudflare.com/da5ff37dc3a56b099d74be433465c853/pages/view/2025-ai-eng",
        icon = "/images/cloudflare.svg",
        description = "Edge deployment dashboard",
        dropdownGroup = DropdownGroup.DEPLOYMENT,
        requiresLogin = true
    ),

    RENDER_POLYGLOT(
        id = "render-polyglot",
        title = "Render (Polyglot)",
        url = "https://dashboard.render.com/web/srv-d11dtore5dus738lm9mg",
        icon = "/images/render.svg",
        description = "Node.js app deployment dashboard",
        dropdownGroup = DropdownGroup.DEPLOYMENT,
        requiresLogin = true
    ),

    KOTLIN_MAIN(
        id = "kotlin-main",
        title = "Kotlin App (Main)",
        url = "https://ai.rodland.no/",
        icon = "/images/kotlin.svg",
        description = "Main deployment of Kotlin app",
        dropdownGroup = DropdownGroup.APPS
    ),

    KOTLIN_BACKUP(
        id = "kotlin-backup",
        title = "Kotlin App (Backup)",
        url = "https://fmr-ai-eng.onrender.com/",
        icon = "/images/kotlin.svg",
        description = "Backup deployment of Kotlin app",
        dropdownGroup = DropdownGroup.APPS
    ),

    DODGY_DAVE(
        id = "dodgy-dave",
        title = "Dodgy Dave",
        url = "https://aieng.rodland.no/",
        icon = "/images/javascript.svg",
        description = "Dodgy Dave Javascript app",
        dropdownGroup = DropdownGroup.APPS
    ),

    POLYGLOT(
        id = "polyglot",
        title = "Polyglot App",
        url = "https://polyglot-vbh0.onrender.com/",
        icon = "/images/javascript.svg",
        description = "Polyglot Javascript app",
        dropdownGroup = DropdownGroup.APPS
    );

    val iconHtml: String = iconHtml(icon, title)
    
    companion object {
        fun groupedEntries(): Map<String, List<OperationsLink>> {
            val groupedByEnum = entries.groupBy { it.dropdownGroup }
            return DropdownGroup.entries.associate { group ->
                group.displayName to (groupedByEnum[group] ?: emptyList())
            }.filterValues { it.isNotEmpty() }
        }
    }
}