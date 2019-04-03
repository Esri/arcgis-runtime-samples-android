/*
 * Copyright 2019 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.esri.arcgisruntime.sample.browsewfslayers;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.FeatureQueryResult;
import com.esri.arcgisruntime.data.FeatureTable;
import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.GeometryType;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.ogc.wfs.WfsFeatureTable;
import com.esri.arcgisruntime.ogc.wfs.WfsLayerInfo;
import com.esri.arcgisruntime.ogc.wfs.WfsService;
import com.esri.arcgisruntime.symbology.Renderer;
import com.esri.arcgisruntime.symbology.SimpleFillSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.esri.arcgisruntime.symbology.SimpleRenderer;

public class MainActivity extends AppCompatActivity implements OnItemSelectedListener {

  private static final String TAG = MainActivity.class.getSimpleName();

  private MapView mMapView;
  private RecyclerView mLayersRecyclerView;
  private View mLoadingView;
  private BottomSheetBehavior<View> mBottomSheetBehavior;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mMapView = findViewById(R.id.mapView);
    mLayersRecyclerView = findViewById(R.id.layersRecyclerView);
    mLoadingView = findViewById(R.id.loadingView);

    View bottomSheet = findViewById(R.id.bottomSheet);
    mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

    mMapView.setOnTouchListener(new DefaultMapViewOnTouchListener(this, mMapView) {
      @Override public boolean onTouch(View view, MotionEvent motionEvent) {
        if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
          mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
          return true;
        }
        return super.onTouch(view, motionEvent);
      }
    });

    ArcGISMap map = new ArcGISMap(Basemap.createImagery());
    mMapView.setMap(map);

    // create WFS service
    WfsService service = new WfsService(getString(R.string.wfs_service_url));
    service.addDoneLoadingListener(() -> {
      if (service.getLoadStatus() == LoadStatus.FAILED_TO_LOAD) {
        logErrorToUser(getString(R.string.error_wfs_service_load_failure, service.getLoadError().getMessage()));
      } else {
        setupRecyclerView(service.getServiceInfo().getLayerInfos());
      }
    });
    service.loadAsync();
  }

  @Override public void onItemSelected(WfsLayerInfo layer) {
    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    mLoadingView.setVisibility(View.VISIBLE);
    updateMap(layer);
  }

  private void updateMap(WfsLayerInfo layer) {
    // clear existing layers
    mMapView.getMap().getOperationalLayers().clear();

    // create feature table
    WfsFeatureTable featureTable = new WfsFeatureTable(layer);

    // set the table's feature request mode
    featureTable.setFeatureRequestMode(ServiceFeatureTable.FeatureRequestMode.MANUAL_CACHE);

    ListenableFuture<FeatureQueryResult> featureQueryResultFuture = featureTable
        .populateFromServiceAsync(new QueryParameters(), false, null);

    new Thread(() -> {
      try {
        FeatureQueryResult featureQueryResult = featureQueryResultFuture.get();

        // create a layer from the table
        FeatureLayer featureLayer = new FeatureLayer(featureTable);

        // set a renderer for the table
        featureLayer.setRenderer(getRandomRendererForTable(featureTable));

        runOnUiThread(() -> {
          // add the layer to the map
          mMapView.getMap().getOperationalLayers().add(featureLayer);

          // zoom to the extent of the layer
          mMapView.setViewpointGeometryAsync(layer.getExtent(), 50);
        });
      } catch (InterruptedException | ExecutionException e) {
        runOnUiThread(
            () -> logErrorToUser(
                getString(R.string.error_feature_table_populate_from_service_failure, e.getMessage())));
      } finally {
        runOnUiThread(() -> mLoadingView.setVisibility(View.GONE));
      }
    }).start();
  }

  private Renderer getRandomRendererForTable(FeatureTable table) {
    Random random = new Random();

    if (table.getGeometryType() == GeometryType.POINT || table.getGeometryType() == GeometryType.MULTIPOINT) {
      return new SimpleRenderer(
          new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, getRandomColor(random, 255), 2));
    } else if (table.getGeometryType() == GeometryType.POLYGON || table.getGeometryType() == GeometryType.ENVELOPE) {
      return new SimpleRenderer(new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, getRandomColor(random, 255), null));
    } else {
      return new SimpleRenderer(new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, getRandomColor(random, 255), 1));
    }
  }

  private int getRandomColor(Random random, int alpha) {
    return Color.argb(alpha, random.nextInt(256), random.nextInt(256), random.nextInt(256));
  }

  private void setupRecyclerView(List<WfsLayerInfo> layers) {
    LayersAdapter layersAdapter = new LayersAdapter(this);
    mLayersRecyclerView.setAdapter(layersAdapter);

    for (WfsLayerInfo layer : layers) {
      // if layer can be shown in legend
      layersAdapter.addLayer(layer);
    }
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_main_activity, menu);
    return true;
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.action_show_layer_list) {
      // if bottom sheet is not shown
      if (mBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
        // show bottom sheet
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
      } else {
        // hide bottom sheet
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
      }
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private void logErrorToUser(String error) {
    Toast.makeText(this, error, Toast.LENGTH_LONG).show();
    Log.e(TAG, error);
  }

  @Override
  protected void onResume() {
    super.onResume();
    mMapView.resume();
  }

  @Override
  protected void onPause() {
    mMapView.pause();
    super.onPause();
  }

  @Override
  protected void onDestroy() {
    mMapView.dispose();
    super.onDestroy();
  }
}
