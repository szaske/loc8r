package com.loc8r.seattle.activities

import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Created by steve on 2/28/2018.
 */

// @LargeTest
@RunWith(AndroidJUnit4::class)
class LoginActivityTest {

    @get:Rule
    val rule: ActivityTestRule<LoginActivity> = ActivityTestRule(LoginActivity::class.java)

    // private lateinit var robot: LoginRobot

    @Before
    fun setup() {
        FirebaseAuth.getInstance().signOut()
        // var robot = LoginRobot()
    }

    @Test
    fun EmailCausesNoErrorToAppear() {
        Log.d("STZ", "running next test")
        LoginRobot()
                .setEmail("test@email.com")
                .clickLogin()
                .screenShot("EmailShowsNoErrorMessage")
                .matchNoEmailError("Please enter your email")
    }


    @Test
    fun MissingEmailCausesErrorToAppear() {
        Log.d("STZ", "running test")
        LoginRobot()
                .setEmail("")
                .clickLogin()
                .screenShot("MissingEmailShowsError")
                .matchEmailError("Please enter your email")
    }


    @Test
    fun loginMissingPassword() {
        LoginRobot()
                .setEmail("mail@example.com")
                .clickLogin()
                .screenShot("loginMissingPassword")
                .matchPasswordError("Password cannot be blank")
    }

//    @Test
//    fun loginWrongPassword() {
//        robot
//                .setEmail("mail@example.com")
//                .setPassword("wrong")
//                .clickLogin()
//                .screenShot("loginWrongPassword")
//                .matchErrorText(R.string.login_fail)
//    }

//    @Test
//    fun loginSuccess() {
//        robot
//                .setEmail("mail@example.com")
//                .setPassword("pass")
//                .clickLogin()
//                .screenShot("loginSuccess")
//                .matchErrorText(R.id.tvName, mActivityTestRule.activity.getString(R.string.name_surname))
//    }
//
//    @Test
//    fun loginProfileAndSettings() {
//        robot
//                .setEmail("mail@example.com")
//                .setPassword("pass")
//                .screenShot("login")
//                .clickLogin()
//                .screenShot("profile")
//                .clickSettings()
//                .screenShot("settings")
//                .toggleNotifications()
//                .screenShot("toggle1")
//                .toggleNightMode()
//                .screenShot("toggle2")
//    }
}