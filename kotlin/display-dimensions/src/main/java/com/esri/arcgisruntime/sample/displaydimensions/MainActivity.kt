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
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.layers.DimensionLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.MobileMapPackage
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.sample.displaydimensions.databinding.ActivityMainBinding
import java.io.File


class MainActivity : AppCompatActivity() {

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

        // authentication with an API key or named user is required to access basemaps and other
        // location services
        ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY)

        // create a map with the BasemapType topographic
        val map = ArcGISMap(BasemapStyle.ARCGIS_NAVIGATION_NIGHT)

        val mmpkFilePath =
            File(externalCacheDir, getString(R.string.Edinburgh_Pylon_Dimensions)).path

        val mobileMapPackage = MobileMapPackage(mmpkFilePath)

        Log.e("now","LOADING")

        mobileMapPackage.addDoneLoadingListener {
            // check the mmpk has loaded successfully and that it contains a map
            if (mobileMapPackage.loadStatus == LoadStatus.LOADED && mobileMapPackage.maps.isNotEmpty()) {
                // add the map from the mobile map package to the map view, and set a min scale to maintain dimension readability
                mapView.map = mobileMapPackage.maps[0]
                mapView.map.minScale = 35000.0

                // find the dimension layer within the map
                val operationLayers = mapView.map.operationalLayers
                for (layer in mapView.map.operationalLayers) {
                    if (layer is DimensionLayer) {
                        val dimensionLayer = layer
                        Toast.makeText(
                            activityMainBinding.root.context,
                            dimensionLayer.name,
                            Toast.LENGTH_SHORT
                        ).show()
                        // set the label to the name of the dimension layer
                        //dimensionLayerName.setText(dimensionLayer.getName())
                        // enable the vbox for dimension layer controls
                        //controlsVBox.setDisable(false)
                        //visibilityCheckBox.setSelected(dimensionLayer.isVisible())
                    }
                }
            } else {
                Toast.makeText(
                    activityMainBinding.root.context,
                    "Failed to load the mobile map package",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        mobileMapPackage.loadAsync()

        // set a definition expression to show dimension lengths of greater than or equal to 450m when the checkbox is selected,
        // or to reset the definition expression to show all dimension lengths when unselected
        //TODO

        settingsButton.setOnClickListener {
            //Inflate the dialog with custom view
            val mDialogView = LayoutInflater.from(this).inflate(R.layout.dialog_layout, null)
            //AlertDialogBuilder
            val mBuilder = AlertDialog.Builder(this)
                .setView(mDialogView)
                .setTitle("Dimension options:")
            //show dialog
            val  mAlertDialog = mBuilder.show()

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

