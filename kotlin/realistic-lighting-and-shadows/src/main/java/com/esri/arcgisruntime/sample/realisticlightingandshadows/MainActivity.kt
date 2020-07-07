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

package com.esri.arcgisruntime.sample.realisticlightingandshadows

import android.app.Dialog
import android.app.TimePickerDialog
import android.icu.util.GregorianCalendar
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.format.DateFormat
import android.transition.Scene
import android.view.View
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import com.esri.arcgisruntime.layers.ArcGISSceneLayer
import com.esri.arcgisruntime.mapping.ArcGISScene
import com.esri.arcgisruntime.mapping.ArcGISTiledElevationSource
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.Surface
import com.esri.arcgisruntime.mapping.view.AtmosphereEffect
import com.esri.arcgisruntime.mapping.view.Camera
import com.esri.arcgisruntime.mapping.view.LightingMode
import kotlinx.android.synthetic.main.activity_main.*
import java.util.Calendar

class MainActivity : AppCompatActivity() {

  private val calendar = Calendar.getInstance()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val scene = ArcGISScene().apply{
      basemap = Basemap.createTopographic()
    }
    sceneView.scene = scene

    val surface = Surface()
    val elevationSurface = ArcGISTiledElevationSource("http://elevation3d.arcgis.com/arcgis/rest/services/WorldElevation3D/Terrain3D/ImageServer")
    surface.elevationSources.add(elevationSurface)
    sceneView.scene.baseSurface = surface

    val sceneLayer = ArcGISSceneLayer("http://tiles.arcgis.com/tiles/P3ePLMYs2RVChkJx/arcgis/rest/services/Buildings_Brest/SceneServer/layers/0")
    scene.operationalLayers.add(sceneLayer)

    val camera = Camera(48.37, -4.50, 1000.0, 10.0, 70.0, 0.0)
    sceneView.setViewpointCamera(camera)

    sceneView.atmosphereEffect = AtmosphereEffect.REALISTIC

    sceneView.sunTime = calendar
    sceneView.sunLighting = LightingMode.LIGHT_AND_SHADOWS

    dateTextView.text = calendar.time.toString().substring(0,16)
  }

  fun showTimePickerDialog(v: View) {
    val updateSunPositionCallback = { calendar: Calendar ->
      dateTextView.text = calendar.time.toString().substring(0,16)
    sceneView.sunTime = calendar
    }
    TimePickerFragment(calendar, updateSunPositionCallback).show(supportFragmentManager, "timePicker")
  }
}

class TimePickerFragment(private val calendar: Calendar, val callback: (Calendar)->Unit) : DialogFragment(), TimePickerDialog.OnTimeSetListener {

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    // Use the current time as the default values for the picker
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)

    // Create a new instance of TimePickerDialog and return it
    return TimePickerDialog(activity, this, hour, minute, DateFormat.is24HourFormat(activity))
  }

  override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
    // Do something with the time chosen by the user
    calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE), hourOfDay, minute)
    callback(calendar)
//    dateTextView.text =
  }
}

