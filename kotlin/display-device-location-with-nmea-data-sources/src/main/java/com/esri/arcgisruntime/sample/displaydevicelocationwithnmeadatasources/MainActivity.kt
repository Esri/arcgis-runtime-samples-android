/* Copyright 2021 Esri
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

package com.esri.arcgisruntime.sample.displaydevicelocationwithnmeadatasources

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.location.LocationDataSource
import com.esri.arcgisruntime.location.NmeaLocationDataSource
import com.esri.arcgisruntime.location.NmeaSatelliteInfo
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.LocationDisplay
import kotlinx.android.synthetic.main.activity_main.*
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet
import kotlin.concurrent.timerTask


class MainActivity : AppCompatActivity() {

    private val TAG = MainActivity::class.java.simpleName

    // create a new NMEA location data source
    private val nmeaLocationDataSource: NmeaLocationDataSource =
        NmeaLocationDataSource(SpatialReferences.getWgs84())
    // location datasource listener
    private var locationDataSourceListener : LocationDataSource.StatusChangedListener? = null
    // create a timer to simulate a stream of NMEA data
    private var timer = Timer()

    private var nmeaSatelliteInfoList: List<NmeaSatelliteInfo> = emptyList()
    private var count = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // authentication with an API key or named user is required to access basemaps and other
        // location services
        ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY)

        // create a map with the Basemap style and set it to the MapView
        val map = ArcGISMap(BasemapStyle.ARCGIS_NAVIGATION)
        mapView.map = map

        // set a viewpoint on the map view centered on Redlands, California
        mapView.setViewpoint(
            Viewpoint(
                Point(-117.191, 34.0306, SpatialReferences.getWgs84()),
                100000.0
            )
        )

        // set the NMEA location data source onto the map view's location display
        val locationDisplay = mapView.locationDisplay
        locationDisplay.locationDataSource = nmeaLocationDataSource
        locationDisplay.autoPanMode = LocationDisplay.AutoPanMode.RECENTER

        // disable map view interaction, the location display will automatically center on the mock device location
        mapView.interactionOptions.isPanEnabled = false
        mapView.interactionOptions.isZoomEnabled = false

        playPauseFAB.setOnClickListener {
            if (!nmeaLocationDataSource.isStarted) {
                //Start location data source
                displayDeviceLocation()
                setLocationStatus(true)
            } else {
                // stop receiving and displaying location data
                nmeaLocationDataSource.stop()
                setLocationStatus(false)
            }
        }
    }

    /**
     * Sets the FAB button to "Start"/"Stop" based on the argument [isShowingLocation]
     */
    private fun setLocationStatus(isShowingLocation: Boolean) = if (isShowingLocation){
        playPauseFAB.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_round_pause_24))
    }else{
        playPauseFAB.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_round_play_arrow_24))
    }

    /**
     * Initializes the location data source, reads the mock data NMEA sentences, and displays location updates from that file
     * on the location display. Data is pushed to the data source using a timeline to simulate live updates, as they would
     * appear if using real-time data from a GPS dongle.
     */
    private fun displayDeviceLocation() {
        val simulatedNmeaDataFile = File(getExternalFilesDir(null)?.path + "/Redlands.nmea")
        if (simulatedNmeaDataFile.exists()) {
            try {
                // read the nmea file contents using a buffered reader and store the mock data sentences in a list
                val bufferedReader = BufferedReader(FileReader(simulatedNmeaDataFile.path))
                // add carriage return for NMEA location data source parser
                val nmeaSentences: MutableList<String> = mutableListOf()
                var line = bufferedReader.readLine()
                while( line != null){
                    nmeaSentences.add(line + "\n")
                    line = bufferedReader.readLine()
                }
                bufferedReader.close()

                locationDataSourceListener = LocationDataSource.StatusChangedListener{
                    if(it.status == LocationDataSource.Status.STARTED){
                        // add a satellite changed listener to the NMEA location data source and display satellite information on the app
                        setupSatelliteChangedListener()

                        timer = Timer()
                        // push the mock data NMEA sentences into the data source every 250 ms
                        timer.schedule(timerTask {
                            nmeaLocationDataSource.pushData(nmeaSentences[count++].toByteArray(StandardCharsets.UTF_8))
                            // reset the count after the last data point is reached
                            if(count == nmeaSentences.size)
                                count = 0
                        }, 250, 250)

                        setLocationStatus(true)
                    }
                    if(it.status == LocationDataSource.Status.STOPPED){
                        timer.cancel()
                        nmeaLocationDataSource.removeStatusChangedListener(locationDataSourceListener)
                        setLocationStatus(false)
                    }
                }

                // initialize the location data source and prepare to begin receiving location updates when data is pushed. As
                // updates are received, they will be displayed on the map
                nmeaLocationDataSource.addStatusChangedListener(locationDataSourceListener)
                nmeaLocationDataSource.addLocationChangedListener {
                    //Convert from Meters to Foot
                    val horizontalAccuracy = it.location.horizontalAccuracy * 3.28084
                    val verticalAccuracy = it.location.verticalAccuracy * 3.28084
                    accuracyTV.text = "Accuracy- Horizontal: %.1fft, Vertical: %.1ft".format(horizontalAccuracy,verticalAccuracy)
                }
                nmeaLocationDataSource.startAsync()


            } catch (e: Exception) {
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                Log.e(TAG, e.message.toString())
            }
        } else {
            Toast.makeText(this, "NMEA File not found", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Obtains NMEA satellite information from the NMEA location data source, and displays satellite information on the app.
     */
    private fun setupSatelliteChangedListener() {

        val uniqueSatelliteIDs: HashSet<Int> = hashSetOf()

        nmeaLocationDataSource.addSatellitesChangedListener {
            // get satellite information from the NMEA location data source every time the satellites change
            nmeaSatelliteInfoList = it.satelliteInfos
            // set the text of the satellite count label
            satelliteCountTV.text = "Satellite count: " + nmeaSatelliteInfoList.size

            for (satInfo in nmeaSatelliteInfoList) {
                // collect unique satellite ids
                uniqueSatelliteIDs.add(satInfo.id)
                // sort the ids numerically
                val sortedIds: MutableList<Int> = ArrayList(uniqueSatelliteIDs)
                sortedIds.sort()
                // display the satellite system and id information
                systemTypeTV.text = "System: " + satInfo.system
                satelliteIDsTV.text = "Satellite IDs: $sortedIds"
            }
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
        nmeaLocationDataSource.stop()
        mapView.dispose()
        super.onDestroy()
    }
}
