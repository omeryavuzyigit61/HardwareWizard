package com.panda.myapplication

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform