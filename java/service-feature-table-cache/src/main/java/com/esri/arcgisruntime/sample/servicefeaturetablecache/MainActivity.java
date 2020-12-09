/* Copyright 2016 ESRI
 *
 * All rights reserved under the copyright laws of the United States
 * and applicable international laws, treaties, and conventions.
 *
 * You may freely redistribute and use this sample code, with or
 * without modification, provided you include the original copyright
 * notice and use restrictions.
 *
 * See the Sample code usage restrictions document for further information.
 *
 */

package com.esri.arcgisruntime.sample.servicefeaturetablecache;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.MapView;

public class MainActivity extends AppCompatActivity {

  MapView mMapView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // authentication with an API key or named user is required to access basemaps and other
    // location services
    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY);

    // inflate MapView from layout
    mMapView = findViewById(R.id.mapView);

    // create a map with the light grey canvas basemap
    ArcGISMap map = new ArcGISMap(BasemapStyle.ARCGIS_LIGHT_GRAY);

    // create feature layer with its service feature table
    // create the service feature table
    ServiceFeatureTable serviceFeatureTable = new ServiceFeatureTable(
        getResources().getString(R.string.sample_service_url));

    //explicitly set the mode to on interaction cache (which is also the default mode for service feature tables)
    serviceFeatureTable.setFeatureRequestMode(ServiceFeatureTable.FeatureRequestMode.ON_INTERACTION_CACHE);

    // create the feature layer using the service feature table
    FeatureLayer featureLayer = new FeatureLayer(serviceFeatureTable);

    // add the layer to the map
    map.getOperationalLayers().add(featureLayer);

    // set the map to be displayed in the mapview
    mMapView.setMap(map);

    // set an initial viewpoint
    mMapView.setViewpoint(new Viewpoint(
        new Envelope(-1.30758164047166E7, 4014771.46954516, -1.30730056797177E7, 4016869.78617381,
            SpatialReferences.getWebMercator())));
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
