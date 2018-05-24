package com.esri.arcgisruntime.scenelayeridentify;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureQueryResult;
import com.esri.arcgisruntime.data.Field;
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
import com.esri.arcgisruntime.security.UserCredential;

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

    //Portal portal = new Portal("http://arcgisruntime.maps.arcgis.com/");

    Portal portal = new Portal("http://runtimecoretest.maps.arcgis.com");
    portal.setCredential(new UserCredential("android_2", "android@100"));

    portal.loadAsync();
    portal.addDoneLoadingListener(() -> {
      if (portal.getLoadStatus() != LoadStatus.LOADED) {
        Log.e(TAG, "Portal failed to load: " + portal.getLoadError());
        return;
      }
      PortalItem portalItem = new PortalItem(portal, "daeb3918364f452291a17c510630e385");
      portalItem.loadAsync();
      portalItem.addDoneLoadingListener(() -> {
        if (portalItem.getLoadStatus() != LoadStatus.LOADED) {
          Log.e(TAG, "Portal item failed to load: " + portalItem.getLoadError());
          return;
        }

        Log.d("portalItem", portalItem.getLoadStatus().toString());
        Log.d("portalItem", portalItem.getType().toString());
        Log.d("portalItem", portalItem.getAccess().toString());

        ArcGISSceneLayer sceneLayer = new ArcGISSceneLayer(portalItem);
        scene.getOperationalLayers().add(sceneLayer);
        Log.d(TAG, "identify enabled: " + sceneLayer.isIdentifyEnabled());

        Camera camera = new Camera(34.05, -117.2, 6.68, 2.02, 88.44, 0.0);
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
                  Feature identifiedFeature = (Feature) result.getElements().get(0);
                  Toast.makeText(MainActivity.this,
                      "Identify results. Elements: " + elements + " Sublayers: " + sublayers + " Popups: " + popups,
                      Toast.LENGTH_LONG).show();
                  ListenableFuture<FeatureQueryResult> featureQueryResultFuture = sceneLayer.getSelectedFeaturesAsync();
                  featureQueryResultFuture.addDoneListener(() -> {
                    try {
                      FeatureQueryResult featureQueryResult = featureQueryResultFuture.get();
                      Iterator<Feature> featureQueryResultIterator = featureQueryResult.iterator();
                      List<Feature> selectedFeatures = new ArrayList<>();
                      for (Field field: featureQueryResult.getFields()) {
                        Log.d(TAG, "field name: " + field.getName());
                      }


                      while(featureQueryResultIterator.hasNext()) {
                        selectedFeatures.add(featureQueryResultIterator.next());
                      }

                      for (Feature feature : selectedFeatures) {
                        Iterator iterator = feature.getAttributes().entrySet().iterator();
                        while (iterator.hasNext()) {
                          Map.Entry pair = (Map.Entry) iterator.next();
                          Log.d(TAG, pair.getKey() + " = " + pair.getValue());
                        }
                      }
                      if (selectedFeatures.contains(identifiedFeature)) {
                        sceneLayer.unselectFeature(identifiedFeature);
                        Log.d(TAG, "unselect " + identifiedFeature.getAttributes().get("name"));
                      } else {
                        sceneLayer.selectFeature(identifiedFeature);
                      }
                    } catch (InterruptedException e) {
                      e.printStackTrace();
                    } catch (ExecutionException e) {
                      e.printStackTrace();
                    }
                  });
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
