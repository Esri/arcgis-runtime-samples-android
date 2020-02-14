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
package com.esri.arcgisruntime.sample.displaygrid

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReference
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.LatitudeLongitudeGrid
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.mapping.view.MgrsGrid
import com.esri.arcgisruntime.mapping.view.UsngGrid
import com.esri.arcgisruntime.mapping.view.UtmGrid
import com.esri.arcgisruntime.symbology.LineSymbol
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.symbology.TextSymbol

class MainActivity : AppCompatActivity() {
  private var mMapView: MapView? = null
  private var mLabelsCheckBox: CheckBox? = null
  private var mLineColor = 0
  private var mLabelColor = 0
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    // inflate views from activity_main
    mMapView = findViewById(R.id.mapView)
    val mMenuButton = findViewById<Button>(R.id.menu_button)
    // set up a popup menu to manage grid settings
    val builder =
      AlertDialog.Builder(this@MainActivity)
    val view = layoutInflater.inflate(R.layout.popup_menu, null)
    builder.setView(view)
    val dialog = builder.create()
    // inflate views from popup_menu
    val mGridSpinner = view.findViewById<Spinner>(R.id.layer_spinner)
    mLabelsCheckBox = view.findViewById(R.id.labels_checkBox)
    val mColorsSpinner = view.findViewById<Spinner>(R.id.line_color_spinner)
    val mLabelColorSpinner = view.findViewById<Spinner>(R.id.label_color_spinner)
    // create drop-down list of different grids
    val adapter = ArrayAdapter(
      this@MainActivity, android.R.layout.simple_spinner_item,
      resources.getStringArray(R.array.layers_array)
    )
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    mGridSpinner.adapter = adapter
    // create drop-down list of different colors
    val colorAdapter = ArrayAdapter(
      this@MainActivity, android.R.layout.simple_spinner_item,
      resources.getStringArray(R.array.colors_array)
    )
    colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    mColorsSpinner.adapter = colorAdapter
    // create drop-down list of different label colors
    val labelColorAdapter = ArrayAdapter(
      this@MainActivity, android.R.layout.simple_spinner_item,
      resources.getStringArray(R.array.colors_array)
    )
    labelColorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    mLabelColorSpinner.adapter = labelColorAdapter
    // create a map with imagery basemap
    val map = ArcGISMap(Basemap.createImagery())
    // set viewpoint
    val center = Point(
      -7702852.905619,
      6217972.345771,
      23227,
      SpatialReference.create(3857)
    )
    map.initialViewpoint = Viewpoint(center, 23227)
    // set the map to be displayed in this view
    mMapView.setMap(map)
    // set defaults on grid
    mMapView.setGrid(LatitudeLongitudeGrid())
    mLabelsCheckBox.setChecked(true)
    // change different grids on the Map View
    mGridSpinner.onItemSelectedListener = object : OnItemSelectedListener {
      override fun onItemSelected(
        parent: AdapterView<*>?,
        view: View,
        position: Int,
        id: Long
      ) { // set the grid type
        when (position) {
          0 -> {
            mMapView.setGrid(LatitudeLongitudeGrid())
            mMapView.setViewpointScaleAsync(23227.0)
          }
          1 -> {
            mMapView.setGrid(MgrsGrid())
            mMapView.setViewpointScaleAsync(23227.0)
          }
          2 -> {
            mMapView.setGrid(UtmGrid())
            mMapView.setViewpointScaleAsync(10000000.0)
          }
          3 -> {
            mMapView.setGrid(UsngGrid())
            mMapView.setViewpointScaleAsync(23227.0)
          }
          else -> Toast.makeText(this@MainActivity, "Unsupported option", Toast.LENGTH_SHORT).show()
        }
        // make sure settings persist on grid type change
        setLabelVisibility()
        changeGridColor(mLineColor)
        changeLabelColor(mLabelColor)
      }

      override fun onNothingSelected(parent: AdapterView<*>?) {}
    }
    // change grid lines color
    mColorsSpinner.onItemSelectedListener = object : OnItemSelectedListener {
      override fun onItemSelected(
        parent: AdapterView<*>?,
        view: View,
        position: Int,
        id: Long
      ) { //set the color
        when (position) {
          0 -> mLineColor = Color.RED
          1 -> mLineColor = Color.WHITE
          2 -> mLineColor = Color.BLUE
          else -> Toast.makeText(this@MainActivity, "Unsupported option", Toast.LENGTH_SHORT).show()
        }
        changeGridColor(mLineColor)
      }

      override fun onNothingSelected(parent: AdapterView<*>?) {}
    }
    // change grid labels color
    mLabelColorSpinner.onItemSelectedListener = object : OnItemSelectedListener {
      override fun onItemSelected(
        parent: AdapterView<*>?,
        view: View,
        position: Int,
        id: Long
      ) { // set the color
        when (position) {
          0 -> mLabelColor = Color.RED
          1 -> mLabelColor = Color.WHITE
          2 -> mLabelColor = Color.BLUE
          else -> Toast.makeText(this@MainActivity, "Unsupported option", Toast.LENGTH_SHORT).show()
        }
        changeLabelColor(mLabelColor)
      }

      override fun onNothingSelected(parent: AdapterView<*>?) {}
    }
    // hide and show label visibility when the checkbox is clicked
    mLabelsCheckBox.setOnClickListener(View.OnClickListener { v: View? -> setLabelVisibility() })
    // display pop-up box when button is clicked
    mMenuButton.setOnClickListener { v: View? -> dialog.show() }
  }

  private fun setLabelVisibility() {
    if (mLabelsCheckBox!!.isChecked) {
      mMapView!!.grid.isLabelVisible = true
    } else {
      mMapView!!.grid.isLabelVisible = false
    }
  }

  private fun changeGridColor(color: Int) {
    val grid = mMapView!!.grid
    val gridLevels = grid.levelCount
    for (gridLevel in 0..gridLevels - 1) {
      val lineSymbol: LineSymbol =
        SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, color, (gridLevel + 1).toFloat())
      grid.setLineSymbol(gridLevel, lineSymbol)
    }
  }

  private fun changeLabelColor(color: Int) {
    val grid = mMapView!!.grid
    val gridLevels = grid.levelCount
    for (gridLevel in 0..gridLevels - 1) {
      val textSymbol = TextSymbol()
      textSymbol.color = color
      textSymbol.size = 14f
      textSymbol.horizontalAlignment = TextSymbol.HorizontalAlignment.LEFT
      textSymbol.verticalAlignment = TextSymbol.VerticalAlignment.BOTTOM
      textSymbol.haloColor = Color.WHITE
      textSymbol.haloWidth = gridLevel + 1.toFloat()
      grid.setTextSymbol(gridLevel, textSymbol)
    }
  }

  override fun onPause() {
    super.onPause()
    mMapView!!.pause()
  }

  override fun onResume() {
    super.onResume()
    mMapView!!.resume()
  }

  override fun onDestroy() {
    super.onDestroy()
    mMapView!!.dispose()
  }
}