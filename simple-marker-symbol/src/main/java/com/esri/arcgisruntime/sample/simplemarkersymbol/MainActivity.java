/* Copyright 2015 ESRI
 *
 * All rights reserved under the copyright laws of the United States
 * and applicable international laws, treaties, and conventions.
 *
 * You may freely redistribute and use this sample code, with or
 * without modification, provided you include the original copyright
 * notice and use restrictions.
 *
 * See the Sample code usage restrictions document for further information.
 *
 */

package com.esri.arcgisruntime.sample.simplemarkersymbol;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Map;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.Color;
import com.esri.arcgisruntime.symbology.RgbColor;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;


public class MainActivity extends AppCompatActivity {

    private MapView mMapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // inflate MapView from layout
        mMapView = (MapView) findViewById(R.id.mapView);

        // create a map with the imagery basemap
        Map map = new Map(Basemap.createImagery());

        // create an initial viewpoint with a point and scale
        Point point = new Point(-226773, 6550477, SpatialReferences.getWebMercator());
        Viewpoint vp = new Viewpoint(point, 7500);

        // set initial map extent
        map.setInitialViewpoint(vp);

        // set the map to be displayed in the mapview
        mMapView.setMap(map);

        // create a new graphics overlay and add it to the mapview
        GraphicsOverlay graphicsOverlay = new GraphicsOverlay();
        mMapView.getGraphicsOverlays().add(graphicsOverlay);

        //[DocRef: Name=Point graphic with symbol, Category=Fundamentals, Topic=Symbols and Renderers]
        //create a simple marker symbol
        Color color = new RgbColor(255, 0, 0, 255); //red, fully opaque
        SimpleMarkerSymbol symbol = new SimpleMarkerSymbol(color, 12, SimpleMarkerSymbol.Style.CIRCLE); //size 12, style of circle

        //add a new graphic with a new point geometry
        Point graphicPoint = new Point(-226773, 6550477, SpatialReferences.getWebMercator());
        Graphic graphic = new Graphic(graphicPoint, symbol);
        graphicsOverlay.getGraphics().add(graphic);
        //[DocRef: END]

    }

    @Override
    protected void onPause(){
        super.onPause();
        // pause MapView
        mMapView.pause();
    }

    @Override
    protected void onResume(){
        super.onResume();
        // resume MapView
        mMapView.resume();
    }
}
