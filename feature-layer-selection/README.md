#Feature layer selection
This sample demonstrates how to select features in a feature layer.

![FeatureLayer Selection](featurelayer-selection.png)

## How to use the sample
Tap on a feature on the map to select it

## How it works
The map view  provides a way to add a listener to screen taps using the ```setOnTouchListener```method. The app uses the ```MotionEvent``` passed in to the ```onSingleTapConfirmed``` method and creates an envelope around that point based on the tolerance. It then creates an query parameter object and sets it geometry property to the envelope it just created (which will be used to find features within it). It then calls the select features method which takes a query parameters object, this selects the features and also returns the result from which you can inspect the selected features.
