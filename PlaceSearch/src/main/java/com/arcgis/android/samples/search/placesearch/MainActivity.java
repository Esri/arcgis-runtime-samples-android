/* Copyright 2014 ESRI
 *
 * All rights reserved under the copyright laws of the United States
 * and applicable international laws, treaties, and conventions.
 *
 * You may freely redistribute and use this sample code, with or
 * without modification, provided you include the original copyright
 * notice and use restrictions.
 *
 * See the Sample code usage restrictions document for further information.
 *
 */

package com.arcgis.android.samples.search.placesearch;

import android.app.Activity;
import android.app.ProgressDialog;
import android.database.MatrixCursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.SearchView.OnSuggestionListener;
import android.widget.SimpleCursorAdapter;

import com.esri.android.map.MapView;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.android.toolkit.map.MapViewHelper;
import com.esri.core.tasks.geocode.Locator;
import com.esri.core.tasks.geocode.LocatorFindParameters;
import com.esri.core.tasks.geocode.LocatorGeocodeResult;
import com.esri.core.tasks.geocode.LocatorSuggestionParameters;
import com.esri.core.tasks.geocode.LocatorSuggestionResult;

import java.util.List;


public class MainActivity extends Activity {

  private static final String TAG = "PlaceSearch";
  private static final String COLUMN_NAME_ADDRESS = "address";
  private static final String COLUMN_NAME_X = "x";
  private static final String COLUMN_NAME_Y = "y";
  private static final String LOCATION_TITLE = "Location";

  private MapView mMapView;
  private String mMapViewState;
  // Entry point to ArcGIS for Android Toolkit
  private MapViewHelper mMapViewHelper;

  private Locator mLocator;
  private SearchView mSearchView;
  private MatrixCursor mSuggestionCursor;
  private SimpleCursorAdapter mSuggestionAdapter;

  private static ProgressDialog mProgressDialog;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // Setup and show progress dialog
    mProgressDialog = new ProgressDialog(this) {
      @Override
      public void onBackPressed() {
        // Back key pressed - just dismiss the dialog
        mProgressDialog.dismiss();
      }
    };

    // After the content of this activity is set the map can be accessed from the layout
    mMapView = (MapView) findViewById(R.id.map);
    // Initialize the helper class to use the Toolkit
    mMapViewHelper = new MapViewHelper(mMapView);
    // Create the default ArcGIS online Locator. If you want to provide your own {@code Locator},
    // user other methods of Locator.
    mLocator = Locator.createOnlineLocator();

    // set logo and enable wrap around
    mMapView.setEsriLogoVisible(true);
    mMapView.enableWrapAround(true);

    // Setup listener for map initialized
    mMapView.setOnStatusChangedListener(new OnStatusChangedListener() {

      @Override
      public void onStatusChanged(Object source, STATUS status) {
        if (source == mMapView && status == STATUS.INITIALIZED) {

          if (mMapViewState == null) {
            Log.i(TAG, "MapView.setOnStatusChangedListener() status=" + status.toString());
          } else {
            mMapView.restoreState(mMapViewState);
          }

        }
      }
    });

  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();
    if (id == R.id.action_search) {
      // Create search view and display on the Action Bar
      initSearchView(item);
      item.setActionView(mSearchView);
      return true;
    } else if (id == R.id.action_clear) {
      // Remove all the marker graphics
      if (mMapViewHelper != null) {
        mMapViewHelper.removeAllGraphics();
      }
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  // Create suggestion list
  private void suggestPlace(String query) {
    if (mLocator == null)
      return;

    new SuggestPlaceTask(mLocator).execute(query);
  }

  // Initialize suggestion cursor
  private void initSuggestionCursor() {
    String[] cols = new String[]{BaseColumns._ID, COLUMN_NAME_ADDRESS, COLUMN_NAME_X, COLUMN_NAME_Y};
    mSuggestionCursor = new MatrixCursor(cols);
  }

  // Set the suggestion cursor to an Adapter then set it to the search view
  private void applySuggestionCursor() {
    String[] cols = new String[]{COLUMN_NAME_ADDRESS};
    int[] to = new int[]{R.id.suggestion_item_address};

    mSuggestionAdapter = new SimpleCursorAdapter(mMapView.getContext(), R.layout.suggestion_item, mSuggestionCursor, cols, to, 0);
    mSearchView.setSuggestionsAdapter(mSuggestionAdapter);
    mSuggestionAdapter.notifyDataSetChanged();
  }

  // Initialize search view and add event listeners to handle query text changes and suggestion
  private void initSearchView(final MenuItem searchMenuItem) {
    if (mMapView == null || !mMapView.isLoaded())
      return;

    mSearchView = new SearchView(this);
    mSearchView.setFocusable(true);
    mSearchView.setIconifiedByDefault(false);
    mSearchView.setQueryHint(getResources().getString(R.string.search_hint));

    mSearchView.setOnQueryTextListener(new OnQueryTextListener() {

      @Override
      public boolean onQueryTextSubmit(String query) {
        return false;
      }

      @Override
      public boolean onQueryTextChange(String newText) {
        suggestPlace(newText);
        return true;
      }
    });

    mSearchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
      @Override
      public void onFocusChange(View view, boolean hasFocus) {
        if (!hasFocus) {
          searchMenuItem.collapseActionView();
          invalidateOptionsMenu();
        }
      }
    });

