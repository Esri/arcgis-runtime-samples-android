package com.esri.arcgisruntime.showlabelsonlayer;

import java.util.Arrays;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.esri.arcgisruntime.arcgisservices.LabelDefinition;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.TextSymbol;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private MapView mMapView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // create a map view and set a map
    mMapView = findViewById(R.id.mapView);
    ArcGISMap map = new ArcGISMap(Basemap.createLightGrayCanvas());
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

    // use red text with white halo for republican district labels
    TextSymbol republicanTextSymbol = new TextSymbol();
    republicanTextSymbol.setSize(10);
    republicanTextSymbol.setColor(Color.RED);
    republicanTextSymbol.setHaloColor(Color.WHITE);
    republicanTextSymbol.setHaloWidth(2);

    // use blue text with white halo for democrat district labels
    TextSymbol democratTextSymbol = new TextSymbol();
    democratTextSymbol.setSize(10);
    democratTextSymbol.setColor(Color.BLUE);
    democratTextSymbol.setHaloColor(Color.WHITE);
    democratTextSymbol.setHaloWidth(2);

    // construct the label definition json
    JsonObject json = new JsonObject();
    // use a custom label expression combining some of the feature's fields
    JsonObject expressionInfo = new JsonObject();
    expressionInfo.add("expression", new JsonPrimitive("$feature.NAME + \" (\" + left($feature.PARTY,1) + \")\\nDistrict \" + $feature.CDFIPS"));
    json.add("labelExpressionInfo", expressionInfo);
    // position the label in the center of the feature
    json.add("labelPlacement", new JsonPrimitive("esriServerPolygonPlacementAlwaysHorizontal"));
    // create a copy of the json with a custom where clause and symbol only for republican districts
    JsonObject republicanJson = json.deepCopy();
    republicanJson.add("where", new JsonPrimitive("PARTY = 'Republican'"));
    republicanJson.add("symbol", new JsonParser().parse(republicanTextSymbol.toJson()));
    // create a copy of the json with a custom where clause and symbol only for democrat districts
    JsonObject democratJson = json.deepCopy();
    democratJson.add("where", new JsonPrimitive("PARTY = 'Democrat'"));
    democratJson.add("symbol", new JsonParser().parse(democratTextSymbol.toJson()));
    // create label definitions from the JSON strings
    LabelDefinition republicanLabelDefinition = LabelDefinition.fromJson(republicanJson.toString());
    LabelDefinition democratLabelDefinition = LabelDefinition.fromJson(democratJson.toString());
    // add the definitions to the feature layer
    featureLayer.getLabelDefinitions().addAll(Arrays.asList(republicanLabelDefinition, democratLabelDefinition));
    featureLayer.getLabelDefinitions().add(democratLabelDefinition);

    // enable labels
    featureLayer.setLabelsEnabled(true);
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
