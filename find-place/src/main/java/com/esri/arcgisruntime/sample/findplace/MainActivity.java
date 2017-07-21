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

package com.esri.arcgisruntime.sample.findplace;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import android.database.MatrixCursor;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
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
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
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

  private static final String COLUMN_NAME_ADDRESS = "address";
  String[] mColumnNames;
  private String TAG;
  private MapView mMapView;
  private SearchView mSearchSearchView;
  private SearchView mLocationSearchView;
  private LocatorTask mLocatorTask;
  private GraphicsOverlay mGraphicsOverlay;
  private GeocodeResult mGeocodeResult;
  private SuggestParameters mSearchSuggestParameters;
  private GeocodeParameters mSearchGeocodeParameters;
  private SuggestParameters mLocationSuggestParameters;
  private GeocodeParameters mLocationGeocodeParameters;
  private PictureMarkerSymbol mPinSourceSymbol;
  private Geometry mCurrentExtent;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    TAG = "Find a place";

    // setup SearchViews
    mSearchSearchView = (SearchView) findViewById(R.id.searchSearchView);
    mSearchSearchView.setIconified(false);
    mSearchSearchView.setQueryHint(getResources().getString(R.string.search_hint));
    mLocationSearchView = (SearchView) findViewById(R.id.locationSearchView);
    mLocationSearchView.setIconified(false);
    mLocationSearchView.setQueryHint(getResources().getString(R.string.location_search_hint));
    // inflate MapView from layout
    mMapView = (MapView) findViewById(R.id.mapView);
    // disable map wraparound
    mMapView.setWrapAroundMode(WrapAroundMode.DISABLED);
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
      Toast.makeText(getApplicationContext(), "Failed to load pin drawable!", Toast.LENGTH_LONG);
    }
    mColumnNames = new String[] { BaseColumns._ID, COLUMN_NAME_ADDRESS };
    mMapView.addViewpointChangedListener(new ViewpointChangedListener() {
      @Override public void viewpointChanged(ViewpointChangedEvent viewpointChangedEvent) {
        //Get the current map extent
        mCurrentExtent = mMapView.getCurrentViewpoint(Viewpoint.Type.BOUNDING_GEOMETRY).getTargetGeometry();
      }
    });
    setupSearch();
    setupLocation();
  }

  private void setupSearch() {

    mSearchSuggestParameters = new SuggestParameters();
    mSearchSuggestParameters.getCategories().add("POI");
    mSearchSuggestParameters.setMaxResults(6);
    mSearchGeocodeParameters = new GeocodeParameters();
    mSearchGeocodeParameters.getResultAttributeNames().add("*");
    mSearchSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

      @Override
      public boolean onQueryTextSubmit(String address) {
        geoCodeTypedAddress(address);
        mSearchSearchView.clearFocus();
        mLocationSearchView.clearFocus();
        return true;
      }

      @Override
      public boolean onQueryTextChange(String newText) {
        // as long as newText isn't empty, get suggestions from the locatorTask
        if (!newText.equals("")) {
          mSearchSuggestParameters.setSearchArea(mCurrentExtent);
          final ListenableFuture<List<SuggestResult>> suggestionsFuture = mLocatorTask
              .suggestAsync(newText, mSearchSuggestParameters);
          suggestionsFuture.addDoneListener(new Runnable() {

            @Override public void run() {
              try {
                // Get the results of the async operation
                List<SuggestResult> suggestResults = suggestionsFuture.get();
                MatrixCursor suggestionsCursor = new MatrixCursor(mColumnNames);
                int key = 0;
                // Iterate the suggestions
                for (SuggestResult result : suggestResults) {
                  suggestionsCursor.addRow(new Object[] { key++, result.getLabel() });
                }

                String[] cols = new String[] { COLUMN_NAME_ADDRESS };
                int[] to = new int[] { R.id.suggestion_item_address };
                final SimpleCursorAdapter suggestionAdapter = new SimpleCursorAdapter(MainActivity.this,
                    R.layout.suggestion, suggestionsCursor, cols, to, 0);
                mSearchSearchView.setSuggestionsAdapter(suggestionAdapter);
                mSearchSearchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
                  @Override public boolean onSuggestionSelect(int position) {
                    return false;
                  }

                  @Override public boolean onSuggestionClick(int position) {
                    MatrixCursor selectedRow = (MatrixCursor) suggestionAdapter.getItem(position);
                    int selectedCursorIndex = selectedRow.getColumnIndex(COLUMN_NAME_ADDRESS);
                    final String address = selectedRow.getString(selectedCursorIndex);
                    geoCodeTypedAddress(address);
                    mSearchSearchView.setQuery(address, false);
                    mSearchSearchView.clearFocus();
                    mLocationSearchView.clearFocus();
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
    mLocationSuggestParameters.setMaxResults(3);
    mLocationGeocodeParameters = new GeocodeParameters();
    mLocationGeocodeParameters.getResultAttributeNames().add("*");
    mLocationGeocodeParameters.setMaxResults(3);
    mLocationSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
      @Override public boolean onQueryTextSubmit(String s) {
        return false;
      }

      @Override public boolean onQueryTextChange(String newText) {
        mLocationSuggestParameters.setSearchArea(mCurrentExtent);
        // as long as newText isn't empty, get suggestions from the locatorTask
        if (!newText.equals("")) {
          final ListenableFuture<List<SuggestResult>> suggestionsFuture = mLocatorTask
              .suggestAsync(newText, mLocationSuggestParameters);
          suggestionsFuture.addDoneListener(new Runnable() {

            @Override public void run() {
              try {
                // Get the results of the async operation
                List<SuggestResult> suggestResults = suggestionsFuture.get();
                MatrixCursor suggestionsCursor = new MatrixCursor(mColumnNames);
                int key = 0;
                // Iterate the suggestions
                for (SuggestResult result : suggestResults) {
                  suggestionsCursor.addRow(new Object[] { key++, result.getLabel() });
                }

                String[] cols = new String[] { COLUMN_NAME_ADDRESS };
                int[] to = new int[] { R.id.suggestion_item_address };
                final SimpleCursorAdapter suggestionAdapter = new SimpleCursorAdapter(MainActivity.this, R.layout.suggestion, suggestionsCursor,
                    cols, to, 0);
                mLocationSearchView.setSuggestionsAdapter(suggestionAdapter);
                mLocationSearchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
                  @Override public boolean onSuggestionSelect(int position) {
                    return false;
                  }

                  @Override public boolean onSuggestionClick(int position) {
                    // on suggestion
                    MatrixCursor selectedRow = (MatrixCursor) suggestionAdapter.getItem(position);
                    int selectedCursorIndex = selectedRow.getColumnIndex(COLUMN_NAME_ADDRESS);
                    final String address = selectedRow.getString(selectedCursorIndex);
                    setSearchArea(address);
                    mLocationSearchView.setQuery(address, false);
                    mLocationSearchView.clearFocus();
                    mSearchSearchView.clearFocus();

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
   * Geocode an address typed in by user
   *
   * @param address
   */
  private void geoCodeTypedAddress(final String address) {
    // Null out any previously located result
    mGeocodeResult = null;
    mSearchGeocodeParameters.setSearchArea(mCurrentExtent);
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
                  // Use the first result as an example
                  // display on the map
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

  private void setSearchArea(final String address) {
    // Execute async task to find the address
    mLocatorTask.addDoneLoadingListener(new Runnable() {
      @Override
      public void run() {
        if (mLocatorTask.getLoadStatus() == LoadStatus.LOADED) {
          // Call geocodeAsync passing in an address
          final ListenableFuture<List<GeocodeResult>> geocodeFuture = mLocatorTask
              .geocodeAsync(address, mSearchGeocodeParameters);
          geocodeFuture.addDoneListener(new Runnable() {
            @Override
            public void run() {
              try {
                // Get the results of the async operation
                List<GeocodeResult> geocodeResults = geocodeFuture.get();
                if (geocodeResults.size() > 0) {
                  // Use the first result as an example
                  // display on the map
                  mGeocodeResult = geocodeResults.get(0);
                  Log.d(TAG, mGeocodeResult.getLabel().toString());
                  mSearchGeocodeParameters
                      .setPreferredSearchLocation(mGeocodeResult.getDisplayLocation());
                } else {
                  Toast.makeText(getApplicationContext(),
                      getString(R.string.location_not_found) + address, Toast.LENGTH_LONG).show();
                }
              } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), getString(R.string.geo_locate_error),
                    Toast.LENGTH_LONG).show();
              }
              // done processing and can remove this listener.
              geocodeFuture.removeDoneListener(this);
            }
          });
        }
      }
    });
  }

  private void displaySearchResult(List<GeocodeResult> geocodeResults) {
    Log.d(TAG, "displaySearchResult");
    if (mMapView.getCallout().isShowing()) {
      mMapView.getCallout().dismiss();
    }
    //remove any previous graphics/search results
    mMapView.getGraphicsOverlays().clear();
    mGraphicsOverlay.getGraphics().clear();
    List<Point> resultPoints = new ArrayList<>();
    for (GeocodeResult result : geocodeResults) {
      // create graphic object for resulting location
      Point resultPoint = result.getDisplayLocation();
      Graphic resultLocGraphic = new Graphic(resultPoint, mPinSourceSymbol);
      // add graphic to location layer
      mGraphicsOverlay.getGraphics().add(resultLocGraphic);
      resultPoints.add(resultPoint);
    }
    // create a geometry containing all result points
    Multipoint resultsMultipoint = new Multipoint(resultPoints);
    Envelope resultsEnvelope = resultsMultipoint.getExtent();
    // add a 25% buffer to the extent Envelope of result points
    Envelope resultsEnvelopeWithBuffer = new Envelope(resultsEnvelope.getCenter(), resultsEnvelope.getWidth() * 1.25,
        resultsEnvelope.getHeight() * 1.25);
    // zoom map to result points Envelope over 3 seconds
    mMapView.setViewpointAsync(new Viewpoint(resultsEnvelopeWithBuffer), 3);
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
}
