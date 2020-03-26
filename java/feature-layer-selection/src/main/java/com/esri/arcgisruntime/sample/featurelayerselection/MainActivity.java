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

import java.util.Iterator;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureQueryResult;
import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
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
        // get the point that was clicked and convert it to a point in map coordinates
        Point clickPoint = mMapView
            .screenToLocation(new android.graphics.Point(Math.round(motionEvent.getX()), Math.round(motionEvent.getY())));
        int tolerance = 10;
        double mapTolerance = tolerance * mMapView.getUnitsPerDensityIndependentPixel();
        // create objects required to do a selection with a query
        Envelope envelope = new Envelope(clickPoint.getX() - mapTolerance, clickPoint.getY() - mapTolerance,
            clickPoint.getX() + mapTolerance, clickPoint.getY() + mapTolerance, map.getSpatialReference());
        QueryParameters query = new QueryParameters();
        query.setGeometry(envelope);
        // call select features
        final ListenableFuture<FeatureQueryResult> featureQueryResultFuture = featureLayer
            .selectFeaturesAsync(query, FeatureLayer.SelectionMode.NEW);
        // add done loading listener to fire when the selection returns
        featureQueryResultFuture.addDoneListener(() -> {
          try {
            // call get on the future to get the result
            FeatureQueryResult featureQueryResult = featureQueryResultFuture.get();
            // create an Iterator
            Iterator<Feature> iterator = featureQueryResult.iterator();
            Feature feature;
            // cycle through selections
            int counter = 0;
            while (iterator.hasNext()) {
              feature = iterator.next();
              counter++;
              Log.d(TAG, "Selection #: " + counter + " Table name: " + feature.getFeatureTable().getTableName());
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
