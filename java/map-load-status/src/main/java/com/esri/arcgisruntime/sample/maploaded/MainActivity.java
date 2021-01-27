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

package com.esri.arcgisruntime.sample.maploaded;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.view.MapView;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = "MapLoadStatus";
  private MapView mMapView;
  private TextView mMapLoadStatusTextView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // authentication with an API key or named user is required to access basemaps and other
    // location services
    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY);

    // inflate MapView from layout
    mMapView = findViewById(R.id.mapView);
    // inflate TextView of the map load status from the layout
    mMapLoadStatusTextView = findViewById(R.id.mapLoadStatusResult);
    loadMap();

  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // handle menu item selection
    int itemId = item.getItemId();
    if (itemId == R.id.Refresh) {
      mMapLoadStatusTextView.setText("");
      // reload the map in the MapView
      loadMap();
    }
    return true;
  }

  /**
   * add a load status change listener on the loadable Map and display the map load status
   */
  private void loadMap() {

    //clear the current map load status of the TextView
    mMapLoadStatusTextView.setText("");
    // create a map with the BasemapType National Geographic
    ArcGISMap map = new ArcGISMap(BasemapStyle.ARCGIS_LIGHT_GRAY);

    // Listener on change in map load status
    map.addLoadStatusChangedListener(loadStatusChangedEvent -> {
      String mapLoadStatus;
      mapLoadStatus = loadStatusChangedEvent.getNewLoadStatus().name();
      // map load status can be any of LOADING, FAILED_TO_LOAD, NOT_LOADED or LOADED
      // set the status in the TextView accordingly
      switch (mapLoadStatus) {
        case "LOADING":
          mMapLoadStatusTextView.setText(R.string.status_loading);
          mMapLoadStatusTextView.setTextColor(Color.BLUE);
          break;

        case "FAILED_TO_LOAD":
          mMapLoadStatusTextView.setText(R.string.status_loadFail);
          mMapLoadStatusTextView.setTextColor(Color.RED);
          break;

        case "NOT_LOADED":
          mMapLoadStatusTextView.setText(R.string.status_notLoaded);
          mMapLoadStatusTextView.setTextColor(Color.GRAY);
          break;

        case "LOADED":
          mMapLoadStatusTextView.setText(R.string.status_loaded);
          mMapLoadStatusTextView.setTextColor(Color.GREEN);
          break;

        default:
          mMapLoadStatusTextView.setText(R.string.status_loadError);
          mMapLoadStatusTextView.setTextColor(Color.WHITE);
          break;
      }

      Log.d(TAG, mapLoadStatus);
    });
    // set the map to be displayed in this view
    mMapView.setMap(map);
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
