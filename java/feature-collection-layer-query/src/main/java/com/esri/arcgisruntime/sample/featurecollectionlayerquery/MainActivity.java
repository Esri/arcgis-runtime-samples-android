/* Copyright 2017 Esri
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

package com.esri.arcgisruntime.sample.featurecollectionlayerquery;

import java.util.concurrent.ExecutionException;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.FeatureCollection;
import com.esri.arcgisruntime.data.FeatureCollectionTable;
import com.esri.arcgisruntime.data.FeatureQueryResult;
import com.esri.arcgisruntime.data.FeatureTable;
import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.layers.FeatureCollectionLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.view.MapView;

public class MainActivity extends AppCompatActivity {

  private final String TAG = MainActivity.class.getSimpleName();

  private MapView mMapView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // authentication with an API key or named user is required to access basemaps and other
    // location services
    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY);

    // inflate MapView from layout
    mMapView = findViewById(R.id.mapView);

    //initialize map with basemap
    ArcGISMap map = new ArcGISMap(BasemapStyle.ARCGIS_OCEANS);

    //assign map to the map view
    mMapView.setMap(map);

    //initialize service feature table to be queried
    FeatureTable featureTable = new ServiceFeatureTable(getResources().getString(R.string.wildfire_feature_server));

    //create query parameters
    QueryParameters queryParams = new QueryParameters();

    // 1=1 will give all the features from the table
    queryParams.setWhereClause("1=1");

    //query feature from the table
    final ListenableFuture<FeatureQueryResult> queryResult = featureTable.queryFeaturesAsync(queryParams);
    queryResult.addDoneListener(() -> {
      try {
        //create a feature collection table from the query results
        FeatureCollectionTable featureCollectionTable = new FeatureCollectionTable(queryResult.get());

        //create a feature collection from the above feature collection table
        FeatureCollection featureCollection = new FeatureCollection();
        featureCollection.getTables().add(featureCollectionTable);

        //create a feature collection layer
        FeatureCollectionLayer featureCollectionLayer = new FeatureCollectionLayer(featureCollection);

        //add the layer to the operational layers array
        mMapView.getMap().getOperationalLayers().add(featureCollectionLayer);
      } catch (InterruptedException | ExecutionException e) {
        Log.e(TAG, "Error in FeatureQueryResult: " + e.getMessage());
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
