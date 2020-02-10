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

package com.esri.arcgisruntime.sample.viewshedlocation

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.geoanalysis.LocationViewshed
import com.esri.arcgisruntime.geoanalysis.Viewshed
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.layers.ArcGISSceneLayer
import com.esri.arcgisruntime.mapping.ArcGISScene
import com.esri.arcgisruntime.mapping.ArcGISTiledElevationSource
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.Surface
import com.esri.arcgisruntime.mapping.view.AnalysisOverlay
import com.esri.arcgisruntime.mapping.view.Camera
import com.esri.arcgisruntime.mapping.view.DefaultSceneViewOnTouchListener
import com.esri.arcgisruntime.mapping.view.OrbitLocationCameraController
import java.util.concurrent.ExecutionException
import kotlin.math.roundToInt
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.viewshed_seek_bars.*


class MainActivity : AppCompatActivity() {

  companion object {
    private val TAG: String = MainActivity::class.java.simpleName
  }

  private val initHeading = 0
  private val initPitch = 60
  private val initHorizontalAngle = 75
  private val initVerticalAngle = 90
  private val initMinDistance = 0
  private val initMaxDistance = 1500

  private var minDistance: Int = 0
  private var maxDistance: Int = 0

  private lateinit var viewShed: LocationViewshed

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // create a surface for elevation data
    val localElevationImageService = getString(R.string.elevation_service)
    val surface = Surface().apply {
      elevationSources.add(ArcGISTiledElevationSource(localElevationImageService))
    }

    // create a layer of buildings
    val buildingsLayer = ArcGISSceneLayer(getString(R.string.buildings_layer))

    // create a scene and add imagery basemap, elevation surface, and buildings layer to it
    val scene = ArcGISScene().apply {
      basemap = Basemap.createImagery()
      baseSurface = surface
      operationalLayers.add(buildingsLayer)
    }

    val initLocation = Point(-4.50, 48.4, 1000.0)
    Viewshed.setFrustumOutlineColor(Color.BLUE)

    // create viewshed from the initial location
    viewShed = LocationViewshed(
      initLocation,
      initHeading.toDouble(),
      initPitch.toDouble(),
      initHorizontalAngle.toDouble(),
      initVerticalAngle.toDouble(),
      initMinDistance.toDouble(),
      initMaxDistance.toDouble()
    ).apply {
      setFrustumOutlineVisible(true)
    }

    // add a camera and set it to orbit the location point of the frustum
    val camera = Camera(initLocation, 20000000.0, 0.0, 55.0, 0.0)
    val orbitCamera = OrbitLocationCameraController(initLocation, 5000.0)
    sceneView.apply {
      this.scene = scene
      this.cameraController = orbitCamera
      this.setViewpointCamera(camera)
    }

    // create an analysis overlay to add the viewshed to the scene view
    val analysisOverlay = AnalysisOverlay()
    analysisOverlay.analyses.add(viewShed)
    sceneView.analysisOverlays.add(analysisOverlay)

