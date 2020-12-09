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

package com.esri.arcgisruntime.sample.arcgisvectortiledlayerurl;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import com.esri.arcgisruntime.layers.ArcGISVectorTiledLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.MapView;

public class MainActivity extends AppCompatActivity {

  private MapView mMapView;
  private ArcGISVectorTiledLayer mVectorTiledLayer;

  private String[] mNavigationDrawerItemTitles;
  private DrawerLayout mDrawerLayout;
  private ListView mDrawerList;
  private ActionBarDrawerToggle mDrawerToggle;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // get a reference to the map view
    mMapView = findViewById(R.id.mapView);

    // create new Vector Tiled Layer from service url
    mVectorTiledLayer = new ArcGISVectorTiledLayer(getString(R.string.mid_century_url));

    // set tiled layer as basemap
    Basemap basemap = new Basemap(mVectorTiledLayer);
    // create a map with the basemap
    ArcGISMap map = new ArcGISMap(basemap);
    // set the map to be displayed in this view
    mMapView.setMap(map);
    // create a viewpoint from lat, long, scale
    Viewpoint viewpoint = new Viewpoint(47.606726, -122.335564, 72223.819286);
    // set viewpoint
    mMapView.setViewpoint(viewpoint);

    // inflate navigation drawer
    mNavigationDrawerItemTitles = getResources().getStringArray(R.array.vector_tiled_types);
    mDrawerLayout = findViewById(R.id.drawer_layout);
    mDrawerList = findViewById(R.id.left_drawer);

    // set the adapter for the list view
    mDrawerList.setAdapter(new ArrayAdapter<>(this, R.layout.drawer_list_item, mNavigationDrawerItemTitles));
    // set the list's click listener
    mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

    // set the navigation vector tiled layer item in the navigation drawer to selected
    mDrawerList.setItemChecked(0, true);

    setupDrawer();

    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setHomeButtonEnabled(true);
    }
    setTitle(getString(R.string.vector_tiled_layer, mNavigationDrawerItemTitles[0]));
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
        vectorTiledLayerUrl = getString(R.string.mid_century_url);
        break;
      case 1:
        vectorTiledLayerUrl = getString(R.string.colored_pencil_url);
        break;
      case 2:
        vectorTiledLayerUrl = getString(R.string.newspaper_url);
        break;
      case 3:
        vectorTiledLayerUrl = getString(R.string.nova_url);
        break;
      case 4:
        vectorTiledLayerUrl = getString(R.string.world_street_night_url);
        break;
    }
    // create the new vector tiled layer using the url
    mVectorTiledLayer = new ArcGISVectorTiledLayer(vectorTiledLayerUrl);
    // change the basemap to the new layer
    mMapView.getMap().setBasemap(new Basemap(mVectorTiledLayer));
  }

  private void setupDrawer() {
    mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {

      /** Called when a drawer has settled in a completely open state. */
      public void onDrawerOpened(View drawerView) {
        super.onDrawerOpened(drawerView);
        invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
      }

      /** Called when a drawer has settled in a completely closed state. */
      public void onDrawerClosed(View view) {
        super.onDrawerClosed(view);
        invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
      }
    };

    mDrawerToggle.setDrawerIndicatorEnabled(true);
    mDrawerLayout.addDrawerListener(mDrawerToggle);
  }

  @Override
  protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
    // Sync the toggle state after onRestoreInstanceState has occurred.
    mDrawerToggle.syncState();
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    mDrawerToggle.onConfigurationChanged(newConfig);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.

    // Activate the navigation drawer toggle
    return (mDrawerToggle.onOptionsItemSelected(item)) || super.onOptionsItemSelected(item);
  }
  
}
