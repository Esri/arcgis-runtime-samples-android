/*
 * Copyright 2019 Esri
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

package com.esri.arcgisruntime.samples.grouplayers;

import java.util.Arrays;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.layers.ArcGISSceneLayer;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.layers.GroupLayer;
import com.esri.arcgisruntime.layers.Layer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISScene;
import com.esri.arcgisruntime.mapping.ArcGISTiledElevationSource;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Surface;
import com.esri.arcgisruntime.mapping.view.Camera;
import com.esri.arcgisruntime.mapping.view.SceneView;
import com.esri.arcgisruntime.util.ListenableList;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private SceneView mSceneView;
  private View mBottomSheet;
  private BottomSheetBehavior mBottomSheetBehavior;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mSceneView = findViewById(R.id.sceneView);
    mBottomSheet = findViewById(R.id.bottomSheet);
    mBottomSheetBehavior = BottomSheetBehavior.from(mBottomSheet);
    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

    // create a scene with a basemap and add it to the scene view
    ArcGISScene scene = new ArcGISScene();
    scene.setBasemap(Basemap.createImagery());
    mSceneView.setScene(scene);

    // set the base surface with world elevation
    Surface surface = new Surface();
    surface.getElevationSources().add(new ArcGISTiledElevationSource(
        "http://elevation3d.arcgis.com/arcgis/rest/services/WorldElevation3D/Terrain3D/ImageServer"));
    scene.setBaseSurface(surface);

    // create different types of layers
    ArcGISSceneLayer devOne = new ArcGISSceneLayer(
        "https://scenesampleserverdev.arcgis.com/arcgis/rest/services/Hosted/DevA_Trees/SceneServer/layers/0");
    ArcGISSceneLayer devTwo = new ArcGISSceneLayer(
        "https://scenesampleserverdev.arcgis.com/arcgis/rest/services/Hosted/DevA_Pathways/SceneServer/layers/0");
    ArcGISSceneLayer devThree = new ArcGISSceneLayer(
        "https://scenesampleserverdev.arcgis.com/arcgis/rest/services/Hosted/DevA_BuildingShell_Textured/SceneServer/layers/0");
    ArcGISSceneLayer nonDevOne = new ArcGISSceneLayer(
        "https://scenesampleserverdev.arcgis.com/arcgis/rest/services/Hosted/PlannedDemo_BuildingShell/SceneServer/layers/0");
    FeatureLayer nonDevTwo = new FeatureLayer(new ServiceFeatureTable(
        "https://services.arcgis.com/P3ePLMYs2RVChkJx/arcgis/rest/services/DevelopmentProjectArea/FeatureServer/0"));

    // create a group layer from scratch by adding the layers as children
    GroupLayer groupLayer = new GroupLayer();
    groupLayer.setName("Group: Dev A");
    groupLayer.getLayers().addAll(Arrays.asList(devOne, devTwo, devThree));

    // add the group layer and other layers to the scene as operational layers
    scene.getOperationalLayers().addAll(Arrays.asList(groupLayer, nonDevOne, nonDevTwo));

    // zoom to the extent of the group layer when the child layers are loaded
    ListenableList<Layer> layers = groupLayer.getLayers();
    for (Layer childLayer : layers) {
      childLayer.addDoneLoadingListener(() -> {
        if (childLayer.getLoadStatus() == LoadStatus.LOADED) {
          mSceneView.setViewpointCamera(new Camera(groupLayer.getFullExtent().getCenter(), 700, 0, 60, 0));
        }
      });
    }
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_main_activity, menu);
    return true;
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.action_show_layer_list) {
      if (mBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
      } else {
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
      }
      return true;
    }
    return super.onOptionsItemSelected(item);
  }
}
