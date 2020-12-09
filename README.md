# Overview
ArcGIS Runtime SDK for Android v100.9.0 samples.  The `master` branch of this repository contains sample app modules for the latest available version of the [ArcGIS Runtime SDK for Android](https://developers.arcgis.com/android/). Samples released under older versions can be found through the [git tags](https://github.com/Esri/arcgis-runtime-samples-android/tags).  Please read our [wiki](https://github.com/Esri/arcgis-runtime-samples-android/wiki) for help with working with this repository.

# Prerequisites
* The samples are building with `compileSdkVersion 29`
* [Android Studio](http://developer.android.com/sdk/index.html)

## Developer Instructions
Please read our [developer instructions wiki page](https://github.com/Esri/arcgis-runtime-samples-android/wiki/dev-instructions) to set up your developer environment with Android Studio.  Instructions include forking and cloning the repository for those new to Git.

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
The samples in this repository can also be viewed in a single sample viewer app. It can be found on the Play Store or [on ArcGIS Online](https://arcgisruntime.maps.arcgis.com/home/item.html?id=32e5e1ce88154de8b640fa0ca9a052db). If downloading from ArcGIS Online, follow these instructions to run the app locally on your device:
1. Download and unzip the file to get the apk **ArcGIS_Runtime_Sample_Viewer_Android_1009.apk**.
1. Install the APK with adb: 
```
adb -d install path/to/ArcGIS_Runtime_Sample_Viewer_Android_1009.apk
```

## Issues

Have a question about functionality in the ArcGIS Runtime SDK for Android? Want to ask other users for development advice, discuss a workflow, ask Esri staff and other users about bugs in the SDK? Use [GeoNet](https://geonet.esri.com/community/developers/native-app-developers/arcgis-runtime-sdk-for-android) for any general ArcGIS Runtime Android SDK questions like this, so others can learn from and contribute to the discussion.

Do you have something to [contribute](.github/CONTRIBUTING.md)? Send a pull request! New Samples, bug fixes and documentation fixes are welcome.

Have a problem running one of the samples in this repo? Does the sample not work on a specific device? Have questions about how the code in this repo is working? Want to request a specific sample? In that case, [submit a new issue](https://github.com/Esri/arcgis-runtime-samples-android/issues).


## Contributing
Anyone and everyone is welcome to [contribute](.github/CONTRIBUTING.md). We do accept pull requests.

1. Get Involved
2. Report Issues
3. Contribute Code
4. Improve Documentation

Please see our [guidelines for contributing doc](https://github.com/Esri/contributing/blob/master/README.md)

## Licensing
Copyright 2020 Esri

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

A copy of the license is available in the repository's [license.txt](https://github.com/Esri/arcgis-android-sdk-gradle-samples/blob/master/LICENSE) file.
