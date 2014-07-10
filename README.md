# Overview
ArcGIS Runtime SDK for Android Early Access Preview Samples for Android Studio.  The ArcGIS Android SDK Gradle samples depends on the [ArcGIS Runtime SDK for Android library module for Android Studio](https://github.com/ArcGIS/arcgis-android-api-lib-module) you will need to clone both repositories and add the ArcGIS Android API Library Module to the gradle sample project.  

# Early Access Preview
**Caution:** The ArcGIS Android SDK Gradle samples are currently available as an **early access preview** for use with [Android Studio](http://developer.android.com/sdk/installing/studio.html) and the [ArcGIS Android API Library Module](https://github.com/ArcGIS/arcgis-android-api-lib-module).  If you are not comfortable using an unfinished product, you may want to use ArcGIS Android Samples in the Eclipse Plugin bundled with the [ArcGIS Android SDK](https://developers.arcgis.com/android/).

# Prerequisites
- [JDK 6 or higher](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
- [Android Studio (Beta) 0.8.0+](https://developer.android.com/sdk/installing/studio.html) with support for gradle plugin ```0.12.+```.  
- Android Support Library.  Ensure that your SDK Manager for Android Studio has the support library installed.  We make use of [v7 appcompat library](http://developer.android.com/tools/support-library/features.html#v7-appcompat) to follow [Android design guidelines](http://developer.android.com/design/index.html). Please ensure you have the latest version of the Android Support Library installed with your Android SDK.

# Developer Instructions
The **ArcGIS Android SDK Samples** are Gradle based Android projects which can be directly cloned and imported into Android Studio.  The **ArcGIS Android SDK Samples** require the **ArcGIS Android API Library Module** to be imported into the **ArcGIS Android SDK Samples** project.

## Fork the repo
If you haven't already, go to https://github.com/ArcGIS/arcgis-android-sdk-gradle-samples and click the **Fork** button and go to https://github.com/ArcGIS/arcgis-android-api-lib-module and click the **Fork** button.

## Clone the repo

### Android Studio
[Clone the **ArcGIS Android SDK Samples** and **ArcGIS Android API Library Module** in Android Studio](http://www.jetbrains.com/idea/webhelp/cloning-a-repository-from-github.html).

1. Choose **VCS > Checkout from Version Control > GitHub** on the main menu.
2. From the **Repository** drop-down list, select the source repository to clone the data from.
3. In the **Folder** text box, specify the directory where the local repository for cloned sources will be set up.
4. Click the Clone button to start cloning the sources from the specified remote repository.

![clone](https://raw.githubusercontent.com/ArcGIS/arcgis-android-sdk-gradle-samples/master/as-clone.png?token=1308001__eyJzY29wZSI6IlJhd0Jsb2I6QXJjR0lTL2FyY2dpcy1hbmRyb2lkLXNkay1ncmFkbGUtc2FtcGxlcy9tYXN0ZXIvYXMtY2xvbmUucG5nIiwiZXhwaXJlcyI6MTQwNTYyMTAzNX0%3D--317720430054a2555c382fa5258476dad177909b)

### Command line Git
[Clone the **ArcGIS Android SDK Samples** and **ArcGIS Android API Library Module**](https://help.github.com/articles/fork-a-repo#step-2-clone-your-fork)

Open your terminal, navigate to your working directory, use ```git clone``` to get a copy of the repo.

```
# Clones your fork of the repository into the current directory in terminal
$ git clone git@github.com:YOUR-USERNAME/arcgis-android-sdk-gradle-samples.git
$ git clone git@github.com:YOUR-USERNAME/arcgis-android-api-lib-module.git
```

**Now that we have both repos cloned locally we can begin to import our gradle samples project and add the **ArcGIS Android API Library Module** module to that project as a dependency.**

## Import Gradle Sample project into Android Studio
Once the project is cloned to disk you can import into Android Studio:

* From the toolbar select **File > Import Project**
* Navigate to the root project folder, *arcgis-android-sdk-gradle-samples* and click **OK**

## Add ArcGIS Android library module
This is where we start to turn our project into an ArcGIS for Android project.

* Right Click your project and select **Open Module Settings**
* Click the ```+``` sign above **SDK Location** and select **Import Existing Project** then click **Next**
* Navigate to the folder where you cloned the ```arcgis-android-api-lib-module``` repo and select the ```arcgis-android-v10.2.3``` folder which contains the library module.  Do not import the entire project, just the library module e.g. ```/[path-to-repo]/arcgis-android-api-lib-module/arcgis-android-v10.2.3``` and click **OK** then **Finish** to import the library module.

**NOTE** If you navigate to the root project directory you will see all available modules listed.  Ensure that ```arcgis-android-v10.2.3``` module is select from your project and that the **app** module is unchecked.

## Run a sample
You should now be able to run any of the included samples.  We will use the ```HelloWorld``` Sample as an example.  

* Select ```HelloWorld``` from the **Select Run/Dubug Configuration** drop down
* Click the **Run** button
