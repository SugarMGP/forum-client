package org.jh.forum.client

class WasmPlatform : Platform {
    override val name: String = "Web with Kotlin/Wasm"
    override val deviceType: String = "phone"
}

actual fun getPlatform(): Platform = WasmPlatform()