package com.esri.arcgisruntime.animate3dgraphic;

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

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.Polyline;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.ArcGISScene;
import com.esri.arcgisruntime.mapping.ArcGISTiledElevationSource;
import com.esri.arcgisruntime.mapping.Basemap;
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
  private Graphic mRouteGraphic;
  private Graphic mPlane2D;
  private Graphic mPlane3D;
  private OrbitGeoElementCameraController mOrbitCameraController;
  private GraphicsOverlay mSceneOverlay;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // load tank model from assets into cache directory
    copyFileFromAssetsToCache(getString(R.string.bristol_model));
    copyFileFromAssetsToCache(getString(R.string.bristol_skin));

    // create a scene and add it to the scene view
    mSceneView = findViewById(R.id.sceneView);
    ArcGISScene scene = new ArcGISScene(Basemap.createImagery());
    mSceneView.setScene(scene);

    // add elevation data
    Surface surface = new Surface();
    surface.getElevationSources().add(new ArcGISTiledElevationSource(
        "http://elevation3d.arcgis.com/arcgis/rest/services/WorldElevation3D/Terrain3D/ImageServer"));
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
    ArcGISMap map = new ArcGISMap(Basemap.createImagery());
    mMapView.setMap(map);

    // create a graphics overlay for the mini map
    GraphicsOverlay mapOverlay = new GraphicsOverlay();
    mMapView.getGraphicsOverlays().add(mapOverlay);

    // create renderer to rotate the plane graphic in the mini map
    SimpleRenderer renderer2D = new SimpleRenderer();
    SimpleMarkerSymbol plane2DSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.TRIANGLE, 0xFF0000FF, 10);
    renderer2D.setSymbol(plane2DSymbol);
    renderer2D.setRotationExpression("[ANGLE]");
    mapOverlay.setRenderer(renderer2D);

    // create a placeholder graphic for showing the mission route in mini map
    SimpleLineSymbol routeSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, 0xFFFF0000, 2);
    mRouteGraphic = new Graphic();
    mRouteGraphic.setSymbol(routeSymbol);
    mapOverlay.getGraphics().add(mRouteGraphic);

    // create a graphic with a blue (0xFF0000FF) triangle symbol to represent the plane on the mini map
    Map<String, Object> attributes = new HashMap<>();
    attributes.put("ANGLE", 0f);
    mPlane2D = new Graphic(new Point(0, 0, SpatialReferences.getWgs84()), attributes);
    mapOverlay.getGraphics().add(mPlane2D);

    // request read permission
    requestWritePermission();

    // create an orbit camera controller to follow the plane
    mOrbitCameraController = new OrbitGeoElementCameraController(mPlane3D, 30.0);
    mOrbitCameraController.setCameraPitchOffset(75.0);
    mSceneView.setCameraController(mOrbitCameraController);

    // get UI elements
    mMissionSelector = findViewById(R.id.missionSelectorSpinner);
    mMissionSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
        changeMission(mMissionSelector.getSelectedItem().toString());
      }

      @Override public void onNothingSelected(AdapterView<?> adapterView) {

      }
    });
    mMissionProgressSeekBar = findViewById(R.id.missionProgressSeekBar);

    // set max speed and touch listener for speed seek bar
    mSpeedSeekBar = findViewById(R.id.speedSeekBar);
    mSpeedSeekBar.setMax(40);
    mSpeedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        startAnimation(i);
      }

      @Override public void onStartTrackingTouch(SeekBar seekBar) {
      }

      @Override public void onStopTrackingTouch(SeekBar seekBar) {
      }
    });
    mCurrAltitude = findViewById(R.id.currAltitudeTextView);
    mCurrHeading = findViewById(R.id.currHeadingTextView);
    mCurrPitch = findViewById(R.id.currPitchTextView);
    mCurrRoll = findViewById(R.id.currRollTextView);
    mFollowFreeCamButton = findViewById(R.id.followFreeCamButton);
    mFollowFreeCamButton.setOnClickListener(view -> {
      if (mFollowFreeCamButton.isSelected()) {
        mFollowFreeCamButton.setText("Follow");
        mFollowFreeCamButton.setSelected(false);
      } else {
        mFollowFreeCamButton.setText("Free cam");
        mFollowFreeCamButton.setSelected(true);
      }
      toggleFollow(mFollowFreeCamButton.isSelected());
    });
    mPlayStopButton = findViewById(R.id.playStopButton);
    mPlayStopButton.setOnClickListener(view -> {
      if (mPlayStopButton.isSelected()) {
        stopAnimation();
      } else {
        startAnimation(mSpeedSeekBar.getProgress());
      }
    });
  }

  private void buildModel() {
    // create a graphic with a ModelSceneSymbol of a plane to add to the scene
    String pathToModel = getCacheDir() + File.separator + getString(R.string.bristol_model);
    ModelSceneSymbol plane3DSymbol = new ModelSceneSymbol(pathToModel, 1.0);
    plane3DSymbol.loadAsync();
    mPlane3D = new Graphic(new Point(0, 0, 0, SpatialReferences.getWgs84()), plane3DSymbol);
    mSceneOverlay.getGraphics().add(mPlane3D);
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

    // set the mini map viewpoint to 25% larger than the route's extent
    mMapView.setViewpoint(new Viewpoint(new Envelope(route.getExtent().getCenter(), route.getExtent().getWidth() * 1.25,
        route.getExtent().getHeight() * 1.25)));

    startAnimation(mSpeedSeekBar.getProgress());
  }

  /**
   * Loads the mission data from a .csv file into memory.
   *
   * @param mission .csv file name containing the mission data
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

  private void startAnimation(int speed) {

    // update button
    mPlayStopButton.setText("Stop");
    mPlayStopButton.setSelected(false);

    // stop the current animation timer
    stopAnimation();

    int period = mSpeedSeekBar.getMax() - speed + 10;

    // create a timer to animate the tank
    mTimer = new Timer();
    mTimer.scheduleAtFixedRate(new TimerTask() {
      @Override public void run() {
        if (mKeyFrame >= mMissionData.size()) {
          mKeyFrame = 0;
        }
        animate(mKeyFrame);
        mKeyFrame++;
      }
    }, 0, period);
  }

  private void stopAnimation() {

    // update button
    mPlayStopButton.setText("Play");
    mPlayStopButton.setSelected(true);

    // cancel the existing timer
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

    mMissionProgressSeekBar.setProgress(mKeyFrame);

    // update plane's position and orientation
    mPlane3D.setGeometry(position);
    mPlane3D.getAttributes().put("HEADING", datum.get("HEADING"));
    mPlane3D.getAttributes().put("PITCH", datum.get("PITCH"));
    mPlane3D.getAttributes().put("ROLL", datum.get("ROLL"));

    // update mini map plane's position and rotation
    mPlane2D.setGeometry(position);
    if (mFollowFreeCamButton.isSelected()) {
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
   * Request write permission on the device.
   */
  private void requestWritePermission() {
    // define permission to request
    String[] reqPermission = new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE };
    int requestCode = 2;
    // For API level 23+ request permission at runtime
    if (ContextCompat.checkSelfPermission(MainActivity.this,
        reqPermission[0]) == PackageManager.PERMISSION_GRANTED) {
      buildModel();
    } else {
      // request permission
      ActivityCompat.requestPermissions(MainActivity.this, reqPermission, requestCode);
    }
  }

  /**
   * Handle permission request response.
   */
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      buildModel();
    } else {
      // report to user that permission was denied
      Toast.makeText(MainActivity.this, getResources().getString(R.string.write_permission_denied),
          Toast.LENGTH_SHORT).show();
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

}
