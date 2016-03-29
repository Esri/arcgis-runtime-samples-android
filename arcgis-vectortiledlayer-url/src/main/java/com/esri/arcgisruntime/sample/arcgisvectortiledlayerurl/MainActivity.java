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

package com.esri.arcgisruntime.sample.arcgisvectortiledlayerurl;

import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.esri.arcgisruntime.layers.ArcGISVectorTiledLayer;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Map;
import com.esri.arcgisruntime.mapping.view.MapView;

public class MainActivity extends AppCompatActivity {

  private MapView mMapView;
  private ArcGISVectorTiledLayer mVectorTiledLayer;

  private String[] mNavigationDrawerItemTitles;
  private DrawerLayout mDrawerLayout;
  private ListView mDrawerList;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // inflate MapView from layout
    mMapView = (MapView) findViewById(R.id.mapView);

    // create new Vector Tiled Layer from service url
    mVectorTiledLayer = new ArcGISVectorTiledLayer(
        getResources().getString(R.string.navigation_url));

    // set tiled layer as basemap
    Basemap basemap = new Basemap(mVectorTiledLayer);
    // create a map with the basemap
    Map map = new Map(basemap);
    // set the map to be displayed in this view
    mMapView.setMap(map);

    // inflate navigation drawer
    mNavigationDrawerItemTitles= getResources().getStringArray(R.array.vector_tiled_types);
    mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
    mDrawerList = (ListView) findViewById(R.id.left_drawer);

    // Set the adapter for the list view
    mDrawerList.setAdapter(new ArrayAdapter<String>(this,
        R.layout.drawer_list_item, mNavigationDrawerItemTitles));
    // Set the list's click listener
    mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

    // set the navigation vector tiled layer item in the navigation drawer to selected
    mDrawerList.setItemChecked(0, true);
  }

  /**
   * The click listener for ListView in the navigation drawer
   */
  private class DrawerItemClickListener implements ListView.OnItemClickListener {
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
      selectItem(position);
    }
  }

  private void selectItem(int position) {

    // update selected item and title, then close the drawer
    mDrawerList.setItemChecked(position, true);
    setTitle(getString(R.string.vector_tiled_layer, mNavigationDrawerItemTitles[position]));
    mDrawerLayout.closeDrawer(mDrawerList);

    // update the MapView with the selected vector tiled layer type
    String vectorTiledLayerUrl = null;

    switch (position) {
      case 0:
        vectorTiledLayerUrl = getResources().getString(R.string.navigation_url);
        break;
      case 1:
        vectorTiledLayerUrl = getResources().getString(R.string.streets_url);
        break;
      case 2:
        vectorTiledLayerUrl = getResources().getString(R.string.night_url);
        break;
      case 3:
        vectorTiledLayerUrl = getResources().getString(R.string.topographic_url);
        break;
    }
    // create the new vector tiled layer using the url
    mVectorTiledLayer = new ArcGISVectorTiledLayer(vectorTiledLayerUrl);
    // change the basemap to the new layer
    mMapView.getMap().setBasemap(new Basemap(mVectorTiledLayer));
  }

}
