/*
 * Copyright 2016 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.esri.arcgisruntime.sample.spatialoperations;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.Part;
import com.esri.arcgisruntime.geometry.PartCollection;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.Polygon;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.SimpleFillSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;

public class MainActivity extends AppCompatActivity {

  final private GraphicsOverlay inputGeometryOverlay = new GraphicsOverlay();
  final private GraphicsOverlay resultGeometryOverlay = new GraphicsOverlay();
  // simple black (0xFF000000) line symbol for outlines
  final private SimpleLineSymbol lineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, 0xFF000000, 1);
  final private SimpleFillSymbol resultFillSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, 0xFFE91F1F,
      lineSymbol);
  private Polygon inputPolygon1;
  private Polygon inputPolygon2;
  // The spatial operation switching menu items.
  private MenuItem noOperationMenuItem = null;
  private MenuItem intersectionMenuItem = null;
  private MenuItem unionMenuItem = null;
  private MenuItem differenceMenuItem = null;
  private MenuItem symmetricDifferenceMenuItem = null;
  private MapView mMapView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mMapView = (MapView) findViewById(R.id.mapView);

    // create ArcGISMap with topographic basemap
    ArcGISMap map = new ArcGISMap(Basemap.createLightGrayCanvas());
    mMapView.setMap(map);

    // create graphics overlays to show the inputs and results of the spatial operation
    mMapView.getGraphicsOverlays().add(inputGeometryOverlay);
    mMapView.getGraphicsOverlays().add(resultGeometryOverlay);

    // create input polygons and add graphics to display these polygons in an overlay
    createPolygons();

    // center the map view on the input geometries
    Geometry viewpointGeom = GeometryEngine.union(inputPolygon1, inputPolygon2).getExtent();
    mMapView.setViewpointGeometryAsync(viewpointGeom, 20);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);

    // Get the menu items that perform spatial operations.
    noOperationMenuItem = menu.getItem(0);
    intersectionMenuItem = menu.getItem(1);
    unionMenuItem = menu.getItem(2);
    differenceMenuItem = menu.getItem(3);
    symmetricDifferenceMenuItem = menu.getItem(4);

    // set the 'no-op' menu item checked by default
    noOperationMenuItem.setChecked(true);

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // handle menu item selection
    int itemId = item.getItemId();

    // clear previous operation result
    resultGeometryOverlay.getGraphics().clear();

    // perform spatial operations and add results as graphics, depending on the option selected
    // if-else is used because this sample is used elsewhere as a Library module
    if (itemId == R.id.action_no_operation) {
      // no spatial operation - graphics have been cleared previously
      noOperationMenuItem.setChecked(true);
      return true;
    } else if (itemId == R.id.action_intersection) {
      intersectionMenuItem.setChecked(true);
      showGeometry(GeometryEngine.intersection(inputPolygon1, inputPolygon2));
      return true;
    } else if (itemId == R.id.action_union) {
      unionMenuItem.setChecked(true);
      showGeometry(GeometryEngine.union(inputPolygon1, inputPolygon2));
      return true;
    } else if (itemId == R.id.action_difference) {
      differenceMenuItem.setChecked(true);
      // note that the difference method gives different results depending on the order of input geometries
      showGeometry(GeometryEngine.difference(inputPolygon1, inputPolygon2));
      return true;
    } else if (itemId == R.id.action_symmetric_difference) {
      symmetricDifferenceMenuItem.setChecked(true);
      showGeometry(GeometryEngine.symmetricDifference(inputPolygon1, inputPolygon2));
      return true;
    } else {
      return super.onOptionsItemSelected(item);
    }
  }

  private void showGeometry(Geometry resultGeometry) {
    // add a graphic from the result geometry, showing result in red (0xFFE91F1F)
    Graphic resultGraphic = new Graphic(resultGeometry, resultFillSymbol);
    resultGeometryOverlay.getGraphics().add(resultGraphic);

    // select the result to highlight it
    resultGraphic.setSelected(true);
  }

  private void createPolygons() {

    // create input polygon 1
    PointCollection pointsPoly = new PointCollection(SpatialReferences.getWebMercator());
    pointsPoly.add(new Point(-13160, 6710100));
    pointsPoly.add(new Point(-13300, 6710500));
    pointsPoly.add(new Point(-13760, 6710730));
    pointsPoly.add(new Point(-14660, 6710000));
    pointsPoly.add(new Point(-13960, 6709400));
    inputPolygon1 = new Polygon(pointsPoly);

    // create and add a blue graphic to show input polygon 1
    SimpleFillSymbol fillSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, 0x990000CC, lineSymbol);
    inputGeometryOverlay.getGraphics().add(new Graphic(inputPolygon1, fillSymbol));

    // create input polygon 2 with a green (0xFF009900) symbol
    // outer ring
    PointCollection outerRingSegmentCollection = new PointCollection(SpatialReferences.getWebMercator());
    outerRingSegmentCollection.add(new Point(-13060, 6711030));
    outerRingSegmentCollection.add(new Point(-12160, 6710730));
    outerRingSegmentCollection.add(new Point(-13160, 6709700));
    outerRingSegmentCollection.add(new Point(-14560, 6710730));
    outerRingSegmentCollection.add(new Point(-13060, 6711030));
    Part outerRing = new Part(outerRingSegmentCollection);

    // inner ring
    PointCollection innerRingSegmentCollection = new PointCollection(SpatialReferences.getWebMercator());
    innerRingSegmentCollection.add(new Point(-13060, 6710910));
    innerRingSegmentCollection.add(new Point(-12450, 6710660));
    innerRingSegmentCollection.add(new Point(-13160, 6709900));
    innerRingSegmentCollection.add(new Point(-14160, 6710630));
    innerRingSegmentCollection.add(new Point(-13060, 6710910));
    Part innerRing = new Part(innerRingSegmentCollection);

    // add both parts (rings) to a part collection and create a geometry from it
    PartCollection polygonParts = new PartCollection(outerRing);
    polygonParts.add(innerRing);
    inputPolygon2 = new Polygon(polygonParts);

    // create and add a green graphic to show input polygon 2
    fillSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, 0x99009900, lineSymbol);
    inputGeometryOverlay.getGraphics().add(new Graphic(inputPolygon2, fillSymbol));
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
