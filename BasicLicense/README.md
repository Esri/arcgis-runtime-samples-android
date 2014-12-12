# Basic Licensing

This sample shows how to set the license level of your ArcGIS application to Basic. Setting the license level to Basic prevents the watermark from appearing on the map. 

## Features

- ArcGISRuntime
- client id
- LicenseResult
- LicenseLevel

## Sample Requirements

In order to set the license level to Basic you need to edit the sample code and assign a valid client id string to the ```CLIENT_ID``` constant. Follow these steps:

- Browse to the [ArcGIS developers site](https://developers.arcgis.com).
- Sign in with your ArcGIS developer account.
- Create an application. This will give you access to a client id string.
- Initialize the ```CLIENT_ID``` constant with the client id string and run the sample. If the license level has been successfully set to Basic you won't see a watermark on the map.

**NOTE:** When you release your app, you should ensure that the client id is encrypted and saved to the device in a secure manner; this sample uses a hardcoded string instead for simplicity of example code. 