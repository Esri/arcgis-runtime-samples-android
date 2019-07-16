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

package com.esri.arcgisruntime.sample.listkmlcontents;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.Toast;

import com.esri.arcgisruntime.layers.KmlLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISScene;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.SceneView;
import com.esri.arcgisruntime.ogc.kml.KmlDataset;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private SceneView mSceneView;
  private ConstraintLayout mDrawerLayout;
  private ActionBarDrawerToggle mDrawerToggle;
  private ListView mDrawerListView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mDrawerListView = findViewById(R.id.left_drawer);
    mDrawerLayout = findViewById(R.id.drawer_layout);

    // on tapping a layer in the drawer list view, toggle the check box and call toggleLayer
    mDrawerListView.setOnItemClickListener(
        (adapterView, view, i, l) ->  {
          CheckBox checkBox = view.findViewById(R.id.geopackageLayerCheckBox);
          checkBox.setChecked(!checkBox.isChecked());
          // toggles the given layer on and off
          toggleLayer(mLayersHashMap.get(mDrawerListView.getItemAtPosition(i).toString()));
        });

    // initialize the array adaptor
    mLayersStringAdaptor = new ArrayAdapter<>(this, R.layout.geopackage_layer_item, R.id.geopackageLayerCheckBox);

    // set the adapter for the list view
    mDrawerListView.setAdapter(mLayersStringAdaptor);

    // get a reference to the scene view
    mSceneView = findViewById(R.id.sceneView);

    // create a map and add it to the map view
    ArcGISScene scene = new ArcGISScene(Basemap.createImageryWithLabels());
    mSceneView.setScene(scene);

    requestReadPermission();

  }

  private void listKmlContents() {
    // load a KML dataset from a local KMZ file and show it as an operational layer
    KmlDataset kmlDataset = new KmlDataset(
        Environment.getExternalStorageDirectory() + getString(R.string.kmz_data_path));
    KmlLayer kmlLayer = new KmlLayer(kmlDataset);
    mSceneView.getScene().getOperationalLayers().add(kmlLayer);

    // when the dataset is loaded, recursively build the tree view with KML nodes starting with the root node(s)
    kmlDataset.addDoneLoadingListener(() -> {
      if (kmlDataset.getLoadStatus() == LoadStatus.LOADED) {
        Log.d(TAG, "LOADED");
        ExpandableListView expandableListView = findViewById(R.id.expandableListView);
        ExpandableListAdapter expandableListAdapter = new ExpandableListViewAdapter(this, kmlDataset.getRootNodes());
        expandableListView.setAdapter(expandableListAdapter);
        ((ExpandableListViewAdapter) expandableListAdapter).notifyDataSetChanged();
      } else {
        String error = "Error loading KML dataset: " + kmlDataset.getLoadError().getMessage();
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        Log.e(TAG, error);
      }
    });
  }

  /**
   * Request read external storage for API level 23+.
   */
  private void requestReadPermission() {
    // define permission to request
    String[] reqPermission = { Manifest.permission.READ_EXTERNAL_STORAGE };
    int requestCode = 2;
    if (ContextCompat.checkSelfPermission(this, reqPermission[0]) == PackageManager.PERMISSION_GRANTED) {
      listKmlContents();
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
      listKmlContents();
    } else {
      // report to user that permission was denied
      Toast.makeText(this, getString(R.string.kmz_read_permission_denied), Toast.LENGTH_SHORT).show();
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
