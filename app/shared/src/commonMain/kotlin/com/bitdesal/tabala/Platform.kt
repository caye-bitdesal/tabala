package com.bitdesal.tabala

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform