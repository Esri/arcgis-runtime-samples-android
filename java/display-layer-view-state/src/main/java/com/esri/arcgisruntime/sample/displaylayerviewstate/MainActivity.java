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

package com.esri.arcgisruntime.sample.displaylayerviewstate;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.TextView;

import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.ArcGISMapImageLayer;
import com.esri.arcgisruntime.layers.ArcGISTiledLayer;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.layers.Layer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.LayerViewStateChangedEvent;
import com.esri.arcgisruntime.mapping.view.LayerViewStateChangedListener;
import com.esri.arcgisruntime.mapping.view.MapView;

public class MainActivity extends AppCompatActivity {

  private static final int MIN_SCALE = 40000000;
  private static final int TILED_LAYER = 0;
  private static final int IMAGE_LAYER = 1;
  private static final int FEATURE_LAYER = 2;
  private MapView mMapView;
  private TextView timeZoneTextView;
  private TextView worldCensusTextView;
  private TextView recreationTextView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // create three layers to add to the map
    final ArcGISTiledLayer tiledLayer = new ArcGISTiledLayer(
        getApplication().getString(R.string.world_timezone_service_URL));
    tiledLayer.setMinScale(4E8);

    final ArcGISMapImageLayer imageLayer = new ArcGISMapImageLayer(
        getApplication().getString(R.string.world_census_service_URL));
    // setting the scales at which this layer can be viewed
    imageLayer.setMinScale(MIN_SCALE);
    imageLayer.setMaxScale(MIN_SCALE / 10);

    // creating a layer from a service feature table
    final ServiceFeatureTable featureTable = new ServiceFeatureTable(
        getApplication().getString(R.string.world_facilities_service_URL));
    final FeatureLayer featureLayer = new FeatureLayer(featureTable);

    // inflate MapView from layout
    mMapView = (MapView) findViewById(R.id.mapView);
    // create a map with the BasemapType topographic
    final ArcGISMap mMap = new ArcGISMap(Basemap.createTopographic());
    // add the layers on the map
    mMap.getOperationalLayers().add(tiledLayer);
    mMap.getOperationalLayers().add(imageLayer);
    mMap.getOperationalLayers().add(featureLayer);

    // set the map to be displayed in this view
    mMapView.setMap(mMap);

    // inflate TextViews from the layout
    timeZoneTextView = (TextView) findViewById(R.id.worldTimeZoneStatusView);
    worldCensusTextView = (TextView) findViewById(R.id.censusStatusView);
    recreationTextView = (TextView) findViewById(R.id.facilitiesStatusView);

    // zoom to custom ViewPoint
    mMapView.setViewpoint(new Viewpoint(
        new Point(-11e6, 45e5, SpatialReferences.getWebMercator()), MIN_SCALE));

    // Listen to changes in the status of the Layer
    mMapView.addLayerViewStateChangedListener(new LayerViewStateChangedListener() {
      @Override
      public void layerViewStateChanged(LayerViewStateChangedEvent layerViewStateChangedEvent) {

        // get the layer which changed it's state
        Layer layer = layerViewStateChangedEvent.getLayer();

        // get the View Status of the layer
        // View status will be either of ACTIVE, ERROR, LOADING, NOT_VISIBLE, OUT_OF_SCALE, UNKNOWN
        String viewStatus = layerViewStateChangedEvent.getLayerViewStatus().iterator().next().toString();

        final int layerIndex = mMap.getOperationalLayers().indexOf(layer);

        // finding and updating status of the layer
        switch (layerIndex) {
          case TILED_LAYER:
            timeZoneTextView.setText(viewStatusString(viewStatus));
            break;
          case IMAGE_LAYER:
            worldCensusTextView.setText(viewStatusString(viewStatus));
            break;
          case FEATURE_LAYER:
            recreationTextView.setText(viewStatusString(viewStatus));
            break;
        }

      }
    });
  }

  /**
   * The method looks up the view status of the layer and returns a string which is displayed
   *
   * @param status View Status of the layer
   * @return String equivalent of the status
   */
  private String viewStatusString(String status) {

    switch (status) {
      case "ACTIVE":
        return getApplication().getString(R.string.active);

      case "ERROR":
        return getApplication().getString(R.string.error);

      case "LOADING":
        return getApplication().getString(R.string.loading);

      case "NOT_VISIBLE":
        return getApplication().getString(R.string.notVisible);

      case "OUT_OF_SCALE":
        return getApplication().getString(R.string.outOfScale);

    }

    return getApplication().getString(R.string.unknown);

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
