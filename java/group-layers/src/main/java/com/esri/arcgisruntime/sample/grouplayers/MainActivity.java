/*
 * Copyright 2020 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.esri.arcgisruntime.sample.grouplayers;

import java.util.Arrays;
import java.util.List;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.layers.ArcGISSceneLayer;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.layers.GroupLayer;
import com.esri.arcgisruntime.layers.GroupVisibilityMode;
import com.esri.arcgisruntime.layers.Layer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISScene;
import com.esri.arcgisruntime.mapping.ArcGISTiledElevationSource;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Surface;
import com.esri.arcgisruntime.mapping.view.Camera;
import com.esri.arcgisruntime.mapping.view.DefaultSceneViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.SceneView;
import com.esri.arcgisruntime.util.ListenableList;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

public class MainActivity extends AppCompatActivity implements OnLayerCheckedChangedListener {

  private SceneView mSceneView;
  private BottomSheetBehavior mBottomSheetBehavior;
  private RecyclerView mLayersRecyclerView;
  private LayersAdapter mLayersAdapter;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mSceneView = findViewById(R.id.sceneView);
    mLayersRecyclerView = findViewById(R.id.layersRecyclerView);

    View bottomSheet = findViewById(R.id.bottomSheet);
    mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

    mSceneView.setOnTouchListener(new DefaultSceneViewOnTouchListener(mSceneView) {
      @Override public boolean onTouch(View view, MotionEvent motionEvent) {
        if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
          mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
          return true;
        }
        return super.onTouch(view, motionEvent);
      }
    });

    // create a scene with a basemap and add it to the scene view
    ArcGISScene scene = new ArcGISScene();
    scene.setBasemap(Basemap.createImagery());
    mSceneView.setScene(scene);

    // set the base surface with world elevation
    Surface surface = new Surface();
    surface.getElevationSources().add(new ArcGISTiledElevationSource(getString(R.string.elevation_source_url)));
    scene.setBaseSurface(surface);

    // create different types of layers
    ArcGISSceneLayer trees = new ArcGISSceneLayer(getString(R.string.tree_scene_service));
    FeatureLayer pathways = new FeatureLayer(new ServiceFeatureTable(getString(R.string.pathway_feature_service)));
    ArcGISSceneLayer buildingsA = new ArcGISSceneLayer(getString(R.string.building_scene_service_a));
    ArcGISSceneLayer buildingsB = new ArcGISSceneLayer(getString(R.string.building_scene_service_b));
    FeatureLayer projectArea = new FeatureLayer(
        new ServiceFeatureTable(getString(R.string.project_area_feature_service)));

    // create a group layer from scratch by adding the trees, pathways, and project area as children
    GroupLayer projectAreaGroupLayer = new GroupLayer();
    projectAreaGroupLayer.setName("Project area group");
    projectAreaGroupLayer.getLayers().addAll(Arrays.asList(projectArea, pathways, trees));

    // create a group layer for the buildings and set its visibility mode to exclusive
    GroupLayer buildingsGroupLayer = new GroupLayer();
    buildingsGroupLayer.setName("Buildings group");
    buildingsGroupLayer.getLayers().addAll(Arrays.asList(buildingsA, buildingsB));
    buildingsGroupLayer.setVisibilityMode(GroupVisibilityMode.EXCLUSIVE);

    // add the group layer and other layers to the scene as operational layers
    scene.getOperationalLayers().addAll(Arrays.asList(projectAreaGroupLayer, buildingsGroupLayer));

    // zoom to the extent of the group layer when the child layers are loaded
    ListenableList<Layer> layers = buildingsGroupLayer.getLayers();
    for (Layer childLayer : layers) {
      childLayer.addDoneLoadingListener(() -> {
        if (childLayer.getLoadStatus() == LoadStatus.LOADED) {
          mSceneView.setViewpointCamera(new Camera(buildingsGroupLayer.getFullExtent().getCenter(), 700, 0, 60, 0));
        }
      });
    }

    setupRecyclerView(scene.getOperationalLayers());
  }

  /**
   * Setup {@link RecyclerView} to display layers
   *
   * @param layers
   */
  private void setupRecyclerView(List<Layer> layers) {
    mLayersAdapter = new LayersAdapter(this);
    mLayersRecyclerView.setAdapter(mLayersAdapter);

    for (Layer layer : layers) {
      // if layer can be shown in legend
      if (layer.canShowInLegend()) {
        layer.addDoneLoadingListener(() -> mLayersAdapter.addLayer(layer));
        layer.loadAsync();
      }
    }
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_group_layer, menu);
    return true;
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.action_show_layer_list) {
      // if bottom sheet is not shown
      if (mBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
        // show bottom sheet
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
      } else {
        // hide bottom sheet
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
      }
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  /**
   * Called when a checkbox on a layer in the list is checked or unchecked
   *
   * @param layer   that has been checked or unchecked
   * @param checked whether the checkbox has been checked or unchecked
   */
  @Override public void layerCheckedChanged(Layer layer, boolean checked) {
    layer.setVisible(checked);
    if (layer instanceof GroupLayer) {
      for (Layer childLayer : ((GroupLayer) layer).getLayers()) {
        childLayer.setVisible(checked);
      }
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
