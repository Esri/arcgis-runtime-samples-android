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

package com.esri.arcgisruntime.sample.analyzehotspots

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.concurrent.Job
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReference
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.tasks.geoprocessing.GeoprocessingString
import com.esri.arcgisruntime.tasks.geoprocessing.GeoprocessingTask
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.progressDialog
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// enum to flag whether the date picker calendar shown should be for the 'from' or 'to' date
enum class InputCalendar {
  From, To
}

class MainActivity : AppCompatActivity() {

//  private lateinit var TAG = MainActivity.class.getSimpleName()

  private lateinit var fromDateText: EditText
  private lateinit var toDateText: EditText

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // create a map with the BasemapType topographic
    val map = ArcGISMap(Basemap.createTopographic())
    //center for initial viewpoint
    val center = Point(-13671170.0, 5693633.0, SpatialReference.create(3857))
    //set initial viewpoint
    map.initialViewpoint = Viewpoint(center, 57779.0)
    // set the map to the map view
    mapView.map = map

    calendarButton.setOnClickListener {
      showDateRangeDialog()
    }
    calendarButton.performClick()
  }

  /**
   * Creates the date range dialog. Includes listeners to handle click events,
   * which call showCalendar(...) or analyzeHotspots(...).
   */
  private fun showDateRangeDialog() {
    // create custom dialog
    val dialog = Dialog(this)
    dialog.setContentView(R.layout.custom_alert_dialog)
    dialog.setCancelable(true)

    val minDate = parseDate(getString(R.string.min_date))
    val maxDate = parseDate(getString(R.string.max_date))

    fromDateText = dialog.findViewById(R.id.fromDateText)
    toDateText = dialog.findViewById(R.id.toDateText)
    val analyzeButton = dialog.findViewById<Button>(R.id.analyzeButton)

    fromDateText.setOnClickListener {
      showCalendar(InputCalendar.From, minDate, maxDate)
    }

    toDateText.setOnClickListener {
      showCalendar(InputCalendar.To, minDate, maxDate)
    }

    val geoprocessingTask = GeoprocessingTask(getString(R.string.hotspot_911_calls))

    analyzeButton.setOnClickListener {
      analyzeHotspots(
        geoprocessingTask,
        fromDateText.text.toString(),
        toDateText.text.toString(),
        false
      )
      dialog.dismiss()
    }

    dialog.show()
  }

  /**
   * Shows a date picker dialog and writes the date chosen to the correct editable text.
   *
   * @param inputCalendar enum which specifies which editable text the chosen date should be written to
   * @param _minDate
   * @param _maxDate
   */
  private fun showCalendar(inputCalendar: InputCalendar, _minDate: Date, _maxDate: Date) {
    var calendarMinDate = _minDate
    var calendarMaxDate = _maxDate

    // define the date picker dialog
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val datePickerDialog = DatePickerDialog(
      this,
      DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->

        val date = StringBuilder()
        date.append(year)
        date.append("-")
        date.append(monthOfYear + 1)
        date.append("-")
        date.append(dayOfMonth)

        if (inputCalendar == InputCalendar.From) {
          fromDateText.setText(date)
          calendarMinDate = parseDate(date.toString())
        } else if (inputCalendar == InputCalendar.To) {
          toDateText.setText(date)
          calendarMaxDate = parseDate(date.toString())
        }
      },
      year,
      month,
      day
    )

    datePickerDialog.datePicker.minDate = calendarMinDate.time
    datePickerDialog.datePicker.maxDate = calendarMaxDate.time
    if (inputCalendar == InputCalendar.From) {
      datePickerDialog.updateDate(1998, 0, 1)
    }
    datePickerDialog.show()

  }

  /**
   * Runs the geoprocessing job, updating progress while loading. On job done, loads the resulting
   * ArcGISMapImageLayer to the map and resets the Viewpoint of the MapView.
   *
   * @param geoprocessingTask Geoprocessing task to generate hotspots
   * @param from string which holds a date
   * @param to   string which holds a date
   * @param isCanceled flag to cancel operation
   */
  private fun analyzeHotspots(
    geoprocessingTask: GeoprocessingTask,
    from: String,
    to: String,
    isCanceled: Boolean
  ) {
    geoprocessingTask.loadAsync()

    // a map image layer is generated as a result, clear previous results
    mapView.map.operationalLayers.clear()

    // create parameters for geoprocessing job
    val paramsFuture = geoprocessingTask.createDefaultParametersAsync()
    paramsFuture.addDoneListener({
      val geoprocessingParameters = paramsFuture.get()
      geoprocessingParameters.processSpatialReference = mapView.spatialReference
      geoprocessingParameters.outputSpatialReference = mapView.spatialReference

      val queryString = StringBuilder("(\"DATE\" > date '")
        .append(from)
        .append(" 00:00:00' AND \"DATE\" < date '")
        .append(to)
        .append(" 00:00:00')")

      val geoprocessingString = GeoprocessingString(queryString.toString())
      geoprocessingParameters.inputs.put("Query", geoprocessingString)
      // create and start geoprocessing job
      val geoprocessingJob = geoprocessingTask.createJob(geoprocessingParameters)
      geoprocessingJob.start()

      // show progress
      val progressDialog = progressDialog(
        message = getString(R.string.dialog_text),
        title = getString(R.string.app_name)
      )

      // update progress
      geoprocessingJob.addProgressChangedListener {
        val progress = geoprocessingJob.progress
        progressDialog.progress = progress
      }

      geoprocessingJob.addJobDoneListener {
        when {
          geoprocessingJob.status == Job.Status.SUCCEEDED -> {
            progressDialog.dismiss()
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
          isCanceled -> alert(getString(R.string.job_canceled))
          else -> alert(getString(R.string.job_failed))
        }
      }
    })
  }

  /**
   * parse String to Date
   */
  private fun parseDate(data: String): Date {
    // create a simple date formatter to parse strings to date
    val simpleDateFormatter = SimpleDateFormat(getString(R.string.date_format), Locale.US)
    return simpleDateFormatter.parse(data)
  }

  override fun onPause() {
    super.onPause()
    mapView.pause()
  }

  override fun onResume() {
    super.onResume()
    mapView.resume()
  }

  override fun onDestroy() {
    super.onDestroy()
    mapView.dispose()
  }
}
