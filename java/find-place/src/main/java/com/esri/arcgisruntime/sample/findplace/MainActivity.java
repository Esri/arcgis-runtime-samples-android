/* Copyright 2017 Esri
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

package com.esri.arcgisruntime.sample.findplace;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.MatrixCursor;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.cursoradapter.widget.SimpleCursorAdapter;

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.Multipoint;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.Callout;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.IdentifyGraphicsOverlayResult;
import com.esri.arcgisruntime.mapping.view.LocationDisplay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.mapping.view.WrapAroundMode;
import com.esri.arcgisruntime.symbology.PictureMarkerSymbol;
import com.esri.arcgisruntime.tasks.geocode.GeocodeParameters;
import com.esri.arcgisruntime.tasks.geocode.GeocodeResult;
import com.esri.arcgisruntime.tasks.geocode.LocatorTask;
import com.esri.arcgisruntime.tasks.geocode.SuggestParameters;
import com.esri.arcgisruntime.tasks.geocode.SuggestResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

  private final String TAG = MainActivity.class.getSimpleName();

  private final String[] reqPermissions = { Manifest.permission.ACCESS_FINE_LOCATION,
      Manifest.permission.ACCESS_COARSE_LOCATION };

  private static final String COLUMN_NAME_ADDRESS = "address";
  private final String[] mColumnNames = { BaseColumns._ID, COLUMN_NAME_ADDRESS };

  private SearchView mPoiSearchView;
  private SearchView mProximitySearchView;

  private boolean mProximitySearchViewEmpty;
  private String mPoiAddress;

  private Point mPreferredSearchProximity;
  private MapView mMapView;
  private LocationDisplay mLocationDisplay;
  private LocatorTask mLocatorTask;
  private GraphicsOverlay mGraphicsOverlay;
  private SuggestParameters mPoiSuggestParameters;
  private GeocodeParameters mPoiGeocodeParameters;
  private SuggestParameters mProximitySuggestParameters;
  private GeocodeParameters mProximityGeocodeParameters;
  private PictureMarkerSymbol mPinSourceSymbol;
  private Geometry mCurrentExtentGeometry;
  private Callout mCallout;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // authentication with an API key or named user is required to access basemaps and other
    // location services
    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY);

    // if permissions are not already granted, request permission from the user
    if (!(ContextCompat.checkSelfPermission(this, reqPermissions[0]) == PackageManager.PERMISSION_GRANTED
        && ContextCompat.checkSelfPermission(this, reqPermissions[1])
        == PackageManager.PERMISSION_GRANTED)) {
      int requestCode = 2;
      ActivityCompat.requestPermissions(this, reqPermissions, requestCode);
    }

    // setup the two SearchViews and show text hint
    mPoiSearchView = findViewById(R.id.poi_searchView);
    mPoiSearchView.setIconified(false);
    mPoiSearchView.setFocusable(false);
    mPoiSearchView.setQueryHint(getResources().getString(R.string.search_hint));
    mProximitySearchView = findViewById(R.id.proximity_searchView);
    mProximitySearchView.setIconified(false);
    mProximitySearchView.setFocusable(false);
    mProximitySearchView.setQueryHint(getResources().getString(R.string.proximity_search_hint));
    // setup redo search button
    Button redoSearchButton = findViewById(R.id.redo_search_button);
    // on redo button click call redoSearchInThisArea
    redoSearchButton.setOnClickListener(v -> redoSearchInThisArea());

    // define pin drawable
    BitmapDrawable pinDrawable = (BitmapDrawable) ContextCompat.getDrawable(this, R.drawable.pin);
    try {
      mPinSourceSymbol = PictureMarkerSymbol.createAsync(pinDrawable).get();
    } catch (InterruptedException | ExecutionException e) {
      String error = "Error creating PictureMarkerSymbol: " + e.getMessage();
      Log.e(TAG, error);
      Toast.makeText(this, error, Toast.LENGTH_LONG).show();
    }
    // set pin to half of native size
    mPinSourceSymbol.setWidth(19f);
    mPinSourceSymbol.setHeight(72f);

    // instantiate flag proximity search view flag
    mProximitySearchViewEmpty = true;

    // create a LocatorTask from an online service
    mLocatorTask = new LocatorTask("https://geocode-api.arcgis.com/arcgis/rest/services/World/GeocodeServer");

    // inflate MapView from layout
    mMapView = findViewById(R.id.mapView);
    // disable map wraparound
    mMapView.setWrapAroundMode(WrapAroundMode.DISABLED);
    // create a map with the Basemap Style topographic
    final ArcGISMap map = new ArcGISMap(BasemapStyle.ARCGIS_TOPOGRAPHIC);
    // set the map to be displayed in this view
    mMapView.setMap(map);
    // add listener to update extent when viewpoint has changed
    mMapView.addViewpointChangedListener(viewpointChangedEvent -> {
      // get the current map extent
      mCurrentExtentGeometry = mMapView.getCurrentViewpoint(Viewpoint.Type.BOUNDING_GEOMETRY).getTargetGeometry();
    });
    // add listener to handle callouts
    mMapView.setOnTouchListener(new DefaultMapViewOnTouchListener(this, mMapView) {
      @Override
      public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
        showCallout(motionEvent);
        return true;
      }
    });
    // setup and start location display
    mLocationDisplay = mMapView.getLocationDisplay();
    mLocationDisplay.setAutoPanMode(LocationDisplay.AutoPanMode.RECENTER);
    mLocationDisplay.startAsync();
    // initially use device location to focus POI search
    final Point[] currentLocation = new Point[1];
    mLocationDisplay.addLocationChangedListener(locationChangedEvent -> {
      currentLocation[0] = mLocationDisplay.getMapLocation();
      // only update preferredSearchLocation if device has moved
      if (!currentLocation[0].equals(mLocationDisplay.getMapLocation(), 100) || mPreferredSearchProximity == null) {
        mPreferredSearchProximity = mLocationDisplay.getMapLocation();
      }
    });
    // define the graphics overlay
    mGraphicsOverlay = new GraphicsOverlay();

    setupPoi();
    setupProximity();
  }

  /**
   * Sets up the POI SearchView. Uses MatrixCursor to show suggestions to the user as the user inputs text.
   */
  private void setupPoi() {

    mPoiSuggestParameters = new SuggestParameters();
    // filter categories for POI
    mPoiSuggestParameters.getCategories().add("POI");
    mPoiGeocodeParameters = new GeocodeParameters();
    // get all attributes
    mPoiGeocodeParameters.getResultAttributeNames().add("*");
    mPoiSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

      @Override
      public boolean onQueryTextSubmit(String address) {
        // if proximity SearchView text box is empty, use the device location
        if (mProximitySearchViewEmpty) {
          mPreferredSearchProximity = mLocationDisplay.getMapLocation();
          mProximitySearchView.setQuery("Using current location...", false);
        }
        // keep track of typed address
        mPoiAddress = address;
        // geocode typed address
        geoCodeTypedAddress(address);
        // clear focus from search views
        mPoiSearchView.clearFocus();
        mProximitySearchView.clearFocus();
        return true;
      }

      @Override
      public boolean onQueryTextChange(final String newText) {
        // as long as newText isn't empty, get suggestions from the locatorTask
        if (!newText.equals("")) {
          mPoiSuggestParameters.setSearchArea(mCurrentExtentGeometry);
          final ListenableFuture<List<SuggestResult>> suggestionsFuture = mLocatorTask
              .suggestAsync(newText, mPoiSuggestParameters);
          suggestionsFuture.addDoneListener(new Runnable() {

            @Override public void run() {
              try {
                // get the results of the async operation
                List<SuggestResult> suggestResults = suggestionsFuture.get();

                if (!suggestResults.isEmpty()) {
                  MatrixCursor suggestionsCursor = new MatrixCursor(mColumnNames);
                  int key = 0;
                  // add each poi_suggestion result to a new row
                  for (SuggestResult result : suggestResults) {
                    suggestionsCursor.addRow(new Object[] { key++, result.getLabel() });
                  }
                  // define SimpleCursorAdapter
                  String[] cols = { COLUMN_NAME_ADDRESS };
                  int[] to = { R.id.suggestion_address };
                  final SimpleCursorAdapter suggestionAdapter = new SimpleCursorAdapter(MainActivity.this,
                      R.layout.suggestion, suggestionsCursor, cols, to, 0);
                  mPoiSearchView.setSuggestionsAdapter(suggestionAdapter);
                  // handle a poi_suggestion being chosen
                  mPoiSearchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
                    @Override public boolean onSuggestionSelect(int position) {
                      return false;
                    }

                    @Override public boolean onSuggestionClick(int position) {
                      // get the selected row
                      MatrixCursor selectedRow = (MatrixCursor) suggestionAdapter.getItem(position);
                      // get the row's index
                      int selectedCursorIndex = selectedRow.getColumnIndex(COLUMN_NAME_ADDRESS);
                      // get the string from the row at index
                      mPoiAddress = selectedRow.getString(selectedCursorIndex);
                      mPoiSearchView.setQuery(mPoiAddress, true);
                      return true;
                    }
                  });
                } else {
                  mPoiAddress = newText;
                }
              } catch (Exception e) {
                Log.e(TAG, "Geocode suggestion error: " + e.getMessage());
              }
            }
          });
        }
        return true;
      }
    });
  }

  /**
   * Sets up the proximity SearchView. Uses MatrixCursor to show suggestions to the user as the user inputs text.
   */
  private void setupProximity() {

    mProximitySuggestParameters = new SuggestParameters();
    mProximitySuggestParameters.getCategories().add("Populated Place");
    mProximityGeocodeParameters = new GeocodeParameters();
    // get all attributes
    mProximityGeocodeParameters.getResultAttributeNames().add("*");
    mProximitySearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
      @Override public boolean onQueryTextSubmit(String address) {
        geoCodeTypedAddress(address);
        // clear focus from search views
        mPoiSearchView.clearFocus();
        mProximitySearchView.clearFocus();
        return true;
      }

      @Override public boolean onQueryTextChange(String newText) {
        // as long as newText isn't empty, get suggestions from the locatorTask
        if (!newText.equals("")) {
          mProximitySearchViewEmpty = false;
          final ListenableFuture<List<SuggestResult>> suggestionsFuture = mLocatorTask
              .suggestAsync(newText, mProximitySuggestParameters);
          suggestionsFuture.addDoneListener(() -> {
            try {
              // get the list of suggestions
              List<SuggestResult> suggestResults = suggestionsFuture.get();
              MatrixCursor suggestionsCursor = new MatrixCursor(mColumnNames);
              int key = 0;
              // add each SuggestResult to a new row
              for (SuggestResult result : suggestResults) {
                suggestionsCursor.addRow(new Object[] { key++, result.getLabel() });
              }
              // define SimpleCursorAdapter
              String[] cols = { COLUMN_NAME_ADDRESS };
              int[] to = { R.id.suggestion_address };
              final SimpleCursorAdapter suggestionAdapter = new SimpleCursorAdapter(MainActivity.this,
                  R.layout.suggestion, suggestionsCursor, cols, to, 0);
              mProximitySearchView.setSuggestionsAdapter(suggestionAdapter);
              mProximitySearchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
                @Override public boolean onSuggestionSelect(int position) {
                  return false;
                }

                @Override public boolean onSuggestionClick(int position) {
                  // get the selected row
                  MatrixCursor selectedRow = (MatrixCursor) suggestionAdapter.getItem(position);
                  // get the row's index
                  int selectedCursorIndex = selectedRow.getColumnIndex(COLUMN_NAME_ADDRESS);
                  // get the string from the row at index
                  final String address = selectedRow.getString(selectedCursorIndex);
                  mLocatorTask.addDoneLoadingListener(() -> {
                    if (mLocatorTask.getLoadStatus() == LoadStatus.LOADED) {
                      // geocode the selected address to get location of address
                      final ListenableFuture<List<GeocodeResult>> geocodeFuture = mLocatorTask
                          .geocodeAsync(address, mProximityGeocodeParameters);
                      geocodeFuture.addDoneListener(() -> {
                        try {
                          // Get the results of the async operation
                          List<GeocodeResult> geocodeResults = geocodeFuture.get();
                          if (!geocodeResults.isEmpty()) {
                            // use geocodeResult to focus search area
                            GeocodeResult geocodeResult = geocodeResults.get(0);
                            // update preferred search area to the geocode result
                            mPreferredSearchProximity = geocodeResult.getDisplayLocation();
                            mPoiGeocodeParameters.setSearchArea(mPreferredSearchProximity);
                            // set the address string to the SearchView, but don't submit as a query
                            mProximitySearchView.setQuery(address, false);
                            // call POI search query
                            mPoiSearchView.setQuery(mPoiAddress, true);
                            // clear focus from search views
                            mProximitySearchView.clearFocus();
                            mPoiSearchView.clearFocus();
                          } else {
                            Toast.makeText(getApplicationContext(),
                                getString(R.string.location_not_found) + address, Toast.LENGTH_LONG).show();
                          }
                        } catch (InterruptedException | ExecutionException e) {
                          Log.e(TAG, "Geocode error: " + e.getMessage());
                          Toast.makeText(getApplicationContext(), getString(R.string.geo_locate_error),
                              Toast.LENGTH_LONG).show();
                        }
                      });
                    }
                  });
                  return true;
                }
              });
            } catch (Exception e) {
              Log.e(TAG, "Geocode suggestion error: " + e.getMessage());
            }
          });
          // if search view is empty, set flag
        } else {
          mProximitySearchViewEmpty = true;
        }
        return true;
      }
    });
  }

  /**
   * Performs a search for the POI listed in the SearchView, using the MapView's current extent to inform the search.
   */
  private void redoSearchInThisArea() {
    // set center of current extent to preferred search proximity
    mPreferredSearchProximity = mCurrentExtentGeometry.getExtent().getCenter();
    mPoiGeocodeParameters.setSearchArea(mCurrentExtentGeometry);
    mProximitySearchView.setQuery(getString(R.string.searching_by_area), false);
    // use most recent POI address
    geoCodeTypedAddress(mPoiAddress);
  }

  /**
   * Identifies the Graphic at the tapped point. Gets attribute of that Graphic and assigns it to a Callout, which is
   * then displayed.
   *
   * @param motionEvent from onSingleTapConfirmed
   */
  private void showCallout(MotionEvent motionEvent) {
    // get the screen point
    android.graphics.Point screenPoint = new android.graphics.Point(Math.round(motionEvent.getX()),
        Math.round(motionEvent.getY()));
    // convert to map point
    final Point mapPoint = mMapView.screenToLocation(screenPoint);
    // from the graphics overlay, get graphics near the tapped location
    final ListenableFuture<IdentifyGraphicsOverlayResult> identifyResultsFuture = mMapView
        .identifyGraphicsOverlayAsync(mGraphicsOverlay, screenPoint, 10, false);
    identifyResultsFuture.addDoneListener(() -> {
      try {
        IdentifyGraphicsOverlayResult identifyGraphicsOverlayResult = identifyResultsFuture.get();
        List<Graphic> graphics = identifyGraphicsOverlayResult.getGraphics();
        // if a graphic has been identified
        if (!graphics.isEmpty()) {
          //get the first graphic identified
          Graphic identifiedGraphic = graphics.get(0);
          // create a TextView for the Callout
          TextView calloutContent = new TextView(getApplicationContext());
          calloutContent.setTextColor(Color.BLACK);
          // set the text of the Callout to graphic's attributes
          calloutContent.setText(identifiedGraphic.getAttributes().get("PlaceName") + "\n"
              + identifiedGraphic.getAttributes().get("StAddr"));
          // get Callout and set its options: animateCallout: true, recenterMap: false, animateRecenter: false
          mCallout = mMapView.getCallout();
          mCallout.setShowOptions(new Callout.ShowOptions(true, false, false));
          // set the leader position and show the callout
          mCallout.setLocation(identifiedGraphic.computeCalloutLocation(mapPoint, mMapView));
          mCallout.setContent(calloutContent);
          mCallout.show();
        } else {
          mCallout.dismiss();
        }
      } catch (Exception e) {
        Log.e(TAG, "Identify error: " + e.getMessage());
      }
    });
  }

  /**
   * Geocode an address passed in by the user.
   *
   * @param address read in from searchViews
   */
  private void geoCodeTypedAddress(final String address) {
    // check that address isn't null
    if (address != null) {
      // POI geocode parameters set from proximity SearchView or, if empty, device location
      mPoiGeocodeParameters.setPreferredSearchLocation(mPreferredSearchProximity);
      mPoiGeocodeParameters.setSearchArea(mPreferredSearchProximity);
      // Execute async task to find the address
      mLocatorTask.addDoneLoadingListener(() -> {
        if (mLocatorTask.getLoadStatus() == LoadStatus.LOADED) {
          // Call geocodeAsync passing in an address
          final ListenableFuture<List<GeocodeResult>> geocodeResultListenableFuture = mLocatorTask
              .geocodeAsync(address, mPoiGeocodeParameters);
          geocodeResultListenableFuture.addDoneListener(() -> {
            try {
              // Get the results of the async operation
              List<GeocodeResult> geocodeResults = geocodeResultListenableFuture.get();
              if (!geocodeResults.isEmpty()) {
                displaySearchResult(geocodeResults);
              } else {
                Toast.makeText(getApplicationContext(), getString(R.string.location_not_found) + address,
                    Toast.LENGTH_LONG).show();
              }
            } catch (InterruptedException | ExecutionException e) {
              Log.e(TAG, "Geocode error: " + e.getMessage());
              Toast.makeText(getApplicationContext(), getString(R.string.geo_locate_error), Toast.LENGTH_LONG)
                  .show();
            }
          });
        } else {
          Log.i(TAG, "Trying to reload locator task");
          mLocatorTask.retryLoadAsync();
        }
      });
      mLocatorTask.loadAsync();
    }
  }

  /**
   * Turns a list of GeocodeResults into Points and adds them to a GraphicOverlay which is then drawn on the map. The
   * points are added to a multipoint used to calculate a viewpoint.
   *
   * @param geocodeResults as a list
   */
  private void displaySearchResult(List<GeocodeResult> geocodeResults) {
    // dismiss any callout
    if (mMapView.getCallout() != null && mMapView.getCallout().isShowing()) {
      mMapView.getCallout().dismiss();
    }
    // clear map of existing graphics
    mMapView.getGraphicsOverlays().clear();
    mGraphicsOverlay.getGraphics().clear();
    // create a list of points from the geocode results
    List<Point> resultPoints = new ArrayList<>();
    for (GeocodeResult result : geocodeResults) {
      // create graphic object for resulting location
      Point resultPoint = result.getDisplayLocation();
      Graphic resultLocGraphic = new Graphic(resultPoint, result.getAttributes(), mPinSourceSymbol);
      // add graphic to location layer
      mGraphicsOverlay.getGraphics().add(resultLocGraphic);
      resultPoints.add(resultPoint);
    }
    // add result points to a Multipoint and get an envelope surrounding it
    Multipoint resultsMultipoint = new Multipoint(resultPoints);
    Envelope resultsEnvelope = resultsMultipoint.getExtent();
    // add a 25% buffer to the extent Envelope of result points
    Envelope resultsEnvelopeWithBuffer = new Envelope(resultsEnvelope.getCenter(), resultsEnvelope.getWidth() * 1.25,
        resultsEnvelope.getHeight() * 1.25);
    // zoom map to result over 3 seconds
    mMapView.setViewpointAsync(new Viewpoint(resultsEnvelopeWithBuffer), 3);
    // set the graphics overlay to the map
    mMapView.getGraphicsOverlays().add(mGraphicsOverlay);
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    // if request is cancelled, the result arrays are empty
    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      mLocationDisplay.startAsync();
    } else {
      // if permission was denied, show toast to inform user what was chosen
      Toast.makeText(this, getResources().getString(R.string.location_permission_denied),
          Toast.LENGTH_SHORT).show();
    }
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
