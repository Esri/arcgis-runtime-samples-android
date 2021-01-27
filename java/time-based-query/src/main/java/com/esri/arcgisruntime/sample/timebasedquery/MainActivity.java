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

package com.esri.arcgisruntime.sample.timebasedquery;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.TimeExtent;
import com.esri.arcgisruntime.mapping.view.MapView;

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

    // inflate MapView from layout
    mMapView = findViewById(R.id.mapView);

    // create a new map with oceans basemap
    ArcGISMap map = new ArcGISMap(BasemapStyle.ARCGIS_OCEANS);

    // create feature table for the hurricane feature service
    ServiceFeatureTable serviceFeatureTable = new ServiceFeatureTable(getString(R.string.hurricanes_service));

    // define the request mode
    serviceFeatureTable.setFeatureRequestMode(ServiceFeatureTable.FeatureRequestMode.MANUAL_CACHE);

    // when feature table is loaded, populate data
    serviceFeatureTable.addDoneLoadingListener(() -> {
      if (serviceFeatureTable.getLoadStatus() == LoadStatus.FAILED_TO_LOAD) {
        String error = "Service feature table failed to load: " + serviceFeatureTable.getLoadError().getCause();
        Toast.makeText(MainActivity.this, error, Toast.LENGTH_LONG).show();
        Log.e(TAG, error);
        return;
      }

      // create new query object that contains a basic 'include everything' clause
      QueryParameters queryParameters = new QueryParameters();
      queryParameters.setWhereClause("1=1");

      // create a new time extent that covers the desired interval (beginning of time to September 16th, 2000)
      TimeExtent timeExtent = new TimeExtent(TimeExtent.MIN_CALENDAR, new GregorianCalendar(2000, 9, 16));

      // apply the time extent to the query parameters
      queryParameters.setTimeExtent(timeExtent);

      // create list of the fields that are returned from the service
      List<String> outFields = new ArrayList<>();
      outFields.add("*");

      // populate feature table with the data based on query
      serviceFeatureTable.populateFromServiceAsync(queryParameters, true, outFields);
    });

    // create a feature layer from the service feature table
    FeatureLayer featureLayer = new FeatureLayer(serviceFeatureTable);

    // add created layer to the map and add the map to the map view
    map.getOperationalLayers().add(featureLayer);
    mMapView.setMap(map);
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
