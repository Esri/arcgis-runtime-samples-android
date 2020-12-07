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

package com.esri.arcgisruntime.sample.uniquevaluerenderer;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Color;
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
import com.esri.arcgisruntime.symbology.SimpleFillSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.UniqueValueRenderer;

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

    //[DocRef: Name=Unique Value Renderer, Topic=Symbols and Renderers, Category=Fundamentals]
    // Create service feature table
    ServiceFeatureTable serviceFeatureTable = new ServiceFeatureTable(
        getResources().getString(R.string.sample_service_url));

    // Create the feature layer using the service feature table
    FeatureLayer featureLayer = new FeatureLayer(serviceFeatureTable);

    // Override the renderer of the feature layer with a new unique value renderer
    UniqueValueRenderer uniqueValueRenderer = new UniqueValueRenderer();
    // Set the field to use for the unique values
    uniqueValueRenderer.getFieldNames().add(
        "STATE_ABBR"); //You can add multiple fields to be used for the renderer in the form of a list, in this case
      // we are only adding a single field

    // Create the symbols to be used in the renderer
    SimpleFillSymbol defaultFillSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.NULL, Color.BLACK,
        new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.GRAY, 2));
    SimpleFillSymbol californiaFillSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, Color.RED,
        new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.RED, 2));
    SimpleFillSymbol arizonaFillSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, Color.GREEN,
        new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.GREEN, 2));
    SimpleFillSymbol nevadaFillSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, Color.BLUE,
        new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLUE, 2));

    // Set default symbol
    uniqueValueRenderer.setDefaultSymbol(defaultFillSymbol);
    uniqueValueRenderer.setDefaultLabel("Other");

    // Set value for california
    List<Object> californiaValue = new ArrayList<>();
    // You add values associated with fields set on the unique value renderer.
    // If there are multiple values, they should be set in the same order as the fields are set
    californiaValue.add("CA");
    uniqueValueRenderer.getUniqueValues().add(
        new UniqueValueRenderer.UniqueValue("California", "State of California", californiaFillSymbol,
            californiaValue));

    // Set value for arizona
    List<Object> arizonaValue = new ArrayList<>();
    // You add values associated with fields set on the unique value renderer.
    // If there are multiple values, they should be set in the same order as the fields are set
    arizonaValue.add("AZ");
    uniqueValueRenderer.getUniqueValues()
        .add(new UniqueValueRenderer.UniqueValue("Arizona", "State of Arizona", arizonaFillSymbol, arizonaValue));

    // Set value for nevada
    List<Object> nevadaValue = new ArrayList<>();
    // You add values associated with fields set on the unique value renderer.
    // If there are multiple values, they should be set in the same order as the fields are set
    nevadaValue.add("NV");
    uniqueValueRenderer.getUniqueValues()
        .add(new UniqueValueRenderer.UniqueValue("Nevada", "State of Nevada", nevadaFillSymbol, nevadaValue));

    // Set the renderer on the feature layer
    featureLayer.setRenderer(uniqueValueRenderer);
    //[DocRef: END]

    // add the layer to the map
    map.getOperationalLayers().add(featureLayer);

    map.setInitialViewpoint(new Viewpoint(
        new Envelope(-13893029.0, 3573174.0, -12038972.0, 5309823.0, SpatialReferences.getWebMercator())));

    // set the map to be displayed in the mapview
    mMapView.setMap(map);

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
