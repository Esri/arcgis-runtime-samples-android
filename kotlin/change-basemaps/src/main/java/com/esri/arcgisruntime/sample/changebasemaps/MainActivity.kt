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
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.Viewpoint
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

  private lateinit var mNavigationDrawerItemTitles: Array<String>

  private val mDrawerToggle: ActionBarDrawerToggle by lazy { setupDrawer() }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // authentication with an API key or named user is required to access basemaps and other 
    // location services
    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY)

    // inflate navigation drawer with all basemap types in a human readable format
    mNavigationDrawerItemTitles =
      BasemapStyle.values().map { it.name.replace("_", " ").toLowerCase().capitalize() }
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
    mapView.map = ArcGISMap(BasemapStyle.ARCGIS_TOPOGRAPHIC)
    mapView.setViewpoint(Viewpoint(47.6047, -122.3334, 10000000.0))
  }

  /**
   * Add navigation drawer items
   */
  private fun addDrawerItems() {
    ArrayAdapter(this, android.R.layout.simple_list_item_1, mNavigationDrawerItemTitles).apply {
      drawerList.adapter = this
      drawerList.onItemClickListener =
        AdapterView.OnItemClickListener { _, _, position, _ -> selectBasemap(position) }
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
      when (BasemapStyle.valueOf(baseMapTitle.replace(" ", "_").toUpperCase())) {
        BasemapStyle.ARCGIS_CHARTED_TERRITORY -> Basemap(BasemapStyle.ARCGIS_CHARTED_TERRITORY)
        BasemapStyle.ARCGIS_COLORED_PENCIL -> Basemap(BasemapStyle.ARCGIS_COLORED_PENCIL)
        BasemapStyle.ARCGIS_COMMUNITY -> Basemap(BasemapStyle.ARCGIS_COMMUNITY)
        BasemapStyle.ARCGIS_DARK_GRAY -> Basemap(BasemapStyle.ARCGIS_DARK_GRAY)
        BasemapStyle.ARCGIS_DARK_GRAY_BASE -> Basemap(BasemapStyle.ARCGIS_DARK_GRAY_BASE)
        BasemapStyle.ARCGIS_DARK_GRAY_LABELS -> Basemap(BasemapStyle.ARCGIS_DARK_GRAY_LABELS)
        BasemapStyle.ARCGIS_HILLSHADE_DARK -> Basemap(BasemapStyle.ARCGIS_HILLSHADE_DARK)
        BasemapStyle.ARCGIS_HILLSHADE_LIGHT -> Basemap(BasemapStyle.ARCGIS_HILLSHADE_LIGHT)
        BasemapStyle.ARCGIS_IMAGERY -> Basemap(BasemapStyle.ARCGIS_IMAGERY)
        BasemapStyle.ARCGIS_IMAGERY_LABELS -> Basemap(BasemapStyle.ARCGIS_IMAGERY_LABELS)
        BasemapStyle.ARCGIS_IMAGERY_STANDARD -> Basemap(BasemapStyle.ARCGIS_IMAGERY_STANDARD)
        BasemapStyle.ARCGIS_LIGHT_GRAY -> Basemap(BasemapStyle.ARCGIS_LIGHT_GRAY)
        BasemapStyle.ARCGIS_LIGHT_GRAY_BASE -> Basemap(BasemapStyle.ARCGIS_LIGHT_GRAY_BASE)
        BasemapStyle.ARCGIS_LIGHT_GRAY_LABELS -> Basemap(BasemapStyle.ARCGIS_LIGHT_GRAY_LABELS)
        BasemapStyle.ARCGIS_MIDCENTURY -> Basemap(BasemapStyle.ARCGIS_MIDCENTURY)
        BasemapStyle.ARCGIS_MODERN_ANTIQUE -> Basemap(BasemapStyle.ARCGIS_MODERN_ANTIQUE)
        BasemapStyle.ARCGIS_NAVIGATION -> Basemap(BasemapStyle.ARCGIS_NAVIGATION)
        BasemapStyle.ARCGIS_NAVIGATION_NIGHT -> Basemap(BasemapStyle.ARCGIS_NAVIGATION_NIGHT)
        BasemapStyle.ARCGIS_NEWSPAPER -> Basemap(BasemapStyle.ARCGIS_NEWSPAPER)
        BasemapStyle.ARCGIS_NOVA -> Basemap(BasemapStyle.ARCGIS_NOVA)
        BasemapStyle.ARCGIS_OCEANS -> Basemap(BasemapStyle.ARCGIS_OCEANS)
        BasemapStyle.ARCGIS_OCEANS_BASE -> Basemap(BasemapStyle.ARCGIS_OCEANS_BASE)
        BasemapStyle.ARCGIS_OCEANS_LABELS -> Basemap(BasemapStyle.ARCGIS_OCEANS_LABELS)
        BasemapStyle.ARCGIS_STREETS -> Basemap(BasemapStyle.ARCGIS_STREETS)
        BasemapStyle.ARCGIS_STREETS_NIGHT -> Basemap(BasemapStyle.ARCGIS_STREETS_NIGHT)
        BasemapStyle.ARCGIS_STREETS_RELIEF -> Basemap(BasemapStyle.ARCGIS_STREETS_RELIEF)
        BasemapStyle.ARCGIS_TERRAIN -> Basemap(BasemapStyle.ARCGIS_TERRAIN)
        BasemapStyle.ARCGIS_TERRAIN_BASE -> Basemap(BasemapStyle.ARCGIS_TERRAIN_BASE)
        BasemapStyle.ARCGIS_TERRAIN_DETAIL -> Basemap(BasemapStyle.ARCGIS_TERRAIN_DETAIL)
        BasemapStyle.ARCGIS_TOPOGRAPHIC -> Basemap(BasemapStyle.ARCGIS_TOPOGRAPHIC)
        BasemapStyle.OSM_DARK_GRAY -> Basemap(BasemapStyle.OSM_DARK_GRAY)
        BasemapStyle.OSM_DARK_GRAY_BASE -> Basemap(BasemapStyle.OSM_DARK_GRAY_BASE)
        BasemapStyle.OSM_DARK_GRAY_LABELS -> Basemap(BasemapStyle.OSM_DARK_GRAY_LABELS)
        BasemapStyle.OSM_LIGHT_GRAY -> Basemap(BasemapStyle.OSM_LIGHT_GRAY)
        BasemapStyle.OSM_LIGHT_GRAY_BASE -> Basemap(BasemapStyle.OSM_LIGHT_GRAY_BASE)
        BasemapStyle.OSM_LIGHT_GRAY_LABELS -> Basemap(BasemapStyle.OSM_LIGHT_GRAY_LABELS)
        BasemapStyle.OSM_STANDARD -> Basemap(BasemapStyle.OSM_STANDARD)
        BasemapStyle.OSM_STANDARD_RELIEF -> Basemap(BasemapStyle.OSM_STANDARD_RELIEF)
        BasemapStyle.OSM_STANDARD_RELIEF_BASE -> Basemap(BasemapStyle.OSM_STANDARD_RELIEF_BASE)
        BasemapStyle.OSM_STREETS -> Basemap(BasemapStyle.OSM_STREETS)
        BasemapStyle.OSM_STREETS_RELIEF -> Basemap(BasemapStyle.OSM_STREETS_RELIEF)
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
