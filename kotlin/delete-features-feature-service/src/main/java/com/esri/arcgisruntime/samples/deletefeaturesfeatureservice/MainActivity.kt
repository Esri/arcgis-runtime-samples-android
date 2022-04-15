/*
 * Copyright 2019 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.esri.arcgisruntime.samples.deletefeaturesfeatureservice

import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.ArcGISRuntimeException
import com.esri.arcgisruntime.data.Feature
import com.esri.arcgisruntime.data.QueryParameters
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.data.ServiceGeodatabase
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.GeoElement
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.Callout
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.samples.deletefeaturesfeatureservice.databinding.ActivityMainBinding
import com.esri.arcgisruntime.samples.deletefeaturesfeatureservice.databinding.ViewCalloutBinding
import java.util.concurrent.ExecutionException

class MainActivity : AppCompatActivity(), ConfirmDeleteFeatureDialog.OnButtonClickedListener {

    private lateinit var featureTable: ServiceFeatureTable

    private lateinit var featureLayer: FeatureLayer

    private val activityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val mapView: MapView by lazy {
        activityMainBinding.mapView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityMainBinding.root)

        // authentication with an API key or named user is required to access basemaps and other
        // location services
        ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY)

        // create a map with streets basemap
        with(ArcGISMap(BasemapStyle.ARCGIS_STREETS)) {
            // set map to be displayed in map view
            mapView.map = this
        }

        // set the map view's view point
        mapView.setViewpoint(Viewpoint(40.0, -95.0, 10000000.0))

        // create and load the service geodatabase
        val serviceGeodatabase = ServiceGeodatabase(getString(R.string.feature_layer_url))
        serviceGeodatabase.loadAsync()
        serviceGeodatabase.addDoneLoadingListener {
            // create a feature layer using the first layer in the ServiceFeatureTable
            featureTable = serviceGeodatabase.getTable(0)
            // create a feature layer from table
            featureLayer = FeatureLayer(featureTable)
            // add the layer to the map
            mapView.map.operationalLayers.add(featureLayer)
        }

        mapView.onTouchListener = object : DefaultMapViewOnTouchListener(this, mapView) {
            override fun onSingleTapConfirmed(motionEvent: MotionEvent?): Boolean {
                motionEvent?.let { event ->
                    // create a screen point from where the user clicked
                    android.graphics.Point(event.x.toInt(), event.y.toInt()).let { screenPoint ->
                        // identify the clicked feature
                        with(mapView.identifyLayerAsync(featureLayer, screenPoint, 1.0, false)) {
                            this.addDoneListener {
                                try {
                                    // get first element found and ensure that it is an instance of Feature before allowing user to delete
                                    // using callout
                                    (this.get().elements?.firstOrNull() as? Feature)?.let { feature ->
                                        // create a map point from a screen point
                                        mapView.screenToLocation(screenPoint).let {
                                            inflateCallout(mapView, feature, it).show()
                                        }
                                    }
                                } catch (e: InterruptedException) {
                                    logToUser(
                                        true,
                                        getString(R.string.error_getting_identify_result, e.cause?.message)
                                    )
                                } catch (e: ExecutionException) {
                                    logToUser(
                                        true,
                                        getString(R.string.error_getting_identify_result, e.cause?.message)
                                    )
                                }
                            }
                        }
                    }
                }
                return super.onSingleTapConfirmed(motionEvent)
            }
        }
    }

    /**
     * Method gets an instance of [Callout] from a [MapView] and inflates a [View] from a layout
     * to display as the content of the [Callout].
     *
     * @param mapView instance of [MapView] where the [Callout] is to be displayed
     * @param feature used to set the [GeoElement] of the [Callout]
     * @param point   the location of the user's tap
     * @return a [Callout] to display on a [MapView]
     */
    private fun inflateCallout(mapView: MapView, feature: GeoElement, point: Point): Callout {
        val viewCalloutBinding = ViewCalloutBinding.inflate(layoutInflater)
        // set OnClickListener for Callout content
        viewCalloutBinding.calloutViewCallToAction.setOnClickListener {
            // get objectid from feature attributes and pass to function to confirm deletion
            confirmDeletion((feature.attributes["objectid"].toString()))
            // dismiss callout
            mapView.callout.dismiss()
        }
        // set callout content as inflated View
        mapView.callout.content = viewCalloutBinding.root
        // set callout GeoElement as feature at tap location
        mapView.callout.setGeoElement(feature, point)
        return mapView.callout
    }

    /**
     * Method displays instance of [ConfirmDeleteFeatureDialog] to allow user to confirm their intent to delete
     * a [Feature].
     *
     * @param featureId id of feature to be deleted
     */
    private fun confirmDeletion(featureId: String) {
        ConfirmDeleteFeatureDialog.newInstance(featureId)
            .show(supportFragmentManager, ConfirmDeleteFeatureDialog::class.java.simpleName)
    }

    /**
     * Callback from [ConfirmDeleteFeatureDialog], invoked when positive button has been clicked in dialog.
     *
     * @param featureId id of feature to be deleted
     */
    override fun onDeleteFeatureClicked(featureId: String) {
        // query feature layer to find element by id
        val queryParameters = QueryParameters()
        queryParameters.whereClause = String.format("OBJECTID = %s", featureId)

        with(featureLayer.featureTable.queryFeaturesAsync(queryParameters)) {
            this.addDoneListener {
                try {
                    // check result has a feature
                    this.get().iterator().next()?.let {
                        // delete found features
                        deleteFeature(it, featureTable) {
                            applyEdits(featureTable)
                        }
                    }
                } catch (e: InterruptedException) {
                    logToUser(true, getString(R.string.error_feature_deletion, e.cause?.message))
                } catch (e: ExecutionException) {
                    logToUser(true, getString(R.string.error_feature_deletion, e.cause?.message))
                }
            }
        }
    }

    /**
     * Deletes a feature from a [ServiceFeatureTable] and applies the changes to the
     * server.
     *
     * @param feature                     [Feature] to delete
     * @param featureTable                [ServiceFeatureTable] to delete [Feature] from
     * @param onDeleteFeatureDoneListener [Runnable] to be invoked when action has completed
     */
    private fun deleteFeature(
        feature: Feature, featureTable: ServiceFeatureTable,
        onDeleteFeatureDoneListener: Runnable
    ) {
        // delete feature from the feature table and apply edit to server
        featureTable.deleteFeatureAsync(feature).addDoneListener(onDeleteFeatureDoneListener)
    }

    /**
     * Sends any edits on the [ServiceFeatureTable] to the server.
     *
     * @param featureTable [ServiceFeatureTable] to apply edits to
     */
    private fun applyEdits(featureTable: ServiceFeatureTable) {
        // apply the changes to the server
        with(featureTable.serviceGeodatabase.applyEditsAsync()) {
            this.addDoneListener {
                try {
                    // check result has an edit
                    this.get().iterator().next()?.let {
                        logToUser(false, getString(R.string.success_feature_deleted))
                    }
                } catch (e: ArcGISRuntimeException) {
                    logToUser(true, getString(R.string.error_applying_edits, e.cause?.message))
                } catch (e: InterruptedException) {
                    logToUser(true, getString(R.string.error_applying_edits, e.cause?.message))
                } catch (e: ExecutionException) {
                    logToUser(true, getString(R.string.error_applying_edits, e.cause?.message))
                }
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

    /**
     * AppCompatActivity extensions
     **/
    private val logTag get() = this::class.java.simpleName

    private fun AppCompatActivity.logToUser(isError: Boolean, message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        if (isError) {
            Log.e(logTag, message)
        } else {
            Log.d(logTag, message)
        }
    }
}