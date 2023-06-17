/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.mozilla.reference.browser.ext

/**
 * Replaces the keys with the values with the map provided.
 */
fun String.replace(pairs: Map<String, String>): String {
    var result = this
    pairs.forEach { (l, r) -> result = result.replace(l, r) }
    return result
}

// This extension function wraps the string in a way that is readable on a txt file
fun String.wrapForTxt(): String {
    val lineWidth = 40 // This line width should prevent text overlap
    val result = StringBuilder()
    var startIndex = 0
    var endIndex = 40 // Initialized as the same as line width

    while (startIndex < length) {
        if (endIndex >= length) {
            endIndex = length
        } else {
            while (endIndex > startIndex && !Character.isWhitespace(this[endIndex])) {
                endIndex--
            }
        }

        if (endIndex <= startIndex) {
            endIndex = startIndex + lineWidth
        }

        result.append(substring(startIndex, endIndex)).append(System.lineSeparator())

        startIndex = endIndex
        endIndex += lineWidth
    }

    return result.toString()
}
