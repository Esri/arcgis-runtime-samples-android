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

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.arcgisruntime.ArcGISRuntimeException;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.layers.Layer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.LayerViewStateChangedEvent;
import com.esri.arcgisruntime.mapping.view.LayerViewStateChangedListener;
import com.esri.arcgisruntime.mapping.view.LayerViewStatus;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.portal.Portal;
import com.esri.arcgisruntime.portal.PortalItem;

public class MainActivity extends AppCompatActivity {

  private FeatureLayer mFeatureLayer;
  private MapView mMapView;
  private Button loadButton;
  private View statesContainer;
  private Button hideButton;
  private TextView activeStateTextView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // inflate MapView from layout
    mMapView = (MapView) findViewById(R.id.mapView);
    // create a map with the BasemapType topographic
    final ArcGISMap mMap = new ArcGISMap(Basemap.createTopographic());
    // add the map to the mapview
    mMapView.setMap(mMap);

    // zoom to custom ViewPoint
    mMapView.setViewpoint(new Viewpoint(
        new Point(-11e6, 45e5, SpatialReferences.getWebMercator()),
        40_000_000.0
    ));

    // Listen to changes in the status of the Layer
    mMapView.addLayerViewStateChangedListener(new LayerViewStateChangedListener() {
      @Override
      public void layerViewStateChanged(LayerViewStateChangedEvent layerViewStateChangedEvent) {

        // get the layer which changed it's state
        Layer layer = layerViewStateChangedEvent.getLayer();
        // we only want to check the view state of the image layer
        if (layer != mFeatureLayer) {
          return;
        }
        // get the View Status of the layer
        // View status will be either of ACTIVE, ERROR, LOADING, NOT_VISIBLE, OUT_OF_SCALE, WARNING
        EnumSet<LayerViewStatus> layerViewStatus = layerViewStateChangedEvent.getLayerViewStatus();
        // if there is an error or warning, display it in a toast
        ArcGISRuntimeException error = layerViewStateChangedEvent.getError();
        if (error != null) {
          Throwable cause = error.getCause();
          String message = (cause != null)? cause.toString() : error.toString();
          Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
        }
        displayViewStateText(layerViewStatus);
      }
    });

    loadButton = (Button) findViewById(R.id.loadButton);
    statesContainer = findViewById(R.id.statesContainer);
    hideButton = (Button) findViewById(R.id.hideButton);
    activeStateTextView = (TextView) findViewById(R.id.activeStateTextView);

    loadButton.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        if (mFeatureLayer != null) return;
        // load a feature layer from a portal item
        PortalItem portalItem = new PortalItem(
            new Portal("https://runtime.maps.arcgis.com/"),
            "b8f4033069f141729ffb298b7418b653"
        );

        mFeatureLayer = new FeatureLayer(portalItem, 0);
        // set the scales at which this layer can be viewed
        mFeatureLayer.setMinScale(400_000_000.0);
        mFeatureLayer.setMaxScale(400_000_000.0 / 10);
        // add the layer on the map to load it
        mMap.getOperationalLayers().add(mFeatureLayer);
        // hide the button
        loadButton.setEnabled(false);
        loadButton.setVisibility(View.GONE);
        // show the view state UI and the hide layer button
        statesContainer.setVisibility(View.VISIBLE);
        hideButton.setVisibility(View.VISIBLE);
      }
    });

    hideButton.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        if (mFeatureLayer == null) return;

        if (mFeatureLayer.isVisible()) {
          hideButton.setText("Show layer");
          mFeatureLayer.setVisible(false);
        }
        else {
          hideButton.setText("Hide layer");
          mFeatureLayer.setVisible(true);
        }
      }
    });
  }

  /**
   * Formats and displays the layer view status flags in a textview.
   *
   * @param layerViewStatus to display
   */
  protected void displayViewStateText(EnumSet<LayerViewStatus> layerViewStatus) {
    // for each view state property that's active,
    // add it to a list and display the states as a comma-separated string
    List<String> stringList = new ArrayList<String>();
    if (layerViewStatus.contains(LayerViewStatus.ACTIVE)) {
      stringList.add(getString(R.string.active_state));
    }
    if (layerViewStatus.contains(LayerViewStatus.ERROR)) {
      stringList.add(getString(R.string.error_state));
    }
    if (layerViewStatus.contains(LayerViewStatus.LOADING)) {
      stringList.add(getString(R.string.loading_state));
    }
    if (layerViewStatus.contains(LayerViewStatus.NOT_VISIBLE)) {
      stringList.add(getString(R.string.not_visible_state));
    }
    if (layerViewStatus.contains(LayerViewStatus.OUT_OF_SCALE)) {
      stringList.add(getString(R.string.out_of_scale_state));
    }
    if (layerViewStatus.contains(LayerViewStatus.WARNING)) {
      stringList.add(getString(R.string.warning_state));
    }
    // join the list of strings with a common and set to display in the text view
    activeStateTextView.setText(TextUtils.join(", ", stringList));
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
