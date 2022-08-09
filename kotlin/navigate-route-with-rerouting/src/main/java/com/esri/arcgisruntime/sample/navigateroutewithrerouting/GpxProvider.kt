package com.esri.arcgisruntime.sample.navigateroutewithrerouting

import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.location.LocationDataSource

class GpxProvider(gpxFilePath: String) : LocationDataSource() {

    private val gpsPoints = mutableListOf<Point>()

    init {
        // read the GPX data.
        readLocations(gpxFilePath)
        // create a timer for updating the location.

    }

    private fun readLocations(gpxFilePath: String) {

    }

    override fun onStart() {
        TODO("Not yet implemented")
    }

    override fun onStop() {
        TODO("Not yet implemented")
    }

}