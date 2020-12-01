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

package com.esri.arcgisruntime.sample.featurecollectionlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureCollection;
import com.esri.arcgisruntime.data.FeatureCollectionTable;
import com.esri.arcgisruntime.data.Field;
import com.esri.arcgisruntime.geometry.GeometryType;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PolygonBuilder;
import com.esri.arcgisruntime.geometry.PolylineBuilder;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.FeatureCollectionLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.Viewpoint;
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

    // inflate MapView from layout
    mMapView = findViewById(R.id.mapView);

    //initialize map with basemap
    ArcGISMap map = new ArcGISMap(BasemapStyle.ARCGIS_OCEANS);
    map.setInitialViewpoint(new Viewpoint( 8.5844, -79.6597, 2000000));

    //assign map to the map view
    mMapView.setMap(map);

    // create feature collection and add to the map as a layer
    FeatureCollection featureCollection = new FeatureCollection();
    FeatureCollectionLayer featureCollectionLayer = new FeatureCollectionLayer(featureCollection);
    map.getOperationalLayers().add(featureCollectionLayer);

    // add point, line, and polygon geometry to feature collection
    createPointTable(featureCollection);
    createPolylineTable(featureCollection);
    createPolygonTables(featureCollection);
  }

  /**
   * Creates a Point Feature Collection Table with one Point and adds it to the Feature collection that was passed.
   *
   * @param featureCollection that the point Feature Collection Table will be added to
   */
  private void createPointTable(FeatureCollection featureCollection) {

    // defines the schema for the geometry's attribute
    List<Field> pointFields = new ArrayList<>();
    pointFields.add(Field.createString("Place", "Place Name", 50));

    // a feature collection table that creates point geometry
    FeatureCollectionTable pointsTable = new FeatureCollectionTable(pointFields, GeometryType.POINT, SpatialReferences.getWgs84());

    // set a default symbol for features in the collection table
    SimpleMarkerSymbol markerSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.TRIANGLE, 0xFFFF0000, 18);
    SimpleRenderer renderer = new SimpleRenderer(markerSymbol);
    pointsTable.setRenderer(renderer);

    // add feature collection table to feature collection
    featureCollection.getTables().add(pointsTable);

    // create feature using the collection table by passing an attribute and geometry
    Map<String, Object> attributes = new HashMap<>();
    attributes.put(pointFields.get(0).getName(), "Current Location");
    Point point = new Point(-79.497238, 8.849289, SpatialReferences.getWgs84());
    Feature addedFeature = pointsTable.createFeature(attributes, point);

    // add feature to collection table
    pointsTable.addFeatureAsync(addedFeature);
  }

  /**
   * Creates a PolyLine Feature Collection Table with one PolyLine and adds it to the Feature collection that was passed.
   *
   * @param featureCollection that the polyline Feature Collection Table will be added to
   */
  private void createPolylineTable(FeatureCollection featureCollection) {

    // defines the schema for the geometry's attribute
    List<Field> polylineFields = new ArrayList<>();
    polylineFields.add(Field.createString("Boundary", "Boundary Name", 50));

    // a feature collection table that creates polyline geometry
    FeatureCollectionTable polylineTable = new FeatureCollectionTable(polylineFields, GeometryType.POLYLINE, SpatialReferences.getWgs84());

    // set a default symbol for features in the collection table
    SimpleLineSymbol lineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.DASH, 0xFF00FF00, 3);
    SimpleRenderer renderer = new SimpleRenderer(lineSymbol);
    polylineTable.setRenderer(renderer);

    // add feature collection table to feature collection
    featureCollection.getTables().add(polylineTable);

    // create feature using the collection table by passing an attribute and geometry
    Map<String, Object> attributes = new HashMap<>();
    attributes.put(polylineFields.get(0).getName(), "AManAPlanACanalPanama");
    PolylineBuilder builder = new PolylineBuilder(SpatialReferences.getWgs84());
    builder.addPoint(new Point(-79.497238, 8.849289));
    builder.addPoint(new Point(-80.035568, 9.432302));
    Feature addedFeature = polylineTable.createFeature(attributes, builder.toGeometry());

    // add feature to collection table
    polylineTable.addFeatureAsync(addedFeature);
  }

  /**
   * Creates a Polygon Feature Collection Table with one Polygon and adds it to the Feature collection that was passed.
   *
   * @param featureCollection that the polygon Feature Collection Table will be added to
   */
  private void createPolygonTables(FeatureCollection featureCollection) {

    // defines the schema for the geometry's attribute
    List<Field> polygonFields = new ArrayList<>();
    polygonFields.add(Field.createString("Area", "Area Name", 50));

    // a feature collection table that creates polygon geometry
    FeatureCollectionTable polygonTable = new FeatureCollectionTable(polygonFields, GeometryType.POLYGON, SpatialReferences.getWgs84());

    // set a default symbol for features in the collection table
    SimpleLineSymbol lineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, 0xFF0000FF, 2);
    SimpleFillSymbol fillSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.DIAGONAL_CROSS, 0xFF00FFFF, lineSymbol);
    SimpleRenderer renderer = new SimpleRenderer(fillSymbol);
    polygonTable.setRenderer(renderer);

    // add feature collection table to feature collection
    featureCollection.getTables().add(polygonTable);

    // create feature using the collection table by passing an attribute and geometry
    Map<String, Object> attributes = new HashMap<>();
    attributes.put(polygonFields.get(0).getName(), "Restricted area");
    PolygonBuilder builder = new PolygonBuilder(SpatialReferences.getWgs84());
    builder.addPoint(new Point(-79.497238, 8.849289));
    builder.addPoint(new Point(-79.337936, 8.638903));
    builder.addPoint(new Point(-79.11409, 8.895422));
    Feature addedFeature = polygonTable.createFeature(attributes, builder.toGeometry());

    // add feature to collection table
    polygonTable.addFeatureAsync(addedFeature);
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

