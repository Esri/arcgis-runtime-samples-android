/*
 *  Copyright 2019 Esri
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

package com.esri.arcgisruntime.sample.navigateroute;

import java.util.Timer;
import java.util.TimerTask;

import com.esri.arcgisruntime.geometry.AngularUnit;
import com.esri.arcgisruntime.geometry.AngularUnitId;
import com.esri.arcgisruntime.geometry.GeodeticCurveType;
import com.esri.arcgisruntime.geometry.GeodeticDistanceResult;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.LinearUnit;
import com.esri.arcgisruntime.geometry.LinearUnitId;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.Polyline;
import com.esri.arcgisruntime.location.LocationDataSource;

/**
 * A LocationDataSource that simulates movement along the specified route. Upon start of the
 * SimulatedLocationDataSource, a timer is started, which updates the location along the route at fixed
 * intervals.
 */
class SimulatedLocationDataSource extends LocationDataSource {

  private Point mCurrentLocation;
  private final Polyline mRoute;

  private Timer mTimer;

  private double distance = 0.0;
  private static final double distanceInterval = .00025;

  SimulatedLocationDataSource(Polyline route) {
    mRoute = route;
  }

  @Override
  protected void onStop() {
    mTimer.cancel();
  }

  @Override
  protected void onStart() {
    // start at the beginning of the route
    mCurrentLocation = mRoute.getParts().get(0).getStartPoint();
    updateLocation(new LocationDataSource.Location(mCurrentLocation));

    mTimer = new Timer("SimulatedLocationDataSource Timer", false);
    mTimer.scheduleAtFixedRate(new TimerTask() {
      @Override public void run() {
        // get a reference to the previous point
        Point previousPoint = mCurrentLocation;
        // update current location by moving [distanceInterval] meters along the route
        mCurrentLocation = GeometryEngine.createPointAlong(mRoute, distance);
        // get the geodetic distance result between the two points
        GeodeticDistanceResult distanceResult = GeometryEngine.distanceGeodetic(previousPoint, mCurrentLocation,
            new LinearUnit(LinearUnitId.METERS), new AngularUnit(AngularUnitId.DEGREES), GeodeticCurveType.GEODESIC);
        // update the location with the current location and use the geodetic distance result to get the azimuth
        updateLocation(new LocationDataSource.Location(mCurrentLocation, 1, 1, distanceResult.getAzimuth1(), false));
        // increment the distance
        distance += distanceInterval;
      }
    }, 0, 1000);
    // this method must be called by the subclass once the location data source has finished its starting process
    onStartCompleted(null);
  }
}
