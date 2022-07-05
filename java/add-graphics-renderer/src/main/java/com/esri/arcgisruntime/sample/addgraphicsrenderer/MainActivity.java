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

package com.esri.arcgisruntime.sample.addgraphicsrenderer;

import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.geometry.CubicBezierSegment;
import com.esri.arcgisruntime.geometry.EllipticArcSegment;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.Part;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.Polygon;
import com.esri.arcgisruntime.geometry.PolygonBuilder;
import com.esri.arcgisruntime.geometry.PolylineBuilder;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.Viewpoint;
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

    // create MapView from layout
    mMapView = findViewById(R.id.mapView);
    // create a map with the Basemap Style topographic
    ArcGISMap map = new ArcGISMap(BasemapStyle.ARCGIS_TOPOGRAPHIC);
    // add graphics overlay
    addGraphicsOverlay();
    // set the map to be displayed in this view
    mMapView.setMap(map);
    mMapView.setViewpoint(new Viewpoint(15.169193, 16.333479, 100000000.0));
  }

  private void addGraphicsOverlay() {
    // point graphic
    Point pointGeometry = new Point(40e5, 40e5, SpatialReferences.getWebMercator());
    // red diamond point symbol
    SimpleMarkerSymbol pointSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.DIAMOND,
        Color.RED, 10);
    // create graphic for point
    Graphic pointGraphic = new Graphic(pointGeometry);
    // create a graphic overlay for the point
    GraphicsOverlay pointGraphicOverlay = new GraphicsOverlay();
    // create simple renderer
    SimpleRenderer pointRenderer = new SimpleRenderer(pointSymbol);
    pointGraphicOverlay.setRenderer(pointRenderer);
    // add graphic to overlay
    pointGraphicOverlay.getGraphics().add(pointGraphic);
    // add graphics overlay to the MapView
    mMapView.getGraphicsOverlays().add(pointGraphicOverlay);

    // line graphic
    PolylineBuilder lineGeometry = new PolylineBuilder(SpatialReferences.getWebMercator());
    lineGeometry.addPoint(-10e5, 40e5);
    lineGeometry.addPoint(20e5, 50e5);
    // solid blue line symbol
    SimpleLineSymbol lineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLUE, 5);
    // create graphic for polyline
    Graphic lineGraphic = new Graphic(lineGeometry.toGeometry());
    // create graphic overlay for polyline
    GraphicsOverlay lineGraphicOverlay = new GraphicsOverlay();
    // create simple renderer
    SimpleRenderer lineRenderer = new SimpleRenderer(lineSymbol);
    // add graphic to overlay
    lineGraphicOverlay.setRenderer(lineRenderer);
    // add graphic to overlay
    lineGraphicOverlay.getGraphics().add(lineGraphic);
    // add graphics overlay to the MapView
    mMapView.getGraphicsOverlays().add(lineGraphicOverlay);

    // polygon graphic
    PolygonBuilder polygonGeometry = new PolygonBuilder(SpatialReferences.getWebMercator());
    polygonGeometry.addPoint(-20e5, 20e5);
    polygonGeometry.addPoint(20e5, 20e5);
    polygonGeometry.addPoint(20e5, -20e5);
    polygonGeometry.addPoint(-20e5, -20e5);
    // solid yellow polygon symbol
    SimpleFillSymbol polygonSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID,
        Color.YELLOW, null);
    // create graphic for polygon
    Graphic polygonGraphic = new Graphic(polygonGeometry.toGeometry());
    // create graphic overlay for polygon
    GraphicsOverlay polygonGraphicOverlay = new GraphicsOverlay();
    // create simple renderer
    SimpleRenderer polygonRenderer = new SimpleRenderer(polygonSymbol);
    // add graphic to overlay
    polygonGraphicOverlay.setRenderer(polygonRenderer);
    // add graphic to overlay
    polygonGraphicOverlay.getGraphics().add(polygonGraphic);
    // add graphics overlay to MapView
    mMapView.getGraphicsOverlays().add(polygonGraphicOverlay);

    // curved polygon graphic
    // create a point for the center of the geometry
    Point originPoint = new Point(40e5, 5e5, SpatialReferences.getWebMercator());
    // create polygon
    Geometry curvedPolygonGeometry = makeHeartGeometry(originPoint, 10e5);
    // create graphic for polygon
    Graphic curvedPolygonGraphic = new Graphic(curvedPolygonGeometry);
    // create a simple fill symbol with outline
    SimpleLineSymbol curvedLineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID,
        Color.BLACK, 1f);
    SimpleFillSymbol curvedFillSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID,
        Color.RED, curvedLineSymbol);
    // create simple renderer
    SimpleRenderer curvedPolygonRenderer = new SimpleRenderer(curvedFillSymbol);

    // create graphic overlay for polygon and add it to the map view
    GraphicsOverlay curvedPolygonGraphicsOverlay = new GraphicsOverlay();
    // add graphic to overlay
    curvedPolygonGraphicsOverlay.getGraphics().add(curvedPolygonGraphic);
    // set the renderer on the graphics overlay to the new renderer
    curvedPolygonGraphicsOverlay.setRenderer(curvedPolygonRenderer);
    // add graphics overlay to MapView
    mMapView.getGraphicsOverlays().add(curvedPolygonGraphicsOverlay);
  }

  /**
   * Create a heart-shape geometry with Bezier and elliptic arc segments from a given point and
   * side length.
   *
   * @param center of the heart shape
   * @param sideLength of the heart shape
   *
   * @return a heart shaped {@link Geometry}
   */
  private static Geometry makeHeartGeometry(Point center, double sideLength) {
    SpatialReference spatialReference = center.getSpatialReference();
    // the x and y coordinates to simplify the calculation
    double minX = center.getX() - 0.5 * sideLength;
    double minY = center.getY() - 0.5 * sideLength;
    // the radius of the arcs
    double arcRadius = sideLength * 0.25;

    // bottom left curve
    Point leftCurveStart = new Point(center.getX(), minY, spatialReference);
    Point leftCurveEnd = new Point(minX, minY + 0.75 * sideLength, spatialReference);
    Point leftControlPoint1 = new Point(center.getX(), minY + 0.25 * sideLength, spatialReference);
    Point leftControlPoint2 = new Point(minX, center.getY(), spatialReference);
    CubicBezierSegment leftCurve = new CubicBezierSegment(
        leftCurveStart,
        leftControlPoint1,
        leftControlPoint2,
        leftCurveEnd,
        spatialReference
    );

    // top left arc
    Point leftArcCenter =
        new Point(minX + 0.25 * sideLength, minY + 0.75 * sideLength, spatialReference);
    EllipticArcSegment leftArc = EllipticArcSegment.createCircularEllipticArc(
        leftArcCenter,
        arcRadius,
        Math.PI,
        -Math.PI,
        spatialReference
    );

    // top right arc
    Point rightArcCenter =
        new Point(minX + 0.75 * sideLength, minY + 0.75 * sideLength, spatialReference);
    EllipticArcSegment rightArc = EllipticArcSegment.createCircularEllipticArc(
        rightArcCenter,
        arcRadius,
        Math.PI,
        -Math.PI,
        spatialReference
    );

    // bottom right curve
    Point rightCurveStart = new Point(minX + sideLength, minY + 0.75 * sideLength,
        spatialReference);
    Point rightCurveEnd = leftCurveStart;
    Point rightControlPoint1 = new Point(minX + sideLength, center.getY(), spatialReference);
    Point rightControlPoint2 = leftControlPoint1;
    CubicBezierSegment rightCurve = new CubicBezierSegment(
        rightCurveStart,
        rightControlPoint1,
        rightControlPoint2,
        rightCurveEnd,
        spatialReference
    );

    Part heart = new Part(spatialReference);
    heart.add(leftCurve);
    heart.add(leftArc);
    heart.add(rightArc);
    heart.add(rightCurve);

    return new Polygon(heart, spatialReference);
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
