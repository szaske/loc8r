package com.loc8r.seattle.activities

import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.google.firebase.auth.FirebaseAuth
import com.loc8r.seattle.R
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
    val mActivityTestRule: ActivityTestRule<LoginActivity> = ActivityTestRule(LoginActivity::class.java)

    private lateinit var robot: LoginRobot

    @Before
    fun setup() {
        FirebaseAuth.getInstance().signOut()
        robot = LoginRobot(mActivityTestRule.activity)
    }

    @Test
    fun MissingEmailCausesErrorToAppear() {
        robot
                .setEmail("")
                .clickLogin()
                .screenShot("loginMissingEmailPassword")
                .matchEmailError("Please enter your email")
    }

    @Test
    fun EmailCausesNoErrorToAppear() {
        robot
                .setEmail("test@users.com")
                .clickLogin()
                .screenShot("loginMissingEmailPassword")
                .matchNoEmailError("Please enter your email")
    }

//    @Test
//    fun loginMissingPassword() {
//        robot
//                .setEmail("mail@example.com")
//                .clickLogin()
//                .screenShot("loginMissingPassword")
//                .matchErrorText(R.string.missing_password_validation_message)
//    }

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