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

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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

public class MainActivity extends AppCompatActivity implements KmlNodeAdapter.OnItemClickListener {

  private static final String TAG = MainActivity.class.getSimpleName();

  private TextView mBreadcrumbTextView;
  private List<KmlNode> mKmlNodeList;
  private List<String> mKmlNodeNames;
  private List<BitmapDrawable> mKmlNodeUxIcons;
  private KmlNodeAdapter mKmlNodeAdapter;

  private SceneView mSceneView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // get a reference to the scene view
    mSceneView = findViewById(R.id.sceneView);

    // get a reference to the android views
    RecyclerView recyclerView = findViewById(R.id.recyclerView);
    mBreadcrumbTextView = findViewById(R.id.breadcrumbTextView);

    // create a map and add it to the map view
    ArcGISScene scene = new ArcGISScene(Basemap.createImageryWithLabels());
    mSceneView.setScene(scene);

    // initialize arrays
    mKmlNodeList = new ArrayList<>();
    mKmlNodeNames = new ArrayList<>();
    mKmlNodeUxIcons = new ArrayList<>();

    // initialize the array adaptor
    mKmlNodeAdapter = new KmlNodeAdapter(mKmlNodeNames, mKmlNodeUxIcons, this);

    // set the adapter for the list view
    recyclerView.setAdapter(mKmlNodeAdapter);
    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
    linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
    recyclerView.setLayoutManager(linearLayoutManager);

    // load a KML dataset from a local KMZ file and show it as an operational layer
    KmlDataset kmlDataset = new KmlDataset(getExternalFilesDir(null) + getString(R.string.kmz_data_path));
    KmlLayer kmlLayer = new KmlLayer(kmlDataset);
    mSceneView.getScene().getOperationalLayers().add(kmlLayer);

    // listen for the KML dataset to finish loading
    kmlDataset.addDoneLoadingListener(() -> {
      if (kmlDataset.getLoadStatus() == LoadStatus.LOADED) {
        // for each KML node in the dataset
        for (KmlNode kmlNode : kmlDataset.getRootNodes()) {
          // add the parent node to the list
          mKmlNodeList.add(kmlNode);
          // add the node name to the list
          mKmlNodeNames.add(kmlNode.getName());
          // add the node icon to the list, if the node has an icon
          if (kmlNode.getUxIcon() != null) {
            mKmlNodeUxIcons.add(getBitmapFromByteArray(kmlNode.getUxIcon()));
          }
        }
        mKmlNodeAdapter.notifyDataSetChanged();

        // on tapping the bread crumb
        mBreadcrumbTextView.setOnClickListener(v -> {
          if (!mKmlNodeList.isEmpty() && mKmlNodeList.get(0).getParentNode() != null) {
            // get a reference to the grand parent node
            KmlNode grandparentNode = mKmlNodeList.get(0).getParentNode().getParentNode();
            if (grandparentNode != null) {
              createListForKmlNode(grandparentNode);

              // set the scene view viewpoint to the extent of the selected node
              Envelope nodeExtent = grandparentNode.getExtent();
              if (nodeExtent != null && !nodeExtent.isEmpty()) {
                mSceneView.setViewpointAsync(new Viewpoint(nodeExtent));
              }
              // build the breadcrumb path
              StringBuilder breadcrumbPathBuilder = new StringBuilder();
              buildKmlBreadcrumbPath(grandparentNode, breadcrumbPathBuilder);
              mBreadcrumbTextView.setText(breadcrumbPathBuilder.toString());
            }
          }
        });
      } else {
        String error = "Error loading KML dataset: " + kmlDataset.getLoadError().getMessage();
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        Log.e(TAG, error);
      }
    });
  }

  /**
   * On tapping a layer in the drawer list view, set the scene view to the tapped node's extent
   *
   * @param position
   */
  @Override public void onItemClick(int position) {
    KmlNode selectedNode = mKmlNodeList.get(position);
    // set the scene view viewpoint to the extent of the selected node
    Envelope nodeExtent = selectedNode.getExtent();
    if (nodeExtent != null && !nodeExtent.isEmpty()) {
      mSceneView.setViewpointAsync(new Viewpoint(nodeExtent));
    }
    // if the node has children, update the list view with the children
    if (!getChildren(selectedNode).isEmpty()) {
      createListForKmlNode(selectedNode);
      StringBuilder breadcrumbPathBuilder = new StringBuilder();
      buildKmlBreadcrumbPath(selectedNode, breadcrumbPathBuilder);
      mBreadcrumbTextView.setText(breadcrumbPathBuilder.toString());
    }
  }

  private void createListForKmlNode(KmlNode selectedNode) {
    // clear the node name adapter and current selection
    mKmlNodeList.clear();
    mKmlNodeNames.clear();
    mKmlNodeUxIcons.clear();
    mKmlNodeList = getChildren(selectedNode);
    for (KmlNode childNode : mKmlNodeList) {
      // some of the nodes in the dataset have their default visibility to off, so set all nodes to visible
      childNode.setVisible(true);
      // build a string consisting of node name, type and a chevron implying whether the node has children
      StringBuilder nodeName = new StringBuilder(childNode.getName());
      // if the node doesn't have an icon, append text indicating the node type instead
      if (childNode.getUxIcon() == null) {
        nodeName.append(getKmlNodeType(childNode));
      }
      // if the node has children, append a > to indicate further drill down is possible
      if (!getChildren(childNode).isEmpty()) {
        nodeName.append(" > ");
      }
      // add the node name to the list
      mKmlNodeNames.add(nodeName.toString());
      // add the node icon to the list
      if (childNode.getUxIcon() != null) {
        mKmlNodeUxIcons.add(getBitmapFromByteArray(childNode.getUxIcon()));
      }
    }
    // notify that the node name adapter's dataset has changed
    mKmlNodeAdapter.notifyDataSetChanged();
  }

  /**
   * Recursively build the node's bread crumb path for display at the the top of the drill down menu.
   *
   * @param kmlNode
   * @param pathBuilder
   */
  private static void buildKmlBreadcrumbPath(KmlNode kmlNode, StringBuilder pathBuilder) {
    if (kmlNode.getParentNode() != null) {
      buildKmlBreadcrumbPath(kmlNode.getParentNode(), pathBuilder);
      pathBuilder.append(" > ");
    }
    pathBuilder.append(kmlNode.getName());
  }

  private static BitmapDrawable getBitmapFromByteArray(byte[] byteArray) {
    ByteArrayInputStream bytes = new ByteArrayInputStream(byteArray);
    return (BitmapDrawable) Drawable.createFromStream(bytes, "kmlIcon");
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
