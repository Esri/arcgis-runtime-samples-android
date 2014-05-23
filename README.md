# Overview
ArcGIS Runtime SDK for Android Early Access Preview Samples for Android Studio.  The ArcGIS Android SDK Gradle samples depends on the [ArcGIS Runtime SDK for Android library module for Android Studio](https://github.com/ArcGIS/arcgis-android-api-lib-module) you will need to clone both repositories and add the ArcGIS Android API Library Module to the gradle sample project.  

# Early Access Preview
**Caution:** The ArcGIS Android SDK Gradle samples are currently available as an **early access preview** for use with [Android Studio](http://developer.android.com/sdk/installing/studio.html) and the [ArcGIS Android API Library Module](https://github.com/ArcGIS/arcgis-android-api-lib-module).  If you are not comfortable using an unfinished product, you may want to use ArcGIS Android Samples in the Eclipse Plugin bundled with the [ArcGIS Android SDK](https://developers.arcgis.com/android/).

# Fork the repo
If you haven't already, go to https://github.com/ArcGIS/arcgis-android-sdk-gradle-samples and click the **Fork** button.

# Clone the repo
Open your terminal, navigate to your working directory, use ```git clone``` to get a copy of the repo.

```
$ git clone git@github.com:YOUR-USERNAME/arcgis-android-sdk-gradle-samples.git
```

## Import project into Android Studio
Once the project is cloned to disk you can import into Android Studio:

* From the toolbar select **File > Import Project**
* Navigate to the root project folder, *arcgis-android-sdk-gradle-samples* and click **OK**

### Import ArcGIS Android lib module
This is where we start to turn our project into an ArcGIS for Android project.

### Import the ArcGIS Android API library module
- Right Click your project and select **Open Module Settings**
- Click the ```+``` sign above **SDK Location** and select **Import Existing Project** then click **Next**
- Navigate to the folder where you cloned the ```arcgis-android-sdk-module``` repo and select the ```arcgis-android-v10.2.3``` folder which contains the library module.  Do not import the entire project, just the library module e.g. ```/[path-to-repo]/arcgis-android-sdk-module/arcgis-android-v10.2.3``` and click **OK** then **Finish** to import the library module.

**NOTE** If you navigate to the root project directory you will see all available modules listed.  Ensure that ```arcgis-android-v10.2.3``` module is select from your project and that the app module is unchecked.

### Run a sample
You should now be able to run any of the included samples.  We will use the ```HelloWorld``` Sample as an example.  

* Select ```HelloWorld``` from the **Select Run/Dubug Configuration** drop down
* Click the **Run** button
