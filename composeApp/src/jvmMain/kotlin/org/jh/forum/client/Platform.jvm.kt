package org.jh.forum.client

class JVMPlatform : Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
    override val deviceType: String = "desktop"
}

actual fun getPlatform(): Platform = JVMPlatform()

actual fun getInstallerSuffix(): String {
    val os = System.getProperty("os.name").lowercase()
    return when {
        "mac" in os || "darwin" in os -> ".dmg"
        "win" in os -> ".msi"
        else -> ".deb"
    }
}
