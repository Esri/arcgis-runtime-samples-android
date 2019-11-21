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

package com.esri.arcgisruntime.sample.manageoperationallayers;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.layers.ArcGISMapImageLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.LayerList;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.MapView;

public class MainActivity extends AppCompatActivity {

  private static LayerList mOperationalLayers;
  private MapView mMapView;

  /**
   * returns the LayerList associated with the Map
   *
   * @return LayerList containing all the operational layers in the Map
   */
  public static LayerList getOperationalLayerList() {
    return mOperationalLayers;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    ArcGISMapImageLayer imageLayerElevation, imagelayerCensus;
    Button selectLayers;

    // inflate MapView from layout
    mMapView = (MapView) findViewById(R.id.mapView);

    // inflate operational layer selection button from the layout
    selectLayers = (Button) findViewById(R.id.operationallayer);

    // create a map with the BasemapType topographic
    ArcGISMap mMap = new ArcGISMap(Basemap.Type.TOPOGRAPHIC, 34.056295, -117.195800, 14);

    imageLayerElevation = new ArcGISMapImageLayer(getResources().getString(R.string.imagelayer_elevation_url));
    imagelayerCensus = new ArcGISMapImageLayer(getResources().getString(R.string.imagelayer_census_url));

    // get the LayerList from the Map
    mOperationalLayers = mMap.getOperationalLayers();
    // add operational layers to the Map
    mOperationalLayers.add(imageLayerElevation);
    mOperationalLayers.add(imagelayerCensus);

    // set the initial viewpoint on the map
    mMap.setInitialViewpoint(new Viewpoint(new Point(-133e5, 45e5, SpatialReference.create(3857)), 2e7));

    // set the map to be displayed in this view
    mMapView.setMap(mMap);

    selectLayers.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(MainActivity.this, OperationalLayers.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);

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
