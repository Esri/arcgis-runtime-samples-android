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

package com.esri.arcgisruntime.sample.featurelayershapefile;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.esri.arcgisruntime.data.ShapefileFeatureTable;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.MapView;

public class MainActivity extends AppCompatActivity {

  private final static String TAG = MainActivity.class.getSimpleName();

  private MapView mMapView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // create a new map to display in the map view with a streets basemap
    mMapView = findViewById(R.id.mapView);
    ArcGISMap map = new ArcGISMap(Basemap.createStreetsVector());
    mMapView.setMap(map);

    // load the shapefile with a local path
    ShapefileFeatureTable shapefileFeatureTable = new ShapefileFeatureTable(
        getExternalFilesDir(null) + getString(R.string.shapefile_path));

    shapefileFeatureTable.loadAsync();
    shapefileFeatureTable.addDoneLoadingListener(() -> {
      if (shapefileFeatureTable.getLoadStatus() == LoadStatus.LOADED) {

        // create a feature layer to display the shapefile
        FeatureLayer shapefileFeatureLayer = new FeatureLayer(shapefileFeatureTable);

        // add the feature layer to the map
        mMapView.getMap().getOperationalLayers().add(shapefileFeatureLayer);

        // zoom the map to the extent of the shapefile
        mMapView.setViewpointAsync(new Viewpoint(shapefileFeatureLayer.getFullExtent()));
      } else {
        String error = "Shapefile feature table failed to load: " + shapefileFeatureTable.getLoadError().toString();
        Toast.makeText(MainActivity.this, error, Toast.LENGTH_LONG).show();
        Log.e(TAG, error);
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
