package org.jh.forum.client.util

/**
 * Application-wide constants
 */
object Constants {
    /**
     * Default avatar URL used when user avatar is empty or null
     */
    const val DEFAULT_AVATAR_URL = "https://blog.sugarmgp.cn/img/avatar.png"
    
    /**
     * Character limits for various inputs
     */
    object CharacterLimits {
        const val POST_TITLE = 30
        const val POST_CONTENT = 1000
        const val POST_TOPIC = 30
        const val POST_TOPIC_COUNT = 10
        const val COMMENT_CONTENT = 400
        const val USER_NICKNAME = 12
        const val USER_SIGNATURE = 20
        const val USER_PROFILE = 50
        const val USER_EMAIL = 40
    }
    
    /**
     * Debounce delays in milliseconds
     */
    object DebounceTimes {
        const val DEFAULT = 500L
        const val NAVIGATION = 300L
    }
}
