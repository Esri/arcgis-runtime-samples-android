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

package com.esri.arcgisruntime.sample.simplerenderer;

import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.esri.arcgisruntime.symbology.SimpleRenderer;

public class MainActivity extends AppCompatActivity {

  MapView mMapView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // authentication with an API key or named user is required to access basemaps and other
    // location services
    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY);

    //Create points to add graphics to the map to allow a renderer to style them
    //These are in WGS84 coordinates (Long, Lat)
    Point oldFaithfullPoint = new Point(-110.828140, 44.460458, SpatialReferences.getWgs84());
    Point cascadeGeyserPoint = new Point(-110.829004, 44.462438, SpatialReferences.getWgs84());
    Point plumeGeyserPoint = new Point(-110.829381, 44.462735, SpatialReferences.getWgs84());
    //Use the farthest points to create an envelope to use for the map views visible area
    Envelope initialEnvelope = new Envelope(oldFaithfullPoint, plumeGeyserPoint);

    // inflate MapView from layout
    mMapView = findViewById(R.id.mapView);
    // create a map with the imagery basemap. This will set the map to have a WebMercator spatial reference
    ArcGISMap map = new ArcGISMap(BasemapStyle.ARCGIS_IMAGERY);
    // set the map to be displayed in the mapview
    mMapView.setMap(map);

    //set initial envelope on the map view sith some padding so all points will be visible
    //This envelope is using the WGS84 points above, but is reprojected by the mapview into the maps spatial
      // reference, so its works fine
    mMapView.setViewpointGeometryAsync(initialEnvelope, 100);

    // create a new graphics overlay and add it to the mapview
    GraphicsOverlay graphicOverlay = new GraphicsOverlay();
    mMapView.getGraphicsOverlays().add(graphicOverlay);

    //[DocRef: Name=Simple Renderer, Category=Fundamentals, Topic=Symbols and Renderers]
    //create a simple symbol for use in a simple renderer
    SimpleMarkerSymbol symbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CROSS, Color.RED,
        12); //size 12, style of cross
    SimpleRenderer renderer = new SimpleRenderer(symbol);

    //apply the renderer to the graphics overlay (so all graphics will use the same symbol from the renderer)
    graphicOverlay.setRenderer(renderer);
    //[DocRef: END]

    //create graphics from the geyser location points. NOTE: no need to set the symbol on the graphic because the
      // renderer takes care of it
    //The points are in WGS84, but graphics get reprojected automatically, so they work fine in a map with a spatial
      // reference of web mercator
    Graphic oldFaithfullGraphic = new Graphic(oldFaithfullPoint);
    Graphic cascadeGeyserGraphic = new Graphic(cascadeGeyserPoint);
    Graphic plumeGeyserGraphic = new Graphic(plumeGeyserPoint);
    graphicOverlay.getGraphics().add(oldFaithfullGraphic);
    graphicOverlay.getGraphics().add(cascadeGeyserGraphic);
    graphicOverlay.getGraphics().add(plumeGeyserGraphic);

  }

  @Override
  protected void onPause() {
    super.onPause();
    // pause MapView
    mMapView.pause();
  }

  @Override
  protected void onResume() {
    super.onResume();
    // resume MapView
    mMapView.resume();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mMapView.dispose();
  }
}
