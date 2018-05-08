/* Copyright 2016 Esri
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

package com.esri.arcgisruntime.basicandroidproject;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.TextView;

import com.esri.arcgisruntime.geometry.*;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.*;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

  private MapView mMapView;
  private SpatialReference srWgs84 = SpatialReferences.getWgs84();
  private Point start;
  private Point destination;
  private Graphic path;
  private Callout mCallout;
  //TODO is this the correct linear unit?
  private LinearUnit unitOfMeasurement = new LinearUnit(LinearUnitId.KILOMETERS);
  private String units = "Kilometers";

  @SuppressLint("ClickableViewAccessibility") @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // create a map
    ArcGISMap map = new ArcGISMap(Basemap.createImagery());

    // set map to a map view
    mMapView = (MapView) findViewById(R.id.mapView);
    mMapView.setMap(map);

    // create a graphic overlay
    GraphicsOverlay graphicsOverlay = new GraphicsOverlay();
    mMapView.getGraphicsOverlays().add(graphicsOverlay);

    // add a graohic at JFK to represent the flight start location //
    start = new Point(-73.7781, 40.6413, srWgs84);
    SimpleMarkerSymbol locationMarker = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, 0xFF0000FF, 10);
    Graphic startLocation = new Graphic(start, locationMarker);
    graphicsOverlay.getGraphics().add(startLocation);

    // create graphic for the destination
    final Graphic endLocation = new Graphic();
    endLocation.setSymbol(locationMarker);
    graphicsOverlay.getGraphics().add(endLocation);

    // create graphic representing the geodesic path between the two locations
    path = new Graphic();
    path.setSymbol(new SimpleLineSymbol(SimpleLineSymbol.Style.DASH, 0xFF0000FF, 5));
    graphicsOverlay.getGraphics().add(path);

    //add onTouchListener to get the location of the user tap
    mMapView.setOnTouchListener(new DefaultMapViewOnTouchListener(this, mMapView) {

      @Override
      public boolean onSingleTapConfirmed(MotionEvent motionEvent) {

        // get the point that was clicked and convert it to a point in the map
        android.graphics.Point clickLocation = new android.graphics.Point(Math.round(motionEvent.getX()),
            Math.round(motionEvent.getY()));
        Point mapPoint = mMapView.screenToLocation(clickLocation);
        destination = (Point) GeometryEngine.project(mapPoint, SpatialReferences.getWgs84());
        endLocation.setGeometry(destination);
        // create a straight line path between the start and end locations
        PointCollection points = new PointCollection(Arrays.asList(start, destination), srWgs84);
        Polyline polyline = new Polyline(points);
        // densify the path as a geodesic curve and show it with the path graphic
        Geometry pathGeometry = GeometryEngine
            .densifyGeodetic(polyline, 1, unitOfMeasurement, GeodeticCurveType.GEODESIC);
        path.setGeometry(pathGeometry);
        // calculate the path distance
        double distance = GeometryEngine.lengthGeodetic(pathGeometry, unitOfMeasurement, GeodeticCurveType.GEODESIC);

        // create a textview for the callout
        TextView calloutContent = new TextView(getApplicationContext());
        calloutContent.setTextColor(Color.BLACK);
        calloutContent.setSingleLine();
        // format coordinates to 2 decimal places
        calloutContent.setText("Distance: " + String.format("%.2f", distance) + units);
        mCallout = mMapView.getCallout();
        mCallout.setLocation(mapPoint);
        mCallout.setContent(calloutContent);
        mCallout.show();
        return true;

      }
    });

  }

  @Override
  protected void onPause() {
    mMapView.pause();
    super.onPause();
  }

  @Override
  protected void onResume() {
    super.onResume();
    mMapView.resume();
  }
}

