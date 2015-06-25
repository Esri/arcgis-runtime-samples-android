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
The Popup samples depend on the [Andriod Support Library](https://developer.android.com/tools/support-library/index.html). 

**NOTE**: The PopupInWebMapForEditing sample depends on the Android Support Library and is included in the sample as a compile time dependency in the modules build.grade file. 
