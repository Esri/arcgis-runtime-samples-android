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

package com.esri.arcgisruntime.sample.identifygraphicoverlay;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.Toast;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.Polygon;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Map;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.DrawStatus;
import com.esri.arcgisruntime.mapping.view.DrawStatusChangedEvent;
import com.esri.arcgisruntime.mapping.view.DrawStatusChangedListener;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.util.ListenableList;


import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private MapView mMapView;
    private GraphicsOverlay grOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // inflate MapView from layout
        mMapView = (MapView) findViewById(R.id.mapView);

        // create a map with the BasemapType topographic
        Map mMap = new Map(Basemap.Type.TOPOGRAPHIC, 34.056295, -117.195800, 14);
        // set the map to be displayed in this view
        mMapView.setMap(mMap);

        // set up gesture for interacting with the MapView
        MapViewTouchListener mMapViewTouchListener = new MapViewTouchListener(this, mMapView);
        mMapView.setOnTouchListener(mMapViewTouchListener);

        // work with the MapView after it has loaded
        mMapView.addDrawStatusChangedListener(new DrawStatusChangedListener() {

            @Override
            public void drawStatusChanged(DrawStatusChangedEvent drawStatusChangedEvent) {
                Log.i(TAG, "drawStatusChanged: " + drawStatusChangedEvent.getDrawStatus().toString());

                if (drawStatusChangedEvent.getDrawStatus() == DrawStatus.COMPLETED) {
                    // get center of MapView
                    Polygon visibleArea = mMapView.getVisibleArea();
                    Envelope polygonExtent = visibleArea.getExtent();
                    Point center = polygonExtent.getCenter();
                    // pass center of MapView to add graphic overlay
                    addGraphicsOverlay(center);
                }

            }
        });
    }

    private void addGraphicsOverlay(Point center) {
        // create values inside the visible area extent for creating graphic
        double xValue = mMapView.getVisibleArea().getExtent().getWidth() / 5;
        double yValue = mMapView.getVisibleArea().getExtent().getHeight() / 10;

        // create point collection
        PointCollection polyPoints = new PointCollection(mMapView.getSpatialReference());
        polyPoints.add(new Point(center.getX() - xValue * 2, center.getY() - yValue * 2));
        polyPoints.add(new Point(center.getX() - xValue * 2, center.getY() + yValue * 2));
        polyPoints.add(new Point(center.getX() + xValue * 2, center.getY() + yValue * 2));
        polyPoints.add(new Point(center.getX() + xValue * 2, center.getY() - yValue * 2));
        // create polygon
        Polygon polygon = new Polygon(polyPoints);

        // create solid line symbol
        int blue = Color.rgb(0, 0, 230);
        SimpleLineSymbol solidLine = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, blue, 4);
        // create graphic from polygon and symbol
        Graphic graphic = new Graphic(polygon, solidLine);

        // create graphics overlay
        grOverlay = new GraphicsOverlay();
        // create list of graphics
        ListenableList<Graphic> graphics = grOverlay.getGraphics();
        // add graphic to graphics overlay
        graphics.add(graphic);
        // add graphics overlay to the MapView
        mMapView.getGraphicsOverlays().add(grOverlay);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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

    /**
     * Override default gestures of the MapView
     */
    class MapViewTouchListener extends DefaultMapViewOnTouchListener{

        /**
         * Constructs a DefaultMapViewOnTouchListener with the specified Context and MapView.
         *
         * @param context the context from which this is being created
         * @param mapView the MapView with which to interact
         */
        public MapViewTouchListener(Context context, MapView mapView){
            super(context, mapView);
        }

        /**
         * Override the onSingleTapConfirmed gesture to handle tapping on the MapView
         * and detected if the Graphic was selected.
         * @param e the motion event
         * @return true if the listener has consumed the event; false otherwise
         */
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            // get the screen point where user tapped
            android.graphics.Point screenPoint = new android.graphics.Point((int)e.getX(), (int)e.getY());

            // identify graphics on the graphics overlay
            final ListenableFuture<List<Graphic>> identifyGraphic = mMapView.identifyGraphicsOverlayAsync(grOverlay, screenPoint, 10.0, 2);

            identifyGraphic.addDoneListener(new Runnable() {
                @Override
                public void run() {
                    try {
                        // get the list of graphics returned by identify
                        List<Graphic> graphic = identifyGraphic.get();
                        // get size of list in results
                        int identifyResultSize = graphic.size();
                        if(!graphic.isEmpty()){
                            // show a toast message if graphic was returned
                            Toast.makeText(getApplicationContext(), "Tapped on " + identifyResultSize + " Graphic", Toast.LENGTH_SHORT).show();
                        }
                    }catch(InterruptedException | ExecutionException ie){
                        ie.printStackTrace();
                    }

                }
            });

            return super.onSingleTapConfirmed(e);
        }

    }
}
