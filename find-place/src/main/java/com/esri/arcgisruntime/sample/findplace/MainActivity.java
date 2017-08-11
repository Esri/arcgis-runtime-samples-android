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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.MatrixCursor;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.Multipoint;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.Callout;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.IdentifyGraphicsOverlayResult;
import com.esri.arcgisruntime.mapping.view.LocationDisplay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.mapping.view.ViewpointChangedEvent;
import com.esri.arcgisruntime.mapping.view.ViewpointChangedListener;
import com.esri.arcgisruntime.mapping.view.WrapAroundMode;
import com.esri.arcgisruntime.symbology.PictureMarkerSymbol;
import com.esri.arcgisruntime.tasks.geocode.GeocodeParameters;
import com.esri.arcgisruntime.tasks.geocode.GeocodeResult;
import com.esri.arcgisruntime.tasks.geocode.LocatorTask;
import com.esri.arcgisruntime.tasks.geocode.SuggestParameters;
import com.esri.arcgisruntime.tasks.geocode.SuggestResult;

public class MainActivity extends AppCompatActivity {

  private final String COLUMN_NAME_ADDRESS = "address";
  String[] reqPermissions = new String[] { Manifest.permission.ACCESS_FINE_LOCATION,
      Manifest.permission.ACCESS_COARSE_LOCATION };
  private int requestCode = 2;
  private String TAG;
  private String[] mColumnNames;
  private boolean mLocationSelected;
  private String mSearchAddress;

  private SearchView mSearchSearchView;
  private SearchView mLocationSearchView;

  private MapView mMapView;
  private LocationDisplay mLocationDisplay;
  private LocatorTask mLocatorTask;
  private GraphicsOverlay mGraphicsOverlay;
  private SuggestParameters mSearchSuggestParameters;
  private GeocodeParameters mSearchGeocodeParameters;
  private SuggestParameters mLocationSuggestParameters;
  private GeocodeParameters mLocationGeocodeParameters;
  private PictureMarkerSymbol mPinSourceSymbol;
  private Geometry mCurrentExtentGeometry;
  private Callout mCallout;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    TAG = "Find a place";

    // flag for whether a valid location has been found
    mLocationSelected = false;

    // setup the two SearchViews to show text hint
    mSearchSearchView = (SearchView) findViewById(R.id.search_searchView);
    mSearchSearchView.setIconified(false);
    mSearchSearchView.setFocusable(false);
    mSearchSearchView.setQueryHint(getResources().getString(R.string.search_hint));
    mLocationSearchView = (SearchView) findViewById(R.id.location_searchView);
    mLocationSearchView.setIconified(false);
    mLocationSearchView.setFocusable(false);
    mLocationSearchView.setQueryHint(getResources().getString(R.string.location_search_hint));

