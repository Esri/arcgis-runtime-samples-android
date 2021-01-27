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

package com.esri.arcgisruntime.sample.creategeometries;

import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.Multipoint;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.Polygon;
import com.esri.arcgisruntime.geometry.Polyline;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.SimpleFillSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;

/**
 * Shows straightforward ways to create Point, Envelope, Multipoint, Polyline, and Polygon geometries. Shows a MapView
 * with a GraphicsOverlay containing Graphics created from the Point, Multipoint, Polyline, and Polygon geometries, and
 * sets the Viewpoint of the Map from the Envelope geometry.
 */
public class MainActivity extends AppCompatActivity {

  private MapView mMapView;

  private Envelope createEnvelope() {

    //[DocRef: Name=Create Envelope, Category=Fundamentals, Topic=Geometries]
    // create an Envelope using minimum and maximum x,y coordinates and a SpatialReference
    Envelope envelope = new Envelope(-123.0, 33.5, -101.0, 48.0, SpatialReferences.getWgs84());
    //[DocRef: END]

    return envelope;
  }

  private Point createPoint() {
    //[DocRef: Name=Create Point, Category=Fundamentals, Topic=Geometries]
    // create a Point using x,y coordinates and a SpatialReference
    Point pt = new Point(34.056295, -117.195800, SpatialReferences.getWgs84());
    //[DocRef: END]

    return pt;
  }

  private Multipoint createMultipoint() {
    //[DocRef: Name=Create Multipoint, Category=Fundamentals, Topic=Geometries]
    // create a Multipoint from a PointCollection
    PointCollection stateCapitalsPST = new PointCollection(SpatialReferences.getWgs84());
    stateCapitalsPST.add(-121.491014, 38.579065); // Sacramento, CA
    stateCapitalsPST.add(-122.891366, 47.039231); // Olympia, WA
    stateCapitalsPST.add(-123.043814, 44.93326); // Salem, OR
    stateCapitalsPST.add(-119.766999, 39.164885); // Carson City, NV
    Multipoint multipoint = new Multipoint(stateCapitalsPST);
    //[DocRef: END]

    return multipoint;
  }

  private Polyline createPolyline() {
    //[DocRef: Name=Create Polyline, Category=Fundamentals, Topic=Geometries]
    // create a Polyline from a PointCollection
    PointCollection borderCAtoNV = new PointCollection(SpatialReferences.getWgs84());
    borderCAtoNV.add(-119.992, 41.989);
    borderCAtoNV.add(-119.994, 38.994);
    borderCAtoNV.add(-114.620, 35.0);
    Polyline polyline = new Polyline(borderCAtoNV);
    //[DocRef: END]

    return polyline;
  }

  private Polygon createPolygon() {
    //[DocRef: Name=Create Polygon, Category=Fundamentals, Topic=Geometries]
    // create a Polygon from a PointCollection
    PointCollection coloradoCorners = new PointCollection(SpatialReferences.getWgs84());
    coloradoCorners.add(-109.048, 40.998);
    coloradoCorners.add(-102.047, 40.998);
    coloradoCorners.add(-102.037, 36.989);
    coloradoCorners.add(-109.048, 36.998);
    Polygon polygon = new Polygon(coloradoCorners);
    //[DocRef: END]

    return polygon;
  }


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // authentication with an API key or named user is required to access basemaps and other
    // location services
    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY);

    // get MapView from layout
    mMapView = findViewById(R.id.mapView);

    // create a map with the BasemapType topographic
    final ArcGISMap mMap = new ArcGISMap(BasemapStyle.ARCGIS_TOPOGRAPHIC);

    // set the map to be displayed in this view
    mMapView.setMap(mMap);

    // create color and symbols for drawing graphics
    SimpleMarkerSymbol markerSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.TRIANGLE, Color.BLUE, 14);
    SimpleFillSymbol fillSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.CROSS, Color.BLUE, null);
    SimpleLineSymbol lineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLUE, 3);

    // add a graphic of point, multipoint, polyline and polygon.
    GraphicsOverlay overlay = new GraphicsOverlay();
    mMapView.getGraphicsOverlays().add(overlay);
    overlay.getGraphics().add(new Graphic(createPolygon(), fillSymbol));
    overlay.getGraphics().add(new Graphic(createPolyline(), lineSymbol));
    overlay.getGraphics().add(new Graphic(createMultipoint(), markerSymbol));
    overlay.getGraphics().add(new Graphic(createPoint(), markerSymbol));

    // use the envelope to set the map viewpoint
    mMapView.setViewpointGeometryAsync(createEnvelope(), getResources().getDimension(R.dimen.viewpoint_padding));

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
