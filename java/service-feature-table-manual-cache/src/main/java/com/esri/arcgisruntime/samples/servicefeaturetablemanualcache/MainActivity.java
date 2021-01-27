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

package com.esri.arcgisruntime.samples.servicefeaturetablemanualcache;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureQueryResult;
import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.MapView;

public class MainActivity extends AppCompatActivity {

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

    // create a map with the topographic basemap
    ArcGISMap map = new ArcGISMap(BasemapStyle.ARCGIS_TOPOGRAPHIC);

    // create feature layer with its service feature table
    // create the service feature table
    final ServiceFeatureTable serviceFeatureTable = new ServiceFeatureTable(
        getResources().getString(R.string.sample_service_url));

    //explicitly set the mode to on manual cache (which means you need to call populate from service)
    serviceFeatureTable.setFeatureRequestMode(ServiceFeatureTable.FeatureRequestMode.MANUAL_CACHE);

    // create the feature layer using the service feature table
    FeatureLayer featureLayer = new FeatureLayer(serviceFeatureTable);

    // add the layer to the map
    map.getOperationalLayers().add(featureLayer);

    // load the table
    serviceFeatureTable.loadAsync();
    // add a done loading listener to call populate from service when the table is loaded is done
    serviceFeatureTable.addDoneLoadingListener(new Runnable() {
      @Override
      public void run() {

        // set up the query parameters
        QueryParameters params = new QueryParameters();
        // for a specific 311 request type
        params.setWhereClause("req_type = 'Tree Maintenance or Damage'");
        // set all outfields
        List<String> outFields = new ArrayList<>();
        outFields.add("*");
        //populate the table based on the query, listen for result in a listenable future
        final ListenableFuture<FeatureQueryResult> future = serviceFeatureTable
            .populateFromServiceAsync(params, true, outFields);
        //add done listener to the future which fires when the async method is complete
        future.addDoneListener(new Runnable() {
          @Override
          public void run() {
            try {
              //call get on the future to get the result
              FeatureQueryResult result = future.get();
              // create an Iterator
              Iterator<Feature> iterator = result.iterator();
              Feature feature;
              // cycle through selections
              int counter = 0;
              while (iterator.hasNext()) {
                feature = iterator.next();
                counter++;
                Log.d(getResources().getString(R.string.app_name),
                    "Selection #: " + counter + " Table name: " + feature.getFeatureTable().getTableName());
              }
              Toast.makeText(getApplicationContext(), counter + " features returned", Toast.LENGTH_SHORT).show();

            } catch (Exception e) {
              Log.e(getResources().getString(R.string.app_name), "Populate from service failed: " + e.getMessage());
            }
          }
        });
      }
    });

    // set the map to be displayed in the mapview
    mMapView.setMap(map);

    //set a viewpoint on the mapview so it zooms to the features once they are cached.
    mMapView.setViewpoint(new Viewpoint(new Point(-13630484, 4545415, SpatialReferences.getWebMercator()), 500000));

  }

  @Override
  protected void onPause() {
    super.onPause();
    // pause MapView
    mMapView.pause();
  }

  @Override
  protected void onResume() {
    super.onResume();
    // resume MapView
    mMapView.resume();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    // dispose MapView
    mMapView.dispose();
  }
}
