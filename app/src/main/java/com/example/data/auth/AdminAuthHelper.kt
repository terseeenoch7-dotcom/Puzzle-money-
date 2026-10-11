package com.example.data.auth

/**
 * Utility helper to identify and authenticate administrators.
 * Ordinary users must NEVER see admin controls or settings.
 */
object AdminAuthHelper {

    // Privileged administrator emails explicitly crowned by the owner
    private val ADMIN_EMAILS = setOf(
        "chrstphrnch@gmail.com",
        "christopherenoch767@gmail.com",
        "christophertersee21@gmail.com",
        "emziyproverfiy@gmail.com"
    )

    fun isAdminEmail(email: String?): Boolean {
        if (email.isNullOrBlank()) return false
        val cleanEmail = email.trim().lowercase()
        return ADMIN_EMAILS.any { it.lowercase() == cleanEmail }
    }

    fun isUserAdmin(role: String?, email: String?): Boolean {
        if (isAdminEmail(email)) return true
        return role.equals("ADMIN", ignoreCase = true) || role.equals("SUPER_ADMIN", ignoreCase = true)
    }
}
