package com.esri.arcgisruntime.showlabelsonlayer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;
import com.esri.arcgisruntime.arcgisservices.LabelDefinition;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
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

    // create a feature layer from an online feature service of US Highways
    ServiceFeatureTable serviceFeatureTable = new ServiceFeatureTable(getString(R.string.us_highways_1));
    FeatureLayer featureLayer = new FeatureLayer(serviceFeatureTable);
    map.getOperationalLayers().add(featureLayer);

    // zoom to the layer when it's done loading
    featureLayer.addDoneLoadingListener(() -> {
      if (featureLayer.getLoadStatus() == LoadStatus.LOADED) {
        // set viewpoint to the center of the US
        mMapView.setViewpointAsync(new Viewpoint(new Point(-10974490, 4814376, 0, SpatialReferences.getWebMercator()), 20000000));
      } else {
        Toast.makeText(this, getString(R.string.error_message) + featureLayer.getLoadError().getMessage(), Toast.LENGTH_LONG).show();
        Log.e(TAG, getString(R.string.error_message) + featureLayer.getLoadError().getMessage());
      }
    });

    // use large blue text with a yellow halo for the labels
    TextSymbol textSymbol = new TextSymbol();
    textSymbol.setSize(20);
    textSymbol.setColor(0xFF0000FF);
    textSymbol.setHaloColor(0xFFFFFF00);
    textSymbol.setHaloWidth(2);

    // construct the label definition json
    JsonObject json = new JsonObject();
    // prepend 'I - ' (for Interstate) to the route number for the label
    JsonObject expressionInfo = new JsonObject();
    expressionInfo.add("expression", new JsonPrimitive("'I -' + $feature.rte_num1"));
    json.add("labelExpressionInfo", expressionInfo);
    // position the label above and along the direction of the road
    json.add("labelPlacement", new JsonPrimitive("esriServerLinePlacementAboveAlong"));
    // only show labels on the interstate highways (others have an empty rte_num1 attribute)
    json.add("where", new JsonPrimitive("rte_num1 <> ' '"));
    // set the text symbol as the label symbol
    json.add("symbol", new JsonParser().parse(textSymbol.toJson()));

    // create a label definition from the JSON string
    LabelDefinition labelDefinition = LabelDefinition.fromJson(json.toString());
    // add the definition to the feature layer and enable labels on it
    featureLayer.getLabelDefinitions().add(labelDefinition);
    featureLayer.setLabelsEnabled(true);
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
