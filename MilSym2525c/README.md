# Mil2525c Sample
The purpose of this sample is to introduce you to the Military Symbology features in the API. For any Military Symbology apps you need a Symbol Dictionary and the default Message Types on your device in the following default location **/<external-storage-directory>/ArcGIS/SymbolDictionary**. This is where the ```MessageProcess``` class will look for the Symbol Dictionary resources. To use a custom path you need to use the ```MessageProcessor``` overloaded constructor which takes in a ```java.lang.String``` Symbol Dictionary path.

## Sample Design
This sample illustrates the use of ```MessageProcessor``` class to process Military 2525c Symbology features whose definitions are stored locally on your device. The ```MessageProcessor``` class provides the capability to take a message received from an external source and convert it into a symbol which is displayed on a map in a ```GraphicsLayer```. The ```MessageProcessor``` class requires a ```GroupLayer``` to be added to an initialized ```MapView```.

The ```MessageProcessor``` constructor requires a ```DictionaryType``` which indicates the type of symbol dictionary is being used. The Message class encapsulates information from an incoming or outgoing message. Apart from a message ID all other properties in the message are held in name-value pairs.

This sample draws samples based on an XML file located in **/res/raw/coa.xml**.  You can add any supported symbols to this file to add more complex symbols.  

## Provision your device

1. The symbol dictionary resource files are included with your local SDK install at **/resources/mil2525c**.
2. Create a folder on your device named **/<external-storage-directory>/ArcGIS/SymbolDictionary**.  Please refer to the [Android SDK adb tool](http://developer.android.com/tools/help/adb.html) for instructions on how to create folders on your device. 
3. Open up a command prompt and execute the adb shell command to start a remote shell on your target device.
4. Navigate to your sdcard directory, **cd /sdcard/**.
5. Create the ArcGIS directory, **mkdir ArcGIS/SymbolDictionary**.
6. You should now have a directory similar on your target device, **/mnt/sdcard/ArcGIS/SymbolDictionary**. We will copy the contents of the resource into this directory.
7. Exit the shell with the, exit command.
8. While still in your command prompt, navigate to your local SDK install at **/resources/mil2525c** folder and execute the following command:
        
 ```$ adb push mil2525c /sdcard/ArcGIS/SymbolDictionary```
