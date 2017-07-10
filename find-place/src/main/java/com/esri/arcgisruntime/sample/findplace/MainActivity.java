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
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SearchView;
import android.widget.Spinner;
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
import com.esri.arcgisruntime.mapping.view.ViewpointChangedEvent;
import com.esri.arcgisruntime.mapping.view.ViewpointChangedListener;
import com.esri.arcgisruntime.symbology.PictureMarkerSymbol;
import com.esri.arcgisruntime.tasks.geocode.GeocodeParameters;
import com.esri.arcgisruntime.tasks.geocode.GeocodeResult;
import com.esri.arcgisruntime.tasks.geocode.LocatorTask;
import com.esri.arcgisruntime.tasks.geocode.SuggestParameters;
import com.esri.arcgisruntime.tasks.geocode.SuggestResult;

public class MainActivity extends AppCompatActivity {

  private String TAG = "Find A Place";

  private MapView mMapView;
  private SearchView mSearchView;
  private LocatorTask mLocatorTask;
  private GraphicsOverlay mGraphicsOverlay;
  private ArrayAdapter<CharSequence> mAdapter;
  private Spinner mSpinner;
  private GeocodeResult mGeocodedLocation;
  private SuggestParameters mSuggestParameters;
  private GeocodeParameters mGeocodeParameters;
  private PictureMarkerSymbol mPinSourceSymbol;
  private Geometry mCurrentExtent;

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

    mSuggestParameters = new SuggestParameters();
    mSuggestParameters.getCategories().add("POI");
    mSuggestParameters.setMaxResults(6);
    mGeocodeParameters = new GeocodeParameters();
    mGeocodeParameters.getResultAttributeNames().add("*");
    mGeocodeParameters.setMaxResults(6);
    mMapView.addViewpointChangedListener(new ViewpointChangedListener() {
      @Override public void viewpointChanged(ViewpointChangedEvent viewpointChangedEvent) {
        // Get the current map extent
        mCurrentExtent = mMapView.getCurrentViewpoint(Viewpoint.Type.BOUNDING_GEOMETRY).getTargetGeometry();
        mSuggestParameters.setSearchArea(mCurrentExtent);
      }
    });

    setSearchView();

  }

  private void setSearchView() {
    mSearchView = (SearchView) findViewById(R.id.searchView);
    mSearchView.setIconifiedByDefault(true);
    mSearchView.setQueryHint(getResources().getString(R.string.search_hint));
    mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
      @Override
      public boolean onQueryTextSubmit(String query) {
        //hideKeyboard();
        geoCodeTypedAddress(query);
        mSearchView.clearFocus();
        return true;
      }

      @Override
      public boolean onQueryTextChange(String newText) {
        Log.d(TAG, "text changed");
        // get suggestions from the locatorTask
        final ListenableFuture<List<SuggestResult>> suggestionsFuture = mLocatorTask
            .suggestAsync(newText, mSuggestParameters);
        suggestionsFuture.addDoneListener(new Runnable() {

          @Override public void run() {
            try {
              // Get the results of the async operation
              List<SuggestResult> suggestResults = suggestionsFuture.get();
              List<String> suggestedAddresses = new ArrayList<>(suggestResults.size());

              // Iterate the suggestions
              for (SuggestResult result : suggestResults) {
                suggestedAddresses.add(result.getLabel());
              }
              mAdapter.addAll(suggestedAddresses);

              // Apply the adapter to the spinner
              mSpinner.setAdapter(mAdapter);
              mSpinner.setSelection(mAdapter.getCount());
              mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                  if (position == mAdapter.getCount()) {
                    mSearchView.clearFocus();
                  } else {
                    mSearchView.setQuery(getResources().getStringArray(R.array.suggestion_items)[position], false);
                    geoCodeTypedAddress(getResources().getStringArray(R.array.suggestion_items)[position]);
                    mSearchView.setIconified(false);
                    mSearchView.clearFocus();
                  }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
              });

              // Specify the layout to use when the list of choices appears
              mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            } catch (Exception e) {
              e.printStackTrace();
            }

          }
        });
        return true;
      }
    });

    mSpinner = (Spinner) findViewById(R.id.spinner);
    // Create an ArrayAdapter using the string array and a default spinner layout
    mAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_dropdown_item) {
      @Override
      public View getView(int position, View convertView, ViewGroup parent) {
        View v = super.getView(position, convertView, parent);
        if (position == getCount()) {
          mSearchView.clearFocus();
        }
        return v;
      }

      @Override
      public int getCount() {
        return super.getCount() - 1; // you dont display last item. It is used as hint.
      }
    };

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
    mSearchView.clearFocus();
    InputMethodManager inputManager = (InputMethodManager) getApplicationContext()
        .getSystemService(Context.INPUT_METHOD_SERVICE);
    inputManager.hideSoftInputFromWindow(mSearchView.getWindowToken(), 0);
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
