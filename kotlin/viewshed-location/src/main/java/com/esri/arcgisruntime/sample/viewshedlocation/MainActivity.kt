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
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
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
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() {

  companion object {
    private val TAG: String = MainActivity::class.java.simpleName
  }
  private val initLocation = Point(-4.50, 48.4, 1000.0)

  private val initHeading = 0
  private val initPitch = 60
  private val initHorizontalAngle = 75
  private val initVerticalAngle = 90
  private val initMinDistance = 0
  private val initMaxDistance = 1500

  private lateinit var currHeading: TextView
  private lateinit var currPitch: TextView
  private lateinit var currHorizontalAngle: TextView
  private lateinit var currVerticalAngle: TextView
  private lateinit var currMinDistance: TextView
  private lateinit var currMaxDistance: TextView

  private lateinit var headingSeekBar: SeekBar
  private lateinit var pitchSeekBar: SeekBar
  private lateinit var horizontalAngleSeekBar: SeekBar
  private lateinit var verticalAngleSeekBar: SeekBar
  private lateinit var minDistanceSeekBar: SeekBar
  private lateinit var maxDistanceSeekBar: SeekBar

  private var minDistance: Int = initMinDistance
  private var maxDistance: Int = initMaxDistance

  private val viewShed: LocationViewshed by lazy {
    LocationViewshed(
      initLocation,
      initHeading.toDouble(),
      initPitch.toDouble(),
      initHorizontalAngle.toDouble(),
      initVerticalAngle.toDouble(),
      initMinDistance.toDouble(),
      initMaxDistance.toDouble()
    )
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val localElevationImageService = getString(R.string.elevation_service)
    val surface = Surface().apply {
      elevationSources.add(ArcGISTiledElevationSource(localElevationImageService))
    }

    val sceneLayer = ArcGISSceneLayer(getString(R.string.buildings_layer))
    val scene = ArcGISScene().apply {
      basemap = Basemap.createImagery()
      baseSurface = surface
      operationalLayers.add(sceneLayer)
    }

    Viewshed.setFrustumOutlineColor(Color.BLUE)

    viewShed.apply {
      setFrustumOutlineVisible(true)
    }

    val camera = Camera(initLocation, 20000000.0, 0.0, 55.0, 0.0)
    val orbitCamera = OrbitLocationCameraController(initLocation, 5000.0)
    sceneView.apply {
      this.scene = scene
      this.cameraController = orbitCamera
      this.setViewpointCamera(camera)
    }

    val analysisOverlay = AnalysisOverlay()
    analysisOverlay.analyses.add(viewShed)
    sceneView.analysisOverlays.add(analysisOverlay)

    handleUiElements()

  }


  /**
   * Handles double touch drag for movement of viewshed location point, inflation of UI elements, and listeners for
   * changes in seek bar progress.
   */
  private fun handleUiElements() {
    sceneView.setOnTouchListener(object : DefaultSceneViewOnTouchListener(sceneView) {
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

    // get views from layout
    currHeading = findViewById<View>(R.id.curr_heading) as TextView
    currPitch = findViewById<View>(R.id.curr_pitch) as TextView
    currHorizontalAngle = findViewById<View>(R.id.curr_horizontal_angle) as TextView
    currVerticalAngle = findViewById<View>(R.id.curr_vertical_angle) as TextView
    currMinDistance = findViewById<View>(R.id.curr_minimum_distance) as TextView
    currMaxDistance = findViewById<View>(R.id.curr_maximum_distance) as TextView

    // heading range 0 - 360
    headingSeekBar = findViewById<View>(R.id.heading_seek_bar) as SeekBar
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
    pitchSeekBar = findViewById<View>(R.id.pitch_seek_bar) as SeekBar
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
    horizontalAngleSeekBar = findViewById<View>(R.id.horizontal_angle_seekbar) as SeekBar
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
    verticalAngleSeekBar = findViewById<View>(R.id.vertical_angle_seekbar) as SeekBar
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

    // set to 1000 below the arbitrary max
    minDistanceSeekBar = findViewById<View>(R.id.min_distance_seekbar) as SeekBar
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

    // set arbitrary max to 9999 to allow a maximum of 4 digits
    maxDistanceSeekBar = findViewById<View>(R.id.max_distance_seekbar) as SeekBar
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
    currMinDistance.text = minDistance.toString()
    viewShed.minDistance = minDistance.toDouble()
  }

  /**
   * Set viewshed maximum distance, seek bar progress, and current maximum distance text view.
   *
   * @param maxDistance in meters
   */
  private fun setMaxDistance(maxDistance: Int) {
    maxDistanceSeekBar.progress = maxDistance
    currMaxDistance.text = maxDistance.toString()
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