    // initialize the UI controls
    handleUiElements()
  }

  /**
   * Handles double touch drag for movement of viewshed location point and listeners for
   * changes in seek bar progress.
   */
  private fun handleUiElements() {

    sceneView.setOnTouchListener(object : DefaultSceneViewOnTouchListener(sceneView) {

      // double tap and hold second tap to drag viewshed to a new location
      override fun onDoubleTouchDrag(motionEvent: MotionEvent): Boolean {
        // convert from screen point to location point
        val screenPoint = android.graphics.Point(
          motionEvent.x.roundToInt(),
          motionEvent.y.roundToInt()
        )
        val locationPointFuture = sceneView.screenToLocationAsync(screenPoint)
        locationPointFuture.addDoneListener {
          try {
            val locationPoint = locationPointFuture.get()

            // add 50 meters to location point and set to viewshed
            viewShed.location = Point(locationPoint.x, locationPoint.y, locationPoint.z + 50)
          } catch (e: InterruptedException) {
            val error = "Error converting screen point to location point: " + e.message
            Log.e(TAG, error)
            Toast.makeText(this@MainActivity, error, Toast.LENGTH_LONG).show()
          } catch (e: ExecutionException) {
            val error = "Error converting screen point to location point: " + e.message
            Log.e(TAG, error)
            Toast.makeText(this@MainActivity, error, Toast.LENGTH_LONG).show()
          }
        }

        // ignore default double touch drag gesture
        return true
      }
    })

    // heading range 0 - 360
    headingSeekBar.max = 360
    setHeading(initHeading)
    headingSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
      override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
        setHeading(seekBar.progress)
      }

      override fun onStartTrackingTouch(seekBar: SeekBar) {}

      override fun onStopTrackingTouch(seekBar: SeekBar) {}
    })

    // set arbitrary max to 180 to avoid nonsensical pitch values
    pitchSeekBar.max = 180
    setPitch(initPitch)
    pitchSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
      override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
        setPitch(seekBar.progress)
      }

      override fun onStartTrackingTouch(seekBar: SeekBar) {}

      override fun onStopTrackingTouch(seekBar: SeekBar) {}
    })

    // horizontal angle range 1 - 120
    horizontalAngleSeekBar.max = 120
    setHorizontalAngle(initHorizontalAngle)
    horizontalAngleSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
      override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
        val horizontalAngle = horizontalAngleSeekBar.progress
        if (horizontalAngle > 0) { // horizontal angle must be > 0
          setHorizontalAngle(horizontalAngle)
        }
      }

      override fun onStartTrackingTouch(seekBar: SeekBar) {}

      override fun onStopTrackingTouch(seekBar: SeekBar) {}
    })

    // vertical angle range 1 - 120
    verticalAngleSeekBar.max = 120
    setVerticalAngle(initVerticalAngle)
    verticalAngleSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
      override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
        val verticalAngle = verticalAngleSeekBar.progress
        if (verticalAngle > 0) { // vertical angle must be > 0
          setVerticalAngle(verticalAngle)
        }
      }

      override fun onStartTrackingTouch(seekBar: SeekBar) {}

      override fun onStopTrackingTouch(seekBar: SeekBar) {}
    })

    // initialize the minimum distance
    minDistance = initMinDistance

    // set to 1000 below the arbitrary max
    minDistanceSeekBar.max = 8999
    setMinDistance(initMinDistance)
    minDistanceSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
      override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
        minDistance = seekBar.progress
        if (maxDistance - minDistance < 1000) {
          maxDistance = minDistance + 1000
          setMaxDistance(maxDistance)
        }
        setMinDistance(minDistance)
      }

      override fun onStartTrackingTouch(seekBar: SeekBar) {}

      override fun onStopTrackingTouch(seekBar: SeekBar) {}
    })

    // initialize the maximum distance
    maxDistance = initMaxDistance

    // set arbitrary max to 9999 to allow a maximum of 4 digits
    maxDistanceSeekBar.max = 9999
    setMaxDistance(initMaxDistance)
    maxDistanceSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
      override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
        maxDistance = seekBar.progress
        if (maxDistance - minDistance < 1000) {
          minDistance = if (maxDistance > 1000) {
            maxDistance - 1000
          } else {
            0
          }
          setMinDistance(minDistance)
        }
        setMaxDistance(maxDistance)
      }

      override fun onStartTrackingTouch(seekBar: SeekBar) {}

      override fun onStopTrackingTouch(seekBar: SeekBar) {}
    })
  }

  /**
   * Set viewshed heading, seek bar progress, and current heading text view.
   *
   * @param heading in degrees
   */
  private fun setHeading(heading: Int) {
    headingSeekBar.progress = heading
    currHeading.text = heading.toString()
    viewShed.heading = heading.toDouble()
  }

  /**
   * Set viewshed pitch, seek bar progress, and current pitch text view.
   *
   * @param pitch in degrees
   */
  private fun setPitch(pitch: Int) {
    pitchSeekBar.progress = pitch
    currPitch.text = pitch.toString()
    viewShed.pitch = pitch.toDouble()
  }

  /**
   * Set viewshed horizontal angle, seek bar progress, and current horizontal angle text view.
   *
   * @param horizontalAngle in degrees, > 0 and <= 120
   */
  private fun setHorizontalAngle(horizontalAngle: Int) {
    if (horizontalAngle in 1..120) {
      horizontalAngleSeekBar.progress = horizontalAngle
      currHorizontalAngle.text = horizontalAngle.toString()
      viewShed.horizontalAngle = horizontalAngle.toDouble()
    } else {
      Log.e(
        TAG,
        "Horizontal angle must be greater than 0 and less than or equal to 120."
      )
    }
  }

  /**
   * Set viewshed vertical angle, seek bar progress, and current vertical angle text view.
   *
   * @param verticalAngle in degrees, > 0 and <= 120
   */
  private fun setVerticalAngle(verticalAngle: Int) {
    if (verticalAngle in 1..120) {
      verticalAngleSeekBar.progress = verticalAngle
      currVerticalAngle.text = verticalAngle.toString()
      viewShed.verticalAngle = verticalAngle.toDouble()
    } else {
      Log.e(
        TAG,
        "Vertical angle must be greater than 0 and less than or equal to 120."
      )
    }
  }

  /**
   * Set viewshed minimum distance, seek bar progress, and current minimum distance text view.
   *
   * @param minDistance in meters
   */
  private fun setMinDistance(minDistance: Int) {
    minDistanceSeekBar.progress = minDistance
    currMinimumDistance.text = minDistance.toString()
    viewShed.minDistance = minDistance.toDouble()
  }

  /**
   * Set viewshed maximum distance, seek bar progress, and current maximum distance text view.
   *
   * @param maxDistance in meters
   */
  private fun setMaxDistance(maxDistance: Int) {
    maxDistanceSeekBar.progress = maxDistance
    currMaximumDistance.text = maxDistance.toString()
    viewShed.maxDistance = maxDistance.toDouble()
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
