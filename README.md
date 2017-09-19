# Overview
ArcGIS Runtime SDK for Android v100.2.0 samples.  This branch, **develop**, is not guarenteed to build against a release version of the ArcGIS Android SDK.  The **master** branch contains sample app modules for the latest available version of the [ArcGIS Runtime SDK for Android](https://developers.arcgis.com/android/). Samples released under older versions can be found through the [git tags](https://github.com/Esri/arcgis-runtime-samples-android/tags).  Please read our [wiki](https://github.com/Esri/arcgis-runtime-samples-android/wiki) for help with working with this repository.  

# Prerequisites
* The samples are building with `compileSdkVersion 26` which requires [JDK 7 or higher](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
* [Android Studio](http://developer.android.com/sdk/index.html)

## Project Structure
Sample are organized by developer language.  All Java based samples are in the [java](java) folder and Kotlin based samples are in the [kotlin](kotlin) folder. These project folders need to be imported directly into Android Studio: 

- From the Android Studio toolbar select **File > Import Project**, or **Import Non-Android Studio** project from the **Welcome Quick Start**.
- Navigate to the **java** or **kotlin** project directory and click OK

## Developer Instructions
Please read our [developer instructions wiki page](https://github.com/Esri/arcgis-runtime-samples-android/wiki/dev-instructions) to set up your developer enviroment with Android Studio.  Instructions include forking and cloning the repository for those new to Git

## Run a sample
Once you have set up your developer environment you can run any sample from within Android Studio by selecting the app module from the **Edit Configurations** drop down and clicking the **Run** button from the toolbar. 

### Build/Run sample from Gradle
You can execute all the build tasks using the [Gradle Wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html) command line tool. It's available as a batch file for Windows (gradlew.bat) and a shell script for Linux/Mac (gradlew) and it is accessible from the **java** or **kotlin** project folders.  

- Build a debug APK

```
$ ./gradlew assembleDebug
```

- Run the app

**Device**
```
adb -d install path/to/sample.apk
```

Built APK's are saved to **arcgis-runtime-samples-android/[module-name]/build/outputs/apk/**. More information about running apps on devices can be found [here](https://developer.android.com/studio/run/device.html).

## Issues
Find a bug or want to request a new feature enhancement?  Please let us know by submitting an issue.

## Contributing
Anyone and everyone is welcome to [contribute](.github/CONTRIBUTING.md). We do accept pull requests.

1. Get Involved
2. Report Issues
3. Contribute Code
4. Improve Documentation

Please see our [guidelines for contributing doc](.github/CONTRIBUTING.md)

## Licensing
Copyright 2017 Esri

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

A copy of the license is available in the repository's [LICENSE](LICENSE) file.
