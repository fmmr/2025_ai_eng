package com.vend.fmr.aieng.utils

@Suppress("unused")
enum class DemoStatus(val displayText: String, val badgeClass: String, val buttonClass: String, val icon: String) {
    COMPLETED("✓ Done", "bg-success", "btn-success", "bi-check-circle"),
    IN_PROGRESS("WIP", "bg-warning text-dark", "btn-warning", "bi-clock"),
    PLANNED("Planned", "bg-primary", "btn-primary", "bi-hourglass"),
    MAYBE("Maybe", "bg-warning text-dark", "btn-warning", "bi-question-circle"),
    SOMEDAY("Someday", "bg-info", "btn-info", "bi-cloud-drizzle"),
    DREAMS("Dreams", "bg-dark", "btn-dark", "bi-cloud"),
    COFFEE("After Coffee", "bg-primary", "btn-primary", "bi-cup-hot"),
    TIME("When I Have Time", "bg-danger", "btn-danger", "bi-hourglass"),
    WILD_IDEAS("Wild Ideas", "bg-secondary", "btn-secondary", "bi-star"),
    PROCRASTINATING("Procrastinating", "bg-warning text-dark", "btn-warning", "bi-emoji-dizzy"),
    NEXT_YEAR("Maybe Next Year", "bg-warning text-dark", "btn-warning", "bi-calendar")
}