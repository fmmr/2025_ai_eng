package com.vend.fmr.aieng.web

import com.vend.fmr.aieng.utils.Demo

data class CategoryData(
    val displayName: String,
    val iconHtml: String,
    val demos: List<Demo>
)