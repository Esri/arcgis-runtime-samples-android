/* Copyright 2017 Esri
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

import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.geometry.GeometryEngine
import com.esri.arcgisruntime.geometry.GeometryType
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.layers.KmlLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.view.SketchCreationMode
import com.esri.arcgisruntime.mapping.view.SketchEditor
import com.esri.arcgisruntime.mapping.view.SketchStyle
import com.esri.arcgisruntime.ogc.kml.KmlAltitudeMode
import com.esri.arcgisruntime.ogc.kml.KmlDataset
import com.esri.arcgisruntime.ogc.kml.KmlDocument
import com.esri.arcgisruntime.ogc.kml.KmlGeometry
import com.esri.arcgisruntime.ogc.kml.KmlIcon
import com.esri.arcgisruntime.ogc.kml.KmlIconStyle
import com.esri.arcgisruntime.ogc.kml.KmlLineStyle
import com.esri.arcgisruntime.ogc.kml.KmlPlacemark
import com.esri.arcgisruntime.ogc.kml.KmlPolygonStyle
import com.esri.arcgisruntime.ogc.kml.KmlStyle
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.kml_geometry_controls_layout.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.customView
import org.jetbrains.anko.editText
import org.jetbrains.anko.toast
import org.jetbrains.anko.verticalLayout

val kmlDocument by lazy { KmlDocument() }

val pointSymbolUrls by lazy {
  listOf(
    "http://static.arcgis.com/images/Symbols/Shapes/BlueCircleLargeB.png",
    "http://static.arcgis.com/images/Symbols/Shapes/BlueDiamondLargeB.png",
    "http://static.arcgis.com/images/Symbols/Shapes/BluePin1LargeB.png",
    "http://static.arcgis.com/images/Symbols/Shapes/BluePin2LargeB.png",
    "http://static.arcgis.com/images/Symbols/Shapes/BlueSquareLargeB.png",
    "http://static.arcgis.com/images/Symbols/Shapes/BlueStarLargeB.png"
  )
}

var color: Int = 0

class MainActivity : AppCompatActivity() {


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    mapView.apply {
      // create a map and add it to the map view
      map = ArcGISMap(Basemap.createDarkGrayCanvasVector()).apply {
        // create a KML layer from a blank KML document and add it to the map
        operationalLayers.add(KmlLayer(KmlDataset(kmlDocument)))
        addDoneLoadingListener {
          createSpinners()
        }
      }

      // create a sketch editor and add it to the map view
      sketchEditor = SketchEditor().apply {
        sketchStyle = SketchStyle()
      }
    }
  }

  private fun createSpinners() {
    // create sketch create mode type spinner
    sketchCreationModeSpinner.apply {
      adapter = ArrayAdapter<String>(
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
            when {
              this == SketchCreationMode.POINT -> {
                pointSymbolSpinner.visibility = View.VISIBLE
                pointSymbolTextView.visibility = View.VISIBLE
                colorSymbolSeekbar.visibility = View.INVISIBLE
                colorTextView.visibility = View.INVISIBLE
              }
              else -> {
                colorSymbolSeekbar.visibility = View.VISIBLE
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
      onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(
          parent: AdapterView<*>?,
          view: View?,
          position: Int,
          id: Long
        ) {
        }
          override fun onNothingSelected(parent: AdapterView<*>?) {}

      }
    }



      colorSymbolSeekbar.apply {
        // create the bar as a rectangle with a linear gradient
        progressDrawable = ShapeDrawable(RectShape()).apply {
          paint.shader = LinearGradient(
            0f, 0f, 600f, 0f,
            intArrayOf(
              Color.BLACK,
              Color.BLUE,
              Color.GREEN,
              Color.CYAN,
              Color.RED,
              Color.MAGENTA,
              Color.YELLOW,
              Color.WHITE
            ),
            null, Shader.TileMode.CLAMP
          )
        }

        setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
          override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            if (fromUser) {
              var r = 0
              var g = 0
              var b = 0

              if (progress < 256) {
                b = progress
              } else if (progress < 256 * 2) {
                g = progress % 256
                b = 256 - progress % 256
              } else if (progress < 256 * 3) {
                g = 255
                b = progress % 256
              } else if (progress < 256 * 4) {
                r = progress % 256
                g = 256 - progress % 256;
                b = 256 - progress % 256;
              } else if (progress < 256 * 5) {
                r = 255
                g = 0
                b = progress % 256
              } else if (progress < 256 * 6) {
                r = 255
                g = progress % 256
                b = 256 - progress % 256
              } else if (progress < 256 * 7) {
                r = 255
                g = 255
                b = progress % 256
              }
              color = Color.rgb(r, g, b)
            }
          }

          override fun onStartTrackingTouch(seekBar: SeekBar?) {

          }

          override fun onStopTrackingTouch(seekBar: SeekBar?) {
            Log.d("seekbar", seekBar?.progress.toString())
          }
        })
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
     * Discard or commit the current sketch to a KML placemark if ESCAPE or ENTER are pressed while
     * sketching.
     */
    public fun addKmlPlaceMark(view: View) {
      if (mapView.sketchEditor.isSketchValid) {

        // project the sketched geometry to WGS84 to comply with the KML standard
        val sketchGeometry = mapView.sketchEditor.geometry
        val projectedGeometry = GeometryEngine.project(sketchGeometry, SpatialReferences.getWgs84())

        mapView.sketchEditor.stop()

        // create a new KML placemark
        val kmlGeometry = KmlGeometry(projectedGeometry, KmlAltitudeMode.CLAMP_TO_GROUND)
        val currentKmlPlacemark = KmlPlacemark(kmlGeometry)

        // update the style of the current KML placemark
        val kmlStyle = KmlStyle()
        currentKmlPlacemark.style = kmlStyle

        // set the selected style for the placemark
        when (sketchGeometry.geometryType) {
          GeometryType.POINT -> {
            val kmlIcon = KmlIcon(pointSymbolUrls[pointSymbolSpinner.selectedItemPosition])
            val kmlIconStyle = KmlIconStyle(kmlIcon, 1.0)
            kmlStyle.iconStyle = kmlIconStyle
          }
          GeometryType.POLYLINE -> {
            val kmlLineStyle = KmlLineStyle(color, 8.0)
            kmlStyle.lineStyle = kmlLineStyle
          }
          GeometryType.POLYGON -> {
            val kmlPolygonStyle = KmlPolygonStyle(color)
            kmlPolygonStyle.isFilled = true
            kmlPolygonStyle.isOutlined = false
            kmlStyle.polygonStyle = kmlPolygonStyle
          }
          else -> {
            toast("Geometry type not supported in this sample.")
          }
        }

        // add the placemark to the kml document
        kmlDocument.childNodes.add(currentKmlPlacemark)
      } else {
        toast("Sketch invalid!")
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
    public fun createSaveDialog(view: View) {

      alert("Please define a file name:") {
        customView {
          verticalLayout {
            // get the file name from the edit text box
            val fileName = editText {
              // set a default file name
              setText("MyKMLDocument.kmz")
            }.text
            // on save button
            positiveButton("Save") {
              // save the KML document to the device with the file name from the edit text box
              kmlDocument.saveAsAsync(getExternalFilesDir(fileName.toString())?.path)
                .addDoneListener {
                  // notify the file has been saved
                  toast("Your KML document was saved as $fileName saved.")
                }
            }
            negativeButton("Cancel") {}
          }
        }
      }.show()
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
