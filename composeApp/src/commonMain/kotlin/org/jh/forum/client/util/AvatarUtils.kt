package org.jh.forum.client.util

/**
 * Returns the avatar URL if it's not empty, otherwise returns the default avatar URL
 */
fun String?.getAvatarOrDefault(): String {
    return if (this.isNullOrEmpty()) {
        Constants.DEFAULT_AVATAR_URL
    } else {
        this
    }
}
