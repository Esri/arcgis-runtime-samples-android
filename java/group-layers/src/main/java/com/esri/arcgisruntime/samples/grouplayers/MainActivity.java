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
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.layers.ArcGISSceneLayer;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.layers.GroupLayer;
import com.esri.arcgisruntime.layers.Layer;
import com.esri.arcgisruntime.mapping.ArcGISScene;
import com.esri.arcgisruntime.mapping.ArcGISTiledElevationSource;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Surface;
import com.esri.arcgisruntime.mapping.view.Camera;
import com.esri.arcgisruntime.mapping.view.SceneView;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private SceneView mSceneView;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mSceneView = findViewById(R.id.sceneView);

    ArcGISScene scene = new ArcGISScene(Basemap.createImagery());
    mSceneView.setScene(scene);

    Surface surface = new Surface();
    surface.getElevationSources().add(new ArcGISTiledElevationSource(
        "http://elevation3d.arcgis.com/arcgis/rest/services/WorldElevation3D/Terrain3D/ImageServer"));
    scene.setBaseSurface(surface);

    initializeGroupLayer(scene);
  }

  private void initializeGroupLayer(ArcGISScene scene) {
    GroupLayer groupLayer = new GroupLayer();
    groupLayer.setName("Group: Dev A");
    groupLayer.addLoadStatusChangedListener(loadStatusChangedEvent -> Log.d(TAG, "GroupLayer Load Status: " + groupLayer.getLoadStatus().toString()));
    groupLayer.addDoneLoadingListener(() -> {
      for (Layer layer : groupLayer.getLayers()) {
        Log.d(TAG, layer.getLoadStatus().toString());
      }
      mSceneView.setViewpointCamera(new Camera(groupLayer.getFullExtent().getCenter(), 700, 0, 60, 0));
    });

    for (String url : getResources().getStringArray(R.array.scene_layer_urls)) {
      ArcGISSceneLayer sceneLayer = new ArcGISSceneLayer(url);
      groupLayer.getLayers().add(sceneLayer);
    }

    ArcGISSceneLayer nonDevOne = new ArcGISSceneLayer("https://scenesampleserverdev.arcgis.com/arcgis/rest/services/Hosted/PlannedDemo_BuildingShell/SceneServer/layers/0");
    FeatureLayer nonDevTwo = new FeatureLayer(new ServiceFeatureTable("https://services.arcgis.com/P3ePLMYs2RVChkJx/arcgis/rest/services/DevelopmentProjectArea/FeatureServer/0"));

    scene.getOperationalLayers().addAll(Arrays.asList(groupLayer, nonDevOne, nonDevTwo));
  }

}
