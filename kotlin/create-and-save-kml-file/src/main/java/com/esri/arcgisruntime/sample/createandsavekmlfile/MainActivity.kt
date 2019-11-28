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

import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.geometry.GeometryEngine
import com.esri.arcgisruntime.geometry.GeometryType
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.layers.KmlLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.view.SketchCreationMode
import com.esri.arcgisruntime.mapping.view.SketchEditor
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
import sun.jvm.hotspot.utilities.IntArray
import java.awt.Event.ENTER
import java.io.File

val kmlDocument by lazy { KmlDocument() }


class MainActivity : AppCompatActivity() {

  private lateinit var iconResources: List<Int>

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    mapView.apply {
      // create a map and add it to the map view
      ArcGISMap(Basemap.createDarkGrayCanvasVector()).apply {
        // create a KML layer from a blank KML document and add it to the map
        operationalLayers.add(KmlLayer(KmlDataset(kmlDocument)))
        // start with POINT selected
        addDoneLoadingListener { sketchCreationModeSpinner.setSelection(0) }
      }
      // create a sketch editor and add it to the map view
      sketchEditor = SketchEditor()

    }

    // add geometry options for KML placemarks
    sketchCreationModeSpinner.apply {
      adapter = ArrayAdapter<String>(
        applicationContext,
        android.R.layout.simple_spinner_item,
        SketchCreationMode.values().map { it.toString() })
      onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
          // get the sketch creation mode
          with(SketchCreationMode.valueOf(selectedItem.toString())) {
            startSketch(mapView.sketchEditor, this)
            // show style controls relevant to the selected sketch creation mode
            when {
              this == SketchCreationMode.POINT -> {
                pointSymbolSpinner.visibility = View.VISIBLE
                colorSymbolSpinner.visibility = View.GONE
              }
              else -> {
                colorSymbolSpinner.visibility = View.VISIBLE
                pointSymbolSpinner.visibility = View.GONE
              }
            }
          }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {}
      }
    }

    // set the images for the icon selection combo box
    iconResources = listOf(
      R.drawable.blue_circle,
      R.drawable.blue_diamond,
      R.drawable.blue_pin_1,
      R.drawable.blue_pin_2,
      R.drawable.blue_square,
      R.drawable.blue_star
    )

    pointSymbolSpinner.adapter = PointSymbolAdapter(applicationContext, iconResources)

    getExternalFilesDir("myKmz.kmz")
  }

  /**
   * Starts the sketch editor based on the selected sketch creation mode.
   */
  private fun startSketch(sketchEditor: SketchEditor, sketchCreationMode: SketchCreationMode) {

  }

  /**
   * Discard or commit the current sketch to a KML placemark if ESCAPE or ENTER are pressed while sketching.
   *
   * @param keyEvent the key event
   */
  @FXML
  private fun handleKeyReleased(keyEvent: KeyEvent) {
    if (keyEvent.getCode() === KeyCode.ESCAPE) {
      
      mapView.run {
        // stop the sketch editor
        sketchEditor.stop()
        // start the sketch editor with the selected creation mode
        sketchEditor.start(SketchCreationMode.valueOf(sketchCreationModeSpinner.selectedItem.toString())
      }

    } else if (keyEvent.getCode() === KeyCode.ENTER && sketchEditor.isSketchValid) {
      // project the sketched geometry to WGS84 to comply with the KML standard
      val sketchGeometry = sketchEditor.geometry
      val projectedGeometry = GeometryEngine.project(sketchGeometry, SpatialReferences.getWgs84())

      // create a new KML placemark
      val kmlGeometry = KmlGeometry(projectedGeometry, KmlAltitudeMode.CLAMP_TO_GROUND)
      val currentKmlPlacemark = KmlPlacemark(kmlGeometry)

      // update the style of the current KML placemark
      val kmlStyle = KmlStyle()
      currentKmlPlacemark.style = kmlStyle

      // set the selected style for the placemark
      when (sketchGeometry.geometryType) {
        GeometryType.POINT -> {
          val iconPath =
            Uri.parse("android.resource://com.esri.arcgisruntime.sample/" + pointSymbolSpinner.selectedItem)
              .toString()
          val kmlIcon = KmlIcon(iconPath)
          val kmlIconStyle = KmlIconStyle(kmlIcon, 1.0)
          kmlStyle.iconStyle = kmlIconStyle
        }
        GeometryType.POLYLINE -> {
          val polylineColor = colorPicker.getValue()
          if (polylineColor != null) {
            val kmlLineStyle = KmlLineStyle(ColorUtil.colorToArgb(polylineColor), 8.0)
            kmlStyle.lineStyle = kmlLineStyle
          }
        }
        GeometryType.POLYGON -> {
          val polygonColor = colorPicker.getValue()
          if (polygonColor != null) {
            val kmlPolygonStyle = KmlPolygonStyle(ColorUtil.colorToArgb(polygonColor))
            kmlPolygonStyle.isFilled = true
            kmlPolygonStyle.isOutlined = false
            kmlStyle.polygonStyle = kmlPolygonStyle
          }
        }
      }

      // add the placemark to the kml document
      kmlDocument.childNodes.add(currentKmlPlacemark)

      // start a new sketch
      startSketch(
        mapView.sketchEditor,
        SketchCreationMode.valueOf(sketchCreationModeSpinner.selectedItem.toString())
      )
    }
  }

  /**
   * Open the file chooser to save the KML Document to a KMZ file.
   */
  @FXML
  private void handleSaveAction()
  {

    // get a path from the file chooser
    File kmzFile = fileChooser . showSaveDialog (mapView.getScene().getWindow());
    if (kmzFile != null) {
      // save the KML document to the file
      kmlDocument.saveAsAsync(kmzFile.getPath()).addDoneListener(() ->
      new Alert (Alert.AlertType.INFORMATION, "KMZ file saved.").show()
      )
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
