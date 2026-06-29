package com.telen.namebattle.util

import java.text.Normalizer

/** Strips diacritics and returns the uppercase ASCII base letter. 'É' → 'E', 'à' → 'A'. */
fun Char.toBaseAscii(): Char {
    val decomposed = Normalizer.normalize(this.toString(), Normalizer.Form.NFD)
    val ascii = decomposed.first { it.code < 128 && it.isLetter() }
    return ascii.uppercaseChar()
}
