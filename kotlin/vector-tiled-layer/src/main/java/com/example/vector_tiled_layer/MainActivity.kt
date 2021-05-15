package com.example.vector_tiled_layer

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.layers.ArcGISVectorTiledLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.portal.Portal
import com.esri.arcgisruntime.portal.PortalItem
import com.example.vector_tiled_layer.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.activity_main.fab

//[DocRef: END]

class MainActivity : AppCompatActivity() {

    // The item ID of the currently showing layer.
    private var currentItemID: String? = null
    /// The item position of the currently showing layer.
    private var checkedItemPos = 0

    // A list of portal item IDs for the online layers.
    private var onlineItemIds: Array<String> = arrayOf(
        "1349bfa0ed08485d8a92c442a3850b06",
        "bd8ac41667014d98b933e97713ba8377",
        "02f85ec376084c508b9c8e5a311724fa",
        "1bf0cc4a4380468fbbff107e100f65a5")

    // A list of portal item IDs for the layers which custom style is applied from local resources.
    private var offlineItemIds: Array<String> = arrayOf(
        // A vector tiled layer created by the local VTPK and light custom style.
        "e01262ef2a4f4d91897d9bbd3a9b1075",
        // A vector tiled layer created by the local VTPK and dark custom style.
        "ce8a34e5d4ca4fa193a097511daa8855")

    private val vectorTiledLayers = arrayOf<ArcGISVectorTiledLayer>()


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

/*        //[DocRef: Name=Create map-Android, Category=Get started, Topic=Develop your first map app with Kotlin]
        // create a map with the BasemapType topographic
        val map = ArcGISMap(BasemapStyle.ARCGIS_NAVIGATION_NIGHT)
        //[DocRef: END]

        //[DocRef: Name=Set map-Android, Category=Get started, Topic=Develop your first map app with Kotlin]
        // set the map to be displayed in the layout's MapView
        mapView.map = map
        //[DocRef: END]

        mapView.setViewpoint(Viewpoint(34.056295, -117.195800, 10000.0))*/

/*        val exportVectorTilesTask = ExportVectorTilesTask("")
        val exportVectorTilesJob = exportVectorTilesTask.exportStyleResourceCache(cacheDir.path)
        exportVectorTilesJob.addJobDoneListener {
            exportVectorTilesJob.result
        }*/

        fab.setOnClickListener {
            showDialogForLayer();
        }

        // Set the currentItemID to default layer.
        currentItemID = onlineItemIds[0]
        showSelectedItem(currentItemID!!)


    }

    private fun showDialogForLayer() {
        // setup the alert builder
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose Vector Styled Layer")

        // add a radio button list
        val animals = arrayOf("Default", "Style 1", "Style 2", "Style 3", "Offline custom style: Light", "Offline custom style: Dark")

        builder.setSingleChoiceItems(animals, checkedItemPos) { dialog, which ->
            // user checked an item
            when (which) {
                0-> currentItemID = onlineItemIds[0]
                1-> currentItemID = onlineItemIds[1]
                2-> currentItemID = onlineItemIds[2]
                3-> currentItemID = onlineItemIds[3]
                4-> currentItemID = offlineItemIds[0]
                5-> currentItemID = offlineItemIds[1]
            }

            checkedItemPos = which
            currentItemID?.let { showSelectedItem(it) }
        }

        builder.setPositiveButton("Select", null)
        builder.setNegativeButton("Cancel", null)

        // create and show the alert dialog
        val dialog = builder.create()
        dialog.show()

    }

    private fun showSelectedItem(itemID: String){
        currentItemID = itemID;

        //val vectorTiledLayer = ArcGISVectorTiledLayer(getPortalURL(itemID))
        if(onlineItemIds.contains(itemID)){
            val portalItem = PortalItem(Portal("https://www.arcgis.com"),itemID)
            val vectorTiledLayer = ArcGISVectorTiledLayer(portalItem)
            val viewpoint = Viewpoint(Point(1990591.559979,794036.007991, SpatialReferences.getWebMercator()),100000000.0)
            setMap(vectorTiledLayer,viewpoint)
        }else{
            val portalItem = PortalItem(Portal("https://www.arcgis.com"),itemID)
            val vectorTiledLayer = ArcGISVectorTiledLayer(portalItem)
            val viewpoint = Viewpoint(Point(-100.01766, 37.76528, SpatialReferences.getWgs84()),10000.0)
            setMap(vectorTiledLayer,viewpoint)
        }


    }

    /**
     * Set the map using the layer and the viewpoint.
     */
    private fun setMap(layer: ArcGISVectorTiledLayer, viewpoint: Viewpoint){
        //Reset the map to release resources
        mapView.map = null;
        // Assign a new map created from the base layer.
        mapView.map = ArcGISMap(Basemap(layer))
        //Set viewpoint without animation.
        mapView.setViewpoint(viewpoint)

    }

    //[DocRef: Name=Pause and resume-Android, Category=Get started, Topic=Develop your first map app with Kotlin]
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
    //[DocRef: END]
}
