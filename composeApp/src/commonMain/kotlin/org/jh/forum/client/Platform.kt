package org.jh.forum.client

interface Platform {
    val name: String
    val deviceType: String
}

expect fun getPlatform(): Platform

expect fun getInstallerSuffix(): String