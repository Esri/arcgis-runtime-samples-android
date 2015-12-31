/* Copyright 2015 Esri
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

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.esri.arcgisruntime.datasource.arcgis.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Map;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.RgbColor;
import com.esri.arcgisruntime.symbology.SimpleFillSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.UniqueValueRenderer;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    MapView mMapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // inflate MapView from layout
        mMapView = (MapView) findViewById(R.id.mapView);

        // create a map with the topographic basemap
        Map map = new Map(Basemap.createTopographic());

        //[DocRef: Name=Unique Value Renderer, Topic=Symbols and Renderers, Category=Fundamentals]
        // Create service feature table
        ServiceFeatureTable serviceFeatureTable = new ServiceFeatureTable(getResources().getString(R.string.sample_service_url));
        // Ensure that the fields used in the renderer are specified as outfields (by default when creating a ServiceFeatureTable, only the minimal set of fields required for rendering are requested)
        serviceFeatureTable.getOutFields().add(0,"STATE_ABBR");

        // Create the feature layer using the service feature table
        FeatureLayer featureLayer = new FeatureLayer(serviceFeatureTable);

        // Override the renderer of the feature layer with a new unique value renderer
        UniqueValueRenderer uniqueValueRenderer = new UniqueValueRenderer();
        // Set the field to use for the unique values
        uniqueValueRenderer.getFieldNames().add("STATE_ABBR"); //You can add multiple fields to be used for the renderer in the form of a list, in this case we are only adding a single field

        // Create the symbols to be used in the renderer
        SimpleFillSymbol defaultFillSymbol = new SimpleFillSymbol(new RgbColor(0,0,0,0), SimpleFillSymbol.Style.NULL, new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, new RgbColor(211, 211, 211, 255), 2));
        SimpleFillSymbol californiaFillSymbol = new SimpleFillSymbol(new RgbColor(255,0,0,255), SimpleFillSymbol.Style.SOLID, new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, new RgbColor(255, 0, 0, 255), 2));
        SimpleFillSymbol arizonaFillSymbol = new SimpleFillSymbol(new RgbColor(0,255,0,255), SimpleFillSymbol.Style.SOLID, new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, new RgbColor(0, 255, 0, 255), 2));
        SimpleFillSymbol nevadaFillSymbol = new SimpleFillSymbol(new RgbColor(0,0,255,255), SimpleFillSymbol.Style.SOLID, new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, new RgbColor(0, 0, 255, 255), 2));

        // Set default symbol
        uniqueValueRenderer.setDefaultSymbol(defaultFillSymbol);
        uniqueValueRenderer.setDefaultLabel("Other");

        // Set value for california
        List californiaValue = new ArrayList();
        californiaValue.add("CA"); //You add values associated with fields set on the unique value renderer. If there are multiple values, they should be set in the same order as the fields are set
        uniqueValueRenderer.getUniqueValues().add(new UniqueValueRenderer.UniqueValue("California", "State of California", californiaFillSymbol, californiaValue));

        // Set value for arizona
        List arizonaValue = new ArrayList();
        arizonaValue.add("AZ"); //You add values associated with fields set on the unique value renderer. If there are multiple values, they should be set in the same order as the fields are set
        uniqueValueRenderer.getUniqueValues().add(new UniqueValueRenderer.UniqueValue("Arizona", "State of Arizona", arizonaFillSymbol, arizonaValue));

        // Set value for nevada
        List nevadaValue = new ArrayList();
        nevadaValue.add("NV"); //You add values associated with fields set on the unique value renderer. If there are multiple values, they should be set in the same order as the fields are set
        uniqueValueRenderer.getUniqueValues().add(new UniqueValueRenderer.UniqueValue("Nevada", "State of Nevada", nevadaFillSymbol, nevadaValue));

        // Set the renderer on the feature layer
        featureLayer.setRenderer(uniqueValueRenderer);
        //[DocRef: END]

        // add the layer to the map
        map.getOperationalLayers().add(featureLayer);

        map.setInitialViewpoint(new Viewpoint(new Envelope(-13893029.0, 3573174.0, -12038972.0, 5309823.0, SpatialReferences.getWebMercator())));

        // set the map to be displayed in the mapview
        mMapView.setMap(map);

    }

    @Override
    protected void onPause(){
        super.onPause();
        // pause MapView
        mMapView.pause();
    }


    @Override
    protected void onResume(){
        super.onResume();
        // resume MapView
        mMapView.resume();
    }
}
