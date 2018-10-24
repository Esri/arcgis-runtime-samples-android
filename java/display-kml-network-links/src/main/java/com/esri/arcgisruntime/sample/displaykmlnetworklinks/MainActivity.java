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

package com.esri.arcgisruntime.sample.displaykmlnetworklinks;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import com.esri.arcgisruntime.displaykmlnetworklinks.R;
import com.esri.arcgisruntime.layers.KmlLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.ogc.kml.KmlDataset;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private MapView mMapView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // get the reference to the map view
    mMapView = findViewById(R.id.mapView);
    // create a map with the dark gray canvas basemap
    ArcGISMap map = new ArcGISMap(Basemap.createImageryWithLabels());

    // set the map to the map view
    mMapView.setMap(map);
    requestReadPermission();
  }

  /**
   * Request read external storage for API level 23+.
   */
  private void requestReadPermission() {
    // define permission to request
    String[] reqPermission = { Manifest.permission.READ_EXTERNAL_STORAGE };
    int requestCode = 2;
    if (ContextCompat.checkSelfPermission(this, reqPermission[0]) == PackageManager.PERMISSION_GRANTED) {
      // set initial KML to URL
      changeSourceToURL();

    } else {
      // request permission
      ActivityCompat.requestPermissions(this, reqPermission, requestCode);
    }
  }

  /**
   * Handle the permissions request response.
   */
  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      // set initial KML to URL
      changeSourceToURL();
    } else {
      // report to user that permission was denied
      Toast.makeText(this, getString(R.string.kml_read_permission_denied), Toast.LENGTH_SHORT).show();
    }
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
  }

  /**
   * Display a kml layer from a URL.
   */
  private void changeSourceToURL() {
    // create a kml data set from a URL
    KmlDataset kmlDataset = new KmlDataset(getString(R.string.kml_url));
    // show an alert when any network link messages are received
    kmlDataset.addKmlNetworkLinkMessageReceivedListener(kmlNetworkLinkMessageReceivedEvent -> {
      String error = "KML Network Link Message: " + kmlNetworkLinkMessageReceivedEvent.getMessage();
      Toast.makeText(this, error, Toast.LENGTH_LONG).show();
      Log.e(TAG, error);
    });

    // a KML layer created from a remote KML file
    KmlLayer kmlLayer = new KmlLayer(kmlDataset);
    display(kmlLayer);

    // report errors if failed to load
    kmlDataset.addDoneLoadingListener(() -> {
      if (kmlDataset.getLoadStatus() != LoadStatus.LOADED) {
        String error = "Failed to load kml layer from URL: " + kmlDataset.getLoadError().getMessage();
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        Log.e(TAG, error);
      }
    });
  }

  /**
   * Clear all operational layers and add the kml layer to the map as an operational layer.
   *
   * @param kmlLayer to add to the map
   */
  private void display(KmlLayer kmlLayer) {
    // clear the existing layers from the map
    mMapView.getMap().getOperationalLayers().clear();

    // add the KML layer to the map
    mMapView.getMap().getOperationalLayers().add(kmlLayer);
  }

  @Override
  protected void onPause() {
    mMapView.pause();
    super.onPause();
  }

  @Override
  protected void onResume() {
    super.onResume();
    mMapView.resume();
  }

  @Override
  protected void onDestroy() {
    mMapView.dispose();
    super.onDestroy();
  }
}
