package com.loc8r.seattle.activities

import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.ViewInteraction
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.hasErrorText
import org.hamcrest.core.IsNot

/**
 *  A base robot class, to create some basic robot building blocks,
 *  such as how to click a button or fill in some text
 */
open class BaseRobot {

    /**
     *  Generic editing of text
     */
    fun fillEditText(resId: Int, text: String): ViewInteraction =
            onView(withId(resId)).perform(ViewActions.replaceText(text), ViewActions.closeSoftKeyboard())

    fun clickButton(resId: Int): ViewInteraction = onView((withId(resId))).perform(ViewActions.click())

    fun clickText(resId: Int): ViewInteraction = onView((withId(resId))).perform(ViewActions.click())

    fun textView(resId: Int): ViewInteraction = onView(withId(resId))

    fun matchErrorText(viewInteraction: ViewInteraction, text: String): ViewInteraction = viewInteraction
            .check(matches(hasErrorText(text)))

    fun matchNoTErrorText(viewInteraction: ViewInteraction, text: String): ViewInteraction = viewInteraction
            .check(matches(IsNot(hasErrorText(text))))
}