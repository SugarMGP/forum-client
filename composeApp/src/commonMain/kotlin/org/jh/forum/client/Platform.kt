package org.jh.forum.client

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform