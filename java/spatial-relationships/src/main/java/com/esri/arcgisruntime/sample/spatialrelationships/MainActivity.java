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

package com.esri.arcgisruntime.sample.spatialrelationships;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.GeometryType;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.Polygon;
import com.esri.arcgisruntime.geometry.Polyline;
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
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private MapView mMapView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // authentication with an API key or named user is required to access basemaps and other
    // location services
    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY);

    // inflate MapView from layout
    mMapView = findViewById(R.id.mapView);
    mMapView.getSelectionProperties().setColor(Color.RED);

    // create a map with a topographic  basemap
    ArcGISMap map = new ArcGISMap(BasemapStyle.ARCGIS_TOPOGRAPHIC);

    // create a graphics overlay
    GraphicsOverlay graphicsOverlay = new GraphicsOverlay();
    mMapView.getGraphicsOverlays().add(graphicsOverlay);

    // create a polygon graphic
    PointCollection polygonPoints = new PointCollection(SpatialReferences.getWebMercator());
    polygonPoints.add(new Point(-5991501.677830, 5599295.131468));
    polygonPoints.add(new Point(-6928550.398185, 2087936.739807));
    polygonPoints.add(new Point(-3149463.800709, 1840803.011362));
    polygonPoints.add(new Point(-1563689.043184, 3714900.452072));
    polygonPoints.add(new Point(-3180355.516764, 5619889.608838));
    Polygon polygon = new Polygon(polygonPoints);
    SimpleFillSymbol polygonSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.FORWARD_DIAGONAL, Color.GREEN,
        new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, 0xFF00FF00, 2));
    Graphic polygonGraphic = new Graphic(polygon, polygonSymbol);
    graphicsOverlay.getGraphics().add(polygonGraphic);

    // create a polyline graphic
    PointCollection polylinePoints = new PointCollection(SpatialReferences.getWebMercator());
    polylinePoints.add(new Point(-4354240.726880, -609939.795721));
    polylinePoints.add(new Point(-3427489.245210, 2139422.933233));
    polylinePoints.add(new Point(-2109442.693501, 4301843.057130));
    polylinePoints.add(new Point(-1810822.771630, 7205664.366363));
    Polyline polyline = new Polyline(polylinePoints);
    Graphic polylineGraphic = new Graphic(polyline, new SimpleLineSymbol(SimpleLineSymbol.Style.DASH, Color.RED,
        4));
    graphicsOverlay.getGraphics().add(polylineGraphic);

    // create a point graphic
    Point point = new Point(-4487263.495911, 3699176.480377, SpatialReferences.getWebMercator());
    SimpleMarkerSymbol locationMarker = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.BLUE, 10);
    Graphic pointGraphic = new Graphic(point, locationMarker);
    graphicsOverlay.getGraphics().add(pointGraphic);

    // create HashMap that will hold relationships in between graphics
    HashMap<String, List<String>> relationships = new HashMap<>();

    // set the map to be displayed in this view and the initial view point
    mMapView.setMap(map);
    mMapView.setViewpoint(new Viewpoint(point, 90000000));

    // create intent to be passed to the other activity
    Intent intent = new Intent(this, ResultsActivity.class);

    // add a touch listener to identify the selected graphic
    mMapView.setOnTouchListener(new DefaultMapViewOnTouchListener(this, mMapView) {
      @Override
      public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
        // identify the clicked graphic(s)
        android.graphics.Point clickLocation = new android.graphics.Point((int) motionEvent.getX(),
            (int) motionEvent.getY());
        ListenableFuture<IdentifyGraphicsOverlayResult> identifyGraphics =
            mMapView.identifyGraphicsOverlayAsync(graphicsOverlay, clickLocation, 1, false);
        identifyGraphics.addDoneListener(() -> {

          try {
            // get the first identified graphic
            IdentifyGraphicsOverlayResult result = identifyGraphics.get();
            List<Graphic> identifiedGraphics = result.getGraphics();
            if (!identifiedGraphics.isEmpty()) {
              // clear previous results
              relationships.put("Point", new ArrayList<>());
              relationships.put("Polyline", new ArrayList<>());
              relationships.put("Polygon", new ArrayList<>());

              // select the identified graphic
              graphicsOverlay.clearSelection();
              Graphic identifiedGraphic = identifiedGraphics.get(0);
              identifiedGraphic.setSelected(true);
              Geometry selectedGeometry = identifiedGraphic.getGeometry();
              GeometryType selectedGeometryType = selectedGeometry.getGeometryType();
              Toast.makeText(MainActivity.this,selectedGeometry.getGeometryType().toString() + " is selected",Toast.LENGTH_LONG).show();
              // populate HashMap that will be passed to the expandable list view
              if (selectedGeometryType != GeometryType.POINT) {
                ArrayList<String> pointRelationships = relationshipStringList(getSpatialRelationships(selectedGeometry,
                    pointGraphic.getGeometry()));
                relationships.put("Point", pointRelationships);
              }
              if (selectedGeometryType != GeometryType.POLYLINE) {
                ArrayList<String> polylineRelationships = relationshipStringList(
                    getSpatialRelationships(selectedGeometry,
                        polylineGraphic.getGeometry()));
                relationships.put("Polyline", polylineRelationships);
              }
              if (selectedGeometryType != GeometryType.POLYGON) {
                ArrayList<String> polygonRelationships = relationshipStringList(
                    getSpatialRelationships(selectedGeometry,
                        polygonGraphic.getGeometry()));
                relationships.put("Polygon", polygonRelationships);
              }
              // pass the HashMap to the intent
              intent.putExtra("HashMap", relationships);
              startActivity(intent);
            }
          } catch (InterruptedException | ExecutionException e) {
            Log.e(TAG, e.toString());
          }
        });
        return true;
      }
    });
  }

  /**
   * Gets a string representation of the spatial relationship list
   *
   * @param relationshipList a list of spatial relationships
   * @return a string list of spatial relationships
   */
  private static ArrayList<String> relationshipStringList(List<QueryParameters.SpatialRelationship> relationshipList) {
    ArrayList<String> stringList = new ArrayList<>();
    for (QueryParameters.SpatialRelationship relationship : relationshipList) {
      stringList.add(relationship.toString());
    }
    return stringList;
  }

  /**
   * Gets a list of spatial relationships that the first geometry has to the second geometry.
   *
   * @param a first geometry
   * @param b second geometry
   * @return list of relationships a has to b
   */
  private static List<QueryParameters.SpatialRelationship> getSpatialRelationships(Geometry a, Geometry b) {
    List<QueryParameters.SpatialRelationship> relationships = new ArrayList<>();
    if (GeometryEngine.crosses(a, b))
      relationships.add(QueryParameters.SpatialRelationship.CROSSES);
    if (GeometryEngine.contains(a, b))
      relationships.add(QueryParameters.SpatialRelationship.CONTAINS);
    if (GeometryEngine.disjoint(a, b))
      relationships.add(QueryParameters.SpatialRelationship.DISJOINT);
    if (GeometryEngine.intersects(a, b))
      relationships.add(QueryParameters.SpatialRelationship.INTERSECTS);
    if (GeometryEngine.overlaps(a, b))
      relationships.add(QueryParameters.SpatialRelationship.OVERLAPS);
    if (GeometryEngine.touches(a, b))
      relationships.add(QueryParameters.SpatialRelationship.TOUCHES);
    if (GeometryEngine.within(a, b))
      relationships.add(QueryParameters.SpatialRelationship.WITHIN);
    return relationships;
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
    mMapView.dispose();
    super.onDestroy();
  }
}
