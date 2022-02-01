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

package com.esri.arcgisruntime.sample.locationlineofsight;

import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MotionEvent;
import android.widget.Toast;

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.geoanalysis.LineOfSight;
import com.esri.arcgisruntime.geoanalysis.LocationLineOfSight;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.mapping.ArcGISScene;
import com.esri.arcgisruntime.mapping.ArcGISTiledElevationSource;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.view.AnalysisOverlay;
import com.esri.arcgisruntime.mapping.view.Camera;
import com.esri.arcgisruntime.mapping.view.DefaultSceneViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.SceneView;

public class MainActivity extends AppCompatActivity {

  private final double mZOffset = 2.0;
  private SceneView mSceneView;
  private AnalysisOverlay mAnalysisOverlay;
  private Point mObserverLocation;
  private Point mTargetLocation;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // authentication with an API key or named user is required to access basemaps and other
    // location services
    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY);

    // create a scene and add a basemap to it
    ArcGISScene scene = new ArcGISScene(BasemapStyle.ARCGIS_IMAGERY);

    // create SceneView from layout
    mSceneView = findViewById(R.id.sceneView);
    mSceneView.setScene(scene);

    // create an elevation source, and add this to the base surface of the scene
    ArcGISTiledElevationSource elevationSource = new ArcGISTiledElevationSource(
        getString(R.string.elevation_image_service));
    scene.getBaseSurface().getElevationSources().add(elevationSource);

    // create an analysis overlay to contain the analysis and add it to the scene view
    mAnalysisOverlay = new AnalysisOverlay();
    mSceneView.getAnalysisOverlays().add(mAnalysisOverlay);

    // add a camera and initial camera position
    Camera camera = new Camera(new Point(-73.10861935949697, -49.25758493899104, 3050), 106, 73, 0);
    mSceneView.setViewpointCamera(camera);

    // set the visible and obstructed colors (default would be green/red)
    // these are static properties that apply to all line of sight analyses in the scene view
    LineOfSight.setVisibleColor(Color.CYAN);
    LineOfSight.setObstructedColor(Color.MAGENTA);

    mSceneView.setOnTouchListener(new DefaultSceneViewOnTouchListener(mSceneView) {
      @Override public boolean onSingleTapConfirmed(MotionEvent motionEvent) {

        // get a screen point from the motion event
        android.graphics.Point screenPoint = new android.graphics.Point(Math.round(motionEvent.getX()),
            Math.round(motionEvent.getY()));

        // convert the screen point to a scene point
        Point scenePoint = mSceneView.screenToBaseSurface(screenPoint);

        // check that the converted point is actually on the surface of the globe
        if (scenePoint != null) {

          if (mObserverLocation == null) {
            // set the observer location
            mObserverLocation = new Point(scenePoint.getX(), scenePoint.getY(), scenePoint.getZ() + mZOffset,
                scenePoint.getSpatialReference());
          } else {
            // set the target location
            mTargetLocation = new Point(scenePoint.getX(), scenePoint.getY(), scenePoint.getZ() + mZOffset,
                scenePoint.getSpatialReference());

            // create a new line of sight analysis with observer and target locations
            LocationLineOfSight locationLineOfSight = new LocationLineOfSight(mObserverLocation, mTargetLocation);

            // add the line of sight analysis to the analysis overlay
            mAnalysisOverlay.getAnalyses().add(locationLineOfSight);
          }
        } else {
          Toast.makeText(MainActivity.this, "Please select a point on the surface.", Toast.LENGTH_LONG).show();
        }
        return super.onSingleTapConfirmed(motionEvent);
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

  @Override
  protected void onDestroy() {
    super.onDestroy();
    // dispose SceneView
    mSceneView.dispose();
  }
}
