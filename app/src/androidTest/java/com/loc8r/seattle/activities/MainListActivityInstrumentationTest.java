package com.loc8r.seattle.activities;

import android.app.Activity;
import android.app.Instrumentation;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.loc8r.seattle.R;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.Intents.intending;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.intent.matcher.IntentMatchers.isInternal;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

/**
 *  Instrumentation Test for MainList Activity
 */

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainListActivityInstrumentationTest {

//    @Rule
//    public ActivityTestRule<MainListActivity> activityTestRule =
//            new ActivityTestRule<>(MainListActivity.class);

    @Rule
    public IntentsTestRule<MainListActivity> intentsTestRule =
            new IntentsTestRule<>(MainListActivity.class);


    @Before
    public void stubAllExternalIntents() {
        // By default Espresso Intents does not stub any Intents. Stubbing needs to be setup before
        // every test run. In this case all external Intents will be blocked.
        intending(not(isInternal())).respondWith(new Instrumentation.ActivityResult(Activity.RESULT_OK, null));
    }

    /**
     *  Basic test to see if both buttons are included in the UI
     */
    @Test
    public void MainMenuContainsThreeButtons() {
        onView(withId(R.id.explore_Button)).check(matches(withText("Explore Map")));
        onView(withId(R.id.my_passport_Button)).check(matches(withText("My Passport")));
    }

    /**
     *  Tests to see that the 'Explore Map' button launches the Map Activity
     */
    @Test
    public void validateButtonLaunchesMapActivity() {
        onView(withId(R.id.explore_Button)).perform(click());

        intended(hasComponent(MapActivity.class.getName()));
    }

    /**
     *  Tests to see if we can get to the Admin page
     */
    @Test
    public void ValidateWeCanGetToAdminPage() {
        // Open the overflow menu OR open the options menu,
        // depending on if the device has a hardware or software overflow menu button.
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        // Click the item.
        onView(withText("Admin Abilities"))
                .perform(click());

        intended(hasComponent(ManagementActivity.class.getName()));
    }
}
