# Overview
ArcGIS Runtime SDK for Android v10.2.6 samples for Android Studio.  The repo contains an [Android Studio](http://developer.android.com/sdk/index.html) project with multi-project sample app modules that can be run from within the Android Studio IDE.

# Prerequisites
* The samples are building with ```compileSdkVersion 21``` which requires [JDK 7 or higher](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
* [Android Studio](http://developer.android.com/sdk/index.html)

# Developer Instructions
The **ArcGIS Android SDK Samples** are [Gradle](https://www.gradle.org) based Android projects which can be directly cloned and imported into Android Studio.

## Fork the repo
If you haven't already, fork the [this repo](https://github.com/Esri/arcgis-android-sdk-gradle-samples/fork).

## Clone the repo

### Android Studio
Clone the **ArcGIS Android SDK Samples** in Android Studio:

1. Choose **VCS > Checkout from Version Control > GitHub** on the main menu.
2. From the **Repository** drop-down list, select the source repository to clone the data from.
3. In the **Folder** text box, specify the directory where the local repository for cloned sources will be set up.
4. Click the Clone button to start cloning the sources from the specified remote repository.

**NOTE**: Do not import the project into Android Studio.  There is an [outstanding issue](https://groups.google.com/forum/#!topic/adt-dev/o8h3Jg9ICGo) in Android Studio that requires importing the project in the steps defined below.

### Command line Git
[Clone the ArcGIS Android SDK Samples](https://help.github.com/articles/fork-a-repo#step-2-clone-your-fork)

Open your terminal, navigate to your working directory, use ```git clone``` to get a copy of the repo.

```
# Clones your fork of the repository into the current directory in terminal
$ git clone https://github.com/YOUR-USERNAME/arcgis-runtime-samples-android.git
```

## Import Gradle Sample project into Android Studio
Once the project is cloned to disk you can import into Android Studio:

* From the toolbar select **File > Import Project**, or **Import Non-Android Studio project** from the Welcome Quick Start.
* Navigate to the root project folder, **arcgis-android-sdk-gradle-samples-10.2.6** directory and click **OK**

## Run a sample
You should now be able to run any of the included samples.  We will use the ```HelloWorld``` Sample as an example.  

* Select ```HelloWorld``` from the **Select Run/Debug Configuration** drop down
* Click the **Run** button

## Location Services
Some of our apps needs active GPS connection to run. However, while trying to execute them on an emulator take a note that although GPS is enabled, you will need to push the location to the emulator.
Refer [here](http://developer.android.com/tools/devices/emulator.html) for more information.
You can also push the location using the Android Device Manager
1. Launch Android Device Manager
2. Select 'Emulator Control' tab
3. Enter Longitude and Latitude and click on Send (This will fix the location on your emulator)


## Issues
Find a bug or want to request a new feature enhancement?  Please let us know by submitting an issue.

## Contributing
Anyone and everyone is welcome to contribute. We do accept pull requests.

1. Get Involved
2. Report Issues
3. Contribute Code
4. Improve Documentation

Please see our [guidelines for contributing doc](https://github.com/Esri/contributing/blob/master/README.md)

## Licensing
Copyright 2015 Esri

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

A copy of the license is available in the repository's [license.txt](https://github.com/Esri/arcgis-android-sdk-gradle-samples/blob/master/LICENSE) file.

[](Esri Tags: ArcGIS Android Mobile)
[](Esri Language: Java)​
