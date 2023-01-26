# Overview
ArcGIS Runtime API for Android v100.15.1 samples.  The `main` branch of this repository contains sample app modules for the latest available version of the [ArcGIS Runtime API for Android](https://developers.arcgis.com/android/). Samples released under older versions can be found through the [git tags](https://github.com/Esri/arcgis-runtime-samples-android/tags).  Please read our [wiki](https://github.com/Esri/arcgis-runtime-samples-android/wiki) for help with working with this repository.

> :exclamation: **Try [ArcGIS Maps SDK for Kotlin version 200](https://github.com/Esri/arcgis-maps-sdk-kotlin-samples/)**
>
>ArcGIS Runtime SDK for Android version 100.15 is a long-term support release focused exclusively on bug fixes and minor updates. 
>
>ArcGIS Maps SDK for Kotlin version 200 builds on the proven architecture of 100.15, and provides a new API designed exclusively for developing Android apps using Kotlin a modern, powerful, and intuitive programming language.

# Prerequisites
* The samples are building with `compileSdkVersion 31`
* [Android Studio](http://developer.android.com/sdk/index.html)
* [An ArcGIS Developers API key](https://developers.arcgis.com/android/get-started/#3-get-an-api-key)

## Developer Instructions
Please read our [developer instructions wiki page](https://github.com/Esri/arcgis-runtime-samples-android/wiki/dev-instructions) to set up your developer environment with Android Studio.  Instructions include forking and cloning the repository for those new to Git.

Once the project is cloned to disk you can import into Android Studio:

* From the Welcome to Android Studio screen, click the **Open** button. (If you're already inside a project, click **File > Open** in the menu bar instead.)
* Navigate to the **arcgis-runtime-samples-android/java** or **arcgis-runtime-samples-android/kotlin** folder and click **OK**.

## Accessing Esri location services
Accessing Esri location services, including basemaps, routing, and geocoding, requires authentication using either an API Key or an ArcGIS identity:
 1. API key: A permanent key that gives your application access to Esri location services. Visit your [ArcGIS Developers Dashboard](https://developers.arcgis.com/dashboard) to create a new API key or access an existing API key.
 
The Android samples in this repository have been structured to use an API key, set once, which will run in all samples. Set your API key in the `gradle.properties` file located in the `/.gradle` folder within your home directory (`/Users/<user_name>/.gradle/gradle.properties`). The API_KEY property should contain quotes around the key itself: `API_KEY = "YOUR_API_KEY"`

2. ArcGIS identity: An ArcGIS named user account that is a member of an organization in ArcGIS Online or ArcGIS Enterprise.

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

### Run samples through the sample viewer
The samples in this repository can also be viewed in a single sample viewer app. It can be found on the Play Store or [on ArcGIS Online](https://arcgisruntime.maps.arcgis.com/home/item.html?id=21ac248ea189406c821400dc28bf686c). If downloading from ArcGIS Online, follow these instructions to run the app locally on your device:
1. Download and unzip the file to get the apk **ArcGIS_Runtime_Sample_Viewer_Android_10010.apk**.
1. Install the APK with adb: 
```
adb -d install path/to/ArcGIS_Runtime_Sample_Viewer_Android_10010.apk
```

## Issues

Have a question about functionality in the ArcGIS Runtime API for Android? Want to ask other users for development advice, discuss a workflow, ask Esri staff and other users about bugs in the API? Use [GeoNet](https://community.esri.com/t5/arcgis-runtime-sdk-for-android/bd-p/arcgis-runtime-sdk-for-android-questions) for any general questions like this, so others can learn from and contribute to the discussion.

Do you have something to [contribute](.github/CONTRIBUTING.md)? Send a pull request! New Samples, bug fixes and documentation fixes are welcome.

Have a problem running one of the samples in this repo? Does the sample not work on a specific device? Have questions about how the code in this repo is working? Want to request a specific sample? In that case, [submit a new issue](https://github.com/Esri/arcgis-runtime-samples-android/issues).


## Contributing
Anyone and everyone is welcome to [contribute](.github/CONTRIBUTING.md). We do accept pull requests.

1. Get Involved
2. Report Issues
3. Contribute Code
4. Improve Documentation

Please see our [guidelines for contributing doc](https://github.com/Esri/contributing/blob/main/README.md)

## Licensing
Copyright 2021 Esri

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

A copy of the license is available in the repository's [license.txt](https://github.com/Esri/arcgis-android-sdk-gradle-samples/blob/main/LICENSE) file.
