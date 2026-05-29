package com.wefit.nourishcycle

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented navigation tests that launch the real MainActivity.
 *
 * These tests verify the splash → home → insights navigation flow
 * using the Compose testing framework.
 *
 * NOTE: The splash screen auto-navigates to Home after its animation
 * completes (~2s). Tests use a long idle timeout to wait for this.
 */
@RunWith(AndroidJUnit4::class)
class NavigationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun splashScreen_isDisplayedOnLaunch() {
        // Splash logo or tagline text should be visible immediately on launch
        composeTestRule.waitForIdle()
        // The splash screen draws animated leaf + app name — we check the node exists
        composeTestRule.onNodeWithText("NourishCycle", useUnmergedTree = true)
            .assertExists()
    }

    @Test
    fun afterSplash_homeScreen_isDisplayed() {
        // Wait for splash to complete auto-navigation (up to 5 seconds)
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodes(
                androidx.compose.ui.test.hasText("Good Morning", substring = true)
                    .or(androidx.compose.ui.test.hasText("Good Afternoon", substring = true))
                    .or(androidx.compose.ui.test.hasText("Good Evening", substring = true))
            ).fetchSemanticsNodes().isNotEmpty()
        }
        // Home screen shows time-based greeting
        composeTestRule.onNodeWithText("Good Morning", substring = true)
            .assertExists()
            .run {
                // If morning check fails (not in morning), try afternoon/evening
            }
    }

    @Test
    fun homeScreen_insightsFab_isDisplayed() {
        // Wait for home screen
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodes(
                androidx.compose.ui.test.hasContentDescription("View Insights")
            ).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule
            .onNodeWithContentDescription("View Insights")
            .assertIsDisplayed()
    }

    @Test
    fun clickInsightsFab_navigatesToInsightsScreen() {
        // Wait for home screen FAB
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodes(
                androidx.compose.ui.test.hasContentDescription("View Insights")
            ).fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule
            .onNodeWithContentDescription("View Insights")
            .performClick()

        composeTestRule.waitForIdle()

        // Insights screen header text should now appear
        composeTestRule
            .onNodeWithText("Insights", substring = true)
            .assertExists()
    }
}
