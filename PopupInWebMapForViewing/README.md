# PopUp in Web Map for Viewing Sample
A popup is associated with a specific layer in a web map and describes how information about features in that layer should be presented.This sample demonstrates how to display popups for a dynamic map service layer and a non-editable feature service layer in a web map. It also shows how to set properties such as background color, text color, and selector color for popup. 

## Features

## Sample Design 
Tapping on the map to query each feature layer and each sub-layer of each dynamic map service layer. The queries are executed in asynchronous tasks. A PopupView is created for each selected graphic by feeding in the graphic and the associated popup definition that is represented by a PopupInfo object. Then add the PopuViews to the PopupContainer and display the PopupContainer in a custom Dialog.

The query upon a feature layer is performed by hit test while the query upon a sub-layer of a dynamic map service layer is through a QueryTask. For the sake of performance, a feature layer or a sub-layer which is not associated with a popup definition or not visible or not within the current scale range will not be queried.

A popup's properties such background color, text color, and selector color can be set through the PopupContainer. The setting in the PopupContainer will affect all the PopupView in the PopupContainer. If a property is not set, a default value will be used.

## Sample Requirements
The Popup samples depend on the [Andriod Support Library](https://developer.android.com/tools/support-library/index.html). 

**NOTE**: The PopupInWebMapForViewing sample depends on the Android Support Library and is included in the sample as a compile time depedency in the modules build.gradle file.
