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

package com.esri.arcgisruntime.sample.lineofsightgeoelement;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.content.res.AssetManager;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.geoanalysis.GeoElementLineOfSight;
import com.esri.arcgisruntime.geoanalysis.LineOfSight;
import com.esri.arcgisruntime.geometry.AngularUnit;
import com.esri.arcgisruntime.geometry.AngularUnitId;
import com.esri.arcgisruntime.geometry.GeodeticCurveType;
import com.esri.arcgisruntime.geometry.GeodeticDistanceResult;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.LinearUnit;
import com.esri.arcgisruntime.geometry.LinearUnitId;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PointBuilder;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.ArcGISSceneLayer;
import com.esri.arcgisruntime.mapping.ArcGISScene;
import com.esri.arcgisruntime.mapping.ArcGISTiledElevationSource;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.Surface;
import com.esri.arcgisruntime.mapping.view.AnalysisOverlay;
import com.esri.arcgisruntime.mapping.view.Camera;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.LayerSceneProperties;
import com.esri.arcgisruntime.mapping.view.SceneView;
import com.esri.arcgisruntime.symbology.ModelSceneSymbol;
import com.esri.arcgisruntime.symbology.Renderer;
import com.esri.arcgisruntime.symbology.SceneSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.esri.arcgisruntime.symbology.SimpleRenderer;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();
  private static final LinearUnit METERS = new LinearUnit(LinearUnitId.METERS);
  private static final AngularUnit DEGREES = new AngularUnit(AngularUnitId.DEGREES);

  private int mWaypointIndex = 0;

  private SceneView mSceneView;
  private Graphic mTaxiGraphic;
  private List<Point> mWaypoints;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // authentication with an API key or named user is required to access basemaps and other
    // location services
    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY);

    // load taxi model from assets into cache directory
    copyFileFromAssetsToCache(getString(R.string.dolmus_model));
    copyFileFromAssetsToCache(getString(R.string.dolmus_back));
    copyFileFromAssetsToCache(getString(R.string.dolmus_front));
    copyFileFromAssetsToCache(getString(R.string.dolmus_side));
    copyFileFromAssetsToCache(getString(R.string.tire_tread));

    // create a scene and add a basemap to it
    ArcGISScene scene = new ArcGISScene(BasemapStyle.ARCGIS_TOPOGRAPHIC);

    // get a reference to the scene view and set the scene to it
    mSceneView = findViewById(R.id.sceneView);
    mSceneView.setScene(scene);

    // add base surface for elevation data
    Surface surface = new Surface();
    surface.getElevationSources().add(new ArcGISTiledElevationSource(getString(R.string.elevation_service_url)));
    scene.setBaseSurface(surface);

    // add buildings from New York City
    String buildingsURL = getString(R.string.new_york_buildings_service_url);
    ArcGISSceneLayer buildings = new ArcGISSceneLayer(buildingsURL);
    scene.getOperationalLayers().add(buildings);

    // create a graphics overlay for the graphics
    GraphicsOverlay graphicsOverlay = new GraphicsOverlay();
    graphicsOverlay.getSceneProperties().setSurfacePlacement(LayerSceneProperties.SurfacePlacement.RELATIVE);
    mSceneView.getGraphicsOverlays().add(graphicsOverlay);

    // set up a heading expression to handle graphic rotation
    SimpleRenderer renderer3D = new SimpleRenderer();
    Renderer.SceneProperties renderProperties = renderer3D.getSceneProperties();
    renderProperties.setHeadingExpression("[HEADING]");
    graphicsOverlay.setRenderer(renderer3D);

    // create a point graph near the Empire State Building to be the observer
    Point observationPoint = new Point(-73.9853, 40.7484, 200, SpatialReferences.getWgs84());
    Graphic observer = new Graphic(observationPoint,
        new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, 0xFFFF0000, 5));
    graphicsOverlay.getGraphics().add(observer);

    // create a slider to change the observer's Z value
    SeekBar heightSeekBar = findViewById(R.id.heightSeekBar);
    TextView currHeightTextView = findViewById(R.id.currHeightTextView);
    // offset the minimum height of the observer on the seek bar
    int seekBarMinHeightOffset = 150;
    heightSeekBar.setMax(seekBarMinHeightOffset);
    heightSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
        // use the offset to calculate the height value
        int height = progress + seekBarMinHeightOffset;
        currHeightTextView.setText(String.valueOf(height));
        PointBuilder pointBuilder = new PointBuilder((Point) observer.getGeometry());
        pointBuilder.setZ(height);
        observer.setGeometry(pointBuilder.toGeometry());
      }

      @Override public void onStartTrackingTouch(SeekBar seekBar) {

      }

      @Override public void onStopTrackingTouch(SeekBar seekBar) {

      }
    });
    // set seek bar initial progress with offset
    heightSeekBar.setProgress((int) observationPoint.getZ() - seekBarMinHeightOffset);

    // create waypoints around a block for the taxi to drive to
    mWaypoints = Arrays.asList(
        new Point(-73.984513, 40.748469, SpatialReferences.getWgs84()),
        new Point(-73.985068, 40.747786, SpatialReferences.getWgs84()),
        new Point(-73.983452, 40.747091, SpatialReferences.getWgs84()),
        new Point(-73.982961, 40.747762, SpatialReferences.getWgs84())
    );

    // create a graphic of a taxi to be the target
    String pathToModel = getCacheDir() + File.separator + getString(R.string.dolmus_model);
    ModelSceneSymbol taxiSymbol = new ModelSceneSymbol(pathToModel, 1.0);
    taxiSymbol.setAnchorPosition(SceneSymbol.AnchorPosition.BOTTOM);
    taxiSymbol.loadAsync();
    mTaxiGraphic = new Graphic(mWaypoints.get(0), taxiSymbol);
    mTaxiGraphic.getAttributes().put("HEADING", 0.0);
    graphicsOverlay.getGraphics().add(mTaxiGraphic);

    // create an analysis overlay to hold the line of sight
    AnalysisOverlay analysisOverlay = new AnalysisOverlay();
    mSceneView.getAnalysisOverlays().add(analysisOverlay);

    // create a line of sight between the two graphics and add it to the analysis overlay
    GeoElementLineOfSight lineOfSight = new GeoElementLineOfSight(observer, mTaxiGraphic);
    analysisOverlay.getAnalyses().add(lineOfSight);

    // select (highlight) the taxi when the line of sight target visibility changes to visible
    lineOfSight.addTargetVisibilityChangedListener(targetVisibilityChangedEvent -> mTaxiGraphic
        .setSelected(targetVisibilityChangedEvent.getTargetVisibility() == LineOfSight.TargetVisibility.VISIBLE)
    );

    // create a timer to animate the tank
    Timer timer = new Timer();
    timer.scheduleAtFixedRate(new TimerTask() {
      @Override public void run() {
        animate();
      }
    }, 0, 50);

    // zoom to show the observer
    Camera camera = new Camera((Point) observer.getGeometry(), 700, -30, 45, 0);
    mSceneView.setViewpointCamera(camera);
  }

  /**
   * Moves the taxi toward the current waypoint a short distance.
   */
  private void animate() {
    Point waypoint = mWaypoints.get(mWaypointIndex);
    // get current location and distance from waypoint
    Point location = (Point) mTaxiGraphic.getGeometry();
    GeodeticDistanceResult distance = GeometryEngine.distanceGeodetic(location, waypoint, METERS, DEGREES,
        GeodeticCurveType.GEODESIC);

    // move toward waypoint a short distance
    location = GeometryEngine.moveGeodetic(location, 1.0, METERS, distance.getAzimuth1(), DEGREES,
        GeodeticCurveType.GEODESIC);
    mTaxiGraphic.setGeometry(location);

    // rotate to the waypoint
    mTaxiGraphic.getAttributes().put("HEADING", distance.getAzimuth1());

    // reached waypoint, move to next waypoint
    if (distance.getDistance() <= 2) {
      mWaypointIndex = (mWaypointIndex + 1) % mWaypoints.size();
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
