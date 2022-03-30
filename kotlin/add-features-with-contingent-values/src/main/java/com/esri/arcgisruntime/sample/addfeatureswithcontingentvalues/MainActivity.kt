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

package com.esri.arcgisruntime.sample.addfeatureswithcontingentvalues

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.data.*
import com.esri.arcgisruntime.geometry.GeometryEngine
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.layers.ArcGISVectorTiledLayer
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.sample.addfeatureswithcontingentvalues.databinding.ActivityMainBinding
import com.esri.arcgisruntime.sample.addfeatureswithcontingentvalues.databinding.AddFeatureLayoutBinding
import com.esri.arcgisruntime.symbology.SimpleFillSymbol
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.io.File

class MainActivity : AppCompatActivity() {

    private val TAG: String = MainActivity::class.java.simpleName

    private val activityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val bottomSheetBinding by lazy {
        AddFeatureLayoutBinding.inflate(layoutInflater)
    }

    private val mapView: MapView by lazy {
        activityMainBinding.mapView
    }

    private val graphicsOverlay = GraphicsOverlay()
    private lateinit var geoDatabase: Geodatabase
    private lateinit var feature: ArcGISFeature
    private lateinit var featureTable: ArcGISFeatureTable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityMainBinding.root)

        // authentication with an API key or named user is required to access basemaps and other
        // location services
        ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY)

        // create a temporary directory to use the geodatabase file
        createGeodatabaseCacheDirectory()

        // Use the vector tiled layer as a basemap
        val fillmoreVectorTiledLayer = ArcGISVectorTiledLayer(getExternalFilesDir(null)?.path + getString(R.string.topographic_map))
        mapView.map = ArcGISMap(Basemap(fillmoreVectorTiledLayer))
        mapView.graphicsOverlays.add(graphicsOverlay)

        // add a listener to the MapView to detect when a user has performed a single tap to add a new feature
        mapView.onTouchListener = object : DefaultMapViewOnTouchListener(this, mapView) {
            override fun onSingleTapConfirmed(motionEvent: MotionEvent?): Boolean {
                motionEvent?.let { event ->
                    // create a point from where the user clicked
                    android.graphics.Point(event.x.toInt(), event.y.toInt()).let { point ->
                        // create a map point and add a new feature to the service feature table
                        openBottomSheetView(mapView.screenToLocation(point))
                    }
                }
                mapView.performClick()
                return super.onSingleTapConfirmed(motionEvent)
            }
        }

        geoDatabase = Geodatabase(cacheDir.path + getString(R.string.bird_nests))
        geoDatabase.loadAsync()
        geoDatabase.addDoneLoadingListener {
            if (geoDatabase.loadStatus == LoadStatus.LOADED) {
                // Get and load the first feature table in the geodatabase
                featureTable = geoDatabase.geodatabaseFeatureTables[0] as ArcGISFeatureTable
                featureTable.loadAsync()
                featureTable.addDoneLoadingListener {
                    // Create and load the feature layer from the feature table
                    val featureLayer = FeatureLayer(featureTable)
                    // Add the feature layer to the map
                    mapView.map.operationalLayers.add(featureLayer)
                    // Set the map's viewpoint to the feature layer's full extent
                    val extent = featureLayer.fullExtent
                    mapView.setViewpoint(Viewpoint(extent))
                    // Add buffer graphics for the feature layer
                    queryFeatures()
                }
            } else {
                val error = "Error loading GeoDatabase: " + geoDatabase.loadError.message
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                Log.e(TAG, error)
            }
        }
    }

    /**
     * Geodatabase creates and uses various temporary files while processing a database,
     * which will need to be cleared before looking up the [geoDatabase] again. A copy of the original geodatabase
     * file is created in the cache folder.
     */
    private fun createGeodatabaseCacheDirectory() {
        // Clear cache directory
        File(cacheDir.path).deleteRecursively()
        // Copy over the original Geodatabase file to be used in the temp cache directory
        File(getExternalFilesDir(null)?.path + getString(R.string.bird_nests))
            .copyTo(File(cacheDir.path + getString(R.string.bird_nests)))
    }

    // Create buffer graphics for the features
    private fun queryFeatures() {
        // Create buffer graphics for the features
        val queryParameters = QueryParameters()
        // Set the where clause to filter for buffer sizes greater than 0
        queryParameters.whereClause = "BufferSize > 0"
        val queryFeaturesFuture = featureTable.queryFeaturesAsync(queryParameters)
        queryFeaturesFuture.addDoneListener {
            try {
                // clear the existing graphics
                graphicsOverlay.graphics.clear()
                // call get on the future to get the result
                val resultIterator = queryFeaturesFuture.get().iterator()
                if (resultIterator.hasNext()) {
                    // Create an array of graphics to add to the graphics overlay
                    val graphics = mutableListOf<Graphic>()
                    // Create graphic for each query result
                    while (resultIterator.hasNext())
                        graphics.add(createGraphic(resultIterator.next()))
                    // Add the graphics to the graphics overlay
                    graphicsOverlay.graphics.addAll(graphics)
                } else {
                    "No features found with BufferSize > 0".also {
                        Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                        Log.d(TAG, it)
                        return@addDoneListener
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                val message = "Error querying features: " + e.message
                Log.e(TAG, message)
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Create a graphic for the given feature
    private fun createGraphic(feature: Feature): Graphic {
        // Get the feature's buffer size
        val bufferSize = feature.attributes["BufferSize"] as Int
        // Get a polygon using the feature's buffer size and geometry
        val polygon = GeometryEngine.buffer(feature.geometry, bufferSize.toDouble())
        // Create the outline for the buffers
        val lineSymbol = SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLACK, 2F)
        // Create the buffer symbol
        val bufferSymbol = SimpleFillSymbol(SimpleFillSymbol.Style.FORWARD_DIAGONAL, Color.RED, lineSymbol)
        // Create an a graphic and add it to the array.
        return Graphic(polygon, bufferSymbol)
    }

    // Add a single feature to the map
    private fun openBottomSheetView(mapPoint: Point) {
        // Creates a new BottomSheetDialog
        val dialog = BottomSheetDialog(this)
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED

        setUpStatusAttributes()

        bottomSheetBinding.apply {

            // Reset bottom sheet values
            statusInputLayout.editText?.setText("")
            protectionInputLayout.editText?.setText("")
            selectedBuffer.text = ""
            protectionInputLayout.isEnabled = false
            bufferSeekBar.isEnabled = false
            bufferSeekBar.value = bufferSeekBar.valueFrom


            applyTv.setOnClickListener {
                // Validate the contingency
                validateContingency(mapPoint)
                dialog.dismiss()
            }

            cancelTv.setOnClickListener { dialog.dismiss() }
        }
        dialog.setCancelable(false)
        // Clear and set bottom sheet content view to layout
        if (bottomSheetBinding.root.parent != null) {
            (bottomSheetBinding.root.parent as ViewGroup).removeAllViews()
        }
        dialog.setContentView(bottomSheetBinding.root)
        // Displays bottom sheet view
        dialog.show()
    }

    private fun setUpStatusAttributes() {
        // Get the first field by name
        val statusField = featureTable.fields.find { field -> field.name.equals("Status") }
        // Get the field's domains as coded value domain
        val codedValueDomain = statusField?.domain as CodedValueDomain
        // Get the coded value domain's coded values
        val statusCodedValues = codedValueDomain.codedValues
        // Get the selected index if applicable
        val statusNames = mutableListOf<String>()
        for (statusCodedValue in statusCodedValues!!) {
            statusNames.add(statusCodedValue.name)
        }
        val items = statusNames.toTypedArray()
        val adapter = ArrayAdapter(bottomSheetBinding.root.context, R.layout.list_item, items)
        val spinner = (bottomSheetBinding.statusInputLayout.editText as? AutoCompleteTextView)
        spinner?.setAdapter(adapter)
        spinner?.setOnItemClickListener { _, _, position, _ ->
            createFeature(statusCodedValues[position])
        }
    }

    private fun setUpProtectionAttributes() {

        bottomSheetBinding.apply {
            protectionInputLayout.isEnabled = true
            bufferSeekBar.isEnabled = false
            bufferSeekBar.value = bufferSeekBar.valueFrom
            protectionInputLayout.editText?.setText("")
            selectedBuffer.text = ""
        }

        // Get the contingent value results with the feature for the protection field
        val contingentValuesResult = featureTable.getContingentValues(feature, "Protection")
        // Get contingent coded values by field group
        val protectionGroupContingentValues = contingentValuesResult.contingentValuesByFieldGroup["ProtectionFieldGroup"]
        val protectionCodedValues = mutableListOf<CodedValue>()
        protectionGroupContingentValues?.forEach { contingentValue ->
            val codedValue = (contingentValue as ContingentCodedValue).codedValue
            protectionCodedValues.add(codedValue)
        }
        val items = mutableListOf<String>()
        protectionCodedValues.forEach {
            items.add(it.name)
        }
        val adapter = ArrayAdapter(bottomSheetBinding.root.context, R.layout.list_item, items)
        val spinner = (bottomSheetBinding.protectionInputLayout.editText as? AutoCompleteTextView)
        spinner?.setAdapter(adapter)
        spinner?.setOnItemClickListener { _, _, i, _ ->
            feature.attributes["Protection"] = protectionCodedValues[i].code
            showBufferSeekbar()
        }
    }

    private fun showBufferSeekbar() {

        bottomSheetBinding.apply {
            bufferSeekBar.isEnabled = true
            selectedBuffer.text = ""
        }

        // Get the contingent value results using the feature and field
        val contingentValueResult = featureTable.getContingentValues(feature, "BufferSize")
        val bufferSizeGroupContingentValues = (contingentValueResult.contingentValuesByFieldGroup["BufferSizeFieldGroup"]?.get(0) as ContingentRangeValue)
        // Set the minimum and maximum possible buffer sizes
        val minValue = bufferSizeGroupContingentValues.minValue as Int
        val maxValue = bufferSizeGroupContingentValues.maxValue as Int
        if (maxValue > 0) {
            val bufferSeekBar = bottomSheetBinding.bufferSeekBar
            bufferSeekBar.valueFrom = minValue.toFloat()
            bufferSeekBar.valueTo = maxValue.toFloat()
            bufferSeekBar.value = bufferSeekBar.valueFrom
            bufferSeekBar.addOnChangeListener { _, value, _ ->
                feature.attributes["BufferSize"] = value.toInt()
                bottomSheetBinding.selectedBuffer.text = value.toInt().toString()
            }
        } else {
            bottomSheetBinding.apply {
                bufferSeekBar.isEnabled = false
                selectedBuffer.text = "0"
                feature.attributes["BufferSize"] = 0
            }
        }
    }

    private fun createFeature(codedValue: CodedValue) {
        // Get the contingent values definition from the feature table
        val contingentValueDefinition = featureTable.contingentValuesDefinition
        // Load the contingent values definition
        contingentValueDefinition.loadAsync()
        contingentValueDefinition.addDoneLoadingListener {
            // Create a feature from the feature table and set the initial attribute
            feature = featureTable.createFeature() as ArcGISFeature
            feature.attributes["Status"] = codedValue.code
            setUpProtectionAttributes()
        }
    }

    // Ensure that the selected values are a valid combination
    private fun validateContingency(mapPoint: Point) {
        // check if all the features have been set
        if(!this::featureTable.isInitialized){
            val message = "Input all values to add a feature to the map"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            Log.e(TAG, message)
            return
        }

        try{
            // Validate the feature's contingencies
            val contingencyViolations = featureTable.validateContingencyConstraints(feature)
            if (contingencyViolations.isEmpty()) {
                // If there are no contingency violations in the array,
                // the feature is valid and ready to add to the feature table
                // Create a symbol to represent a bird's nest
                val symbol = SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.BLACK, 11F)
                // Add the graphic to the graphics overlay
                graphicsOverlay.graphics.add(Graphic(mapPoint, symbol))
                feature.geometry = mapPoint
                val graphic = createGraphic(feature)
                // Add the feature to the feature table
                featureTable.addFeatureAsync(feature)
                featureTable.addDoneLoadingListener {
                    // Add the graphic to the graphics overlay
                    graphicsOverlay.graphics.add(graphic)
                }
            } else {
                val message = "Invalid contingent values: " + contingencyViolations.size + " violations found."
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                Log.e(TAG, message)
            }
        }catch (e : Exception){
            val message = "Invalid contingent values: " + e.message
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            Log.e(TAG, message)
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
        geoDatabase.close()
        super.onDestroy()
    }
}
