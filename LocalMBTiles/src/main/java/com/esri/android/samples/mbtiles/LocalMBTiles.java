/* Copyright 2014 ESRI
 *
 * All rights reserved under the copyright laws of the United States
 * and applicable international laws, treaties, and conventions.
 *
 * You may freely redistribute and use this sample code, with or
 * without modification, provided you include the original copyright
 * notice and use restrictions.
 *
 * See the Sample code usage restrictions document for further information.
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