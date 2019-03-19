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

    Portal portal = new Portal("https://www.arcgis.com");
    portal.addDoneLoadingListener(() -> {
      if (portal.getLoadStatus() == LoadStatus.LOADED) {
        PortalItem portalItem = new PortalItem(portal,"91a4fafd747a47c7bab7797066cb9272");
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
