package com.telen.namebattle.util

import java.security.MessageDigest

fun String.sha256(): String {
    val bytes = MessageDigest.getInstance("SHA-256").digest(toByteArray())
    return bytes.joinToString("") { "%02x".format(it) }
}

fun ByteArray.sha256Hex(): String {
    val bytes = MessageDigest.getInstance("SHA-256").digest(this)
    return bytes.joinToString("") { "%02x".format(it) }
}
