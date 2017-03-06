# Overview
ArcGIS Runtime SDK for Android v100.0.0 samples.  The `master` branch of this repository contains sample app modules for the latest available version of the [ArcGIS Runtime SDK for Android](https://developers.arcgis.com/android/). Samples released under older versions can be found through the [repository releases](https://github.com/Esri/arcgis-runtime-samples-android/releases).

# Prerequisites
* The samples are building with `compileSdkVersion 25` which requires [JDK 7 or higher](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
* [Android Studio](http://developer.android.com/sdk/index.html)

# Developer Instructions
The **ArcGIS Android SDK Samples** are [Gradle](https://www.gradle.org) based Android projects which can be directly cloned and imported into Android Studio.

The latest ArcGIS Android SDK compile dependency is defined for all sample modules in the root project build.gradle.  This is the only place where you need to define the dependency to the ArcGIS Android SDK.

```groovy
subprojects{
    afterEvaluate {project ->
        if(project.hasProperty("dependencies")){
            dependencies {
                compile 'com.esri.arcgisruntime:arcgis-android:100.0.0'
            }
        }
    }
}
```

Our SDK is hosted in our public maven repository hosted by Bintray.  Our repository url is added to the projects root build.gradle file.

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

Open your terminal, navigate to your working directory, use `git clone` to get a copy of the repo.

```
# Clones your fork of the repository into the current directory in terminal
$ git clone https://github.com/YOUR-USERNAME/arcgis-runtime-samples-android.git
```

## Configure remote upstream for your fork
To sync changes you make in a fork with this repository, you must configure a remote that points to the upstream repository in Git.

- Open a terminal or command prompt
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

- Open a terminal or command prompt
- Change to the current working directory of your local repository
- Fetch the branches and commits from the upstream repository.  Commits to `master` will be stored in a local branch, `upstream/master`.

```
$ git fetch upstream
```

- Check out your forks local `master` branch

```
$ git checkout master
```

- Merge changes from `upstream/master` into  your local `master` branch which syncs your forks `master` branch with our samples repository.

```
$ git merge upstream/master
```

## Import Gradle Sample project into Android Studio
Once the project is cloned to disk you can import into Android Studio:

* From the toolbar select **File > Import Project**, or **Import Non-Android Studio project** from the Welcome Quick Start.
* Navigate to the root project folder, **arcgis-runtime-samples-android** directory and click **OK**

## Run a sample
You should now be able to run any of the included samples.  We will use the `set-map-initial-location` sample as an example.

* Select `set-map-initial-location` from the **Select Run/Debug Configuration** drop down
* Click the **Run** button

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
