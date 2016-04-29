package com.esri.arcgisruntime.sample.show_initial_map_location;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ToggleButton;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Map;
import com.esri.arcgisruntime.mapping.view.LocationDisplay;
import com.esri.arcgisruntime.mapping.view.MapView;


public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback , View.OnClickListener {

  private MapView mMapView;

  private ToggleButton mDefaultBtn, mNavBtn, mCompassBtn;

  private LocationDisplay mLocationDisplay;

  private static final int PERMISSION_REQUEST_LOCATION = 0;

  private View mLayout;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mLayout = findViewById(R.id.mainLayout);


    // Set up the buttons for changing
    // AutoPanMode
    setUpButtons();

    // All devices running N and above require explicit permissions
    // checking when the app is first run.

    requestLocationPermission();
  }


  private void setUpMap(){
    // inflate MapView from layout
    mMapView = (MapView) findViewById(R.id.mapView);

    // create a map with the BasemapType topographic
    Map mMap = new Map(Basemap.Type.TOPOGRAPHIC, 34.056295, -117.195800, 16);

    // set the map to be displayed in this view
    mMapView.setMap(mMap);

    //
    mLocationDisplay = mMapView.getLocationDisplay();

    mLocationDisplay.startAsync();

    mLocationDisplay.setAutoPanMode(LocationDisplay.AutoPanMode.DEFAULT);

  }
  private void setUpButtons(){
    mDefaultBtn = (ToggleButton) findViewById(R.id.btnDefault);
    mDefaultBtn.setOnClickListener(this);
    mDefaultBtn.setChecked(true);

    mCompassBtn = (ToggleButton) findViewById(R.id.btnCompass);
    mCompassBtn.setOnClickListener(this);

    mNavBtn = (ToggleButton) findViewById(R.id.btnNav);
    mNavBtn.setOnClickListener(this);

  }

  /**
   * Requests the {@link android.Manifest.permission#ACCESS_COARSE_LOCATION}
   * permission. If an additional rationale should be displayed, the user has
   * to launch the request from a SnackBar that includes additional
   * information.
   */

  private void requestLocationPermission() {
    // Permission has not been granted and must be requested.
    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
      // We need a reference to the containing widget when
      // managing the Snackbar (used for notifying users about app
      // permissions)


      // Provide an additional rationale to the user if the permission was
      // not granted
      // and the user would benefit from additional context for the use of
      // the permission.
      // Display a SnackBar with a button to request the missing
      // permission.
      Snackbar.make(mLayout, "Location access is required to display the map.", Snackbar.LENGTH_INDEFINITE)
          .setAction("OK", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              // Request the permission
              ActivityCompat.requestPermissions(MainActivity.this,
                  new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                  PERMISSION_REQUEST_LOCATION);
            }
          }).show();

    } else {
      // Request the permission. The result will be received in
      // onRequestPermissionResult().
      ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
          PERMISSION_REQUEST_LOCATION);
    }
  }
  /**
   * Once the app has prompted for permission to access location, the response
   * from the user is handled here. If permission exists to access location
   * check if GPS is available and device is not in airplane mode.
   *
   * @param requestCode
   *            int: The request code passed into requestPermissions
   * @param permissions
   *            String: The requested permission(s).
   * @param grantResults
   *            int: The grant results for the permission(s). This will be
   *            either PERMISSION_GRANTED or PERMISSION_DENIED
   */
  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

    if (requestCode == PERMISSION_REQUEST_LOCATION) {
      // Request for camera permission.
      if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

       setUpMap();

      } else {
        // Permission request was denied.
        Snackbar.make(mLayout, "Location permission request was denied.", Snackbar.LENGTH_SHORT).show();
      }
    }
  }
  @Override
  protected void onPause(){
    super.onPause();
    if (mMapView != null){
      mMapView.pause();
    }

  }

  @Override
  protected void onResume(){
    super.onResume();
    if (mMapView != null){
      mMapView.resume();
    }

  }


  @Override public void onClick(View v) {
    switch (v.getId()){
      case R.id.btnCompass:
        mLocationDisplay.setAutoPanMode(LocationDisplay.AutoPanMode.COMPASS);
        mCompassBtn.setChecked(true);
        mDefaultBtn.setChecked(false);
        mNavBtn.setChecked(false);
        break;

      case R.id.btnDefault:
        mLocationDisplay.setAutoPanMode(LocationDisplay.AutoPanMode.DEFAULT);
        mDefaultBtn.setChecked(true);
        mCompassBtn.setChecked(false);
        mNavBtn.setChecked(false);
        break;

      case R.id.btnNav:
        mLocationDisplay.setAutoPanMode(LocationDisplay.AutoPanMode.NAVIGATION);
        mNavBtn.setChecked(true);
        mDefaultBtn.setChecked(false);
        mCompassBtn.setChecked(false);
        break;

    }
  }
}