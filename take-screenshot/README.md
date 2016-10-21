# Take Screenshot
The purpose of this sample is to demonstrate how to export the map as an image file. ```MapView.exportImageAsync``` exports the map to **Bitmap** format which can be saved in any image file format.

![Take Screenshot](take-screenshot.png)

## Features

* Map
* MapView
* ExportImageAsync

## Developer Pattern

```java
// export the image from the mMapView
        final ListenableFuture<Bitmap> export = mMapView.exportImageAsync();
        export.addDoneListener(new Runnable() {
            @Override
            public void run() {
                try {
                    Bitmap currentMapImage = export.get();
                    // play the camera shutter sound
                    MediaActionSound sound = new MediaActionSound();
                    sound.play(MediaActionSound.SHUTTER_CLICK);
                    Log.d(TAG,"Captured the image!!");
                    // save the exported bitmap to an image file
                    SaveImageTask saveImageTask = new SaveImageTask();
                    saveImageTask.execute(currentMapImage);
                } catch (Exception e) {
                    Log.d(TAG, "Fail to export map image: " + e.getMessage());
                }
            }
        });
    }
    
```
