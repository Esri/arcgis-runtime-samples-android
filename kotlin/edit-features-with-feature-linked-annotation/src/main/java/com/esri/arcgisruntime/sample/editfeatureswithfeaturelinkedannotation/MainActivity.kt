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

package com.esri.arcgisruntime.sample.editfeatureswithfeaturelinkedannotation

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.data.Feature
import com.esri.arcgisruntime.data.Geodatabase
import com.esri.arcgisruntime.geometry.*
import com.esri.arcgisruntime.layers.AnnotationLayer
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.sample.editfeatureswithfeaturelinkedannotation.databinding.ActivityMainBinding
import com.esri.arcgisruntime.sample.editfeatureswithfeaturelinkedannotation.databinding.EditAttributeLayoutBinding
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    private val TAG = MainActivity::class.simpleName

    private var selectedFeature: Feature? = null
    private var selectedFeatureIsPolyline = false

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

        // NOTE: to be a writable geodatabase, this geodatabase must be generated from a service with a
        // GeodatabaseSyncTask. See the "generate geodatabase" sample to see how to generate a
        // geodatabase

        // create and load geodatabase
        val geodatabase = Geodatabase(getExternalFilesDir(null)?.path + "/loudoun_anno.geodatabase")
        geodatabase.loadAsync()
        geodatabase.addDoneLoadingListener {
            // create feature layers from tables in the geodatabase
            val addressPointFeatureLayer =
                FeatureLayer(geodatabase.getGeodatabaseFeatureTable("Loudoun_Address_Points_1"))
            val parcelLinesFeatureLayer =
                FeatureLayer(geodatabase.getGeodatabaseFeatureTable("ParcelLines_1"))
            // create annotation layers from tables in the geodatabase
            val addressPointsAnnotationLayer =
                AnnotationLayer(geodatabase.getGeodatabaseAnnotationTable("Loudoun_Address_PointsAnno_1"))
            val parcelLinesAnnotationLayer =
                AnnotationLayer(geodatabase.getGeodatabaseAnnotationTable("ParcelLinesAnno_1"))

            // create the map with a light gray canvas basemap
            val map = ArcGISMap(BasemapStyle.ARCGIS_LIGHT_GRAY).apply {
                // add the feature layers to the map
                operationalLayers.add(parcelLinesFeatureLayer)
                operationalLayers.add(addressPointFeatureLayer)
                // add the annotation layers to the map
                operationalLayers.add(parcelLinesAnnotationLayer)
                operationalLayers.add(addressPointsAnnotationLayer)
            }

            mapView.apply {
                // add the map to the map view
                this.map = map

                // set the initial viewpoint to Loudoun, Virginia
                setViewpoint(Viewpoint(39.0204, -77.4159, 2000.0))

                // set on tap behaviour
                onTouchListener =
                    object : DefaultMapViewOnTouchListener(applicationContext, mapView) {
                        override fun onSingleTapUp(e: MotionEvent): Boolean {
                            val screenPoint =
                                android.graphics.Point(e.x.roundToInt(), e.y.roundToInt())
                            selectOrMove(screenPoint)
                            return true
                        }
                    }
            }
        }
    }

    /**
     * Select the nearest feature, or move the point or polyline vertex to the given screen point.
     *
     * @param screenPoint at which to move or select feature
     */
    private fun selectOrMove(screenPoint: android.graphics.Point) {
        // if a feature hasn't been selected
        if (selectedFeature == null) {
            selectFeature(screenPoint)
        } else {
            // convert screen point to map point
            val mapPoint = mapView.screenToLocation(screenPoint)
            // move the feature
            if (selectedFeatureIsPolyline) {
                movePolylineVertex(mapPoint)
            } else {
                movePoint(mapPoint)
            }
        }
    }

    /**
     * Select a feature near the given screen point using identify and, for a point feature, show a
     * dialog to edit attributes. Future taps will call move functions.
     *
     * @param screenPoint at which to select a feature
     */
    private fun selectFeature(screenPoint: android.graphics.Point) {
        // clear any previously selected features
        clearSelection()

        // identify across all layers
        val identifyLayerResultFuture = mapView.identifyLayersAsync(screenPoint, 10.0, false)
        identifyLayerResultFuture.addDoneListener {
            // get the list of result from the future
            val identifyLayerResult = identifyLayerResultFuture.get()
            // for each layer from which an element was identified
            identifyLayerResult.forEach { result ->
                // check if the layer is a feature layer, thereby excluding annotation layers
                (result.layerContent as? FeatureLayer)?.let { featureLayer ->
                    // get a reference to the identified feature
                    selectedFeature = result.elements[0] as? Feature
                    // if the selected feature is a polyline with any part containing more than one segment
                    // (i.e. a curve)
                    (selectedFeature?.geometry as? Polyline)?.parts?.forEach {
                        if (it.pointCount > 2) {
                            // set selected feature to null
                            selectedFeature = null
                            // show message reminding user to select straight (single segment) polylines only
                            Toast.makeText(
                                this,
                                getString(R.string.curved_polylines_message),
                                Toast.LENGTH_SHORT
                            )
                                .show()
                            // return early, effectively disallowing selection of multi segmented polylines
                            return@forEach
                        }
                    }
                    selectedFeature?.let {
                        // select the feature
                        featureLayer.selectFeature(it)
                        when (it.geometry.geometryType) {
                            // when selected feature is a point, show editable attributes
                            GeometryType.POINT -> showEditableAttributes(it)
                            // when selected feature is a polyline,
                            GeometryType.POLYLINE -> selectedFeatureIsPolyline = true
                            else -> Log.e(TAG, "Feature of unexpected geometry type selected")
                        }
                        // return, since a feature has been selected
                        return@addDoneListener
                    }
                }
            }
        }
    }

    /**
     * Create an alert dialog with edit texts to allow editing of the given feature's 'AD_ADDRESS' and
     * 'ST_STR_NAM' attributes.
     *
     * @param selectedFeature whose attributes will be edited
     */
    private fun showEditableAttributes(selectedFeature: Feature) {
        // inflate the edit attribute layout
        //val editAttributeView = layoutInflater.inflate(R.layout.edit_attribute_layout, null)
        val editAttributeLayoutBinding = EditAttributeLayoutBinding.inflate(LayoutInflater.from(this))
        // create an alert dialog
        AlertDialog.Builder(this).apply {
            setTitle("Edit feature attribute:")
            setView(editAttributeLayoutBinding.root)
            // populate edit texts with current attribute values
            editAttributeLayoutBinding.addressNumberEditText.setText(selectedFeature.attributes["AD_ADDRESS"].toString())
            editAttributeLayoutBinding.streetEditText.setText(selectedFeature.attributes["ST_STR_NAM"].toString())
            setPositiveButton("OK") { _, _ ->
                // set AD_ADDRESS value to the int from the edit text
                val editAttributeString = editAttributeLayoutBinding.addressNumberEditText.text.toString()
                if (editAttributeString != "") {
                    selectedFeature.attributes["AD_ADDRESS"] = editAttributeString.toInt()
                } else {
                    Toast.makeText(
                        applicationContext,
                        "AD_ADDRESS field must contain an integer!",
                        Toast.LENGTH_LONG
                    ).show()
                }
                // set ST_STR_NAM value to the string from edit text
                selectedFeature.attributes["ST_STR_NAM"] =
                    editAttributeLayoutBinding.streetEditText.text.toString()
                // update the selected feature's feature table
                selectedFeature.featureTable?.updateFeatureAsync(selectedFeature)
            }
            setNegativeButton("Cancel") { _, _ ->
                clearSelection()
            }
        }.show()
    }

    /**
     * Move the currently selected point feature to the given map point, by updating the selected
     * feature's geometry and feature table.
     *
     * @param mapPoint to which the point geometry should be moved to
     */
    private fun movePoint(mapPoint: Point) {
        // set the selected features' geometry to a new map point
        selectedFeature?.geometry = mapPoint
        // update the selected feature's feature table
        selectedFeature?.featureTable?.updateFeatureAsync(selectedFeature)
        // clear selection of point
        clearSelection()
    }

    /**
     * Move the last of the vertex point of the currently selected polyline to the given map point, by updating the selected
     * feature's geometry and feature table.
     *
     * @param mapPoint to which the last point of the polyline should be moved to
     */
    private fun movePolylineVertex(mapPoint: Point) {
        // get the selected feature's geometry as a polyline
        val polyline = selectedFeature?.geometry as Polyline
        // get the nearest vertex to the map point on the selected feature polyline
        val nearestVertex = GeometryEngine.nearestVertex(
            polyline,
            GeometryEngine.project(mapPoint, polyline.spatialReference) as Point
        )
        val polylineBuilder = PolylineBuilder(polyline)
        // get the part of the polyline nearest to the map point
        polylineBuilder.parts[nearestVertex.partIndex.toInt()].apply {
            // remove the nearest point to the map point
            removePoint(nearestVertex.pointIndex.toInt())
            // add the map point as the new point on the polyline
            addPoint(GeometryEngine.project(mapPoint, spatialReference) as Point)
        }
        // set the selected feature's geometry to the new polyline
        selectedFeature?.geometry = polylineBuilder.toGeometry()
        // update the selected feature's feature table
        selectedFeature?.featureTable?.updateFeatureAsync(selectedFeature)
        // clear selection of polyline
        clearSelection()
        selectedFeatureIsPolyline = false
    }

    /**
     * Clear selection from all feature layers.
     */
    private fun clearSelection() {
        mapView.map.operationalLayers.filterIsInstance<FeatureLayer>().forEach { featureLayer ->
            featureLayer.clearSelection()
        }
        selectedFeature = null
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
