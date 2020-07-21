/* Copyright 2016 ESRI
 *
 * All rights reserved under the copyright laws of the United States
 * and applicable international laws, treaties, and conventions.
 *
 * You may freely redistribute and use this sample code, with or
 * without modification, provided you include the original copyright
 * notice and use restrictions.
 *
 * See the Sample code usage restrictions document for further information.
 *
 */

package com.esri.arcgisruntime.sample.featurelayerselection;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.GeoElement;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.IdentifyLayerResult;
import com.esri.arcgisruntime.mapping.view.MapView;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private MapView mMapView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // get reference to map view
    mMapView = findViewById(R.id.mapView);
    mMapView.getSelectionProperties().setColor(Color.RED);

    // create a map with the streets basemap
    final ArcGISMap map = new ArcGISMap(Basemap.createStreets());

    // set an initial viewpoint
    map.setInitialViewpoint(new Viewpoint(new Envelope(-1131596.019761, 3893114.069099, 3926705.982140, 7977912.461790,
        SpatialReferences.getWebMercator())));

    // set the map to be displayed in the MapView
    mMapView.setMap(map);

    // create service feature table and a feature layer from it
    final ServiceFeatureTable serviceFeatureTable = new ServiceFeatureTable(getString(R.string.gdp_per_capita_url));
    final FeatureLayer featureLayer = new FeatureLayer(serviceFeatureTable);

    // add the layer to the map
    map.getOperationalLayers().add(featureLayer);

    // set an on touch listener to listen for click events
    mMapView.setOnTouchListener(new DefaultMapViewOnTouchListener(this, mMapView) {
      @Override
      public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
        featureLayer.clearSelection();
        Point screenPoint = new Point(Math.round(motionEvent.getX()), Math.round(motionEvent.getY()));
        int tolerance = 10;

        ListenableFuture<IdentifyLayerResult> identifyLayerResultFuture = mMapView.identifyLayerAsync(
            featureLayer,
            screenPoint,
            tolerance,
            false,
            -1
        );

        identifyLayerResultFuture.addDoneListener(() -> {
          try {
            IdentifyLayerResult identifyLayerResult = identifyLayerResultFuture.get();
            int counter = 0;
            for (GeoElement element : identifyLayerResult.getElements()) {
              if (element instanceof Feature) {
                Feature feature = (Feature) element;
                featureLayer.selectFeature(feature);
                counter++;
                Log.d(TAG, "Selection #: " + counter + " Table name: " + feature.getFeatureTable().getTableName());
              }
            }
            Toast.makeText(getApplicationContext(), counter + " features selected", Toast.LENGTH_SHORT).show();
          } catch (Exception e) {
            Log.e(TAG, "Select feature failed: " + e.getMessage());
          }
        });

        return super.onSingleTapConfirmed(motionEvent);
      }
    });
  }

  @Override
  protected void onPause() {
    mMapView.pause();
    super.onPause();
  }

  @Override
  protected void onResume() {
    super.onResume();
    mMapView.resume();
  }

  @Override
  protected void onDestroy() {
    mMapView.dispose();
    super.onDestroy();
  }
}
