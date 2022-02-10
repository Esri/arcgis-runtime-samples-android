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

package com.esri.arcgisruntime.sample.viewshedlocation;

import java.util.concurrent.ExecutionException;

import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.geoanalysis.LocationViewshed;
import com.esri.arcgisruntime.geoanalysis.Viewshed;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.layers.ArcGISSceneLayer;
import com.esri.arcgisruntime.mapping.ArcGISScene;
import com.esri.arcgisruntime.mapping.ArcGISTiledElevationSource;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.Surface;
import com.esri.arcgisruntime.mapping.view.AnalysisOverlay;
import com.esri.arcgisruntime.mapping.view.Camera;
import com.esri.arcgisruntime.mapping.view.DefaultSceneViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.OrbitLocationCameraController;
import com.esri.arcgisruntime.mapping.view.SceneView;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  // initial values
  private static final int mInitHeading = 0;
  private static final int mInitPitch = 60;
  private static final int mInitHorizontalAngle = 75;
  private static final int mInitVerticalAngle = 90;
  private static final int mInitMinDistance = 0;
  private static final int mInitMaxDistance = 1500;

  private SceneView mSceneView;
  private LocationViewshed mViewshed;
  private int mMinDistance;
  private int mMaxDistance;

  private SeekBar mHeadingSeekBar;
  private SeekBar mPitchSeekBar;
  private SeekBar mHorizontalAngleSeekBar;
  private SeekBar mVerticalAngleSeekBar;
  private SeekBar mMinDistanceSeekBar;
  private SeekBar mMaxDistanceSeekBar;
  private TextView mCurrHeading;
  private TextView mCurrPitch;
  private TextView mCurrHorizontalAngle;
  private TextView mCurrVerticalAngle;
  private TextView mCurrMinDistance;
  private TextView mCurrMaxDistance;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // authentication with an API key or named user is required to access basemaps and other
    // location services
    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY);

    // set initial values
    mMinDistance = mInitMinDistance;
    mMaxDistance = mInitMaxDistance;

    // create a scene and add an imagery basemap to it
    mSceneView = findViewById(R.id.sceneView);
    ArcGISScene scene = new ArcGISScene(BasemapStyle.ARCGIS_IMAGERY);
    mSceneView.setScene(scene);

    // add base surface for elevation data
    Surface surface = new Surface();
    final String localElevationImageService = getString(
        com.esri.arcgisruntime.sample.viewshedlocation.R.string.elevation_service);
    surface.getElevationSources().add(new ArcGISTiledElevationSource(localElevationImageService));
    scene.setBaseSurface(surface);

    // add a scene layer
    final String buildings = getString(com.esri.arcgisruntime.sample.viewshedlocation.R.string.buildings_layer);
    ArcGISSceneLayer sceneLayer = new ArcGISSceneLayer(buildings);
    scene.getOperationalLayers().add(sceneLayer);

    // create viewshed from location
    Point location = new Point(-4.50, 48.4, 100.0);
    Viewshed.setFrustumOutlineColor(Color.BLUE);
    mViewshed = new LocationViewshed(location, mInitHeading, mInitPitch, mInitHorizontalAngle, mInitVerticalAngle,
        mInitMinDistance, mInitMaxDistance);
    mViewshed.setFrustumOutlineVisible(true);

    // add a camera and set it to orbit the location point
    Camera camera = new Camera(location, 20000000, 0, 55, 0);
    OrbitLocationCameraController orbitCamera = new OrbitLocationCameraController(location, 5000);
    mSceneView.setCameraController(orbitCamera);
    mSceneView.setViewpointCamera(camera);

    // create an analysis overlay to add the mViewshed to the scene view
    AnalysisOverlay analysisOverlay = new AnalysisOverlay();
    analysisOverlay.getAnalyses().add(mViewshed);
    mSceneView.getAnalysisOverlays().add(analysisOverlay);

    handleUiElements();
  }

  /**
   * Handles double touch drag for movement of viewshed location point, inflation of UI elements, and listeners for
   * changes in seek bar progress.
   */
  private void handleUiElements() {
    mSceneView.setOnTouchListener(new DefaultSceneViewOnTouchListener(mSceneView) {
      @Override public boolean onDoubleTouchDrag(MotionEvent motionEvent) {
        // convert from screen point to location point
        android.graphics.Point screenPoint = new android.graphics.Point(Math.round(motionEvent.getX()),
            Math.round(motionEvent.getY()));
        ListenableFuture<Point> locationPointFuture = mSceneView.screenToLocationAsync(screenPoint);
        locationPointFuture.addDoneListener(() -> {
          try {
            Point locationPoint = locationPointFuture.get();

            // add 50 meters to location point and set to viewshed
            mViewshed.setLocation(new Point(locationPoint.getX(), locationPoint.getY(), locationPoint.getZ() + 50));
          } catch (InterruptedException | ExecutionException e) {
            String error = "Error converting screen point to location point: " + e.getMessage();
            Log.e(TAG, error);
            Toast.makeText(MainActivity.this, error, Toast.LENGTH_LONG).show();
          }
        });

        // ignore default double touch drag gesture
        return true;
      }
    });

    // get views from layout
    mCurrHeading = findViewById(R.id.curr_heading);
    mCurrPitch = findViewById(R.id.curr_pitch);
    mCurrHorizontalAngle = findViewById(R.id.curr_horizontal_angle);
    mCurrVerticalAngle = findViewById(R.id.curr_vertical_angle);
    mCurrMinDistance = findViewById(R.id.curr_minimum_distance);
    mCurrMaxDistance = findViewById(R.id.curr_maximum_distance);

    // heading range 0 - 360
    mHeadingSeekBar = findViewById(R.id.heading_seek_bar);
    mHeadingSeekBar.setMax(360);
    setHeading(mInitHeading);
    mHeadingSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        setHeading(seekBar.getProgress());
      }

      @Override public void onStartTrackingTouch(SeekBar seekBar) {
      }

      @Override public void onStopTrackingTouch(SeekBar seekBar) {
      }
    });

    // set arbitrary max to 180 to avoid nonsensical pitch values
    mPitchSeekBar = findViewById(R.id.pitch_seek_bar);
    mPitchSeekBar.setMax(180);
    setPitch(mInitPitch);
    mPitchSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        setPitch(seekBar.getProgress());
      }

      @Override public void onStartTrackingTouch(SeekBar seekBar) {
      }

      @Override public void onStopTrackingTouch(SeekBar seekBar) {
      }
    });

    // horizontal angle range 1 - 120
    mHorizontalAngleSeekBar = findViewById(R.id.horizontal_angle_seekbar);
    mHorizontalAngleSeekBar.setMax(120);
    setHorizontalAngle(mInitHorizontalAngle);
    mHorizontalAngleSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        int horizontalAngle = mHorizontalAngleSeekBar.getProgress();
        if (horizontalAngle > 0) { // horizontal angle must be > 0
          setHorizontalAngle(horizontalAngle);
        }
      }

      @Override public void onStartTrackingTouch(SeekBar seekBar) {
      }

      @Override public void onStopTrackingTouch(SeekBar seekBar) {
      }
    });

    // vertical angle range 1 - 120
    mVerticalAngleSeekBar = findViewById(R.id.vertical_angle_seekbar);
    mVerticalAngleSeekBar.setMax(120);
    setVerticalAngle(mInitVerticalAngle);
    mVerticalAngleSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        int verticalAngle = mVerticalAngleSeekBar.getProgress();
        if (verticalAngle > 0) { // vertical angle must be > 0
          setVerticalAngle(verticalAngle);
        }
      }

      @Override public void onStartTrackingTouch(SeekBar seekBar) {
      }

      @Override public void onStopTrackingTouch(SeekBar seekBar) {
      }
    });

    // set to 1000 below the arbitrary max
    mMinDistanceSeekBar = findViewById(R.id.min_distance_seekbar);
    mMinDistanceSeekBar.setMax(8999);
    setMinDistance(mInitMinDistance);
    mMinDistanceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        mMinDistance = seekBar.getProgress();
        if (mMaxDistance - mMinDistance < 1000) {
          mMaxDistance = mMinDistance + 1000;
          setMaxDistance(mMaxDistance);
        }
        setMinDistance(mMinDistance);
      }

      @Override public void onStartTrackingTouch(SeekBar seekBar) {
      }

      @Override public void onStopTrackingTouch(SeekBar seekBar) {
      }
    });

    // set arbitrary max to 9999 to allow a maximum of 4 digits
    mMaxDistanceSeekBar = findViewById(R.id.max_distance_seekbar);
    mMaxDistanceSeekBar.setMax(9999);
    setMaxDistance(mInitMaxDistance);
    mMaxDistanceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        mMaxDistance = seekBar.getProgress();
        if (mMaxDistance - mMinDistance < 1000) {
          if (mMaxDistance > 1000) {
            mMinDistance = mMaxDistance - 1000;
          } else {
            mMinDistance = 0;
          }
          setMinDistance(mMinDistance);
        }
        setMaxDistance(mMaxDistance);
      }

      @Override public void onStartTrackingTouch(SeekBar seekBar) {
      }

      @Override public void onStopTrackingTouch(SeekBar seekBar) {
      }
    });
  }

  /**
   * Set viewshed heading, seek bar progress, and current heading text view.
   *
   * @param heading in degrees
   */
  private void setHeading(int heading) {
    mViewshed.setHeading(heading);
    mHeadingSeekBar.setProgress(heading);
    mCurrHeading.setText(Integer.toString(heading));
  }

  /**
   * Set viewshed pitch, seek bar progress, and current pitch text view.
   *
   * @param pitch in degrees
   */
  private void setPitch(int pitch) {
    mViewshed.setPitch(pitch);
    mPitchSeekBar.setProgress(pitch);
    mCurrPitch.setText(Integer.toString(pitch));
  }

  /**
   * Set viewshed horizontal angle, seek bar progress, and current horizontal angle text view.
   *
   * @param horizontalAngle in degrees, > 0 and <= 120
   */
  private void setHorizontalAngle(int horizontalAngle) {
    if (horizontalAngle > 0 && horizontalAngle <= 120) {
      mViewshed.setHorizontalAngle(horizontalAngle);
      mHorizontalAngleSeekBar.setProgress(horizontalAngle);
      mCurrHorizontalAngle.setText(Integer.toString(horizontalAngle));
    } else {
      Log.e(TAG, "Horizontal angle must be greater than 0 and less than or equal to 120.");
    }
  }

  /**
   * Set viewshed vertical angle, seek bar progress, and current vertical angle text view.
   *
   * @param verticalAngle in degrees, > 0 and <= 120
   */
  private void setVerticalAngle(int verticalAngle) {
    if (verticalAngle > 0 && verticalAngle <= 120) {
      mViewshed.setVerticalAngle(verticalAngle);
      mVerticalAngleSeekBar.setProgress(verticalAngle);
      mCurrVerticalAngle.setText(Integer.toString(verticalAngle));
    } else {
      Log.e(TAG, "Vertical angle must be greater than 0 and less than or equal to 120.");
    }
  }

  /**
   * Set viewshed minimum distance, seek bar progress, and current minimum distance text view.
   *
   * @param minDistance in meters
   */
  private void setMinDistance(int minDistance) {
    mViewshed.setMinDistance(minDistance);
    mMinDistanceSeekBar.setProgress(minDistance);
    mCurrMinDistance.setText(Integer.toString(minDistance));
  }

  /**
   * Set viewshed maximum distance, seek bar progress, and current maximum distance text view.
   *
   * @param maxDistance in meters
   */
  private void setMaxDistance(int maxDistance) {
    mViewshed.setMaxDistance(maxDistance);
    mMaxDistanceSeekBar.setProgress(maxDistance);
    mCurrMaxDistance.setText(Integer.toString(maxDistance));
  }

  @Override
  protected void onPause() {
    mSceneView.pause();
    super.onPause();
  }

  @Override
  protected void onResume() {
    super.onResume();
    mSceneView.resume();
  }

  @Override
  protected void onDestroy() {
    mSceneView.dispose();
    super.onDestroy();
  }
}
