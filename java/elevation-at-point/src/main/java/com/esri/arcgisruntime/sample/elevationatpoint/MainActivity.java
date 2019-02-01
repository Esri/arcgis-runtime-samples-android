package com.esri.arcgisruntime.sample.elevationatpoint;

import java.util.concurrent.ExecutionException;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.mapping.ArcGISScene;
import com.esri.arcgisruntime.mapping.ArcGISTiledElevationSource;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.Camera;
import com.esri.arcgisruntime.mapping.view.DefaultSceneViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.SceneView;
import com.esri.arcgisruntime.symbology.SceneSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSceneSymbol;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private SceneView mSceneView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // create SceneView from layout
    mSceneView = findViewById(R.id.sceneView);

    // create a scene and add a basemap to it
    ArcGISScene scene = new ArcGISScene();
    scene.setBasemap(Basemap.createImagery());
    mSceneView.setScene(scene);

    // create an elevation source, and add this to the base surface of the scene
    ArcGISTiledElevationSource elevationSource = new ArcGISTiledElevationSource(getString(R.string.elevation_image_service));
    scene.getBaseSurface().getElevationSources().add(elevationSource);

    // create a point symbol to mark where elevation is being measured
    SimpleMarkerSceneSymbol sphereSymbol = new SimpleMarkerSceneSymbol(SimpleMarkerSceneSymbol.Style.SPHERE,
        Color.RED, 200, 200, 200, SceneSymbol.AnchorPosition.CENTER);

    // create a graphics overlay
    GraphicsOverlay graphicsOverlay = new GraphicsOverlay(GraphicsOverlay.RenderingMode.DYNAMIC);
    mSceneView.getGraphicsOverlays().add(graphicsOverlay);

    // add a camera and initial camera position
    Camera camera = new Camera(28.42, 83.9, 10000.0, 10.0, 80.0, 0.0);
    mSceneView.setViewpointCamera(camera);

    // create a touch listener to handle taps
    mSceneView.setOnTouchListener(new DefaultSceneViewOnTouchListener(mSceneView) {
      @Override public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
        // clear any existing graphics from the graphics overlay
        graphicsOverlay.getGraphics().clear();
        // get the tapped screen point
        android.graphics.Point screenPoint = new android.graphics.Point(Math.round(motionEvent.getX()),
            Math.round(motionEvent.getY()));
        // convert the screen point to a point on the surface
        Point surfacePoint = mSceneView.screenToBaseSurface(screenPoint);
        // create a new graphic at the surface point and add it to the graphics overlay
        Graphic surfacePointGraphic = new Graphic(surfacePoint, sphereSymbol);
        graphicsOverlay.getGraphics().add(surfacePointGraphic);

        // get the surface elevation at the surface point
        ListenableFuture<Double> elevationFuture = scene.getBaseSurface().getElevationAsync(surfacePoint);
        elevationFuture.addDoneListener(() -> {
          try {
            Double elevation = elevationFuture.get();
            String elevationMessage = "Elevation at tapped point: " + Math.round(elevation) + 'm';
            Toast.makeText(MainActivity.this, elevationMessage, Toast.LENGTH_LONG).show();
            Log.i(TAG, elevationMessage);
          } catch (ExecutionException | InterruptedException e) {
            String error = "Error getting elevation: " + e.getMessage();
            Toast.makeText(MainActivity.this, error, Toast.LENGTH_LONG).show();
            Log.e(TAG, error);
          }
        });
        return super.onSingleTapConfirmed(motionEvent);;
      }
    });

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
