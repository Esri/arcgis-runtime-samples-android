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
package com.esri.arcgisruntime.sample.surfaceplacement

import android.graphics.Color
import android.os.Bundle
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.layers.ArcGISSceneLayer
import com.esri.arcgisruntime.mapping.ArcGISScene
import com.esri.arcgisruntime.mapping.ArcGISTiledElevationSource
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.view.Camera
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.mapping.view.LayerSceneProperties.SurfacePlacement
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol
import com.esri.arcgisruntime.symbology.TextSymbol
import com.esri.arcgisruntime.symbology.TextSymbol.VerticalAlignment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // create a scene
    val scene = ArcGISScene().apply {
      // add a basemap to it
      basemap = Basemap.createImagery()
      // add base surface for elevation data
      baseSurface.elevationSources.add(
        ArcGISTiledElevationSource(getString(R.string.elevation_image_service))
      )
      // create a scene layer from the Brest, France scene server
      operationalLayers.add(
        ArcGISSceneLayer(getString(R.string.scene_layer_service))
      )
    }

    // set an initial viewpoint
    val initialViewPoint = Point(-4.45968, 48.3889, 37.9922)
    val camera = Camera(initialViewPoint, 329.91, 96.6632, 0.0)

    sceneView.apply {
      this.scene = scene
      setViewpointCamera(camera)
    }

    // create point for the scene related graphic with a z value of 0
    val sceneRelatedPoint =
      Point(
        -4.4610562,
        48.3902727,
        0.0,
        camera.location.spatialReference
      )
    // create point for the surface related graphics with z value of 70
    val surfaceRelatedPoint =
      Point(
        -4.4609257,
        48.3903965,
        70.0,
        camera.location.spatialReference
      )

    // create a red triangle symbol
    val triangleSymbol =
      SimpleMarkerSymbol(SimpleMarkerSymbol.Style.TRIANGLE, Color.RED, 10f)

    // create a text symbol for elevation mode
    val drapedFlatText = TextSymbol(
      15F, "DRAPED FLAT", Color.BLUE, TextSymbol.HorizontalAlignment.LEFT,
      VerticalAlignment.TOP
    ).apply {
      offsetY = 20f
    }
    // create the draped flat overlay
    val drapedFlatOverlay = GraphicsOverlay().apply {
      sceneProperties.surfacePlacement = SurfacePlacement.DRAPED_FLAT
      graphics.addAll(
        arrayOf(
          Graphic(surfaceRelatedPoint, triangleSymbol),
          Graphic(surfaceRelatedPoint, drapedFlatText)
        )
      )
    }

    // create a text symbol for elevation mode
    val drapedBillboardedText = TextSymbol(
      15F, "DRAPED BILLBOARDED", Color.BLUE, TextSymbol.HorizontalAlignment.LEFT,
      VerticalAlignment.TOP
    ).apply {
      offsetY = 20f
    }
    // create the draped billboarded overlay
    val drapedBillboardedOverlay = GraphicsOverlay().apply {
      sceneProperties.surfacePlacement =
        SurfacePlacement.DRAPED_BILLBOARDED
      graphics.addAll(
        arrayOf(
          Graphic(surfaceRelatedPoint, triangleSymbol),
          Graphic(surfaceRelatedPoint, drapedBillboardedText)
        )
      )
      // hide the draped billboarded overlay because the toggle default option is draped flat
      isVisible = false
    }

    // create a text symbol for elevation mode
    val relativeText = TextSymbol(
      15f, "RELATIVE", Color.BLUE, TextSymbol.HorizontalAlignment.LEFT,
      VerticalAlignment.TOP
    ).apply {
      offsetY = 20f
    }
    // create the relative overlay
    val relativeOverlay = GraphicsOverlay().apply {
      sceneProperties.surfacePlacement = SurfacePlacement.RELATIVE
      graphics.addAll(
        arrayOf(
          Graphic(surfaceRelatedPoint, triangleSymbol),
          Graphic(surfaceRelatedPoint, relativeText)
        )
      )
    }

    // create a text symbol for elevation mode
    val absoluteText = TextSymbol(
      15f, "ABSOLUTE", Color.BLUE, TextSymbol.HorizontalAlignment.LEFT,
      VerticalAlignment.TOP
    ).apply {
      offsetY = 20f
    }
    // create the absolute overlay
    val absoluteOverlay = GraphicsOverlay().apply {
      sceneProperties.surfacePlacement = SurfacePlacement.ABSOLUTE
      graphics.addAll(
        arrayOf(
          Graphic(surfaceRelatedPoint, triangleSymbol),
          Graphic(surfaceRelatedPoint, absoluteText)
        )
      )
    }

    // create a text symbol for elevation mode
    val relativeToSceneText = TextSymbol(
      15f,
      "RELATIVE TO SCENE",
      Color.BLUE,
      TextSymbol.HorizontalAlignment.RIGHT,
      VerticalAlignment.TOP
    ).apply {
      offsetY = 20f
    }
    // create the relative to scene overlay
    val relativeToSceneOverlay = GraphicsOverlay().apply {
      sceneProperties.surfacePlacement = SurfacePlacement.RELATIVE_TO_SCENE
      graphics.addAll(
        arrayOf(
          Graphic(sceneRelatedPoint, triangleSymbol),
          Graphic(sceneRelatedPoint, relativeToSceneText)
        )
      )
    }

    // add the graphics overlays to the scene view
    sceneView.graphicsOverlays.addAll(
      arrayOf(
        drapedFlatOverlay,
        drapedBillboardedOverlay,
        relativeOverlay,
        absoluteOverlay,
        relativeToSceneOverlay
      )
    )

    // toggle visibility of the draped and billboarded graphics overlays
    drapedToggle.setOnClickListener {
      drapedBillboardedOverlay.isVisible = drapedToggle.isChecked
      drapedFlatOverlay.isVisible = !drapedToggle.isChecked
    }

    // change the z-positions of the graphics when the seek bar changes
    seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
      override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        sceneView.graphicsOverlays.forEach { graphicsOverlay ->
          graphicsOverlay.graphics.forEach { graphic ->
            // get the current point and change only its z position
            val oldPoint = graphic.geometry as Point
            graphic.geometry = Point(oldPoint.x, oldPoint.y, seekBar.progress.toDouble())
          }
        }
      }

      override fun onStartTrackingTouch(seekBar: SeekBar) {}
      override fun onStopTrackingTouch(seekBar: SeekBar?) {}
    })
  }

  override fun onPause() {
    sceneView.pause()
    super.onPause()
  }

  override fun onResume() {
    super.onResume()
    sceneView.resume()
  }

  override fun onDestroy() {
    sceneView.dispose()
    super.onDestroy()
  }
}
