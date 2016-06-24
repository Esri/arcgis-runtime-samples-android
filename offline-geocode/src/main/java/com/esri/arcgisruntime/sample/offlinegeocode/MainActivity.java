package com.esri.arcgisruntime.sample.offlinegeocode;

import android.content.Context;
import android.database.MatrixCursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.provider.BaseColumns;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.FrameLayout.LayoutParams;
import android.widget.Toast;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.datasource.arcgis.TileCache;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.Geometry;
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
import com.esri.arcgisruntime.mapping.view.LocationDisplay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.PictureMarkerSymbol;
import com.esri.arcgisruntime.tasks.geocode.GeocodeParameters;
import com.esri.arcgisruntime.tasks.geocode.GeocodeResult;
import com.esri.arcgisruntime.tasks.geocode.LocatorTask;
import com.esri.arcgisruntime.tasks.geocode.ReverseGeocodeParameters;
import com.esri.arcgisruntime.tasks.geocode.SuggestParameters;
import com.esri.arcgisruntime.tasks.geocode.SuggestResult;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "OfflineActivity";
    final String extern = Environment.getExternalStorageDirectory().getPath();
    final String tpkPath = "/ArcGIS/samples/OfflineRouting/SanDiego.tpk";
    final String locatorPath = "/ArcGIS/samples/OfflineRouting/SanDiego_StreetAddress.lox";
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
    private LayoutInflater mInflater;
    private View mSearchBox;
    private static LayoutParams mLayoutParams;
    private static int TOP_MARGIN_SEARCH = 55;
    private SearchView mSearchview;
    public static final String SEARCH_HINT = "Search";
    private static final String COLUMN_NAME_ADDRESS = "address";

    private static final String COLUMN_NAME_X = "x";

    private static final String COLUMN_NAME_Y = "y";
    private boolean suggestionClickFlag;
    private SuggestParameters suggestParams;
    private MatrixCursor mSuggestionCursor;
    private static List<SuggestResult> mSuggestionsList;
    //private Point mLocation = null;
    private GeocodeResult mGeocodedLocation;
    //private GeocodeParameters mGeocodeParams;
    //private LocationDisplay mLocationDisplay;
    private LocatorTask mLocator;

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

        Point p = new Point(-117.047710, 32.624837, SpatialReference.create(4326));
        Viewpoint vp = new Viewpoint(p, 20000);
        mMapView.setViewpointAsync(vp);

        // add a graphics overlay
        graphicsOverlay = new GraphicsOverlay();
        graphicsOverlay.setSelectionColor(0xFF00FFFF);
        mMapView.getGraphicsOverlays().add(graphicsOverlay);



        // Show current location
       /* mLocationDisplay = mMapView.getLocationDisplay();
        mLocationDisplay.startAsync();
        mLocationDisplay.setAutoPanMode(LocationDisplay.AutoPanMode.RECENTER);
        mLocationDisplay.setInitialZoomScale(50000);

        // Handle any location changes
        mLocationDisplay.addLocationChangedListener(new LocationListener());*/

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


        mMapView.setOnTouchListener(new MapTouchListener(getApplicationContext(), mMapView));
        mInflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // Setting up the layout params for the searchview and searchresult
        // layout
        mLayoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.LEFT | Gravity.TOP);
        int LEFT_MARGIN_SEARCH = 15;
        int RIGHT_MARGIN_SEARCH = 15;
        int BOTTOM_MARGIN_SEARCH = 0;

        mLayoutParams.setMargins(LEFT_MARGIN_SEARCH, TOP_MARGIN_SEARCH, RIGHT_MARGIN_SEARCH, BOTTOM_MARGIN_SEARCH);

       /* // Create find parameters
        mGeocodeParams = new GeocodeParameters();
        // Set max results and spatial reference
        mGeocodeParams.setMaxResults(2);
        mGeocodeParams.setOutputSpatialReference(mMap.getSpatialReference());
        // Use the centre of the current map extent as the location
        //mGeocodeParams.setSearchArea(calculateSearchArea());
        //mGeocodeParams.setPreferredSearchLocation(mLocation);*/

        setUpSearchView();




    }
    /**
     * Listen for location changes and update my location
     */
    /*private class LocationListener implements LocationDisplay.LocationChangedListener {

        @Override
        public void onLocationChanged(LocationDisplay.LocationChangedEvent locationChangedEvent) {
            if (locationChangedEvent.getLocation().getPosition() != null) {
                Log.d(TAG,"LocationListener");
                mLocation = locationChangedEvent.getLocation().getPosition();
            }
        }
    }*/


    private void setUpSearchView() {

        mSearchBox = mInflater.inflate(R.layout.searchview, null);

        mSearchBox.setLayoutParams(mLayoutParams);
        // Initializing the searchview and the image view
        //mSearchview = (SearchView) mSearchBox.findViewById(R.id.searchView1);

        mSearchview = new SearchView(MainActivity.this);

        mSearchview.setIconifiedByDefault(false);
        mSearchview.setQueryHint(SEARCH_HINT);
        RelativeLayout myLayout = (RelativeLayout) findViewById(R.id.relativeLayout);
        //View hiddenInfo = getLayoutInflater().inflate(R.layout.searchview, myLayout, false);
        myLayout.addView(mSearchview);

        /*View suggestionsView = getLayoutInflater().inflate(R.layout.search_suggestion_item, myLayout, false);
        myLayout.addView(suggestionsView);*/

        applySuggestionCursor();

        try {
            //Inflate the Hidden Layout Information View


            // Setup the listener when the search button is pressed on the keyboard
            mSearchview.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

                @Override
                public boolean onQueryTextSubmit(String query) {

                    Log.d(TAG,"onQueryTextSubmit");
                    onSearchButtonClicked(query);
                    mSearchview.clearFocus();
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    /*if (mLocator == null)
                        return false;*/
                    Log.d(TAG,"onQueryTextChange");
                    getSuggestions(newText);
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
                    MatrixCursor cursor = (MatrixCursor) mSearchview.getSuggestionsAdapter().getItem(position);
                    int indexColumnSuggestion = cursor.getColumnIndex(COLUMN_NAME_ADDRESS);
                    final String address = cursor.getString(indexColumnSuggestion);

                    Log.d(TAG,"onSuggestionClick");
                    suggestionClickFlag = true;
                    // Find the Location of the suggestion
                    geoCodeSuggestedLocation(address);

                    cursor.close();

                    return true;
                }
            });


        } catch (Exception e) {
            Log.d(TAG,e.getMessage());
            e.printStackTrace();
        }


    }

    /**
     *
     * Retrieves location for selected suggestion
     * @param address Suggested address user clicked on
     */
    private void geoCodeSuggestedLocation(final String address) {

        Log.d(TAG,"geoCodeSuggestedLocation");

        final String TAG_LOCATOR_PROGRESS_DIALOG = "TAG_LOCATOR_PROGRESS_DIALOG";
        // Display progress dialog on UI thread
        // Null out any previously located result
        mGeocodedLocation = null;

        SuggestResult matchedSuggestion = null;
        // get the Location for the suggestion from the ArrayList
        for (SuggestResult result : mSuggestionsList) {
            // changed from address.matches because addresses with parentheses were throwing off REGEX.
            if (address.equalsIgnoreCase(result.getLabel())) {
                matchedSuggestion = result;
                break;
            }
        }
        if (matchedSuggestion != null) {
            final SuggestResult matchedAddress = matchedSuggestion;
            // Prepare the GeocodeParameters for geocoding the address
            locatorParams();

            final ListenableFuture<List<GeocodeResult>> locFuture = mLocatorTask.geocodeAsync(matchedAddress, mGeocodeParameters);
            // Attach a done listener that executes upon completion of the async call
            locFuture.addDoneListener(new Runnable() {
                @Override
                public void run() {
                    try {

                        Log.d(TAG,"locFuture");
                        List<GeocodeResult> locationResults = locFuture.get();
                        showSuggestedPlace(locationResults, address);
                    } catch (Exception e) {
                        // Notify that there was a problem with geocoding
                        Log.e(TAG, "Geocode error " + e.getMessage());

                        Toast.makeText(getApplicationContext(),
                                getString(R.string.geo_locate_error),
                                Toast.LENGTH_LONG).show();
                    }
                }
            });
        }else{
            // Notify that no matched suggestion was found

            Toast.makeText(getApplicationContext(),
                    getString(R.string.location_not_foud) + " " + address,
                    Toast.LENGTH_LONG).show();
        }

    }

    private void getSuggestions(final String query) {

        if (query == null || query.isEmpty()) {
            return;
        }
        Log.d(TAG,"getSuggestions");

        locatorParams();
        

        mLocatorTask.addDoneLoadingListener(new Runnable() {
            @Override public void run() {
                // Does this locator support suggestions?
                if (mLocatorTask.getLoadStatus().name() != LoadStatus.LOADED.name()){
                    //Log.i(TAG,"##### " + mLocator.getLoadStatus().name());
                } else if (!mLocatorTask.getLocatorInfo().isSupportsSuggestions()){
                    return;
                }
                Log.i(TAG,"****** " + mLocatorTask.getLoadStatus().name());
                final ListenableFuture<List<SuggestResult>> suggestionsFuture = mLocatorTask.suggestAsync(query, suggestParams);
                // Attach a done listener that executes upon completion of the async call
                suggestionsFuture.addDoneListener(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // Get the suggestions returned from the locator task.
                            // Store retrieved suggestions for future use (e.g. if the user
                            // selects a retrieved suggestion, it can easily be
                            // geocoded).
                            Log.d(TAG,"suggestionsFuture");
                            mSuggestionsList = suggestionsFuture.get();

                            showSuggestedPlaceNames(mSuggestionsList);

                        } catch (Exception e) {
                            Log.e(TAG, "Error on getting suggestions " + e.getMessage());
                        }
                    }
                });
            }
        });
        // Initiate the asynchronous call
        mLocatorTask.loadAsync();
    }

    private void showSuggestedPlaceNames(List<SuggestResult> suggestions){
        Log.d(TAG,"showSuggestedPlaceNames");
        if (suggestions == null || suggestions.isEmpty()){
            return;
        }
        initSuggestionCursor();
        int key = 0;
        for (SuggestResult result : suggestions) {
            Log.d("Suggestion:",result.getLabel());
            // Add the suggestion results to the cursor
            mSuggestionCursor.addRow(new Object[]{key++, result.getLabel(), "0", "0"});

        }
        applySuggestionCursor();
    }

    /**
     * Given an address and the geocode results, dismiss
     * progress dialog and keyboard and show the geocoded location.
     * @param locationResults - List of GeocodeResult
     */
    private void showSuggestedPlace(final List<GeocodeResult> locationResults, final String address){

        Log.d(TAG,"showSuggestedPlace");
        Point resultPoint = null;
        String resultAddress = null;
        if (locationResults != null && locationResults.size() > 0) {
            // Get the first returned result
            mGeocodedLocation = locationResults.get(0);
            resultPoint = mGeocodedLocation.getDisplayLocation();
            resultAddress = mGeocodedLocation.getLabel();
        }else{
            Log.i(TAG, "No geocode results found for suggestion");
        }

        if (resultPoint == null){
            Toast.makeText(getApplicationContext(),
                    getString(R.string.location_not_foud) + resultAddress,
                    Toast.LENGTH_LONG).show();
            return;
        }
        // Display the result
        displaySearchResult(resultPoint, address);
        hideKeyboard();
    }

    private void locatorParams() {
        Log.d(TAG,"locatorParams");
        // Create suggestion parameters
        suggestParams = new SuggestParameters();
        suggestParams.setMaxResults(5);
        //suggestParams.setSearchArea(calculateSearchArea());
        //suggestParams.setPreferredSearchLocation(mLocation);
    }

    /**
     * Calculate search geometry given current map extent
     *
     * @return Envelope representing an area double the size of the current map
     * extent
     */
    private Envelope calculateSearchArea() {
        SpatialReference sR = mMapView.getSpatialReference();

        // Get the current map space
        Geometry mapGeometry = mMapView.getCurrentViewpoint(Viewpoint.Type.BOUNDING_GEOMETRY).getTargetGeometry();
        Envelope mapExtent = mapGeometry.getExtent();
        // Calculate distance for find operation

        // assume map is in metres, other units wont work, double current
        // envelope
        double width = mapExtent.getWidth() > 0 ? mapExtent.getWidth() * 2 : 10000;
        double height = mapExtent.getHeight() > 0 ? mapExtent.getHeight() * 2 : 10000;
        double xMax = mapExtent.getXMax() + width;
        double xMin = mapExtent.getXMin() - width;
        double yMax = mapExtent.getYMax() + height;
        double yMin = mapExtent.getYMin() - height;
        return new Envelope(new Point(xMax, yMax, sR), new Point(xMin, yMin, sR));
    }


    private void onSearchButtonClicked(String address) {
        Log.i(TAG, " #### Submitted address " + address);
        // Hide virtual keyboard
        hideKeyboard();

        // Remove any previous graphics and routes
        //resetGraphicsLayers();

        geoCodeTypedAddress(address);

    }

    /**
     * Geocode an address typed in by user
     *
     * @param address
     */
    private void geoCodeTypedAddress(final String address) {

        Log.d(TAG,"geoCodeTypedAddress");
        // Create Locator parameters from single line address string
        /*final GeocodeParameters geoParameters = new GeocodeParameters();
        geoParameters.setMaxResults(2);*/

        // Use the centre of the current map extent as the find location point
        /*if (mLocation != null) {
            geoParameters.setPreferredSearchLocation(mLocation);
        }*/
        // Null out any previously located result
        mGeocodedLocation = null;

        // Set address spatial reference to match map
        /*SpatialReference sR = mMapView.getSpatialReference();
        geoParameters.setOutputSpatialReference(sR);*/

        //geoParameters.setSearchArea(calculateSearchArea());

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

                                }else{
                                    Toast.makeText(getApplicationContext(),
                                            getString(R.string.location_not_foud) + address,
                                            Toast.LENGTH_LONG).show();

                                }

                            } catch (InterruptedException e) {
                                // Deal with exception...
                                e.printStackTrace();
                                Toast.makeText(getApplicationContext(),
                                        getString(R.string.geo_locate_error),
                                        Toast.LENGTH_LONG);

                            } catch (ExecutionException e) {
                                // Deal with exception...
                                e.printStackTrace();
                                Toast.makeText(getApplicationContext(),
                                        getString(R.string.geo_locate_error),
                                        Toast.LENGTH_LONG);
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

        // create marker symbol to represent location
        Bitmap icon = BitmapFactory.decodeResource(getApplication().getResources(), R.drawable.pin);
        BitmapDrawable drawable = new BitmapDrawable(getApplication().getResources(), icon);
        PictureMarkerSymbol resultSymbol = new PictureMarkerSymbol(drawable);
        // create graphic object for resulting location
        Graphic resultLocGraphic = new Graphic(resultPoint, resultSymbol);
        // add graphic to location layer
        graphicsOverlay.getGraphics().add(resultLocGraphic);

        /*mFoundLocation = resultPoint;

        mLocationLayerPointString = address;*/

        // Zoom map to geocode result location
        mMapView.setViewpointCenterAsync(resultPoint);
        TextView calloutContent = new TextView(getApplicationContext());
        calloutContent.setTextColor(Color.BLACK);
        calloutContent.setText(address);

        // get callout, set content and show
        mCallout = mMapView.getCallout();
        mCallout.setLocation(resultPoint);
        mCallout.setContent(calloutContent);

        mCallout.show();
    }

    /**
     * Initialize Suggestion Cursor
     */
    private void initSuggestionCursor() {
        String[] cols = {BaseColumns._ID, COLUMN_NAME_ADDRESS, COLUMN_NAME_X, COLUMN_NAME_Y};
        mSuggestionCursor = new MatrixCursor(cols);
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
                    Log.d(TAG, "here");
                    Log.d(TAG, location.toString());
                    Log.d(TAG, geocode.getRouteLocation().toJson());
                    Log.d(TAG, location.getX() + " " + location.getY() + " " + location.getZ() + " " + location.getM());
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
