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
package com.esri.arcgisruntime.samples.changefeaturelayerrenderer;

import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleRenderer;

public class MainActivity extends AppCompatActivity {

  MapView mMapView;
  FeatureLayer mFeatureLayer;

  boolean overrideActive;

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
    mMapView = (MapView) findViewById(R.id.mapView);

    // create a map with the topographic basemap
    ArcGISMap map = new ArcGISMap(BasemapStyle.ARCGIS_TOPOGRAPHIC);
    //set an initial viewpoint
    map.setInitialViewpoint(new Viewpoint(
        new Envelope(-1.30758164047166E7, 4014771.46954516, -1.30730056797177E7, 4016869.78617381,
            SpatialReferences.getWebMercator())));

    // create feature layer with its service feature table
    ServiceFeatureTable serviceFeatureTable = new ServiceFeatureTable(
        getResources().getString(R.string.sample_service_url));
    mFeatureLayer = new FeatureLayer(serviceFeatureTable);

    // add the layer to the map
    map.getOperationalLayers().add(mFeatureLayer);

    // set the map to be displayed in the mapview
    mMapView.setMap(map);

  }

  private void overrideRenderer() {

    // create a new simple renderer for the line feature layer
    SimpleLineSymbol lineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.rgb(0, 0, 255), 2);
    SimpleRenderer simpleRenderer = new SimpleRenderer(lineSymbol);

    // override the current renderer with the new renderer defined above
    mFeatureLayer.setRenderer(simpleRenderer);
  }

  private void resetRenderer() {

    // reset the renderer back to the definition from the source (feature service) using the reset renderer method
    mFeatureLayer.resetRenderer();

  }

  private void createBottomToolbar() {

    Toolbar bottomToolbar = (Toolbar) findViewById(R.id.bottomToolbar);
    bottomToolbar.inflateMenu(R.menu.menu_main);

    bottomToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
      @Override
      public boolean onMenuItemClick(MenuItem item) {
        // Handle action bar item clicks
        int itemId = item.getItemId();
        //if statement is used because this sample is used elsewhere as a Library module
        if (itemId == R.id.action_override_rend) {
          // check the state of the menu item
          if (!overrideActive) {
            overrideRenderer();
            // change the text to reset
            overrideActive = true;
            item.setTitle(R.string.action_reset);
          } else {
            resetRenderer();
            // change the text to override
            overrideActive = false;
            item.setTitle(R.string.action_override_rend);
          }
        }
        return true;
      }
    });
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
