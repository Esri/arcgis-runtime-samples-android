/*
 * Copyright 2019 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.esri.arcgisruntime.samples.wfsxmlquery;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutionException;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.FeatureQueryResult;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.ogc.wfs.OgcAxisOrder;
import com.esri.arcgisruntime.ogc.wfs.WfsFeatureTable;

public class MainActivity extends AppCompatActivity {

  private MapView mMapView;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mMapView = findViewById(R.id.mapView);

    // create a map and set the map to display in the map view
    ArcGISMap map = new ArcGISMap(Basemap.createNavigationVector());
    mMapView.setMap(map);

    // create the WFS feature table from URL and name
    WfsFeatureTable statesTable = new WfsFeatureTable(getString(R.string.wfs_feature_table_url),
        getString(R.string.wfs_feature_table_table_name));

    // set the feature request mode and axis order
    statesTable.setAxisOrder(OgcAxisOrder.NO_SWAP);
    statesTable.setFeatureRequestMode(ServiceFeatureTable.FeatureRequestMode.MANUAL_CACHE);

    statesTable.addDoneLoadingListener(() -> {
      String xmlQuery = null;

      try {
        xmlQuery = loadQueryFromAssets();
      } catch (IOException e) {
        String error = "Error reading XML query file file: " + e.getMessage();
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        Log.e(MainActivity.class.getSimpleName(), error);
      }

      ListenableFuture<FeatureQueryResult> featureQueryResultFuture = statesTable
          .populateFromServiceAsync(xmlQuery, true);

      new Thread(() -> {
        try {
          featureQueryResultFuture.get();

          // Create a feature layer to visualize the table.
          FeatureLayer statesLayer = new FeatureLayer(statesTable);

          runOnUiThread(() -> {
            // Add the layer to the map.
            mMapView.getMap().getOperationalLayers().add(statesLayer);

            mMapView.setViewpointGeometryAsync(statesLayer.getFullExtent(), 50);
          });
        } catch (InterruptedException | ExecutionException e) {
          e.printStackTrace();
        }
      }).start();
    });

    statesTable.loadAsync();
  }

  private String loadQueryFromAssets() throws IOException {
    StringBuilder xmlString = new StringBuilder();
    BufferedReader bufferedReader = new BufferedReader(
        new InputStreamReader(getAssets().open(getString(R.string.xml_query_file_name))));
    String line;
    while ((line = bufferedReader.readLine()) != null) {
      xmlString.append(line);
    }
    return xmlString.toString();
  }
}
