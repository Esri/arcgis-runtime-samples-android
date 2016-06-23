package com.esri.arcgisruntime.sample.offlinegeocode;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.TextView;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.datasource.arcgis.TileCache;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.layers.ArcGISTiledLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.Callout;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.PictureMarkerSymbol;
import com.esri.arcgisruntime.tasks.geocode.GeocodeParameters;
import com.esri.arcgisruntime.tasks.geocode.GeocodeResult;
import com.esri.arcgisruntime.tasks.geocode.LocatorTask;
import com.esri.arcgisruntime.tasks.geocode.ReverseGeocodeParameters;

import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "OfflineActivity";
    final String extern = Environment.getExternalStorageDirectory().getPath();
    final String tpkPath = "/ArcGIS/samples/OfflineRouting/SanDiego.tpk";
    final String locatorPath = "/ArcGIS/samples/OfflineRouting/SanDiego_StreetAddress.loc";
    GraphicsOverlay graphicsOverlay;
    GeocodeParameters mGeocodeParameters;
    PictureMarkerSymbol mPinSourceSymbol;
    private MapView mMapView;
    private ArcGISMap mMap;
    private ArcGISTiledLayer tiledLayer;
    private LocatorTask mLocatorTask;
    private ReverseGeocodeParameters mReverseGeocodeParameters;
    private Callout mCallout;
    private float mDownX;
    private float mDownY;
    private final float SCROLL_THRESHOLD = 10;
    private boolean isOnClick = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // inflate MapView from layout
        mMapView = (MapView) findViewById(R.id.mapView);

        // create a basemap from a local tile package
        TileCache tileCache = new TileCache(extern + tpkPath);
        tiledLayer = new ArcGISTiledLayer(tileCache);
        Basemap basemap = new Basemap(tiledLayer);

        // create ArcGISMap with imagery basemap
        mMap = new ArcGISMap(basemap);

        mMapView.setMap(mMap);

        // add a graphics overlay
        graphicsOverlay = new GraphicsOverlay();
        graphicsOverlay.setSelectionColor(0xFF00FFFF);
        mMapView.getGraphicsOverlays().add(graphicsOverlay);

        /*Point point = new Point(-13042254.715252, 3857970.236806, SpatialReference.create(3857));
        Viewpoint vp = new Viewpoint(point,200000);
        mMapView.setViewpoint(vp);*/

        mMap.addDoneLoadingListener(new Runnable() {
            @Override
            public void run() {

                Point p = new Point(-117.047710, 32.624837, SpatialReference.create(4326));
                Viewpoint vp = new Viewpoint(p, 20000);
                mMapView.setViewpointAsync(vp);

            }
        });

        mGeocodeParameters = new GeocodeParameters();
        //mGeocodeParameters.setOutputSpatialReference(mMap.getSpatialReference());
        mGeocodeParameters.getResultAttributeNames().add("*");
        mGeocodeParameters.setMaxResults(1);

        //[DocRef: Name=Picture Marker Symbol Drawable-android, Category=Fundamentals, Topic=Symbols and Renderers]
        //Create a picture marker symbol from an app resource
        BitmapDrawable startDrawable = (BitmapDrawable) ContextCompat.getDrawable(this, R.drawable.pin);
        mPinSourceSymbol = new PictureMarkerSymbol(startDrawable);
        mPinSourceSymbol.loadAsync();

        mReverseGeocodeParameters = new ReverseGeocodeParameters();
        mReverseGeocodeParameters.getResultAttributeNames().add("*");
        mReverseGeocodeParameters.setOutputSpatialReference(mMap.getSpatialReference());
        mReverseGeocodeParameters.setMaxResults(1);

        mLocatorTask = new LocatorTask(extern + locatorPath);

        mMapView.setOnTouchListener(new MapTouchListener(getApplicationContext(),mMapView));

        //setUpSearchView();




    }

    private class MapTouchListener extends DefaultMapViewOnTouchListener {

        public MapTouchListener(Context context, MapView mapView) {
            super(context, mapView);
        }

        @Override
        public boolean onDoubleTouchDrag(MotionEvent motionEvent) {
            // get the point that was clicked and convert it to a point in map coordinates
            android.graphics.Point screenPoint = new android.graphics.Point(Math.round(motionEvent.getX()),
                    Math.round(motionEvent.getY()));

            Point singleTapPoint = mMapView.screenToLocation(screenPoint);

            ListenableFuture<List<GeocodeResult>> results = mLocatorTask.reverseGeocodeAsync(singleTapPoint,
                    mReverseGeocodeParameters);
            results.addDoneListener(new ResultsLoadedListener(results));

            return true;

            //return true;
        }
    }



    /**
     * Updates marker and callout when new results are loaded.
     */
    private class ResultsLoadedListener implements Runnable {

        private final ListenableFuture<List<GeocodeResult>> results;

        /**
         * Constructs a runnable listener for the geocode results.
         *
         * @param results results from a {@link LocatorTask#geocodeAsync} task
         */
        ResultsLoadedListener(ListenableFuture<List<GeocodeResult>> results) {
            this.results = results;
        }

        @Override
        public void run() {

            try {
                List<GeocodeResult> geocodes = results.get();
                if (geocodes.size() > 0) {
                    // get the top result
                    GeocodeResult geocode = geocodes.get(0);

                    // set the viewpoint to the marker
                    Point location = geocode.getDisplayLocation();
                    Log.d(TAG,"here");
                    Log.d(TAG,location.toString());
                    Log.d(TAG,geocode.getRouteLocation().toJson());
                    Log.d(TAG,location.getX() + " " + location.getY() + " " + location.getZ() + " " + location.getM());
                    //mMapView.setViewpointCenterWithScaleAsync(location, 100000);

                    // get attributes from the result for the callout
                    String title;
                    String detail;
                    Object matchAddr = geocode.getAttributes().get("Match_addr");
                    if (matchAddr != null) {
                        // attributes from a query-based search
                        title = matchAddr.toString().split(",")[0];
                        detail = matchAddr.toString().substring(matchAddr.toString().indexOf(",") + 1);
                    } else {
                        // attributes from a click-based search
                        String street = geocode.getAttributes().get("Street").toString();
                        String city = geocode.getAttributes().get("City").toString();
                        String state = geocode.getAttributes().get("State").toString();
                        String zip = geocode.getAttributes().get("ZIP").toString();
                        title = street;
                        detail = city + ", " + state + " " + zip;
                    }

                    // get attributes from the result for the callout
                    HashMap<String, Object> attributes = new HashMap<>();
                    attributes.put("title", title);
                    attributes.put("detail", detail);

                    //Log.d(TAG, title + " " + detail);

                    // create the marker
                    Graphic marker = new Graphic(geocode.getDisplayLocation(), attributes, mPinSourceSymbol);
                    graphicsOverlay.getGraphics().clear();

                    // add the markers to the graphics overlay
                    graphicsOverlay.getGraphics().add(marker);


                    TextView calloutContent = new TextView(getApplicationContext());
                    calloutContent.setTextColor(Color.BLACK);
                    calloutContent.setText(title + ", " + detail);

                    // get callout, set content and show
                    mCallout = mMapView.getCallout();
                    mCallout.setLocation(geocode.getDisplayLocation());
                    mCallout.setContent(calloutContent);

                    mCallout.show();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}
