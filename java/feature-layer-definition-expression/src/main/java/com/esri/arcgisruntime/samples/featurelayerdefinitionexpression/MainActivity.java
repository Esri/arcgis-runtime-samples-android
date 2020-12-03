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

package com.esri.arcgisruntime.samples.featurelayerdefinitionexpression;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.view.MapView;

public class MainActivity extends AppCompatActivity {

  MapView mMapView;
  FeatureLayer mFeatureLayer;

  boolean applyActive;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // authentication with an API key or named user is required to access basemaps and other
    // location services
    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY);

    // set up the bottom toolbar
    createBottomToolbar();

    // inflate MapView from layout
    mMapView = findViewById(R.id.mapView);

    // create a map with the topographic basemap
    ArcGISMap map = new ArcGISMap(BasemapStyle.ARCGIS_TOPOGRAPHIC);

    // create feature layer with its service feature table
    // create the service feature table
    ServiceFeatureTable serviceFeatureTable = new ServiceFeatureTable(
        getResources().getString(R.string.sample_service_url));

    // create the feature layer using the service feature table
    mFeatureLayer = new FeatureLayer(serviceFeatureTable);

    // add the layer to the map
    map.getOperationalLayers().add(mFeatureLayer);

    // set the map to be displayed in the mapview
    mMapView.setMap(map);

    // zoom to a view point of the USA
    mMapView.setViewpointCenterAsync(new Point(-13630845, 4544861, SpatialReferences.getWebMercator()), 600000);

  }

  private void applyDefinitionExpression() {
    // apply a definition expression on the feature layer
    // if this is called before the layer is loaded, it will be applied to the loaded layer
    mFeatureLayer.setDefinitionExpression("req_Type = 'Tree Maintenance or Damage'");
  }

  private void resetDefinitionExpression() {
    // set the definition expression to nothing (empty string, null also works)
    mFeatureLayer.setDefinitionExpression("");
  }

  private void createBottomToolbar() {

    Toolbar bottomToolbar = findViewById(R.id.bottomToolbar);
    bottomToolbar.inflateMenu(R.menu.menu_main);

    bottomToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
      @Override
      public boolean onMenuItemClick(MenuItem item) {
        // handle action bar item clicks
        int itemId = item.getItemId();
        // if statement is used because this sample is used elsewhere as a Library module
        if (itemId == R.id.action_def_exp) {
          // check the state of the menu item
          if (!applyActive) {
            applyDefinitionExpression();
            // change the text to reset
            applyActive = true;
            item.setTitle(R.string.action_reset);
          } else {
            resetDefinitionExpression();
            // change the text to apply
            applyActive = false;
            item.setTitle(R.string.action_def_exp);
          }
        }
        return true;
      }
    });
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
