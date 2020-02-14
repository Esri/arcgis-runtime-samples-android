/* Copyright 2020 Esri
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
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReference
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.LatitudeLongitudeGrid
import com.esri.arcgisruntime.mapping.view.MgrsGrid
import com.esri.arcgisruntime.mapping.view.UsngGrid
import com.esri.arcgisruntime.mapping.view.UtmGrid
import com.esri.arcgisruntime.symbology.LineSymbol
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.symbology.TextSymbol
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.popup_menu.view.*

class MainActivity : AppCompatActivity() {
  private var lineColor = 0
  private var labelColor = 0
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    // set up a popup menu to manage grid settings
    val builder =
      AlertDialog.Builder(this@MainActivity)
    val popupView = layoutInflater.inflate(R.layout.popup_menu, null)
    builder.setView(popupView)
    val dialog = builder.create()
    // create drop-down list of different grids
    val adapter = ArrayAdapter(
      this@MainActivity, android.R.layout.simple_spinner_item,
      resources.getStringArray(R.array.layers_array)
    )
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    popupView.layer_spinner.adapter = adapter
    // create drop-down list of different colors
    val colorAdapter = ArrayAdapter(
      this@MainActivity, android.R.layout.simple_spinner_item,
      resources.getStringArray(R.array.colors_array)
    )
    colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    popupView.line_color_spinner.adapter = colorAdapter
    // create drop-down list of different label colors
    val labelColorAdapter = ArrayAdapter(
      this@MainActivity, android.R.layout.simple_spinner_item,
      resources.getStringArray(R.array.colors_array)
    )
    labelColorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    popupView.label_color_spinner.adapter = labelColorAdapter
    // create a map with imagery basemap
    val map = ArcGISMap(Basemap.createImagery())
    // set viewpoint
    val center = Point(
      -7702852.905619,
      6217972.345771,
      23227.0,
      SpatialReference.create(3857)
    )
    map.initialViewpoint = Viewpoint(center, 23227.0)
    // set the map to be displayed in this view
    mapView.setMap(map)
    // set defaults on grid
    mapView.setGrid(LatitudeLongitudeGrid())
    popupView.labels_checkBox.setChecked(true)
    // change different grids on the Map View
    popupView.layer_spinner.onItemSelectedListener = object : OnItemSelectedListener {
      override fun onItemSelected(
        parent: AdapterView<*>?,
        view: View,
        position: Int,
        id: Long
      ) { // set the grid type
        when (position) {
          0 -> {
            mapView.setGrid(LatitudeLongitudeGrid())
            mapView.setViewpointScaleAsync(23227.0)
          }
          1 -> {
            mapView.setGrid(MgrsGrid())
            mapView.setViewpointScaleAsync(23227.0)
          }
          2 -> {
            mapView.setGrid(UtmGrid())
            mapView.setViewpointScaleAsync(10000000.0)
          }
          3 -> {
            mapView.setGrid(UsngGrid())
            mapView.setViewpointScaleAsync(23227.0)
          }
          else -> Toast.makeText(this@MainActivity, "Unsupported option", Toast.LENGTH_SHORT).show()
        }
        // make sure settings persist on grid type change
        setLabelVisibility(popupView.labels_checkBox.isChecked)
        changeGridColor(lineColor)
        changeLabelColor(labelColor)
      }

      override fun onNothingSelected(parent: AdapterView<*>?) {}
    }
    // change grid lines color
    popupView.line_color_spinner.onItemSelectedListener = object : OnItemSelectedListener {
      override fun onItemSelected(
        parent: AdapterView<*>?,
        view: View,
        position: Int,
        id: Long
      ) { //set the color
        when (position) {
          0 -> lineColor = Color.RED
          1 -> lineColor = Color.WHITE
          2 -> lineColor = Color.BLUE
          else -> Toast.makeText(this@MainActivity, "Unsupported option", Toast.LENGTH_SHORT).show()
        }
        changeGridColor(lineColor)
      }

      override fun onNothingSelected(parent: AdapterView<*>?) {}
    }
    // change grid labels color
    popupView.label_color_spinner.onItemSelectedListener = object : OnItemSelectedListener {
      override fun onItemSelected(
        parent: AdapterView<*>?,
        view: View,
        position: Int,
        id: Long
      ) { // set the color
        when (position) {
          0 -> labelColor = Color.RED
          1 -> labelColor = Color.WHITE
          2 -> labelColor = Color.BLUE
          else -> Toast.makeText(this@MainActivity, "Unsupported option", Toast.LENGTH_SHORT).show()
        }
        changeLabelColor(labelColor)
      }

      override fun onNothingSelected(parent: AdapterView<*>?) {}
    }
    // hide and show label visibility when the checkbox is clicked
    popupView.labels_checkBox.setOnClickListener(View.OnClickListener { v: View? ->
      setLabelVisibility(
        popupView.labels_checkBox.isChecked
      )
    })
    // display pop-up box when button is clicked
    menu_button.setOnClickListener { v: View? -> dialog.show() }
  }

  private fun setLabelVisibility(visible: Boolean) {
    mapView.grid.isLabelVisible = visible
  }

  private fun changeGridColor(color: Int) {
    val grid = mapView.grid
    val gridLevels = grid.levelCount
    for (gridLevel in 0 until gridLevels) {
      val lineSymbol: LineSymbol =
        SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, color, (gridLevel + 1).toFloat())
      grid.setLineSymbol(gridLevel, lineSymbol)
    }
  }

  private fun changeLabelColor(color: Int) {
    val grid = mapView.grid
    val gridLevels = grid.levelCount
    for (gridLevel in 0 until gridLevels) {
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