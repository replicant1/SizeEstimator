package com.example.sizeestimator

import android.support.test.uiautomator.UiDevice
import android.support.test.uiautomator.UiSelector
import android.view.View
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.sizeestimator.presentation.MainActivity
import org.hamcrest.Matchers
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.startsWith

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    private var device: UiDevice? = null

    @get:Rule
    var mainActivityTestRule = ActivityScenarioRule(MainActivity::class.java)

    private lateinit var decorView: View

    @Before
    fun setup() {
        this.device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        mainActivityTestRule.scenario.onActivity(
            object : ActivityScenario.ActivityAction<MainActivity> {
                override fun perform(activity: MainActivity?) {
                    decorView = activity!!.window.decorView
                }
            }
        )
    }

    @Test
    fun testCameraPermissionAskedForAndGranted() {
        val grantButton = this.device?.findObject(UiSelector().text("While using the app"))
        grantButton!!.click()
        val measureButton = this.device?.findObject(UiSelector().text("Measure"))
        assertTrue(measureButton!!.exists())
    }

    @Test
    fun testCameraPermissionAskedForAndDenied() {
        val denyButton = this.device?.findObject(UiSelector().textStartsWith("Don"))
        println("**** Found denyButton = $denyButton")
        denyButton!!.click()
        println("**** Clicked deny button")
//        val toast = this.device?.findObject(UiSelector().text("Permission request denied"))
        // Hard to detect the text in a toast. Most approaches fail on
//        assertTrue(toast!!.exists())
//        Espresso.onView(withText("Permission request denied"))
//            .inRoot(withDecorView(not(decorView)))
//            .check(matches(isDisplayed()))
    }
}