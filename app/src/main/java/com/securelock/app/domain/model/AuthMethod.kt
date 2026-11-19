package com.securelock.app.domain.model

enum class AuthMethod(
    val id: String,
    val title: String,
    val subtitle: String
) {
    PIN(
        id = "PIN",
        title = "Numeric PIN",
        subtitle = "4 to 8 digits numeric passcode"
    ),
    PASSWORD(
        id = "PASSWORD",
        title = "Alphanumeric Password",
        subtitle = "Letters, numbers, and symbols (minimum 6 characters)"
    ),
    PATTERN(
        id = "PATTERN",
        title = "Pattern Lock",
        subtitle = "Connect at least 4 dots on a 3x3 grid"
    );

    companion object {
        fun fromId(id: String?): AuthMethod? {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) }
        }
    }
}
