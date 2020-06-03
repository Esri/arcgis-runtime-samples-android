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

package com.esri.arcgisruntime.stylewmslayer;

import java.util.Collections;
import java.util.List;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.esri.arcgisruntime.layers.WmsLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.MapView;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private MapView mMapView;
  // objects that implement Loadable must be class fields to prevent being garbage collected before loading
  private WmsLayer mWmsLayer;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // create a map and add it to the map view
    mMapView = findViewById(R.id.mapView);
    ArcGISMap map = new ArcGISMap(Basemap.createImagery());
    mMapView.setMap(map);

    // create a WMS layer
    List<String> wmsLayerNames = Collections.singletonList(getString(R.string.wms_layer_name_minnesota));
    mWmsLayer = new WmsLayer(getString(R.string.wms_layer_url_minnesota), wmsLayerNames);
    mWmsLayer.loadAsync();
    mWmsLayer.addDoneLoadingListener(() -> {
      if (mWmsLayer.getLoadStatus() == LoadStatus.LOADED) {
        // add the layer to the map
        map.getOperationalLayers().add(mWmsLayer);

        // zoom to the layer on the map
        mMapView.setViewpoint(new Viewpoint(mWmsLayer.getFullExtent()));

        // get styles
        List<String> styles = mWmsLayer.getSublayers().get(0).getSublayerInfo().getStyles();

        // set the style when the button is toggled
        ToggleButton toggle = findViewById(R.id.toggleStyleButton);
        toggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
          if (isChecked) {
            // set the sublayer's current style
            mWmsLayer.getSublayers().get(0).setCurrentStyle(styles.get(1));
          } else {
            //[DocRef: Name=Set WMS Layer Style, Category=Fundamentals, Topic=Symbols and Renderers]
            // set the sublayer's current style
            mWmsLayer.getSublayers().get(0).setCurrentStyle(styles.get(0));
            //[DocRef: END]
          }
        });
      } else {
        String error = "Failed to load WMS layer: " + mWmsLayer.getLoadError().getMessage();
        Log.e(TAG, error);
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
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
