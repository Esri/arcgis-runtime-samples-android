/*
 *  Copyright 2019 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.esri.arcgisruntime.sample.displaywfslayer;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.ogc.wfs.WfsFeatureTable;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleRenderer;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private MapView mMapView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mMapView = findViewById(R.id.mapView);

    // create a map with topographic basemap and set it to the map view
    ArcGISMap map = new ArcGISMap(Basemap.createTopographic());
    mMapView.setMap(map);
    Point topLeft = new Point(-122.341581, 47.617207, SpatialReferences.getWgs84());
    Point bottomRight = new Point(-122.332662, 47.613758, SpatialReferences.getWgs84());
    Envelope initialExtent = new Envelope(topLeft, bottomRight);
    mMapView.setViewpointGeometryAsync(initialExtent);

    // create a FeatureTable from the WFS service URL and name of the layer
    WfsFeatureTable wfsFeatureTable = new WfsFeatureTable(getString(R.string.wfs_service_url),
        getString(R.string.seattle_downtown_table));

    // set the feature request mode to manual in this mode, you must manually populate the table - panning and zooming
    // won't request features automatically
    wfsFeatureTable.setFeatureRequestMode(ServiceFeatureTable.FeatureRequestMode.MANUAL_CACHE);

    // create a feature layer to visualize the WFS features
    FeatureLayer wfsFeatureLayer = new FeatureLayer(wfsFeatureTable);

    Log.d(TAG, "calling populate");
    populateFromServer(wfsFeatureTable, initialExtent);

    // apply a renderer to the feature layer
    SimpleRenderer renderer = new SimpleRenderer(new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.RED, 3));
    wfsFeatureLayer.setRenderer(renderer);

    // add the layer to the map's operational layers
    map.getOperationalLayers().add(wfsFeatureLayer);

    // use the navigation completed event to populate the table with the features needed for the current extent
    mMapView.addNavigationChangedListener(navigationChangedEvent -> {
      // once the map view has stopped navigating
      if (!navigationChangedEvent.isNavigating()) {
        populateFromServer(wfsFeatureTable, mMapView.getVisibleArea().getExtent());
      }
    });
  }

  private void populateFromServer(WfsFeatureTable wfsFeatureTable, Envelope extent) {

    Log.d(TAG, "Call to POPULATE");

    wfsFeatureTable.addDoneLoadingListener(() -> {
      Log.d(TAG, "wfs feature table loaded");
      // create a query based on the current visible extent
      QueryParameters visibleExtentQuery = new QueryParameters();
      visibleExtentQuery.setGeometry(extent);
      visibleExtentQuery.setSpatialRelationship(QueryParameters.SpatialRelationship.INTERSECTS);
      // populate the WFS feature table based on the current extent
      wfsFeatureTable.populateFromServiceAsync(visibleExtentQuery, false, null);
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

  @Override protected void onDestroy() {
    mMapView.dispose();
    super.onDestroy();
  }
}
