package com.esri.arcgisruntime.sample.viewshedlocation;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.SeekBar;
import android.widget.TextView;

import com.esri.arcgisruntime.geoanalysis.LocationViewshed;
import com.esri.arcgisruntime.geoanalysis.Viewshed;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.ArcGISSceneLayer;
import com.esri.arcgisruntime.mapping.ArcGISScene;
import com.esri.arcgisruntime.mapping.ArcGISTiledElevationSource;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Surface;
import com.esri.arcgisruntime.mapping.view.AnalysisOverlay;
import com.esri.arcgisruntime.mapping.view.Camera;
import com.esri.arcgisruntime.mapping.view.OrbitLocationCameraController;
import com.esri.arcgisruntime.mapping.view.SceneView;

public class MainActivity extends AppCompatActivity {

  private SceneView mSceneView;
  private LocationViewshed mViewshed;

  private int mMinDistance;
  private int mMaxDistance;
  private int mInitHeading;
  private int mInitPitch;
  private int mInitHorizontalAngle;
  private int mInitVerticalAngle;
  private int mInitMinDistance;
  private int mInitMaxDistance;

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

    // set initial values
    mInitHeading = 0;
    mInitPitch = 60;
    mInitHorizontalAngle = 120;
    mInitVerticalAngle = 90;
    mInitMinDistance = 0;
    mInitMaxDistance = 10000;
    mMinDistance = mInitMinDistance;
    mMaxDistance = mInitMaxDistance;

    // create a scene and add a basemap to it
    mSceneView = findViewById(R.id.sceneView);
    ArcGISScene scene = new ArcGISScene();
    scene.setBasemap(Basemap.createImagery());
    mSceneView.setScene(scene);

    // add base surface for elevation data
    Surface surface = new Surface();
    final String localElevationImageService = getString(com.esri.arcgisruntime.sample.viewshedlocation.R.string.elevation_service);
    surface.getElevationSources().add(new ArcGISTiledElevationSource(localElevationImageService));
    scene.setBaseSurface(surface);

    // add a scene layer
    final String buildings = getString(com.esri.arcgisruntime.sample.viewshedlocation.R.string.buildings_layer);
    ArcGISSceneLayer sceneLayer = new ArcGISSceneLayer(buildings);
    scene.getOperationalLayers().add(sceneLayer);

    // create viewshed from the camera
    Point location = new Point(-4.50, 48.4,100.0, SpatialReferences.getWgs84());
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

  private void handleUiElements() {

    // inflate UI elements
    mHeadingSeekBar = findViewById(R.id.heading_seek_bar);
    mPitchSeekBar = findViewById(R.id.pitch_seek_bar);
    mHorizontalAngleSeekBar = findViewById(R.id.horizontal_angle_seekbar);
    mVerticalAngleSeekBar= findViewById(R.id.vertical_angle_seekbar);
    mMinDistanceSeekBar = findViewById(R.id.min_distance_seekbar);
    mMaxDistanceSeekBar = findViewById(R.id.max_distance_seekbar);
    mCurrHeading = findViewById(R.id.curr_heading);
    mCurrPitch = findViewById(R.id.curr_pitch);
    mCurrHorizontalAngle = findViewById(R.id.curr_horizontal_angle);
    mCurrVerticalAngle = findViewById(R.id.curr_vertical_angle);
    mCurrMinDistance = findViewById(R.id.curr_minimum_distance);
    mCurrMaxDistance = findViewById(R.id.curr_maximum_distance);

    // heading range 0 - 360
    mHeadingSeekBar.setMax(360);
    setHeading(mInitHeading);
    mHeadingSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        int heading = seekBar.getProgress();
        setHeading(heading);
      }

      @Override public void onStartTrackingTouch(SeekBar seekBar) { }

