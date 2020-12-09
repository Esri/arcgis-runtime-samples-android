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

package com.esri.arcgisruntime.sample.statisticalquery;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.FeatureTable;
import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.data.StatisticDefinition;
import com.esri.arcgisruntime.data.StatisticRecord;
import com.esri.arcgisruntime.data.StatisticType;
import com.esri.arcgisruntime.data.StatisticsQueryParameters;
import com.esri.arcgisruntime.data.StatisticsQueryResult;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private CheckBox mCurrentExtentCheckbox;
  private CheckBox mGreater5mCheckbox;
  private MapView mMapView;
  private FeatureTable mWorldCitiesTable;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // authentication with an API key or named user is required to access basemaps and other
    // location services
    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY);

    // inflate ui elements
    mMapView = findViewById(R.id.mapView);
    mCurrentExtentCheckbox = findViewById(R.id.currentExtentCheckBox);
    mGreater5mCheckbox = findViewById(R.id.greater5mCheckBox);
    Button getStatisticsButton = findViewById(R.id.getStatisticsButton);

    // create a new Map with the world streets vector basemap
    ArcGISMap map = new ArcGISMap(BasemapStyle.ARCGIS_STREETS);

    // create feature table using the world cities URI
    mWorldCitiesTable = new ServiceFeatureTable(getString(R.string.world_cities_service_0));

    // create a new feature layer to display features in the world cities table
    FeatureLayer worldCitiesLayer = new FeatureLayer(mWorldCitiesTable);

    // add the world cities layer to the map
    map.getOperationalLayers().add(worldCitiesLayer);

    // assign the map to the MapView
    mMapView.setMap(map);

    // create definitions for each statistic to calculate and add to a list
    List<StatisticDefinition> statDefinitions = new ArrayList<>();
    statDefinitions.add(new StatisticDefinition("POP", StatisticType.AVERAGE, ""));
    statDefinitions.add(new StatisticDefinition("POP", StatisticType.MINIMUM, ""));
    statDefinitions.add(new StatisticDefinition("POP", StatisticType.MAXIMUM, ""));
    statDefinitions.add(new StatisticDefinition("POP", StatisticType.SUM, ""));
    statDefinitions.add(new StatisticDefinition("POP", StatisticType.STANDARD_DEVIATION, ""));
    statDefinitions.add(new StatisticDefinition("POP", StatisticType.VARIANCE, ""));
    // create a definition for count that includes an alias for the output
    statDefinitions.add(new StatisticDefinition("POP", StatisticType.COUNT, "CityCount"));

    // add click listener to get statistics button which calls getStatistics, passing in a new instance of the statistic query parameters
    getStatisticsButton.setOnClickListener(view -> {
      // create the statistics query parameters, pass in the list of definitions
      StatisticsQueryParameters statQueryParams = new StatisticsQueryParameters(statDefinitions);
      getStatistics(statQueryParams);
    });
  }

  private void getStatistics(StatisticsQueryParameters statQueryParams) {

    // if only using features in the current extent, set up the spatial filter for the statistics query parameters
    if (mCurrentExtentCheckbox.isChecked()) {
      // get the current extent (as an envelope) from the map view
      Envelope currentExtent = (Envelope) mMapView.getCurrentViewpoint(Viewpoint.Type.BOUNDING_GEOMETRY)
          .getTargetGeometry();

      // set the statistics query parameters geometry with the envelope
      statQueryParams.setGeometry(currentExtent);

      // set the spatial relationship to Intersects (which is the default)
      statQueryParams.setSpatialRelationship(QueryParameters.SpatialRelationship.INTERSECTS);
    }

    // set up an attribute filter
    if (mGreater5mCheckbox.isChecked()) {
      // set a where clause to get only cities with populations over five million
      statQueryParams.setWhereClause("POP_CLASS = '5,000,000 and greater'");
    }

    // execute the statistical query with these parameters and await the results
    ListenableFuture<StatisticsQueryResult> statQueryResultFuture = mWorldCitiesTable
        .queryStatisticsAsync(statQueryParams);
    statQueryResultFuture.addDoneListener(() -> {
      try {
        // get the result
        StatisticsQueryResult statQueryResult = statQueryResultFuture.get();

        // get iterator from the result
        Iterator<StatisticRecord> statisticRecordIterator = statQueryResult.iterator();

        // build a result string for display
        StringBuilder result = new StringBuilder();
        while (statisticRecordIterator.hasNext()) {
          Map<String, Object> statisticsMap = statisticRecordIterator.next().getStatistics();
          for (Map.Entry<String, Object> stat : statisticsMap.entrySet()) {
            result.append(stat.getKey()).append(": ")
                .append(String.format(Locale.US, "%,.0f", stat.getValue())).append("\n");
          }
        }

        // show the results in a snackbar
        Snackbar reportSnackbar = Snackbar.make(findViewById(R.id.activityMain), result, Snackbar.LENGTH_INDEFINITE);
        reportSnackbar.setAction("New Query", view -> reportSnackbar.dismiss());
        TextView snackbarTextView = reportSnackbar.getView().findViewById(com.google.android.material.R.id.snackbar_text);
        snackbarTextView.setSingleLine(false);
        reportSnackbar.show();

      } catch (InterruptedException | ExecutionException e) {
        Toast.makeText(MainActivity.this, "Error getting Statistical Query Results: " + e.getMessage(),
            Toast.LENGTH_LONG).show();
        Log.e(TAG, "Error getting Statistical Query Results: " + e.getMessage());
        e.printStackTrace();
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
    super.onDestroy();
    mMapView.dispose();
  }
}
