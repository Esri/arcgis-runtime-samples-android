/*
 * Copyright 2018 Esri
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

package com.esri.arcgisruntime.showlabelsonlayer;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.arcgisservices.LabelDefinition;
import com.esri.arcgisruntime.arcgisservices.LabelingPlacement;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.labeling.ArcadeLabelExpression;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.TextSymbol;

import java.util.Arrays;

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

    // create a map view and set a map
    mMapView = findViewById(R.id.mapView);
    ArcGISMap map = new ArcGISMap(BasemapStyle.ARCGIS_LIGHT_GRAY);
    mMapView.setMap(map);

    // create a feature layer from an online feature service of US Congressional Districts
    ServiceFeatureTable serviceFeatureTable = new ServiceFeatureTable(getString(R.string.congressional_districts_url));
    FeatureLayer featureLayer = new FeatureLayer(serviceFeatureTable);
    map.getOperationalLayers().add(featureLayer);

    // zoom to the layer when it's done loading
    featureLayer.addDoneLoadingListener(() -> {
      if (featureLayer.getLoadStatus() == LoadStatus.LOADED) {
        // set viewpoint to feature layer extent
        mMapView.setViewpointAsync(new Viewpoint(featureLayer.getFullExtent()));
      } else {
        String error = "Error loading feature layer :" + featureLayer.getLoadError().getCause();
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        Log.e(TAG, error);
      }
    });

    LabelDefinition republicanLabelDefinition = makeLabelDefinition("Republican", Color.RED);
    LabelDefinition democratLabelDefinition = makeLabelDefinition("Democrat", Color.BLUE);

    // add the definitions to the feature layer
    featureLayer.getLabelDefinitions().addAll(Arrays.asList(republicanLabelDefinition, democratLabelDefinition));

    // enable labels
    featureLayer.setLabelsEnabled(true);
  }

  /**
   * Creates a label definition for a given party (field value) and color to populate a text symbol with.
   *
   * @param party the name of the party to be passed into the label definition's WHERE clause
   * @param textColor the color to be passed into the text symbol
   *
   * @return label definition created from the given arcade expression
   */
  private LabelDefinition makeLabelDefinition(String party, int textColor) {

    // create text symbol for styling the label
    TextSymbol textSymbol = new TextSymbol();
    textSymbol.setSize(12);
    textSymbol.setColor(textColor);
    textSymbol.setHaloColor(Color.WHITE);
    textSymbol.setHaloWidth(2);

    // create a label definition with an Arcade expression script
    ArcadeLabelExpression arcadeLabelExpression =
        new ArcadeLabelExpression("$feature.NAME + \" (\" + left($feature.PARTY,1) + \")\\nDistrict \" + $feature.CDFIPS");
    LabelDefinition labelDefinition = new LabelDefinition(arcadeLabelExpression, textSymbol);
    labelDefinition.setPlacement(LabelingPlacement.POLYGON_ALWAYS_HORIZONTAL);
    labelDefinition.setWhereClause(String.format("PARTY = '%s'", party));

    return labelDefinition;
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
    mMapView.dispose();
    super.onDestroy();
  }
}
