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
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.data.ArcGISFeature
import com.esri.arcgisruntime.data.ArcGISFeatureTable
import com.esri.arcgisruntime.data.CodedValue
import com.esri.arcgisruntime.data.CodedValueDomain
import com.esri.arcgisruntime.data.ContingentCodedValue
import com.esri.arcgisruntime.data.ContingentRangeValue
import com.esri.arcgisruntime.data.Feature
import com.esri.arcgisruntime.data.Geodatabase
import com.esri.arcgisruntime.data.QueryParameters
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

    // graphic overlay instance to add the feature graphic to the map
    private val graphicsOverlay = GraphicsOverlay()

    // mobile database containing offline feature data. GeoDatabase is closed on app exit
    private var geoDatabase: Geodatabase? = null

    // instance of the contingent feature to be added to the map
    private var feature: ArcGISFeature? = null

    // instance of the feature table retrieved from the GeoDatabase, updates when new feature is added
    private var featureTable: ArcGISFeatureTable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityMainBinding.root)

        // authentication with an API key or named user is required to access basemaps and other location services
        ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY)

        // create a temporary directory to use the geodatabase file
        createGeodatabaseCacheDirectory()

        // use the offline vector tiled layer as a basemap
        val fillmoreVectorTiledLayer = ArcGISVectorTiledLayer(
            getExternalFilesDir(null)?.path + "/FillmoreTopographicMap.vtpk"
        )
        mapView.apply {
            // set the basemap layer and the graphic overlay to the MapView
            map = ArcGISMap(Basemap(fillmoreVectorTiledLayer))
            graphicsOverlays.add(graphicsOverlay)

            // add a listener to the MapView to detect when a user has performed a single tap to add a new feature
            onTouchListener = object : DefaultMapViewOnTouchListener(this@MainActivity, this) {
                override fun onSingleTapConfirmed(motionEvent: MotionEvent?): Boolean {
                    motionEvent?.let { event ->
                        // create a point from where the user clicked
                        android.graphics.Point(event.x.toInt(), event.y.toInt()).let { point ->
                            // create a map point and add a new feature to the service feature table
                            openBottomSheetView(screenToLocation(point))
                        }
                    }
                    performClick()
                    return super.onSingleTapConfirmed(motionEvent)
                }
            }
        }

        // retrieve and load the offline mobile GeoDatabase file from the cache directory
        geoDatabase = Geodatabase(cacheDir.path + "/ContingentValuesBirdNests.geodatabase")
        geoDatabase?.loadAsync()
        geoDatabase?.addDoneLoadingListener {
            if (geoDatabase?.loadStatus == LoadStatus.LOADED) {
                (geoDatabase?.geodatabaseFeatureTables?.first() as? ArcGISFeatureTable)?.let { featureTable ->
                    this.featureTable = featureTable
                    featureTable.loadAsync()
                    featureTable.addDoneLoadingListener {
                        // create and load the feature layer from the feature table
                        val featureLayer = FeatureLayer(featureTable)
                        // add the feature layer to the map
                        mapView.map.operationalLayers.add(featureLayer)
                        // set the map's viewpoint to the feature layer's full extent
                        val extent = featureLayer.fullExtent
                        mapView.setViewpoint(Viewpoint(extent))
                        // add buffer graphics for the feature layer
                        queryFeatures()
                    }
                }

            } else {
                val error = "Error loading GeoDatabase: " + geoDatabase?.loadError?.message
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
        // clear cache directory
        File(cacheDir.path).deleteRecursively()
        // copy over the original Geodatabase file to be used in the temp cache directory
        File(getExternalFilesDir(null)?.path + "/ContingentValuesBirdNests.geodatabase").copyTo(
            File(cacheDir.path + "/ContingentValuesBirdNests.geodatabase")
        )
    }

    /**
     * Create buffer graphics for the features and adds the graphics to
     * the [graphicsOverlay]
     */
    private fun queryFeatures() {
        // create buffer graphics for the features
        val queryParameters = QueryParameters().apply {
            // set the where clause to filter for buffer sizes greater than 0
            whereClause = "BufferSize > 0"
        }
        // query the features using the queryParameters on the featureTable
        val queryFeaturesFuture = featureTable?.queryFeaturesAsync(queryParameters)
        queryFeaturesFuture?.addDoneListener {
            try {
                // clear the existing graphics
                graphicsOverlay.graphics.clear()
                // call get on the future to get the result
                val resultIterator = queryFeaturesFuture.get().iterator()
                if (resultIterator.hasNext()) {
                    // create an array of graphics to add to the graphics overlay
                    val graphics = mutableListOf<Graphic>()
                    // create graphic for each query result by calling createGraphic(feature)
                    //while (resultIterator.hasNext()) graphics.add(createGraphic(resultIterator.next()))
                    queryFeaturesFuture.get().iterator().forEach {
                        graphics.add(createGraphic(it))
                    }
                    // add the graphics to the graphics overlay
                    graphicsOverlay.graphics.addAll(graphics)
                } else {
                    val message = "No features found with BufferSize > 0"
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                    Log.d(TAG, message)
                }
            } catch (e: Exception) {
                val message = "Error querying features: " + e.message
                Log.e(TAG, message)
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Create a graphic for the given [feature] and returns a Graphic with the features attributes
     */
    private fun createGraphic(feature: Feature): Graphic {
        // get the feature's buffer size
        val bufferSize = feature.attributes["BufferSize"] as Int
        // get a polygon using the feature's buffer size and geometry
        val polygon = GeometryEngine.buffer(feature.geometry, bufferSize.toDouble())
        // create the outline for the buffers
        val lineSymbol = SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLACK, 2F)
        // create the buffer symbol
        val bufferSymbol = SimpleFillSymbol(
            SimpleFillSymbol.Style.FORWARD_DIAGONAL, Color.RED, lineSymbol
        )
        // create an a graphic and add it to the array.
        return Graphic(polygon, bufferSymbol)
    }

    /**
     * Open BottomSheetDialog view to handle contingent value interaction.
     * Once the contingent values have been set and the apply button is clicked,
     * the function will call validateContingency() to add the feature to the map.
     */
    private fun openBottomSheetView(mapPoint: Point) {
        // creates a new BottomSheetDialog
        val dialog = BottomSheetDialog(this)
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED

        // set up the first content value attribute
        setUpStatusAttributes()

        bottomSheetBinding.apply {
            // reset bottom sheet values, this is needed to showcase contingent values behavior
            statusInputLayout.editText?.setText("")
            protectionInputLayout.editText?.setText("")
            selectedBuffer.text = ""
            protectionInputLayout.isEnabled = false
            bufferSeekBar.isEnabled = false
            bufferSeekBar.value = bufferSeekBar.valueFrom

            // set apply button to validate and apply contingency feature on map
            applyTv.setOnClickListener {
                // check if the contingent features set is valid and set it to the map if valid
                validateContingency(mapPoint)
                dialog.dismiss()
            }
            // dismiss on cancel clicked
            cancelTv.setOnClickListener { dialog.dismiss() }
        }
        // clear and set bottom sheet content view to layout, to be able to set the content view on each bottom sheet draw
        if (bottomSheetBinding.root.parent != null) {
            (bottomSheetBinding.root.parent as ViewGroup).removeAllViews()
        }
        // set the content view to the root of the binding layout
        dialog.setContentView(bottomSheetBinding.root)
        // display the bottom sheet view
        dialog.show()
    }

    /**
     *  Retrieve the status fields, add the fields to a ContingentValueDomain, and set the values to the spinner
     *  When status attribute selected, createFeature() is called.
     */
    private fun setUpStatusAttributes() {
        // get the first field by name
        val statusField = featureTable?.fields?.find { field -> field.name.equals("Status") }
        // get the field's domains as coded value domain
        val codedValueDomain = statusField?.domain as CodedValueDomain
        // get the coded value domain's coded values
        val statusCodedValues = codedValueDomain.codedValues
        // get the selected index if applicable
        val statusNames = mutableListOf<String>()
        statusCodedValues.forEach {
            statusNames.add(it.name)
        }
        // get the items to be added to the spinner adapter
        val adapter = ArrayAdapter(bottomSheetBinding.root.context, R.layout.list_item, statusNames)
        (bottomSheetBinding.statusInputLayout.editText as? AutoCompleteTextView)?.apply {
            setAdapter(adapter)
            setOnItemClickListener { _, _, position, _ ->
                // get the CodedValue of the item selected, and create a feature needed for feature attributes
                createFeature(statusCodedValues[position])
            }
        }
    }

    /**
     *  Retrieve the protection attribute fields, add the fields to a ContingentCodedValue, and set the values to the spinner
     *  When status attribute selected, showBufferSeekbar() is called.
     */
    private fun setUpProtectionAttributes() {
        // set the bottom sheet view to enable the Protection attribute, and disable input elsewhere
        bottomSheetBinding.apply {
            protectionInputLayout.isEnabled = true
            bufferSeekBar.isEnabled = false
            bufferSeekBar.value = bufferSeekBar.valueFrom
            protectionInputLayout.editText?.setText("")
            selectedBuffer.text = ""
        }

        // get the contingent value results with the feature for the protection field
        val contingentValuesResult = featureTable?.getContingentValues(feature, "Protection")
        if (contingentValuesResult != null) {
            // get contingent coded values by field group
            // convert the list of ContingentValues to a list of CodedValue
            val protectionCodedValues = mutableListOf<CodedValue>()
            contingentValuesResult.contingentValuesByFieldGroup["ProtectionFieldGroup"]?.forEach { contingentValue ->
                protectionCodedValues.add((contingentValue as ContingentCodedValue).codedValue)
            }
            // set the items to be added to the spinner adapter
            val adapter = ArrayAdapter(
                bottomSheetBinding.root.context,
                R.layout.list_item,
                protectionCodedValues.map { it.name })
            (bottomSheetBinding.protectionInputLayout.editText as? AutoCompleteTextView)?.apply {
                setAdapter(adapter)
                setOnItemClickListener { _, _, position, _ ->
                    // set the protection CodedValue of the item selected, and then enable buffer seekbar
                    feature?.attributes?.set("Protection", protectionCodedValues[position].code)
                    showBufferSeekbar()
                }
            }
        } else {
            val message = "Error loading ContingentValuesResult from the FeatureTable"
            Log.e(TAG, message)
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     *  Retrieve the buffer attribute fields, add the fields to a ContingentRangeValue,
     *  and set the values to a SeekBar
     */
    private fun showBufferSeekbar() {
        // set the bottom sheet view to enable the buffer attribute
        bottomSheetBinding.apply {
            bufferSeekBar.isEnabled = true
            selectedBuffer.text = ""
        }

        // get the contingent value results using the feature and field
        val contingentValueResult = featureTable?.getContingentValues(feature, "BufferSize")
        val bufferSizeGroupContingentValues =
            (contingentValueResult?.contingentValuesByFieldGroup?.get("BufferSizeFieldGroup")?.get(0) as ContingentRangeValue)
        // set the minimum and maximum possible buffer sizes
        val minValue = bufferSizeGroupContingentValues.minValue as Int
        val maxValue = bufferSizeGroupContingentValues.maxValue as Int
        // check if there can be a max value, if not disable SeekBar & set value to attribute size to 0
        if (maxValue > 0) {
            // get SeekBar instance from the binding layout
            bottomSheetBinding.bufferSeekBar.apply {
                // set the min, max and current value of the SeekBar
                valueFrom = minValue.toFloat()
                valueTo = maxValue.toFloat()
                value = valueFrom
                // set the initial attribute and the text to the min of the ContingentRangeValue
                feature?.attributes?.set("BufferSize", value.toInt())
                bottomSheetBinding.selectedBuffer.text = value.toInt().toString()
                // set the change listener to update the attribute value and the displayed value to the SeekBar position
                addOnChangeListener { _, value, _ ->
                    feature?.attributes?.set("BufferSize", value.toInt())
                    bottomSheetBinding.selectedBuffer.text = value.toInt().toString()
                }
            }
        } else {
            // max value is 0, so disable seekbar and update the attribute value accordingly
            bottomSheetBinding.apply {
                bufferSeekBar.isEnabled = false
                selectedBuffer.text = "0"
            }
            feature?.attributes?.set("BufferSize", 0)
        }
    }

    /**
     * Set up the [feature] using the status attribute's coded value
     * by loading the [featureTable]'s Contingent Value Definition.
     * This function calls setUpProtectionAttributes() once the [feature] has been set
     */
    private fun createFeature(codedValue: CodedValue) {
        // get the contingent values definition from the feature table
        val contingentValueDefinition = featureTable?.contingentValuesDefinition
        if (contingentValueDefinition != null) {
            // load the contingent values definition
            contingentValueDefinition.loadAsync()
            contingentValueDefinition.addDoneLoadingListener {
                // create a feature from the feature table and set the initial attribute
                feature = featureTable?.createFeature() as ArcGISFeature
                feature?.attributes?.set("Status", codedValue.code)
                setUpProtectionAttributes()
            }
        } else {
            val message = "Error retrieving ContingentValuesDefinition from the FeatureTable"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            Log.e(TAG, message)
        }
    }

    /**
     * Ensure that the selected values are a valid combination.
     * If contingencies are valid, then display [feature] on the [mapPoint]
     */
    private fun validateContingency(mapPoint: Point) {
        // check if all the features have been set
        if (featureTable == null) {
            val message = "Input all values to add a feature to the map"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            Log.e(TAG, message)
            return
        }

        try {
            // validate the feature's contingencies
            val contingencyViolations = featureTable?.validateContingencyConstraints(feature)
            if (contingencyViolations?.isEmpty() == true) {
                // if there are no contingency violations in the array,
                // the feature is valid and ready to add to the feature table
                // create a symbol to represent a bird's nest
                val symbol = SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.BLACK, 11F)
                // add the graphic to the graphics overlay
                graphicsOverlay.graphics.add(Graphic(mapPoint, symbol))
                feature?.geometry = mapPoint
                val graphic = feature?.let { createGraphic(it) }
                // add the feature to the feature table
                featureTable?.addFeatureAsync(feature)
                featureTable?.addDoneLoadingListener {
                    // add the graphic to the graphics overlay
                    graphicsOverlay.graphics.add(graphic)
                }
            } else {
                val message = "Invalid contingent values: " + (contingencyViolations?.size?: 0) + " violations found."
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                Log.e(TAG, message)
            }
        } catch (e: Exception) {
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
        // closing the GeoDatabase will commit the transactions made to the temporary ".geodatabase" file
        // then removes the temporary ".geodatabase-wal" and ".geodatabase-shm" files from the cache dir
        geoDatabase?.close()
        super.onDestroy()
    }
}
