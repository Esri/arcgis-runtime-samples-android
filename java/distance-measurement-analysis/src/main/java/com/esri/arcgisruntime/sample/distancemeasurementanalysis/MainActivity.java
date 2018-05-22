/* Copyright 2017 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.esri.arcgisruntime.sample.distancemeasurementanalysis;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.esri.arcgisruntime.UnitSystem;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.geoanalysis.LocationDistanceMeasurement;
import com.esri.arcgisruntime.geometry.Distance;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.ArcGISSceneLayer;
import com.esri.arcgisruntime.mapping.ArcGISScene;
import com.esri.arcgisruntime.mapping.ArcGISTiledElevationSource;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Surface;
import com.esri.arcgisruntime.mapping.view.AnalysisOverlay;
import com.esri.arcgisruntime.mapping.view.Camera;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.DefaultSceneViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.SceneView;
import com.esri.arcgisruntime.symbology.SceneSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSceneSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

  private SceneView mSceneView;
  private TextView mDirectDistance;
  private TextView mVerticalDistance;
  private TextView mHorizontalDistance;
  private Spinner mUnitSpinner;
  boolean firstClick = true;
  private Graphic sphereGraphic;

  private LocationDistanceMeasurement distanceMeasurement;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // create a scene and add a basemap to it
    final ArcGISScene scene = new ArcGISScene();
    scene.setBasemap(Basemap.createImagery());

    // create views from layout
    mSceneView = findViewById(R.id.sceneView);
    mDirectDistance = findViewById(R.id.total_distance);
    mHorizontalDistance = findViewById(R.id.horizontal_distance);
    mVerticalDistance = findViewById(R.id.vertical_distance);
    mUnitSpinner = findViewById(R.id.units_spinner);
    mSceneView.setScene(scene);


    // add base surface for elevation data
    Surface surface = new Surface();
    surface.getElevationSources().add(new ArcGISTiledElevationSource("http://elevation3d.arcgis" +
        ".com/arcgis/rest/services/WorldElevation3D/Terrain3D/ImageServer"));
    surface.getElevationSources().add(new ArcGISTiledElevationSource(
        "https://tiles.arcgis.com/tiles/d3voDfTFbHOCRwVR/arcgis/rest/services/MNT_IDF/ImageServer"));
    scene.setBaseSurface(surface);

    final String buildings = "http://tiles.arcgis.com/tiles/P3ePLMYs2RVChkJx/arcgis/rest/services/Buildings_Brest/SceneServer/layers/0";
    ArcGISSceneLayer sceneLayer = new ArcGISSceneLayer(buildings);
    scene.getOperationalLayers().add(sceneLayer);

    // create analysis overlay and add it to scene
    AnalysisOverlay analysisOverlay = new AnalysisOverlay();
    mSceneView.getAnalysisOverlays().add(analysisOverlay);


    //initialize a distance measurement and add it to the analyssis overlay
    Point start = new Point(-4.494677, 48.384472, 24.772694, SpatialReferences.getWgs84());
    Point end = new Point(-4.495646, 48.384377, 58.501115, SpatialReferences.getWgs84());
    double size = 10;

    distanceMeasurement = new LocationDistanceMeasurement(start, end);
    analysisOverlay.getAnalyses().add(distanceMeasurement);

    // zoom to initial measurement
    Camera camera = new Camera(start, 200.0, 0.0, 45.0, 0.0);
    mSceneView.setViewpointCamera(camera);

    ArrayList<String> unitsList = new ArrayList<>();
    for(UnitSystem unitSystemItem :UnitSystem.values()){
      unitsList.add(unitSystemItem.toString());
    }

    // set up drop-down
    ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this,android.R.layout.simple_spinner_item,
        unitsList);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    mUnitSpinner.setAdapter(adapter);
    mUnitSpinner.setSelection(1);

    mUnitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        switch (i){
          case 0: distanceMeasurement.setUnitSystem(UnitSystem.IMPERIAL);
            break;
          case 1: distanceMeasurement.setUnitSystem(UnitSystem.METRIC);
            break;
        }
      }

      @Override public void onNothingSelected(AdapterView<?> adapterView) { }
    });

    // show the distances in the UI when the measurement changes
    final DecimalFormat decimalFormat = new DecimalFormat("#.##");
    distanceMeasurement.addMeasurementChangedListener(measurementChangedEvent -> {
      Distance directDistance = distanceMeasurement.getDirectDistance();
      Distance verticalDistance = distanceMeasurement.getVerticalDistance();
      Distance horizonalDistance = distanceMeasurement.getHorizontalDistance();

      mDirectDistance.setText(String.format("%s %s",
          decimalFormat.format(directDistance.getValue()), directDistance.getUnit().getAbbreviation()));
      mHorizontalDistance.setText(String.format("%s %s",
          decimalFormat.format(verticalDistance.getValue()), directDistance.getUnit().getAbbreviation()));
      mVerticalDistance.setText(String.format("%s %s",
          decimalFormat.format(horizonalDistance.getValue()), directDistance.getUnit().getAbbreviation()));
    });


    // add onTouchListener to get the location of the user tap
    mSceneView.setOnTouchListener(new DefaultSceneViewOnTouchListener(mSceneView){

      @Override
      public boolean onSingleTapConfirmed(MotionEvent motionEvent){

        // convert from screen point to location point
        android.graphics.Point screenPoint = new android.graphics.Point(Math.round(motionEvent.getX()),
            Math.round(motionEvent.getY()));
        ListenableFuture<Point> locationPointFuture = mSceneView.screenToLocationAsync(screenPoint);
        locationPointFuture.addDoneListener(() ->{

          try {
            Point location = locationPointFuture.get();
            distanceMeasurement.setStartLocation(location);

          } catch (InterruptedException | ExecutionException e ) {
            String error = "Error converting screen point to location point: " + e.getMessage();
            Log.e(MainActivity.this.toString(), error);
            Toast.makeText(MainActivity.this, error, Toast.LENGTH_LONG).show();
          }
        });
        return true;
      }

      // handles the double tap
      @Override
      public boolean onDoubleTouchDrag(MotionEvent motionEvent) {

        // convert from screen point to location point
        android.graphics.Point screenPoint = new android.graphics.Point(Math.round(motionEvent.getX()),
            Math.round(motionEvent.getY()));
        ListenableFuture<Point> locationPointFuture = mSceneView.screenToLocationAsync(screenPoint);
        locationPointFuture.addDoneListener(() ->{

          try {
            Point location = locationPointFuture.get();
            distanceMeasurement.setEndLocation(location);

          } catch (InterruptedException | ExecutionException e ) {
            e.printStackTrace();
          }
        });
        return true;
      }

    });
  }
  @Override
  protected void onPause() {
    super.onPause();
    // pause SceneView
    mSceneView.pause();
  }

  @Override
  protected void onResume() {
    super.onResume();
    // resume SceneView
    mSceneView.resume();
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    // dispose SceneView
    mSceneView.dispose();
  }
}
