/*
 *  Copyright 2020 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.esri.arcgisruntime.sample.navigateroute

import com.esri.arcgisruntime.geometry.AngularUnit
import com.esri.arcgisruntime.geometry.AngularUnitId
import com.esri.arcgisruntime.geometry.GeodeticCurveType
import com.esri.arcgisruntime.geometry.GeometryEngine
import com.esri.arcgisruntime.geometry.LinearUnit
import com.esri.arcgisruntime.geometry.LinearUnitId
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.Polyline
import com.esri.arcgisruntime.location.LocationDataSource
import java.util.Timer
import java.util.TimerTask

/**
 * A LocationDataSource that simulates movement along the specified route. Upon start of the
 * SimulatedLocationDataSource, a timer is started, which updates the location along the route at fixed
 * intervals.
 */
internal class SimulatedLocationDataSource(private val route: Polyline) :
  LocationDataSource() {
  private var currentLocation: Point? = null
  private val timer: Timer by lazy {
    Timer("SimulatedLocationDataSource Timer", false)
  }
  private var distance = 0.0
  override fun onStop() {
    timer.cancel()
  }

  companion object {
    private const val distanceInterval = .00025
  }

  override fun onStart() {
    // start at the beginning of the route
    currentLocation = route.parts[0].startPoint
    updateLocation(Location(currentLocation))
    timer.apply {
      scheduleAtFixedRate(object : TimerTask() {
        override fun run() {
          // get a reference to the previous point
          val previousPoint = currentLocation
          // update current location by moving [distanceInterval] meters along the route
          currentLocation = GeometryEngine.createPointAlong(route, distance)
          // get the geodetic distance result between the two points
          val distanceResult = GeometryEngine.distanceGeodetic(
            previousPoint,
            currentLocation,
            LinearUnit(LinearUnitId.METERS),
            AngularUnit(AngularUnitId.DEGREES),
            GeodeticCurveType.GEODESIC
          )
          // update the location with the current location and use the geodetic distance result to get the azimuth
          updateLocation(
            Location(
              currentLocation,
              1.0,
              1.0,
              distanceResult.azimuth1,
              false
            )
          )
          // increment the distance
          distance += distanceInterval
        }
      }, 0, 1000)
      // this method must be called by the subclass once the location data source has finished its starting process
      onStartCompleted(null)
    }
  }
}