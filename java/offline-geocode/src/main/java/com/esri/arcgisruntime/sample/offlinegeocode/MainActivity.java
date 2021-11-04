/* Copyright 2018 Esri
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

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.TileCache;
import com.esri.arcgisruntime.geometry.Point;
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
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.esri.arcgisruntime.tasks.geocode.GeocodeParameters;
import com.esri.arcgisruntime.tasks.geocode.GeocodeResult;
import com.esri.arcgisruntime.tasks.geocode.LocatorTask;
import com.esri.arcgisruntime.tasks.geocode.ReverseGeocodeParameters;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private GraphicsOverlay mGraphicsOverlay;
  private GeocodeParameters mGeocodeParameters;
  private MapView mMapView;
  private LocatorTask mLocatorTask;
  private ReverseGeocodeParameters mReverseGeocodeParameters;
  private SearchView mSearchView;
  private SimpleMarkerSymbol mPointSymbol;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // get a reference to the map view
    mMapView = findViewById(R.id.mapView);
    // define a map
    ArcGISMap map = new ArcGISMap();
    // set the map to the map view
    mMapView.setMap(map);
    // add a graphics overlay to the map view
    mGraphicsOverlay = new GraphicsOverlay();
    mMapView.getGraphicsOverlays().add(mGraphicsOverlay);
    // create a point symbol for showing the address location
    mPointSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.RED, 20.0f);
    // add a touch listener to the map view
    mMapView.setOnTouchListener(new CustomMapViewOnTouchListener(this, mMapView));

    // load the tile cache from local storage
    TileCache tileCache = new TileCache(getExternalFilesDir(null) + getString(R.string.san_diego_tpkx));
    // use the tile cache extent to set the view point
    tileCache.addDoneLoadingListener(() -> mMapView.setViewpoint(new Viewpoint(tileCache.getFullExtent())));
    // create a tiled layer and add it to as the base map
    ArcGISTiledLayer tiledLayer = new ArcGISTiledLayer(tileCache);
    mMapView.getMap().setBasemap(new Basemap(tiledLayer));
    // create geocode parameters
    mGeocodeParameters = new GeocodeParameters();
    mGeocodeParameters.getResultAttributeNames().add("*");
    mGeocodeParameters.setMaxResults(1);
    // create reverse geocode parameters
    mReverseGeocodeParameters = new ReverseGeocodeParameters();
    mReverseGeocodeParameters.getResultAttributeNames().add("*");
    mReverseGeocodeParameters.setOutputSpatialReference(mMapView.getMap().getSpatialReference());
    mReverseGeocodeParameters.setMaxResults(1);
    // load the locator task from external storage
    mLocatorTask = new LocatorTask(
        getExternalFilesDir(null) + getResources().getString(R.string.san_diego_loc));
    mLocatorTask.loadAsync();

    mSearchView = findViewById(R.id.searchView);
    mSearchView.setIconifiedByDefault(true);
    mSearchView.setQueryHint(getString(R.string.search_hint));
    mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
      @Override
      public boolean onQueryTextSubmit(String query) {
        geoCodeTypedAddress(query);
        mSearchView.clearFocus();
        return true;
      }

      @Override
      public boolean onQueryTextChange(String newText) {
        return false;
      }
    });
    // create an array adapter using the string array and a default spinner layout
    final ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this,
        android.R.layout.simple_spinner_dropdown_item) {
      @Override
      public int getCount() {
        return super.getCount() - 1;
      }
    };
    // specify the layout to use when the list of choices appears
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    adapter.addAll(getResources().getStringArray(R.array.suggestion_items));
    Spinner spinner = findViewById(R.id.spinner);
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
      // set vertical offset to spinner dropdown for API less than 21
      spinner.setDropDownVerticalOffset(80);
    }
    // Apply the adapter to the spinner
    spinner.setAdapter(adapter);
    spinner.setSelection(adapter.getCount());
    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (position == adapter.getCount()) {
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
  }

  /**
   * Use the locator task to geocode the the given address.
   *
   * @param address as a string
   */
  private void geoCodeTypedAddress(final String address) {
    // Execute async task to find the address
    mLocatorTask.addDoneLoadingListener(() -> {
      if (mLocatorTask.getLoadStatus() == LoadStatus.LOADED) {
        // get a list of geocode results for the given address
        ListenableFuture<List<GeocodeResult>> geocodeFuture = mLocatorTask.geocodeAsync(address, mGeocodeParameters);
        geocodeFuture.addDoneListener(() -> {
          try {
            // get the geocode results
            List<GeocodeResult> geocodeResults = geocodeFuture.get();
            if (!geocodeResults.isEmpty()) {
              // get the first result
              GeocodeResult geocodeResult = geocodeResults.get(0);
              displayGeocodeResult(geocodeResult.getDisplayLocation(), geocodeResult.getLabel());
            } else {
              Toast.makeText(this, "No location found for: " + address, Toast.LENGTH_LONG).show();
            }
          } catch (InterruptedException | ExecutionException e) {
            String error = "Error getting geocode result: " + e.getMessage();
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            Log.e(TAG, error);
          }
        });
      } else {
        String error = "Error loading locator task: " + mLocatorTask.getLoadError().getMessage();
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        Log.e(TAG, error);
      }
    });
  }

  /**
   * Uses the locator task to reverse geocode for the given point.
   *
   * @param point on which to perform the reverse geocode
   */
  private void reverseGeocode(Point point) {
    final ListenableFuture<List<GeocodeResult>> results = mLocatorTask.reverseGeocodeAsync(point, mReverseGeocodeParameters);
    try {
      List<GeocodeResult> geocodeResults = results.get();
      if (!geocodeResults.isEmpty()) {
        // get the top result
        GeocodeResult geocode = geocodeResults.get(0);
        String detail;
        // attributes from a click-based search
        String street = geocode.getAttributes().get("StAddr").toString();
        String city = geocode.getAttributes().get("City").toString();
        String region = geocode.getAttributes().get("Region").toString();
        String postCode = geocode.getAttributes().get("Postal").toString();
        detail = city + ", " + region + ", " + postCode;
        String address = street + ", " + detail;
        displayGeocodeResult(point, address);
      }
    } catch (ExecutionException | InterruptedException e) {
      String error = "Error getting geocode results: " + e.getMessage();
      Toast.makeText(this, error, Toast.LENGTH_LONG).show();
      Log.e(TAG, error);
    }
  }

  /**
   * Draw a point and open a callout showing geocode results on map.
   *
   * @param resultPoint geometry to show where the geocode result is
   * @param address     to display in the associated callout
   */
  private void displayGeocodeResult(Point resultPoint, CharSequence address) {
    // dismiss the callout if showing
    if (mMapView.getCallout().isShowing()) {
      mMapView.getCallout().dismiss();
    }
    // remove any previous graphics/search results
    mGraphicsOverlay.getGraphics().clear();
    // create graphic object for resulting location
    Graphic pointGraphic = new Graphic(resultPoint, mPointSymbol);
    // add graphic to location layer
    mGraphicsOverlay.getGraphics().add(pointGraphic);
    // Zoom map to geocode result location
    mMapView.setViewpointAsync(new Viewpoint(resultPoint, 8000), 3);
    showCallout(resultPoint, address);
  }

  /**
   * Show a callout at the given point with the given text.
   *
   * @param point to define callout location
   * @param text to define callout content
   */
  private void showCallout(Point point, CharSequence text) {
    Callout callout = mMapView.getCallout();
    TextView calloutTextView = new TextView(this);
    calloutTextView.setText(text);
    callout.setLocation(point);
    callout.setContent(calloutTextView);
    callout.show();
  }

  /**
   * Define a listener to handle drag events.
   */
  private class DragTouchListener extends DefaultMapViewOnTouchListener {

    DragTouchListener(Context context, MapView mapView) {
      super(context, mapView);
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
      switch (event.getAction()) {
        case MotionEvent.ACTION_MOVE:
          final int pointerIndex = event.getActionIndex();
          final float x = event.getX(pointerIndex);
          final float y = event.getY(pointerIndex);
          android.graphics.Point screenPoint = new android.graphics.Point(Math.round(x), Math.round(y));
          final Point singleTapPoint = mMapView.screenToLocation(screenPoint);
          reverseGeocode(singleTapPoint);
          break;
        case MotionEvent.ACTION_UP:
          if (!mGraphicsOverlay.getGraphics().isEmpty()) {
            mGraphicsOverlay.getGraphics().get(0).setSelected(false);
            mMapView.setOnTouchListener(new CustomMapViewOnTouchListener(getApplicationContext(), mMapView));
          }
          break;
        default:
          return true;
      }
      return true;
    }
  }

  /**
   * Define a listener to handle long press (reverse geocode the point) and single taps (select the tapped graphic).
   */
  private class CustomMapViewOnTouchListener extends DefaultMapViewOnTouchListener {

    CustomMapViewOnTouchListener(Context context, MapView mapView) {
      super(context, mapView);
    }

    @Override
    public void onLongPress(MotionEvent event) {
      android.graphics.Point screenPoint = new android.graphics.Point(Math.round(event.getX()), Math.round(event.getY()));
      Point mapPoint = mMapView.screenToLocation(screenPoint);
      reverseGeocode(mapPoint);
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent event) {
      if (!mGraphicsOverlay.getGraphics().isEmpty()) {
        if (mGraphicsOverlay.getGraphics().get(0).isSelected()) {
          mGraphicsOverlay.getGraphics().get(0).setSelected(false);
        }
      }
      // get the screen point where user tapped
      final android.graphics.Point screenPoint = new android.graphics.Point((int) event.getX(),
          (int) event.getY());
      // identify graphics on the graphics overlay
      final ListenableFuture<IdentifyGraphicsOverlayResult> identifyGraphic = mMapView
          .identifyGraphicsOverlayAsync(mGraphicsOverlay, screenPoint, 10.0, false, 1);
      identifyGraphic.addDoneListener(() -> {
        try {
          IdentifyGraphicsOverlayResult grOverlayResult = identifyGraphic.get();
          // get the list of graphics returned by identify
          List<Graphic> graphic = grOverlayResult.getGraphics();
          // if identified graphic is not empty, start DragTouchListener
          if (!graphic.isEmpty()) {
            graphic.get(0).setSelected(true);
            Toast.makeText(MainActivity.this, getString(R.string.reverse_geocode_message), Toast.LENGTH_SHORT).show();
            mMapView.setOnTouchListener(new DragTouchListener(getApplicationContext(), mMapView));
          }
        } catch (InterruptedException | ExecutionException e) {
          String error = "Error identifying graphic: " + e.getMessage();
          Toast.makeText(MainActivity.this, error, Toast.LENGTH_LONG).show();
          Log.e(TAG, error);
        }
      });
      return super.onSingleTapConfirmed(event);
    }
  }

  @Override
  protected void onPause() {
    mMapView.pause();
    super.onPause();
  }

  @Override
  protected void onResume() {
    super.onResume();
    mMapView.resume();
  }

  @Override
  protected void onDestroy() {
    mMapView.dispose();
    super.onDestroy();
  }
}
