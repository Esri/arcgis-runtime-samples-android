/*
 *  Copyright 2019 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.esri.arcgisruntime.sample.navigateinar;

import java.util.ArrayList;
import java.util.LinkedList;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.arcgisruntime.location.AndroidLocationDataSource;
import com.esri.arcgisruntime.mapping.ArcGISScene;
import com.esri.arcgisruntime.mapping.ArcGISTiledElevationSource;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.NavigationConstraint;
import com.esri.arcgisruntime.mapping.Surface;
import com.esri.arcgisruntime.mapping.view.Camera;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.LayerSceneProperties;
import com.esri.arcgisruntime.navigation.RouteTracker;
import com.esri.arcgisruntime.symbology.MultilayerPolylineSymbol;
import com.esri.arcgisruntime.symbology.SimpleRenderer;
import com.esri.arcgisruntime.symbology.SolidStrokeSymbolLayer;
import com.esri.arcgisruntime.symbology.StrokeSymbolLayer;
import com.esri.arcgisruntime.symbology.SymbolLayer;
import com.esri.arcgisruntime.tasks.networkanalysis.RouteResult;
import com.esri.arcgisruntime.toolkit.ar.ArcGISArView;
import com.esri.arcgisruntime.toolkit.control.JoystickSeekBar;

public class ARNavigateActivity extends AppCompatActivity {

  private static final String TAG = ARNavigateActivity.class.getSimpleName();

  private ArcGISArView mArView;

  private TextView mHelpLabel;
  private View mCalibrationView;

  public static RouteResult sRouteResult;

  private ArcGISScene mScene;

  private boolean mIsCalibrating = false;
  private RouteTracker mRouteTracker;
  private TextToSpeech mTextToSpeech;
  private GraphicsOverlay mRouteOverlay;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_ar);

    // ensure at route has been set by the previous activity
    if (sRouteResult.getRoutes().get(0) == null) {
      String error = "Route not set before launching activity!";
      Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
      Log.e(TAG, error);
    }

    // get a reference to the ar view
    mArView = findViewById(R.id.arView);
    mArView.registerLifecycle(getLifecycle());
    // create a scene and add it to the scene view
    mScene = new ArcGISScene(Basemap.createImagery());
    mArView.getSceneView().setScene(mScene);
    // create and add an elevation surface to the scene
    ArcGISTiledElevationSource elevationSource = new ArcGISTiledElevationSource(getString(R.string.elevation_url));
    Surface elevationSurface = new Surface();
    elevationSurface.getElevationSources().add(elevationSource);
    mArView.getSceneView().getScene().setBaseSurface(elevationSurface);
    // allow the user to navigate underneath the surface
    elevationSurface.setNavigationConstraint(NavigationConstraint.NONE);
    // hide the basemap. The image feed provides map context while navigating in AR
    elevationSurface.setOpacity(0f);

    // create and add a graphics overlay for showing the route line
    mRouteOverlay = new GraphicsOverlay();
    mArView.getSceneView().getGraphicsOverlays().add(mRouteOverlay);
    Graphic routeGraphic = new Graphic(sRouteResult.getRoutes().get(0).getRouteGeometry());
    mRouteOverlay.getGraphics().add(routeGraphic);
    // display the graphic 3 meters above the ground
    mRouteOverlay.getSceneProperties().setSurfacePlacement(LayerSceneProperties.SurfacePlacement.RELATIVE);
    mRouteOverlay.getSceneProperties().setAltitudeOffset(3);
    // create a renderer for the route geometry
    SolidStrokeSymbolLayer strokeSymbolLayer = new SolidStrokeSymbolLayer(1, Color.YELLOW, new LinkedList<>(), StrokeSymbolLayer.LineStyle3D.TUBE);
    strokeSymbolLayer.setCapStyle(StrokeSymbolLayer.CapStyle.ROUND);
    ArrayList<SymbolLayer> layers = new ArrayList<>();
    layers.add(strokeSymbolLayer);
    MultilayerPolylineSymbol polylineSymbol = new MultilayerPolylineSymbol(layers);
    SimpleRenderer polylineRenderer = new SimpleRenderer(polylineSymbol);
    mRouteOverlay.setRenderer(polylineRenderer);

    // create and start a location data source for use with the route tracker
    AndroidLocationDataSource trackingLocationDataSource = new AndroidLocationDataSource(this);
    trackingLocationDataSource.addLocationChangedListener(locationChangedEvent -> {
      if (mRouteTracker != null) {
        // pass new location to the route tracker
        mRouteTracker.trackLocationAsync(locationChangedEvent.getLocation());
      }
    });
    trackingLocationDataSource.startAsync();

    // get references to the views defined in the layout
    mHelpLabel = findViewById(R.id.helpLabelTextView);
    mArView = findViewById(R.id.arView);
    mCalibrationView = findViewById(R.id.calibrationView);

    // show/hide calibration view
    Button calibrationButton = findViewById(R.id.calibrateButton);
    calibrationButton.setOnClickListener(v -> toggleCalibration());
    Button navigateButton = findViewById(R.id.navigateStartButton);
    // start turn-by-turn when the user is ready
    navigateButton.setOnClickListener(v -> {
      // create a route tracker with the route result
      mRouteTracker = new RouteTracker(this, sRouteResult, 0);

      // initialize text-to-speech to play navigation voice guidance
      mTextToSpeech = new TextToSpeech(this, status -> {
        if (status != TextToSpeech.ERROR) {
          mTextToSpeech.setLanguage(Resources.getSystem().getConfiguration().locale);
        }
      });

      mRouteTracker.addNewVoiceGuidanceListener((RouteTracker.NewVoiceGuidanceEvent newVoiceGuidanceEvent) -> {
        // Get new guidance
        String newGuidanceText = newVoiceGuidanceEvent.getVoiceGuidance().getText();

        // Display and then read out the new guidance
        mHelpLabel.setText(newGuidanceText);
        // read out directions
        mTextToSpeech.stop();
        mTextToSpeech.speak(newGuidanceText, TextToSpeech.QUEUE_FLUSH, null);
      });

      mRouteTracker.addTrackingStatusChangedListener((RouteTracker.TrackingStatusChangedEvent trackingStatusChangedEvent) -> {
        // Display updated guidance
        mHelpLabel.setText(mRouteTracker.generateVoiceGuidance().getText());
      });
    });

    // if you want heights above ellipsoid (not mean sea level/orthometric), use this instead
    mArView.setLocationDataSource(trackingLocationDataSource);

    // disable plane visualization. It is not useful for this AR scenario.
    mArView.getArSceneView().getPlaneRenderer().setEnabled(false);
    mArView.getArSceneView().getPlaneRenderer().setVisible(false);


    JoystickSeekBar altitudeJoystick = findViewById(R.id.altitudeJoystick);
    // listen for calibration value changes for altitude
    altitudeJoystick.addDeltaProgressUpdatedListener(delta -> {
      // add the altitude change to the existing altitude
      double altitude = mRouteOverlay.getSceneProperties().getAltitudeOffset() + delta;
      // set the route overlay's altitude offset to the new altitude
      mRouteOverlay.getSceneProperties().setAltitudeOffset(altitude);
    });

    JoystickSeekBar headingJoystick = findViewById(R.id.headingJoystick);
    // listen for calibration value changes for heading
    headingJoystick.addDeltaProgressUpdatedListener(delta -> {
      // get the origin camera
      Camera camera = mArView.getOriginCamera();
      // add the heading change to the existing heading
      double heading = camera.getHeading() + delta;
      // get a camera with a new heading
      Camera newCam = camera.rotateTo(heading, camera.getPitch(), camera.getRoll());
      // apply the new origin camera
      mArView.setOriginCamera(newCam);
    });

    // remind the user to calibrate the heading and altitude before starting navigation
    Toast.makeText(this, "Calibrate your heading and altitude before navigating!", Toast.LENGTH_LONG).show();

    requestPermissions();
  }

  private void toggleCalibration() {
    // toggle calibration
    mIsCalibrating = !mIsCalibrating;
    if (mIsCalibrating) {
      mScene.getBaseSurface().setOpacity(0.5f);
      mCalibrationView.setVisibility(View.VISIBLE);
    } else {
      mScene.getBaseSurface().setOpacity(0f);
      mCalibrationView.setVisibility(View.GONE);
    }
  }

  /**
   * Request read external storage for API level 23+.
   */
  private void requestPermissions() {
    // define permission to request
    String[] reqPermission = { Manifest.permission.CAMERA };
    int requestCode = 2;
    if (ContextCompat.checkSelfPermission(this, reqPermission[0]) == PackageManager.PERMISSION_GRANTED) {
      //setupArView();
    } else {
      // request permission
      ActivityCompat.requestPermissions(this, reqPermission, requestCode);
    }
  }

  /**
   * Handle the permissions request response.
   */
  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      //setupArView();
    } else {
      // report to user that permission was denied
      Toast.makeText(this, getString(R.string.navigate_ar_permission_denied), Toast.LENGTH_SHORT).show();
    }
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
  }

  @Override
  protected void onPause() {
    mArView.stopTracking();
    super.onPause();
  }

  @Override
  protected void onResume() {
    super.onResume();
    mArView.startTracking(ArcGISArView.ARLocationTrackingMode.CONTINUOUS);
  }
}
