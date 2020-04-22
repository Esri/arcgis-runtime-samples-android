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

package com.esri.arcgisruntime.sample.animateimageswithimageoverlay

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.esri.arcgisruntime.geometry.Envelope
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.layers.ArcGISTiledLayer
import com.esri.arcgisruntime.mapping.ArcGISScene
import com.esri.arcgisruntime.mapping.ArcGISTiledElevationSource
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.Surface
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.Camera
import com.esri.arcgisruntime.mapping.view.DefaultSceneViewOnTouchListener
import com.esri.arcgisruntime.mapping.view.ImageFrame
import com.esri.arcgisruntime.mapping.view.ImageOverlay
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.util.Arrays
import java.util.Timer
import kotlin.concurrent.fixedRateTimer

class MainActivity : AppCompatActivity() {

  private var images: Array<File>? = null
  private var imageIndex: Int = 0

  private var timer: Timer? = null
  private var isTimerRunning = true
  private var period: Long = 67

  private lateinit var pacificSouthwestEnvelope: Envelope

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // create a new tiled layer from World Dark Gray Base REST service
    val worldDarkGrayBasemap =
      ArcGISTiledLayer("https://services.arcgisonline.com/arcgis/rest/services/Canvas/World_Dark_Gray_Base/MapServer")

    // create a new elevation source from Terrain3D REST service
    val elevationSource =
      ArcGISTiledElevationSource("https://elevation3d.arcgis.com/arcgis/rest/services/WorldElevation3D/Terrain3D/ImageServer")

    // create an envelope of the pacific southwest sector for displaying the image frame
    val pointForImageFrame =
      Point(-120.0724273439448, 35.131016955536694, SpatialReferences.getWgs84())
    pacificSouthwestEnvelope = Envelope(pointForImageFrame, 15.09589635986124, -14.3770441522488)

    // create a camera, looking at the pacific southwest sector
    val observationPoint = Point(-116.621, 24.7773, 856977.0)
    val camera = Camera(observationPoint, 353.994, 48.5495, 0.0)
    val pacificSouthwestViewpoint = Viewpoint(pacificSouthwestEnvelope, camera)

    // create a scene with the dark gray basemap and elevation source
    val darkGrayScene =
      ArcGISScene(Basemap(worldDarkGrayBasemap), Surface(listOf(elevationSource))).apply {
        // set the pacific southwest as the initial viewpoint
        initialViewpoint = pacificSouthwestViewpoint
      }

    // create the scene view
    sceneView.apply {
      // add the scene to the scene view
      scene = darkGrayScene
      // create and append an image overlay to the scene view
      sceneView.imageOverlays.add(ImageOverlay())
      // create a touch listener
      setOnTouchListener(object : DefaultSceneViewOnTouchListener(sceneView) {
        // close the options sheet when the map is tapped
        override fun onTouch(view: View?, motionEvent: MotionEvent?): Boolean {
          if (fab.isExpanded) {
            fab.isExpanded = false
          }
          return super.onTouch(view, motionEvent)
        }
      })
      // ensure the floating action button moves to be above the attribution view
      addAttributionViewLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
        val heightDelta = bottom - oldBottom
        (fab.layoutParams as CoordinatorLayout.LayoutParams).bottomMargin += heightDelta
      }
    }

    // get the image files and store the names
    File(getExternalFilesDir(null).toString() + "/ImageFrames/PacificSouthWest").let {imageOverlayDirectory ->
      images = imageOverlayDirectory.listFiles()
      // sort the files
      Arrays.sort(images)
    }

    // show the options sheet when the floating action button is clicked
    fab.setOnClickListener {
      fab.isExpanded = !fab.isExpanded
    }

    // seek bar controls opacity
    opacitySeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
      override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        // convert the seekbar progress (0 - 100) to a float 0.0 - 1.0
        val opacity: Float = progress.toFloat() / 100
        sceneView.imageOverlays[0].opacity = opacity
        currOpacityTextView.text = opacity.toString()
      }

      override fun onStartTrackingTouch(seekBar: SeekBar?) {}
      override fun onStopTrackingTouch(seekBar: SeekBar?) {}
    })

    // spinner controls how many image overlays to display per second
    fpsSpinner.apply {
      // create an adapter with fps options
      adapter = ArrayAdapter(
        this@MainActivity,
        android.R.layout.simple_spinner_dropdown_item,
        arrayOf("60 fps", "30 fps", "15 fps")
      )
      // set period based on the fps option selected
      onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
          // get the period for the chosen fps
          period = when (position) {
            0 -> 17 // 1000ms/17 = 60 fps
            1 -> 33 // 1000ms/33 = 30 fps
            2 -> 67 // 1000ms/67 = 15 fps
            else -> 0
          }
          // create a new timer for this period
          if (isTimerRunning) {
            timer?.cancel()
            createNewTimer()
          }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {}
      }
      // start at 15 fps
      setSelection(2)
    }
  }

  /**
   * Create a new image frame from the image at the current index and add it to the image overlay.
   */
  private fun animateImagesWithImageOverlay() {
    // create an image with the given path and use it to create an image frame
    ImageFrame(images?.get(imageIndex)?.path, pacificSouthwestEnvelope).let { imageFrame ->
      // set image frame to image overlay
      sceneView.imageOverlays[0].imageFrame = imageFrame
      // increment the index to keep track of which image to load next
      imageIndex++
      // reset index once all files have been loaded
      if (imageIndex == images?.size)
        imageIndex = 0
    }
  }

  /**
   * Toggle's starting and stopping the timer on button tap.
   */
  fun toggleStartStopButton(view: View) {
    isTimerRunning = when {
      isTimerRunning -> {
        timer?.cancel()
        startStopButton.text = getString(R.string.start)
        !isTimerRunning
      }
      else -> {
        createNewTimer()
        startStopButton.text = getString(R.string.stop)
        !isTimerRunning
      }
    }
  }

  /**
   * Create a new timer for the given period which repeatedly calls animateImagesWithImageOverlay.
   */
  private fun createNewTimer() {
    timer = fixedRateTimer("Image overlay timer", period = period) {
      animateImagesWithImageOverlay()
    }
  }

  override fun onPause() {
    // cancel timer if it's running
    if (isTimerRunning) {
      toggleStartStopButton(startStopButton)
    }
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
