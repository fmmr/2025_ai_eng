package com.vend.fmr.aieng.web

import com.vend.fmr.aieng.utils.Demo

data class NavbarData(
    val displayName: String,
    val iconHtml: String,
    val dropdownId: String,
    val demos: List<Demo>
)