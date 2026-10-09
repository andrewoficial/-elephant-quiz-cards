package com.elephantquizcards

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform