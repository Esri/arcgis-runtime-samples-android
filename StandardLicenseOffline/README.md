# Standard Licensing Offline
This sample shows how a Standard license can be set for an app by logging in to a Portal when a device has network connectivity, and how license information can be saved to license the app when network connectivity is unavailable. Alternatively you can explore how the set Standard license level via a license string obtained from Esri customer service (see the Standard License sample for more information). A Standard license removes the developer watermark from the map, and also enables offline functions.

This sample also loads a Tile Map Package (TPK) from device storage if network is not available when the app starts. A sample .tpk is available from ArcGIS Online.
 
## Features
- ArcGISRuntime
- client id
- LicenseInfo, toJson and fromJson methods
- LicenseResult
- LicenseLevel
- OAuthView

## Sample Requirements
To use this sample, you must set a valid client id and copy data locally.

In order to set the license level to Standard you need to edit the sample code and assign a valid client id string to the ```CLIENT_ID``` constant. Follow these steps:
- Browse to the [ArcGIS developers site](https://developers.arcgis.com).
- Sign in with your ArcGIS developer account.
- Create an application. This will give you access to a client id string.
- Initialize the ```CLIENT_ID``` constant with the client id string and run the sample. If the client id has been successfully set a OAuth sign in UI is shown which allows the user to sign in to a portal. After successful sign in the license level is set to standard based on the LicenseInfo retrieved from the authenticated portal.

**NOTE:** When you release your app, you should ensure that the client id is encrypted and saved to the device in a secure manner; this sample uses a hardcoded string instead for simplicity of example code. 

In order to copy the example .tpk file locally, follow these steps:
- On your device, browse to [http://www.arcgis.com/home/item.html?id=9a7e015149854c35b5eb94494f4aeb89](http://www.arcgis.com/home/item.html?id=9a7e015149854c35b5eb94494f4aeb89).
- Click Open and Download.
- Copy the file to the device at the expected path, for example to:
    <external-storage-folder>/ArcGIS/samples/SanFrancisco.tpk
Alternatively, you can set the path to a .tpk file you already have on the device by changing the ```DATA_RELATIVE_PATH``` constant to point to the file, as a relative path under the external storage folder.