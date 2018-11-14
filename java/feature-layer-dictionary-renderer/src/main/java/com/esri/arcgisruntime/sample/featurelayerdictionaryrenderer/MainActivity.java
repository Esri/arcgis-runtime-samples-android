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

package com.esri.arcgisruntime.sample.featurelayerdictionaryrenderer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;
import android.widget.Toast;
import com.esri.arcgisruntime.data.Geodatabase;
import com.esri.arcgisruntime.data.GeodatabaseFeatureTable;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.DictionaryRenderer;
import com.esri.arcgisruntime.symbology.DictionarySymbolStyle;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  // permission to read external storage
  private final String[] reqPermission = new String[] { Manifest.permission.READ_EXTERNAL_STORAGE };

  private MapView mMapView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // get the reference to the map view
    mMapView = findViewById(R.id.mapView);
    // create a map with the dark gray canvas basemap
//    ArcGISMap map = new ArcGISMap(Type.DARK_GRAY_CANVAS_VECTOR, 51.960403, 10.029217, 5);
    ArcGISMap map = new ArcGISMap(Basemap.createTopographic());
    // set the map to the map view
    mMapView.setMap(map);

    // For API level 23+ request permission at runtime
    if (ContextCompat.checkSelfPermission(MainActivity.this, reqPermission[0]) == PackageManager.PERMISSION_GRANTED) {
      loadGeodatabaseSymbolDictionary();
    } else {
      // request permission
      int requestCode = 2;
      ActivityCompat.requestPermissions(MainActivity.this, reqPermission, requestCode);
    }

  }

  private void loadGeodatabaseSymbolDictionary() {

    // load geo-database from local location
    Geodatabase geodatabase = new Geodatabase(Environment.getExternalStorageDirectory() + "/ArcGIS/samples/Dictionary/militaryoverlay.geodatabase");
    geodatabase.loadAsync();

    // render tells layer what symbols to apply to what features
    DictionarySymbolStyle symbolDictionary = new DictionarySymbolStyle("mil2525d", Environment.getExternalStorageDirectory() + "/ArcGIS/samples/Dictionary/mil2525d.stylx");
    symbolDictionary.loadAsync();

    geodatabase.addDoneLoadingListener(() -> {
      if (geodatabase.getLoadStatus() == LoadStatus.LOADED) {

        for (GeodatabaseFeatureTable table : geodatabase.getGeodatabaseFeatureTables()) {
          // add each layer to map
          FeatureLayer featureLayer = new FeatureLayer(table);
          featureLayer.loadAsync();
          // Features no longer show after this scale
          featureLayer.setMinScale(1000000);
          mMapView.getMap().getOperationalLayers().add(featureLayer);

          // displays features from layer using mil2525d symbols
          DictionaryRenderer dictionaryRenderer = new DictionaryRenderer(symbolDictionary);
          featureLayer.setRenderer(dictionaryRenderer);

          featureLayer.addDoneLoadingListener(() -> {
            if (featureLayer.getLoadStatus() == LoadStatus.LOADED) {
              // initial viewpoint to encompass all graphics displayed on the map view
              mMapView.setViewpointGeometryAsync(featureLayer.getFullExtent());
            } else {
              String error = "Feature Layer Failed to Load!";
              Toast.makeText(this, error, Toast.LENGTH_LONG).show();
              Log.e(TAG, error);
            }
          });
        }
      } else {
        String error = "Geodatabase Failed to Load!";
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        Log.e(TAG, error);
      }
    });
  }

  /**
   * Handle the permissions request response
   */
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      loadGeodatabaseSymbolDictionary();
    } else {
      // report to user that permission was denied
      Toast.makeText(MainActivity.this, getResources().getString(R.string.write_permission_denied),
          Toast.LENGTH_SHORT).show();
    }
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
