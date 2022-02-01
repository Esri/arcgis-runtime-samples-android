/* Copyright 2019 Esri
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

package com.esri.arcgisruntime.sample.getelevationatpoint;

import java.util.concurrent.ExecutionException;

import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.mapping.ArcGISScene;
import com.esri.arcgisruntime.mapping.ArcGISTiledElevationSource;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.view.Camera;
import com.esri.arcgisruntime.mapping.view.DefaultSceneViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.SceneView;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private SceneView mSceneView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // authentication with an API key or named user is required to access basemaps and other
    // location services
    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY);

    // create a scene and add a basemap to it
    ArcGISScene scene = new ArcGISScene(BasemapStyle.ARCGIS_IMAGERY);

    // get a reference to the scene view and set the scene to it
    mSceneView = findViewById(R.id.sceneView);
    mSceneView.setScene(scene);

    // create an elevation source, and add this to the base surface of the scene
    ArcGISTiledElevationSource elevationSource = new ArcGISTiledElevationSource(
        getString(R.string.elevation_image_service));
    scene.getBaseSurface().getElevationSources().add(elevationSource);

    // create a point symbol to mark where elevation is being measured
    SimpleMarkerSymbol circleSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.RED, 10);

    // create a graphics overlay
    GraphicsOverlay graphicsOverlay = new GraphicsOverlay(GraphicsOverlay.RenderingMode.DYNAMIC);
    mSceneView.getGraphicsOverlays().add(graphicsOverlay);

    // add a camera and initial camera position
    Camera camera = new Camera(28.42, 83.9, 10000.0, 10.0, 80.0, 0.0);
    mSceneView.setViewpointCamera(camera);

    // create a touch listener to handle taps
    mSceneView.setOnTouchListener(new DefaultSceneViewOnTouchListener(mSceneView) {
      @Override public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
        // clear any existing graphics from the graphics overlay
        graphicsOverlay.getGraphics().clear();
        // get the tapped screen point
        android.graphics.Point screenPoint = new android.graphics.Point(Math.round(motionEvent.getX()),
            Math.round(motionEvent.getY()));
        // convert the screen point to a point on the surface
        Point surfacePoint = mSceneView.screenToBaseSurface(screenPoint);
        if (surfacePoint == null) {
          Toast.makeText(MainActivity.this, "Cannot get an elevation for a point which is not on the surface.",
              Toast.LENGTH_SHORT).show();
          return super.onSingleTapConfirmed(motionEvent);
        }
        // create a new graphic at the surface point and add it to the graphics overlay
        Graphic surfacePointGraphic = new Graphic(surfacePoint, circleSymbol);
        graphicsOverlay.getGraphics().add(surfacePointGraphic);

        // get the surface elevation at the surface point
        ListenableFuture<Double> elevationFuture = scene.getBaseSurface().getElevationAsync(surfacePoint);
        elevationFuture.addDoneListener(() -> {
          try {
            Double elevation = elevationFuture.get();
            String elevationMessage = "Elevation at tapped point: " + Math.round(elevation) + 'm';
            Toast.makeText(MainActivity.this, elevationMessage, Toast.LENGTH_LONG).show();
            Log.i(TAG, elevationMessage);
          } catch (ExecutionException | InterruptedException e) {
            String error = "Error getting elevation: " + e.getMessage();
            Toast.makeText(MainActivity.this, error, Toast.LENGTH_LONG).show();
            Log.e(TAG, error);
          }
        });

        return super.onSingleTapConfirmed(motionEvent);
      }
    });
  }

  @Override
  protected void onPause() {
    mSceneView.pause();
    super.onPause();
  }

  @Override
  protected void onResume() {
    super.onResume();
    mSceneView.resume();
  }

  @Override protected void onDestroy() {
    mSceneView.dispose();
    super.onDestroy();
  }
}
