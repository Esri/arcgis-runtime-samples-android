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

import java.util.ArrayList;
import java.util.List;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.layers.KmlLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISScene;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.SceneView;
import com.esri.arcgisruntime.ogc.kml.KmlContainer;
import com.esri.arcgisruntime.ogc.kml.KmlDataset;
import com.esri.arcgisruntime.ogc.kml.KmlDocument;
import com.esri.arcgisruntime.ogc.kml.KmlFolder;
import com.esri.arcgisruntime.ogc.kml.KmlGroundOverlay;
import com.esri.arcgisruntime.ogc.kml.KmlNetworkLink;
import com.esri.arcgisruntime.ogc.kml.KmlNode;
import com.esri.arcgisruntime.ogc.kml.KmlPlacemark;
import com.esri.arcgisruntime.ogc.kml.KmlScreenOverlay;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private ActionBarDrawerToggle mDrawerToggle;
  private DrawerLayout mDrawerLayout;
  private ListView mDrawerListView;
  private List<KmlNode> mFlattenedKmlNodeList;
  private ArrayAdapter<String> mNodeNameAdapter;

  private SceneView mSceneView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mDrawerListView = findViewById(R.id.listView);
    mDrawerLayout = findViewById(R.id.drawer);
    setUpDrawerToggle();

    // get a reference to the scene view
    mSceneView = findViewById(R.id.sceneView);

    // create a map and add it to the map view
    ArcGISScene scene = new ArcGISScene(Basemap.createImageryWithLabels());
    mSceneView.setScene(scene);

    // initialize the flattened list of kml nodes
    mFlattenedKmlNodeList = new ArrayList<>();

    // initialize the array adaptor
    mNodeNameAdapter = new ArrayAdapter<>(this, R.layout.node_row);

    // set the adapter for the list view
    mDrawerListView.setAdapter(mNodeNameAdapter);

    requestReadPermission();
  }

  private void listKmlContents() {
    // load a KML dataset from a local KMZ file and show it as an operational layer
    KmlDataset kmlDataset = new KmlDataset(Environment.getExternalStorageDirectory() + getString(R.string.kmz_data_path));
    KmlLayer kmlLayer = new KmlLayer(kmlDataset);
    mSceneView.getScene().getOperationalLayers().add(kmlLayer);

    // listen for the KML dataset to finish loading
    kmlDataset.addDoneLoadingListener(() -> {
      if (kmlDataset.getLoadStatus() == LoadStatus.LOADED) {
        // for each KML node in the dataset
        for (KmlNode kmlNode : kmlDataset.getRootNodes()) {
          // add the parent node to a list
          mFlattenedKmlNodeList.add(kmlNode);
          // add the parent node to the adapter for use in the drawer list view
          mNodeNameAdapter.add(kmlNode.getName());
          // add the flattened list of children to the lists
          flattenKmlNodes(kmlNode, 1);
        }
        // once all nodes have been written out to the
        mNodeNameAdapter.notifyDataSetChanged();
        // on tapping a layer in the drawer list view, set the scene view to the selected node's extent
        mDrawerListView.setOnItemClickListener(
            (adapterView, view, i, l) -> {
              KmlNode selectedNode = mFlattenedKmlNodeList.get(i);
              Envelope nodeExtent = selectedNode.getExtent();
              if (nodeExtent != null && !nodeExtent.isEmpty()) {
                mSceneView.setViewpointAsync(new Viewpoint(nodeExtent));
                mDrawerLayout.closeDrawer(Gravity.START);
              }
            });
        // open the drawer once the dataset is first loaded
        mDrawerLayout.openDrawer(Gravity.START);
      } else {
        String error = "Error loading KML dataset: " + kmlDataset.getLoadError().getMessage();
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        Log.e(TAG, error);
      }
    });
  }

  /**
   * Flatten all child nodes of the given KML node into a list, keeping track of the recursion depth.
   *
   * @param kmlNode for which to flatten children
   * @param recursionDepth as an int
   */
  private void flattenKmlNodes(KmlNode kmlNode, int recursionDepth) {
    // set all nodes to be visible
    kmlNode.setVisible(true);
    // peek to see if the kml node has children
    if (!getChildren(kmlNode).isEmpty()) {
      // get each child
      for (KmlNode child : getChildren(kmlNode)) {
        // add the child to a list
        mFlattenedKmlNodeList.add(child);
        // add the kml node's name prepended with spaces to reflect the node's recursion depth and appended with the
        // layer's type
        mNodeNameAdapter.add(spacesForRecursionDepth(recursionDepth) + child.getName() + getKmlNodeType(child));
        // recursively call to get all children, keeping track of the recursion depth
        flattenKmlNodes(child, recursionDepth + 1);
      }
    }
  }

  /**
   * Returns the list of child nodes of the given node, for those node types which can have children.
   *
   * @param parentNode a kml node from which a list of children is returned, if possible
   * @return list of child kml nodes
   */
  private static List<KmlNode> getChildren(KmlNode parentNode) {
    List<KmlNode> children = new ArrayList<>();
    // if the node is of a type that can have children
    if (parentNode instanceof KmlContainer) {
      children.addAll(((KmlContainer) parentNode).getChildNodes());
    } else if (parentNode instanceof KmlNetworkLink) {
      children.addAll(((KmlNetworkLink) parentNode).getChildNodes());
    }
    return children;
  }

  /**
   * Return the type of the given kml node as a string prepended with a '-'.
   *
   * @param kmlNode from which to get the the type
   * @return type as a string prepended with a '-'
   */
  private static String getKmlNodeType(KmlNode kmlNode) {
    String type = null;
    if (kmlNode instanceof KmlDocument) {
      type = "KmlDocument";
    } else if (kmlNode instanceof KmlFolder) {
      type = "KmlFolder";
    } else if (kmlNode instanceof KmlGroundOverlay) {
      type = "KmlGroundOverlay";
    } else if (kmlNode instanceof KmlScreenOverlay) {
      type = "KmlScreenOverlay";
    } else if (kmlNode instanceof KmlPlacemark) {
      type = "KmlPlacemark";
    }
    return " - " + type;
  }

  /**
   * Return a string with a number of spaces equal to the recursion depth.
   *
   * @param recursionDepth as an int
   * @return a string of spaces equal to recursion depth
   */
  private static String spacesForRecursionDepth(int recursionDepth) {
    String spaces = "";
    if (recursionDepth > 0) {
      spaces = String.format("%1$" + recursionDepth * 2 + 's', "");
    }
    return spaces;
  }

  private void setUpDrawerToggle() {
    ActionBar actionBar = getSupportActionBar();
    actionBar.setDisplayHomeAsUpEnabled(true);
    actionBar.setHomeButtonEnabled(true);

    mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {
      @Override
      public void onDrawerClosed(View drawerView) {
        invalidateOptionsMenu();
      }

      @Override
      public void onDrawerOpened(View drawerView) {
        invalidateOptionsMenu();
      }
    };

    mDrawerLayout.addDrawerListener(mDrawerToggle);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
  }

  @Override
  protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
    mDrawerToggle.syncState();
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
