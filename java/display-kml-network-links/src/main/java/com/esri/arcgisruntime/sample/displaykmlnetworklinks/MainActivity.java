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

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.layers.KmlLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.ogc.kml.KmlDataset;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private MapView mMapView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // authentication with an API key or named user is required to access basemaps and other
    // location services
    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY);

    // get the reference to the map view
    mMapView = findViewById(R.id.mapView);
    // create a map with the dark gray canvas basemap
    ArcGISMap map = new ArcGISMap(BasemapStyle.ARCGIS_DARK_GRAY);
    // set the map to the map view
    mMapView.setMap(map);
    mMapView.setViewpoint(new Viewpoint(51.960403, 10.029217, 10000000));

    // create a kml data set from a URL
    KmlDataset kmlDataset = new KmlDataset(getString(R.string.european_air_traffic_kml_url));
    // show an alert when any network link messages are received
    kmlDataset.addKmlNetworkLinkMessageReceivedListener(kmlNetworkLinkMessageReceivedEvent -> {
      String message = "KML Network Link Message: " + kmlNetworkLinkMessageReceivedEvent.getMessage();
      Toast.makeText(this, message, Toast.LENGTH_LONG).show();
      Log.i(TAG, message);
    });

    // a KML layer created from a remote KML file
    KmlLayer kmlLayer = new KmlLayer(kmlDataset);

    // clear the existing layers from the map
    mMapView.getMap().getOperationalLayers().clear();

    // add the KML layer to the map as an operational layer.
    mMapView.getMap().getOperationalLayers().add(kmlLayer);

    // report errors if failed to load
    kmlDataset.addDoneLoadingListener(() -> {
      if (kmlDataset.getLoadStatus() != LoadStatus.LOADED) {
        String error = "Failed to load kml layer from URL: " + kmlDataset.getLoadError().getMessage();
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        Log.e(TAG, error);
      }
    });
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
