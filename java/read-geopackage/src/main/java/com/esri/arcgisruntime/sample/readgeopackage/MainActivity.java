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

package com.esri.arcgisruntime.sample.readgeopackage;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;
import com.esri.arcgisruntime.data.GeoPackage;
import com.esri.arcgisruntime.data.GeoPackageFeatureTable;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.layers.Layer;
import com.esri.arcgisruntime.layers.RasterLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.raster.GeoPackageRaster;

import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

  private static final String[] reqPermission = { Manifest.permission.READ_EXTERNAL_STORAGE };

  private final static String TAG = MainActivity.class.getSimpleName();

  private ListView mDrawerListView;
  private ArrayAdapter<String> mLayersStringAdaptor;

  private MapView mMapView;
  private HashMap<String,Layer> mLayersHashMap;
  private DrawerLayout mDrawerLayout;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mDrawerListView = findViewById(R.id.left_drawer);
    mDrawerLayout = findViewById(R.id.drawer_layout);

    // on tapping a layer in the drawer list view, toggle the check box and call toggleLayer
    mDrawerListView.setOnItemClickListener(
        (adapterView, view, i, l) ->  {
          CheckBox checkBox = view.findViewById(R.id.geopackageLayerCheckBox);
          checkBox.setChecked(!checkBox.isChecked());
          // toggles the given layer on and off
          toggleLayer(mLayersHashMap.get(mDrawerListView.getItemAtPosition(i).toString()));
        });

    // initialize the array adaptor
    mLayersStringAdaptor = new ArrayAdapter<>(this, R.layout.geopackage_layer_item, R.id.geopackageLayerCheckBox);

    // set the adapter for the list view
    mDrawerListView.setAdapter(mLayersStringAdaptor);

    // inflate MapView from layout
    mMapView = findViewById(R.id.mapView);

    // create a new map centered on Aurora Colorado and add it to the map view
    ArcGISMap map = new ArcGISMap(Basemap.Type.STREETS, 39.7294, -104.8319, 11);
    mMapView.setMap(map);

    requestPermissions();

  }

  private void readGeoPackage() {

    mLayersHashMap = new HashMap<>();

    // open and load the GeoPackage
    GeoPackage geoPackage = new GeoPackage(
        Environment.getExternalStorageDirectory() + getString(R.string.geopackage_path));
    geoPackage.loadAsync();
    geoPackage.addDoneLoadingListener(() -> {
      if (geoPackage.getLoadStatus() == LoadStatus.FAILED_TO_LOAD) {
        String error = "Geopackage failed to load: " + geoPackage.getLoadError();
        Log.e(TAG, error);
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        return;
      }

      // loop through each GeoPackageRaster
      for (GeoPackageRaster geoPackageRaster : geoPackage.getGeoPackageRasters()) {
        // create a RasterLayer from the GeoPackageRaster
        RasterLayer rasterLayer = new RasterLayer(geoPackageRaster);

        // set the opacity on the RasterLayer to partially visible
        rasterLayer.setOpacity(0.55f);

        // load the RasterLayer so we can get to its properties
        rasterLayer.loadAsync();
        rasterLayer.addDoneLoadingListener(() -> {

          // create a string to hold the name of the RasterLayer for display in the ListView and as the hash map key
          String rasterLayerName;

          String path = geoPackageRaster.getPath();

          // if getName is not null, use it as the raster's name, else use the end of the path name as the raster's name
          rasterLayerName = rasterLayer.getName().isEmpty() ? path.substring(path.lastIndexOf('/') + 1) : rasterLayer.getName();

          // append the layer type to the name
          rasterLayerName += "\n(RasterLayer)";

          // add the name of the RasterLayer and the RasterLayer itself to the layers HashMap
          mLayersHashMap.put(rasterLayerName, rasterLayer);

          // add the name of the RasterLayer to the layers StringAdapter
          mLayersStringAdaptor.add(rasterLayerName);
          mLayersStringAdaptor.notifyDataSetChanged();
        });
      }

      // get the list of GeoPackageFeatureTables from the GeoPackage
      List<GeoPackageFeatureTable> geoPackageFeatureTables = geoPackage.getGeoPackageFeatureTables();

      // loop through each GeoPackageFeatureTable
      for (GeoPackageFeatureTable geoPackageFeatureTable : geoPackageFeatureTables) {
        // create a FeatureLayer from the GeoPackageFeatureLayer
        FeatureLayer featureLayer = new FeatureLayer(geoPackageFeatureTable);

        // load the FeatureLayer - that way we can get to it's properties
        featureLayer.loadAsync();
        featureLayer.addDoneLoadingListener(() -> {

          // create a string to hold the name of the FeatureLayer for display in the ListView and as the hash map key
          String featureLayerName = featureLayer.getName();

          // append the layer type to the name
          featureLayerName += "\n(FeatureLayer)";

          // add the name of the FeatureLayer and the FeatureLayer itself into the hash map
          mLayersHashMap.put(featureLayerName, featureLayer);

          // add the name of the FeatureLayer to the layers StringAdapter
          mLayersStringAdaptor.add(featureLayerName);
          mLayersStringAdaptor.notifyDataSetChanged();
        });
      }
      // open the drawer
      mDrawerLayout.openDrawer(Gravity.START);
    });
  }

  /**
   * Toggles the given layer on or off.
   *
   * @param layer from layers HashMap
   */
  private void toggleLayer(Layer layer) {
    if (mMapView.getMap().getOperationalLayers().contains(layer)) {
      // remove the layer from the map
      mMapView.getMap().getOperationalLayers().remove(layer);
    } else {
      // add the layer to the map
      mMapView.getMap().getOperationalLayers().add(layer);
      // set the viewpoint to the extent of the layer
      mMapView.setViewpointAsync(new Viewpoint(layer.getFullExtent()), 1);
    }
  }

  /**
   * Request permissions on the device.
   */
  private void requestPermissions() {
    int requestCode = 1;
    // For API level 23+ request permission at runtime
    if (ContextCompat.checkSelfPermission(this, reqPermission[0]) == PackageManager.PERMISSION_GRANTED) {
      readGeoPackage();
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
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      readGeoPackage();
    } else {
      // report to user that permission was denied
      Toast.makeText(this, getResources().getString(R.string.permission_denied), Toast.LENGTH_SHORT).show();
    }
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
