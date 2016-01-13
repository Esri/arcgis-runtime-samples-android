/* Copyright 2015 Esri
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

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.Polygon;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Map;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.mapping.view.VisibleAreaChangedEvent;
import com.esri.arcgisruntime.mapping.view.VisibleAreaChangedListener;
import com.esri.arcgisruntime.symbology.Color;
import com.esri.arcgisruntime.symbology.RgbColor;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.esri.arcgisruntime.symbology.SimpleRenderer;
import com.esri.arcgisruntime.util.ListenableList;

public class MainActivity extends AppCompatActivity {

    private MapView mMapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // inflate MapView from layout
        mMapView = (MapView) findViewById(R.id.mapView);

        // create a map with the Basemap Type topographic
        Map mMap = new Map(Basemap.Type.TOPOGRAPHIC, 34.056295, -117.195800, 14);
        // set the map to be displayed in this view
        mMapView.setMap(mMap);

        // work with the MapView after it has loaded
        mMapView.addVisibleAreaChangedListener(new VisibleAreaChangedListener() {
            @Override
            public void visibleAreaChanged(VisibleAreaChangedEvent visibleAreaChangedEvent) {
                //remove the listener so it's only called once
                mMapView.removeVisibleAreaChangedListener(this);
                // add graphics overlay
                addGraphicsOverlay();
            }
        });
    }

    private void addGraphicsOverlay(){
        // get center of MapView
        Polygon visibleArea = mMapView.getVisibleArea();
        Envelope polygonExtent = visibleArea.getExtent();
        Point center = polygonExtent.getCenter();

        // create values inside the visible area extent for creating graphic
        double xValue = mMapView.getVisibleArea().getExtent().getWidth() / 5;
        double yValue = mMapView.getVisibleArea().getExtent().getHeight() / 10;

        // create point collection
        PointCollection polyPoints = new PointCollection(mMapView.getSpatialReference());
        polyPoints.add(new Point(center.getX() - xValue * 2, center.getY() - yValue * 2));
        polyPoints.add(new Point(center.getX() - xValue * 2, center.getY() + yValue * 2));
        polyPoints.add(new Point(center.getX() + xValue * 2, center.getY() + yValue * 2));
        polyPoints.add(new Point(center.getX() + xValue * 2, center.getY() - yValue * 2));

        // create graphics overlay
        GraphicsOverlay grOverlay = new GraphicsOverlay();

        // create list of graphics
        ListenableList<Graphic> graphics = grOverlay.getGraphics();

        // add points from PointCollection to graphics list
        for(Point pt : polyPoints){
            // add graphic to graphics overlay
            graphics.add(new Graphic(pt));
        }
        // create color for graphic
        Color yellow = new RgbColor(255, 255, 0, 127);
        // create simple renderer
        SimpleRenderer simpleRenderer = new SimpleRenderer();
        // create point symbol
        SimpleMarkerSymbol pointSymbol = new SimpleMarkerSymbol(yellow, 30, SimpleMarkerSymbol.Style.SQUARE);
        // set symbol to renderer
        simpleRenderer.setSymbol(pointSymbol);
        // set renderer to graphics overlay
        grOverlay.setRenderer(simpleRenderer);

        // add graphics overlay to the MapView
        mMapView.getGraphicsOverlays().add(grOverlay);
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
