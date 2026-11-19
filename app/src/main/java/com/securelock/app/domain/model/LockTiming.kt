package com.securelock.app.domain.model

enum class LockTiming(
    val id: String,
    val title: String,
    val description: String,
    val timeoutMillis: Long
) {
    IMMEDIATELY(
        id = "immediately",
        title = "Immediately",
        description = "Re-locks the application as soon as you exit or switch to another app.",
        timeoutMillis = 0L
    ),
    AFTER_ONE_MINUTE(
        id = "1_minute",
        title = "After 1 minute",
        description = "Allows switching between apps freely for 1 minute before re-locking.",
        timeoutMillis = 60_000L
    );

    companion object {
        fun fromId(id: String?): LockTiming {
            return entries.firstOrNull { it.id == id } ?: IMMEDIATELY
        }
    }
}
