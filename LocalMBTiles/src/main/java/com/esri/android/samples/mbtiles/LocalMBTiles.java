/* Copyright 2015 Esri
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

package com.esri.android.samples.mbtiles;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;

import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;

public class LocalMBTiles extends Activity {
  MapView mMapView = null;
  ArcGISTiledMapServiceLayer tileLayer;
  boolean activeNetwork;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    // Retrieve the map and initial extent from XML layout
    mMapView = (MapView) findViewById(R.id.map);

    // create an ArcGISTiledMapServiceLayer as a background if network available
    activeNetwork = isNetworkAvailable();
    if (activeNetwork) {
      tileLayer = new ArcGISTiledMapServiceLayer(
          "http://services.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer");
      // Add tiled layer to MapView
      mMapView.addLayer(tileLayer);
    }else{
      Toast toast = Toast.makeText(this, R.string.offline_message, Toast.LENGTH_SHORT);
      toast.show();
    }

    // Add a MBTilesLayer on top with 50% opacity
    MBTilesLayer mbLayer = new MBTilesLayer(Environment.getExternalStorageDirectory().getPath()
        + "/ArcGIS/samples/mbtiles/world_countries.mbtiles");
    mbLayer.setOpacity(0.5f);
    mMapView.addLayer(mbLayer);
    // enable map to wrap around
    mMapView.enableWrapAround(true);

  }

  private boolean isNetworkAvailable() {
    ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();

    if (netInfo != null && netInfo.isConnected()) {
      return true;
    }

    return false;
  }

  @Override
  protected void onPause() {
    super.onPause();
    mMapView.pause();
  }

  @Override
  protected void onResume() {
    super.onResume();
    mMapView.unpause();
  }

}