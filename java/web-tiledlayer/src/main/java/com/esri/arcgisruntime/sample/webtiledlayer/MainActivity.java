/* Copyright 2017 Esri
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

package com.esri.arcgisruntime.sample.webtiledlayer;

import java.util.Arrays;
import java.util.List;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.esri.arcgisruntime.layers.WebTiledLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.MapView;

public class MainActivity extends AppCompatActivity {

  private MapView mMapView;
  private WebTiledLayer mWebTiledLayer;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // get reference to map view
    mMapView = findViewById(R.id.mapView);

    // list of sub-domains
    List<String> subDomains = Arrays.asList("a", "b", "c", "d");

    // build the web tiled layer from stamen
    mWebTiledLayer = new WebTiledLayer(getString(R.string.template_uri_stamen), subDomains);
    mWebTiledLayer.loadAsync();
    mWebTiledLayer.addDoneLoadingListener(() -> {
      if (mWebTiledLayer.getLoadStatus() == LoadStatus.LOADED) {
        // use web tiled layer as Basemap
        ArcGISMap map = new ArcGISMap(new Basemap(mWebTiledLayer));
        mMapView.setMap(map);
        // custom attributes
        mWebTiledLayer.setAttribution(getString(R.string.stamen_attribution));
      }
    });
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
