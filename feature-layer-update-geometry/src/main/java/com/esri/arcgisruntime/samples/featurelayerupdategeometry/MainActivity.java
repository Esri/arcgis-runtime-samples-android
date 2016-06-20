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
package com.esri.arcgisruntime.samples.featurelayerupdategeometry;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.datasource.arcgis.ArcGISFeature;
import com.esri.arcgisruntime.datasource.arcgis.FeatureEditResult;
import com.esri.arcgisruntime.datasource.arcgis.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.GeoElement;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.IdentifyLayerResult;
import com.esri.arcgisruntime.mapping.view.MapView;

import java.util.List;


public class MainActivity extends AppCompatActivity {
  MapView mMapView;
  FeatureLayer mFeatureLayer;
  boolean mFeatureSelected = false;
  ArcGISFeature mIdentifiedFeature;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    // inflate MapView from layout
    mMapView = (MapView) findViewById(R.id.mapView);
    // create a map with the streets basemap
    ArcGISMap map = new ArcGISMap(Basemap.createStreets());
    //set an initial viewpoint
    map.setInitialViewpoint(new Viewpoint(new Envelope(-1131596.019761, 3893114.069099, 3926705.982140, 7977912.461790, SpatialReferences.getWebMercator())));
    // set the map to be displayed in the mapview
    mMapView.setMap(map);

    // create feature layer with its service feature table
    // create the service feature table
    final ServiceFeatureTable serviceFeatureTable = new ServiceFeatureTable(getResources().getString(R.string.sample_service_url));
    // create the feature layer using the service feature table
    mFeatureLayer = new FeatureLayer(serviceFeatureTable);
    mFeatureLayer.setSelectionColor(Color.CYAN); 
    mFeatureLayer.setSelectionWidth(3);
    // add the layer to the map
    map.getOperationalLayers().add(mFeatureLayer);
    Toast.makeText(getApplicationContext(), "Tap on a feature to select it", Toast.LENGTH_LONG).show();

    // set an on touch listener to listen for click events
    mMapView.setOnTouchListener(new DefaultMapViewOnTouchListener(this, mMapView) {
      @Override
      public boolean onSingleTapConfirmed(MotionEvent e) {

        if (!mFeatureSelected) {
          android.graphics.Point screenCoordinate = new android.graphics.Point(Math.round(e.getX()), Math.round(e.getY()));
          int tolerance = 20;
          //Identify Layers to find features
          final ListenableFuture<List<IdentifyLayerResult>> identifyFuture = mMapView.identifyLayersAsync(screenCoordinate, tolerance, 1);
          identifyFuture.addDoneListener(new Runnable() {
            @Override
            public void run() {
              try {
                List<IdentifyLayerResult> identifyLayerResultsList = identifyFuture.get();
                if(identifyLayerResultsList.size() > 0){
                  List<GeoElement> identifiedFeaturesList = identifyLayerResultsList.get(0).getIdentifiedElements();
                  mIdentifiedFeature = (ArcGISFeature) identifiedFeaturesList.get(0);
                  //Select the identified feature
                  mFeatureLayer.selectFeature(mIdentifiedFeature);
                  mFeatureSelected = true;
                  Toast.makeText(getApplicationContext(), "Feature Selected. Tap on map to update its geometry " , Toast.LENGTH_LONG).show();
                }else{
                  Toast.makeText(getApplicationContext(), "No Features Selected. Tap on a feature" , Toast.LENGTH_LONG).show();
                }
              } catch (Exception e) {
                Log.e(getResources().getString(R.string.app_name), "No Features Selected. Tap on a feature: " + e.getMessage());
              }
            }
          });
        } else {
          final Point movedPoint = mMapView.screenToLocation(new android.graphics.Point(Math.round(e.getX()), Math.round(e.getY())));
          mIdentifiedFeature.addDoneLoadingListener(new Runnable() {
            @Override
            public void run() {
              mIdentifiedFeature.setGeometry(movedPoint);
              final ListenableFuture<Void> updateFuture = mFeatureLayer.getFeatureTable().updateFeatureAsync(mIdentifiedFeature);
              updateFuture.addDoneListener(new Runnable() {
                @Override
                public void run() {
                  try {
                    if (updateFuture.get() != null) {
                      applyEditsToServer();
                      mFeatureLayer.clearSelection();
                      mFeatureSelected = false;
                    } else {
                      Log.e(getResources().getString(R.string.app_name), "Update feature failed");
                    }
                  } catch (Exception e) {
                    Log.e(getResources().getString(R.string.app_name), "Update feature failed: " + e.getMessage());
                  }
                }
              });
            }
          });
          mIdentifiedFeature.loadAsync();
        }
        return super.onSingleTapConfirmed(e);
      }
    });
  }

  /**
   * Applies edits to the FeatureService
   */
  private void applyEditsToServer() {
    final ListenableFuture<List<FeatureEditResult>> applyEditsFuture = ((ServiceFeatureTable) mFeatureLayer.getFeatureTable()).applyEditsAsync();
    applyEditsFuture.addDoneListener(new Runnable() {
      @Override
      public void run() {
        try {
          List<FeatureEditResult> featureEditResultsList = applyEditsFuture.get();
          if (featureEditResultsList.get(0).getError() != null) {
            Toast.makeText(getApplicationContext(), "Applied Geometry Edits to Server. ObjectID: " + featureEditResultsList.get(0).getObjectId(), Toast.LENGTH_SHORT).show();
          }
        } catch (Exception e) {
          Log.e(getResources().getString(R.string.app_name), "Failed to Apply Edits" + e.getMessage());
        }
      }
    });
  }

  @Override
  protected void onPause() {
    super.onPause();
    // pause MapView
    mMapView.pause();
  }

  @Override
  protected void onResume() {
    super.onResume();
    // resume MapView
    mMapView.resume();
  }
}
