package com.vend.fmr.aieng.utils

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class BuildInfo(@Value("\${app.build.timestamp:Unknown}") val buildTimestamp: String)