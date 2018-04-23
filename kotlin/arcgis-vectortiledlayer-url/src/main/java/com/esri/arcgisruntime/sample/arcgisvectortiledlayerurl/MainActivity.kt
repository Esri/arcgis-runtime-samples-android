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

package com.esri.arcgisruntime.sample.arcgisvectortiledlayerurl

import android.content.res.Configuration
import android.os.Bundle
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter

import com.esri.arcgisruntime.layers.ArcGISVectorTiledLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.Viewpoint

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

  private var mDrawerToggle: ActionBarDrawerToggle? = null

  private var mNavigationDrawerItemTitles: Array<String>? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // create a map with the basemap and set it to the map view
    mapView.map = ArcGISMap().apply {
      // set vector tiled layer from url as basemap
      basemap = Basemap(ArcGISVectorTiledLayer(getString(R.string.mid_century_url)))
      // create a viewpoint from lat, long, scale
      initialViewpoint = Viewpoint(47.606726, -122.335564, 72223.819286)
    }

    // inflate navigation drawer
    mNavigationDrawerItemTitles = resources.getStringArray(R.array.vector_tiled_types)

    left_drawer.apply {
      // Set the adapter for the list view
      adapter = ArrayAdapter(applicationContext, R.layout.drawer_list_item, mNavigationDrawerItemTitles!!)
      // Set the list's click listener
      onItemClickListener = DrawerItemClickListener()
      // set the navigation vector tiled layer item in the navigation drawer to selected
      setItemChecked(0, true)
    }

    setupDrawer()

    supportActionBar?.let {
      it.setDisplayHomeAsUpEnabled(true)
      it.setHomeButtonEnabled(true)
    }

    title = getString(R.string.vector_tiled_layer, mNavigationDrawerItemTitles!![0])
  }

  override fun onPause() {
    super.onPause()
    mapView.pause()
  }

  override fun onResume() {
    super.onResume()
    mapView.resume()
  }

  override fun onDestroy() {
    super.onDestroy()
    mapView.dispose()
  }

  /**
   * The click listener for ListView in the navigation drawer
   */
  private inner class DrawerItemClickListener : AdapterView.OnItemClickListener {
    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
      selectItem(position)
    }
  }

  private fun selectItem(position: Int) {

    // update selected item and title, then close the drawer
    left_drawer.setItemChecked(position, true)
    title = getString(R.string.vector_tiled_layer, mNavigationDrawerItemTitles!![position])
    drawer_layout.closeDrawer(left_drawer)

    // change the basemap to the new layer
    mapView.map.apply {
      when (position) {
      // create the new vector tiled layer using the url
        0 -> basemap = Basemap(ArcGISVectorTiledLayer(getString(R.string.mid_century_url)))
        1 -> basemap = Basemap(ArcGISVectorTiledLayer(getString(R.string.colored_pencil_url)))
        2 -> basemap = Basemap(ArcGISVectorTiledLayer(getString(R.string.newspaper_url)))
        3 -> basemap = Basemap(ArcGISVectorTiledLayer(getString(R.string.nova_url)))
        4 -> basemap = Basemap(ArcGISVectorTiledLayer(getString(R.string.world_street_night_url)))
      }
    }
  }

  private fun setupDrawer() {
    mDrawerToggle = object : ActionBarDrawerToggle(this, drawer_layout, R.string.drawer_open, R.string.drawer_close) {

      /** Called when a drawer has settled in a completely open state.  */
      override fun onDrawerOpened(drawerView: View) {
        super.onDrawerOpened(drawerView)
        invalidateOptionsMenu() // creates call to onPrepareOptionsMenu()
      }

      /** Called when a drawer has settled in a completely closed state.  */
      override fun onDrawerClosed(view: View) {
        super.onDrawerClosed(view)
        invalidateOptionsMenu() // creates call to onPrepareOptionsMenu()
      }
    }

    mDrawerToggle!!.isDrawerIndicatorEnabled = true
    drawer_layout.addDrawerListener(mDrawerToggle!!)
  }

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)
    // Sync the toggle state after onRestoreInstanceState has occurred.
    mDrawerToggle!!.syncState()
  }

  override fun onConfigurationChanged(newConfig: Configuration) {
    super.onConfigurationChanged(newConfig)
    mDrawerToggle!!.onConfigurationChanged(newConfig)
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    // Inflate the menu; this adds items to the action bar if it is present.
    menuInflater.inflate(R.menu.menu_main, menu)
    return true
  }

  // Activate the navigation drawer toggle
  override fun onOptionsItemSelected(item: MenuItem): Boolean =
      mDrawerToggle!!.onOptionsItemSelected(item) || super.onOptionsItemSelected(item)
}
