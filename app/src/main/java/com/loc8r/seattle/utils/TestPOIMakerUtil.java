package com.loc8r.seattle.utils;

import android.content.Context;

import com.loc8r.seattle.models.POI;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

/**
 * Created by steve on 2/5/2018.
 */

public class TestPOIMakerUtil {

    // Seattle's N. 50th St.
//    private static final Double NORTHERN_LAT = 47.665147;
//
//    // Seattle's N. 40th St.
//    private static final Double SOUTHERN_LAT = 47.655406;
//
//    // Seattle's Aurora Ave.
//    private static final Double WESTERN_LONG = -122.347190;
//
//    // Seattle's 1-5
//    private static final Double EASTERN_LONG = -122.322860;

    // All of Seattle North to 74th St, South to, West to Harbor, East to Lake Wa
    private static final Double NORTHERN_LAT = 47.68269;
    private static final Double SOUTHERN_LAT = 47.591383;
    private static final Double WESTERN_LONG = -122.356337;
    private static final Double EASTERN_LONG = -122.286039;

    private static final String[] NAME_FIRST_WORDS = {
            "Foo",
            "Bar",
            "Baz",
            "Qux",
            "Fire",
            "Sam's",
            "World Famous",
            "Steve's ",
            "The Best",
            "Seattle's",
            "Wallingford"
    };

    private static final String[] NAME_SECOND_WORDS = {
            "Restaurant",
            "Cafe",
            "Spot",
            "Eatin' Place",
            "Eatery",
            "Drive Thru",
            "Diner",
            "Mustache",
            "Drive-in",
            "Burgers",
            "Lounge"
    };

    private static final String[] COLLECTIONS = {
            "Historical Photos",
            "Street Art",
            "Movie Locations",
            "Landmarks",
            "Corporate Headquarters",
            "Art Deco",
            "Grunge Scene"
    };

    private static final String[] IMG_URLS = {
            "https://s3-media2.fl.yelpcdn.com/bphoto/lQbCziasMjBHjrVtGfOueQ/o.jpg",
            "https://s3-media2.fl.yelpcdn.com/bphoto/nD68rdWAKDqNDUrhWGfcTQ/o.jpg",
            "https://s3-media2.fl.yelpcdn.com/bphoto/WwrDxr0EgaVXNa79aU8SvA/o.jpg",
            "https://s3-media3.fl.yelpcdn.com/bphoto/iE4pc-nw6InozKWAXHfuTQ/o.jpg",
            "https://s3-media2.fl.yelpcdn.com/bphoto/PMqIAYAknTK01diJEspDOA/o.jpg",
            "https://s3-media4.fl.yelpcdn.com/bphoto/OHwwgF4rn4yNZei-7TaPjw/o.jpg",
            "https://s3-media4.fl.yelpcdn.com/bphoto/SK2_I9mNa0rvGG3f02ZMQA/o.jpg",
    };

    private static final String DESC = "2320 NW Market St,Seattle, WA 98107 - This is my filler text. It's designed to go to 400 characters in length. Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit";

    /**
     * Create a random Restaurant POJO.
     */
    public static POI getRandom() {

        Random random = new Random();

        String nam = getRandomName(random);
        String col = getRandomString(COLLECTIONS,random);

        POI poi = new POI("0",
                nam,
                makeRelease(),
                getRandomLatitude(),
                getRandomLongitude(),
                DESC,
                getRandomString(IMG_URLS,random),
                0,
                0,
                col,
                0,
                col.substring(0,2).toUpperCase() + "_" + nam.substring(0,2).toUpperCase());
        // poi.setName(getRandomName(random));
        // poi.setRelease(makeRelease());
        // poi.setLatitude(getRandomLatitude());
        // poi.setLongitude(getRandomLongitude());
        // poi.setDescription(DESC);
        // poi.setImg_url(getRandomString(IMG_URLS,random));
        // poi.setCollection(getRandomString(COLLECTIONS,random));
        // poi.setStampText(poi.getCollection().substring(0,2).toUpperCase() + "_" + poi.getName().substring(0,2).toUpperCase());
        //unfortunately not an easy way to set collectionPosition, so setting it in loop instead.

        return poi;
    }

    private static int makeRelease(){
        //Get the current timestamp
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyMMdd");
        return Integer.valueOf(simpleDateFormat.format(new Date()));
    }

    private static String getRandomName(Random random) {
        return getRandomString(NAME_FIRST_WORDS, random) + " "
                + getRandomString(NAME_SECOND_WORDS, random);
    }

    private static String getRandomString(String[] array, Random random) {
        int ind = random.nextInt(array.length);
        return array[ind];
    }

    private static Double getRandomLatitude(){
        Random r = new Random();
        return ((NORTHERN_LAT - SOUTHERN_LAT) * r.nextDouble()) + SOUTHERN_LAT ;
    }

    private static Double getRandomLongitude(){
        Random r = new Random();
        return ((EASTERN_LONG - WESTERN_LONG) * r.nextDouble()) + WESTERN_LONG ;
    }

}
