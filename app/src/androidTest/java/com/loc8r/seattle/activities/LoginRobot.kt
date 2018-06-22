package com.loc8r.seattle.activities

import android.support.test.espresso.ViewInteraction
import com.loc8r.seattle.R
import com.loc8r.utils.TestUtils


/**
 * A testing class written in the Robot design pattern by Jake Wharton, more info at
 *  https://academy.realm.io/posts/kau-jake-wharton-testing-robots/
 *  and https://dev.to/adammc331/leveraging-the-robot-pattern-for-espresso-tests
 *
 *  Each Activity should have it's own robot
 *
 */


// fun login(func: `LoginRobot-old`.() -> Unit) = `LoginRobot-old`().apply { func() }

class LoginRobot : BaseRobot() {

    /**
     *  apply in Kotlin returns 'this' a LoginRobot
     *  Another way to write these functions would be:
     *
     *    fun setEmail(email: String): LoginRobot {
     *      fillEditText(R.id.emailEditText, email)
     *      return this
     *    }
     *
     */
    fun setEmail(email: String) = apply {
        fillEditText(R.id.emailEditText, email) }

    fun setPassword(pass: String) = apply {
        fillEditText(R.id.passwordEditText, pass) }

    fun clickLogin() = apply {
        clickButton(R.id.bt_Login)
    }

    fun clickSignUp() = apply {
        clickText(R.id.tv_register) }

    fun matchEmailError(err: String): ViewInteraction {
        return matchErrorText(textView(R.id.emailEditText),err)
    }

    fun matchPasswordError(err: String): ViewInteraction {
        return matchErrorText(textView(R.id.passwordEditText),err)
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