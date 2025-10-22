package org.jh.forum.client.util

import kotlinx.datetime.*
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime

class TimeUtils {
    companion object {
        @OptIn(ExperimentalTime::class)
        fun formatTime(timestamp: String): String {
            return try {
                val zone = TimeZone.currentSystemDefault()

                // 当前时间和消息时间都用 LocalDateTime
                val messageTime = LocalDateTime.parse(timestamp)
                val now = Clock.System.now().toLocalDateTime(zone)
                val diff = now.toInstant(zone) - messageTime.toInstant(zone)

                when {
                    diff < 1.minutes -> "刚刚"
                    diff < 60.minutes -> "${diff.inWholeMinutes} 分钟前"
                    diff < 24.hours -> "${diff.inWholeHours} 小时前"
                    diff < 2.days -> "昨天"
                    else -> {
                        "${messageTime.year}-${
                            messageTime.month.number.toString().padStart(2, '0')
                        }-${messageTime.day.toString().padStart(2, '0')}"
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                timestamp
            }
        }
    }
}