/*
 * Copyright 2017 Esri
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

package com.esri.arcgisruntime.sample.analyzehotspots

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.concurrent.Job
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReference
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.tasks.geoprocessing.GeoprocessingString
import com.esri.arcgisruntime.tasks.geoprocessing.GeoprocessingTask
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.date_range_dialog.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// enum to flag whether the date picker calendar shown should be for the 'from' or 'to' date
enum class InputCalendar {
  From, To
}

class MainActivity : AppCompatActivity() {

  private val TAG = this::class.java.simpleName

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY)

    // create a map with the BasemapType topographic
    val map = ArcGISMap(BasemapStyle.ARCGIS_TOPOGRAPHIC).apply {
      //set initial viewpoint
      initialViewpoint =
        Viewpoint(Point(-13671170.0, 5693633.0, SpatialReference.create(3857)), 57779.0)
    }

    // set the map to the map view
    mapView.map = map

    calendarButton.setOnClickListener {
      showDateRangeDialog()
    }

    showDateRangeDialog()
  }

  /**
   * Creates the date range dialog. Includes listeners to handle click events,
   * which call showCalendar(...) or analyzeHotspots(...).
   */
  private fun showDateRangeDialog() {
    val geoprocessingTask = GeoprocessingTask(getString(R.string.hotspot_911_calls))

    // create custom dialog
    Dialog(this).apply {
      setContentView(R.layout.date_range_dialog)
      setCancelable(true)

      fromDateText.setOnClickListener {
        showCalendar(this, InputCalendar.From)
      }

      toDateText.setOnClickListener {
        showCalendar(this, InputCalendar.To)
      }

      analyzeButton.setOnClickListener {
        analyzeHotspots(geoprocessingTask, fromDateText.text.toString(), toDateText.text.toString())
        dismiss()
      }
    }.show()
  }

  /**
   * Shows a date picker dialog and writes the date chosen to the correct editable text.
   *
   * @param inputCalendar enum which specifies which editable text the chosen date should be written to
   */
  private fun showCalendar(dialog: Dialog, inputCalendar: InputCalendar) {

    // define the date picker dialog
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    DatePickerDialog(this, DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->

      val dateString = StringBuilder()
        .append(year)
        .append("-")
        .append(monthOfYear + 1)
        .append("-")
        .append(dayOfMonth)

      when (inputCalendar) {
        InputCalendar.From -> {
          dialog.fromDateText.setText(dateString)
        }
        InputCalendar.To -> {
          dialog.toDateText.setText(dateString)
        }
      }
    }, year, month, day).apply {
      datePicker.minDate = parseDate(getString(R.string.min_date))!!.time
      datePicker.maxDate = parseDate(getString(R.string.max_date))!!.time
      if (inputCalendar == InputCalendar.From) {
        updateDate(1998, 0, 1)
      }
    }.show()
  }

  /**
   * Runs the geoprocessing job, updating progress while loading. On job done, loads the resulting
   * ArcGISMapImageLayer to the map and resets the Viewpoint of the MapView.
   *
   * @param geoprocessingTask Geoprocessing task to generate hotspots
   * @param from string which holds a date
   * @param to   string which holds a date
   */
  private fun analyzeHotspots(geoprocessingTask: GeoprocessingTask, from: String, to: String) {
    geoprocessingTask.loadAsync()

    // a map image layer is generated as a result, clear previous results
    mapView.map.operationalLayers.clear()

    // create parameters for geoprocessing job
    val paramsFuture = geoprocessingTask.createDefaultParametersAsync()
    paramsFuture.addDoneListener {
      try {
        val geoprocessingParameters = paramsFuture.get()
        geoprocessingParameters.processSpatialReference = mapView.spatialReference
        geoprocessingParameters.outputSpatialReference = mapView.spatialReference

        val queryString = StringBuilder("(\"DATE\" > date '")
          .append(from)
          .append(" 00:00:00' AND \"DATE\" < date '")
          .append(to)
          .append(" 00:00:00')")

        val geoprocessingString = GeoprocessingString(queryString.toString())
        geoprocessingParameters.inputs["Query"] = geoprocessingString
        // create and start geoprocessing job
        val geoprocessingJob = geoprocessingTask.createJob(geoprocessingParameters)
        geoprocessingJob.start()

        // show progress
        val progressDialog = ProgressDialog(this).apply {
          setTitle(getString(R.string.app_name))
          setMessage(getString(R.string.dialog_text))
          setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
          show()
        }

        // update progress
        geoprocessingJob.addProgressChangedListener {
          val progress = geoprocessingJob.progress
          progressDialog.progress = progress
        }

        // when the job finishes
        geoprocessingJob.addJobDoneListener {
          // dismiss the dialog
          progressDialog.dismiss()

          when (geoprocessingJob.status) {
            Job.Status.SUCCEEDED -> {
              // get results
              val geoprocessingResult = geoprocessingJob.result
              val hotspotMapImageLayer = geoprocessingResult.mapImageLayer
              hotspotMapImageLayer.opacity = 0.5f

              // add new layer to map
              mapView.map.operationalLayers.add(hotspotMapImageLayer)

              // zoom to the layer extent
              hotspotMapImageLayer.addDoneLoadingListener {
                mapView.setViewpointGeometryAsync(hotspotMapImageLayer.fullExtent)
              }
            }
            else -> Toast.makeText(this, getString(R.string.job_failed), Toast.LENGTH_LONG).show()
          }
        }
      } catch (e: Exception) {
        val error = "Error generating geoprocessing parameters: " + e.message
        Toast.makeText(this, error, Toast.LENGTH_LONG).show()
        Log.e(TAG, error)
      }
    }
  }

  /**
   * Parse the date using a simple date format.
   */
  private fun parseDate(date: String): Date? {
    return SimpleDateFormat(getString(R.string.date_format), Locale.US).parse(date)
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
