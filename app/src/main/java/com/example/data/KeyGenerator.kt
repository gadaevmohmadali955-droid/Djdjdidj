package com.example.data

import java.security.SecureRandom

object KeyGenerator {
    private val CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray()
    private val random = SecureRandom()

    fun generateUniqueKey(): String {
        fun block(): String {
            val sb = StringBuilder(4)
            for (i in 0 until 4) {
                sb.append(CHARS[random.nextInt(CHARS.size)])
            }
            return sb.toString()
        }
        return "${block()}-${block()}-${block()}-${block()}"
    }

    fun isValidKeyFormat(key: String): Boolean {
        val trimmed = key.trim().uppercase()
        val regex = Regex("^[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}$")
        return regex.matches(trimmed)
    }
}
