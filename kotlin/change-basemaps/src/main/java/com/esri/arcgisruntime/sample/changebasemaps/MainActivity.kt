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

package com.esri.arcgisruntime.sample.changebasemaps

import android.content.res.Configuration
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

  private lateinit var mNavigationDrawerItemTitles: Array<String>

  private val mDrawerToggle: ActionBarDrawerToggle by lazy { setupDrawer() }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // inflate navigation drawer with all basemap types in a human readable format
    mNavigationDrawerItemTitles =
      Basemap.Type.values().map { it.name.replace("_", " ").toLowerCase().capitalize() }
        .toTypedArray()

    addDrawerItems()
    drawerLayout.addDrawerListener(mDrawerToggle)

    supportActionBar?.apply {
      setDisplayHomeAsUpEnabled(true)
      setHomeButtonEnabled(true)
      // set opening basemap title to Topographic
      title = mNavigationDrawerItemTitles[2]
    }

    // create a map with Topographic Basemap and set it to the map
    mapView.map = ArcGISMap(Basemap.Type.TOPOGRAPHIC, 47.6047381, -122.3334255, 12)
  }

  /**
   * Add navigation drawer items
   */
  private fun addDrawerItems() {
    ArrayAdapter(this, android.R.layout.simple_list_item_1, mNavigationDrawerItemTitles).apply {
      drawerList.adapter = this
      drawerList.onItemClickListener =
        AdapterView.OnItemClickListener { adapterView, view, position, id -> selectBasemap(position) }
    }
  }

  /**
   * Set up the navigation drawer
   */
  private fun setupDrawer() =
    object :
      ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close) {

      override fun isDrawerIndicatorEnabled() = true

      /** Called when a drawer has settled in a completely open state.  */
      override fun onDrawerOpened(drawerView: View) {
        super.onDrawerOpened(drawerView)
        supportActionBar?.title = title
        invalidateOptionsMenu() // creates call to onPrepareOptionsMenu()
      }

      /** Called when a drawer has settled in a completely closed state.  */
      override fun onDrawerClosed(view: View) {
        super.onDrawerClosed(view)
        invalidateOptionsMenu() // creates call to onPrepareOptionsMenu()
      }
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

    // get basemap title by position
    val baseMapTitle = mNavigationDrawerItemTitles[position]
    supportActionBar?.title = baseMapTitle

    // select basemap by title
    mapView.map.basemap =
      when (Basemap.Type.valueOf(baseMapTitle.replace(" ", "_").toUpperCase())) {
        Basemap.Type.DARK_GRAY_CANVAS_VECTOR -> Basemap.createDarkGrayCanvasVector()
        Basemap.Type.IMAGERY -> Basemap.createImagery()
        Basemap.Type.IMAGERY_WITH_LABELS -> Basemap.createImageryWithLabels()
        Basemap.Type.IMAGERY_WITH_LABELS_VECTOR -> Basemap.createImageryWithLabelsVector()
        Basemap.Type.LIGHT_GRAY_CANVAS -> Basemap.createLightGrayCanvas()
        Basemap.Type.LIGHT_GRAY_CANVAS_VECTOR -> Basemap.createDarkGrayCanvasVector()
        Basemap.Type.NATIONAL_GEOGRAPHIC -> Basemap.createNationalGeographic()
        Basemap.Type.NAVIGATION_VECTOR -> Basemap.createNavigationVector()
        Basemap.Type.OCEANS -> Basemap.createOceans()
        Basemap.Type.OPEN_STREET_MAP -> Basemap.createOceans()
        Basemap.Type.STREETS -> Basemap.createStreets()
        Basemap.Type.STREETS_NIGHT_VECTOR -> Basemap.createStreetsNightVector()
        Basemap.Type.STREETS_WITH_RELIEF_VECTOR -> Basemap.createStreetsWithReliefVector()
        Basemap.Type.STREETS_VECTOR -> Basemap.createStreetsVector()
        Basemap.Type.TOPOGRAPHIC -> Basemap.createTopographic()
        Basemap.Type.TERRAIN_WITH_LABELS -> Basemap.createTerrainWithLabels()
        Basemap.Type.TERRAIN_WITH_LABELS_VECTOR -> Basemap.createTerrainWithLabelsVector()
        Basemap.Type.TOPOGRAPHIC_VECTOR -> Basemap.createTopographicVector()
      }
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    // Activate the navigation drawer toggle
    return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item)
  }

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)
    mDrawerToggle.syncState()
  }

  override fun onConfigurationChanged(newConfig: Configuration) {
    super.onConfigurationChanged(newConfig)
    mDrawerToggle.onConfigurationChanged(newConfig)
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
