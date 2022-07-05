/*
 * Copyright 2017 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.esri.arcgisruntime.sample.formatcoordinates;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.esri.arcgisruntime.ArcGISRuntimeException;
import com.esri.arcgisruntime.geometry.CoordinateFormatter;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.ArcGISTiledLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {

  private MapView mMapView;

  // Graphic indicating coordinate location in the map
  private Graphic coordinateLocation;

  // TextViews containing coordinate notation strings
  private TextView mLatLongDDValue;
  private TextView mLatLongDMSValue;
  private TextView mUtmValue;
  private TextView mUSNGValue;

  /**
   * Coordinate notations supported by this sample
   */
  private enum NotationType {
    DMS,
    DD,
    UTM,
    USNG
  }

  /**
   * Uses CoordinateFormatter to update the UI with coordinate notation strings based on the given Point.
   * @param newLocation Point to convert to coordinate notations
   */
  private void toCoordinateNotationFromPoint(Point newLocation) {
    if (newLocation != null && ! newLocation.isEmpty()) {
      coordinateLocation.setGeometry(newLocation);

      try {
        // use CoordinateFormatter to convert to Latitude Longitude, formatted as Decimal Degrees
        mLatLongDDValue.setText(CoordinateFormatter.toLatitudeLongitude(newLocation,
            CoordinateFormatter.LatitudeLongitudeFormat.DECIMAL_DEGREES, 4));

        // use CoordinateFormatter to convert to Latitude Longitude, formatted as Degrees, Minutes, Seconds
        mLatLongDMSValue.setText(CoordinateFormatter.toLatitudeLongitude(newLocation,
            CoordinateFormatter.LatitudeLongitudeFormat.DEGREES_MINUTES_SECONDS, 1));

        // use CoordinateFormatter to convert to Universal Transverse Mercator, using latitudinal bands indicator
        mUtmValue.setText(CoordinateFormatter.toUtm(newLocation,
            CoordinateFormatter.UtmConversionMode.LATITUDE_BAND_INDICATORS, true));

        // use CoordinateFormatter to convert to United States National Grid (USNG)
        mUSNGValue.setText(CoordinateFormatter.toUsng(newLocation, 4, true));
      }
      catch (ArcGISRuntimeException convertException) {
        String message = String.format("%s Point at '%s'\n%s", getString(R.string.failed_convert),
            newLocation, convertException.getMessage());
        Snackbar.make(mMapView, message, BaseTransientBottomBar.LENGTH_SHORT).show();
      }
    }
  }

  /**
   * Uses CoordinateFormatter to update the graphic in the map from the given coordinate notation string entered by the
   * user. Also calls corresponding method to update all the remaining coordinate notation strings.
   * @param type the given coordinate notation type
   * @param coordinateNotation a string containing the coordinate notation to convert to a point
   */
  private void fromCoordinateNotationToPoint(MainActivity.NotationType type, String coordinateNotation) {
    // ignore empty input coordinate notation strings, do not update UI
    if (TextUtils.isEmpty(coordinateNotation)) return;

    Point convertedPoint = null;
    try {
      switch (type) {
        case DMS:
        case DD:
          // use CoordinateFormatter to parse Latitude Longitude - different numeric notations (Decimal Degrees;
          // Degrees, Minutes, Seconds; Degrees, Decimal Minutes) can all be passed to this same method
          convertedPoint = CoordinateFormatter.fromLatitudeLongitude(coordinateNotation, null);
          break;
        case UTM:
          // use CoordinateFormatter to parse UTM coordinates
          convertedPoint = CoordinateFormatter.fromUtm(coordinateNotation, null,
              CoordinateFormatter.UtmConversionMode.LATITUDE_BAND_INDICATORS);
          break;
        case USNG:
          // use CoordinateFormatter to parse US National Grid coordinates
          convertedPoint = CoordinateFormatter.fromUsng(coordinateNotation, null);
          break;
        default:
          Snackbar.make(mMapView, getString(R.string.unsupported_message), BaseTransientBottomBar.LENGTH_SHORT).show();
          break;
      }

      // update the location shown in the map
      toCoordinateNotationFromPoint(convertedPoint);
    }
    catch (ArcGISRuntimeException convertException) {
      String message = String.format("%s '%s'\n%s", getString(R.string.failed_convert), coordinateNotation,
          convertException.getMessage());
      Snackbar.make(mMapView, message, BaseTransientBottomBar.LENGTH_SHORT).show();
    }
  }

  /**
   * Shows a dialog allowing a user to enter a coordinate string in the given notation, then parses that string into a
   * Point displayed on the map, and also updates
   * @param type indicates the type of coordinate notation to be entered into the editable text box in the dialog
   * @param currentValue existing value of the coordinate, shown as default in the text box
   */
  private void showEnterCoordinateDialog(final MainActivity.NotationType type, final String currentValue) {
    String title = "";
    switch (type) {
      case DMS:
      case DD:
        title = getString(R.string.enter_latlong_message);
        break;
      case UTM:
        title = getString(R.string.enter_utm_message);
        break;
      case USNG:
        title = getString(R.string.enter_usng_message);
        break;
      default:
        Snackbar.make(mMapView, getString(R.string.unsupported_message), BaseTransientBottomBar.LENGTH_SHORT).show();
        break;
    }

    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

    LayoutInflater inflater = getLayoutInflater();
    final View dialogView = inflater.inflate(R.layout.dialog_coordinate, null);
    dialogBuilder.setView(dialogView);
    final EditText coordinateEditText = dialogView.findViewById(R.id.coordinateNotation);
    coordinateEditText.setText(currentValue);

    dialogBuilder.setTitle(title)
        .setCancelable(true)
        .setPositiveButton(R.string.set_location,
            (dialog, id) -> fromCoordinateNotationToPoint(type, coordinateEditText.getText().toString()));

    // create and show the dialog.
    dialogBuilder.create().show();
  }


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // retrieve coordinate label views from the layout; set each to allow a user to enter a new coordinate string when
    // the View is tapped
    mLatLongDDValue = findViewById(R.id.latLongDDNotation);
    mLatLongDDValue.setOnClickListener(view -> {
      TextView currentValue = (TextView)view;
      showEnterCoordinateDialog(NotationType.DD, currentValue.getText().toString());
    });
    mLatLongDMSValue = findViewById(R.id.latLongDMSNotation);
    mLatLongDMSValue.setOnClickListener(view -> {
      TextView currentValue = (TextView)view;
      showEnterCoordinateDialog(NotationType.DMS, currentValue.getText().toString());
    });
    mUtmValue = findViewById(R.id.utmNotation);
    mUtmValue.setOnClickListener(view -> {
      TextView currentValue = (TextView)view;
      showEnterCoordinateDialog(NotationType.UTM, currentValue.getText().toString());
    });
    mUSNGValue = findViewById(R.id.usngNotation);
    mUSNGValue.setOnClickListener(view -> {
      TextView currentValue = (TextView)view;
      showEnterCoordinateDialog(NotationType.USNG, currentValue.getText().toString());
    });

    // retrieve the MapView from layout
    mMapView = findViewById(R.id.mapView);

    // create a map that has the WGS 84 coordinate system and set this into the map
    ArcGISTiledLayer basemapLayer = new ArcGISTiledLayer(getString(R.string.basemap_url));
    Basemap wgs84Basemap = new Basemap(basemapLayer);
    ArcGISMap map = new ArcGISMap(wgs84Basemap);
    mMapView.setMap(map);

    // set up a Graphic to indicate where the coordinates relate to, with an initial location
    Point initialPoint = new Point(0,0, SpatialReferences.getWgs84());
    coordinateLocation = new Graphic(initialPoint,
        new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CROSS, Color.YELLOW, 20f));
    mMapView.getGraphicsOverlays().add(new GraphicsOverlay());
    mMapView.getGraphicsOverlays().get(0).getGraphics().add(coordinateLocation);
    toCoordinateNotationFromPoint(initialPoint);

    // set up a map touch listener that shows coordinates when a user taps on the map view
    mMapView.setOnTouchListener(new ShowCoordinatesMapTouchListener(this, mMapView));
  }

  /**
   * A map touch listener that updates formatted coordinates when a user taps on a location in the associated MapView.
   */
  private class ShowCoordinatesMapTouchListener extends DefaultMapViewOnTouchListener {

    public ShowCoordinatesMapTouchListener(Context context, MapView mapView) {
      super(context, mapView);
    }

    /**
     * Overrides the onSingleTapConfirmed gesture on the MapView, showing formatted coordinates of the tapped location.
     * @param e the motion event
     * @return true if the listener has consumed the event; false otherwise
     */
    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
      // convert the screen location where user tapped into a map point
      Point tapPoint = mMapView.screenToLocation(new android.graphics.Point((int) e.getX(), (int) e.getY()));
      toCoordinateNotationFromPoint(tapPoint);
      return true;
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
