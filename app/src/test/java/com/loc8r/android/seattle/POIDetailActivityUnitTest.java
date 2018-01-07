package com.loc8r.android.seattle;

import com.loc8r.seattle.activities.POIDetailActivity;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by steve on 10/22/2017.
 */

public class POIDetailActivityUnitTest {

    private POIDetailActivity testActivity;

    @Before
    public void setup(){
        testActivity = new POIDetailActivity();
    }

    @After
    public void tearDown(){
        testActivity = null;
    }

    /**
     *  Test the distance from my house to JSIS correctly,
     *  within 10 meters of accuracy
     */
    @Test
    public void testDistanceReturnsTheCorrectDistanceToJSIS(){
        assertEquals(336,testActivity.distance(47.658352,-122.328027,47.657258,-122.323861),10);
    }

    /**
     *  Test the distance from Kite Hill to the Space Needle correctly,
     *  within 10 meters of accuracy
     */
    @Test
    public void testDistanceReturnsTheCorrectDistanceFromKiteHillToSpaceNeedle(){
        assertEquals(2920,testActivity.distance(47.645313,-122.336373,47.620508,-122.349278),10);
    }
}
