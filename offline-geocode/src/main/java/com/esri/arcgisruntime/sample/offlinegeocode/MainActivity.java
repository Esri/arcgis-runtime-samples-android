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

package com.esri.arcgisruntime.sample.offlinegeocode;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.TileCache;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.layers.ArcGISTiledLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.Callout;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.IdentifyGraphicsOverlayResult;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.PictureMarkerSymbol;
import com.esri.arcgisruntime.tasks.geocode.GeocodeParameters;
import com.esri.arcgisruntime.tasks.geocode.GeocodeResult;
import com.esri.arcgisruntime.tasks.geocode.LocatorTask;
import com.esri.arcgisruntime.tasks.geocode.ReverseGeocodeParameters;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "OfflineActivity";
    private final String extern = Environment.getExternalStorageDirectory().getPath();
    final int requestCode = 2;
    final String[] permission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private GraphicsOverlay graphicsOverlay;
    private GeocodeParameters mGeocodeParameters;
    private PictureMarkerSymbol mPinSourceSymbol;
    ArcGISMap mMap;
    ArcGISTiledLayer tiledLayer;
    private MapView mMapView;
    private LocatorTask mLocatorTask;
    private ReverseGeocodeParameters mReverseGeocodeParameters;
    private Callout mCallout;
    private SearchView mSearchview;
    private String mGraphicPointAddress;
    private Point mGraphicPoint;
    private GeocodeResult mGeocodedLocation;
    Spinner mSpinner;
    private boolean isPinSelected;
    private TextView mCalloutContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // inflate MapView from layout
        mMapView = (MapView) findViewById(R.id.mapView);

        // Check permissions to see if failure may be due to lack of permissions.
        boolean permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, permission[0]) ==
                PackageManager.PERMISSION_GRANTED;

        if (!permissionCheck) {
            // If permissions are not already granted, request permission from the user.
            ActivityCompat.requestPermissions(MainActivity.this, permission, requestCode);
        } else { // if permission was already granted, set up offline map and geocoding, reverse geocoding and LocatorTask
            setUpOfflineMapGeocoding();
            setSearchView();
        }
        mMapView.setOnTouchListener(new MapTouchListener(getApplicationContext(), mMapView));
    }

    private void setSearchView() {


        mSearchview = (SearchView) findViewById(R.id.searchView1);
        mSearchview.setIconifiedByDefault(true);
        mSearchview.setQueryHint(getResources().getString(R.string.search_hint));
        mSearchview.setOnQueryTextListener(new OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                hideKeyboard();
                geoCodeTypedAddress(query);
                mSearchview.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        mSpinner = (Spinner) findViewById(R.id.spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        final ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_dropdown_item) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                View v = super.getView(position, convertView, parent);
                if (position == getCount()) {
                    mSearchview.clearFocus();
                }

                return v;
            }

            @Override
            public int getCount() {
                return super.getCount() - 1; // you dont display last item. It is used as hint.
            }

        };

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter.addAll(getResources().getStringArray(R.array.suggestion_items));

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            // set vertical offset to spinner dropdown for API less than 21
            mSpinner.setDropDownVerticalOffset(80);
        }
        // Apply the adapter to the spinner
        mSpinner.setAdapter(adapter);
        mSpinner.setSelection(adapter.getCount());


        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == adapter.getCount()) {
                    mSearchview.clearFocus();
                } else {
                    hideKeyboard();
                    mSearchview.setQuery(getResources().getStringArray(R.array.suggestion_items)[position], false);
                    geoCodeTypedAddress(getResources().getStringArray(R.array.suggestion_items)[position]);
                    mSearchview.setIconified(false);
                    mSearchview.clearFocus();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

    }

    private void setUpOfflineMapGeocoding() {
        // create a basemap from a local tile package
        TileCache tileCache = new TileCache(extern + getResources().getString(R.string.sandiego_tpk));
        tiledLayer = new ArcGISTiledLayer(tileCache);
        Basemap basemap = new Basemap(tiledLayer);

        // create ArcGISMap with imagery basemap
        mMap = new ArcGISMap(basemap);

        mMapView.setMap(mMap);

        mMap.addDoneLoadingListener(new Runnable() {
            @Override
            public void run() {
                Point p = new Point(-117.162040, 32.718260, SpatialReference.create(4326));
                Viewpoint vp = new Viewpoint(p, 10000);
                mMapView.setViewpointAsync(vp, 3);
            }
        });


        // add a graphics overlay
        graphicsOverlay = new GraphicsOverlay();
        graphicsOverlay.setSelectionColor(0xFF00FFFF);
        mMapView.getGraphicsOverlays().add(graphicsOverlay);


        mGeocodeParameters = new GeocodeParameters();
        mGeocodeParameters.getResultAttributeNames().add("*");
        mGeocodeParameters.setMaxResults(1);

        //[DocRef: Name=Picture Marker Symbol Drawable-android, Category=Fundamentals, Topic=Symbols and Renderers]
        //Create a picture marker symbol from an app resource
        BitmapDrawable startDrawable = (BitmapDrawable) ContextCompat.getDrawable(this, R.drawable.pin);
        mPinSourceSymbol = new PictureMarkerSymbol(startDrawable);
        mPinSourceSymbol.setHeight(90);
        mPinSourceSymbol.setWidth(20);
        mPinSourceSymbol.loadAsync();
        mPinSourceSymbol.setLeaderOffsetY(45);
        mPinSourceSymbol.setOffsetY(-48);

        mReverseGeocodeParameters = new ReverseGeocodeParameters();
        mReverseGeocodeParameters.getResultAttributeNames().add("*");
        mReverseGeocodeParameters.setOutputSpatialReference(mMap.getSpatialReference());
        mReverseGeocodeParameters.setMaxResults(1);

        mLocatorTask = new LocatorTask(extern + getResources().getString(R.string.sandiego_loc));

        mCalloutContent = new TextView(getApplicationContext());
        mCalloutContent.setTextColor(Color.BLACK);
        mCalloutContent.setTextIsSelectable(true);
    }

    /**
     * Geocode an address typed in by user
     *
     * @param address
     */
    private void geoCodeTypedAddress(final String address) {
        // Null out any previously located result
        mGeocodedLocation = null;

        // Execute async task to find the address
        mLocatorTask.addDoneLoadingListener(new Runnable() {
            @Override
            public void run() {
                if (mLocatorTask.getLoadStatus() == LoadStatus.LOADED) {
                    // Call geocodeAsync passing in an address
                    final ListenableFuture<List<GeocodeResult>> geocodeFuture = mLocatorTask.geocodeAsync(address,
                            mGeocodeParameters);
                    geocodeFuture.addDoneListener(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                // Get the results of the async operation
                                List<GeocodeResult> geocodeResults = geocodeFuture.get();

                                if (geocodeResults.size() > 0) {
                                    // Use the first result - for example
                                    // display on the map
                                    mGeocodedLocation = geocodeResults.get(0);
                                    displaySearchResult(mGeocodedLocation.getDisplayLocation(), mGeocodedLocation.getLabel());

                                } else {
                                    Toast.makeText(getApplicationContext(),
                                            getString(R.string.location_not_foud) + address,
                                            Toast.LENGTH_LONG).show();
                                }

                            } catch (InterruptedException | ExecutionException e) {
                                // Deal with exception...
                                e.printStackTrace();
                                Toast.makeText(getApplicationContext(),
                                        getString(R.string.geo_locate_error),
                                        Toast.LENGTH_LONG).show();

                            }
                            // Done processing and can remove this listener.
                            geocodeFuture.removeDoneListener(this);
                        }
                    });

                } else {
                    Log.i(TAG, "Trying to reload locator task");
                    mLocatorTask.retryLoadAsync();
                }
            }
        });
        mLocatorTask.loadAsync();
    }

    /**
     * Hides soft keyboard
     */
    private void hideKeyboard() {
        mSearchview.clearFocus();
        InputMethodManager inputManager = (InputMethodManager) getApplicationContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(mSearchview.getWindowToken(), 0);
    }

    private void displaySearchResult(Point resultPoint, String address) {


        if (mMapView.getCallout().isShowing()) {
            mMapView.getCallout().dismiss();
        }
        //remove any previous graphics/search results
        //mMapView.getGraphicsOverlays().clear();
        graphicsOverlay.getGraphics().clear();
        // create graphic object for resulting location
        Graphic resultLocGraphic = new Graphic(resultPoint, mPinSourceSymbol);
        // add graphic to location layer
        graphicsOverlay.getGraphics().add(resultLocGraphic);

        // Zoom map to geocode result location
        mMapView.setViewpointAsync(new Viewpoint(resultPoint, 8000), 3);

        mGraphicPoint = resultPoint;
        mGraphicPointAddress = address;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // If request is cancelled, the result arrays are empty.
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Location permission was granted. This would have been triggered in response to failing to start the
            // LocationDisplay, so try starting this again.
            setUpOfflineMapGeocoding();
            setSearchView();
        } else {
            // If permission was denied, show toast to inform user what was chosen. If LocationDisplay is started again,
            // request permission UX will be shown again, option should be shown to allow never showing the UX again.
            // Alternative would be to disable functionality so request is not shown again.
            Toast.makeText(MainActivity.this, getResources().getString(R.string.storage_permission_denied), Toast
                    .LENGTH_SHORT).show();

        }
    }

    private class DragTouchListener extends DefaultMapViewOnTouchListener {

        float dX, dY;

        public DragTouchListener(Context context, MapView mapView) {
            super(context, mapView);
        }

        @Override
        public boolean onTouch(View view, MotionEvent event) {

            switch (event.getAction()) {

                case MotionEvent.ACTION_DOWN:
                    dX = view.getX() - event.getRawX();
                    dY = view.getY() - event.getRawY();
                    break;

                case MotionEvent.ACTION_MOVE:
                    final int pointerIndex = MotionEventCompat.getActionIndex(event);
                    final float x = MotionEventCompat.getX(event, pointerIndex);
                    final float y = MotionEventCompat.getY(event, pointerIndex);
                    android.graphics.Point screenPoint = new android.graphics.Point(Math.round(x), Math.round(y));
                    final Point singleTapPoint = mMapView.screenToLocation(screenPoint);
                    final ListenableFuture<List<GeocodeResult>> results = mLocatorTask.reverseGeocodeAsync(singleTapPoint,
                            mReverseGeocodeParameters);
                    graphicsOverlay.getGraphics().clear();
                    Graphic resultLocGraphic = new Graphic(singleTapPoint, mPinSourceSymbol);
                    resultLocGraphic.setSelected(true);
                    // add graphic to location layer
                    graphicsOverlay.getGraphics().add(resultLocGraphic);
                    // display callout with reverse-geocode result on UI thread
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                List<GeocodeResult> geocodes = results.get();
                                if (geocodes.size() > 0) {
                                    // get the top result
                                    GeocodeResult geocode = geocodes.get(0);
                                    String detail;
                                    // attributes from a click-based search
                                    String street = geocode.getAttributes().get("Street").toString();
                                    String city = geocode.getAttributes().get("City").toString();
                                    String state = geocode.getAttributes().get("State").toString();
                                    String zip = geocode.getAttributes().get("ZIP").toString();
                                    detail = city + ", " + state + " " + zip;

                                    String address = street + "," + detail;
                                    mCalloutContent.setText(address);
                                    // get callout, set content and show
                                    mCallout = mMapView.getCallout();
                                    mCallout.setLocation(singleTapPoint);
                                    mCallout.setContent(mCalloutContent);
                                    mCallout.show();

                                    mGraphicPoint = singleTapPoint;
                                    mGraphicPointAddress = address;
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });


                    break;
                case MotionEvent.ACTION_UP:
                    if (graphicsOverlay.getGraphics().size() > 0) {
                        graphicsOverlay.getGraphics().get(0).setSelected(false);
                        isPinSelected = false;
                        mMapView.setOnTouchListener(new MapTouchListener(getApplicationContext(), mMapView));
                    }
                    break;
                default:
                    return false;
            }
            return true;
        }
    }

    private class MapTouchListener extends DefaultMapViewOnTouchListener {

        public MapTouchListener(Context context, MapView mapView) {
            super(context, mapView);
        }

        @Override
        public void onLongPress(MotionEvent e) {
            android.graphics.Point screenPoint = new android.graphics.Point(Math.round(e.getX()),
                    Math.round(e.getY()));

            Point longPressPoint = mMapView.screenToLocation(screenPoint);

            ListenableFuture<List<GeocodeResult>> results = mLocatorTask.reverseGeocodeAsync(longPressPoint,
                    mReverseGeocodeParameters);
            results.addDoneListener(new ResultsLoadedListener(results));

        }


        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {


            if (mMapView.getCallout().isShowing()) {
                mMapView.getCallout().dismiss();
            }
            if (graphicsOverlay.getGraphics().size() > 0) {
                if (graphicsOverlay.getGraphics().get(0).isSelected()) {
                    isPinSelected = false;
                    graphicsOverlay.getGraphics().get(0).setSelected(false);
                }
            }
            // get the screen point where user tapped
            final android.graphics.Point screenPoint = new android.graphics.Point((int) e.getX(), (int) e.getY());

            // identify graphics on the graphics overlay
            final ListenableFuture<IdentifyGraphicsOverlayResult> identifyGraphic = mMapView.identifyGraphicsOverlayAsync(graphicsOverlay, screenPoint, 1.0, false, 1);

            identifyGraphic.addDoneListener(new Runnable() {
                @Override
                public void run() {
                    try {
                        IdentifyGraphicsOverlayResult grOverlayResult = identifyGraphic.get();
                        // get the list of graphics returned by identify
                        List<Graphic> graphic = grOverlayResult.getGraphics();
                        // if identified graphic is not empty, start DragTouchListener
                        if (!graphic.isEmpty()) {

                            if (!isPinSelected) {
                                isPinSelected = true;
                                graphic.get(0).setSelected(true);
                                Toast.makeText(getApplicationContext(),
                                        getString(R.string.reverse_geocode_message),
                                        Toast.LENGTH_SHORT).show();
                                mMapView.setOnTouchListener(new DragTouchListener(getApplicationContext(), mMapView));
                            }

                            mCalloutContent.setText(mGraphicPointAddress);
                            // get callout, set content and show
                            mCallout = mMapView.getCallout();
                            mCallout.setContent(mCalloutContent);
                            mCallout.setLocation(mGraphicPoint);
                            mCallout.show();
                        }
                    } catch (InterruptedException | ExecutionException ie) {
                        ie.printStackTrace();
                    }

                }
            });

            return super.onSingleTapConfirmed(e);
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


                    // create the marker
                    Graphic marker = new Graphic(geocode.getDisplayLocation(), attributes, mPinSourceSymbol);
                    graphicsOverlay.getGraphics().clear();

                    // add the markers to the graphics overlay
                    graphicsOverlay.getGraphics().add(marker);

                    if (isPinSelected) {
                        marker.setSelected(true);
                    }
                    String calloutText = title + ", " + detail;
                    mCalloutContent.setText(calloutText);
                    // get callout, set content and show
                    mCallout = mMapView.getCallout();
                    mCallout.setLocation(geocode.getDisplayLocation());
                    mCallout.setContent(mCalloutContent);
                    mCallout.show();

                    mGraphicPoint = location;
                    mGraphicPointAddress = title + ", " + detail;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}
