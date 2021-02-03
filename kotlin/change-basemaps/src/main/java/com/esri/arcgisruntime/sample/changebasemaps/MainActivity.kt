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
            BasemapStyle.values().map { it.name.replace("_", " ") }
                .toTypedArray()

        addDrawerItems()
        drawerLayout.addDrawerListener(mDrawerToggle)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
            // set opening basemap title to ARCGIS IMAGERY
            title = mNavigationDrawerItemTitles[0]
        }

        // create a map with the imagery Basemap and set it to the map
        mapView.map = ArcGISMap(BasemapStyle.ARCGIS_IMAGERY)
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

        // create a new Basemap(BasemapStyle.THE_ENUM_SELECTED)
        mapView.map.basemap =
            Basemap(BasemapStyle.valueOf(baseMapTitle.replace(" ", "_")))
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
