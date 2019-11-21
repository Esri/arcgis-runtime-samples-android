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

package com.esri.arcgisruntime.sample.openstreetmaplayer;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.MapView;

public class MainActivity extends AppCompatActivity {

  private MapView mMapView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // access MapView from layout
    mMapView = (MapView) findViewById(R.id.mapView);
    // instantiate an ArcGISMap with OpenStreetMap Basemap
    ArcGISMap map = new ArcGISMap(Basemap.Type.OPEN_STREET_MAP, 34.056295, -117.195800, 10);
    mMapView.setMap(map);
  }

  @Override
  protected void onPause() {
    super.onPause();
    // pause MapView
    mMapView.pause();
  }

  @Override
  protected void onResume() {
    super.onResume();
    // resume MapView
    mMapView.resume();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    // dispose MapView
    mMapView.dispose();
  }
}
