# PopUp in Web Map for Editing Sample
The purpose of this sample is to demonstrate the editing features of popups. Besides providing UI to display information about features in a layer, a PopupContainer also provides a UI that makes it very easy to collect information about a graphic in an editable feature layer from the user. Attribute editing and attachment managing are part of the editing benefits supplied by the PopupContainer. The PopupContainer also provides hooks for developers to handle user interaction such as geometry capturing and editing, edits posting and etc. on their own. 

## Features
* WebMap
* Popup API
* Query Feature Layer in AsyncTask

## Sample Design 
This sample presents two editing workflows: adding a new point feature and editing an existing point feature. Single tap on the map will bring up popups for existing features from the feature layer in the web map. Then users can edit attributes, edit geometry, save edits to the server, and delete the feature. Long press on the map will create a popup for a new feature. After input data for attributes and save the edits, a new feature will be created and added to the feature service.

The PopupContainer calls its PopupEditingListener as the user attempts to edit a feature. PopupEditingListener is an interface. You should implement one or more methods defined in the interface which pertain to the user interaction you want to handle. This sample implements all the methods in the PopupEditingListener to handle stating editing session, adding attachment, deleting feature, canceling editing, editing geometry, and posting edits to server. Though the method for canceling editing has been implemented, the Cancel button is set invisible and the Android Back button is used instead to cancel editing.

The PopupEditingListener.onSave method will be invoked when the Save button is tapped. First, it adds a new feature or updates an existing feature to the server through ArcGISFeatureLayer.applyEdits. If applyEdits successes saves newly added attachments. Then delete attachments that are mark as “delete”.

## Sample Requirements
The Popup samples depend on the [Andriod Support Library](https://developer.android.com/tools/support-library/index.html). Instructions for setting that it up prior to running the app is detailed below. 

### Steps
 1. Create a new Sample project for PopupWebMapForEditing. Refer to the [Integration features document](https://developers.arcgis.com/en/android/guide/integration-features.htm#ESRI_SECTION1_162634B4429843789DA0311F52908566) to create a new sample project.
 2. Right click the sample project and select Android Tools > Add Support Library
 3. Accept packages to install and click Install
 4. Under Android Private Libraries you should see the android-support-v4.jar file library
 5. Right click the sample project and select Properties
 6. Select the Java Build Path on the left hand side then select Order and Export in the Java Build Path tabs
 7. Make sure Android Private Libraries is checked
 8. Run the application
 9. Single tap on the map to edit existing features. Long press on the map to create a new feature. Tap the Edit button to enter into editing mode to edit attributes, add/delete attachments, edit geometry, delete feature and save edits.
