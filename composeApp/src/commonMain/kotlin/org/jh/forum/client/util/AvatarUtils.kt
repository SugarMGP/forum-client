package org.jh.forum.client.util

/**
 * Returns the avatar URL if it's not empty, otherwise returns the default avatar URL
 */
fun String?.getAvatarOrDefault(): String {
    return if (this.isNullOrEmpty()) {
        "https://blog.sugarmgp.cn/img/avatar.png"
    } else {
        this
    }
}