    // inflate MapView from layout
    mMapView = (MapView) findViewById(R.id.mapView);
    // disable map wraparound
    mMapView.setWrapAroundMode(WrapAroundMode.DISABLED);
    mLocationDisplay = mMapView.getLocationDisplay();
    mLocationDisplay.setAutoPanMode(LocationDisplay.AutoPanMode.RECENTER);
    mLocationDisplay.startAsync();
    // create a map with the BasemapType topographic
    ArcGISMap map = new ArcGISMap(Basemap.createTopographic());
    // set the map to be displayed in this view
    mMapView.setMap(map);
    // create a locator task from an online service
    mLocatorTask = new LocatorTask("http://geocode.arcgis.com/arcgis/rest/services/World/GeocodeServer");
    // define the graphics overlay and drawable responsible for showing relevant points
    mGraphicsOverlay = new GraphicsOverlay();
    BitmapDrawable pinDrawable = (BitmapDrawable) ContextCompat.getDrawable(this, R.drawable.pin);
    try {
      mPinSourceSymbol = PictureMarkerSymbol.createAsync(pinDrawable).get();
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
      Toast.makeText(getApplicationContext(), "Failed to load pin drawable!", Toast.LENGTH_LONG).show();
    }
    // set pin to half of native size
    mPinSourceSymbol.setWidth(19f);
    mPinSourceSymbol.setHeight(72f);
    mColumnNames = new String[] { BaseColumns._ID, COLUMN_NAME_ADDRESS };
    // on redo button click call redoSearchInThisArea
    Button redoSearchButton = (Button) findViewById(R.id.redo_search_button);
    redoSearchButton.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        redoSearchInThisArea();
      }
    });
    // add listener to update extent when viewpoint changes
    mMapView.addViewpointChangedListener(new ViewpointChangedListener() {
      @Override public void viewpointChanged(ViewpointChangedEvent viewpointChangedEvent) {
        if (!mMapView.isNavigating()) {
          // get the current map extent
          mCurrentExtentGeometry = mMapView.getCurrentViewpoint(Viewpoint.Type.BOUNDING_GEOMETRY).getTargetGeometry();
        }
      }
    });
    mMapView.setOnTouchListener(new DefaultMapViewOnTouchListener(this, mMapView) {

      @Override
      public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
        showCallout(motionEvent);
        return true;
      }
    });
    setupLocationDisplay();
    setupSearch();
    setupLocation();
  }

  /**
   * Sets up the top SearchView, which filters for POI. Uses MatrixCursor to show suggestions to the user as the user
   * inputs text.
   */
  private void setupSearch() {

    mSearchSuggestParameters = new SuggestParameters();
    // filter categories for POI
    mSearchSuggestParameters.getCategories().add("POI");
    mSearchGeocodeParameters = new GeocodeParameters();
    // get all attributes
    mSearchGeocodeParameters.getResultAttributeNames().add("*");
    mSearchSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

      @Override
      public boolean onQueryTextSubmit(String address) {
        geoCodeTypedAddress(address);
        // clear focus from search views
        mSearchSearchView.clearFocus();
        mLocationSearchView.clearFocus();
        return true;
      }

      @Override
      public boolean onQueryTextChange(String newText) {
        // as long as newText isn't empty, get suggestions from the locatorTask
        if (!newText.equals("")) {
          mSearchSuggestParameters.setSearchArea(mCurrentExtentGeometry);
          final ListenableFuture<List<SuggestResult>> suggestionsFuture = mLocatorTask
              .suggestAsync(newText, mSearchSuggestParameters);
          suggestionsFuture.addDoneListener(new Runnable() {

            @Override public void run() {
              try {
                // get the results of the async operation
                List<SuggestResult> suggestResults = suggestionsFuture.get();
                MatrixCursor suggestionsCursor = new MatrixCursor(mColumnNames);
                int key = 0;
                // add each poi_suggestion result to a new row
                for (SuggestResult result : suggestResults) {
                  suggestionsCursor.addRow(new Object[] { key++, result.getLabel() });
                }
                // define SimpleCursorAdapter
                String[] cols = new String[] { COLUMN_NAME_ADDRESS };
                int[] to = new int[] { R.id.suggestion_address };
                final SimpleCursorAdapter suggestionAdapter = new SimpleCursorAdapter(MainActivity.this,
                    R.layout.suggestion, suggestionsCursor, cols, to, 0);
                mSearchSearchView.setSuggestionsAdapter(suggestionAdapter);
                // handle a poi_suggestion being chosen
                mSearchSearchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
                  @Override public boolean onSuggestionSelect(int position) {
                    return false;
                  }

                  @Override public boolean onSuggestionClick(int position) {
                    // get the selected row
                    MatrixCursor selectedRow = (MatrixCursor) suggestionAdapter.getItem(position);
                    // get the row's index
                    int selectedCursorIndex = selectedRow.getColumnIndex(COLUMN_NAME_ADDRESS);
                    // get the string from the row at index
                    mSearchAddress = selectedRow.getString(selectedCursorIndex);
                    // if no location selected, use device location
                    if (mLocationSelected) {
                      mSearchSearchView.setQuery(mSearchAddress, true);
                    } else {
                      mSearchSearchView.setQuery(mSearchAddress, true);
                      mSearchGeocodeParameters.setPreferredSearchLocation(mLocationDisplay.getLocation().getPosition());
                      //mLocationSearchView.setQuery("Current location", false);
                    }
                    return true;
                  }
                });
              } catch (Exception e) {
                e.printStackTrace();
              }
            }
          });
        }
        return true;
      }
    });
  }

  private void setupLocation() {

    mLocationSuggestParameters = new SuggestParameters();
    mLocationGeocodeParameters = new GeocodeParameters();
    // get all attributes
    mLocationGeocodeParameters.getResultAttributeNames().add("*");
    mLocationSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
      @Override public boolean onQueryTextSubmit(String address) {
        geoCodeTypedAddress(address);
        // clear focus from search views
        mSearchSearchView.clearFocus();
        mLocationSearchView.clearFocus();
        return true;
      }

      @Override public boolean onQueryTextChange(String newText) {
        // as long as newText isn't empty, get suggestions from the locatorTask
        if (!newText.equals("")) {
          final ListenableFuture<List<SuggestResult>> suggestionsFuture = mLocatorTask
              .suggestAsync(newText, mLocationSuggestParameters);
          suggestionsFuture.addDoneListener(new Runnable() {

            @Override public void run() {
              try {
                // get the results of the async operation
                List<SuggestResult> suggestResults = suggestionsFuture.get();
                MatrixCursor suggestionsCursor = new MatrixCursor(mColumnNames);
                int key = 0;
                // add each poi_suggestion result to a new row
                for (SuggestResult result : suggestResults) {
                  suggestionsCursor.addRow(new Object[] { key++, result.getLabel() });
                }
                // define SimpleCursorAdapter
                String[] cols = new String[] { COLUMN_NAME_ADDRESS };
                int[] to = new int[] { R.id.suggestion_address };
                final SimpleCursorAdapter suggestionAdapter = new SimpleCursorAdapter(MainActivity.this,
                    R.layout.suggestion, suggestionsCursor, cols, to, 0);
                mLocationSearchView.setSuggestionsAdapter(suggestionAdapter);
                mLocationSearchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
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
                    mLocatorTask.addDoneLoadingListener(new Runnable() {
                      @Override
                      public void run() {
                        if (mLocatorTask.getLoadStatus() == LoadStatus.LOADED) {
                          // Call geocodeAsync passing in an address
                          final ListenableFuture<List<GeocodeResult>> geocodeFuture = mLocatorTask
                              .geocodeAsync(address, mLocationGeocodeParameters);
                          geocodeFuture.addDoneListener(new Runnable() {
                            @Override
                            public void run() {
                              try {
                                // Get the results of the async operation
                                List<GeocodeResult> geocodeResults = geocodeFuture.get();
                                if (geocodeResults.size() > 0) {
                                  // valid location found
                                  mLocationSelected = true;
                                  // use geocodeResult to focus search area
                                  GeocodeResult geocodeResult = geocodeResults.get(0);
                                  mSearchGeocodeParameters
                                      .setPreferredSearchLocation(geocodeResult.getDisplayLocation());
                                  mSearchGeocodeParameters.setSearchArea(geocodeResult.getDisplayLocation());
                                  // set the address string to the SearchView, but don't submit as a query
                                  mLocationSearchView.setQuery(address, false);
                                  // call search query
                                  mSearchSearchView.setQuery(mSearchAddress, true);
                                  mLocationSearchView.clearFocus();
                                  mSearchSearchView.clearFocus();
                                } else {
                                  // flag for whether a location has been found
                                  mLocationSelected = false;
                                  Toast.makeText(getApplicationContext(),
                                      getString(R.string.location_not_found) + address, Toast.LENGTH_LONG).show();
                                }
                              } catch (InterruptedException | ExecutionException e) {
                                e.printStackTrace();
                                Toast.makeText(getApplicationContext(), getString(R.string.geo_locate_error),
                                    Toast.LENGTH_LONG).show();
                              }
                              // done processing and can remove this listener
                              geocodeFuture.removeDoneListener(this);
                            }
                          });
                        }
                      }
                    });

                    return true;
                  }
                });
              } catch (Exception e) {
                e.printStackTrace();
              }
            }
          });
        }
        return true;
      }
    });
  }

  /**
   * Does a search for existing POI using the MapView's current extent to inform the search.
   */

  private void redoSearchInThisArea() {
    // set center of current extent to preferred search location
    mSearchGeocodeParameters.setPreferredSearchLocation(mCurrentExtentGeometry.getExtent().getCenter());
    mSearchGeocodeParameters.setSearchArea(mCurrentExtentGeometry);
    mLocationSearchView.setQuery(getString(R.string.searching_by_area), false);
    // use whatever text is in the search box to call geoCodeTypedAddress
    geoCodeTypedAddress(mSearchSearchView.getQuery().toString());
  }

  /**
   * Identifies the Graphic at the clicked point. Gets attribute of that Graphic and assigns it to a Callout, which is
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
    identifyResultsFuture.addDoneListener(new Runnable() {
      @Override public void run() {
        try {
          IdentifyGraphicsOverlayResult identifyGraphicsOverlayResult = identifyResultsFuture.get();
          List<Graphic> graphics = identifyGraphicsOverlayResult.getGraphics();
          // if a graphic has been identified
          if (graphics.size() > 0) {
            //get the first graphic identified
            Graphic identifiedGraphic = graphics.get(0);
            // create a TextView for the Callout
            TextView calloutContent = new TextView(getApplicationContext());
            calloutContent.setTextColor(Color.BLACK);
            // set the text of the Callout to graphic's attributes
            calloutContent.setText(identifiedGraphic.getAttributes().get("PlaceName").toString() + "\n"
                + identifiedGraphic.getAttributes().get("StAddr").toString());
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
          e.printStackTrace();
        }
      }
    });
  }

  /**
   * Geocode an address typed in by user
   *
   * @param address
   */
  private void geoCodeTypedAddress(final String address) {
    Log.d("geocodetypedaddress", address);
    // Execute async task to find the address
    mLocatorTask.addDoneLoadingListener(new Runnable() {
      @Override
      public void run() {
        if (mLocatorTask.getLoadStatus() == LoadStatus.LOADED) {
          // Call geocodeAsync passing in an address
          final ListenableFuture<List<GeocodeResult>> geocodeResultListenableFuture = mLocatorTask
              .geocodeAsync(address, mSearchGeocodeParameters);
          geocodeResultListenableFuture.addDoneListener(new Runnable() {
            @Override
            public void run() {
              try {
                // Get the results of the async operation
                List<GeocodeResult> geocodeResults = geocodeResultListenableFuture.get();
                if (geocodeResults.size() > 0) {
                  displaySearchResult(geocodeResults);
                } else {
                  Toast.makeText(getApplicationContext(), getString(R.string.location_not_found) + address,
                      Toast.LENGTH_LONG).show();
                }
              } catch (InterruptedException | ExecutionException e) {
                // Deal with exception...
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), getString(R.string.geo_locate_error), Toast.LENGTH_LONG).show();
              }
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

  private void displaySearchResult(List<GeocodeResult> geocodeResults) {
    // dismiss any callout and clear map
    if (mMapView.getCallout().isShowing()) {
      mMapView.getCallout().dismiss();
    }
    mMapView.getGraphicsOverlays().clear();
    mGraphicsOverlay.getGraphics().clear();
    // create a list of result points from the geocode results
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

  private void setupLocationDisplay() {
    // check and request permissions
    boolean permissionCheck1 = ContextCompat.checkSelfPermission(MainActivity.this, reqPermissions[0]) ==
        PackageManager.PERMISSION_GRANTED;
    boolean permissionCheck2 = ContextCompat.checkSelfPermission(MainActivity.this, reqPermissions[1]) ==
        PackageManager.PERMISSION_GRANTED;

    if (!(permissionCheck1 && permissionCheck2)) {
      // If permissions are not already granted, request permission from the user.
      ActivityCompat.requestPermissions(MainActivity.this, reqPermissions, requestCode);
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    // If request is cancelled, the result arrays are empty.
    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      // Location permission was granted. This would have been triggered in response to failing to start the
      // LocationDisplay, so try starting this again.
      mLocationDisplay.startAsync();
    } else {
      // If permission was denied, show toast to inform user what was chosen. If LocationDisplay is started again,
      // request permission UX will be shown again, option should be shown to allow never showing the UX again.
      // Alternative would be to disable functionality so request is not shown again.
      Toast.makeText(MainActivity.this, getResources().getString(R.string.location_permission_denied), Toast.LENGTH_SHORT).show();
    }
  }
}
