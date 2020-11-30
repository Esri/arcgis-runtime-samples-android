/* Copyright 2016 Esri
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

package com.esri.arcgisruntime.sample.changeviewpoint;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.Polyline;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.MapView;

public class MainActivity extends AppCompatActivity {

  private static final int SCALE = 5000;
  private MapView mMapView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // authentication with an API key or named user is required to access basemaps and other
    // location services
    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY);

    // inflate MapView from layout
    mMapView = findViewById(R.id.mapView);
    // create a map with the BasemapType topographic
    ArcGISMap map = new ArcGISMap(BasemapStyle.ARCGIS_IMAGERY);
    map.setInitialViewpoint(new Viewpoint(new Point(-14093.0, 6711377.0, SpatialReferences.getWebMercator()), SCALE));
    // set the map to be displayed in this view
    mMapView.setMap(map);
  }

  public void onAnimateClicked(View view) {
    // create the London location point
    Point londonPoint = new Point(-14093.0, 6711377.0, SpatialReferences.getWebMercator());
    // create the viewpoint with the London point and scale
    Viewpoint viewpoint = new Viewpoint(londonPoint, SCALE);
    // set the map view's viewpoint to London with a seven second animation duration
    mMapView.setViewpointAsync(viewpoint, 7f);
  }

  public void onCenterClicked(View view) {
    // create the Waterloo location point
    Point waterlooPoint = new Point(-12153.0, 6710527.0, SpatialReferences.getWebMercator());
    // set the map view's viewpoint centered on Waterloo and scaled
    mMapView.setViewpointCenterAsync(waterlooPoint, SCALE);
  }

  public void onGeometryClicked(View view) {
    // create a collection of points around Westminster
    PointCollection westminsterPoints = new PointCollection(SpatialReferences.getWebMercator());
    westminsterPoints.add(new Point(-13823.0, 6710390.0));
    westminsterPoints.add(new Point(-13823.0, 6710150.0));
    westminsterPoints.add(new Point(-14680.0, 6710390.0));
    westminsterPoints.add(new Point(-14680.0, 6710150.0));
    Polyline geometry = new Polyline(westminsterPoints);

    // set the map view's viewpoint to Westminster
    mMapView.setViewpointGeometryAsync(geometry);
  }

  @Override
  protected void onPause() {
    super.onPause();
    mMapView.pause();
  }

  @Override
  protected void onResume() {
    super.onResume();
    mMapView.resume();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mMapView.dispose();
  }
}
