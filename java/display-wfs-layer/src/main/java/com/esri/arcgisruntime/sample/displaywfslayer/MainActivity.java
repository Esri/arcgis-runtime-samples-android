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

import androidx.appcompat.app.AppCompatActivity;
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.ogc.wfs.WfsFeatureTable;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleRenderer;

public class MainActivity extends AppCompatActivity {

  private MapView mMapView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // authentication with an API key or named user is required to access basemaps and other
    // location services
    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY);

    mMapView = findViewById(R.id.mapView);

    // create a map with topographic basemap and set it to the map view
    ArcGISMap map = new ArcGISMap(BasemapStyle.ARCGIS_TOPOGRAPHIC);
    mMapView.setMap(map);

    // create an initial extent to load
    Point topLeft = new Point(-13618106.950944, 6042391.201455);
    Point bottomRight = new Point(-13617513.444292, 6041961.243171);
    Envelope initialExtent = new Envelope(topLeft, bottomRight);
    mMapView.setViewpoint(new Viewpoint(initialExtent));

    // create a FeatureTable from the WFS service URL and name of the layer
    WfsFeatureTable wfsFeatureTable = new WfsFeatureTable(getString(R.string.wfs_service_url),
        getString(R.string.seattle_downtown_table));

    // set the feature request mode to manual in this mode, you must manually populate the table - panning and zooming
    // won't request features automatically
    wfsFeatureTable.setFeatureRequestMode(ServiceFeatureTable.FeatureRequestMode.MANUAL_CACHE);

    // create a feature layer to visualize the WFS features
    FeatureLayer wfsFeatureLayer = new FeatureLayer(wfsFeatureTable);

    // apply a renderer to the feature layer
    SimpleRenderer renderer = new SimpleRenderer(new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.RED, 3));
    wfsFeatureLayer.setRenderer(renderer);

    // add the layer to the map's operational layers
    map.getOperationalLayers().add(wfsFeatureLayer);

    // make an initial call to load the initial extent's data from the WFS, using the WFS spatial reference
    populateFromServer(wfsFeatureTable, initialExtent);

    // use the navigation completed event to populate the table with the features needed for the current extent
    mMapView.addNavigationChangedListener(navigationChangedEvent -> {
      // once the map view has stopped navigating
      if (!navigationChangedEvent.isNavigating()) {
        populateFromServer(wfsFeatureTable, mMapView.getVisibleArea().getExtent());
      }
    });
  }

  /**
   * Create query parameters using the given extent to populate the WFS table from service.
   *
   * @param wfsFeatureTable the WFS feature table to populate
   * @param extent          the extent used to define the QueryParameters' geometry
   */
  private void populateFromServer(WfsFeatureTable wfsFeatureTable, Envelope extent) {
    // create a query based on the current visible extent
    QueryParameters visibleExtentQuery = new QueryParameters();
    visibleExtentQuery.setGeometry(extent);
    visibleExtentQuery.setSpatialRelationship(QueryParameters.SpatialRelationship.INTERSECTS);
    // populate the WFS feature table based on the current extent
    wfsFeatureTable.populateFromServiceAsync(visibleExtentQuery, false, null);
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
