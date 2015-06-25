# Popup UI Customization
The purpose of this sample is to show how to customize the UI of a pop-up. The layout and style of a pop-up can be changed through XML files and extending some built-in classes of the pop-up API.

## Features
* Popup API
* MapViewHelper


## Sample Design
This sample shows how to customize the UI of a pop-up and two ways of creating pop-ups. The simplest way is to utilize a helper class from [ArcGIS Android Application Tookit](https://developers.arcgis.com/android/guide/application-framework.htm) called ```MapViewHelper``` to create pop-ups with default UI. The ```MapViewHelper``` class will iterate through and query each layer in the ```MapView```. A pop-up will be created for each feature in the query result and will be added to a ```PopupContainer```. The user-defined ```PopupCreateListener``` is called after the pop-ups are created. You can use the logic in the ```PopupCreateListener``` to display the pop-ups. 

Another approach is more complicated but more powerful. This approach allows users to customize the UI of a pop-up. In this sample a class called ```LayerQueryTask``` demonstrates this approach. ```LayerQueryTask``` loops through each layer and queries the ```MapView``` in an asynchronous task. The asynchronous task will create pop-ups for the query result. Before a pop-up is created, a new ```PopupLayoutInfo``` will be populated from XML file and be passed to ```Layer.createPopup()``` as a parameter. Then the four basic views such as title, attribute, media and attachment view will be modified by instantiating the sub-classes of the built-in classes of the views. The style of the views then can be customized through these sub-classes and XML files. 

Pop-ups will be display in a fragment called ```PopupFragment```. This fragment also provides menu to edit or delete a feature based on the edit properties of the feature.

## Sample Requirements
The Popup samples depend on the [Application Tookit for ArcGIS Android]( https://developers.arcgis.com/android/guide/application-toolkit-for-arcgis-android.htm) and the [Andriod Support Library](https://developer.android.com/tools/support-library/index.html).


**NOTE**: The PopupUICustomization sample depends on the Android Support Library and is included in the sample as a compile time depedency in the modules build.gradle file.
