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

package com.esri.arcgisruntime.sample.featurelayergeodatabase;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.esri.arcgisruntime.data.Geodatabase;
import com.esri.arcgisruntime.data.GeodatabaseFeatureTable;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.MapView;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private MapView mMapView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // create map and add to map view
    ArcGISMap map = new ArcGISMap(Basemap.createStreets());
    mMapView = findViewById(R.id.mapView);
    mMapView.setMap(map);

    // create path to local geodatabase
    String path = getExternalFilesDir(null) + getString(R.string.config_geodb_name);

    // create a new geodatabase from local path
    final Geodatabase geodatabase = new Geodatabase(path);

    // load the geodatabase
    geodatabase.loadAsync();

    // create feature layer from geodatabase and add to the map
    geodatabase.addDoneLoadingListener(() -> {
      if (geodatabase.getLoadStatus() == LoadStatus.LOADED) {
        // access the geodatabase's feature table Trailheads
        GeodatabaseFeatureTable geodatabaseFeatureTable = geodatabase.getGeodatabaseFeatureTable("Trailheads");
        geodatabaseFeatureTable.loadAsync();
        // create a layer from the geodatabase feature table and add to map
        final FeatureLayer featureLayer = new FeatureLayer(geodatabaseFeatureTable);
        featureLayer.addDoneLoadingListener(() -> {
          if (featureLayer.getLoadStatus() == LoadStatus.LOADED) {
            // set viewpoint to the feature layer's extent
            mMapView.setViewpointAsync(new Viewpoint(featureLayer.getFullExtent()));
          } else {
            Toast.makeText(MainActivity.this, "Feature Layer failed to load!", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Feature Layer failed to load!");
          }
        });
        // add feature layer to the map
        mMapView.getMap().getOperationalLayers().add(featureLayer);
      } else {
        Toast.makeText(MainActivity.this, "Geodatabase failed to load!", Toast.LENGTH_LONG).show();
        Log.e(TAG, "Geodatabase failed to load!");
      }
    });
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
