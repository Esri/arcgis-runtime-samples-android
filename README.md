# Overview
ArcGIS Runtime SDK for Android v100.0.0 samples.  The `master` branch of this repository contains sample app modules for the latest available version of the [ArcGIS Runtime SDK for Android](https://developers.arcgis.com/android/). Samples released under older versions can be found through the [git tags](https://github.com/Esri/arcgis-runtime-samples-android/tags).

# Prerequisites
* The samples are building with `compileSdkVersion 25` which requires [JDK 7 or higher](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
* [Android Studio](http://developer.android.com/sdk/index.html)

## SDK Maven repo
The SDK is available through the Bintray Maven repo, you can take a look at the repository with the link below: 

[ ![Download](https://api.bintray.com/packages/esri/arcgis/arcgis-android/images/download.svg?version=100.0.0) ](https://bintray.com/esri/arcgis/arcgis-android/100.0.0/link)

## Developer Instructions
Please read our developer instructions wiki page to set up your developer enviroment with Android Studio.  Insructions include forking and cloning the repository for those new to Git

## Run a sample
Once you have set up your developer environment you can run any sample from within Android Studio by selecting the app module from the **Edit Configurations** drop down and clicking the **Run** button from the toolbar. 

### Build/Run sample from Gradle
You can execute all the build tasks using the [Gradle Wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html) command line tool. It's available as a batch file for Windows (gradlew.bat) and a shell script for Linux/Mac (gradlew.sh) and it is accessible from the root of the project.  

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
Anyone and everyone is welcome to [contribute](CONTRIBUTING.md). We do accept pull requests.

1. Get Involved
2. Report Issues
3. Contribute Code
4. Improve Documentation

Please see our [guidelines for contributing doc](https://github.com/Esri/contributing/blob/master/README.md)

## Licensing
Copyright 2017 Esri

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

A copy of the license is available in the repository's [license.txt](https://github.com/Esri/arcgis-android-sdk-gradle-samples/blob/master/LICENSE) file.

[](Esri Tags: ArcGIS Android Mobile)
[](Esri Language: Java)​
