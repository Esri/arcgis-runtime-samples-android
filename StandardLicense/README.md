# Standard Licensing

This sample shows how to set the license level of your ArcGIS application to Standard. Setting the license level of your application to Standard allows you to use features of the ArcGIS SDK for Android that require Standard license level, such as offline routing and geocoding, and also removes the developer watermark from the map. 

## Features

- ArcGISRuntime
- client id
- LicenseInfo
- license string
- LicenseResult
- LicenseLevel

## Sample Requirements
In order to set the license level to Standard you need to edit the sample code and assign a valid client id string to the ```CLIENT_ID``` constant. Follow these steps:

- Browse to the [ArcGIS developers site](https://developers.arcgis.com).
- Sign in with your ArcGIS developer account.
- Create an application. This will give you access to a client id string.
- Initialize the ```CLIENT_ID``` constant with the client id string and run the sample. If the client id has been successfully set a OAuth sign in UI is shown which allows the user to sign in to a portal. After successful sign in the license level is set to standard based on the LicenseInfo retrieved from the authenticated portal.

**NOTE:** When you release your app, you should ensure that the client id is encrypted and saved to the device in a secure manner; this sample uses a hardcoded string instead for simplicity of example code. 

Alternatively you can explore how to set Standard license level via a license string obtained from Esri customer service. In this case edit the code to initialize the ```LICENSE_STRING``` constant with a license string and set ```USE_LICENSE_INFO``` to false. License strings should also always be stored in a secure encrypted manner on the device. 