      @Override public void onStopTrackingTouch(SeekBar seekBar) { }
    });

    mPitchSeekBar.setMax(180);
    setPitch(mInitPitch);
    mPitchSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        int pitch = seekBar.getProgress();
        setPitch(pitch);
      }

      @Override public void onStartTrackingTouch(SeekBar seekBar) { }

      @Override public void onStopTrackingTouch(SeekBar seekBar) { }
    });

    // horizontal angle range 1 - 120
    mHorizontalAngleSeekBar.setMax(120);
    setHorizontalAngle(mInitHorizontalAngle);
    mHorizontalAngleSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        int horizontalAngle = mHorizontalAngleSeekBar.getProgress();
        if (horizontalAngle > 0) { // horizontal angle must be > 0
          setHorizontalAngle(horizontalAngle);
        }
      }

      @Override public void onStartTrackingTouch(SeekBar seekBar) { }

      @Override public void onStopTrackingTouch(SeekBar seekBar) { }
    });

    // vertical angle range 1 - 120
    mVerticalAngleSeekBar.setMax(120);
    setVerticalAngle(mInitVerticalAngle);
    mVerticalAngleSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        int verticalAngle = mVerticalAngleSeekBar.getProgress();
        if (verticalAngle > 0) { // vertical angle must be > 0
          setVerticalAngle(verticalAngle);
        }
      }

      @Override public void onStartTrackingTouch(SeekBar seekBar) { }

      @Override public void onStopTrackingTouch(SeekBar seekBar) { }
    });

    mMinDistanceSeekBar.setMax(9000);
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

      @Override public void onStartTrackingTouch(SeekBar seekBar) { }

      @Override public void onStopTrackingTouch(SeekBar seekBar) { }
    });

    mMaxDistanceSeekBar.setMax(10000);
    setMaxDistance(mInitMaxDistance);
    mMaxDistanceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        mMaxDistance = seekBar.getProgress();
        if (mMaxDistance  - mMinDistance < 1000) {
          if (mMaxDistance > 1000) {
            mMinDistance = mMaxDistance - 1000;
          } else {
            mMinDistance = 0;
          }
          setMinDistance(mMinDistance);
        }
        setMaxDistance(mMaxDistance);
      }

      @Override public void onStartTrackingTouch(SeekBar seekBar) { }

      @Override public void onStopTrackingTouch(SeekBar seekBar) { }
    });
  }

  private void setHeading(int heading) {
    mViewshed.setHeading(heading);
    mHeadingSeekBar.setProgress(heading);
    mCurrHeading.setText(Integer.toString(heading));
  }

  private void setPitch(int pitch) {
    mViewshed.setPitch(pitch);
    mPitchSeekBar.setProgress(pitch);
    mCurrPitch.setText(Integer.toString(pitch));
  }

  private void setHorizontalAngle(int horizontalAngle) {
    mViewshed.setHorizontalAngle(horizontalAngle);
    mHorizontalAngleSeekBar.setProgress(horizontalAngle);
    mCurrHorizontalAngle.setText(Integer.toString(horizontalAngle));
  }

  private void setVerticalAngle(int verticalAngle) {
    mViewshed.setVerticalAngle(verticalAngle);
    mVerticalAngleSeekBar.setProgress(verticalAngle);
    mCurrVerticalAngle.setText(Integer.toString(verticalAngle));
  }

  private void setMinDistance(int minDistance) {
    mViewshed.setMinDistance(minDistance);
    mMinDistanceSeekBar.setProgress(minDistance);
    mCurrMinDistance.setText(Integer.toString(minDistance));
  }

  private void setMaxDistance(int maxDistance) {
    mViewshed.setMaxDistance(maxDistance);
    mMaxDistanceSeekBar.setProgress(maxDistance);
    mCurrMaxDistance.setText(Integer.toString(maxDistance));
  }

  @Override
  protected void onPause(){
    super.onPause();
    // pause SceneView
    mSceneView.pause();
  }

  @Override
  protected void onResume() {
    super.onResume();
    // resume SceneView
    mSceneView.resume();
  }
}
