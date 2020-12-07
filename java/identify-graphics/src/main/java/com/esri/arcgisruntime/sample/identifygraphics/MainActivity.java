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

package com.esri.arcgisruntime.sample.identifygraphics;

import java.util.List;
import java.util.concurrent.ExecutionException;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.geometry.PolygonBuilder;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.IdentifyGraphicsOverlayResult;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.SimpleFillSymbol;
import com.esri.arcgisruntime.util.ListenableList;

public class MainActivity extends AppCompatActivity {

  private MapView mMapView;
  private GraphicsOverlay grOverlay;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // authentication with an API key or named user is required to access basemaps and other
    // location services
    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY);

    // inflate MapView from layout
    mMapView = findViewById(R.id.mapView);

    // create a map with the BasemapType topographic
    ArcGISMap map = new ArcGISMap(BasemapStyle.ARCGIS_TOPOGRAPHIC);
    // set the map to be displayed in this view
    mMapView.setMap(map);
    mMapView.setViewpoint(new Viewpoint(3.184710, -4.734690, 100000000));

    // set up gesture for interacting with the MapView
    MainActivity.MapViewTouchListener mMapViewTouchListener = new MainActivity.MapViewTouchListener(this, mMapView);
    mMapView.setOnTouchListener(mMapViewTouchListener);

    addGraphicsOverlay();
  }

  private void addGraphicsOverlay() {
    // create the polygon
    PolygonBuilder polygonGeometry = new PolygonBuilder(SpatialReferences.getWebMercator());
    polygonGeometry.addPoint(-20e5, 20e5);
    polygonGeometry.addPoint(20e5, 20.e5);
    polygonGeometry.addPoint(20e5, -20e5);
    polygonGeometry.addPoint(-20e5, -20e5);

    // create solid line symbol
    SimpleFillSymbol polygonSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, Color.YELLOW, null);
    // create graphic from polygon geometry and symbol
    Graphic graphic = new Graphic(polygonGeometry.toGeometry(), polygonSymbol);

    // create graphics overlay
    grOverlay = new GraphicsOverlay();
    // create list of graphics
    ListenableList<Graphic> graphics = grOverlay.getGraphics();
    // add graphic to graphics overlay
    graphics.add(graphic);
    // add graphics overlay to the MapView
    mMapView.getGraphicsOverlays().add(grOverlay);
  }

  /**
   * Override default gestures of the MapView
   */
  class MapViewTouchListener extends DefaultMapViewOnTouchListener {

    /**
     * Constructs a DefaultMapViewOnTouchListener with the specified Context and MapView.
     *
     * @param context the context from which this is being created
     * @param mapView the MapView with which to interact
     */
    public MapViewTouchListener(Context context, MapView mapView) {
      super(context, mapView);
    }

    /**
     * Override the onSingleTapConfirmed gesture to handle tapping on the MapView
     * and detected if the Graphic was selected.
     *
     * @param e the motion event
     * @return true if the listener has consumed the event; false otherwise
     */
    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
      // get the screen point where user tapped
      Point screenPoint = new Point((int) e.getX(), (int) e.getY());

      // identify graphics on the graphics overlay
      final ListenableFuture<IdentifyGraphicsOverlayResult> identifyGraphic = mMapView
          .identifyGraphicsOverlayAsync(grOverlay, screenPoint, 10.0, false, 2);

      identifyGraphic.addDoneListener(() -> {
        try {
          IdentifyGraphicsOverlayResult grOverlayResult = identifyGraphic.get();
          // get the list of graphics returned by identify graphic overlay
          List<Graphic> graphic = grOverlayResult.getGraphics();
          // get size of list in results
          int identifyResultSize = graphic.size();
          if (!graphic.isEmpty()) {
            // show a toast message if graphic was returned
            Toast.makeText(getApplicationContext(), "Tapped on " + identifyResultSize + " Graphic", Toast.LENGTH_SHORT)
                .show();
          }
        } catch (InterruptedException | ExecutionException ie) {
          ie.printStackTrace();
        }

      });

      return super.onSingleTapConfirmed(e);
    }
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
