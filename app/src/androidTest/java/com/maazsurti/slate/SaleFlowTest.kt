package com.maazsurti.slate

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SaleFlowTest {

    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    @Test
    fun aCashSaleIsRecordedAndTheBasketEmpties() {
        rule.awaitText("Basmati Rice 5kg")
        rule.onNodeWithText("Basmati Rice 5kg").performClick()
        rule.onNodeWithText("Complete sale").performClick()

        rule.awaitText("Sale recorded")
        rule.onNodeWithText("0 in the basket", substring = true).assertIsDisplayed()
    }

    @Test
    fun aCreditSaleRefusesToCompleteWithoutACustomer() {
        rule.awaitText("Milk 500ml")
        rule.onNodeWithText("Milk 500ml").performClick()
        rule.onNodeWithText("Credit").performClick()
        rule.onNodeWithText("Complete sale").performClick()

        rule.awaitText("Pick a customer for a credit sale")
    }
}
