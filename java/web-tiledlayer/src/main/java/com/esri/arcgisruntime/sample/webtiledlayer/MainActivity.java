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
import android.support.v7.app.AppCompatActivity;

import com.esri.arcgisruntime.layers.WebTiledLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.MapView;

public class MainActivity extends AppCompatActivity {

  private MapView mMapView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // access MapView from layout
    mMapView = (MapView) findViewById(R.id.mapView);

    // list of subdomains
    List<String> subDomains = Arrays.asList("a", "b", "c", "d");
    // url pattern
    String templateUri = "http://{subDomain}.tile.stamen.com/terrain/{level}/{col}/{row}.png";

    // webtile layer
    final WebTiledLayer webTiledLayer = new WebTiledLayer(templateUri, subDomains);
    webTiledLayer.loadAsync();
    webTiledLayer.addDoneLoadingListener(new Runnable() {
      @Override
      public void run() {
        if (webTiledLayer.getLoadStatus() == LoadStatus.LOADED) {
          // use webtile layer as Basemap
          ArcGISMap map = new ArcGISMap(new Basemap(webTiledLayer));
          mMapView.setMap(map);
          // custom attributes
          webTiledLayer.setAttribution("Map tiles by <a href=\"http://stamen.com/\">Stamen Design</a>, " +
              "under <a href=\"http://creativecommons.org/licenses/by/3.0\">CC BY 3.0</a>. " +
              "Data by <a href=\"http://openstreetmap.org/\">OpenStreetMap</a>, " +
              "under <a href=\"http://creativecommons.org/licenses/by-sa/3.0\">CC BY SA</a>.");
        }
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
