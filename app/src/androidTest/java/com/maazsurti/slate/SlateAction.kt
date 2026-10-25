package com.maazsurti.slate

import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.printToString

/** Waits for text to show up (overlines are uppercased, so match ignoring case). */
fun ComposeTestRule.awaitText(text: String, timeoutMillis: Long = 8_000) {
    try {
        waitUntil(timeoutMillis) {
            onAllNodesWithText(text, substring = true, ignoreCase = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    } catch (error: Throwable) {
        println("=== waiting for '$text' failed; tree follows ===")
        println(onRoot().printToString(maxDepth = 100))
        throw error
    }
}
