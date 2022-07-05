/* Copyright 2018 Esri
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
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
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
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.Surface;
import com.esri.arcgisruntime.mapping.view.AnalysisOverlay;
import com.esri.arcgisruntime.mapping.view.Camera;
import com.esri.arcgisruntime.mapping.view.DefaultSceneViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.SceneView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

  private SceneView mSceneView;

  private TextView mDirectDistance;

  private TextView mVerticalDistance;

  private TextView mHorizontalDistance;

  private LocationDistanceMeasurement distanceMeasurement;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // authentication with an API key or named user is required to access basemaps and other
    // location services
    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY);

    // create a scene and add a basemap to it
    final ArcGISScene scene = new ArcGISScene(BasemapStyle.ARCGIS_TOPOGRAPHIC);

    // inflate views from layout
    mSceneView = findViewById(R.id.sceneView);
    mDirectDistance = findViewById(R.id.totalDistance);
    mHorizontalDistance = findViewById(R.id.horizontalDistance);
    mVerticalDistance = findViewById(R.id.verticalDistance);
    Spinner unitSpinner = findViewById(R.id.units_spinner);
    // set the scene to the view
    mSceneView.setScene(scene);

    // add base surface for elevation data
    Surface surface = new Surface();
    surface.getElevationSources().add(new ArcGISTiledElevationSource(
        "https://scene.arcgis.com/arcgis/rest/services/BREST_DTM_1M/ImageServer"));
    scene.setBaseSurface(surface);

    // add building layer
    ArcGISSceneLayer sceneLayer = new ArcGISSceneLayer(
        "https://tiles.arcgis.com/tiles/P3ePLMYs2RVChkJx/arcgis/rest/services/Buildings_Brest/SceneServer/layers/0");
    // offset for visual purposes
    sceneLayer.setAltitudeOffset(1);
    scene.getOperationalLayers().add(sceneLayer);

    // create analysis overlay and add it to scene
    AnalysisOverlay analysisOverlay = new AnalysisOverlay();
    mSceneView.getAnalysisOverlays().add(analysisOverlay);

    // initialize a distance measurement and add it to the analysis overlay
    Point start = new Point(-4.494677, 48.384472, 24.772694, SpatialReferences.getWgs84());
    Point end = new Point(-4.495646, 48.384377, 58.501115, SpatialReferences.getWgs84());
    distanceMeasurement = new LocationDistanceMeasurement(start, end);
    analysisOverlay.getAnalyses().add(distanceMeasurement);

    // zoom to initial measurement
    Camera camera = new Camera(start, 300.0, 315.0, 60.0, 0.0);
    mSceneView.setViewpointCamera(camera);

    // initialize a list to contain the available units
    ArrayList<String> unitsList = new ArrayList<>();
    for (UnitSystem unitSystemItem : UnitSystem.values()) {
      unitsList.add(unitSystemItem.toString());
    }

    // set up drop-down list
    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
        unitsList);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    unitSpinner.setAdapter(adapter);
    unitSpinner.setSelection(1);

    unitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override public void onItemSelected(AdapterView<?> adapterView, View view, int position,
          long l) {
        switch (position) {
          case 0:
            distanceMeasurement.setUnitSystem(UnitSystem.IMPERIAL);
            break;
          case 1:
            distanceMeasurement.setUnitSystem(UnitSystem.METRIC);
            break;
          default:
            Toast.makeText(MainActivity.this, "Unsupported option", Toast.LENGTH_SHORT).show();
            break;
        }
      }

      @Override public void onNothingSelected(AdapterView<?> adapterView) {
      }
    });

    // show the distances in the UI when the measurement changes
    final DecimalFormat decimalFormat = new DecimalFormat("#.##");
    distanceMeasurement.addMeasurementChangedListener(measurementChangedEvent -> {
      Distance directDistance = distanceMeasurement.getDirectDistance();
      Distance verticalDistance = distanceMeasurement.getVerticalDistance();
      Distance horizontalDistance = distanceMeasurement.getHorizontalDistance();

      mDirectDistance.setText(String.format("%s %s",
          decimalFormat.format(directDistance.getValue()),
          directDistance.getUnit().getAbbreviation()));
      mHorizontalDistance.setText(String.format("%s %s",
          decimalFormat.format(horizontalDistance.getValue()),
          horizontalDistance.getUnit().getAbbreviation()));
      mVerticalDistance.setText(String.format("%s %s",
          decimalFormat.format(verticalDistance.getValue()),
          verticalDistance.getUnit().getAbbreviation()));
    });

    // add onTouchListener to set the start point and end point with a SingleTap and a DoubleTapDrag
    mSceneView.setOnTouchListener(new DefaultSceneViewOnTouchListener(mSceneView) {
      @Override
      public boolean onSingleTapConfirmed(MotionEvent motionEvent) {

        // convert from screen point to location point
        android.graphics.Point screenPoint = new android.graphics.Point(
            Math.round(motionEvent.getX()),
            Math.round(motionEvent.getY()));
        ListenableFuture<Point> locationPointFuture = mSceneView.screenToLocationAsync(screenPoint);
        locationPointFuture.addDoneListener(() -> {

          try {
            Point location = locationPointFuture.get();
            distanceMeasurement.setStartLocation(location);

          } catch (InterruptedException | ExecutionException e) {
            String error = "Error converting screen point to location point ";
            Log.e(MainActivity.this.toString(), error + ":" + e.getMessage());
            Toast.makeText(MainActivity.this, error, Toast.LENGTH_LONG).show();
          }
        });
        return true;
      }

      @Override
      public boolean onDoubleTouchDrag(MotionEvent motionEvent) {

        // convert from screen point to location point
        android.graphics.Point screenPoint = new android.graphics.Point(
            Math.round(motionEvent.getX()),
            Math.round(motionEvent.getY()));
        ListenableFuture<Point> locationPointFuture = mSceneView.screenToLocationAsync(screenPoint);
        locationPointFuture.addDoneListener(() -> {

          try {
            Point location = locationPointFuture.get();
            distanceMeasurement.setEndLocation(location);

          } catch (InterruptedException | ExecutionException e) {
            String error = "Error converting screen point to location point ";
            Log.e(MainActivity.this.toString(), error + ":" + e.getMessage());
            Toast.makeText(MainActivity.this, error, Toast.LENGTH_LONG).show();
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
