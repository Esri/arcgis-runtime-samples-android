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

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import com.esri.arcgisruntime.geometry.*;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.SimpleFillSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;

public class MainActivity extends AppCompatActivity {

  private MapView mMapView;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // inflate MapViews
    mMapView = findViewById(R.id.mapView);
    // create a map with the Basemap
    ArcGISMap map = new ArcGISMap(SpatialReferences.getWebMercator());
    map.setBasemap(Basemap.createTopographic());
    // set the map to be displayed in this view
    mMapView.setMap(map);

    Geometry startingEnvelope = new Envelope(-10863035.97, 3838021.34, -10744801.344, 3887145.299,
        SpatialReferences.getWebMercator());
    mMapView.setViewpointGeometryAsync(startingEnvelope);

    // create a graphics overlay to contain the buffered geometry graphics
    final GraphicsOverlay graphicsOverlay = new GraphicsOverlay();
    mMapView.getGraphicsOverlays().add(graphicsOverlay);

    // set up units to convert from miles to meters
    final LinearUnit miles = new LinearUnit(LinearUnitId.MILES);
    final LinearUnit meters = new LinearUnit(LinearUnitId.METERS);

    //create a semi-transparent green fill symbol for the buffer regions
    final SimpleFillSymbol fillSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID,  0x8800FF00, new
        SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, 0xFF057e15, 5));

    // create a buffer around the clicked location
    mMapView.setOnTouchListener(new DefaultMapViewOnTouchListener(this,mMapView){

      @Override
      public boolean onSingleTapConfirmed(MotionEvent motionEvent){

        // get the point that was clicked and convert it to a point in the map
        android.graphics.Point clickLocation = new android.graphics.Point(Math.round(motionEvent.getX()),
            Math.round(motionEvent.getY()));
        Point mapPoint = mMapView.screenToLocation(clickLocation);
        Polygon bufferGeometry = GeometryEngine.buffer(mapPoint,miles.convertTo(meters,10));
        // show the buffered region as a green graphic
        Graphic bufferGraphic = new Graphic(bufferGeometry,fillSymbol);
        graphicsOverlay.getGraphics().add(bufferGraphic);
        //show a red marker where clicked
        Graphic markerGraphic = new Graphic(mapPoint, new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE,
            0xFFFF0000, 5));
        graphicsOverlay.getGraphics().add(markerGraphic);
        return true;
      }
    }

    );



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
