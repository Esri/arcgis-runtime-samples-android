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

package com.esri.arcgisruntime.sample.attributionviewchange;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

  private MapView mMapView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // inflate MapView from layout
    mMapView = findViewById(R.id.mapView);
    // create a map with a web mercator basemap
    ArcGISMap map = new ArcGISMap(Basemap.Type.TOPOGRAPHIC, 47.495052, -121.786863, 12);
    // set the map to be displayed in this view
    mMapView.setMap(map);

    // create a FAB to respond to attribution bar
    FloatingActionButton floatingActionButton = findViewById(R.id.floatingActionButton);
    floatingActionButton
        .setOnClickListener(v -> Toast.makeText(this, "Tap the attribution bar below", Toast.LENGTH_LONG).show());

    // set attribution bar listener
    mMapView.addAttributionViewLayoutChangeListener(
        (v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
          int heightDelta = oldBottom - bottom;
          floatingActionButton.animate().setDuration(1);
          floatingActionButton.animate().translationYBy(heightDelta);
        });
  }

  @Override
  protected void onResume() {
    super.onResume();
    mMapView.resume();
  }

  @Override
  protected void onPause() {
    mMapView.pause();
    super.onPause();
  }

  @Override
  protected void onDestroy() {
    mMapView.dispose();
    super.onDestroy();
  }
}
