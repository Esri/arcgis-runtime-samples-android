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
import android.graphics.Point
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.arcgisservices.ServiceVersionParameters
import com.esri.arcgisruntime.arcgisservices.VersionAccess
import com.esri.arcgisruntime.data.Feature
import com.esri.arcgisruntime.data.ServiceGeodatabase
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.sample.editwithbranchversioning.databinding.ActivityMainBinding
import com.esri.arcgisruntime.sample.editwithbranchversioning.databinding.CreateVersionDialogBinding
import com.esri.arcgisruntime.sample.editwithbranchversioning.databinding.EditFeatureAttributeDialogBinding
import com.esri.arcgisruntime.security.UserCredential

class MainActivity : AppCompatActivity() {

  private val TAG = MainActivity::class.java.simpleName

  private val serviceGeodatabase: ServiceGeodatabase by lazy {
    ServiceGeodatabase("https://sampleserver7.arcgisonline.com/server/rest/services/DamageAssessment/FeatureServer")
  }

  private var featureLayer: FeatureLayer? = null
  private var selectedFeature: Feature? = null
  private var createdVersionName: String = ""

  private val activityMainBinding by lazy {
    ActivityMainBinding.inflate(layoutInflater)
  }

  private val mapView: MapView by lazy {
    activityMainBinding.mapView
  }

  private val switchVersionButton: Button by lazy {
    activityMainBinding.switchVersionButton
  }

  private val currentVersionNameTextView: TextView by lazy {
    activityMainBinding.currentVersionNameTextView
  }

  private val createVersionButton: Button by lazy {
    activityMainBinding.createVersionButton
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(activityMainBinding.root)

    // authentication with an API key or named user is required to access basemaps and other
    // location services
    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY)

    // hardcode user credentials since this sample has been setup to work with this specific
    // service. Normally you'd handle authentication with the AuthenticationChallengeHandler
    serviceGeodatabase.credential =
      UserCredential(
        getString(R.string.editor01_username),
        getString(R.string.editor01_password)
      )
    // load the service geodatabase
    serviceGeodatabase.loadAsync()
    // when the service geodatabase has loaded
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

      // create a map with a streets vector basemap and add the feature layer
      val map = ArcGISMap(BasemapStyle.ARCGIS_STREETS).apply {
        // add the layer to the map
        operationalLayers.add(featureLayer)
      }

