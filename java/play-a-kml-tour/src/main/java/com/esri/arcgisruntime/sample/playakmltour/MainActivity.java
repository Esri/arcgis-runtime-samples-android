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

package com.esri.arcgisruntime.sample.playakmltour;

import java.util.List;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageButton;
import android.util.Log;
import android.widget.Toast;

import com.esri.arcgisruntime.layers.KmlLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISScene;
import com.esri.arcgisruntime.mapping.ArcGISTiledElevationSource;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Surface;
import com.esri.arcgisruntime.mapping.view.SceneView;
import com.esri.arcgisruntime.ogc.kml.KmlContainer;
import com.esri.arcgisruntime.ogc.kml.KmlDataset;
import com.esri.arcgisruntime.ogc.kml.KmlNode;
import com.esri.arcgisruntime.ogc.kml.KmlTour;
import com.esri.arcgisruntime.ogc.kml.KmlTourController;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private SceneView mSceneView;
  private KmlTourController mKmlTourController;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mSceneView = findViewById(R.id.sceneView);

    // create the controller used to play, pause and reset the tour
    mKmlTourController = new KmlTourController(this);

    // create a scene
    ArcGISScene scene = new ArcGISScene(Basemap.createImagery());
    mSceneView.setScene(scene);

    // add elevation data
    Surface surface = new Surface();
    surface.getElevationSources().add(new ArcGISTiledElevationSource(getString(R.string.world_terrain_service)));
    scene.setBaseSurface(surface);

    requestReadPermission();
  }

  private void createKmlTour() {

    // add a KML layer from a KML dataset with a KML tour
    KmlDataset kmlDataset = new KmlDataset(
        Environment.getExternalStorageDirectory() + getString(R.string.kml_tour_path));
    KmlLayer kmlLayer = new KmlLayer(kmlDataset);
    mSceneView.getScene().getOperationalLayers().add(kmlLayer);

    // handle play click, not enabled until kml layer has loaded
    AppCompatImageButton playButton = findViewById(R.id.playButton);
    playButton.setOnClickListener(v -> mKmlTourController.play());
    playButton.setEnabled(false);

    // handle pause click, not enabled until kml layer has loaded
    AppCompatImageButton pauseButton = findViewById(R.id.pauseButton);
    pauseButton.setOnClickListener(v -> mKmlTourController.pause());
    pauseButton.setEnabled(false);

    // handle reset click, not enabled until kml layer has loaded
    AppCompatImageButton resetButton = findViewById(R.id.resetButton);
    resetButton.setOnClickListener(v -> mKmlTourController.reset());
    resetButton.setEnabled(false);
    kmlLayer.addDoneLoadingListener(() -> {
      if (kmlLayer.getLoadStatus() == LoadStatus.LOADED) {
        // find the first KML tour in the dataset when loaded
        KmlTour kmlTour = findFirstKMLTour(kmlDataset.getRootNodes());
        if (kmlTour != null) {
          // set the tour to the tour controller and enable UI controls
          mKmlTourController.setTour(kmlTour);
          playButton.setEnabled(true);
          pauseButton.setEnabled(true);
          resetButton.setEnabled(true);
        } else {
          String error = "No KML tour found in dataset";
          Toast.makeText(this, error, Toast.LENGTH_LONG).show();
          Log.e(TAG, error);
        }
      } else {
        String error = "KML layer failed to load: " + kmlLayer.getLoadError().getMessage();
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        Log.e(TAG, error);
      }
    });
  }

  /**
   * Recursively searches for the first KML tour in a list of KML nodes.
   *
   * @return the first KML tour, or null if there are no tours
   */
  private static KmlTour findFirstKMLTour(List<KmlNode> kmlNodes) {
    for (KmlNode node : kmlNodes) {
      if (node instanceof KmlTour) {
        return (KmlTour) node;
      } else if (node instanceof KmlContainer) {
        return findFirstKMLTour(((KmlContainer) node).getChildNodes());
      }
    }
    return null;
  }

  /**
   * Request read external storage for API level 23+.
   */
  private void requestReadPermission() {
    // define permission to request
    String[] reqPermission = { Manifest.permission.READ_EXTERNAL_STORAGE };
    int requestCode = 2;
    if (ContextCompat.checkSelfPermission(this, reqPermission[0]) == PackageManager.PERMISSION_GRANTED) {
      // do something
      createKmlTour();
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
      // do something
      createKmlTour();
    } else {
      // report to user that permission was denied
      Toast.makeText(this, getString(R.string.kml_tour_read_permission_denied), Toast.LENGTH_SHORT).show();
    }
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
