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

package com.esri.arcgisruntime.sample.performspatialoperations

import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.geometry.Geometry
import com.esri.arcgisruntime.geometry.GeometryEngine
import com.esri.arcgisruntime.geometry.Part
import com.esri.arcgisruntime.geometry.PartCollection
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.PointCollection
import com.esri.arcgisruntime.geometry.Polygon
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.symbology.SimpleFillSymbol
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

  private val inputGeometryGraphicsOverlay: GraphicsOverlay by lazy { GraphicsOverlay() }
  private val resultGeometryGraphicsOverlay: GraphicsOverlay by lazy { GraphicsOverlay() }

  // simple black line symbol for outlines
  private val lineSymbol = SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLACK, 1f)
  private val resultFillSymbol = SimpleFillSymbol(
    SimpleFillSymbol.Style.SOLID, Color.RED, lineSymbol
  )
  private lateinit var inputPolygon1: Polygon
  private lateinit var inputPolygon2: Polygon

  // the spatial operation switching menu items.
  private var noOperationMenuItem: MenuItem? = null
  private var intersectionMenuItem: MenuItem? = null
  private var unionMenuItem: MenuItem? = null
  private var differenceMenuItem: MenuItem? = null
  private var symmetricDifferenceMenuItem: MenuItem? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // authentication with an API key or named user is required to access basemaps and other
    // location services
    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY)

    mapView.apply {
      // create an ArcGISMap with a light gray basemap
      map = ArcGISMap(BasemapStyle.ARCGIS_LIGHT_GRAY)

      // create graphics overlays to show the inputs and results of the spatial operation
      graphicsOverlays.add(inputGeometryGraphicsOverlay)
      graphicsOverlays.add(resultGeometryGraphicsOverlay)

    }

    // create input polygons and add graphics to display these polygons in an overlay
    createPolygons()

    // center the map view on the input geometries
    val envelope = GeometryEngine.union(inputPolygon1, inputPolygon2).extent
    mapView.setViewpointGeometryAsync(envelope, 20.0)
  }

  private fun showGeometry(resultGeometry: Geometry) {
    // add a graphic from the result geometry, showing result in red (0xFFE91F1F)
    val graphic = Graphic(resultGeometry, resultFillSymbol).also {
      // select the result to highlight it
      it.isSelected = true
    }

    resultGeometryGraphicsOverlay.graphics.add(graphic)
  }

  private fun createPolygons() {
    // create input polygon 1
    inputPolygon1 = Polygon(PointCollection(SpatialReferences.getWebMercator()).apply {
      add(Point(-13160.0, 6710100.0))
      add(Point(-13300.0, 6710500.0))
      add(Point(-13760.0, 6710730.0))
      add(Point(-14660.0, 6710000.0))
      add(Point(-13960.0, 6709400.0))

    })

    // create and add a blue graphic to show input polygon 1
    val blueFill = SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, Color.BLUE, lineSymbol)
    inputGeometryGraphicsOverlay.graphics.add(Graphic(inputPolygon1, blueFill))

    // outer ring
    val outerRing = Part(PointCollection(SpatialReferences.getWebMercator()).apply {
      add(Point(-13060.0, 6711030.0))
      add(Point(-12160.0, 6710730.0))
      add(Point(-13160.0, 6709700.0))
      add(Point(-14560.0, 6710730.0))
      add(Point(-13060.0, 6711030.0))
    })

    // inner ring
    val innerRing = Part(PointCollection(SpatialReferences.getWebMercator()).apply {
      add(Point(-13060.0, 6710910.0))
      add(Point(-12450.0, 6710660.0))
      add(Point(-13160.0, 6709900.0))
      add(Point(-14160.0, 6710630.0))
      add(Point(-13060.0, 6710910.0))
    })

    // add both parts (rings) to a part collection and create a geometry from it
    PartCollection(outerRing).run {
      add(innerRing)
      inputPolygon2 = Polygon(this)
    }

    // create and add a green graphic to show input polygon 2
    val greenFill = SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, Color.GREEN, lineSymbol)
    inputGeometryGraphicsOverlay.graphics.add(Graphic(inputPolygon2, greenFill))

  }

  override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    // inflate the menu; this adds items to the action bar if it is present.
    menuInflater.inflate(R.menu.menu_main, menu)

    // Get the menu items that perform spatial operations.
    noOperationMenuItem = menu?.getItem(0)
    intersectionMenuItem = menu?.getItem(1)
    unionMenuItem = menu?.getItem(2)
    differenceMenuItem = menu?.getItem(3)
    symmetricDifferenceMenuItem = menu?.getItem(4)

    // set the 'no-op' menu item checked by default
    noOperationMenuItem?.isChecked = true

    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    // handle menu item selection
    val itemId = item.itemId

    // clear previous operation result
    resultGeometryGraphicsOverlay.graphics.clear()

    // perform spatial operations and add results as graphics, depending on the option selected
    when (itemId) {
      R.id.action_no_operation -> {
        // no spatial operation - graphics have been cleared previously
        noOperationMenuItem?.isChecked = true
        return true
      }
      R.id.action_intersection -> {
        intersectionMenuItem?.isChecked = true
        showGeometry(GeometryEngine.intersection(inputPolygon1, inputPolygon2))
        return true
      }
      R.id.action_union -> {
        unionMenuItem?.isChecked = true
        showGeometry(GeometryEngine.union(inputPolygon1, inputPolygon2))
        return true
      }
      R.id.action_difference -> {
        differenceMenuItem?.isChecked = true
        // note that the difference method gives different results depending on the order of input geometries
        showGeometry(GeometryEngine.difference(inputPolygon1, inputPolygon2))
        return true
      }
      R.id.action_symmetric_difference -> {
        symmetricDifferenceMenuItem?.isChecked = true
        showGeometry(GeometryEngine.symmetricDifference(inputPolygon1, inputPolygon2))
        return true
      }
      else -> {
        return super.onOptionsItemSelected(item)
      }
    }
  }

  override fun onResume() {
    super.onResume()
    mapView.resume()
  }

  override fun onPause() {
    mapView.pause()
    super.onPause()
  }

  override fun onDestroy() {
    mapView.dispose()
    super.onDestroy()
  }
}
