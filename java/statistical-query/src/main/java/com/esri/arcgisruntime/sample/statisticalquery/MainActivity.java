package com.esri.arcgisruntime.sample.statisticalquery;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.CheckBox;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.FeatureTable;
import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.data.StatisticDefinition;
import com.esri.arcgisruntime.data.StatisticType;
import com.esri.arcgisruntime.data.StatisticsQueryParameters;
import com.esri.arcgisruntime.data.StatisticsQueryResult;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.MapView;

public class MainActivity extends AppCompatActivity {

  private MapView mMapView;
  private FeatureTable mWorldCitiesTable;

  private CheckBox mCurrentExtentCheckbox;
  private CheckBox mGreater5mCheckbox;
  private Button mGetStatisticsButton;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // inflate ui elements
    mMapView = findViewById(R.id.mapView);
    mCurrentExtentCheckbox = findViewById(R.id.currentExtentCheckBox);
    mGreater5mCheckbox = findViewById(R.id.greater5mCheckBox);
    mGetStatisticsButton = findViewById(R.id.getStatisticsButton);

    // create a new Map with the world streets vector basemap
    ArcGISMap map = new ArcGISMap(Basemap.createStreetsVector());

    // create feature table using the world cities URI
    mWorldCitiesTable = new ServiceFeatureTable(getString(R.string.world_cities_service));

    // create a new feature layer to display features in the world cities table
    FeatureLayer worldCitiesLayer = new FeatureLayer(mWorldCitiesTable);

    // add the world cities layer to the map
    map.getOperationalLayers().add(worldCitiesLayer);

    // assign the map to the MapView
    mMapView.setMap(map);

    // add click listener to get statistics button
    mGetStatisticsButton.setOnClickListener(view -> getStatistics());
  }

  private void getStatistics() {

    // create definitions for each statistic to calculate
    StatisticDefinition statDefinitionAvgPop = new StatisticDefinition("POP", StatisticType.AVERAGE, "");
    StatisticDefinition statDefinitionMinPop = new StatisticDefinition("POP", StatisticType.MINIMUM, "");
    StatisticDefinition statDefinitionMaxPop = new StatisticDefinition("POP", StatisticType.MAXIMUM, "");
    StatisticDefinition statDefinitionSumPop = new StatisticDefinition("POP", StatisticType.SUM, "");
    StatisticDefinition statDefinitionStdDevPop = new StatisticDefinition("POP", StatisticType.STANDARD_DEVIATION, "");
    StatisticDefinition statDefinitionVarPop = new StatisticDefinition("POP", StatisticType.VARIANCE, "");

    // create a definition for count that includes an alias for the output
    StatisticDefinition statDefinitionCount = new StatisticDefinition("POP", StatisticType.COUNT, "CityCount");

    // add the statistics definitions to a list
    List<StatisticDefinition> statDefinitions = new ArrayList<>();
    statDefinitions.add(statDefinitionAvgPop);
    statDefinitions.add(statDefinitionCount);
    statDefinitions.add(statDefinitionMinPop);
    statDefinitions.add(statDefinitionMaxPop);
    statDefinitions.add(statDefinitionSumPop);
    statDefinitions.add(statDefinitionStdDevPop);
    statDefinitions.add(statDefinitionVarPop);

    // create the statistics query parameters, pass in the list of definitions
    StatisticsQueryParameters statQueryParams = new StatisticsQueryParameters(statDefinitions);

    // if only using features in the current extent, set up the spatial filter for the statistics query parameters
    if (mCurrentExtentCheckbox.isChecked()) {
      // get the current extent (envelope) from the map view
      Envelope currentExtent = (Envelope) mMapView.getCurrentViewpoint(Viewpoint.Type.BOUNDING_GEOMETRY).getTargetGeometry();

      // set the statistics query parameters geometry with the envelope
      statQueryParams.setGeometry(currentExtent);

      // set the spatial relationship to Intersects (which is the default)
      statQueryParams.setSpatialRelationship(QueryParameters.SpatialRelationship.INTERSECTS);
    }

    // if only evaluating the largest cities (over 5 million in population), set up an attribute filter
    if (mGreater5mCheckbox.isChecked()) {
      // Set a where clause to get the largest cities (could also use "POP_CLASS = '5,000,000 and greater'")
      statQueryParams.setWhereClause("POP_RANK = 1");
    }

    // execute the statistical query with these parameters and await the results
    ListenableFuture<StatisticsQueryResult> statQueryResultFuture = mWorldCitiesTable.queryStatisticsAsync(statQueryParams);

    statQueryResultFuture.addDoneListener(() -> {
      try {
        StatisticsQueryResult statQueryResult = statQueryResultFuture.get();

        // display results in the list box
        //StatResultsListBox.ItemsSource = statQueryResult.iterator().next().getStatistics().Statistics.ToList();
      } catch (InterruptedException | ExecutionException e) {
        e.printStackTrace();
      }
    });
  }
}
