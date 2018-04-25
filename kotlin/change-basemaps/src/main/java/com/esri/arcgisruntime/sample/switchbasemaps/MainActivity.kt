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

package com.esri.arcgisruntime.sample.switchbasemaps

import android.content.res.Configuration
import android.os.Bundle
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter

import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

  private var mNavigationDrawerItemTitles: Array<String>? = null

  private var mDrawerToggle: ActionBarDrawerToggle? = null
  private var mActivityTitle: String? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // inflate navigation drawer
    mNavigationDrawerItemTitles = resources.getStringArray(R.array.vector_tiled_types)
    // get app title
    mActivityTitle = title.toString()

    addDrawerItems()
    setupDrawer()

    supportActionBar?.apply {
      setDisplayHomeAsUpEnabled(true)
      setHomeButtonEnabled(true)
      // set opening basemap title to Topographic
      title = mNavigationDrawerItemTitles!![2]
    }

    // create a map with Topographic Basemap and set it to the map
    mapView.map = ArcGISMap(Basemap.Type.TOPOGRAPHIC, 47.6047381, -122.3334255, 12)
  }

  /**
   * Add navigation drawer items
   */
  private fun addDrawerItems() {
    ArrayAdapter(this, android.R.layout.simple_list_item_1, mNavigationDrawerItemTitles!!).apply {
      drawerList.adapter = this
      drawerList.onItemClickListener =
          AdapterView.OnItemClickListener { adapterView, view, position, id -> selectBasemap(position) }
    }
  }

  /**
   * Set up the navigation drawer
   */
  private fun setupDrawer() {

    mDrawerToggle = object : ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close) {

      /** Called when a drawer has settled in a completely open state.  */
      override fun onDrawerOpened(drawerView: View) {
        super.onDrawerOpened(drawerView)
        supportActionBar!!.title = mActivityTitle
        invalidateOptionsMenu() // creates call to onPrepareOptionsMenu()
      }

      /** Called when a drawer has settled in a completely closed state.  */
      override fun onDrawerClosed(view: View) {
        super.onDrawerClosed(view)
        invalidateOptionsMenu() // creates call to onPrepareOptionsMenu()
      }
    }

    mDrawerToggle!!.isDrawerIndicatorEnabled = true
    drawerLayout.addDrawerListener(mDrawerToggle!!)
  }

  /**
   * Select the Basemap item based on position in the navigation drawer
   *
   * @param position order int in navigation drawer
   */
  private fun selectBasemap(position: Int) {
    // update selected item and title, then close the drawer
    drawerList.setItemChecked(position, true)
    drawerLayout.closeDrawer(drawerList)

    // select basemap and title by position
    when (position) {
      0 -> { // position 0 = Streets
        mapView.map.basemap = Basemap.createStreets()
        supportActionBar!!.title = mNavigationDrawerItemTitles!![position]
      }
      1 -> { // position 1 = Navigation Vector
        mapView.map.basemap = Basemap.createNavigationVector()
        supportActionBar!!.title = mNavigationDrawerItemTitles!![position]
      }
      2 -> { // position 2 = Topographic
        mapView.map.basemap = Basemap.createTopographic()
        supportActionBar!!.title = mNavigationDrawerItemTitles!![position]
      }
      3 -> { // position 3 = Topographic Vector
        mapView.map.basemap = Basemap.createTopographicVector()
        supportActionBar!!.title = mNavigationDrawerItemTitles!![position]
      }
      4 -> { // position 3 = Gray Canvas
        mapView.map.basemap = Basemap.createLightGrayCanvas()
        supportActionBar!!.title = mNavigationDrawerItemTitles!![position]
      }
      5 -> { // position 3 = Gray Canvas Vector
        mapView.map.basemap = Basemap.createLightGrayCanvasVector()
        supportActionBar!!.title = mNavigationDrawerItemTitles!![position]
      }
    }
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    // Activate the navigation drawer toggle
    return mDrawerToggle!!.onOptionsItemSelected(item) || super.onOptionsItemSelected(item)
  }

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)
    mDrawerToggle!!.syncState()
  }

  override fun onConfigurationChanged(newConfig: Configuration) {
    super.onConfigurationChanged(newConfig)
    mDrawerToggle!!.onConfigurationChanged(newConfig)
  }

  override fun onResume() {
    super.onResume()
    mapView.resume()
  }

  override fun onPause() {
    super.onPause()
    mapView.pause()

  }

  override fun onDestroy() {
    super.onDestroy()
    mapView.dispose()
  }
}
