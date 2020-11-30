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

package com.esri.arcgisruntime.sample.displaykml;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import com.esri.arcgisruntime.portal.Portal;
import com.esri.arcgisruntime.portal.PortalItem;

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
    map.setInitialViewpoint(new Viewpoint(39, -98, 100000000));

    // set the map to the map view
    mMapView.setMap(map);

    // set the initial source to a KML layer from a URL
    changeSourceToURL();
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

  /**
   * Display a kml layer from a URL.
   */
  private void changeSourceToURL() {
    // create a kml data set from a URL
    KmlDataset kmlDataset = new KmlDataset(getString(R.string.noaa_weather_kml_url));

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
   * Display a kml layer from a portal item.
   */
  private void changeSourceToPortalItem() {
    // create a portal to ArcGIS Online
    Portal portal = new Portal(getString(R.string.arcgis_online_url));

    // create a portal item from a kml item id
    PortalItem portalItem = new PortalItem(portal, getString(R.string.kml_item_id));

    // a KML layer created from an ArcGIS Online portal item
    KmlLayer kmlLayer = new KmlLayer(portalItem);
    display(kmlLayer);

    // report errors if failed to load
    kmlLayer.addDoneLoadingListener(() -> {
      if (kmlLayer.getLoadStatus() != LoadStatus.LOADED) {
        String error = "Failed to load kml layer from portal item: " + kmlLayer.getLoadError().getCause().getMessage();
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        Log.e(TAG, error);
      }
    });
  }

  /**
   * Display a kml layer from external storage.
   */
  private void changeSourceToFileExternalStorage() {
    // a data set made from data in external storage
    KmlDataset kmlDataset = new KmlDataset(getExternalFilesDir(null) + getString(R.string.kml_path));

    // a KML layer created from a local KML file
    KmlLayer kmlLayer = new KmlLayer(kmlDataset);
    display(kmlLayer);

    // report errors if failed to load
    kmlDataset.addDoneLoadingListener(() -> {
      if (kmlDataset.getLoadStatus() != LoadStatus.LOADED) {
        String error =
            "Failed to load kml data set from external storage: " + kmlDataset.getLoadError().getCause().getMessage();
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        Log.e(TAG, error);
      }
    });
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.kml_sources, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    int i = item.getItemId();
    if (i == R.id.kmlFromUrl) {
      changeSourceToURL();
    } else if (i == R.id.kmlFromPortal) {
      changeSourceToPortalItem();
    } else if (i == R.id.kmlFromExternalStorage) {
      changeSourceToFileExternalStorage();
    } else {
      Log.e(TAG, "Menu option not implemented");
    }
    return super.onOptionsItemSelected(item);
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