    mSearchView.setOnSuggestionListener(new OnSuggestionListener() {

      @Override
      public boolean onSuggestionSelect(int position) {
        return false;
      }

      @Override
      public boolean onSuggestionClick(int position) {
        // Obtain the content of the selected suggesting place via cursor
        MatrixCursor cursor = (MatrixCursor) mSearchView.getSuggestionsAdapter().getItem(position);
        int indexColumnSuggestion = cursor.getColumnIndex(COLUMN_NAME_ADDRESS);
        int indexColumnX = cursor.getColumnIndex(COLUMN_NAME_X);
        int indexColumnY = cursor.getColumnIndex(COLUMN_NAME_Y);
        String address = cursor.getString(indexColumnSuggestion);
        double x = cursor.getDouble(indexColumnX);
        double y = cursor.getDouble(indexColumnY);

        if (x == 0.0 && y == 0.0) {
          // Place has not been located. Find the place
          new FindPlaceTask(mLocator).execute(address);
        } else {
          // Place has been located. Zoom to the place and add a marker for this place
          mMapViewHelper.addMarkerGraphic(y, x, LOCATION_TITLE, address, android.R.drawable.ic_menu_myplaces, null, false, 1);
          mMapView.centerAndZoom(y, x, 14);
          mSearchView.setQuery(address, true);
        }
        cursor.close();

        return true;
      }
    });
  }

  // Find the address
  private class FindPlaceTask extends AsyncTask<String, Void, List<LocatorGeocodeResult>> {
    private static final String SUGGESTION_ADDRESS_DELIMNATOR = ", ";
    private Locator mLocator;

    public FindPlaceTask(Locator locator) {
      mLocator = locator;
    }

    @Override
    protected List<LocatorGeocodeResult> doInBackground(String... queries) {
      for (String query : queries) {
        // Create Locator parameters from single line address string
        LocatorFindParameters params;
        int index = query.indexOf(SUGGESTION_ADDRESS_DELIMNATOR);
        if (index > 0) {
          params = new LocatorFindParameters(query.substring(index + SUGGESTION_ADDRESS_DELIMNATOR.length()));
        } else {
          params = new LocatorFindParameters(query);
        }
        // Use the centre of the current map extent as the find location point
        params.setLocation(mMapView.getCenter(), mMapView.getSpatialReference());
        // Set the radial search distance in meters
        params.setDistance(500.0);

        // Execute the task
        List<LocatorGeocodeResult> results = null;
        try {
          results = mLocator.find(params);
        } catch (Exception e) {
          e.printStackTrace();
        }

        return results;
      }

      return null;
    }

    @Override
    protected void onPreExecute() {
      // Display progress dialog on UI thread
      mProgressDialog.setMessage(getString(R.string.address_search));
      mProgressDialog.show();
    }

    @Override
    protected void onPostExecute(List<LocatorGeocodeResult> results) {
      // Dismiss progress dialog
      mProgressDialog.dismiss();
      if (results == null || results.size() == 0)
        return;

      // Add the first result to the map and zoom to it
      LocatorGeocodeResult result = results.get(0);
      double x = result.getLocation().getX();
      double y = result.getLocation().getY();
      String address = result.getAddress();
      // Add a marker at the found place. When tapping on the marker, a Callout with the address
      // will be displayed
      mMapViewHelper.addMarkerGraphic(y, x, LOCATION_TITLE, address, android.R.drawable.ic_menu_myplaces, null, false, 1);
      mMapView.centerAndZoom(y, x, 14);
      mSearchView.setQuery(address, true);
    }
  }

  // Obtain a list of search suggestions.
  private class SuggestPlaceTask extends AsyncTask<String, Void, List<LocatorSuggestionResult>> {
    private Locator mLocator;

    public SuggestPlaceTask(Locator locator) {
      mLocator = locator;
    }

    @Override
    protected List<LocatorSuggestionResult> doInBackground(String... queries) {
      for (String query : queries) {
        // Create suggestion parameter
        LocatorSuggestionParameters params = new LocatorSuggestionParameters(query);
        //Set the location to be used for proximity based suggestion
        params.setLocation(mMapView.getCenter(), mMapView.getSpatialReference());
        // Set the radial search distance in meters
        params.setDistance(500.0);

        List<LocatorSuggestionResult> results = null;
        try {
          results = mLocator.suggest(params);
        } catch (Exception e) {
          e.printStackTrace();
        }

        return results;
      }

      return null;
    }

    @Override
    protected void onPostExecute(List<LocatorSuggestionResult> results) {
      if (results == null) {
        return;
      }

      int key = 0;
      // Add suggestion list to a cursor
      initSuggestionCursor();
      for (LocatorSuggestionResult result : results) {
        mSuggestionCursor.addRow(new Object[]{key++, result.getText(), "0", "0"});
      }

      applySuggestionCursor();
    }
  }


  @Override
  protected void onDestroy() {
    super.onDestroy();
  }

  @Override
  protected void onPause() {
    super.onPause();

    mMapViewState = mMapView.retainState();
    mMapView.pause();
  }

  @Override
  protected void onResume() {
    super.onResume();

    // Start the MapView running again
    if (mMapView != null) {
      mMapView.unpause();
      if (mMapViewState != null) {
        mMapView.restoreState(mMapViewState);
      }
    }
  }

}
