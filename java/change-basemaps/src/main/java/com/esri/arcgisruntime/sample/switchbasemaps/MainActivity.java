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

package com.esri.arcgisruntime.sample.switchbasemaps;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.MapView;

public class MainActivity extends AppCompatActivity {

  private MapView mMapView;
  private ArcGISMap mMap;

  private String[] mNavigationDrawerItemTitles;

  private ListView mDrawerList;

  private ActionBarDrawerToggle mDrawerToggle;
  private DrawerLayout mDrawerLayout;
  private String mActivityTitle;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // authentication with an API key or named user is required to access basemaps and other
    // location services
    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY);

    // inflate navigation drawer
    mNavigationDrawerItemTitles = getResources().getStringArray(R.array.basemap_styles);
    mDrawerList = findViewById(R.id.navList);
    mDrawerLayout = findViewById(R.id.drawer_layout);
    // get app title
    mActivityTitle = getTitle().toString();

    addDrawerItems();
    setupDrawer();

    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setHomeButtonEnabled(true);
      // set opening basemap title to Topographic
      getSupportActionBar().setTitle(mNavigationDrawerItemTitles[2]);
    }

    // inflate MapView from layout
    mMapView = findViewById(R.id.mapView);
    // create a map with Topographic Basemap
    mMap = new ArcGISMap(BasemapStyle.ARCGIS_TOPOGRAPHIC);
    // set the map to be displayed in this view
    mMapView.setMap(mMap);
    // set a viewpoint around Seattle
    mMapView.setViewpoint(new Viewpoint( 47.6047381, -122.3334255, 100000));
  }

  /**
   * Add navigation drawer items
   */
  private void addDrawerItems() {
    ArrayAdapter<String> mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
        mNavigationDrawerItemTitles);
    mDrawerList.setAdapter(mAdapter);

    mDrawerList.setOnItemClickListener((adapterView, view, position, id) -> selectBasemap(position));
  }

  /**
   * Set up the navigation drawer
   */
  private void setupDrawer() {

    mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {

      /** Called when a drawer has settled in a completely open state. */
      @Override public void onDrawerOpened(View drawerView) {
        super.onDrawerOpened(drawerView);
        getSupportActionBar().setTitle(mActivityTitle);
        invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
      }

      /** Called when a drawer has settled in a completely closed state. */
      @Override public void onDrawerClosed(View view) {
        super.onDrawerClosed(view);
        invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
      }
    };

    mDrawerToggle.setDrawerIndicatorEnabled(true);
    mDrawerLayout.addDrawerListener(mDrawerToggle);
  }

  /**
   * Select the Basemap item based on position in the navigation drawer
   *
   * @param position order int in navigation drawer
   */
  private void selectBasemap(int position) {
    // update selected item and title, then close the drawer
    mDrawerList.setItemChecked(position, true);
    mDrawerLayout.closeDrawer(mDrawerList);

    // if-else is used because this sample is used elsewhere as a Library module
    if (position == 0) {
      // position 0 = Streets
      mMap.setBasemap(new Basemap(BasemapStyle.ARCGIS_STREETS));
      getSupportActionBar().setTitle(mNavigationDrawerItemTitles[0]);
    } else if (position == 1) {
      // position 1 = Navigation Vector
      mMap.setBasemap(new Basemap(BasemapStyle.ARCGIS_NAVIGATION));
      getSupportActionBar().setTitle(mNavigationDrawerItemTitles[1]);
    } else if (position == 2) {
      // position 2 = Topographic
      mMap.setBasemap(new Basemap(BasemapStyle.ARCGIS_TOPOGRAPHIC));
      getSupportActionBar().setTitle(mNavigationDrawerItemTitles[2]);
    } else if (position == 3) {
      // position 3 = Terrain
      mMap.setBasemap(new Basemap(BasemapStyle.ARCGIS_TERRAIN));
      getSupportActionBar().setTitle(mNavigationDrawerItemTitles[3]);
    } else if (position == 4) {
      // position 4 = Gray Canvas
      mMap.setBasemap(new Basemap(BasemapStyle.ARCGIS_LIGHT_GRAY));
      getSupportActionBar().setTitle(mNavigationDrawerItemTitles[4]);
    } else if (position == 5) {
      // position 5 = OSM Light Gray
      mMap.setBasemap(new Basemap(BasemapStyle.OSM_LIGHT_GRAY));
      getSupportActionBar().setTitle(mNavigationDrawerItemTitles[5]);
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Activate the navigation drawer toggle
    return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
  }

  @Override
  protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
    mDrawerToggle.syncState();
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    mDrawerToggle.onConfigurationChanged(newConfig);
  }

  @Override
  protected void onResume() {
    super.onResume();
    mMapView.resume();
  }

  @Override
  protected void onPause() {
    super.onPause();
    mMapView.pause();

  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mMapView.dispose();
  }
}
