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

package com.esri.arcgisruntime.sample.editwithbranchversioning

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.arcgisservices.ServiceVersionParameters
import com.esri.arcgisruntime.arcgisservices.VersionAccess
import com.esri.arcgisruntime.data.Feature
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.data.ServiceGeodatabase
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener
import com.esri.arcgisruntime.security.UserCredential
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

  private val TAG = MainActivity::class.java.simpleName

  private val serviceGeodatabase: ServiceGeodatabase by lazy {
    ServiceGeodatabase("https://sampleserver7.arcgisonline.com/arcgis/rest/services/DamageAssessment/FeatureServer").apply {
      credential = UserCredential("editor01", "editor01.password")
      loadAsync()
    }
  }

  private var featureLayer: FeatureLayer? = null

  private var currentVersionName: String = ""
  private var createdVersionName: String = ""
  private var defaultVersionName: String = ""

  private var shouldEditLocation = false

  private var selectedFeature: Feature? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    mapView.map = ArcGISMap(Basemap.createStreetsVector())

    serviceGeodatabase.addDoneLoadingListener {
      if (serviceGeodatabase.loadStatus != LoadStatus.LOADED) {
        serviceGeodatabase.loadError?.let {
          Log.e(TAG, "Service Geodatabase failed to load: ${it.cause}")
        }
        return@addDoneLoadingListener
      }
      val serviceFeatureTable = serviceGeodatabase.getTable(0)
      featureLayer = FeatureLayer(serviceFeatureTable).apply {
        addDoneLoadingListener {
          mapView.setViewpointAsync(Viewpoint(fullExtent))
        }
      }
      mapView.map.apply {
        operationalLayers.add(featureLayer)
      }
      defaultVersionName = serviceGeodatabase.defaultVersionName
      currentVersionName = serviceGeodatabase.versionName
      currentVersionNameTextView.text = currentVersionName

      mapView.onTouchListener = object : DefaultMapViewOnTouchListener(this, mapView) {
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
          if (currentVersionName.isBlank() || currentVersionName == defaultVersionName) {
            val message = "This sample does not allow editing of features on the default version."
            Log.e(TAG, message)
            Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
            return true
          }
          val point = android.graphics.Point(e.x.toInt(), e.y.toInt())
          if (shouldEditLocation) {
            selectedFeature?.let { editFeatureLocation(it, point) }
            return true
          }
          try {
            val identifyFuture = mapView.identifyLayerAsync(featureLayer, point, 10.0, false)
            identifyFuture.addDoneListener {
              val identifyLayerResult = identifyFuture.get()
              if (identifyLayerResult.elements.isNotEmpty()) {
                val feature = identifyLayerResult.elements[0] as Feature
                featureLayer?.selectFeature(feature)
                selectedFeature = feature
                editFeatureAttribute()
              }
            }
          } catch (e: Exception) {
            val error = "Identify layer failed: ${e.message}"
            Log.e(TAG, error)
            Toast.makeText(this@MainActivity, error, Toast.LENGTH_LONG).show()
          }
          return true
        }
      }
    }
  }

  fun createBranchDialog(view: View) {
    val dialogView = LayoutInflater.from(this).inflate(R.layout.create_branch_dialog, null)
    val createNameEditText = dialogView.findViewById<EditText>(R.id.createNameEditText)
    val createDescriptionEditText =
      dialogView.findViewById<EditText>(R.id.createDescriptionEditText)
    val createAccessVersionSpinner =
      dialogView.findViewById<Spinner>(R.id.createAccessVersionSpinner)

    ArrayAdapter.createFromResource(
      this,
      R.array.version_access_array,
      android.R.layout.simple_spinner_item
    ).also { adapter ->
      adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
      createAccessVersionSpinner.adapter = adapter
    }

    val builder = AlertDialog.Builder(this)
    builder
      .setView(dialogView)
      .setTitle("Create a new version")
      .setNegativeButton("Cancel") { dialog: DialogInterface, _: Int -> dialog.cancel() }
      .setPositiveButton("Confirm") { dialog: DialogInterface, _: Int ->
        createBranch(
          createNameEditText.text.toString(),
          VersionAccess.valueOf(createAccessVersionSpinner.selectedItem.toString()),
          createDescriptionEditText.text.toString()
        )
        dialog.dismiss()
      }
      .create()
      .show()
  }

  private fun createBranch(versionName: String, versionAccess: VersionAccess, description: String) {
    val serviceVersionInfoFuture = serviceGeodatabase.fetchVersionsAsync()
    serviceVersionInfoFuture.addDoneListener {
      val serviceVersionInfo = serviceVersionInfoFuture.get()

      val serviceVersionParameters = ServiceVersionParameters().apply {
        name = versionName
        access = versionAccess
        setDescription(description)
      }

      serviceGeodatabase.createVersionAsync(serviceVersionParameters).addDoneListener {
        createdVersionName = versionName
        switchVersion(null)
      }

      createBranchButton.visibility = View.GONE
      switchVersionButton.visibility = View.VISIBLE
    }
  }

  fun switchVersion(view: View?) {
    if (createdVersionName.isBlank() || defaultVersionName.isBlank()) {
      val message = "Version names have not been initialized!"
      Log.e(TAG, message)
      Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    val versionName = when (currentVersionName) {
      defaultVersionName -> createdVersionName
      createdVersionName -> defaultVersionName
      else -> defaultVersionName
    }

    val serviceFeatureTable = featureLayer?.featureTable as ServiceFeatureTable

    if (serviceGeodatabase.hasLocalEdits()) {
      serviceGeodatabase.applyEditsAsync().addDoneListener {
        serviceGeodatabase.switchVersionAsync(versionName).addDoneListener {
          currentVersionName = versionName
          currentVersionNameTextView.text = currentVersionName
        }
      }
    } else {
      serviceGeodatabase.switchVersionAsync(versionName).addDoneListener {
        currentVersionName = versionName
        currentVersionNameTextView.text = currentVersionName
        featureLayer?.featureTable?.loadAsync()
      }
    }
  }

  private fun editFeatureAttribute() {
    selectedFeature?.let { feature ->
      val dialogView =
        LayoutInflater.from(this).inflate(R.layout.edit_feature_attribute_dialog, null)
      val featureAttributeSpinner = dialogView.findViewById<Spinner>(R.id.featureAttributeSpinner)
      ArrayAdapter.createFromResource(
        this,
        R.array.feature_attribute_array,
        android.R.layout.simple_spinner_item
      ).also { adapter ->
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        featureAttributeSpinner.adapter = adapter
      }

      featureAttributeSpinner.setSelection(
        resources.getStringArray(R.array.feature_attribute_array)
          .indexOf(feature.attributes["TYPDAMAGE"])
      )

      val builder = AlertDialog.Builder(this)
      builder.setView(dialogView)
        .setTitle("Edit feature attribute")
        .setNegativeButton("Cancel") { dialog: DialogInterface, id: Int ->
          selectedFeature = null
          featureLayer?.clearSelection()
          dialog.cancel()
        }
        .setPositiveButton("Confirm") { dialog: DialogInterface, id: Int ->
          feature.attributes["TYPDAMAGE"] = featureAttributeSpinner.selectedItem.toString()
          feature.featureTable.updateFeatureAsync(feature)
          shouldEditLocation = true
          dialog.dismiss()
        }
        .create()
        .show()
    }
  }

  private fun editFeatureLocation(feature: Feature, screenPoint: android.graphics.Point) {
    feature.geometry = mapView.screenToLocation(screenPoint)
    feature.featureTable.updateFeatureAsync(feature)
    featureLayer?.clearSelection()
    shouldEditLocation = false
    selectedFeature = null
  }

}
