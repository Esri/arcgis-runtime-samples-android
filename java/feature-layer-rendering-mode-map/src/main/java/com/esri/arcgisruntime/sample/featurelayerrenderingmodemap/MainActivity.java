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

package com.esri.arcgisruntime.sample.featurelayerrenderingmodemap;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.MapView;

public class MainActivity extends AppCompatActivity {

  private MapView mMapViewTop;
  private MapView mMapViewBottom;
  private Viewpoint mZoomedIn;
  private Viewpoint mZoomedOut;
  private Button mZoomButton;
  private TextView mNavigatingTextView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    Point centerPoint = new Point(-118.37, 34.45, SpatialReferences.getWgs84());

    // define viewpoints
    mZoomedOut = new Viewpoint(centerPoint, 650000, 0);
    mZoomedIn = new Viewpoint(centerPoint, 50000, 90);

    // inflate the zoom button
    mZoomButton = findViewById(R.id.zoomButton);

    // inflate MapViews from layout
    mMapViewTop = findViewById(R.id.mapViewTop);
    mMapViewBottom = findViewById(R.id.mapViewBottom);

    // inflate navigating text view
    mNavigatingTextView = findViewById(R.id.isNavigatingTextView);
    mNavigatingTextView.setVisibility(View.INVISIBLE);

    // create a map (top) and set it to render all features in static rendering mode
    ArcGISMap mapTop = new ArcGISMap();
    mapTop.getLoadSettings().setPreferredPointFeatureRenderingMode(FeatureLayer.RenderingMode.STATIC);
    mapTop.getLoadSettings().setPreferredPolylineFeatureRenderingMode(FeatureLayer.RenderingMode.STATIC);
    mapTop.getLoadSettings().setPreferredPolygonFeatureRenderingMode(FeatureLayer.RenderingMode.STATIC);

    // create a map (bottom) and set it to render all features in dynamic rendering mode
    ArcGISMap mapBottom = new ArcGISMap();
    mapBottom.getLoadSettings().setPreferredPointFeatureRenderingMode(FeatureLayer.RenderingMode.DYNAMIC);
    mapBottom.getLoadSettings().setPreferredPolylineFeatureRenderingMode(FeatureLayer.RenderingMode.DYNAMIC);
    mapBottom.getLoadSettings().setPreferredPolygonFeatureRenderingMode(FeatureLayer.RenderingMode.DYNAMIC);

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

    // add the feature layers to the map
    mapTop.getOperationalLayers().add(faultFeatureLayer);
    mapTop.getOperationalLayers().add(contactsFeatureLayer);
    mapTop.getOperationalLayers().add(outcropFeatureLayer);
    mapBottom.getOperationalLayers().add(faultFeatureLayer.copy());
    mapBottom.getOperationalLayers().add(contactsFeatureLayer.copy());
    mapBottom.getOperationalLayers().add(outcropFeatureLayer.copy());

    mMapViewTop.setMap(mapTop);
    mMapViewTop.setViewpoint(mZoomedOut);
    mMapViewBottom.setMap(mapBottom);
    mMapViewBottom.setViewpoint(mZoomedOut);

    mZoomButton.setOnClickListener(v -> animatedZoom());

    // disable the top map view on touch listener
    mMapViewTop.setOnTouchListener(new DefaultMapViewOnTouchListener(this, mMapViewTop) {
      @Override public boolean onTouch(View v, MotionEvent event) {
        return false;
      }
    });

    // disable the bottom map view on touch listener
    mMapViewBottom.setOnTouchListener(new DefaultMapViewOnTouchListener(this, mMapViewBottom) {
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
    zoomTo(mZoomedIn, 5).addDoneListener(() -> {
      mNavigatingTextView.setVisibility(View.INVISIBLE);
      zoomTo(mZoomedIn, 3).addDoneListener(() -> {
        mNavigatingTextView.setVisibility(View.VISIBLE);
        zoomTo(mZoomedOut, 5).addDoneListener(() -> {
          mZoomButton.setClickable(true);
          mNavigatingTextView.setVisibility(View.INVISIBLE);
        });
      });
    });
  }

  /**
   * Sets both MapViews to a Viewpoint over a number of seconds.
   *
   * @param viewpoint to which both MapViews should be set.
   * @param seconds over which the viewpoint is asynchronously set.
   *
   * @return a ListenableFuture representing the result of the Viewpoint change.
   */
  private ListenableFuture<Boolean> zoomTo(Viewpoint viewpoint, int seconds) {
    ListenableFuture<Boolean> setViewpointFuture = mMapViewTop.setViewpointAsync(viewpoint, seconds);
    mMapViewBottom.setViewpointAsync(viewpoint, seconds);
    return setViewpointFuture;
  }

  @Override
  protected void onPause() {
    super.onPause();
    mMapViewTop.pause();
    mMapViewBottom.pause();
  }

  @Override
  protected void onResume() {
    super.onResume();
    mMapViewTop.resume();
    mMapViewBottom.resume();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mMapViewTop.dispose();
    mMapViewBottom.dispose();
  }
}
