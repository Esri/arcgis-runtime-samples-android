# Overview
ArcGIS Runtime SDK for Android v10.2.8 samples for Android Studio.  The repo contains an [Android Studio](http://developer.android.com/sdk/index.html) project with multi-project sample app modules that can be run from within the Android Studio IDE.

# Prerequisites
* The samples are building with ```compileSdkVersion 21``` which requires [JDK 7 or higher](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
* [Android Studio](http://developer.android.com/sdk/index.html)

# Developer Instructions
The **ArcGIS Android SDK Samples** are [Gradle](https://www.gradle.org) based Android projects which can be directly cloned and imported into Android Studio.

Each sample app module has a build.gradle file with the latest ArcGIS Android SDK compile dependency.

```groovy
dependencies {
    compile 'com.esri.arcgis.android:arcgis-android:10.2.8'
}
```

Our SDK is hosted in our public maven repository hosted by Bintray.  Our repository url is added to the projects build.gradle file.

```groovy
repositories {
    jcenter()
    maven {
        url 'https://esri.bintray.com/arcgis'
    }
}
```

[ ![Download](https://api.bintray.com/packages/esri/arcgis/arcgis-android/images/download.svg) ](https://bintray.com/esri/arcgis/arcgis-android/_latestVersion)

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

## Configure remote upstream for your fork
To sync changes you make in a fork with this repository, you must configure a remote that points to the upstream repository in Git.

- Open a terminal (Mac users) or command prompt (Windows & Linux users)
- List the current configured remote repository for your fork

```
$ git remote -v
origin	https://github.com/YOUR_USERNAME/arcgis-runtime-samples-android.git (fetch)
origin	https://github.com/YOUR_USERNAME/arcgis-runtime-samples-android.git (push)
```

- Specify a new remote upstream repository

```
$ git remote add upstream https://github.com/Esri/arcgis-runtime-samples-android.git
```

- Verify the new upstream repository

```
$ git remote -v

origin	https://github.com/YOUR_USERNAME/arcgis-runtime-samples-android.git (fetch)
origin	https://github.com/YOUR_USERNAME/arcgis-runtime-samples-android.git (push)
upstream https://github.com/Esri/arcgis-runtime-samples-android.git (fetch)
upstream https://github.com/Esri/arcgis-runtime-samples-android.git (push)
```

### Sync your fork
Once you have set up a remote upstream you can keep your fork up to date with our samples repository by syncing your fork.

- Open a terminal (Mac users) or command prompt (Windows & Linux users)
- Change to the current working directory of your local repository
- Fetch the branches and commits from the upstream repository.  Commits to ```master``` will be stored in a local branch, ```upstream/master```.

```
$ git fetch upstream
```

- Check out your forks local ```master``` branch

```
$ git checkout master
```

- Merge changes from ```upstream/master``` into  your local ```master``` branch which syncs your forks ```master``` branch with our samples repository.

```
$ git merge upstream/master
```

## Import Gradle Sample project into Android Studio
Once the project is cloned to disk you can import into Android Studio:

* From the toolbar select **File > Import Project**, or **Import Non-Android Studio project** from the Welcome Quick Start.
* Navigate to the root project folder, **arcgis-android-sdk-gradle-samples-10.2.8** directory and click **OK**

## Run a sample
You should now be able to run any of the included samples.  We will use the ```HelloWorld``` Sample as an example.  

* Select ```HelloWorld``` from the **Select Run/Debug Configuration** drop down
* Click the **Run** button

## Location Services
Some of our sample app modules need an active GPS connection to run. If you are running these samples on an emulator you will need to push the location to a GPS enabled emulator.
Refer [here](http://developer.android.com/tools/devices/emulator.html) for more information.
You can also push the location using the Android Device Manager.

Steps to fix a location on your emulator:

1. Launch Android Device Manager
2. Select **Emulator Control** tab
3. Enter **Longitude** and **Latitude** then click on **Send** 


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
Copyright 2016 Esri

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

A copy of the license is available in the repository's [license.txt](https://github.com/Esri/arcgis-android-sdk-gradle-samples/blob/master/LICENSE) file.

[](Esri Tags: ArcGIS Android Mobile)
[](Esri Language: Java)​
