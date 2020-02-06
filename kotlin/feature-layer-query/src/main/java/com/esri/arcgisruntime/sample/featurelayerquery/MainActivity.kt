/*
 * Copyright 2020 Esri
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

package com.esri.arcgisruntime.sample.featurelayerquery

import com.esri.arcgisruntime.data.ServiceFeatureTable
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import com.esri.arcgisruntime.concurrent.ListenableFuture
import com.esri.arcgisruntime.data.FeatureQueryResult
import com.esri.arcgisruntime.data.QueryParameters
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.Viewpoint
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception
import java.util.Locale

class MainActivity : AppCompatActivity() {

  companion object {
    private val TAG: String = MainActivity::class.java.simpleName
  }

  private val serviceFeatureTable: ServiceFeatureTable by lazy {
    ServiceFeatureTable(getString(R.string.us_daytime_population_url))
  }

  private val featureLayer: FeatureLayer by lazy {
    FeatureLayer(serviceFeatureTable)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    featureLayer.apply {
      opacity = 0.8f
      maxScale = 10000.0
    }

    mapView.map = ArcGISMap(Basemap.createTopographic()).apply {
      operationalLayers.add(featureLayer)
      initialViewpoint =
        Viewpoint(Point(-11000000.0, 5000000.0, SpatialReferences.getWebMercator()), 100000000.0)
    }

  }

  /**
   * Handle the search intent from the search widget
   */
  override fun onNewIntent(intent: Intent) {
    this.intent = intent

    if (Intent.ACTION_SEARCH == intent.action) {
      intent.getStringExtra(SearchManager.QUERY)?.let {
        if (it.isNotEmpty()) {
          searchForState(it)
        }
      }
    }
  }

  private fun searchForState(searchString: String) {
    // clear any previous selections
    featureLayer.clearSelection()
    // create objects required to do a selection with a query
    val query = QueryParameters()
    // make search case insensitive
    query.whereClause =
      ("upper(STATE_NAME) LIKE '%" + searchString.toUpperCase(Locale.US) + "%'")
    // call select features
    val future: ListenableFuture<FeatureQueryResult> = serviceFeatureTable.queryFeaturesAsync(query)
    // add done loading listener to fire when the selection returns
    future.addDoneListener {
      try {
        // call get on the future to get the result
        val result = future.get()
        // check there are some results
        val resultIterator = result.iterator()
        if (resultIterator.hasNext()) {
          resultIterator.next().run {
            // get the extent of the first feature in the result to zoom to
            val envelope = geometry.extent
            mapView.setViewpointGeometryAsync(envelope, 10.0)
            // select the feature
            featureLayer.selectFeature(this)
          }
        } else {
          "No states found with name: $searchString".also {
            Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            Log.d(TAG, it)
          }
        }
      } catch (e: Exception) {
        "Feature search failed for: $searchString. Error: ${e.message}".also {
          Toast.makeText(this, it, Toast.LENGTH_LONG).show()
          Log.e(TAG, it)
        }

      }
    }
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    // inflate the menu; this adds items to the action bar if it is present.
    menuInflater.inflate(R.menu.menu_main, menu)
    // get the SearchView and set the searchable configuration
    val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager

    (menu.findItem(R.id.action_search).actionView as? SearchView)?.let {
      // assumes current activity is the searchable activity, as specified in manifest
      it.setSearchableInfo(searchManager.getSearchableInfo(componentName))
      it.setIconifiedByDefault(false)
    }

    return true
  }

  override fun onPause() {
    mapView.pause()
    super.onPause()
  }

  override fun onResume() {
    super.onResume()
    mapView.resume()
  }

  override fun onDestroy() {
    mapView.dispose()
    super.onDestroy()
  }

}
