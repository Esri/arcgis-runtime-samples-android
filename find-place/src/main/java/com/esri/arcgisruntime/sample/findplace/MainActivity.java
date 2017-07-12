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

import android.content.Context;
import android.database.MatrixCursor;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.PictureMarkerSymbol;
import com.esri.arcgisruntime.tasks.geocode.GeocodeParameters;
import com.esri.arcgisruntime.tasks.geocode.GeocodeResult;
import com.esri.arcgisruntime.tasks.geocode.LocatorTask;
import com.esri.arcgisruntime.tasks.geocode.SuggestParameters;
import com.esri.arcgisruntime.tasks.geocode.SuggestResult;

public class MainActivity extends AppCompatActivity {

  private static final String COLUMN_NAME_ADDRESS = "address";
  private String TAG = "Find A Place";
  private MapView mMapView;
  private SearchView mPoiSearchView;
  private SearchView mPlaceSearchView;
  private LocatorTask mLocatorTask;
  private GraphicsOverlay mGraphicsOverlay;
  private SimpleCursorAdapter mSuggestionAdapter;
  private GeocodeResult mGeocodedLocation;
  private SuggestParameters mSuggestParameters;
  private GeocodeParameters mGeocodeParameters;
  private PictureMarkerSymbol mPinSourceSymbol;
  private Geometry mCurrentExtent;
  String[] mColumnNames;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // inflate MapView from layout
    mMapView = (MapView) findViewById(R.id.mapView);
    // create a map with the BasemapType topographic
    ArcGISMap map = new ArcGISMap(Basemap.createTopographic());
    // set the map to be displayed in this view
    mMapView.setMap(map);
    // create a locator task
    mLocatorTask = new LocatorTask("http://geocode.arcgis.com/arcgis/rest/services/World/GeocodeServer");

    mGraphicsOverlay = new GraphicsOverlay();

    BitmapDrawable startDrawable = (BitmapDrawable) ContextCompat.getDrawable(this, R.drawable.pin);
    try {
      mPinSourceSymbol = PictureMarkerSymbol.createAsync(startDrawable).get();
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
    }
    mColumnNames = new String[] { BaseColumns._ID, COLUMN_NAME_ADDRESS };

    mSuggestParameters = new SuggestParameters();
    mSuggestParameters.getCategories().add("POI");
    mSuggestParameters.setMaxResults(6);
    mGeocodeParameters = new GeocodeParameters();
    mGeocodeParameters.getResultAttributeNames().add("*");
    mGeocodeParameters.setMaxResults(6);
    //mMapView.addViewpointChangedListener(new ViewpointChangedListener() {
    //  @Override public void viewpointChanged(ViewpointChangedEvent viewpointChangedEvent) {
    // Get the current map extent
    //    mCurrentExtent = mMapView.getCurrentViewpoint(Viewpoint.Type.BOUNDING_GEOMETRY).getTargetGeometry();
    //    mSuggestParameters.setSearchArea(mCurrentExtent);
    //  }
    //});

    setSearchViews();
  }

  private void setSearchViews() {
    mPoiSearchView = (SearchView) findViewById(R.id.poiSearchView);
    mPlaceSearchView = (SearchView) findViewById(R.id.placeSearchView);
    mPoiSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

      @Override
      public boolean onQueryTextSubmit(String query) {
        geoCodeTypedAddress(query);
        mPoiSearchView.clearFocus();
        return true;
      }

      @Override
      public boolean onQueryTextChange(String newText) {
        Log.d(TAG, "text changed");

        // get suggestions from the locatorTask
        if (!newText.equals("")) {
          final ListenableFuture<List<SuggestResult>> suggestionsFuture = mLocatorTask.suggestAsync(newText, mSuggestParameters);
          suggestionsFuture.addDoneListener(new Runnable() {

            @Override public void run() {
              try {
                // Get the results of the async operation
                List<SuggestResult> suggestResults = suggestionsFuture.get();
                final List<String> suggestedAddresses = new ArrayList<>();
                Log.d(TAG, "suggestResults length " + suggestResults.size());
                MatrixCursor suggestionsCursor = new MatrixCursor(mColumnNames);
                int key = 0;
                // Iterate the suggestions
                for (SuggestResult result : suggestResults) {
                  suggestedAddresses.add(result.getLabel());
                  Log.d("resultlabel", result.getLabel());
                  suggestionsCursor.addRow(new Object[] { key++, result.getLabel() });
                  //Log.d("cursor", mSuggestionCursor.getString(0));
                }
                Log.d(TAG, "suggestedAddresses length " + suggestedAddresses.size());

                String[] cols = new String[] { COLUMN_NAME_ADDRESS };
                int[] to = new int[] { R.id.suggestion_item_address };
                mSuggestionAdapter = new SimpleCursorAdapter(MainActivity.this, R.layout.suggestion, suggestionsCursor, cols, to, 0);
                mPoiSearchView.setSuggestionsAdapter(mSuggestionAdapter);
                //mSuggestionAdapter.notifyDataSetChanged();
                mPoiSearchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
                  @Override public boolean onSuggestionSelect(int position) {
                    return false;
                  }

                  @Override public boolean onSuggestionClick(int position) {
                    MatrixCursor selectedRow = (MatrixCursor) mSuggestionAdapter.getItem(position);
                    int selectedCursorIndex = selectedRow.getColumnIndex(COLUMN_NAME_ADDRESS);
                    final String address = selectedRow.getString(selectedCursorIndex);
                    geoCodeTypedAddress(address);
                    mPoiSearchView.setQuery(address, false);
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
    mGeocodedLocation = null;

    // Execute async task to find the address
    mLocatorTask.addDoneLoadingListener(new Runnable() {
      @Override
      public void run() {
        if (mLocatorTask.getLoadStatus() == LoadStatus.LOADED) {
          // Call geocodeAsync passing in an address
          final ListenableFuture<List<GeocodeResult>> geocodeFuture = mLocatorTask
              .geocodeAsync(address, mGeocodeParameters);
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
                      getString(R.string.location_not_found) + address,
                      Toast.LENGTH_LONG).show();
                }

              } catch (InterruptedException | ExecutionException e) {
                // Deal with exception...
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), getString(R.string.geo_locate_error), Toast.LENGTH_LONG).show();

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

  private void displaySearchResult(Point resultPoint, String address) {
    if (mMapView.getCallout().isShowing()) {
      mMapView.getCallout().dismiss();
    }
    //remove any previous graphics/search results
    //mMapView.getGraphicsOverlays().clear();
    mGraphicsOverlay.getGraphics().clear();
    // create graphic object for resulting location
    Graphic resultLocGraphic = new Graphic(resultPoint, mPinSourceSymbol);
    // add graphic to location layer
    mGraphicsOverlay.getGraphics().add(resultLocGraphic);
    // Zoom map to geocode result location
    mMapView.setViewpointAsync(new Viewpoint(resultPoint, 8000), 3);
    //mGraphicPoint = resultPoint;
    //mGraphicPointAddress = address;
  }

  /**
   * Hides soft keyboard
   */
  private void hideKeyboard() {
    mPoiSearchView.clearFocus();
    InputMethodManager inputManager = (InputMethodManager) getApplicationContext()
        .getSystemService(Context.INPUT_METHOD_SERVICE);
    inputManager.hideSoftInputFromWindow(mPoiSearchView.getWindowToken(), 0);
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
