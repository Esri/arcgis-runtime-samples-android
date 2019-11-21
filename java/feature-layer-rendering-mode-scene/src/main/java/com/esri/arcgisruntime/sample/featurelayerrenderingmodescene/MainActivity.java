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

package com.esri.arcgisruntime.sample.featurelayerrenderingmodescene;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.mapping.ArcGISScene;
import com.esri.arcgisruntime.mapping.view.Camera;
import com.esri.arcgisruntime.mapping.view.DefaultSceneViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.SceneView;

public class MainActivity extends AppCompatActivity {

  private SceneView mSceneViewTop;
  private SceneView mSceneViewBottom;
  private Camera mZoomedIn;
  private Camera mZoomedOut;
  private Button mZoomButton;
  private TextView mNavigatingTextView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    Point zoomedOutPoint = new Point(-118.37, 34.46, SpatialReferences.getWgs84());
    Point zoomedInPoint = new Point(-118.45, 34.395, SpatialReferences.getWgs84());

    // define viewpoints
    mZoomedOut = new Camera(zoomedOutPoint, 42000, 0, 0, 0);
    mZoomedIn = new Camera(zoomedInPoint, 2500, 90, 75, 0);

    // inflate the zoom button
    mZoomButton = findViewById(R.id.zoomButton);

    // inflate SceneViews from layout
    mSceneViewTop = findViewById(R.id.sceneViewTop);
    mSceneViewBottom = findViewById(R.id.sceneViewBottom);

    // inflate navigating text view
    mNavigatingTextView = findViewById(R.id.isNavigatingTextView);
    mNavigatingTextView.setVisibility(View.INVISIBLE);

    // create a scene (top) and set it to render all features in static rendering mode
    ArcGISScene sceneTop = new ArcGISScene();
    sceneTop.getLoadSettings().setPreferredPointFeatureRenderingMode(FeatureLayer.RenderingMode.STATIC);
    sceneTop.getLoadSettings().setPreferredPolylineFeatureRenderingMode(FeatureLayer.RenderingMode.STATIC);
    sceneTop.getLoadSettings().setPreferredPolygonFeatureRenderingMode(FeatureLayer.RenderingMode.STATIC);

    // create a scene (bottom) and set it to render all features in dynamic rendering mode
    ArcGISScene sceneBottom = new ArcGISScene();
    sceneBottom.getLoadSettings().setPreferredPointFeatureRenderingMode(FeatureLayer.RenderingMode.DYNAMIC);
    sceneBottom.getLoadSettings().setPreferredPolylineFeatureRenderingMode(FeatureLayer.RenderingMode.DYNAMIC);
    sceneBottom.getLoadSettings().setPreferredPolygonFeatureRenderingMode(FeatureLayer.RenderingMode.DYNAMIC);

    // create the service feature table
    ServiceFeatureTable faultServiceFeatureTable = new ServiceFeatureTable(
        getResources().getString(R.string.energy_geology_feature_service) + "0");
    ServiceFeatureTable contactsServiceFeatureTable = new ServiceFeatureTable(
        getResources().getString(R.string.energy_geology_feature_service) + "8");
    ServiceFeatureTable outcropServiceFeatureTable = new ServiceFeatureTable(
        getResources().getString(R.string.energy_geology_feature_service) + "9");

    // create the feature layer using the service feature table
    FeatureLayer faultFeatureLayer = new FeatureLayer(faultServiceFeatureTable);
    FeatureLayer contactsFeatureLayer = new FeatureLayer(contactsServiceFeatureTable);
    FeatureLayer outcropFeatureLayer = new FeatureLayer(outcropServiceFeatureTable);

    // add the feature layers to the scenes
    sceneTop.getOperationalLayers().add(faultFeatureLayer);
    sceneTop.getOperationalLayers().add(contactsFeatureLayer);
    sceneTop.getOperationalLayers().add(outcropFeatureLayer);
    sceneBottom.getOperationalLayers().add(faultFeatureLayer.copy());
    sceneBottom.getOperationalLayers().add(contactsFeatureLayer.copy());
    sceneBottom.getOperationalLayers().add(outcropFeatureLayer.copy());

    mSceneViewTop.setScene(sceneTop);
    mSceneViewBottom.setScene(sceneBottom);

    mSceneViewTop.setViewpointCamera(mZoomedOut);
    mSceneViewBottom.setViewpointCamera(mZoomedOut);

    mZoomButton.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        animatedZoom();
      }
    });

    // disable the top scene view on touch listener
    mSceneViewTop.setOnTouchListener(new DefaultSceneViewOnTouchListener(mSceneViewTop) {
      @Override public boolean onTouch(View v, MotionEvent event) {
        return false;
      }
    });

    // disable the bottom scene view on touch listener
    mSceneViewBottom.setOnTouchListener(new DefaultSceneViewOnTouchListener(mSceneViewBottom) {
      @Override public boolean onTouch(View v, MotionEvent event) {
        return false;
      }
    });
  }

  /**
   * Controls animated zoom and updates the 'navigating' text view.
   */
  private void animatedZoom() {
    mZoomButton.setClickable(false);
    mNavigatingTextView.setVisibility(View.VISIBLE);
    zoomTo(mZoomedIn, 5).addDoneListener(new Runnable() {
      @Override public void run() {
        mNavigatingTextView.setVisibility(View.INVISIBLE);
        zoomTo(mZoomedIn, 3).addDoneListener(new Runnable() {
          @Override public void run() {
            mNavigatingTextView.setVisibility(View.VISIBLE);
            zoomTo(mZoomedOut, 5).addDoneListener(new Runnable() {
              @Override public void run() {
                mZoomButton.setClickable(true);
                mNavigatingTextView.setVisibility(View.INVISIBLE);
              }
            });
          }
        });
      }
    });
  }

  /**
   * Sets both SceneViews to a ViewpointCamera over a number of seconds.
   *
   * @param camera to which both SceneViews should be set.
   * @param seconds over which the Viewpoint is asynchronously set.
   *
   * @return a ListenableFuture representing the result of the Viewpoint change.
   */
  private ListenableFuture<Boolean> zoomTo(Camera camera, int seconds) {
    ListenableFuture<Boolean> setViewpointFuture = mSceneViewTop.setViewpointCameraAsync(camera, seconds);
    mSceneViewBottom.setViewpointCameraAsync(camera, seconds);
    return setViewpointFuture;
  }

  @Override
  protected void onPause() {
    super.onPause();
    mSceneViewTop.pause();
    mSceneViewBottom.pause();
  }

  @Override
  protected void onResume() {
    super.onResume();
    mSceneViewTop.resume();
    mSceneViewBottom.resume();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mSceneViewTop.dispose();
    mSceneViewBottom.dispose();
  }
}
