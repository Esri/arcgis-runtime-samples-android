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

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
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
import kotlin.math.floor

class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val calendar = Calendar.getInstance()
    calendar.apply {
      set(Calendar.HOUR_OF_DAY, 12)
      set(Calendar.MINUTE, 0)
    }

    sceneView.apply {
      setViewpointCamera(Camera(48.37, -4.50, 1000.0, 10.0, 70.0, 0.0))
      atmosphereEffect = AtmosphereEffect.REALISTIC
      sunTime = calendar
      sunLighting = LightingMode.LIGHT_AND_SHADOWS
    }
    sceneView.scene = ArcGISScene().apply {
      basemap = Basemap.createTopographic()
      baseSurface = Surface().apply {
        elevationSources.add(ArcGISTiledElevationSource("http://elevation3d.arcgis.com/arcgis/rest/services/WorldElevation3D/Terrain3D/ImageServer"))
      }
      operationalLayers.add(ArcGISSceneLayer("http://tiles.arcgis.com/tiles/P3ePLMYs2RVChkJx/arcgis/rest/services/Buildings_Brest/SceneServer/layers/0"))
    }

    dateTextView.text = calendar.time.toString().substring(0, 16)

    seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
      override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        val hours = floor((progress / 60.0)).toInt()
        val minutes = progress % 60
        calendar.apply {
          set(Calendar.HOUR_OF_DAY, hours)
          set(Calendar.MINUTE, minutes)
        }
        dateTextView.text = calendar.time.toString().substring(0, 16)
        sceneView.sunTime = calendar
      }

      override fun onStartTrackingTouch(seekBar: SeekBar?) {}
      override fun onStopTrackingTouch(seekBar: SeekBar?) {}
    })

    // create a spinner adapter to select lighting modes
    ArrayAdapter.createFromResource(
      this,
      R.array.lighting_modes,
      android.R.layout.simple_spinner_item
    ).also { adapter ->
      adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
      spinner.adapter = adapter
    }

    // change the lighting mode when a spinner item is selected
    spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener,
      AdapterView.OnItemClickListener {
      override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
        val mode = parent.getItemAtPosition(position) as String
        sceneView.sunLighting = when (mode) {
          "No sun" -> LightingMode.NO_LIGHT
          "Sun only" -> LightingMode.LIGHT
          "Sun and shadows" -> LightingMode.LIGHT_AND_SHADOWS
          else -> LightingMode.LIGHT_AND_SHADOWS
        }
      }
      override fun onNothingSelected(parent: AdapterView<*>?) {}
      override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {}
    }

    sceneView.apply {
      // make sure the fab doesn't obscure the attribution bar
      addAttributionViewLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
        val layoutParams = fab.layoutParams as CoordinatorLayout.LayoutParams
        layoutParams.bottomMargin += bottom - oldBottom
      }
      // close the options when the scene is tapped
      setOnTouchListener { _, _ ->
        if (fab.isExpanded) {
          fab.isExpanded = false
        }
        true
      }
    }
    // open and close the options with the fab
    fab.setOnClickListener { fab.isExpanded = !fab.isExpanded }
  }
}
