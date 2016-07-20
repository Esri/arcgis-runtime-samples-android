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

package com.esri.arcgisruntime.sample.arcgistiledlayerrendermode;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.esri.arcgisruntime.layers.ArcGISTiledLayer;
import com.esri.arcgisruntime.layers.ImageTiledLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.MapView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

  private MapView mMapView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // inflate MapView from layout
    mMapView = (MapView) findViewById(R.id.mapView);

    // create new Tiled Layer from service url
    final ArcGISTiledLayer tiledLayerBaseMap = new ArcGISTiledLayer(
        getResources().getString(R.string.world_topo_service));

    // set tiled layer as basemap
    Basemap basemap = new Basemap(tiledLayerBaseMap);
    // create a map with the basemap
    ArcGISMap map = new ArcGISMap(basemap);
    // create a viewpoint from lat, long, scale
    Viewpoint vp = new Viewpoint(47.606726, -122.335564, 144447.638572);
    // set initial map extent
    map.setInitialViewpoint(vp);
    // set the map to be displayed in this view
    mMapView.setMap(map);

    // populate the spinner list with possible Render Mode values
    Spinner rendermodesSpinner = (Spinner) findViewById(R.id.rendermodesspinner);
    List<String> mRendermodeSpinnerList = new ArrayList<>();
    mRendermodeSpinnerList.add("RenderMode - AESTHETIC");
    mRendermodeSpinnerList.add("RenderMode - SCALE");

    // initialize the adapter for the rendermodes spinner
    ArrayAdapter<String> mDataAdapter = new ArrayAdapter<>(this,
        R.layout.spinner_item, mRendermodeSpinnerList);
    rendermodesSpinner.setAdapter(mDataAdapter);

    // when an item is selected in the spinner set the respective tiled layer Render mode
    rendermodesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
          case 0:
            tiledLayerBaseMap.setRenderMode(ImageTiledLayer.RenderMode.AESTHETIC);
            break;
          case 1:
            tiledLayerBaseMap.setRenderMode(ImageTiledLayer.RenderMode.SCALE);
            break;
        }
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {
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
}