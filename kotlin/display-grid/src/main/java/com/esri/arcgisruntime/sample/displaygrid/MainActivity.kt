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
import android.widget.Button
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReference
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.*
import com.esri.arcgisruntime.symbology.LineSymbol
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.symbology.TextSymbol
import com.esri.arcgisruntime.sample.displaygrid.databinding.ActivityMainBinding
import com.esri.arcgisruntime.sample.displaygrid.databinding.PopupMenuBinding

class MainActivity : AppCompatActivity() {
    private var lineColor = 0
    private var labelColor = 0
    private var labelPosition = Grid.LabelPosition.ALL_SIDES
    private var isLabelVisible = true

    private val activityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val mapView: MapView by lazy {
        activityMainBinding.mapView
    }

    private val menuButton: Button by lazy {
        activityMainBinding.menuButton
    }

    // create a point to focus the map on in Quebec province
    private val center: Point by lazy {
        Point(
            -7702852.905619,
            6217972.345771,
            SpatialReference.create(3857)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityMainBinding.root)

        // authentication with an API key or named user is required to access basemaps and other
        // location services
        ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY)

        mapView.apply {
            // create a map with imagery basemap
            map = ArcGISMap(BasemapStyle.ARCGIS_IMAGERY)
            // set the initial viewpoint of the map
            setViewpoint(Viewpoint(center, 23227.0))
            // set defaults on grid
            grid = LatitudeLongitudeGrid()
        }

        // set up a popup menu to manage grid settings
        val builder =
            AlertDialog.Builder(this@MainActivity)
        val popUpMenuBinding = PopupMenuBinding.inflate(layoutInflater)

        builder.setView(popUpMenuBinding.root)
        val dialog = builder.create()

        // set up options in popup menu
        // create drop-down list of different layer types
        setupLayerSpinner(popUpMenuBinding)

        // create drop-down list of different line colors
        setupLineColorSpinner(popUpMenuBinding)

        // create drop-down list of different label colors
        setupLabelColorSpinner(popUpMenuBinding)

        // create drop-down list of different label positions
        setupLabelPositionSpinner(popUpMenuBinding)

        // setup the checkbox to change the visibility of the labels
        setupLabelsCheckbox(popUpMenuBinding)

