package com.vend.fmr.aieng.utils

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Suppress("unused")
@Component
class BuildInfo(
    @Value("\${app.build.timestamp:Unknown}") val buildTimestamp: String,
    @Value("\${app.git.commit:Unknown}") val gitCommit: String,
    @Value("\${app.git.branch:Unknown}") val gitBranch: String,
    @Value("\${app.git.commit.time:Unknown}") val gitCommitTime: String
) {
    fun getVersionInfo(): String {
        return "$gitCommit on $gitBranch"
    }
    
    fun getFullBuildInfo(): String {
        return "Built: $buildTimestamp | Git: $gitCommit ($gitBranch)"
    }
}