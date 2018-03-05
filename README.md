Seattle Passport 
===================

A simple boilerplate application which demonstrates the downloading, persisting and syncing of data - displayed with a common layout used amongst applications.

The project using the following libraries:

- Functional tests with [Espresso](http://google.github.io/android-testing-support-library/docs/espresso)
- [OkHttp](https://github.com/square/okhttp)
- [Spoon](https://github.com/square/spoon)

- [Butterknife](https://github.com/JakeWharton/butterknife)


Requirements
------------

 - ???

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


## Credits
