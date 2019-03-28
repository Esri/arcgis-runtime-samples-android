/*
 * Copyright 2019 Esri
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

package com.esri.arcgisruntime.sample.viewcontentbeneaththeterrainsurface;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISScene;
import com.esri.arcgisruntime.mapping.NavigationConstraint;
import com.esri.arcgisruntime.mapping.view.SceneView;
import com.esri.arcgisruntime.portal.Portal;
import com.esri.arcgisruntime.portal.PortalItem;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private SceneView mSceneView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mSceneView = findViewById(R.id.sceneView);

    Portal portal = new Portal(getString(R.string.arcgis_online_url));
    portal.addDoneLoadingListener(() -> {
      if (portal.getLoadStatus() == LoadStatus.LOADED) {
        PortalItem portalItem = new PortalItem(portal, getString(R.string.subsurface_item_id));
        portalItem.addDoneLoadingListener(() -> {
          if (portalItem.getLoadStatus() == LoadStatus.LOADED) {
            // create a scene from a web scene Url and set it to the scene view
            ArcGISScene scene = new ArcGISScene(portalItem);
            // when the scene has loaded, set navigation constraint and opacity to see below the surface
            scene.addDoneLoadingListener(() -> {
              // ensure the navigation constraint is set to NONE
              scene.getBaseSurface().setNavigationConstraint(NavigationConstraint.NONE);
              // set opacity to view content beneath the base surface
              scene.getBaseSurface().setOpacity(0.5f);
            });
            mSceneView.setScene(scene);
          } else {
            String error = "Portal item failed to load: " + portalItem.getLoadError().getMessage();
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            Log.e(TAG, error);
          }
        });
        portalItem.loadAsync();
      } else {
        String error = "Portal failed to load: " + portal.getLoadError().getMessage();
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        Log.e(TAG, error);
      }
    });
    portal.loadAsync();
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

  @Override protected void onDestroy() {
    mSceneView.dispose();
    super.onDestroy();
  }
}
