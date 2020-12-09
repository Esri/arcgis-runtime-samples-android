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
package com.esri.arcgisruntime.sample.buffer;

import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.geometry.GeodeticCurveType;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.LinearUnit;
import com.esri.arcgisruntime.geometry.LinearUnitId;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.SimpleFillSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.esri.arcgisruntime.symbology.SimpleRenderer;

public class MainActivity extends AppCompatActivity {

  private MapView mMapView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // authentication with an API key or named user is required to access basemaps and other
    // location services
    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY);

    // inflate views
    mMapView = findViewById(R.id.mapView);
    EditText bufferInput = findViewById(R.id.bufferInput);

    // create a map with the Basemap
    ArcGISMap map = new ArcGISMap(SpatialReferences.getWebMercator());
    map.setBasemap(new Basemap(BasemapStyle.ARCGIS_TOPOGRAPHIC));
    // set the map to be displayed in this view
    mMapView.setMap(map);

    // create a graphics overlay to contain the buffered geometry graphics
    GraphicsOverlay graphicsOverlay = new GraphicsOverlay();
    mMapView.getGraphicsOverlays().add(graphicsOverlay);

    // create a fill symbol for geodesic buffer polygons
    SimpleLineSymbol geodesicOutlineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLACK, 2);
    SimpleFillSymbol geodesicBufferFillSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, Color.GREEN,
        geodesicOutlineSymbol);

    // create a fill symbol for planar buffer polygons
    SimpleLineSymbol planarOutlineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLACK, 2);
    SimpleFillSymbol planarBufferFillSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, Color.RED,
        planarOutlineSymbol);

    // create a marker symbol for tap locations
    SimpleMarkerSymbol tapSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CROSS, Color.WHITE, 14);

    // create a graphics overlay to display geodesic polygons, set its renderer and add it to the map view.
    final GraphicsOverlay geodesicGraphicsOverlay = new GraphicsOverlay();
    geodesicGraphicsOverlay.setRenderer(new SimpleRenderer(geodesicBufferFillSymbol));
    geodesicGraphicsOverlay.setOpacity(0.5f);
    mMapView.getGraphicsOverlays().add(geodesicGraphicsOverlay);

    // create a graphics overlay to display planar polygons, set its renderer and add it to the map view.
    final GraphicsOverlay planarGraphicsOverlay = new GraphicsOverlay();
    planarGraphicsOverlay.setRenderer(new SimpleRenderer(planarBufferFillSymbol));
    planarGraphicsOverlay.setOpacity(0.5f);
    mMapView.getGraphicsOverlays().add(planarGraphicsOverlay);

    // create a graphics overlay to display tap locations for buffers, set its renderer and add it to the map view.
    final GraphicsOverlay tapLocationsOverlay = new GraphicsOverlay();
    tapLocationsOverlay.setRenderer(new SimpleRenderer(tapSymbol));
    mMapView.getGraphicsOverlays().add(tapLocationsOverlay);

    // create a buffer around the clicked location
    mMapView.setOnTouchListener(new DefaultMapViewOnTouchListener(this, mMapView) {
      @Override
      public boolean onSingleTapConfirmed(MotionEvent motionEvent) {

        // get the point that was clicked and convert it to a point in the map
        android.graphics.Point screenPoint = new android.graphics.Point(Math.round(motionEvent.getX()),
            Math.round(motionEvent.getY()));
        Point mapPoint = mMapView.screenToLocation(screenPoint);

        // only draw a buffer if a value was entered
        if (!bufferInput.getText().toString().isEmpty()) {
          // get the buffer distance (miles) entered in the text box.
          double bufferInMiles = Double.valueOf(bufferInput.getText().toString());

          // convert the input distance to meters, 1609.34 meters in one mile
          double bufferInMeters = bufferInMiles * 1609.34;

          // create a planar buffer graphic around the input location at the specified distance
          Geometry bufferGeometryPlanar = GeometryEngine.buffer(mapPoint, bufferInMeters);
          Graphic planarBufferGraphic = new Graphic(bufferGeometryPlanar);

          // create a geodesic buffer graphic using the same location and distance
          Geometry bufferGeometryGeodesic = GeometryEngine.bufferGeodetic(mapPoint, bufferInMeters,
              new LinearUnit(LinearUnitId.METERS), Double.NaN, GeodeticCurveType.GEODESIC);
          Graphic geodesicBufferGraphic = new Graphic(bufferGeometryGeodesic);

          // create a graphic for the user tap location
          Graphic locationGraphic = new Graphic(mapPoint);

          // add the buffer polygons and tap location graphics to the appropriate graphic overlays.
          planarGraphicsOverlay.getGraphics().add(planarBufferGraphic);
          geodesicGraphicsOverlay.getGraphics().add(geodesicBufferGraphic);
          tapLocationsOverlay.getGraphics().add(locationGraphic);
        } else {
          Toast.makeText(MainActivity.this, "Please enter a buffer distance first.", Toast.LENGTH_LONG).show();
        }
        return true;
      }
    });

    // clear all graphics on button click
    Button clearButton = findViewById(R.id.clearButton);
    clearButton.setOnClickListener(v -> {
      planarGraphicsOverlay.getGraphics().clear();
      geodesicGraphicsOverlay.getGraphics().clear();
      tapLocationsOverlay.getGraphics().clear();
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
