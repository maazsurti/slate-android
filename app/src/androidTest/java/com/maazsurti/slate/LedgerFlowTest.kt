package com.maazsurti.slate

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LedgerFlowTest {

    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    @Test
    fun aCustomerAccountOpensWithItsHistory() {
        rule.awaitText("Point of sale")
        rule.onNodeWithText("Ledger").performClick()
        rule.awaitText("Credit ledger")
        rule.onNodeWithText("Ramesh Patel").performClick()

        rule.awaitText("History")
        rule.awaitText("Record payment")
        rule.onNodeWithText("Record payment").assertIsDisplayed()
    }
}
