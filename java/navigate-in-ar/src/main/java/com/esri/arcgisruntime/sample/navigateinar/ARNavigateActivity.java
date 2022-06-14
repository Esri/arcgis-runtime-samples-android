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
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.location.AndroidLocationDataSource;
import com.esri.arcgisruntime.mapping.ArcGISScene;
import com.esri.arcgisruntime.mapping.ArcGISTiledElevationSource;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
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
import com.esri.arcgisruntime.toolkit.ar.ArLocationDataSource;
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

  private float mCurrentVerticalOffset;

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

    requestPermissions();
  }

  private void navigateInAr() {
    // get a reference to the ar view
    mArView = findViewById(R.id.arView);
    mArView.registerLifecycle(getLifecycle());
    // disable touch interactions with the scene view
    mArView.getSceneView().setOnTouchListener((view, motionEvent) -> true);
    // create a scene and add it to the scene view
    mScene = new ArcGISScene(BasemapStyle.ARCGIS_IMAGERY);
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
    // disable plane visualization. It is not useful for this AR scenario.
    mArView.getArSceneView().getPlaneRenderer().setEnabled(false);
    mArView.getArSceneView().getPlaneRenderer().setVisible(false);
    // add an ar location data source to update location
    mArView.setLocationDataSource(new ArLocationDataSource(this));

    // create and add a graphics overlay for showing the route line
    GraphicsOverlay routeOverlay = new GraphicsOverlay();
    mArView.getSceneView().getGraphicsOverlays().add(routeOverlay);
    Graphic routeGraphic = new Graphic(sRouteResult.getRoutes().get(0).getRouteGeometry());
    routeOverlay.getGraphics().add(routeGraphic);
    // display the graphic 3 meters above the ground
    routeOverlay.getSceneProperties().setSurfacePlacement(LayerSceneProperties.SurfacePlacement.RELATIVE);
    routeOverlay.getSceneProperties().setAltitudeOffset(3);
    // create a renderer for the route geometry
    SolidStrokeSymbolLayer strokeSymbolLayer = new SolidStrokeSymbolLayer(1, Color.YELLOW, new LinkedList<>(),
        StrokeSymbolLayer.LineStyle3D.TUBE);
    strokeSymbolLayer.setCapStyle(StrokeSymbolLayer.CapStyle.ROUND);
    ArrayList<SymbolLayer> layers = new ArrayList<>();
    layers.add(strokeSymbolLayer);
    MultilayerPolylineSymbol polylineSymbol = new MultilayerPolylineSymbol(layers);
    SimpleRenderer polylineRenderer = new SimpleRenderer(polylineSymbol);
    routeOverlay.setRenderer(polylineRenderer);

    // create and start a location data source for use with the route tracker
    AndroidLocationDataSource trackingLocationDataSource = new AndroidLocationDataSource(this);
    trackingLocationDataSource.addLocationChangedListener(locationChangedEvent -> {
      if (mRouteTracker != null) {
        // pass new location to the route tracker
        mRouteTracker.trackLocationAsync(locationChangedEvent.getLocation());
      }
    });
    trackingLocationDataSource.startAsync();

    // get references to the ui views defined in the layout
    mHelpLabel = findViewById(R.id.helpLabelTextView);
    mArView = findViewById(R.id.arView);
    mCalibrationView = findViewById(R.id.calibrationView);

    // show/hide calibration view
    Button calibrationButton = findViewById(R.id.calibrateButton);
    calibrationButton.setOnClickListener(v -> {
      // toggle calibration
      mIsCalibrating = !mIsCalibrating;
      if (mIsCalibrating) {
        mScene.getBaseSurface().setOpacity(0.5f);
        mCalibrationView.setVisibility(View.VISIBLE);
      } else {
        mScene.getBaseSurface().setOpacity(0f);
        mCalibrationView.setVisibility(View.GONE);
      }
    });

    // start navigation
    Button navigateButton = findViewById(R.id.navigateStartButton);
    // start turn-by-turn when the user is ready
    navigateButton.setOnClickListener(v -> {
      // create a route tracker with the route result
      mRouteTracker = new RouteTracker(this, sRouteResult, 0, true);
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
        mTextToSpeech.speak(newGuidanceText, TextToSpeech.QUEUE_FLUSH, null, newVoiceGuidanceEvent.getSource().toString());
      });
      mRouteTracker
          .addTrackingStatusChangedListener((RouteTracker.TrackingStatusChangedEvent trackingStatusChangedEvent) -> {
            // Display updated guidance
            mHelpLabel.setText(mRouteTracker.generateVoiceGuidance().getText());
          });
    });

    // wire up joystick seek bars to allow manual calibration of height and heading
    JoystickSeekBar headingJoystick = findViewById(R.id.headingJoystick);
    // listen for calibration value changes for heading
    headingJoystick.addDeltaProgressUpdatedListener(delta -> {
      // get the origin camera
      Camera camera = mArView.getOriginCamera();
      // add the heading delta to the existing camera heading
      double heading = camera.getHeading() + delta;
      // get a camera with a new heading
      Camera newCam = camera.rotateTo(heading, camera.getPitch(), camera.getRoll());
      // apply the new origin camera
      mArView.setOriginCamera(newCam);
    });
    JoystickSeekBar altitudeJoystick = findViewById(R.id.altitudeJoystick);
    // listen for calibration value changes for altitude
    altitudeJoystick.addDeltaProgressUpdatedListener(delta -> {
      mCurrentVerticalOffset += delta;
      // get the origin camera
      Camera camera = mArView.getOriginCamera();
      // elevate camera by the delta
      Camera newCam = camera.elevate(delta);
      // apply the new origin camera
      mArView.setOriginCamera(newCam);
    });
    // this step is handled on the back end anyways, but we're applying a vertical offset to every update as per the
    // calibration step above
    mArView.getLocationDataSource().addLocationChangedListener(locationChangedEvent -> {
      Point updatedLocation = locationChangedEvent.getLocation().getPosition();
      mArView.setOriginCamera(new Camera(
          new Point(updatedLocation.getX(), updatedLocation.getY(), updatedLocation.getZ() + mCurrentVerticalOffset),
          mArView.getOriginCamera().getHeading(), mArView.getOriginCamera().getPitch(),
          mArView.getOriginCamera().getRoll()));
    });

    // remind the user to calibrate the heading and altitude before starting navigation
    Toast.makeText(this, "Calibrate your heading and altitude before navigating!", Toast.LENGTH_LONG).show();
  }

  /**
   * Request read external storage for API level 23+.
   */
  private void requestPermissions() {
    // define permission to request
    String[] reqPermission = { Manifest.permission.CAMERA };
    int requestCode = 2;
    if (ContextCompat.checkSelfPermission(this, reqPermission[0]) == PackageManager.PERMISSION_GRANTED) {
      navigateInAr();
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
      navigateInAr();
    } else {
      // report to user that permission was denied
      Toast.makeText(this, getString(R.string.navigate_ar_permission_denied), Toast.LENGTH_SHORT).show();
    }
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
  }

  @Override
  protected void onPause() {
    if (mArView != null) {
      mArView.stopTracking();
    }
    super.onPause();
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (mArView != null) {
      mArView.startTracking(ArcGISArView.ARLocationTrackingMode.CONTINUOUS);
    }
  }
}
