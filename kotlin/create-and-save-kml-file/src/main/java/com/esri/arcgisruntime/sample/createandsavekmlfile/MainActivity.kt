/*
 * Copyright 2019 Esri
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

package com.esri.arcgisruntime.sample.createandsavekmlfile

import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.geometry.GeometryEngine
import com.esri.arcgisruntime.geometry.GeometryType
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.layers.KmlLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.mapping.view.SketchCreationMode
import com.esri.arcgisruntime.mapping.view.SketchEditor
import com.esri.arcgisruntime.mapping.view.SketchStyle
import com.esri.arcgisruntime.ogc.kml.*
import com.esri.arcgisruntime.sample.createandsavekmlfile.databinding.ActivityMainBinding
import com.esri.arcgisruntime.sample.createandsavekmlfile.databinding.KmlGeometryControlsLayoutBinding
import java.io.File

class MainActivity : AppCompatActivity() {

    private val activityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val mapView: MapView by lazy {
        activityMainBinding.mapView
    }

    private val kmlGeometryControlsLayoutBinding: KmlGeometryControlsLayoutBinding by lazy {
        activityMainBinding.controls
    }

    private val pointSymbolSpinner: Spinner by lazy {
        kmlGeometryControlsLayoutBinding.pointSymbolSpinner
    }

    private val sketchCreationModeSpinner: Spinner by lazy {
        kmlGeometryControlsLayoutBinding.sketchCreationModeSpinner
    }

    private val pointSymbolTextView: TextView by lazy {
        kmlGeometryControlsLayoutBinding.pointSymbolTextView
    }

    private val colorSpinner: Spinner by lazy {
        kmlGeometryControlsLayoutBinding.colorSpinner
    }

    private val colorTextView: TextView by lazy {
        kmlGeometryControlsLayoutBinding.colorTextView
    }

    private val kmlDocument by lazy { KmlDocument() }

    private val pointSymbolUrls by lazy {
        listOf(
            "http://static.arcgis.com/images/Symbols/Shapes/BlueCircleLargeB.png",
            "http://static.arcgis.com/images/Symbols/Shapes/BlueDiamondLargeB.png",
            "http://static.arcgis.com/images/Symbols/Shapes/BluePin1LargeB.png",
            "http://static.arcgis.com/images/Symbols/Shapes/BluePin2LargeB.png",
            "http://static.arcgis.com/images/Symbols/Shapes/BlueSquareLargeB.png",
            "http://static.arcgis.com/images/Symbols/Shapes/BlueStarLargeB.png"
        )
    }

    // set the default color to blue
    var color: Int = Color.parseColor("Blue")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityMainBinding.root)

        // authentication with an API key or named user is required to access basemaps and other
        // location services
        ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY)

        // create a map with a dark gray vector basemap and add a KML layer
        val map = ArcGISMap(BasemapStyle.ARCGIS_DARK_GRAY).apply {
            // create a KML layer from a blank KML document and add it to the map
            operationalLayers.add(KmlLayer(KmlDataset(kmlDocument)))
        }

        mapView.apply {
            // add the map to the map view
            this.map = map

            // create a sketch editor and add it to the map view
            sketchEditor = SketchEditor().apply {
                sketchStyle = SketchStyle()
            }
        }

        // once the map is done loading, create spinners
        map.addDoneLoadingListener {
            createSpinners()
        }
    }

    /**
     * Starts the sketch editor based on the selected sketch creation mode.
     */
    private fun startSketch(sketchEditor: SketchEditor, sketchCreationMode: SketchCreationMode) {
        mapView.run {
            // stop the sketch editor
            sketchEditor.stop()
            // start the sketch editor with the selected creation mode
            sketchEditor.start(sketchCreationMode)
        }
    }

    /**
     * Take the current sketch and use it to create a KML placemark. Add the KML placemark as a child
     * node to the KML document.
     */
    fun addKmlPlaceMark(view: View) {
        if (mapView.sketchEditor.isSketchValid) {
            // project the sketched geometry to WGS84 to comply with the KML standard
            val sketchGeometry = mapView.sketchEditor.geometry
            val projectedGeometry =
                GeometryEngine.project(sketchGeometry, SpatialReferences.getWgs84())

            // stop the current sketch
            mapView.sketchEditor.stop()

            // create a new KML placemark
            val currentKmlPlacemark =
                KmlPlacemark(KmlGeometry(projectedGeometry, KmlAltitudeMode.CLAMP_TO_GROUND))

            // update the style of the current KML placemark
            val kmlStyle = KmlStyle()
            when (sketchGeometry.geometryType) {
                GeometryType.POINT -> {
                    kmlStyle.iconStyle =
                        KmlIconStyle(
                            KmlIcon(pointSymbolUrls[pointSymbolSpinner.selectedItemPosition]),
                            1.0
                        )
                }
                GeometryType.POLYLINE -> {
                    kmlStyle.lineStyle = KmlLineStyle(color, 8.0)
                }
                GeometryType.POLYGON -> {
                    kmlStyle.polygonStyle = KmlPolygonStyle(color).apply {
                        isFilled = true
                        isOutlined = false
                    }
                }
                else -> {
                    Toast.makeText(
                        this,
                        "Geometry type not supported in this sample.",
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
            }
            currentKmlPlacemark.style = kmlStyle

            // add the placemark to the kml document
            kmlDocument.childNodes.add(currentKmlPlacemark)
        } else {
            Toast.makeText(this, "Sketch invalid!", Toast.LENGTH_LONG).show()
        }
        // start a new sketch
        startSketch(
            mapView.sketchEditor,
            SketchCreationMode.valueOf(sketchCreationModeSpinner.selectedItem.toString())
        )
    }


    /**
     * Create a save dialog to get a file name and save the KML Document to a KMZ file.
     */
    fun createSaveDialog(view: View) {
        // create an edit text to choose a file name to save the KML document to
        val fileNameEditText = EditText(applicationContext).apply {
            // set a default file name
            setText(getString(R.string.default_save_name))
        }
        // create an alert dialog
        AlertDialog.Builder(this).apply {
            // set the alert dialog title
            setTitle("Please define a file name:")
            // add the edit text to the view
            setView(fileNameEditText)
            // set positive button to call save async on the KML document
            setPositiveButton("Save") { _: DialogInterface, _: Int ->
                // save the KML document to the device with the file name from the edit text box
                val saveFuture =
                    kmlDocument.saveAsAsync(getExternalFilesDir(null)?.path + File.separator + fileNameEditText.text.toString())
                saveFuture.addDoneListener {
                    try {
                        // call get on the save future to check if it saved correctly
                        saveFuture.get()
                        // notify the file has been saved
                        Toast.makeText(
                            applicationContext,
                            "Your KML document was saved as: " + fileNameEditText.text,
                            Toast.LENGTH_LONG
                        ).show()
                    } catch (e: Exception) {
                        // notify the file was not saved correctly
                        Toast.makeText(
                            applicationContext,
                            "KML document was not saved: " + e.message,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
            setCancelable(true)
        }.show()
    }

    /**
     * Create the geometry, point symbol and color spinners.
     */
    private fun createSpinners() {
        // create sketch create mode type spinner
        sketchCreationModeSpinner.apply {
            adapter = ArrayAdapter(
                applicationContext,
                R.layout.spinner_row,
                listOf(
                    SketchCreationMode.POINT.toString(),
                    SketchCreationMode.POLYLINE.toString(),
                    SketchCreationMode.POLYGON.toString()
                )
            )
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    // get the sketch creation mode
                    with(SketchCreationMode.valueOf(selectedItem.toString())) {
                        startSketch(mapView.sketchEditor, this)
                        // show style controls relevant to the selected sketch creation mode
                        when (SketchCreationMode.POINT) {
                            this -> {
                                pointSymbolSpinner.visibility = View.VISIBLE
                                pointSymbolTextView.visibility = View.VISIBLE
                                colorSpinner.visibility = View.INVISIBLE
                                colorTextView.visibility = View.INVISIBLE
                            }
                            else -> {
                                colorSpinner.visibility = View.VISIBLE
                                colorTextView.visibility = View.VISIBLE
                                pointSymbolSpinner.visibility = View.INVISIBLE
                                pointSymbolTextView.visibility = View.INVISIBLE
                            }
                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }

        // create point symbol spinner
        pointSymbolSpinner.apply {
            val listOfSymbols = listOf(
                R.drawable.blue_circle,
                R.drawable.blue_diamond,
                R.drawable.blue_pin_1,
                R.drawable.blue_pin_2,
                R.drawable.blue_square,
                R.drawable.blue_star
            )
            adapter = PointSymbolAdapter(
                applicationContext,
                listOfSymbols
            )
        }

        colorSpinner.apply {
            adapter = ArrayAdapter<String>(
                applicationContext, R.layout.spinner_row, listOf(
                    "BLUE", "GREEN", "CYAN", "RED", "MAGENTA", "YELLOW"
                )
            )
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    color = Color.parseColor(selectedItem.toString())
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
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
