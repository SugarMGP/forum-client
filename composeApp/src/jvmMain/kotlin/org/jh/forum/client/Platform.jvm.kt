package org.jh.forum.client

class JVMPlatform : Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
    override val deviceType: String = "desktop"
}

actual fun getPlatform(): Platform = JVMPlatform()