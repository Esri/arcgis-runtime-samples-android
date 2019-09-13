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

package com.esri.arcgisruntime.explorescenesinar;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.layers.IntegratedMeshLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISScene;
import com.esri.arcgisruntime.mapping.ArcGISTiledElevationSource;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.NavigationConstraint;
import com.esri.arcgisruntime.mapping.view.AtmosphereEffect;
import com.esri.arcgisruntime.mapping.view.Camera;
import com.esri.arcgisruntime.mapping.view.SpaceEffect;
import com.esri.arcgisruntime.portal.Portal;
import com.esri.arcgisruntime.portal.PortalItem;
import com.esri.arcgisruntime.toolkit.ar.ArcGISArView;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private ArcGISArView mArView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mArView = findViewById(R.id.arView);
    mArView.registerLifecycle(getLifecycle());
    mArView.setTracking(true);

    // create scene with imagery basemap
    ArcGISScene scene = new ArcGISScene(Basemap.createImagery());

    // create an integrated mesh layer
    Portal portal = new Portal(getString(R.string.arcgis_portal_url));
    PortalItem portalItem = new PortalItem(portal, getString(R.string.vricon_integrated_mesh_layer_url));
    IntegratedMeshLayer integratedMeshLayer = new IntegratedMeshLayer(portalItem);
    scene.getOperationalLayers().add(integratedMeshLayer);

    // create an elevation source and add it to the scene
    ArcGISTiledElevationSource elevationSource = new ArcGISTiledElevationSource(
        getString(R.string.world_terrain_service_url));
    scene.getBaseSurface().getElevationSources().add(elevationSource);
    scene.getBaseSurface().setNavigationConstraint(NavigationConstraint.NONE);

    // add the scene to the scene view
    mArView.getSceneView().setScene(scene);

    // wait for the layer to load, then set the AR camera
    integratedMeshLayer.addDoneLoadingListener(() -> {
      if (integratedMeshLayer.getLoadStatus() == LoadStatus.LOADED) {
        Envelope envelope = integratedMeshLayer.getFullExtent();
        Camera camera = new Camera(envelope.getCenter().getY(), envelope.getCenter().getX(), 250, 0, 90, 0);
        mArView.setOriginCamera(camera);
      } else {
        String error = getString(R.string.error_loading_integrated_mesh_layer) + integratedMeshLayer.getLoadError().getMessage();
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        Log.e(TAG, error);
      }
    });

    // set the translation factor to enable rapid movement through the scene
    mArView.setTranslationFactor(1000);

    // turn the space and atmosphere effects on for an immersive experience
    mArView.getSceneView().setSpaceEffect(SpaceEffect.STARS);
    mArView.getSceneView().setAtmosphereEffect(AtmosphereEffect.REALISTIC);
  }

  @Override
  protected void onPause() {
    mArView.getSceneView().pause();
    super.onPause();
  }

  @Override
  protected void onResume() {
    super.onResume();
    mArView.getSceneView().resume();
  }

  @Override
  protected void onDestroy() {
    mArView.getSceneView().dispose();
    super.onDestroy();
  }
}
