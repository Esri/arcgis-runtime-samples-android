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

package com.esri.arcgisruntime.sample.findaddress;

import android.database.MatrixCursor;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.cursoradapter.widget.SimpleCursorAdapter;

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
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
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.PictureMarkerSymbol;
import com.esri.arcgisruntime.tasks.geocode.GeocodeParameters;
import com.esri.arcgisruntime.tasks.geocode.GeocodeResult;
import com.esri.arcgisruntime.tasks.geocode.LocatorTask;
import com.esri.arcgisruntime.tasks.geocode.SuggestResult;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

  private final String TAG = MainActivity.class.getSimpleName();
  private final String COLUMN_NAME_ADDRESS = "address";
  private final String[] mColumnNames = { BaseColumns._ID, COLUMN_NAME_ADDRESS };
  private SearchView mAddressSearchView;

  private MapView mMapView;
  private LocatorTask mLocatorTask;
  private GraphicsOverlay mGraphicsOverlay;
  private GeocodeParameters mAddressGeocodeParameters;
  private PictureMarkerSymbol mPinSourceSymbol;
  private Callout mCallout;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // authentication with an API key or named user is required to access basemaps and other
    // location services
    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY);

    // inflate address search view
    mAddressSearchView = findViewById(R.id.addressSearchView);
    mAddressSearchView.setIconified(false);
    mAddressSearchView.setFocusable(false);
    mAddressSearchView.setQueryHint(getResources().getString(R.string.address_search_hint));

    // define pin drawable
    BitmapDrawable pinDrawable = (BitmapDrawable) ContextCompat.getDrawable(this, R.drawable.pin);
    try {
      mPinSourceSymbol = PictureMarkerSymbol.createAsync(pinDrawable).get();
    } catch (InterruptedException | ExecutionException e) {
      Log.e(TAG, "Picture Marker Symbol error: " + e.getMessage());
      Toast.makeText(getApplicationContext(), "Failed to load pin drawable.", Toast.LENGTH_LONG).show();
    }
    // set pin to half of native size
    mPinSourceSymbol.setWidth(19f);
    mPinSourceSymbol.setHeight(72f);

    // create a LocatorTask from an online service
    mLocatorTask = new LocatorTask("https://geocode-api.arcgis.com/arcgis/rest/services/World/GeocodeServer");

    // inflate MapView from layout
    mMapView = (MapView) findViewById(R.id.mapView);
    // create a map with the Basemap Style topographic
    final ArcGISMap map = new ArcGISMap(BasemapStyle.ARCGIS_STREETS);
    map.setInitialViewpoint(new Viewpoint(40,-100,100000000));

    // once the map has loaded successfully, set up address finding UI
    map.addDoneLoadingListener(() -> {
      if (map.getLoadStatus() == LoadStatus.LOADED) {
        setupAddressSearchView();
      } else {
        Log.e(TAG, "Map failed to load: " + map.getLoadError().getMessage());
        Toast.makeText(
                getApplicationContext(),
                "Map failed to load: " + map.getLoadError().getMessage(),
                Toast.LENGTH_LONG
        ).show();
      }
    });

    // set the map to be displayed in this view
    mMapView.setMap(map);
    // set the map viewpoint to start over North America
    mMapView.setViewpoint(new Viewpoint(40, -100, 100000000));

    // add listener to handle screen taps
    mMapView.setOnTouchListener(new DefaultMapViewOnTouchListener(this, mMapView) {
      @Override
      public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
        identifyGraphic(motionEvent);
        return true;
      }
    });

    // define the graphics overlay
    mGraphicsOverlay = new GraphicsOverlay();

  }

  /**
   * Sets up the address SearchView. Uses MatrixCursor to show suggestions to the user as the user inputs text.
   */
  private void setupAddressSearchView() {

    mAddressGeocodeParameters = new GeocodeParameters();
    // get place name and address attributes
    mAddressGeocodeParameters.getResultAttributeNames().add("PlaceName");
    mAddressGeocodeParameters.getResultAttributeNames().add("Place_addr");
    // return only the closest result
    mAddressGeocodeParameters.setMaxResults(1);
    mAddressSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

      @Override
      public boolean onQueryTextSubmit(String address) {
        // geocode typed address
        geoCodeTypedAddress(address);
        // clear focus from search views
        mAddressSearchView.clearFocus();
        return true;
      }

      @Override
      public boolean onQueryTextChange(String newText) {
        // as long as newText isn't empty, get suggestions from the locatorTask
        if (!newText.equals("")) {
          final ListenableFuture<List<SuggestResult>> suggestionsFuture = mLocatorTask.suggestAsync(newText);
          suggestionsFuture.addDoneListener(new Runnable() {

            @Override public void run() {
              try {
                // get the results of the async operation
                List<SuggestResult> suggestResults = suggestionsFuture.get();
                MatrixCursor suggestionsCursor = new MatrixCursor(mColumnNames);
                int key = 0;
                // add each address suggestion to a new row
                for (SuggestResult result : suggestResults) {
                  suggestionsCursor.addRow(new Object[] { key++, result.getLabel() });
                }
                // define SimpleCursorAdapter
                String[] cols = new String[] { COLUMN_NAME_ADDRESS };
                int[] to = new int[] { R.id.suggestion_address };
                final SimpleCursorAdapter suggestionAdapter = new SimpleCursorAdapter(MainActivity.this,
                    R.layout.suggestion, suggestionsCursor, cols, to, 0);
                mAddressSearchView.setSuggestionsAdapter(suggestionAdapter);
                // handle an address suggestion being chosen
                mAddressSearchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
                  @Override public boolean onSuggestionSelect(int position) {
                    return false;
                  }

                  @Override public boolean onSuggestionClick(int position) {
                    // get the selected row
                    MatrixCursor selectedRow = (MatrixCursor) suggestionAdapter.getItem(position);
                    // get the row's index
                    int selectedCursorIndex = selectedRow.getColumnIndex(COLUMN_NAME_ADDRESS);
                    // get the string from the row at index
                    String address = selectedRow.getString(selectedCursorIndex);
                    // use clicked suggestion as query
                    mAddressSearchView.setQuery(address, true);
                    return true;
                  }
                });
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
   * Identifies the Graphic at the tapped point.
   *
   * @param motionEvent containing a tapped screen point
   */
  private void identifyGraphic(MotionEvent motionEvent) {
    // get the screen point
    android.graphics.Point screenPoint = new android.graphics.Point(Math.round(motionEvent.getX()),
        Math.round(motionEvent.getY()));
    // from the graphics overlay, get graphics near the tapped location
    final ListenableFuture<IdentifyGraphicsOverlayResult> identifyResultsFuture = mMapView
        .identifyGraphicsOverlayAsync(mGraphicsOverlay, screenPoint, 10, false);
    identifyResultsFuture.addDoneListener(new Runnable() {
      @Override public void run() {
        try {
          IdentifyGraphicsOverlayResult identifyGraphicsOverlayResult = identifyResultsFuture.get();
          List<Graphic> graphics = identifyGraphicsOverlayResult.getGraphics();
          // if a graphic has been identified
          if (!graphics.isEmpty()) {
            //get the first graphic identified
            Graphic identifiedGraphic = graphics.get(0);
            showCallout(identifiedGraphic);
          } else {
            // if no graphic identified
            mCallout.dismiss();
          }
        } catch (Exception e) {
          Log.e(TAG, "Identify error: " + e.getMessage());
        }
      }
    });
  }

  /**
   * Shows the Graphic's attributes as a Callout.
   *
   * @param graphic containing attributes
   */
  private void showCallout(final Graphic graphic) {
    // create a TextView for the Callout
    TextView calloutContent = new TextView(getApplicationContext());
    calloutContent.setTextColor(Color.BLACK);
    // set the text of the Callout to graphic's attributes
    if (graphic.getAttributes().get("PlaceName").toString().isEmpty()) {
      calloutContent.setText(graphic.getAttributes().get("Place_addr").toString());
    } else {
      calloutContent.setText(graphic.getAttributes().get("PlaceName") + "\n"
          + graphic.getAttributes().get("Place_addr"));
    }
    // get Callout
    mCallout = mMapView.getCallout();
    // set Callout options: animateCallout: true, recenterMap: false, animateRecenter: false
    mCallout.setShowOptions(new Callout.ShowOptions(true, false, false));
    mCallout.setContent(calloutContent);
    // set the leader position and show the callout
    // set the leader position and show the callout
    Point calloutLocation = graphic.computeCalloutLocation(graphic.getGeometry().getExtent().getCenter(), mMapView);
    mCallout.setGeoElement(graphic, calloutLocation);
    mCallout.show();
  }

  /**
   * Geocode an address passed in by the user.
   *
   * @param address read in from searchViews
   */
  private void geoCodeTypedAddress(final String address) {
    // check that address isn't null
    if (address != null) {

      // Execute async task to find the address
      mLocatorTask.addDoneLoadingListener(new Runnable() {
        @Override
        public void run() {
          if (mLocatorTask.getLoadStatus() == LoadStatus.LOADED) {
            // Call geocodeAsync passing in an address
            final ListenableFuture<List<GeocodeResult>> geocodeResultListenableFuture = mLocatorTask
                .geocodeAsync(address, mAddressGeocodeParameters);
            geocodeResultListenableFuture.addDoneListener(new Runnable() {
              @Override
              public void run() {
                try {
                  // Get the results of the async operation
                  List<GeocodeResult> geocodeResults = geocodeResultListenableFuture.get();
                  if (geocodeResults.size() > 0) {
                    displaySearchResult(geocodeResults.get(0));
                  } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.location_not_found) + address,
                        Toast.LENGTH_LONG).show();
                  }
                } catch (InterruptedException | ExecutionException e) {
                  Log.e(TAG, "Geocode error: " + e.getMessage());
                  Toast.makeText(getApplicationContext(), getString(R.string.geo_locate_error), Toast.LENGTH_LONG)
                      .show();
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
  }

  /**
   * Turns a GeocodeResult into a Point and adds it to a GraphicOverlay which is then drawn on the map.
   *
   * @param geocodeResult a single geocode result
   */
  private void displaySearchResult(GeocodeResult geocodeResult) {
    // dismiss any callout
    if (mMapView.getCallout() != null && mMapView.getCallout().isShowing()) {
      mMapView.getCallout().dismiss();
    }
    // clear map of existing graphics
    mMapView.getGraphicsOverlays().clear();
    mGraphicsOverlay.getGraphics().clear();
    // create graphic object for resulting location
    Point resultPoint = geocodeResult.getDisplayLocation();
    Graphic resultLocGraphic = new Graphic(resultPoint, geocodeResult.getAttributes(), mPinSourceSymbol);
    // add graphic to location layer
    mGraphicsOverlay.getGraphics().add(resultLocGraphic);
    // zoom map to result over 3 seconds
    mMapView.setViewpointAsync(new Viewpoint(geocodeResult.getExtent()), 3).addDoneListener(() -> showCallout(resultLocGraphic));
    // set the graphics overlay to the map
    mMapView.getGraphicsOverlays().add(mGraphicsOverlay);

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
