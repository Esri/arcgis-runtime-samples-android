package com.esri.arcgisruntime.sample.viewshedlocation;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.esri.arcgisruntime.geoanalysis.LocationViewshed;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.layers.ArcGISSceneLayer;
import com.esri.arcgisruntime.mapping.ArcGISScene;
import com.esri.arcgisruntime.mapping.ArcGISTiledElevationSource;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Surface;
import com.esri.arcgisruntime.mapping.view.AnalysisOverlay;
import com.esri.arcgisruntime.mapping.view.Camera;
import com.esri.arcgisruntime.mapping.view.SceneView;

public class MainActivity extends AppCompatActivity {

  private SceneView mSceneView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // create a scene and add a basemap to it
    mSceneView = findViewById(R.id.sceneView);
    ArcGISScene scene = new ArcGISScene();
    scene.setBasemap(Basemap.createImagery());
    mSceneView.setScene(scene);

    // add base surface for elevation data
    Surface surface = new Surface();
    final String localElevationImageService = getString(com.esri.arcgisruntime.sample.viewshedlocation.R.string.elevation_service);
    surface.getElevationSources().add(new ArcGISTiledElevationSource(localElevationImageService));
    scene.setBaseSurface(surface);

    // add a scene layer
    final String buildings = getString(com.esri.arcgisruntime.sample.viewshedlocation.R.string.buildings_layer);
    ArcGISSceneLayer sceneLayer = new ArcGISSceneLayer(buildings);
    scene.getOperationalLayers().add(sceneLayer);

    // create a viewshed from the camera
    Point location = new Point(-4.50, 48.4,100.0);
    LocationViewshed viewshed = new LocationViewshed(location, headingSlider.getValue(), pitchSlider.getValue(),
        horizontalAngleSlider.getValue(), verticalAngleSlider.getValue(), minDistanceSlider.getValue(),
        maxDistanceSlider.getValue());

    // set the camera
    Camera camera = new Camera(location, 200.0, 20.0, 70.0, 0.0);
    mSceneView.setViewpointCamera(camera);

    // create an analysis overlay to add the viewshed to the scene view
    AnalysisOverlay analysisOverlay = new AnalysisOverlay();
    analysisOverlay.getAnalyses().add(viewshed);
    mSceneView.getAnalysisOverlays().add(analysisOverlay);

  }
}
