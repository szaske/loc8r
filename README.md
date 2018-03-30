Seattle Passport 
===================

An Android application for exploring the city of Seattle, WA.

This project uses the following libraries:

- [Firebase](https://firebase.google.com/)
- [Picasso](http://square.github.io/picasso/)
- [Parceler](https://github.com/johncarl81/parceler)
- [Butterknife](https://github.com/JakeWharton/butterknife)
- [SlideUP](https://github.com/mancj/SlideUp-Android)
- Functional tests with [Espresso](http://google.github.io/android-testing-support-library/docs/espresso)
- [Spoon](https://github.com/square/spoon)

Building
--------

To build, install and run a debug version, run this from the root of the project:

    ./gradlew installRunDebug

Testing
--------

For Android Studio to use syntax highlighting for Automated tests and Unit tests you **must** switch the Build Variant to the desired mode.

To run **pmd**, **checkstyle** and **findbug** checks on your machine:

    ./gradlew check

To run **unit** tests on your machine:

    ./gradlew testDebugUnitTest

To run **automated** tests on connected devices:

    ./gradlew spoon.  Results will be stored at [REPO]\app\build\spoon\[build]\index.html

Attributions
------------

Thanks to the following for use of icons off of Noun Project:

Map Marker outline - https://thenounproject.com/term/map-marker/59715/ "Map Marker by Lee Mette from the Noun Project"
Fremont Troll - https://www.flickr.com/photos/dph1110/472754028, https://creativecommons.org/licenses/by/2.0/legalcode


## Credits
