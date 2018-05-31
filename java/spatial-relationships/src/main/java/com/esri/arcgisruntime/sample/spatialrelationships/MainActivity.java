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

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.Polygon;
import com.esri.arcgisruntime.geometry.Polyline;
import com.esri.arcgisruntime.geometry.SpatialReferences;
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

    // inflate MapView from layout
    mMapView = findViewById(R.id.mapView);
    // create a map with a topographic  basemap
    ArcGISMap map = new ArcGISMap(Basemap.createTopographic());

    // set the map to be displayed in this view
    mMapView.setMap(map);

    // create a grphics overlay
    GraphicsOverlay graphicsOverlay = new GraphicsOverlay();
    mMapView.getGraphicsOverlays().add(graphicsOverlay);
    graphicsOverlay.setSelectionColor(0xFFFFF00);

    //create a polygon graphic
    PointCollection polygonPoints = new PointCollection(SpatialReferences.getWebMercator());
    polygonPoints.add(new Point(-5991501.677830, 5599295.131468));
    polygonPoints.add(new Point(-6928550.398185, 2087936.739807));
    polygonPoints.add(new Point(-3149463.800709, 1840803.011362));
    polygonPoints.add(new Point(-1563689.043184, 3714900.452072));
    polygonPoints.add(new Point(-3180355.516764, 5619889.608838));
    Polygon polygon = new Polygon(polygonPoints);
    SimpleFillSymbol polygonSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.FORWARD_DIAGONAL, 0xFF00FF00,
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
    Graphic polylineGraphic = new Graphic(polyline, new SimpleLineSymbol(SimpleLineSymbol.Style.DASH, 0xFFFF0000,
        4));
    graphicsOverlay.getGraphics().add(polylineGraphic);

    // create a point graphic
    Point point = new Point(-4487263.495911, 3699176.480377, SpatialReferences.getWebMercator());
    SimpleMarkerSymbol locationMarker = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, 0xFF0000FF, 10);
    Graphic pointGraphic = new Graphic(point, locationMarker);
    graphicsOverlay.getGraphics().add(pointGraphic);

    mMapView.setOnTouchListener(new DefaultMapViewOnTouchListener(this,mMapView){

      @Override
      public boolean onSingleTapConfirmed(MotionEvent motionEvent){
        showResults();
        return true;
      }
    } );

  }

  private void showResults(){
    Log.e("uhm","print");
    Intent intent = new Intent(this,ResultsActivity.class);
    startActivity(intent);
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