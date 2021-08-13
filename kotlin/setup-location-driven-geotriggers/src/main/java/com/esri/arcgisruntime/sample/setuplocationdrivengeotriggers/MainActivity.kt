package com.esri.arcgisruntime.sample.setuplocationdrivengeotriggers

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.arcade.ArcadeExpression
import com.esri.arcgisruntime.concurrent.ListenableFuture
import com.esri.arcgisruntime.data.ArcGISFeature
import com.esri.arcgisruntime.data.Attachment
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.geometry.Polyline
import com.esri.arcgisruntime.geotriggers.*
import com.esri.arcgisruntime.location.SimulatedLocationDataSource
import com.esri.arcgisruntime.location.SimulationParameters
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.view.LocationDisplay
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.portal.Portal
import com.esri.arcgisruntime.portal.PortalItem
import com.esri.arcgisruntime.sample.setuplocationdrivengeotriggers.databinding.ActivityMainBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity() {

    private var currentSections: MutableList<GardenSection> = ArrayList()

    private var poiList: MutableList<GardenSection> = ArrayList()

    private var sectionsVisited: HashMap<String, GardenSection> = HashMap()

    private val POI_GEOTRIGGER = "POI Geotrigger"
    private val SECTION_GEOTRIGGER = "Section Geotrigger"

    // make monitors properties to prevent garbage collection
    private lateinit var sectionGeotriggerMonitor: GeotriggerMonitor
    private lateinit var poiGeotriggerMonitor: GeotriggerMonitor

    private lateinit var attachmentsFuture: ListenableFuture<MutableList<Attachment>>

    private lateinit var listAdapter: ListAdapter

    private val activityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val mapView: MapView by lazy {
        activityMainBinding.mapView
    }

    private val sectionButton: Button by lazy {
        activityMainBinding.currentSectionButton
    }

    private val poiListView: ListView by lazy {
        activityMainBinding.poiList
    }

    private val playPauseFAB: FloatingActionButton by lazy {
        activityMainBinding.playPauseFAB
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityMainBinding.root)

        // authentication with an API key or named user is required to access basemaps and other
        // location services
        ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY)

        val portal = Portal("https://www.arcgis.com", true)
        // This sample uses a web map with a predefined tile basemap, feature styles, and labels
        val map = ArcGISMap(PortalItem(portal, "6ab0e91dc39e478cae4f408e1a36a308"))
        mapView.map = map
        // Instantiate the service feature tables to later create GeotriggerMonitors for
        val gardenSections =
            ServiceFeatureTable(PortalItem(portal, "1ba816341ea04243832136379b8951d9"), 0)
        val gardenPOIs =
            ServiceFeatureTable(PortalItem(portal, "7c6280c290c34ae8aeb6b5c4ec841167"), 0)


        // Start the simulated location display data source and create a LocationGeotriggerFeed that will be required in createGeotriggerMonitor()
        val geotriggerFeed = initializeSimulatedLocationDisplay()

        // Create geotriggers for each of the service feature tables
        sectionGeotriggerMonitor =
            createGeotriggerMonitor(gardenSections, 0.0, SECTION_GEOTRIGGER, geotriggerFeed)
        poiGeotriggerMonitor =
            createGeotriggerMonitor(gardenPOIs, 10.0, POI_GEOTRIGGER, geotriggerFeed)

        listAdapter = ListAdapter(this, poiList)
        poiListView.adapter = listAdapter
    }

    private fun initializeSimulatedLocationDisplay(): LocationGeotriggerFeed {
        val simulatedLocationDataSource = SimulatedLocationDataSource()

        // Create SimulationParameters starting at the current time, a velocity of 10 m/s, and a horizontal and vertical accuracy of 0.0
        val simulationParameters = SimulationParameters(Calendar.getInstance(), 3.0, 0.0, 0.0)

        // Use the polyline as defined above or from this AGOL GeoJSON to define the path. retrieved
        // from https://https://arcgisruntime.maps.arcgis.com/home/item.html?id=2a346cf1668d4564b8413382ae98a956
        simulatedLocationDataSource.setLocations(
            Polyline.fromJson(getString(R.string.polyline_json)) as Polyline,
            simulationParameters
        )

        mapView.locationDisplay.apply {
            locationDataSource = simulatedLocationDataSource
            autoPanMode = LocationDisplay.AutoPanMode.RECENTER
            initialZoomScale = 1000.0
        }.startAsync()

        simulatedLocationDataSource.startAsync()

        // LocationGeotriggerFeed will be used in instantiating a FenceGeotrigger in createGeotriggerMonitor()
        return LocationGeotriggerFeed(simulatedLocationDataSource)
    }

    /**
     *  This function is used to create two geotrigger monitors in this sample - one for the
     *  sections and one for the points ot interest The parameters for this function are the only
     *  differences between the two geotrigger monitors
     */
    private fun createGeotriggerMonitor(
        serviceFeatureTable: ServiceFeatureTable,
        bufferSize: Double,
        geotriggerName: String,
        geotriggerFeed: LocationGeotriggerFeed
    ): GeotriggerMonitor {

        // Initialize FeatureFenceParameters with the service feature table and a buffer of 0 meters to display the exact garden section the user has entered
        val featureFenceParameters = FeatureFenceParameters(serviceFeatureTable, bufferSize)
        val fenceGeotrigger = FenceGeotrigger(
            geotriggerFeed,
            FenceRuleType.ENTER_OR_EXIT,
            featureFenceParameters,
            ArcadeExpression("\$fenceFeature.name"),
            geotriggerName
        )
        val geotriggerMonitor = GeotriggerMonitor(fenceGeotrigger)
        geotriggerMonitor.addGeotriggerMonitorNotificationEventListener {
            handleGeotriggerNotification(it.geotriggerNotificationInfo)
        }

        playPauseFAB.setOnClickListener {
            geotriggerFeed.apply {
                if (locationDataSource.isStarted) {
                    locationDataSource.stop()
                    Toast.makeText(this@MainActivity, "Stopped Simulation", Toast.LENGTH_SHORT)
                        .show()
                    playPauseFAB.setImageResource(R.drawable.ic_round_play_arrow_24)
                } else {
                    locationDataSource.startAsync()
                    mapView.locationDisplay.autoPanMode = LocationDisplay.AutoPanMode.RECENTER
                    Toast.makeText(this@MainActivity, "Resumed Simulation", Toast.LENGTH_SHORT)
                        .show()
                    playPauseFAB.setImageResource(R.drawable.ic_round_pause_24)
                }
            }
        }

        // Start must be explicitly called. It is called after the signal connection is defined to avoid a race condition in Qt.
        geotriggerMonitor.startAsync()

        return geotriggerMonitor
    }

    private fun handleGeotriggerNotification(geotriggerNotificationInfo: GeotriggerNotificationInfo) {

        // FenceGeotriggerNotificationInfo provides access to the feature that triggered the notification
        val fenceGeotriggerNotificationInfo =
            geotriggerNotificationInfo as FenceGeotriggerNotificationInfo

        // name of the fence feature
        val fenceFeatureName = fenceGeotriggerNotificationInfo.message

        if (fenceGeotriggerNotificationInfo.fenceNotificationType == FenceNotificationType.ENTERED) {
            // If the user enters a given geofence, add the feature's information to the UI and save the feature for querying.
            addFeatureInformation(fenceFeatureName, fenceGeotriggerNotificationInfo)
        } else if (fenceGeotriggerNotificationInfo.fenceNotificationType == FenceNotificationType.EXITED) {
            removeFeatureInformation(
                fenceFeatureName,
                geotriggerNotificationInfo.geotriggerMonitor.geotrigger.name
            )
        }
    }


    private fun addFeatureInformation(
        fenceFeatureName: String,
        fenceGeotriggerNotificationInfo: FenceGeotriggerNotificationInfo
    ) {

        // Recenter the camera on the user if need be
        mapView.locationDisplay.autoPanMode = LocationDisplay.AutoPanMode.RECENTER

        // if the section has not been visited before
        if (!sectionsVisited.containsKey(fenceFeatureName)) {

            // the fence GeoElement as an ArcGISFeature
            val fenceFeature = fenceGeotriggerNotificationInfo.fenceGeoElement as ArcGISFeature

            // get the description from the feature's attributes
            val description = fenceFeature.attributes?.getValue("description").toString()

            // fetch the fence feature's attachments
            attachmentsFuture = fenceFeature.fetchAttachmentsAsync()
            // listen for fetch attachments to complete
            attachmentsFuture.addDoneListener {
                // get the feature attachments
                val attachments = attachmentsFuture.get()
                if (attachments.isNotEmpty()) {
                    // get the first (and only) attachment for the feature, which is an image
                    val imageAttachment = attachments.first()
                    // fetch the attachment's data
                    val attachmentDataFuture = imageAttachment.fetchDataAsync()
                    // listen for fetch data to complete
                    attachmentDataFuture.addDoneListener {
                        // get the attachments data as an input stream
                        val attachmentInputStream = attachmentDataFuture.get()
                        // save the input stream to the device and get a reference to it's URI
                        val attachmentImageURI =
                            saveToInternalStorage(fenceFeatureName, attachmentInputStream)
                        // create a new garden section data object and add it to the hash map of visited sections
                        sectionsVisited[fenceFeatureName] =
                            GardenSection(
                                fenceFeature,
                                fenceFeatureName,
                                description,
                                attachmentImageURI
                            )
                        populateUI(
                            fenceFeatureName,
                            sectionsVisited[fenceFeatureName]!!,
                            fenceGeotriggerNotificationInfo.geotriggerMonitor.geotrigger.name
                        )
                    }
                }
            }
            // if the section has been visited before, get it from the hash map
        } else {
            // this garden section has already been visited, show the information again
            populateUI(
                fenceFeatureName,
                sectionsVisited[fenceFeatureName]!!,
                fenceGeotriggerNotificationInfo.geotriggerMonitor.geotrigger.name
            )
        }
    }

    private fun removeFeatureInformation(fenceFeatureName: String, geotriggerType: String) {
        if (geotriggerType == SECTION_GEOTRIGGER) {
            sectionButton.text = "N/A"
        } else {
            poiList.remove(sectionsVisited[fenceFeatureName])
            listAdapter.notifyDataSetChanged()
            if (poiList.size == 0) {
                activityMainBinding.listAvailable.visibility = View.VISIBLE
            }
        }
        currentSections.remove(sectionsVisited[fenceFeatureName])
        //gardenSectionAdapter.notifyDataSetChanged()
    }


    private val tempList: MutableList<String> = ArrayList()
    private fun populateUI(
        fenceFeatureName: String,
        gardenSection: GardenSection,
        geotriggerType: String
    ) {

        if (geotriggerType == SECTION_GEOTRIGGER) {
            sectionButton.text = gardenSection.title
            sectionButton.setOnClickListener {
                if (gardenSection.title == sectionButton.text) {
/*                    val alertDialog: AlertDialog.Builder = AlertDialog.Builder(this)
                    alertDialog.setTitle(gardenSection.title)
                    alertDialog.setMessage(gardenSection.description)

                    // Displays the where clause dialog
                    val alert: AlertDialog = alertDialog.create()
                    alert.show()*/
                    GardenDescriptionFragment(gardenSection).show(
                        supportFragmentManager,
                        "GardenDescriptionFragment"
                    )
                }
            }
        } else {
            poiList.add(gardenSection)
            listAdapter.notifyDataSetChanged()
            activityMainBinding.listAvailable.visibility = View.GONE
        }

        //currentSections.add(gardenSection)
        //gardenSectionAdapter.notifyDataSetChanged()
    }

    private fun saveToInternalStorage(name: String, imageInputStream: InputStream): String {

        val imagePath = File(cacheDir, name.filter { !it.isWhitespace() })
        var fileOutputStream: FileOutputStream? = null
        try {
            fileOutputStream = FileOutputStream(imagePath)
            // Use the compress method on the BitMap object to write image to the OutputStream
            val bitmapImage = BitmapFactory.decodeStream(imageInputStream)
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                fileOutputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return imagePath.absolutePath
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
