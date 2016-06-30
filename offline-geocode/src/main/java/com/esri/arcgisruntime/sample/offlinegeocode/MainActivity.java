package com.esri.arcgisruntime.sample.offlinegeocode;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.MatrixCursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.datasource.arcgis.TileCache;
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
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.PictureMarkerSymbol;
import com.esri.arcgisruntime.tasks.geocode.GeocodeParameters;
import com.esri.arcgisruntime.tasks.geocode.GeocodeResult;
import com.esri.arcgisruntime.tasks.geocode.LocatorTask;
import com.esri.arcgisruntime.tasks.geocode.ReverseGeocodeParameters;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private static final String SEARCH_HINT = "Search";
    private static final String TAG = "OfflineActivity";
    private static final String COLUMN_NAME_ADDRESS = "address";
    private static final String COLUMN_NAME_X = "x";
    private static final String COLUMN_NAME_Y = "y";
    final String extern = Environment.getExternalStorageDirectory().getPath();
    GraphicsOverlay graphicsOverlay;
    GeocodeParameters mGeocodeParameters;
    PictureMarkerSymbol mPinSourceSymbol;
    ArcGISMap mMap;
    ArcGISTiledLayer tiledLayer;
    private MapView mMapView;
    private LocatorTask mLocatorTask;
    private ReverseGeocodeParameters mReverseGeocodeParameters;
    private Callout mCallout;
    private SearchView mSearchview;
    private String mGraphicPointAddress;
    private Point mGraphicPoint;
    private MatrixCursor mSuggestionCursor;
    private GeocodeResult mGeocodedLocation;
    private String[] recent = {
            "1455 Market St, San Francisco, CA 94103", "2011 Mission St, San Francisco  CA  94110",
            "820 Bryant St, San Francisco  CA  94103", "1 Zoo Rd, San Francisco, 944132",
            "1201 Mason Street, San Francisco, CA 94108", "151 Third Street, San Francisco, CA 94103",
            "1050 Lombard Street, San Francisco, CA 94109"
    };
    int requestCode = 2;
    String[] permission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

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
        }

        mMapView.setOnTouchListener(new MapTouchListener(getApplicationContext(), mMapView));

        setUpSearchView();


    }

    private void setUpOfflineMapGeocoding() {
        // create a basemap from a local tile package
        TileCache tileCache = new TileCache(extern + getResources().getString(R.string.sanfrancisco_tpk));
        tiledLayer = new ArcGISTiledLayer(tileCache);
        Basemap basemap = new Basemap(tiledLayer);

        // create ArcGISMap with imagery basemap
        mMap = new ArcGISMap(basemap);

        mMapView.setMap(mMap);


        Point p = new Point(-122.41730573536672, 37.772537383913132, SpatialReference.create(4326));
        Viewpoint vp = new Viewpoint(p, 20000);
        mMapView.setViewpointAsync(vp);

        // add a graphics overlay
        graphicsOverlay = new GraphicsOverlay();
        graphicsOverlay.setSelectionColor(0xFF00FFFF);
        mMapView.getGraphicsOverlays().add(graphicsOverlay);


        mGeocodeParameters = new GeocodeParameters();
        mGeocodeParameters.getResultAttributeNames().add("*");
        mGeocodeParameters.setMaxResults(5);

        //[DocRef: Name=Picture Marker Symbol Drawable-android, Category=Fundamentals, Topic=Symbols and Renderers]
        //Create a picture marker symbol from an app resource
        BitmapDrawable startDrawable = (BitmapDrawable) ContextCompat.getDrawable(this, R.drawable.pin);
        mPinSourceSymbol = new PictureMarkerSymbol(startDrawable);
        mPinSourceSymbol.loadAsync();

        mReverseGeocodeParameters = new ReverseGeocodeParameters();
        mReverseGeocodeParameters.getResultAttributeNames().add("*");
        mReverseGeocodeParameters.setOutputSpatialReference(mMap.getSpatialReference());
        mReverseGeocodeParameters.setMaxResults(1);

        mLocatorTask = new LocatorTask(extern + getResources().getString(R.string.sanfrancisco_loc));
    }

    private void setUpSearchView() {


        ViewGroup viewGroup = (ViewGroup) findViewById(R.id.relativeLayout);
        mSearchview = new SearchView(MainActivity.this);
        mSearchview.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorGrey));
        mSearchview.setIconifiedByDefault(false);
        mSearchview.setQueryHint(SEARCH_HINT);
        viewGroup.addView(mSearchview);


        applySuggestionCursor();

        try {
            // Setup the listener when the search button is pressed on the keyboard
            mSearchview.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

                @Override
                public boolean onQueryTextSubmit(String query) {
                    hideKeyboard();
                    geoCodeTypedAddress(query);
                    mSearchview.clearFocus();
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    initSuggestionCursor();
                    applySuggestionCursor();
                    return true;
                }
            });

            mSearchview.setOnSuggestionListener(new SearchView.OnSuggestionListener() {

                @Override
                public boolean onSuggestionSelect(int position) {
                    return false;
                }

                @Override
                public boolean onSuggestionClick(int position) {
                    // Obtain the content of the selected suggesting place via
                    // cursor
                    mSearchview.clearFocus();
                    MatrixCursor cursor = (MatrixCursor) mSearchview.getSuggestionsAdapter().getItem(position);
                    int indexColumnSuggestion = cursor.getColumnIndex(COLUMN_NAME_ADDRESS);
                    final String address = cursor.getString(indexColumnSuggestion);
                    //suggestionClickFlag = true;
                    // Find the Location of the suggestion
                    geoCodeTypedAddress(address);
                    hideKeyboard();
                    mSearchview.setQuery(address, false);
                    cursor.close();

                    return true;
                }
            });


        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
            e.printStackTrace();
        }


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

                            } catch (InterruptedException e) {
                                // Deal with exception...
                                e.printStackTrace();
                                Toast.makeText(getApplicationContext(),
                                        getString(R.string.geo_locate_error),
                                        Toast.LENGTH_LONG).show();

                            } catch (ExecutionException e) {
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
    protected void hideKeyboard() {
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
        // create marker symbol to represent location
        Bitmap icon = BitmapFactory.decodeResource(getApplication().getResources(), R.drawable.pin);
        BitmapDrawable drawable = new BitmapDrawable(getApplication().getResources(), icon);
        PictureMarkerSymbol resultSymbol = new PictureMarkerSymbol(drawable);
        // create graphic object for resulting location
        Graphic resultLocGraphic = new Graphic(resultPoint, resultSymbol);
        // add graphic to location layer
        graphicsOverlay.getGraphics().add(resultLocGraphic);

        // Zoom map to geocode result location
        mMapView.setViewpointCenterAsync(resultPoint);

        mGraphicPoint = resultPoint;
        mGraphicPointAddress = address;
    }

    /**
     * Initialize Suggestion Cursor
     */
    private void initSuggestionCursor() {
        String[] cols = {BaseColumns._ID, COLUMN_NAME_ADDRESS, COLUMN_NAME_X, COLUMN_NAME_Y};
        mSuggestionCursor = new MatrixCursor(cols);

        int key = 0;
        for (String s : recent) {
            mSuggestionCursor.addRow(new Object[]{key++, s, "0", "0"});
        }
    }

    /**
     * Set the suggestion cursor to an Adapter then set it to the search view
     */
    private void applySuggestionCursor() {
        String[] cols = {COLUMN_NAME_ADDRESS};
        int[] to = {R.id.suggestion_item_address};
        SimpleCursorAdapter mSuggestionAdapter = new SimpleCursorAdapter(mMapView.getContext(),
                R.layout.search_suggestion_item, mSuggestionCursor, cols, to, 0);
        mSearchview.setSuggestionsAdapter(mSuggestionAdapter);
        mSuggestionAdapter.notifyDataSetChanged();
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

        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {


            if (mMapView.getCallout().isShowing()) {
                mMapView.getCallout().dismiss();
            }
            // get the screen point where user tapped
            final android.graphics.Point screenPoint = new android.graphics.Point((int) e.getX(), (int) e.getY());

            // identify graphics on the graphics overlay
            final ListenableFuture<List<Graphic>> identifyGraphic = mMapView.identifyGraphicsOverlayAsync(graphicsOverlay, screenPoint, 1.0, 1);

            identifyGraphic.addDoneListener(new Runnable() {
                @Override
                public void run() {
                    try {
                        // get the list of graphics returned by identify
                        List<Graphic> graphic = identifyGraphic.get();
                        // get size of list in results
                        int identifyResultSize = graphic.size();
                        if (!graphic.isEmpty()) {
                            // show a toast message if graphic was returned
                            TextView calloutContent = new TextView(getApplicationContext());
                            calloutContent.setTextColor(Color.BLACK);
                            calloutContent.setText(mGraphicPointAddress);
                            calloutContent.setTextIsSelectable(true);
                            // get callout, set content and show
                            mCallout = mMapView.getCallout();
                            mCallout.setContent(calloutContent);
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

                    //Log.d(TAG, title + " " + detail);

                    // create the marker
                    Graphic marker = new Graphic(geocode.getDisplayLocation(), attributes, mPinSourceSymbol);
                    graphicsOverlay.getGraphics().clear();

                    // add the markers to the graphics overlay
                    graphicsOverlay.getGraphics().add(marker);

                    TextView calloutContent = new TextView(getApplicationContext());
                    calloutContent.setTextColor(Color.BLACK);
                    String calloutText = title + ", " + detail;
                    calloutContent.setText(calloutText);
                    calloutContent.setTextIsSelectable(true);
                    // get callout, set content and show
                    mCallout = mMapView.getCallout();
                    mCallout.setLocation(geocode.getDisplayLocation());
                    mCallout.setContent(calloutContent);
                    mCallout.show();

                    mGraphicPoint = location;
                    mGraphicPointAddress = title + ", " + detail;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // If request is cancelled, the result arrays are empty.
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Location permission was granted. This would have been triggered in response to failing to start the
            // LocationDisplay, so try starting this again.
            setUpOfflineMapGeocoding();
        } else {
            // If permission was denied, show toast to inform user what was chosen. If LocationDisplay is started again,
            // request permission UX will be shown again, option should be shown to allow never showing the UX again.
            // Alternative would be to disable functionality so request is not shown again.
            Toast.makeText(MainActivity.this, getResources().getString(R.string.storage_permission_denied), Toast
                    .LENGTH_SHORT).show();

        }
    }

}
