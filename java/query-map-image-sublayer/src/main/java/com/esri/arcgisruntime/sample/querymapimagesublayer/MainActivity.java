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

package com.esri.arcgisruntime.sample.querymapimagesublayer;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureQueryResult;
import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.ArcGISMapImageLayer;
import com.esri.arcgisruntime.layers.ArcGISMapImageSublayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.SimpleFillSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.esri.arcgisruntime.symbology.Symbol;

import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

  private MapView mMapView;
  private Button mQueryButton;
  private EditText mQueryInputBox;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // authentication with an API key or named user is required to access basemaps and other
    // location services
    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY);

    // inflate views from layout
    mMapView = findViewById(R.id.mapView);
    mQueryButton = findViewById(R.id.queryButton);
    mQueryInputBox = findViewById(R.id.queryInputBox);

    // create a map with a streets vector basemap
    ArcGISMap map = new ArcGISMap(BasemapStyle.ARCGIS_STREETS);

    // set the map to be displayed in this view
    mMapView.setMap(map);
    Point initialLocation = new Point(-13171939.239529, 3923971.284048, SpatialReferences.getWebMercator());
    mMapView.setViewpoint(new Viewpoint(initialLocation, 9500000));

    // create and add a map image layer to the map
    ArcGISMapImageLayer imageLayer = new ArcGISMapImageLayer(getString(R.string.usa_map));
    map.getOperationalLayers().add(imageLayer);

    // create a graphics overlay to show the results in
    GraphicsOverlay graphicsOverlay = new GraphicsOverlay();
    mMapView.getGraphicsOverlays().add(graphicsOverlay);

    // create symbols for showing the results of each sublayer
    SimpleMarkerSymbol citySymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, 0xFFFF0000, 16);
    SimpleLineSymbol stateSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, 0xFF0000FF, 6);
    SimpleLineSymbol countyLineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.DASH, 0xFF00FFFF, 2);
    SimpleFillSymbol countySymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.DIAGONAL_CROSS, 0xFF00FFFF,
        countyLineSymbol);

    mQueryInputBox.setText(Integer.toString(1800000));
    mQueryButton.setEnabled(false);

    // wait until the layer is loaded before enabling the query button
    imageLayer.addDoneLoadingListener(() -> {
      mQueryButton.setEnabled(true);

      // get and load each sublayer to query
      ArcGISMapImageSublayer citiesSublayer = (ArcGISMapImageSublayer) imageLayer.getSublayers().get(0);
      ArcGISMapImageSublayer statesSublayer = (ArcGISMapImageSublayer) imageLayer.getSublayers().get(2);
      ArcGISMapImageSublayer countiesSublayer = (ArcGISMapImageSublayer) imageLayer.getSublayers().get(3);
      citiesSublayer.loadAsync();
      statesSublayer.loadAsync();
      countiesSublayer.loadAsync();

      // query the sublayers when the button is clicked
      mQueryButton.setOnClickListener(v -> {

        // clear previous results
        graphicsOverlay.getGraphics().clear();

        // create query parameters filtering based on population and the map view's current viewpoint
        QueryParameters populationQuery = new QueryParameters();
        populationQuery.setWhereClause("POP2000 > " + mQueryInputBox.getText());
        populationQuery
                .setGeometry(mMapView.getCurrentViewpoint(Viewpoint.Type.BOUNDING_GEOMETRY).getTargetGeometry());

        QueryAndDisplayGraphics(citiesSublayer, citySymbol, populationQuery, graphicsOverlay);
        QueryAndDisplayGraphics(statesSublayer, stateSymbol, populationQuery, graphicsOverlay);
        QueryAndDisplayGraphics(countiesSublayer, countySymbol, populationQuery, graphicsOverlay);

      });
    });
  }

  /**
   * Queries the sublayer's feature table with the query parameters and displays the result features as graphics
   *
   * @param sublayer        - type of sublayer to query from
   * @param sublayerSymbol  - symbol to display on map
   * @param query           - filters based on the population and the current view point
   * @param graphicsOverlay - manages the graphics that will be added to the map view
   */
  private static void QueryAndDisplayGraphics(ArcGISMapImageSublayer sublayer, Symbol sublayerSymbol, QueryParameters query,
      GraphicsOverlay graphicsOverlay) {
    if (sublayer.getLoadStatus() == LoadStatus.LOADED) {
      ServiceFeatureTable sublayerTable = sublayer.getTable();
      ListenableFuture<FeatureQueryResult> sublayerQuery = sublayerTable.queryFeaturesAsync(query);
      sublayerQuery.addDoneListener(() -> {
        try {
          FeatureQueryResult result = sublayerQuery.get();
          for (Feature feature : result) {
            Graphic sublayerGraphic = new Graphic(feature.getGeometry(), sublayerSymbol);
            graphicsOverlay.getGraphics().add(sublayerGraphic);
          }
        } catch (InterruptedException | ExecutionException e) {
          Log.e(MainActivity.class.getSimpleName(), e.toString());
        }
      });
    }
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
