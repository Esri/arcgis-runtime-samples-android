/* Copyright 2018 Esri
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

package com.esri.arcgisruntime.geodesicoperations;

import java.util.Arrays;

import android.graphics.Color;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.TextView;

import com.esri.arcgisruntime.geometry.GeodeticCurveType;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.LinearUnit;
import com.esri.arcgisruntime.geometry.LinearUnitId;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.Polyline;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.Callout;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;

public class MainActivity extends AppCompatActivity {

  private MapView mMapView;
  private final SpatialReference mSrWgs84 = SpatialReferences.getWgs84();
  private final LinearUnit mUnitOfMeasurement = new LinearUnit(LinearUnitId.KILOMETERS);
  private final String mUnits = "Kilometers";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // create a map
    ArcGISMap map = new ArcGISMap(Basemap.createImagery());

    // set map to a map view
    mMapView = findViewById(R.id.mapView);
    mMapView.setMap(map);

    // create a graphic overlay
    GraphicsOverlay graphicsOverlay = new GraphicsOverlay();
    mMapView.getGraphicsOverlays().add(graphicsOverlay);

    // add a graphic at JFK to represent the flight start location //
    final Point start = new Point(-73.7781, 40.6413, mSrWgs84);
    SimpleMarkerSymbol locationMarker = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, 0xFF0000FF, 10);
    Graphic startLocation = new Graphic(start, locationMarker);
    graphicsOverlay.getGraphics().add(startLocation);

    // create graphic for the destination
    final Graphic endLocation = new Graphic();
    endLocation.setSymbol(locationMarker);
    graphicsOverlay.getGraphics().add(endLocation);

    // create graphic representing the geodesic path between the two locations
    final Graphic path = new Graphic();
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
        final Point destination = (Point) GeometryEngine.project(mapPoint, SpatialReferences.getWgs84());
        endLocation.setGeometry(destination);
        // create a straight line path between the start and end locations
        PointCollection points = new PointCollection(Arrays.asList(start, destination), mSrWgs84);
        Polyline polyline = new Polyline(points);
        // densify the path as a geodesic curve and show it with the path graphic
        Geometry pathGeometry = GeometryEngine
            .densifyGeodetic(polyline, 1, mUnitOfMeasurement, GeodeticCurveType.GEODESIC);
        path.setGeometry(pathGeometry);
        // calculate the path distance
        double distance = GeometryEngine.lengthGeodetic(pathGeometry, mUnitOfMeasurement, GeodeticCurveType.GEODESIC);

        // create a textview for the callout
        TextView calloutContent = new TextView(getApplicationContext());
        calloutContent.setTextColor(Color.BLACK);
        calloutContent.setSingleLine();
        // format coordinates to 2 decimal places
        calloutContent.setText("Distance: " + String.format("%.2f", distance) + mUnits);
        final Callout callout = mMapView.getCallout();
        callout.setLocation(mapPoint);
        callout.setContent(calloutContent);
        callout.show();
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
  @Override
  protected void onDestroy() {
    super.onDestroy();
    mMapView.dispose();
  }
}

