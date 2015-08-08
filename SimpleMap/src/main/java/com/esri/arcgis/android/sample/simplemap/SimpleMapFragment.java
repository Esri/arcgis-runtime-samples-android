/* Copyright 2015 Esri
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

package com.esri.arcgis.android.sample.simplemap;

import android.app.Fragment;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.esri.android.map.MapView;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.android.toolkit.geocode.GeocodeHelper;
import com.esri.android.toolkit.map.MapViewHelper;
import com.esri.core.geometry.Point;
import com.esri.core.map.CallbackListener;
import com.esri.core.tasks.geocode.Locator;
import com.esri.core.tasks.geocode.LocatorReverseGeocodeResult;

import java.util.Map;

public class SimpleMapFragment extends Fragment {

  // Keys and member variables to save address results.
  private static final String KEY_MAPSTATE = "mapState";
  String mMapState = null;

  private static final String KEY_RESULT_TITLE = "title";
  String mResultTitle = null;

  private static final String KEY_RESULT_SNIPPET = "snippet";
  String mResultSnippet = null;

  private static final String KEY_RESULT_X = "locationX";
  double mResultX = Double.NaN;

  private static final String KEY_RESULT_Y = "locationY";
  double mResultY = Double.NaN;

  // The MapView in the fragment layout, and a MapViewHelper that will be used 
  // with this map.
  MapView mMapView;

  MapViewHelper mMapViewHelper;

  // Locator to be used to perform a reverse geocode, finding an address for a
  // set of coordinates.
  Locator mLocator;

  // Fields from the reverse geocode result shown in the result graphic callout.
  final String[] mResultCalloutFields = { "Address", "City", "Region", "Postal" };

  // A drawable icon to draw reverse geocoded locations.
  Drawable mIcon;

  @Override
  public void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);

    // Initialize online locator.
    mLocator = Locator.createOnlineLocator();

    // Create a drawable icon to use when
    mIcon = getResources().getDrawable(R.drawable.route_destination);

  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View v = inflater.inflate(R.layout.fragment_simple_map, container, false);

    // Retrieve the map and map options from XML layout.
    mMapView = (MapView) v.findViewById(R.id.map);

    // Check for app state saved if the activity and fragment were destroyed.
    if (savedInstanceState != null) {
      mMapState = savedInstanceState.getString(KEY_MAPSTATE, null);
      mResultTitle = savedInstanceState.getString(KEY_RESULT_TITLE, null);
      mResultSnippet = savedInstanceState.getString(KEY_RESULT_SNIPPET, null);
      mResultX = savedInstanceState.getDouble(KEY_RESULT_X, Double.NaN);
      mResultY = savedInstanceState.getDouble(KEY_RESULT_Y, Double.NaN);

      // Too early to set map state here, as the map is not initialized;
      // at this point restoreState would be ignored.
    }

    // Set the MapView to wrap around.
    mMapView.enableWrapAround(true);

    // Set the Esri logo to be visible on the map.
    mMapView.setEsriLogoVisible(true);

    // Create a MapView Helper for use in MapView listeners set below.
    mMapViewHelper = new MapViewHelper(mMapView);
    
    mMapView.setOnStatusChangedListener(new OnStatusChangedListener() {
      private static final long serialVersionUID = 1L;

      @Override
      public void onStatusChanged(Object source, STATUS status) {

        if ((status == STATUS.INITIALIZED) && (source instanceof MapView )) {

          // When map is initialized, restore the map state (center and resolution)
          // if one was saved.
          if ((mMapState != null) && (!mMapState.isEmpty())) {
            mMapView.restoreState(mMapState);
          }
          else {
            // If there was no saved state then set default resolution and center
           mMapView.zoomToResolution(new Point(-1.3046152655046357E7, 
               4036221.883444177), 1.53);
          }

          if ((mResultTitle != null) && (!mResultTitle.isEmpty())) {
            // Additionally, if a tapped location was saved, then add this to 
            // the map as a graphic.
            mMapViewHelper.getMapView().getCallout().getStyle().setBackgroundColor(Color.BLACK);
            mMapViewHelper.addMarkerGraphic(mResultY, mResultX, mResultTitle,
                mResultSnippet, null, null, false, 0);
            
          } else {
            // If there is no location saved, then add a graphic to represent 
            // ESRI Headquarters, using the MapViewHelper.addMarkerGraphic
            // simplification API by specifying a latitude and longitude, a
            // drawable icon, and the title and content of a callout that is
            // shown when the icon is tapped.
            mMapViewHelper.getMapView().getCallout().getStyle().setBackgroundColor(Color.BLACK);
            mMapViewHelper.addMarkerGraphic(34.056695, -117.195693, "ESRI",
                "World Headquarters", null, mIcon, false, 0);
          }
        }
        
      }
    });
    
    // Set a listener for a single tap on the map.
    mMapView.setOnSingleTapListener(new OnSingleTapListener() {
      private static final long serialVersionUID = 1L;

      @Override
      public void onSingleTap(float x, float y) {
        // Remove all previous graphics added by the MapViewHelper.
        mMapViewHelper.removeAllGraphics();
        mResultTitle = mResultSnippet = null;
        mResultX = mResultY = Double.NaN;

        // Show address at the tapped point, by passing in screen coordinates,
        // the Locator created above, the MapViewHelper, and the Fields to be 
        // shown in the result. This uses a default symbol.
        GeocodeHelper.showAddress(x, y, mLocator, mMapViewHelper, null,
            mResultCalloutFields,
            new CallbackListener<LocatorReverseGeocodeResult>() {

              @Override
              public void onError(Throwable e) {
                // Indicate that there was a problem with this reverse geocode.
                Toast.makeText(getActivity(), "Error", Toast.LENGTH_LONG)
                    .show();
              }

              @Override
              public void onCallback(LocatorReverseGeocodeResult objs) {
                
                // In this callback, the reverse geocode has found a location;
                // GeocodeHelper adds the result location to the Map, but the 
                // result is preserved here for use if the fragment is destroyed.
                mResultX = objs.getLocation().getX();
                mResultY = objs.getLocation().getY();

                // To save the callout contents, the specified address fields 
                // are concatenated; GeocodeHelper uses these attributes as 
                // the Title in the callout.
                StringBuilder address = new StringBuilder();
                for (String field : mResultCalloutFields) {
                  Map<String, String> resultFields = objs.getAddressFields();
                  if (resultFields.containsKey(field)) {
                    address.append(resultFields.get(field)).append(" ");
                  }
                }
                mResultTitle = address.toString().trim();
              }
            });
      }
    });

    // Return the inflated view.
    return v;

  }

  
  @Override
  public void onPause() {
    super.onPause();

    if (mMapView != null) {
      // Save map state
      mMapState = mMapView.retainState();
    
    // Call MapView.pause to suspend map rendering while the activity is 
    // paused, which can save battery usage.
      mMapView.pause();
    }
  }

  
  @Override
  public void onResume() {
    super.onResume();
    
    // Call MapView.unpause to resume map rendering when the activity returns
    // to the foreground.
    if (mMapView != null) {      
      mMapView.unpause();
    }
  }

  
  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    
    // Save the current state of the map before the activity is destroyed. 
    outState.putString(KEY_MAPSTATE, mMapState);
    outState.putString(KEY_RESULT_TITLE, mResultTitle);
    outState.putString(KEY_RESULT_SNIPPET, mResultSnippet);
    outState.putDouble(KEY_RESULT_X, mResultX);
    outState.putDouble(KEY_RESULT_Y, mResultY);
  }

}
