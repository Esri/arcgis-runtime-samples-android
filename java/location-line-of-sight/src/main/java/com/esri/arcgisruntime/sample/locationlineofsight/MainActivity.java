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
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import com.esri.arcgisruntime.geoanalysis.LineOfSight;
import com.esri.arcgisruntime.geoanalysis.LocationLineOfSight;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.mapping.ArcGISScene;
import com.esri.arcgisruntime.mapping.ArcGISTiledElevationSource;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.AnalysisOverlay;
import com.esri.arcgisruntime.mapping.view.Camera;
import com.esri.arcgisruntime.mapping.view.DefaultSceneViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.SceneView;

public class MainActivity extends AppCompatActivity {

  private SceneView mSceneView;
  private LocationLineOfSight mLineOfSightAnalysis;
  private Point mObserverLocation;
  private Point mTargetLocation;

  private double mZOffset = 2.0;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // create a scene and add a basemap to it
    ArcGISScene scene = new ArcGISScene();
    scene.setBasemap(Basemap.createImagery());

    // create SceneView from layout
    mSceneView = findViewById(R.id.sceneView);
    mSceneView.setScene(scene);

    // create an elevation source, and add this to the base surface of the scene
    ArcGISTiledElevationSource elevationSource = new ArcGISTiledElevationSource(
        getString(R.string.elevation_image_service));
    scene.getBaseSurface().getElevationSources().add(elevationSource);

    // add a camera and initial camera position
    Camera camera = new Camera(28.4, 83.9, 10010.0, 10.0, 80.0, 0);
    mSceneView.setViewpointCamera(camera);

    // Create a new line of sight analysis with arbitrary points (observer and target will be defined by the user)
    mLineOfSightAnalysis = new LocationLineOfSight(new Point(0.0, 0.0, SpatialReferences.getWgs84()),
        new Point(0.0, 0.0, SpatialReferences.getWgs84()));

    // Set the visible and obstructed colors (default would be green/red)
    // These are static properties that apply to all line of sight analyses in the scene view
    LineOfSight.setVisibleColor(Color.CYAN);
    LineOfSight.setObstructedColor(Color.MAGENTA);

    mSceneView.setOnTouchListener(new DefaultSceneViewOnTouchListener(mSceneView) {
      @Override public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
        // get a screen point from the motion event
        android.graphics.Point screenPoint = new android.graphics.Point(Math.round(motionEvent.getX()),
            Math.round(motionEvent.getY()));
        // convert the screen point to a scene point
        Point scenePointFuture = mSceneView.screenToBaseSurface(screenPoint);
        if (mObserverLocation == null) {
          // set the observer location
          mObserverLocation = scenePointFuture;
          Log.d("obs", mObserverLocation.toString());
          mLineOfSightAnalysis.setObserverLocation(
              new Point(mObserverLocation.getX(), mObserverLocation.getY(), mObserverLocation.getZ() + mZOffset,
                  mObserverLocation.getSpatialReference()));
        } else if (mTargetLocation == null) {
          // set the target location
          mTargetLocation = scenePointFuture;
          mLineOfSightAnalysis.setTargetLocation(
              new Point(mTargetLocation.getX(), mTargetLocation.getY(), mTargetLocation.getZ() + mZOffset,
                  mTargetLocation.getSpatialReference()));
          Log.d("tar", mTargetLocation.toString());
          // Create an analysis overlay to contain the analysis and add it to the scene view
          AnalysisOverlay lineOfSightOverlay = new AnalysisOverlay();
          lineOfSightOverlay.getAnalyses().add(mLineOfSightAnalysis);
          mSceneView.getAnalysisOverlays().add(lineOfSightOverlay);
        } else {
          // reset observer and target locations
          Toast.makeText(getApplicationContext(), "Resetting observer and target locations", Toast.LENGTH_SHORT)
              .show();
          mObserverLocation = null;
          mLineOfSightAnalysis = null;
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
}
