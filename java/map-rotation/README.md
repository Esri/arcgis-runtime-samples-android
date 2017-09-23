# Map Rotation
This sample demonstrates how to rotate a map on your Android device using an Android `SeekBar`.

![map-rotation](map-rotation.png)

# Features
* ArcGISMap
* Basemap
* MapView

# Developer Pattern
Create a `SeekBar` in the layout setting the max value to `360`. 

```xml
<SeekBar
    android:id="@+id/rotationSeekBar"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_alignParentBottom="true"
    android:layout_centerHorizontal="true"
    android:layout_marginBottom="40dp"
    android:layout_toLeftOf="@+id/rotationValueText"
    android:max="360"/>
```

Attach the `SeekBar.setOnSeekBarChangeListener` to be notified when the angle value changes and rotate the `MapView`.  

```java
mRotationSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
    @Override
     public void onProgressChanged(SeekBar seekBar, int angle, boolean b) {
        // convert progress to double
        double dAngle = progress;
        // set the text to SeekBar value
        mRotationValueText.setText(String.valueOf(angle));
        // rotate MapView to double angle value
        mMapView.setViewpointRotationAsync(dAngle);
    }
    ...
});
```