package com.vend.fmr.aieng.utils

enum class DemoStatus(
    val displayText: String,
    val badgeClass: String,
    val icon: String
) {
    COMPLETED("✓ Done", "bg-success", "bi-check-circle"),
    IN_PROGRESS("WIP", "bg-warning text-dark", "bi-clock"),
    PLANNED("Planned", "bg-primary", "bi-hourglass"),
    MAYBE("Maybe", "bg-warning text-dark", "bi-question-circle"),
    SOMEDAY("Someday", "bg-secondary", "bi-cloud-drizzle"),
    DREAMS("Dreams", "bg-secondary", "bi-cloud"),
    COFFEE("After Coffee", "bg-primary", "bi-cup-hot"),
    TIME("When I Have Time", "bg-secondary", "bi-hourglass"),
    WILD_IDEAS("Wild Ideas", "bg-secondary", "bi-star"),
    PROCRASTINATING("Procrastinating", "bg-warning text-dark", "bi-emoji-dizzy"),
    NEXT_YEAR("Maybe Next Year", "bg-warning text-dark", "bi-calendar")
}