        // display pop-up box when button is clicked
        menuButton.setOnClickListener { dialog.show() }
    }

    /**
     * Sets up the spinner for selecting a grid type
     * and handles behavior for when a new grid type is selected.
     *
     * @param popupMenuBinding the popup binding inflated in onCreate()
     */
    private fun setupLayerSpinner(popupMenuBinding: PopupMenuBinding) {
        popupMenuBinding.layerSpinner.apply {
            // create drop-down list of different grids
            adapter = ArrayAdapter(
                this@MainActivity, android.R.layout.simple_spinner_item,
                resources.getStringArray(R.array.layers_array)
            ).also { gridAdapter ->
                gridAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            // change between different grids on the mapView
            onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    // set the grid type and move to the starting point over 1 second.
                    when (position) {
                        0 -> {
                            // LatitudeLongitudeGrid can have a label format of DECIMAL_DEGREES or DEGREES_MINUTES_SECONDS
                            mapView.grid = LatitudeLongitudeGrid().apply {
                                labelFormat = LatitudeLongitudeGrid.LabelFormat.DECIMAL_DEGREES
                            }
                            mapView.setViewpointAsync(Viewpoint(center, 23227.0), 1f)
                        }
                        1 -> {
                            mapView.grid = MgrsGrid()
                            mapView.setViewpointAsync(Viewpoint(center, 23227.0), 1f)
                        }
                        2 -> {
                            mapView.grid = UtmGrid()
                            mapView.setViewpointAsync(Viewpoint(center, 10000000.0), 1f)
                        }
                        3 -> {
                            mapView.grid = UsngGrid()
                            mapView.setViewpointAsync(Viewpoint(center, 23227.0), 1f)
                        }
                        else -> Toast.makeText(
                            this@MainActivity,
                            "Unsupported option",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    // make sure settings persist on grid type change
                    setLabelVisibility(isLabelVisible)
                    changeGridColor(lineColor)
                    changeLabelColor(labelColor)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }

    /**
     * Sets up the spinner for selecting a line color and handles behavior for when a new line color is selected.
     *
     * @param popupMenuBinding the popup binding inflated in onCreate()
     */
    private fun setupLineColorSpinner(popupMenuBinding: PopupMenuBinding) {
        popupMenuBinding.lineColorSpinner.apply {
            // create drop-down list of different line colors
            adapter = ArrayAdapter(
                this@MainActivity, android.R.layout.simple_spinner_item,
                resources.getStringArray(R.array.colors_array)
            ).also { colorAdapter ->
                colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }

            // change grid lines color
            onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View,
                    position: Int,
                    id: Long
                ) { // set the color
                    when (position) {
                        0 -> lineColor = Color.RED
                        1 -> lineColor = Color.WHITE
                        2 -> lineColor = Color.BLUE
                        else -> Toast.makeText(
                            this@MainActivity,
                            "Unsupported option",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    changeGridColor(lineColor)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }

    /**
     * Sets up the spinner for selecting a label color
     * and handles behavior for when a new label color is selected.
     *
     * @param popupMenuBinding the popup binding inflated in onCreate()
     */
    private fun setupLabelColorSpinner(popupMenuBinding: PopupMenuBinding) {
        popupMenuBinding.labelColorSpinner.apply {
            adapter = ArrayAdapter(
                this@MainActivity, android.R.layout.simple_spinner_item,
                resources.getStringArray(R.array.colors_array)
            ).also { labelColorAdapter ->
                labelColorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }

            // change grid labels color
            onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    // set the color
                    when (position) {
                        0 -> labelColor = Color.RED
                        1 -> labelColor = Color.WHITE
                        2 -> labelColor = Color.BLUE
                        else -> Toast.makeText(
                            this@MainActivity,
                            "Unsupported option",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    changeLabelColor(labelColor)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }

    /**
     * Sets up the spinner for selecting a label position relative to the grid
     * and handles behavior for when a label position is selected.
     *
     * @param popupMenuBinding the popup binding inflated in onCreate()
     */
    private fun setupLabelPositionSpinner(popupMenuBinding: PopupMenuBinding) {
        popupMenuBinding.labelPositionSpinner.apply {
            adapter = ArrayAdapter(
                this@MainActivity, android.R.layout.simple_spinner_item,
                resources.getStringArray(R.array.positions_array)
            ).also { positionAdapter ->
                positionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }

            onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    // set the label position
                    when (position) {
                        0 -> labelPosition = Grid.LabelPosition.ALL_SIDES
                        1 -> labelPosition = Grid.LabelPosition.BOTTOM_LEFT
                        2 -> labelPosition = Grid.LabelPosition.BOTTOM_RIGHT
                        3 -> labelPosition = Grid.LabelPosition.CENTER
                        4 -> labelPosition = Grid.LabelPosition.GEOGRAPHIC
                        5 -> labelPosition = Grid.LabelPosition.TOP_LEFT
                        6 -> labelPosition = Grid.LabelPosition.TOP_RIGHT
                        else -> Toast.makeText(
                            this@MainActivity,
                            "Unsupported option",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    changeLabelPosition(labelPosition)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }

    /**
     * Sets up the spinner for the checkbox making labels visible or invisible.
     *
     * @param popupMenuBinding the popup binding inflated in onCreate()
     */
    private fun setupLabelsCheckbox(popupMenuBinding: PopupMenuBinding) {
        popupMenuBinding.labelsCheckBox.apply {
            isChecked = true
            // hide and show label visibility when the checkbox is clicked
            setOnClickListener {
                isLabelVisible = isChecked
                setLabelVisibility(isLabelVisible)
            }
        }
    }

    /**
     * Sets the labels as visible or invisible.
     *
     * @param visible whether the labels should be visible
     */
    private fun setLabelVisibility(visible: Boolean) {
        mapView.grid.isLabelVisible = visible
    }

    /**
     * Sets the color of the grid lines.
     *
     * @param color the integer color to use
     */
    private fun changeGridColor(color: Int) {
        val grid = mapView.grid
        val gridLevels = grid.levelCount
        for (gridLevel in 0 until gridLevels) {
            val lineSymbol: LineSymbol =
                SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, color, (gridLevel + 1).toFloat())
            grid.setLineSymbol(gridLevel, lineSymbol)
        }
    }

    /**
     * Sets the color of the labels on the grid.
     *
     * @param labelColor the integer color to use
     */
    private fun changeLabelColor(labelColor: Int) {
        val grid = mapView.grid
        val gridLevels = grid.levelCount
        for (gridLevel in 0 until gridLevels) {
            val textSymbol = TextSymbol().apply {
                color = labelColor
                size = 14f
                horizontalAlignment = TextSymbol.HorizontalAlignment.LEFT
                verticalAlignment = TextSymbol.VerticalAlignment.BOTTOM
                haloColor = Color.WHITE
                haloWidth = gridLevel + 1.toFloat()
            }
            grid.setTextSymbol(gridLevel, textSymbol)
        }
    }

    /**
     * Sets the position of the labels on the grid.
     *
     * @param labelPosition the LabelPosition to use
     */
    private fun changeLabelPosition(labelPosition: Grid.LabelPosition) {
        mapView.grid.labelPosition = labelPosition
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
