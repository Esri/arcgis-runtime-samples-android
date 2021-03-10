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

package com.esri.arcgisruntime.scenelayerselection;

import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.layers.ArcGISSceneLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISScene;
import com.esri.arcgisruntime.mapping.ArcGISTiledElevationSource;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.GeoElement;
import com.esri.arcgisruntime.mapping.Surface;
import com.esri.arcgisruntime.mapping.view.Camera;
import com.esri.arcgisruntime.mapping.view.DefaultSceneViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.IdentifyLayerResult;
import com.esri.arcgisruntime.mapping.view.SceneView;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private SceneView mSceneView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // create a scene and add a basemap to it
    ArcGISScene scene = new ArcGISScene();
    scene.setBasemap(Basemap.createTopographic());

    // set the scene to the scene view
    mSceneView = findViewById(R.id.sceneView);
    mSceneView.setScene(scene);

    // add base surface with elevation data
    Surface surface = new Surface();
    final String elevationService = getString(R.string.world_elevation_url);
    surface.getElevationSources().add(new ArcGISTiledElevationSource(elevationService));
    scene.setBaseSurface(surface);

    // add a scene layer of Brest buildings to the scene
    final String buildings = getString(R.string.brest_buildings);
    ArcGISSceneLayer sceneLayer = new ArcGISSceneLayer(buildings);
    scene.getOperationalLayers().add(sceneLayer);

    // add a camera and initial camera position
    Camera camera = new Camera(48.378, -4.494, 200, 345, 65, 0);
    mSceneView.setViewpointCamera(camera);

    // zoom to the layer's extent when loaded
    sceneLayer.addDoneLoadingListener(() -> {
      if (sceneLayer.getLoadStatus() == LoadStatus.LOADED) {

        // when the scene is clicked, identify the clicked feature and select it
        mSceneView.setOnTouchListener(new DefaultSceneViewOnTouchListener(mSceneView) {

          @Override public boolean onSingleTapConfirmed(MotionEvent motionEvent) {

            // clear any previous selection
            sceneLayer.clearSelection();

            android.graphics.Point screenPoint = new android.graphics.Point(Math.round(motionEvent.getX()),
                Math.round(motionEvent.getY()));
            // identify clicked feature
            ListenableFuture<IdentifyLayerResult> identify = mSceneView
                .identifyLayerAsync(sceneLayer, screenPoint, 10, false, 1);
            identify.addDoneListener(() -> {
              try {
                // get the identified result and check that it is a feature
                IdentifyLayerResult result = identify.get();
                List<GeoElement> geoElements = result.getElements();
                if (!geoElements.isEmpty()) {
                  Log.d(TAG, "geoelement not empty");
                  GeoElement geoElement = geoElements.get(0);
                  if (geoElement instanceof Feature) {
                    // select the feature
                    sceneLayer.selectFeature((Feature) geoElement);
                  }
                }
              } catch (InterruptedException | ExecutionException e) {
                String error = "Error while identifying layer result: " + e.getMessage();
                Log.e(TAG, error);
                Toast.makeText(MainActivity.this, error, Toast.LENGTH_LONG).show();
              }
            });
            return true;
          }
        });
      } else if (sceneLayer.getLoadStatus() == LoadStatus.FAILED_TO_LOAD) {
        String error = "Error loading scene layer " + sceneLayer.getLoadStatus();
        Log.e(TAG, error);
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
      }
    });
  }

  @Override
  protected void onPause() {
    super.onPause();
    mSceneView.pause();
  }

  @Override
  protected void onResume() {
    super.onResume();
    mSceneView.resume();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mSceneView.dispose();
  }
}
