/* Copyright 2018 ESRI
 *
 * All rights reserved under the copyright laws of the United States
 * and applicable international laws, treaties, and conventions.
 *
 * You may freely redistribute and use this sample code, with or
 * without modification, provided you include the original copyright
 * notice and use restrictions.
 *
 * See the Sample code usage restrictions document for further information.
 *
 */

package com.esri.arcgisruntime.sample.viewshedgeoelement;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.geoanalysis.GeoElementViewshed;
import com.esri.arcgisruntime.geometry.AngularUnit;
import com.esri.arcgisruntime.geometry.AngularUnitId;
import com.esri.arcgisruntime.geometry.GeodeticCurveType;
import com.esri.arcgisruntime.geometry.GeodeticDistanceResult;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.LinearUnit;
import com.esri.arcgisruntime.geometry.LinearUnitId;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.ArcGISSceneLayer;
import com.esri.arcgisruntime.mapping.ArcGISScene;
import com.esri.arcgisruntime.mapping.ArcGISTiledElevationSource;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.Surface;
import com.esri.arcgisruntime.mapping.view.AnalysisOverlay;
import com.esri.arcgisruntime.mapping.view.DefaultSceneViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.LayerSceneProperties;
import com.esri.arcgisruntime.mapping.view.OrbitGeoElementCameraController;
import com.esri.arcgisruntime.mapping.view.SceneView;
import com.esri.arcgisruntime.symbology.ModelSceneSymbol;
import com.esri.arcgisruntime.symbology.Renderer;
import com.esri.arcgisruntime.symbology.SceneSymbol;
import com.esri.arcgisruntime.symbology.SimpleRenderer;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private static final LinearUnit METERS = new LinearUnit(LinearUnitId.METERS);
  private static final AngularUnit DEGREES = new AngularUnit(AngularUnitId.DEGREES);
  private SceneView mSceneView;
  private Point mWaypoint;
  private Graphic mTankGraphic;
  private Timer mTimer;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // authentication with an API key or named user is required to access basemaps and other
    // location services
    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY);

    // create a scene and add a basemap to it
    ArcGISScene scene = new ArcGISScene(BasemapStyle.ARCGIS_IMAGERY);

    // add the SceneView to the stack pane
    mSceneView = findViewById(R.id.sceneView);
    mSceneView.setScene(scene);

    // add base surface for elevation data
    Surface surface = new Surface();
    surface.getElevationSources().add(new ArcGISTiledElevationSource(getString(R.string.elevation_service)));
    scene.setBaseSurface(surface);

    // add a scene layer
    ArcGISSceneLayer sceneLayer = new ArcGISSceneLayer(getString(R.string.buildings_layer));
    scene.getOperationalLayers().add(sceneLayer);

    // request read permission
    requestWritePermission();
  }

  /**
   * Creates a GeoElement Viewshed fixed to a graphic of a tank. Includes a touch listener which uses a single tap as a
   * waypoint for navigation of the tank and associated viewshed.
   */
  private void viewshedGeoElement() {

    // load tank model from assets into cache directory
    copyFileFromAssetsToCache(getString(R.string.bradley_model));
    copyFileFromAssetsToCache(getString(R.string.bradley_skin));

    // create a graphics overlay for the tank
    GraphicsOverlay graphicsOverlay = new GraphicsOverlay();
    graphicsOverlay.getSceneProperties().setSurfacePlacement(LayerSceneProperties.SurfacePlacement.RELATIVE);
    mSceneView.getGraphicsOverlays().add(graphicsOverlay);

    // set up heading expression for tank
    SimpleRenderer renderer3D = new SimpleRenderer();
    Renderer.SceneProperties renderProperties = renderer3D.getSceneProperties();
    renderProperties.setHeadingExpression("[HEADING]");
    graphicsOverlay.setRenderer(renderer3D);

    String pathToModel = getCacheDir() + File.separator + getString(R.string.bradley_model);

    ModelSceneSymbol tankSymbol = new ModelSceneSymbol(pathToModel, 10.0);
    tankSymbol.setHeading(90);
    tankSymbol.setAnchorPosition(SceneSymbol.AnchorPosition.BOTTOM);
    mTankGraphic = new Graphic(new Point(-4.506390, 48.385624, SpatialReferences.getWgs84()), tankSymbol);
    mTankGraphic.getAttributes().put("HEADING", 0.0);
    graphicsOverlay.getGraphics().add(mTankGraphic);

    // create a viewshed to attach to the tank
    GeoElementViewshed geoElementViewshed = new GeoElementViewshed(mTankGraphic, 90.0, 40.0, 0.1, 250.0, 0.0, 0.0);
    // offset viewshed observer location to top of tank
    geoElementViewshed.setOffsetZ(3.0);

    // create an analysis overlay to add the viewshed to the scene view
    AnalysisOverlay analysisOverlay = new AnalysisOverlay();
    analysisOverlay.getAnalyses().add(geoElementViewshed);
    mSceneView.getAnalysisOverlays().add(analysisOverlay);

    // set the waypoint where the user taps
    mSceneView.setOnTouchListener(new DefaultSceneViewOnTouchListener(mSceneView) {
      @Override public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
        // get a screen point from the motion event
        android.graphics.Point screenPoint = new android.graphics.Point(Math.round(motionEvent.getX()),
            Math.round(motionEvent.getY()));

        // convert the screen point to a scene point
        mWaypoint = mSceneView.screenToBaseSurface(screenPoint);

        // create a timer to animate the tank
        mTimer = new Timer();
        mTimer.scheduleAtFixedRate(new TimerTask() {
          @Override public void run() {
            animate();
          }
        }, 0, 50);

        return true;
      }
    });

    // set camera controller to follow tank
    OrbitGeoElementCameraController cameraController = new OrbitGeoElementCameraController(mTankGraphic, 200.0);
    cameraController.setCameraPitchOffset(45.0);
    mSceneView.setCameraController(cameraController);
  }

  /**
   * Moves the tank toward the current waypoint a short distance.
   */
  private void animate() {
    if (mWaypoint != null) {
      // get current location and distance from waypoint
      Point location = (Point) mTankGraphic.getGeometry();
      GeodeticDistanceResult distance = GeometryEngine
          .distanceGeodetic(location, mWaypoint, METERS, DEGREES, GeodeticCurveType.GEODESIC);

      // move toward waypoint a short distance
      location = GeometryEngine
          .moveGeodetic(location, 1.0, METERS, distance.getAzimuth1(), DEGREES, GeodeticCurveType.GEODESIC);
      mTankGraphic.setGeometry(location);

      // rotate toward waypoint
      double heading = (double) mTankGraphic.getAttributes().get("HEADING");
      mTankGraphic.getAttributes().put("HEADING", heading + ((distance.getAzimuth1() - heading) / 10));

      // reached waypoint, stop moving and set waypoint to null
      if (distance.getDistance() <= 5) {
        mTimer.cancel();
        mWaypoint = null;
      }
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
      viewshedGeoElement();
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
      viewshedGeoElement();
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

  @Override
  protected void onPause() {
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

  @Override
  protected void onDestroy() {
    super.onDestroy();
    // dispose SceneView
    mSceneView.dispose();
  }
}
