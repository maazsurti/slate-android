package com.maazsurti.slate

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StockFlowTest {

    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    @Test
    fun aProductAddedOnTheStockTabShowsUpInTheList() {
        val name = "Test Atta ${System.currentTimeMillis()}"
        rule.awaitText("Point of sale")
        rule.onNodeWithText("Stock").performClick()
        rule.onNodeWithText("Add").performClick()
        rule.onNodeWithText("Name").performTextInput(name)
        rule.onNodeWithText("Price").performTextInput("120")
        rule.onNodeWithText("In stock").performTextInput("12")
        rule.onNodeWithText("Grains").performClick()
        rule.onNodeWithText("Save product").performClick()

        rule.awaitText("Added")
        rule.onNode(hasScrollAction()).performScrollToNode(hasText(name))
        rule.onNodeWithText(name).assertIsDisplayed()
    }
}
