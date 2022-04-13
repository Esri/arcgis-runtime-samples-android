/* Copyright 2022 Esri
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

package com.esri.arcgisruntime.sample.displaydimensions

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.esri.arcgisruntime.layers.DimensionLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.MobileMapPackage
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.sample.displaydimensions.databinding.ActivityMainBinding
import com.esri.arcgisruntime.sample.displaydimensions.databinding.DimensionsDialogLayoutBinding


class MainActivity : AppCompatActivity() {

    private val TAG: String = MainActivity::class.java.simpleName

    private lateinit var dimensionLayer: DimensionLayer
    private var isDimensionLayerEnabled: Boolean = true
    private var isDefinitionEnabled: Boolean = false


    private val activityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val mapView: MapView by lazy {
        activityMainBinding.mapView
    }

    private val settingsButton: ConstraintLayout by lazy {
        activityMainBinding.settingsLayout
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityMainBinding.root)

        // create and load a mobile map package
        val mobileMapPackage = MobileMapPackage(getExternalFilesDir(null)?.path + "/Edinburgh_Pylon_Dimensions.mmpk")

        mobileMapPackage.addDoneLoadingListener {
            // check the mmpk has loaded successfully and that it contains a map
            if (mobileMapPackage.loadStatus == LoadStatus.LOADED && mobileMapPackage.maps.isNotEmpty()) {
                // add the map from the mobile map package to the map view, and set a min scale to maintain dimension readability
                mapView.map = mobileMapPackage.maps[0]
                mapView.map.minScale = 35000.0

                // find the dimension layer within the map
                dimensionLayer = mapView.map.operationalLayers.firstOrNull { it is DimensionLayer } as DimensionLayer
            } else {
                val errorMessage = "Failed to load the mobile map package: " + mobileMapPackage.loadError.message
                Log.e(TAG, errorMessage)
                Toast.makeText(
                    this,
                    errorMessage,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        mobileMapPackage.loadAsync()

        settingsButton.setOnClickListener {
            // inflate the dialog layout and get references to each of its components
            val dialogBinding = DimensionsDialogLayoutBinding.inflate(LayoutInflater.from(this))
            val dimensionLayerSwitch = dialogBinding.dimensionLayerSwitch.apply {
                isChecked = isDimensionLayerEnabled
            }
            val definitionSwitch = dialogBinding.definitionSwitch.apply {
                isChecked = isDefinitionEnabled
            }

            // set up the dialog
            AlertDialog.Builder(this).apply {
                setView(dialogBinding.root)
                setTitle("Dimension options:")

                dimensionLayerSwitch.setOnCheckedChangeListener { _, isEnabled ->
                    // set the visibility of the dimension layer
                    dimensionLayer.isVisible = isEnabled
                    isDimensionLayerEnabled = isEnabled
                }


                definitionSwitch.setOnCheckedChangeListener { _, isEnabled ->
                    // set a definition expression to show dimension lengths of
                    // greater than or equal to 450m when the checkbox is selected,
                    // or to reset the definition expression to show all
                    // dimension lengths when unselected
                    val defExpression =
                        if (isEnabled) "DIMLENGTH >= 450" else ""
                    dimensionLayer.definitionExpression = defExpression
                    isDefinitionEnabled = isEnabled

                }

            }.show()
        }
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

