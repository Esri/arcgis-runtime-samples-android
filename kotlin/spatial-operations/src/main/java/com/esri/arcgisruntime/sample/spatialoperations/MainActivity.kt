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

package com.esri.arcgisruntime.sample.spatialoperations

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.esri.arcgisruntime.geometry.*
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.symbology.SimpleFillSymbol
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

  private val inputGeometryOverlay: GraphicsOverlay by lazy { GraphicsOverlay() }
  private val resultGeometryOverlay: GraphicsOverlay by lazy { GraphicsOverlay() }

  // simple black (0xFF000000) line symbol for outlines
  private val lineSymbol = SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, -0x1000000, 1f)
  private val resultFillSymbol = SimpleFillSymbol(
    SimpleFillSymbol.Style.SOLID, -0x16e0e1,
    lineSymbol
  )
  private var inputPolygon1: Polygon? = null
  private var inputPolygon2: Polygon? = null

  // The spatial operation switching menu items.
  private var noOperationMenuItem: MenuItem? = null
  private var intersectionMenuItem: MenuItem? = null
  private var unionMenuItem: MenuItem? = null
  private var differenceMenuItem: MenuItem? = null
  private var symmetricDifferenceMenuItem: MenuItem? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // create ArcGISMap with topographic basemap
    ArcGISMap(Basemap.createLightGrayCanvas()).let {
      mapView.map = it
    }

    // create graphics overlays to show the inputs and results of the spatial operation
    mapView.graphicsOverlays.add(inputGeometryOverlay)
    mapView.graphicsOverlays.add(resultGeometryOverlay)

    // create input polygons and add graphics to display these polygons in an overlay
    createPolygons()

    GeometryEngine.union(inputPolygon1, inputPolygon2).extent.let {
      mapView.setViewpointGeometryAsync(it, 20.0)
    }


  }

  private fun showGeometry(resultGeometry: Geometry) {
    // add a graphic from the result geometry, showing result in red (0xFFE91F1F)
    Graphic(resultGeometry, resultFillSymbol).let {
      resultGeometryOverlay.graphics.add(it)

      // select the result to highlight it
      it.isSelected = true
    }
  }


  private fun createPolygons() {
    // create input polygon 1
    PointCollection(SpatialReferences.getWebMercator()).run {
      add(Point(-13160.0, 6710100.0))
      add(Point(-13300.0, 6710500.0))
      add(Point(-13760.0, 6710730.0))
      add(Point(-14660.0, 6710000.0))
      add(Point(-13960.0, 6709400.0))
      inputPolygon1 = Polygon(this)

    }

    // create and add a blue graphic to show input polygon 1
    SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, -0x66ffff34, lineSymbol).let {
      inputGeometryOverlay.getGraphics().add(Graphic(inputPolygon1, it))
    }

    // outer ring
    val outerRing = Part(PointCollection(SpatialReferences.getWebMercator()).also {
      it.add(Point(-13060.0, 6711030.0))
      it.add(Point(-12160.0, 6710730.0))
      it.add(Point(-13160.0, 6709700.0))
      it.add(Point(-14560.0, 6710730.0))
      it.add(Point(-13060.0, 6711030.0))
    })

    // inner ring
    val innerRing = Part(PointCollection(SpatialReferences.getWebMercator()).also {
      it.add(Point(-13060.0, 6710910.0))
      it.add(Point(-12450.0, 6710660.0))
      it.add(Point(-13160.0, 6709900.0))
      it.add(Point(-14160.0, 6710630.0))
      it.add(Point(-13060.0, 6710910.0))
    })

    // add both parts (rings) to a part collection and create a geometry from it
    PartCollection(outerRing).run {
      add(innerRing)
      inputPolygon2 = Polygon(this)
    }

    // create and add a green graphic to show input polygon 2
    SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, -0x66ff6700, lineSymbol).let {
      inputGeometryOverlay.getGraphics().add(Graphic(inputPolygon2, it))
    }

    // center the map view on the input geometries
    GeometryEngine.union(inputPolygon1, inputPolygon2).extent.let {
      mapView.setViewpointGeometryAsync(it, 20.0)
    }

  }

  override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    // Inflate the menu; this adds items to the action bar if it is present.
    menuInflater.inflate(R.menu.menu_main, menu)

    // Get the menu items that perform spatial operations.
    noOperationMenuItem = menu?.getItem(0)
    intersectionMenuItem = menu?.getItem(1)
    unionMenuItem = menu?.getItem(2)
    differenceMenuItem = menu?.getItem(3)
    symmetricDifferenceMenuItem = menu?.getItem(4)

    // set the 'no-op' menu item checked by default
    noOperationMenuItem?.setChecked(true)

    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    // handle menu item selection
    val itemId = item.itemId

    // clear previous operation result
    resultGeometryOverlay.graphics.clear()

    // perform spatial operations and add results as graphics, depending on the option selected
    // if-else is used because this sample is used elsewhere as a Library module
    if (itemId == R.id.action_no_operation) {
      // no spatial operation - graphics have been cleared previously
      noOperationMenuItem?.setChecked(true)
      return true
    } else if (itemId == R.id.action_intersection) {
      intersectionMenuItem?.setChecked(true)
      showGeometry(GeometryEngine.intersection(inputPolygon1, inputPolygon2))
      return true
    } else if (itemId == R.id.action_union) {
      unionMenuItem?.setChecked(true)
      showGeometry(GeometryEngine.union(inputPolygon1, inputPolygon2))
      return true
    } else if (itemId == R.id.action_difference) {
      differenceMenuItem?.setChecked(true)
      // note that the difference method gives different results depending on the order of input geometries
      showGeometry(GeometryEngine.difference(inputPolygon1, inputPolygon2))
      return true
    } else if (itemId == R.id.action_symmetric_difference) {
      symmetricDifferenceMenuItem?.setChecked(true)
      showGeometry(GeometryEngine.symmetricDifference(inputPolygon1, inputPolygon2))
      return true
    } else {
      return super.onOptionsItemSelected(item)
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
