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

package com.esri.arcgisruntime.sample.project;

import java.text.DecimalFormat;

import android.graphics.Color;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.TextView;

import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.Callout;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;

public class MainActivity extends AppCompatActivity {

  private MapView mMapView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // inflate MapView from layout
    mMapView = findViewById(R.id.mapView);
    // create a map with a web mercator basemap
    ArcGISMap map = new ArcGISMap(SpatialReferences.getWebMercator());
    map.setBasemap(Basemap.createNationalGeographic());

    // set the map to be displayed in this view
    mMapView.setMap(map);

    // zoom to Minneapolis
    Geometry startingEnvelope = new Envelope(-10995912.335747, 5267868.874421, -9880363.974046, 5960699.183877,
        SpatialReferences.getWebMercator());
    mMapView.setViewpointGeometryAsync(startingEnvelope);

    // create graphics to show the input location
    GraphicsOverlay graphicsOverlay = new GraphicsOverlay();
    mMapView.getGraphicsOverlays().add(graphicsOverlay);

    // create a red marker symbol for the input point
    final SimpleMarkerSymbol markerSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, 0xFFFF0000, 5);
    final Graphic inputPointGraphic = new Graphic();
    inputPointGraphic.setSymbol(markerSymbol);
    graphicsOverlay.getGraphics().add(inputPointGraphic);

    final DecimalFormat decimalFormat = new DecimalFormat("#.00000");

    // show the input location where the user clicks on the map
    mMapView.setOnTouchListener(new DefaultMapViewOnTouchListener(this, mMapView) {

      @Override
      public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
        android.graphics.Point clickedLocation = new android.graphics.Point(Math.round(motionEvent.getX()),
            Math.round(motionEvent.getY()));
        Point originalPoint = mMapView.screenToLocation(clickedLocation);
        inputPointGraphic.setGeometry(originalPoint);
        // project the web mercator point to WGS84 (WKID 4236)
        Point projectedPoint = (Point) GeometryEngine.project(originalPoint, SpatialReferences.getWgs84());

        // show the original and projected point coordinates in a callout from the graphic
        String ox = decimalFormat.format(originalPoint.getX());
        String oy = decimalFormat.format(originalPoint.getY());
        String px = decimalFormat.format(projectedPoint.getX());
        String py = decimalFormat.format(projectedPoint.getY());
        // create a textView for the content of the callout
        TextView calloutContent = new TextView(getApplicationContext());
        calloutContent.setTextColor(Color.BLACK);
        calloutContent.setText(String.format("Coordinates\nOriginal: %s, %s\nProjected: %s, %s", ox, oy, px, py));
        // create callout
        final Callout callout = mMapView.getCallout();
        callout.setLocation(originalPoint);
        callout.setContent(calloutContent);
        callout.show();
        return true;
      }
    });
  }

  @Override
  protected void onPause() {
    super.onPause();
    mMapView.pause();
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
