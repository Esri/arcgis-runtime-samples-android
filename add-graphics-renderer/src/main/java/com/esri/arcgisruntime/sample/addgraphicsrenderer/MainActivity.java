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
import android.support.v7.app.AppCompatActivity;

import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PolylineBuilder;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Map;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.esri.arcgisruntime.symbology.SimpleRenderer;

public class MainActivity extends AppCompatActivity {

    private MapView mMapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // inflate MapView from layout
        mMapView = (MapView) findViewById(R.id.mapView);
        // create a map with the Basemap Type topographic
        Map mMap = new Map(Basemap.Type.TOPOGRAPHIC, 15.169193, 16.333479, 2);
        // add graphics overlay
        addGraphicsOverlay();
        // set the map to be displayed in this view
        mMapView.setMap(mMap);
    }

    private void addGraphicsOverlay(){
        // point graphic
        Point pointGeometry = new Point(40e5, 40e5, SpatialReferences.getWebMercator());
        // yellow diamond point symbol
        SimpleMarkerSymbol pointSymbol = new SimpleMarkerSymbol(Color.YELLOW, 10, SimpleMarkerSymbol.Style.DIAMOND);
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
        SimpleLineSymbol lineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLUE, 5);
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

    }

    @Override
    protected void onPause(){
        super.onPause();
        mMapView.pause();
    }

    @Override
    protected void onResume(){
        super.onResume();
        mMapView.resume();
    }
}
