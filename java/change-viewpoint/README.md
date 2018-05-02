# Change Viewpoint
### Category: MapViews, SceneViews and UI
This sample demonstrates different ways to change the `ViewPoint` of a map. 

![Change Viewpoint](change-viewpoint.png)

Three methods are demonstrated to change the view point namely:

* Geometry: Zoom to a real-world feature - Griffith Park by creating geometry from JSON file
 
* Center and Scale: Zoom to Waterloo

* Animation: Set the map views's viewpoint to London with a ten second duration  

# Features

* MapView
* SpatialReference
* setViewpointCenterWithScaleAsync
* setViewpointGeometryAsync
* setViewpointWithDurationAsync
* setViewpointCenterWithScaleAsync

# Developer Pattern

```java
mAnimateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // create the London location point
                Point londonPoint = new Point(28677947.756181,22987445.6186465, spatialReference);
                // create the viewpoint with the London point and scale
                Viewpoint viewpoint = new Viewpoint(londonPoint, SCALE);
                // set the map views's viewpoint to London with a ten second duration
                mMapView.setViewpointWithDurationAsync(viewpoint, 10);
            }
        });
        
mCenterScaleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // create the Waterloo location point
                Point waterlooPoint = new Point(28681235.9843606, 22990575.7224154, spatialReference);
                // set the map views's viewpoint centered on Waterloo and scaled
                mMapView.setViewpointCenterWithScaleAsync(waterlooPoint, SCALE);
            }
        });
```
