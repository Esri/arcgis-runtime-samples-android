package com.esri.arcgisruntime.sample.timebasedquery;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.MapView;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private MapView mMapView;

  private ServiceFeatureTable mServiceFeatureTable;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mMapView = findViewById(R.id.mapView);

    // create a new map with oceans basemap
    ArcGISMap map = new ArcGISMap(Basemap.createOceans());

    // create feature table for the hurricane feature service
    mServiceFeatureTable = new ServiceFeatureTable(getString(R.string.hurricanes_service));

    // define the request mode
    mServiceFeatureTable.setFeatureRequestMode(ServiceFeatureTable.FeatureRequestMode.MANUAL_CACHE);

    // when feature table is loaded, populate data
    mServiceFeatureTable.addDoneLoadingListener(() -> {
      if (mServiceFeatureTable.getLoadStatus() != LoadStatus.LOADED) {
        String error = "Service feature table failed to load : " + mServiceFeatureTable.getLoadError();
        Toast.makeText(MainActivity.this, error, Toast.LENGTH_LONG).show();
        Log.e(TAG, error);
        return;
      }

      // create new query object that contains a basic 'include everything' clause
      QueryParameters queryParameters = new QueryParameters();
      queryParameters.setWhereClause("1=1");

      // create a new time extent that covers the desired interval (beginning of time to September 16th, 2000)
      TimeExtent myExtent = new TimeExtent(new DateTime(1, 1, 1), new DateTime(2000, 9, 16));

      // Apply the time extent to the query parameters
      queryParameters.TimeExtent = myExtent;

      // Create list of the fields that are returned from the service
      var outputFields = new string[] { "*" };

      // Populate feature table with the data based on query
      await _myFeatureTable.PopulateFromServiceAsync(queryParameters, true, outputFields);

    });

    // create FeatureLayer from table
    FeatureLayer featureLayer = new FeatureLayer(mServiceFeatureTable);

    // add created layer to the map and add the map to the map view
    map.getOperationalLayers().add(featureLayer);
    mMapView.setMap(map);
  }

  private async void OnLoadedPopulateData(object sender, LoadStatusEventArgs e)
  {

  }
}
