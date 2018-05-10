package com.esri.arcgisruntime.scenelayeridentify;

import java.util.concurrent.ExecutionException;

import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.layers.ArcGISSceneLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISScene;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.Camera;
import com.esri.arcgisruntime.mapping.view.DefaultSceneViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.IdentifyLayerResult;
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

    // create a scene and add a basemap to it
    ArcGISScene scene = new ArcGISScene();
    scene.setBasemap(Basemap.createImagery());

    mSceneView = findViewById(R.id.sceneView);
    mSceneView.setScene(scene);

    Portal portal = new Portal("http://www.arcgis.com/");

    //Portal portal = new Portal("http://runtimecoretest.maps.arcgis.com");
    //portal.setCredential(new UserCredential("android_2", "android@100"));

    portal.loadAsync();
    portal.addDoneLoadingListener(() -> {
      if (portal.getLoadStatus() != LoadStatus.LOADED) {
        Log.e(TAG, "Portal failed to load: " + portal.getLoadError());
        return;
      }
      PortalItem portalItem = new PortalItem(portal, "2342ab7928834076a1240fb93c60e978");
      //PortalItem portalItem = new PortalItem(portal, "daeb3918364f452291a17c510630e385");
      portalItem.loadAsync();
      portalItem.addDoneLoadingListener(() -> {
        if (portalItem.getLoadStatus() != LoadStatus.LOADED) {
          Log.e(TAG, "Portal item failed to load: " + portalItem.getLoadError());
          return;
        }

        Log.d("portalItem", portalItem.getLoadStatus().toString());
        Log.d("portalItem", portalItem.getType().toString());
        Log.d("portalItem", portalItem.getAccess().toString());

        // add a scene service to the scene for viewing buildings
        //ArcGISSceneLayer sceneLayer = new ArcGISSceneLayer(getResources().getString(R.string.brest_buildings));
        ArcGISSceneLayer sceneLayer = new ArcGISSceneLayer(getString(R.string.buildings_layer));
        scene.getOperationalLayers().add(sceneLayer);

        // add a camera and initial camera position
        //Camera camera = new Camera(48.378, -4.494, 200, 345, 65, 0);
        //mSceneView.setViewpointCamera(camera);

        Camera camera = new Camera(42.36, -71.05, 6.68, 2.02, 88.44, 0.0);
        //Camera camera = new Camera(42.37, -71.12, 30.53, 14.37, 86.47, 0.0);
        mSceneView.setViewpointCamera(camera);

        mSceneView.setOnTouchListener(new DefaultSceneViewOnTouchListener(mSceneView) {

          @Override public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
            Point screenPoint = new Point(Math.round(motionEvent.getX()), Math.round(motionEvent.getY()));
            ListenableFuture<IdentifyLayerResult> identifyLayerResultFuture = mSceneView
                .identifyLayerAsync(sceneLayer, screenPoint, 100, false);
            identifyLayerResultFuture.addDoneListener(() -> {
              try {
                IdentifyLayerResult result = identifyLayerResultFuture.get();
                if (result.getElements().isEmpty() && result.getSublayerResults().isEmpty() && result.getPopups()
                    .isEmpty()) {
                  Toast.makeText(MainActivity.this, "No results identified.", Toast.LENGTH_SHORT).show();
                } else {
                  int elements = result.getElements().size();
                  int sublayers = result.getSublayerResults().size();
                  int popups = result.getPopups().size();
                  Toast.makeText(MainActivity.this,
                      "Identify results. Elements: " + elements + " Sublayers: " + sublayers + " Popups: " + popups, Toast.LENGTH_LONG).show();
                }
              } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
              }
            });
            return super.onSingleTapConfirmed(motionEvent);
          }
        });
      });
    });
  }

  @Override
  protected void onPause() {
    super.onPause();
    mSceneView.pause();
  }

  @Override
  protected void onResume() {
    super.onResume();
    mSceneView.resume();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mSceneView.dispose();
  }
}
