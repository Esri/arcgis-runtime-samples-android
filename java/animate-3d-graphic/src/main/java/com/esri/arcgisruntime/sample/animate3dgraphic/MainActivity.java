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

package com.esri.arcgisruntime.sample.animate3dgraphic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.content.res.AssetManager;
import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.Polyline;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.ArcGISScene;
import com.esri.arcgisruntime.mapping.ArcGISTiledElevationSource;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.Surface;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.GlobeCameraController;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.LayerSceneProperties;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.mapping.view.OrbitGeoElementCameraController;
import com.esri.arcgisruntime.mapping.view.SceneView;
import com.esri.arcgisruntime.symbology.ModelSceneSymbol;
import com.esri.arcgisruntime.symbology.Renderer;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.esri.arcgisruntime.symbology.SimpleRenderer;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private List<Map<String, Object>> mMissionData;
  private Timer mTimer;
  private int mKeyFrame;

  private TextView mCurrAltitude;
  private TextView mCurrHeading;
  private TextView mCurrPitch;
  private TextView mCurrRoll;
  private SeekBar mMissionProgressSeekBar;
  private SeekBar mSpeedSeekBar;
  private Spinner mMissionSelector;
  private Button mPlayStopButton;
  private Button mFollowFreeCamButton;

  private MapView mMapView;
  private SceneView mSceneView;
  private OrbitGeoElementCameraController mOrbitCameraController;
  private Graphic mRouteGraphic;
  private Graphic mPlane2D;
  private Graphic mPlane3D;
  private GraphicsOverlay mSceneOverlay;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // authentication with an API key or named user is required to access basemaps and other
    // location services
    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY);

    // load tank model from assets into cache directory
    copyFileFromAssetsToCache(getString(R.string.bristol_model));
    copyFileFromAssetsToCache(getString(R.string.bristol_skin));

    // create a scene and add it to the scene view
    mSceneView = findViewById(R.id.sceneView);
    ArcGISScene scene = new ArcGISScene(BasemapStyle.ARCGIS_IMAGERY);
    mSceneView.setScene(scene);

    // add elevation data
    Surface surface = new Surface();
    surface.getElevationSources().add(new ArcGISTiledElevationSource(getString(R.string.world_elevation_service_url)));
    scene.setBaseSurface(surface);

    // create a graphics overlay for the scene
    mSceneOverlay = new GraphicsOverlay();
    mSceneOverlay.getSceneProperties().setSurfacePlacement(LayerSceneProperties.SurfacePlacement.ABSOLUTE);
    mSceneView.getGraphicsOverlays().add(mSceneOverlay);

    // create renderer to handle updating plane's orientation
    SimpleRenderer renderer3D = new SimpleRenderer();
    Renderer.SceneProperties renderProperties = renderer3D.getSceneProperties();
    renderProperties.setHeadingExpression("[HEADING]");
    renderProperties.setPitchExpression("[PITCH]");
    renderProperties.setRollExpression("[ROLL]");
    mSceneOverlay.setRenderer(renderer3D);

    // set up mini map
    mMapView = findViewById(R.id.mapView);
    ArcGISMap map = new ArcGISMap(BasemapStyle.ARCGIS_IMAGERY);
    mMapView.setMap(map);
    // make sure the map view renders on top of the scene view
    mMapView.setZOrderMediaOverlay(true);

    // create a graphics overlay for route
    GraphicsOverlay routeOverlay = new GraphicsOverlay();
    // create a placeholder graphic for showing the mission route in mini map
    SimpleLineSymbol routeSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.RED, 2);
    mRouteGraphic = new Graphic();
    mRouteGraphic.setSymbol(routeSymbol);
    routeOverlay.getGraphics().add(mRouteGraphic);
    mMapView.getGraphicsOverlays().add(routeOverlay);

    // create a graphics overlay for plane symbol
    GraphicsOverlay plane2dOverlay = new GraphicsOverlay();
    // create renderer to rotate the plane graphic in the mini map
    SimpleRenderer renderer2D = new SimpleRenderer();
    SimpleMarkerSymbol plane2DSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.TRIANGLE, Color.BLUE, 10);
    renderer2D.setSymbol(plane2DSymbol);
    renderer2D.setRotationExpression("[ANGLE]");
    plane2dOverlay.setRenderer(renderer2D);
    // create a graphic with a blue triangle symbol to represent the plane on the mini map
    Map<String, Object> attributes = new HashMap<>();
    attributes.put("ANGLE", 0f);
    mPlane2D = new Graphic(new Point(0, 0, SpatialReferences.getWgs84()), attributes);
    plane2dOverlay.getGraphics().add(mPlane2D);
    mMapView.getGraphicsOverlays().add(plane2dOverlay);

    // when the plane model is done loading, create an orbit camera controller to follow the plane
    loadModel().addDoneLoadingListener(() -> {
      mOrbitCameraController = new OrbitGeoElementCameraController(mPlane3D, 30.0);
      mOrbitCameraController.setCameraPitchOffset(75.0);
      mSceneView.setCameraController(mOrbitCameraController);
    });

    // get references to and wire up UI elements
    createUiElements();
  }

  /**
   * Setup the app's UI elements.
   */
  private void createUiElements() {
    // get UI elements
    mMissionSelector = findViewById(R.id.missionSelectorSpinner);
    mMissionSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
        changeMission(mMissionSelector.getSelectedItem().toString());
      }

      @Override public void onNothingSelected(AdapterView<?> adapterView) {

      }
    });
    mMissionSelector.setSelection(0);

    // set mission progress seek bar to update key frame on change
    mMissionProgressSeekBar = findViewById(R.id.missionProgressSeekBar);
    mMissionProgressSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        mKeyFrame = i;
      }

      @Override public void onStartTrackingTouch(SeekBar seekBar) {

      }

      @Override public void onStopTrackingTouch(SeekBar seekBar) {

      }
    });

    // set speed progress bar with max speed and set speed on change
    mSpeedSeekBar = findViewById(R.id.speedSeekBar);
    mSpeedSeekBar.setMax(30);
    mSpeedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        startAnimation(i);
      }

      @Override public void onStartTrackingTouch(SeekBar seekBar) {
      }

      @Override public void onStopTrackingTouch(SeekBar seekBar) {
      }
    });

    Button zoomInButton = findViewById(R.id.zoomInButton);
    zoomInButton.setOnClickListener(view -> mMapView.setViewpointScaleAsync(mMapView.getMapScale() / 5));
    Button zoomOutButton = findViewById(R.id.zoomOutButton);
    zoomOutButton.setOnClickListener(view -> mMapView.setViewpointScaleAsync(mMapView.getMapScale() * 5));

    // get references to HUD text views
    mCurrAltitude = findViewById(R.id.currAltitudeTextView);
    mCurrHeading = findViewById(R.id.currHeadingTextView);
    mCurrPitch = findViewById(R.id.currPitchTextView);
    mCurrRoll = findViewById(R.id.currRollTextView);

    // handle follow/free cam button actions
    mFollowFreeCamButton = findViewById(R.id.followFreeCamButton);
    mFollowFreeCamButton.setSelected(true);
    mFollowFreeCamButton.setOnClickListener(view -> {
      if (mFollowFreeCamButton.isSelected()) {
        mFollowFreeCamButton.setText(R.string.follow);
        mFollowFreeCamButton.setSelected(false);
      } else {
        mFollowFreeCamButton.setText(getString(R.string.free_cam));
        mFollowFreeCamButton.setSelected(true);
      }
      toggleFollow(mFollowFreeCamButton.isSelected());
    });

    // handle play/stop button stop and start animation
    mPlayStopButton = findViewById(R.id.playStopButton);
    mPlayStopButton.setSelected(true);
    mPlayStopButton.setOnClickListener(view -> {
      if (mPlayStopButton.isSelected()) {
        stopAnimation();
        mPlayStopButton.setSelected(false);
        mPlayStopButton.setText(R.string.play);
      } else {
        startAnimation(mSpeedSeekBar.getProgress());
        mPlayStopButton.setSelected(true);
        mPlayStopButton.setText(getString(R.string.stop));
      }
    });
  }

  /**
   * Load the plane model from the cache, use to construct a Model Scene Symbol and add it to the scene's graphic overlay.
   */
  private ModelSceneSymbol loadModel() {
    // create a graphic with a ModelSceneSymbol of a plane to add to the scene
    String pathToModel = getCacheDir() + File.separator + getString(R.string.bristol_model);
    ModelSceneSymbol plane3DSymbol = new ModelSceneSymbol(pathToModel, 1.0);
    plane3DSymbol.loadAsync();
    mPlane3D = new Graphic(new Point(0, 0, 0, SpatialReferences.getWgs84()), plane3DSymbol);
    mSceneOverlay.getGraphics().add(mPlane3D);
    return plane3DSymbol;
  }

  /**
   * Change the mission data and reset the animation.
   *
   * @param mission name of .csv file containing mission data
   */
  private void changeMission(String mission) {

    stopAnimation();

    // clear previous mission data
    mMissionData = new ArrayList<>();

    // get mission data
    mMissionData = getMissionData(mission);
    mMissionProgressSeekBar.setMax(mMissionData.size());

    // draw mission route on mini map
    PointCollection points = new PointCollection(SpatialReferences.getWgs84());
    for (Map<String, Object> ordinates : mMissionData) {
      points.add((Point) ordinates.get("POSITION"));
    }
    Polyline route = new Polyline(points);
    mRouteGraphic.setGeometry(route);

    // set the mini map scale
    mMapView.setViewpointScaleAsync(100000).addDoneListener(() -> {
      // start the animation at the current key frame progress point
      startAnimation(mSpeedSeekBar.getProgress());
    });
  }

  /**
   * Loads the mission data from a .csv file into memory.
   *
   * @param mission name of the .csv file containing the mission data
   * @return ordered list of mapped key value pairs representing coordinates and rotation parameters for each step of
   * the mission
   */
  private List<Map<String, Object>> getMissionData(String mission) {
    List<Map<String, Object>> missionList = new ArrayList<>();

    // open a file reader to the mission file that automatically closes after read
    try (BufferedReader missionFile = new BufferedReader(new InputStreamReader(getAssets().open(mission)))) {
      String line;
      while ((line = missionFile.readLine()) != null) {
        String[] l = line.split(",");
        Map<String, Object> ordinates = new HashMap<>();
        ordinates.put("POSITION",
            new Point(Float.valueOf(l[0]), Float.valueOf(l[1]), Float.valueOf(l[2]), SpatialReferences.getWgs84()));
        ordinates.put("HEADING", Float.valueOf(l[3]));
        ordinates.put("PITCH", Float.valueOf(l[4]));
        ordinates.put("ROLL", Float.valueOf(l[5]));
        missionList.add(ordinates);
      }
    } catch (IOException e) {
      String error = "Error reading mission file: " + e.getMessage();
      Toast.makeText(this, error, Toast.LENGTH_LONG).show();
      Log.e(TAG, error);
    }
    return missionList;
  }

  /**
   * Start the animation.
   *
   * @param speed at which key frames increment
   */
  private void startAnimation(int speed) {

    // stop the current animation timer
    stopAnimation();

    // calculate period from speed
    int period = mSpeedSeekBar.getMax() - speed + 10;

    // create a timer to animate the tank
    mTimer = new Timer();
    mTimer.scheduleAtFixedRate(new TimerTask() {
      @Override public void run() {
        if (mMissionData == null) {
          return;
        }
        // reset key frame at end of mission
        if (mKeyFrame >= mMissionData.size()) {
          mKeyFrame = 0;
        }
        // animate the given key frame
        animate(mKeyFrame);
        mKeyFrame++;
      }
    }, 0, period);
  }

  /**
   * Stop the animation by canceling the timer.
   */
  private void stopAnimation() {
    if (mTimer != null) {
      mTimer.cancel();
    }
  }

  /**
   * Animates a single keyframe corresponding to the index in the mission data profile. Updates the position and
   * rotation of the 2D/3D plane graphic and sets the camera viewpoint.
   *
   * @param keyFrame index in mission data to show
   */
  private void animate(int keyFrame) {

    // get the next position from the mission data
    Map<String, Object> datum = mMissionData.get(keyFrame);
    Point position = (Point) datum.get("POSITION");

    // update the HUD
    runOnUiThread(() -> {
      mCurrAltitude.setText(String.format("%.2f", position.getZ()));
      mCurrHeading.setText(String.format("%.2f", (float) datum.get("HEADING")));
      mCurrPitch.setText(String.format("%.2f", (float) datum.get("PITCH")));
      mCurrRoll.setText(String.format("%.2f", (float) datum.get("ROLL")));
    });

    // update mission progress seek bar
    mMissionProgressSeekBar.setProgress(mKeyFrame);

    // update plane's position and orientation
    mPlane3D.setGeometry(position);
    mPlane3D.getAttributes().put("HEADING", datum.get("HEADING"));
    mPlane3D.getAttributes().put("PITCH", datum.get("PITCH"));
    mPlane3D.getAttributes().put("ROLL", datum.get("ROLL"));

    // update mini map plane's position and rotation
    mPlane2D.setGeometry(position);
    if (mFollowFreeCamButton.isSelected()) {
      if (mMapView == null || position == null) {
        return;
      }
      // rotate the map view in the direction of motion to make graphic always point up
      mMapView.setViewpoint(new Viewpoint(position, mMapView.getMapScale(), 360 + (float) datum.get("HEADING")));
    } else {
      mPlane2D.getAttributes().put("ANGLE", 360 + (float) datum.get("HEADING") - mMapView.getMapRotation());
    }
  }

  /**
   * Switches between the orbiting camera controller and default globe camera controller.
   */
  private void toggleFollow(boolean follow) {
    if (follow) {
      // reset mini-map plane's rotation to point up
      mPlane2D.getAttributes().put("ANGLE", 0f);
      // set orbit camera controller
      mSceneView.setCameraController(mOrbitCameraController);
    } else {
      // set camera controller back to default
      mSceneView.setCameraController(new GlobeCameraController());
    }
  }

  /**
   * Copy the given file from the app's assets folder to the app's cache directory.
   *
   * @param fileName as String
   */
  private void copyFileFromAssetsToCache(String fileName) {
    AssetManager assetManager = getApplicationContext().getAssets();
    File file = new File(getCacheDir() + File.separator + fileName);
    if (!file.exists()) {
      try {
        InputStream in = assetManager.open(fileName);
        OutputStream out = new FileOutputStream(getCacheDir() + File.separator + fileName);
        byte[] buffer = new byte[1024];
        int read = in.read(buffer);
        while (read != -1) {
          out.write(buffer, 0, read);
          read = in.read(buffer);
        }
        Log.i(TAG, fileName + " copied to cache.");
      } catch (Exception e) {
        Log.e(TAG, "Error writing " + fileName + " to cache. " + e.getMessage());
      }
    } else {
      Log.i(TAG, fileName + " already in cache.");
    }
  }

  @Override
  protected void onPause() {
    mSceneView.pause();
    mMapView.pause();
    mTimer.cancel();
    super.onPause();
  }

  @Override
  protected void onResume() {
    super.onResume();
    mSceneView.resume();
    mMapView.resume();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mSceneView.resume();
    mMapView.dispose();
  }
}
