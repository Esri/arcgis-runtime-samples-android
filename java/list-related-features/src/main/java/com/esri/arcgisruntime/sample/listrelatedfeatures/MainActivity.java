/* Copyright 2017 Esri
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

package com.esri.arcgisruntime.sample.listrelatedfeatures;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.ArcGISFeature;
import com.esri.arcgisruntime.data.ArcGISFeatureTable;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.RelatedFeatureQueryResult;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.layers.Layer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.GeoElement;
import com.esri.arcgisruntime.mapping.LayerList;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.IdentifyLayerResult;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private final ArrayList<FeatureLayer> mOperationalLayers = new ArrayList<>();
  private final List<String> mRelatedValues = new LinkedList<>();
  private MapView mMapView;
  private ArcGISMap mArcGISMap;
  private BottomSheetBehavior mBottomSheetBehavior = null;
  private ArrayAdapter<String> mArrayAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // The View with the BottomSheetBehavior
    mBottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottom_sheet));
    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

    // get bottomsheet collapsed height in dp
    DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
    final float dp =
        mBottomSheetBehavior.getPeekHeight() / ((float) displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);

    ListView tableList = findViewById(R.id.related_list);
    mArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mRelatedValues);
    tableList.setAdapter(mArrayAdapter);

    // get a reference to the map view
    mMapView = findViewById(R.id.mapView);
    mMapView.getSelectionProperties().setColor(Color.YELLOW);
    // create a mArcGISMap a webmap
    mArcGISMap = new ArcGISMap(getResources().getString(R.string.webmap_url));
    // set the mArcGISMap to be displayed in this view
    mMapView.setMap(mArcGISMap);
    mArcGISMap.addDoneLoadingListener(new Runnable() {
      @Override
      public void run() {
        if (mArcGISMap.getLoadStatus() == LoadStatus.LOADED) {
          // create Features to use for listing related features
          createFeatures(mArcGISMap);
        }
      }
    });

    mMapView.setOnTouchListener(new DefaultMapViewOnTouchListener(this, mMapView) {
      @Override
      public boolean onSingleTapConfirmed(MotionEvent e) {
        // clear ListAdapter of previous results
        mArrayAdapter.clear();
        // hide the bottomsheet
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        // get the point that was clicked and convert it to a point in mArcGISMap coordinates
        Point screenPoint = new Point(Math.round(e.getX()), Math.round(e.getY()));
        // get the FeatureLayer to query
        final FeatureLayer selectedLayer = mOperationalLayers.get(0);
        // get a list of related features to display
        queryRelatedFeatures(selectedLayer, screenPoint);
        return true;
      }
    });

    // respond to bottom sheet interaction
    mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
      @Override
      public void onStateChanged(@NonNull View bottomSheet, int newState) {
        if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
          // set attribution bar above bottom sheet when collapsed
          mMapView.setViewInsets(0, 0, 0, dp);
        } else {
          // set attribution bar to bottom when bottom sheet hidden or sliding
          mMapView.setViewInsets(0, 0, 0, 0);
        }
      }

      @Override
      public void onSlide(@NonNull View bottomSheet, float slideOffset) {
        // bottom sheet sliding up or down
      }
    });
  }

  /**
   * Uses the selected FeatureLayer to get FeatureTable RelationshipInfos used to
   * QueryRelatedFeaturesAsync which returns a list of related features.
   *
   * @param featureLayer Layer selected from the Map
   * @param clickPoint   Tapped location to query
   */
  private void queryRelatedFeatures(final FeatureLayer featureLayer, Point clickPoint) {

    int tolerance = 10;
    final ListenableFuture<IdentifyLayerResult> identifyLayerResultFuture = mMapView
        .identifyLayerAsync(featureLayer, clickPoint, tolerance, false, 1);
    // clear previously selected layers
    featureLayer.clearSelection();

    identifyLayerResultFuture.addDoneListener(() -> {
      try {
        IdentifyLayerResult identifyLayerResult = identifyLayerResultFuture.get();
        for (GeoElement element : identifyLayerResult.getElements()) {
          Feature feature = (Feature) element;
          ArcGISFeature arcGISFeature = (ArcGISFeature) feature;
          ArcGISFeatureTable selectedTable = (ArcGISFeatureTable) feature.getFeatureTable();
          final ListenableFuture<List<RelatedFeatureQueryResult>> relatedFeatureQueryResultFuture = selectedTable
              .queryRelatedFeaturesAsync(arcGISFeature);
          relatedFeatureQueryResultFuture.addDoneListener(() -> {
            try {
              List<RelatedFeatureQueryResult> relatedFeatureQueryResultList = relatedFeatureQueryResultFuture.get();
              // iterate over returned RelatedFeatureQueryResults
              for (RelatedFeatureQueryResult relatedQueryResult : relatedFeatureQueryResultList) {
                // add Table Name to List
                String relatedTableName = relatedQueryResult.getRelatedTable().getTableName();
                mRelatedValues.add(relatedTableName);
                // iterate over Features returned
                for (Feature relatedFeature : relatedQueryResult) {
                  // get the Display field to use as filter on related attributes
                  ArcGISFeature agsFeature = (ArcGISFeature) relatedFeature;
                  String displayFieldName = agsFeature.getFeatureTable().getLayerInfo().getDisplayFieldName();
                  String displayFieldValue = agsFeature.getAttributes().get(displayFieldName).toString();
                  mRelatedValues.add(displayFieldValue);
                  // notify ListAdapter content has changed
                  mArrayAdapter.notifyDataSetChanged();
                }
              }
            } catch (InterruptedException | ExecutionException e) {
              String error = "Error getting related feature query result: " + e.getMessage();
              Toast.makeText(this, error, Toast.LENGTH_LONG).show();
              Log.e(TAG, error);
            }
          });
        }
      } catch (InterruptedException | ExecutionException e) {
        String error = "Error getting related feature query result: " + e.getMessage();
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        Log.e(TAG, error);
      }
      // show the bottomsheet with results
      mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    });
  }

  /**
   * Create Features from Layers in the Map
   *
   * @param map ArcGISMap to get Layers and Tables
   */
  private void createFeatures(ArcGISMap map) {
    LayerList layers = map.getOperationalLayers();
    // add the National Parks Feature layer to LayerList
    for (Layer layer : layers) {
      FeatureLayer fLayer = (FeatureLayer) layer;
      if (fLayer.getName().contains("Alaska National Parks")) {
        mOperationalLayers.add(fLayer);
      }
    }
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
