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
import com.esri.arcgisruntime.data.ServiceGeodatabase
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener
import com.esri.arcgisruntime.security.AuthenticationManager
import com.esri.arcgisruntime.security.DefaultAuthenticationChallengeHandler
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

  private val TAG = MainActivity::class.java.simpleName

  private val serviceGeodatabase: ServiceGeodatabase by lazy {
    ServiceGeodatabase("https://sampleserver7.arcgisonline.com/arcgis/rest/services/DamageAssessment/FeatureServer").apply {
      loadAsync()
    }
  }

  private var featureLayer: FeatureLayer? = null
  private var selectedFeature: Feature? = null
  private var createdVersionName: String = ""

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // create a default authentication challenge handler because this service requires authentication
    AuthenticationManager.setAuthenticationChallengeHandler(
      DefaultAuthenticationChallengeHandler(
        this
      )
    )

    // create a map with a streets vector basemap and set it to the mapview
    mapView.map = ArcGISMap(Basemap.createStreetsVector())

    serviceGeodatabase.addDoneLoadingListener {
      // check if the service geodatabase failed to load
      if (serviceGeodatabase.loadStatus != LoadStatus.LOADED) {
        serviceGeodatabase.loadError?.let {
          Log.e(TAG, "Service Geodatabase failed to load: ${it.cause}")
        }
        return@addDoneLoadingListener
      }
      // create a feature layer from the service geodatabase's service feature table
      val serviceFeatureTable = serviceGeodatabase.getTable(0)
      featureLayer = FeatureLayer(serviceFeatureTable).apply {
        addDoneLoadingListener {
          // zoom to the layer's extent
          mapView.setViewpointAsync(Viewpoint(fullExtent))
        }
      }
      // add the layer to the map
      mapView.map.apply {
        operationalLayers.add(featureLayer)
      }
      // display the current name in a text view
      currentVersionNameTextView.text = serviceGeodatabase.versionName

      mapView.onTouchListener = object : DefaultMapViewOnTouchListener(this, mapView) {
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
          // The default version, sde.DEFAULT, is protected and the user provided
          // is not the owner so edits will not be allowed.
          // So for simplicity, if the current version is the default, prevent editing
          if (serviceGeodatabase.versionName.isBlank() || serviceGeodatabase.versionName == serviceGeodatabase.defaultVersionName) {
            val message = "This sample does not allow editing of features on the default version."
            Log.e(TAG, message)
            Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
            return true
          }

          val point = android.graphics.Point(e.x.toInt(), e.y.toInt())

          // if selected feature is not null then it should be moved and return early
          selectedFeature?.let {
            editFeatureLocation(it, point)
            return true
          }

          // if no feature should be moved, identify the feature at the tapped location
          try {
            val identifyFuture = mapView.identifyLayerAsync(featureLayer, point, 10.0, false)
            identifyFuture.addDoneListener {
              val identifyLayerResult = identifyFuture.get()
              // if there is a feature at the location, select it and edit its attribute
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

  /**
   * Shows a dialog to the user which is used to create a new version.
   * Calls createVersion() when the user confirms the dialog.
   *
   * @param view which called this method on click
   */
  fun createVersionDialog(view: View) {
    // inflate the view and store references to each of its components
    val dialogView = LayoutInflater.from(this).inflate(R.layout.create_version_dialog, null)
    val createNameEditText = dialogView.findViewById<EditText>(R.id.createNameEditText)
    val createDescriptionEditText =
      dialogView.findViewById<EditText>(R.id.createDescriptionEditText)
    val createAccessVersionSpinner =
      dialogView.findViewById<Spinner>(R.id.createAccessVersionSpinner)

    // set up the spinner to display options for the VersionAccess parameter for creating a version
    ArrayAdapter.createFromResource(
      this,
      R.array.version_access_array,
      android.R.layout.simple_spinner_item
    ).also { adapter ->
      adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
      createAccessVersionSpinner.adapter = adapter
    }

    // set up the dialog
    val builder = AlertDialog.Builder(this)
    builder
      .setView(dialogView)
      .setTitle("Create a new version")
      .setNegativeButton("Cancel") { _: DialogInterface, _: Int -> }
      .setPositiveButton("Confirm") { _: DialogInterface, _: Int ->
        // when the user confirms, create the version using the options selected
        createVersion(
          createNameEditText.text.toString(),
          VersionAccess.valueOf(createAccessVersionSpinner.selectedItem.toString()),
          createDescriptionEditText.text.toString()
        )
      }
      .create()
      .show()
  }

  /**
   * Creates a new version for this geodatabase
   *
   * @param versionName the name of the new version
   * @param versionAccess the access modifier for this version
   * @param description a text description of the versioin
   */
  private fun createVersion(
    versionName: String,
    versionAccess: VersionAccess,
    description: String
  ) {
    // create service version parameters with the parameters passed to this method
    val serviceVersionParameters = ServiceVersionParameters().apply {
      name = versionName
      access = versionAccess
      setDescription(description)
    }

    // create the version
    val serviceVersionInfoFuture = serviceGeodatabase.createVersionAsync(serviceVersionParameters)
    serviceVersionInfoFuture.addDoneListener {
      // get the new version's name and switch to it
      val serviceVersionInfo = serviceVersionInfoFuture.get()
      createdVersionName = serviceVersionInfo.name
      switchVersion(null)
    }

    // hide the create version button and allow the user to switch versions now
    createVersionButton.visibility = View.GONE
    switchVersionButton.visibility = View.VISIBLE
  }

  /**
   *  Switches between the created version and the default version
   *
   *  @param view which called this method on click
   */
  fun switchVersion(view: View?) {
    // don't switch versions if the new version has not been created yet or the name has not been stored
    if (createdVersionName.isBlank()) {
      val message = "Version names have not been initialized!"
      Log.e(TAG, message)
      Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    val versionName = when (serviceGeodatabase.versionName) {
      serviceGeodatabase.defaultVersionName -> createdVersionName
      createdVersionName -> serviceGeodatabase.defaultVersionName
      else -> serviceGeodatabase.defaultVersionName
    }

    // if the user has changed any features, apply them before switching
    if (serviceGeodatabase.hasLocalEdits()) {
      serviceGeodatabase.applyEditsAsync().addDoneListener {
        try {
          serviceGeodatabase.switchVersionAsync(versionName).addDoneListener {
            currentVersionNameTextView.text = serviceGeodatabase.versionName
          }
        } catch (e: Exception) {
          val error = "Failed to switch version: ${e.message}"
          Log.e(TAG, error)
          Toast.makeText(this@MainActivity, error, Toast.LENGTH_LONG).show()
        }
      }
    } else {
      try {
        serviceGeodatabase.switchVersionAsync(versionName).addDoneListener {
          currentVersionNameTextView.text = serviceGeodatabase.versionName
          featureLayer?.featureTable?.loadAsync()
        }
      } catch (e: Exception) {
        val error = "Failed to switch version: ${e.message}"
        Log.e(TAG, error)
        Toast.makeText(this@MainActivity, error, Toast.LENGTH_LONG).show()
      }
    }
  }

  /**
   * Creates a dialog which allows the user to edit a feature's TYPDAMAGE attribute or go on to edit
   * the feature's location
   */
  private fun editFeatureAttribute() {
    // if there is a selected feature
    selectedFeature?.let { feature ->
      val dialogView =
        LayoutInflater.from(this).inflate(R.layout.edit_feature_attribute_dialog, null)
      val featureAttributeSpinner = dialogView.findViewById<Spinner>(R.id.featureAttributeSpinner)
      // set up the spinner with acceptable TYPDAMAGE values
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

      // create the dialog
      val builder = AlertDialog.Builder(this)
      builder.setView(dialogView)
        .setTitle(feature.attributes["PLACENAME"].toString())
        .setNegativeButton("Cancel") { _: DialogInterface, _: Int ->
          // clear the selection
          selectedFeature = null
          featureLayer?.clearSelection()
        }
        .setNeutralButton("Edit location") { _: DialogInterface, _: Int ->
          // change the attribute
          feature.attributes["TYPDAMAGE"] = featureAttributeSpinner.selectedItem.toString()
          feature.featureTable.updateFeatureAsync(feature).addDoneListener {
            serviceGeodatabase.applyEditsAsync()
          }
        }
        .setPositiveButton("Confirm") { _: DialogInterface, _: Int ->
          // change the attribute
          feature.attributes["TYPDAMAGE"] = featureAttributeSpinner.selectedItem.toString()
          feature.featureTable.updateFeatureAsync(feature).addDoneListener {
            serviceGeodatabase.applyEditsAsync()
          }
          // clear the selection
          featureLayer?.clearSelection()
          selectedFeature = null
        }
        .setOnCancelListener {
          // clear the selection if the user taps outside the dialog
          featureLayer?.clearSelection()
          selectedFeature = null
        }
        .create()
        .show()
    }
  }

  /**
   * Changes the location of a feature on the map
   *
   * @param feature to move
   * @param screenPoint where the user tapped on the screen
   */
  private fun editFeatureLocation(feature: Feature, screenPoint: android.graphics.Point) {
    feature.geometry = mapView.screenToLocation(screenPoint)
    // features will not update visually until the feature table has been updated
    feature.featureTable.updateFeatureAsync(feature).addDoneListener {
      serviceGeodatabase.applyEditsAsync()
    }
    featureLayer?.clearSelection()
    selectedFeature = null
  }

  override fun onResume() {
    super.onResume()
    mapView.resume()
  }

  override fun onPause() {
    mapView.pause()
    super.onPause()
  }

  override fun onDestroy() {
    mapView.dispose()
    super.onDestroy()
  }
}
