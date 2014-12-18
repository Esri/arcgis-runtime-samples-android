# OAuth2
The purpose of this sample is to show how to use the [OAuth 2.0 protocol](https://developers.arcgis.com/en/authentication/user-logins.html) for authentication into the ArcGIS platform via the sample.  Once authenticated the app will return all the [web maps](http://resources.arcgis.com/en/help/arcgisonline/index.html#/What_is_an_ArcGIS_web_map/010q000000m4000000/) associated with the account.  You can then select a web map to open it up in a MapView. 

## Features
* OAuthView
* User Credentials
* Web Map
* clientID

## Sample Requirements
In order to work with OAuth2 you need to edit the string resource file and assign a valid client id string to the ```client_id``` parameter. Follow these steps:

- Browse to the [ArcGIS developers site](https://developers.arcgis.com).
- Sign in with your ArcGIS developer account.
- Create an application. This will give you access to a client id string.
- Initialize the ```client_id``` string resource, ```res/values/strings.xml```, with the client id string and run the sample. 

## Sample Design 
The sample consists of two [FragmentActivity](http://developer.android.com/reference/android/support/v4/app/FragmentActivity.html) classes. The OAuth2Sample class checks if there are any previously saved UserCredentials. If credentials are not previously cached the app creates an instance of OAuthView and prompts the user to enter credentials. Once successfully authenticated the server returns the credentials in the onCallback method of the CallbackListener. The UserCredentials object is first encrypted by creating an instance of SealedObject and then serialized to the sdcard. The OAuth2Sample class then launches the UserContentActivity. If UserCredentials are present on the sdcard then OAuth2Sample class
simply calls UserContentActivity class and bypasses all other steps.

The UserContentActivity class queries ArcGIS Portal for web maps with the user account and puts them in an ArrayList of UserWebmaps. Using the FragmentManager the app adds a new instance of UserContentFragment to the Activity. The UserContentFragment uses the userPortalDataList to populate an instance of UserContentArrayAdapter and display the thumbnail image, title, and description of each web map in the userPortalDataList. All the user interactions in the UserContentFragment are handled by the onFragmentInteraction method of OnFragmentInteractionListener interface which UserContentActivity implements. When the user taps on an item in the list, the onFragmentInteraction method is called, passing in the itemid of the item selected. This itemid is then put into a Bundle instance and passed on to the MapFragment. FragmentTransaction then replaces the UserContentFragment with the MapFragment.

The web map is constructed in the MapFragment onCreate method. The web map is created using the item chosen in the item list, when the callback returns successfully the web map is added to the MapView. The MapView instance is then added to the view of the the MapFragment.



