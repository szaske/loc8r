package com.loc8r.seattle.activities

import android.content.Context
import android.support.test.espresso.ViewInteraction
import com.loc8r.seattle.R
import com.loc8r.utils.TestUtils
import android.widget.TextView
import android.support.test.espresso.matcher.BoundedMatcher
import android.support.annotation.NonNull
import android.support.test.espresso.ViewAssertion
import android.support.test.espresso.assertion.ViewAssertions
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.view.View
import com.loc8r.seattle.R.string.missing_email_validation_message
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers


/**
 * A testing class written in the Robot design pattern by Jake Wharton, more info at
 *  https://academy.realm.io/posts/kau-jake-wharton-testing-robots/
 *
 *  Each Activity should have it's own robot
 *
 */


// fun login(func: LoginRobot.() -> Unit) = LoginRobot().apply { func() }

class LoginRobot(private val context: Context) : BaseTestRobot() {
    fun setEmail(email: String) = apply { fillEditText(R.id.emailEditText, email); }

    fun setPassword(pass: String) = apply { fillEditText(R.id.passwordEditText, pass) }

    fun clickLogin() = apply {
        clickButton(R.id.bt_Login)
    }

    fun clickSignUp() = apply { clickText(R.id.tv_register) }

    fun matchEmailError(err: String): ViewInteraction {
        return matchErrorText(textView(R.id.emailEditText),err)
    }

    fun matchNoEmailError(err: String): ViewInteraction {
        return matchNoTErrorText(textView(R.id.emailEditText),err)
    }

    fun sleep()=apply {
        Thread.sleep(500)
    }

    fun screenShot(tag:String) = apply {
        sleep()
        TestUtils.screenShot(tag)
    }

}