      mapView.apply {
        // set the map to the map view
        this.map = map
        // set on touch listener for single taps
        onTouchListener =
          object : DefaultMapViewOnTouchListener(this@MainActivity, mapView) {
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
              // don't allow edits on the sde.DEFAULT version
              if (serviceGeodatabase.versionName.isBlank()
                || serviceGeodatabase.versionName == serviceGeodatabase.defaultVersionName
              ) {
                val message =
                  "This sample does not allow editing of features on the default version."
                Log.e(TAG, message)
                Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
                return true
              }

              // get the screen point of the single tap
              val screenPoint = android.graphics.Point(e.x.toInt(), e.y.toInt())

              // if selected feature is not null, edit the feature location
              selectedFeature?.let {
                editFeatureLocation(it, screenPoint)
                return true
              }

              // if no feature should be moved, identify the feature at the tapped location
              identifyFeature(screenPoint)
              return true
            }
          }
      }

      // display the current name in a text view
      currentVersionNameTextView.text = "Current version: ${serviceGeodatabase.versionName}"
    }
  }

  /**
   * Shows a dialog to the user which is used to create a new version. Calls create version when the
   * user confirms the dialog.
   *
   * @param view which called this method on click
   */
  fun createVersionDialog(view: View) {
    // inflate the view and get references to each of its components
    val dialogBinding = CreateVersionDialogBinding.inflate(LayoutInflater.from(this))
    val createNameEditText = dialogBinding.createNameEditText
    val createDescriptionEditText = dialogBinding.createDescriptionEditText
    val createAccessVersionSpinner = dialogBinding.createAccessVersionSpinner

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
    AlertDialog.Builder(this).apply {
      setView(dialogBinding.root)
      setTitle("Create a new version")
      setNegativeButton("Cancel") { _: DialogInterface, _: Int -> }
      setPositiveButton("Create") { _: DialogInterface, _: Int ->
        // when the user confirms check a name has been entered
        if (createNameEditText.text.toString().isNotEmpty()) {
          // create the version with the given parameters
          createVersion(
            createNameEditText.text.toString(),
            VersionAccess.valueOf(createAccessVersionSpinner.selectedItem.toString()),
            createDescriptionEditText.text.toString()
          )
        } else {
          Toast.makeText(
            this@MainActivity,
            "A version name is required!",
            Toast.LENGTH_LONG
          ).show()
        }
      }
    }.create().show()
  }

  /**
   * Creates a new version for this geodatabase
   *
   * @param versionName the name of the new version
   * @param versionAccess the access modifier for this version
   * @param description a text description of the version
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
    val serviceVersionInfoFuture =
      serviceGeodatabase.createVersionAsync(serviceVersionParameters)
    serviceVersionInfoFuture.addDoneListener {
        try {
            // get the new version's name and switch to it
            val serviceVersionInfo = serviceVersionInfoFuture.get()
            createdVersionName = serviceVersionInfo.name
            switchVersion(null)
            // hide the create version button and allow the user to switch versions now
            createVersionButton.visibility = View.GONE
            switchVersionButton.visibility = View.VISIBLE
            } catch (e: java.lang.Exception) {
            val errorMessage = "Error getting service info: " + e.message
            Log.e(TAG, errorMessage)
            Toast.makeText(this, errorMessage,Toast.LENGTH_SHORT).show()
        }
    }
  }

  /**
   *  Switches between the version created by the user and the default version
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

    // switch between default and created version names
    val versionName = when (serviceGeodatabase.versionName) {
      serviceGeodatabase.defaultVersionName -> createdVersionName
      createdVersionName -> serviceGeodatabase.defaultVersionName
      else -> serviceGeodatabase.defaultVersionName
    }

    // if the user has changed any features
    if (serviceGeodatabase.hasLocalEdits()) {
      // apply those changes
      serviceGeodatabase.applyEditsAsync().addDoneListener {
        try {
          // switch versions
          serviceGeodatabase.switchVersionAsync(versionName).addDoneListener {
            currentVersionNameTextView.text =
              "Current version: ${serviceGeodatabase.versionName}"
          }
        } catch (e: Exception) {
          val error = "Failed to switch version: ${e.message}"
          Log.e(TAG, error)
          Toast.makeText(this@MainActivity, error, Toast.LENGTH_LONG).show()
        }
      }
    } else {
      try {
        // switch versions
        serviceGeodatabase.switchVersionAsync(versionName).addDoneListener {
          currentVersionNameTextView.text =
            "Current version: ${serviceGeodatabase.versionName}"
        }
      } catch (e: Exception) {
        val error = "Failed to switch version: ${e.message}"
        Log.e(TAG, error)
        Toast.makeText(this@MainActivity, error, Toast.LENGTH_LONG).show()
      }
    }
  }

  /**
   * Identify the first feature near the given screen point. If one is found, select it and call
   * edit feature attribute.
   *
   * @param screenPoint to identify from
   */
  private fun identifyFeature(screenPoint: Point) {
    try {
      val identifyFuture = mapView.identifyLayerAsync(featureLayer, screenPoint, 10.0, false)
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
  }

  /**
   * Changes the location of the selected feature on the map.
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
    editFeatureAttribute()
  }

  /**
   * Creates a dialog which allows the user to edit a feature's TYPDAMAGE attribute or go on to edit
   * the feature's location
   */
  private fun editFeatureAttribute() {
    // if there is a selected feature
    selectedFeature?.let { feature ->
      val editFeatureAttributeDialogBinding =
        EditFeatureAttributeDialogBinding.inflate(layoutInflater)
      val featureAttributeSpinner = editFeatureAttributeDialogBinding.featureAttributeSpinner
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
      AlertDialog.Builder(this).apply {
        setView(editFeatureAttributeDialogBinding.root)
        setTitle(feature.attributes["PLACENAME"].toString())
        setNegativeButton("Cancel") { _: DialogInterface, _: Int ->
          // clear the selection
          selectedFeature = null
          featureLayer?.clearSelection()
        }
        setNeutralButton("Edit location") { _: DialogInterface, _: Int ->
          // change the attribute
          feature.attributes["TYPDAMAGE"] =
            featureAttributeSpinner.selectedItem.toString()
          feature.featureTable.updateFeatureAsync(feature).addDoneListener {
            serviceGeodatabase.applyEditsAsync()
          }
        }
        setPositiveButton("Confirm") { _: DialogInterface, _: Int ->
          // change the attribute
          feature.attributes["TYPDAMAGE"] =
            featureAttributeSpinner.selectedItem.toString()
          feature.featureTable.updateFeatureAsync(feature).addDoneListener {
            serviceGeodatabase.applyEditsAsync()
          }
          // clear the selection
          featureLayer?.clearSelection()
          selectedFeature = null
        }
        setOnCancelListener {
          // clear the selection if the user taps outside the dialog
          featureLayer?.clearSelection()
          selectedFeature = null
        }.create().show()
      }
    }
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
