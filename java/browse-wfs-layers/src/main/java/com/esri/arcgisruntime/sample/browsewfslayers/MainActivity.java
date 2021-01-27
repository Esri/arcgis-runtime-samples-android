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

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureQueryResult;
import com.esri.arcgisruntime.data.FeatureTable;
import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.GeometryType;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.ogc.wfs.OgcAxisOrder;
import com.esri.arcgisruntime.ogc.wfs.WfsFeatureTable;
import com.esri.arcgisruntime.ogc.wfs.WfsLayerInfo;
import com.esri.arcgisruntime.ogc.wfs.WfsService;
import com.esri.arcgisruntime.symbology.Renderer;
import com.esri.arcgisruntime.symbology.SimpleFillSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.esri.arcgisruntime.symbology.SimpleRenderer;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

public class MainActivity extends AppCompatActivity implements OnItemSelectedListener {

  private static final String TAG = MainActivity.class.getSimpleName();

  private MapView mMapView;
  private CheckBox mAxisCheckbox;
  private RecyclerView mLayersRecyclerView;
  private View mLoadingView;
  private BottomSheetBehavior<View> mBottomSheetBehavior;

  private WfsLayerInfo mSelectedWfsLayerInfo;
  // objects that implement Loadable must be class fields to prevent being garbage collected before loading
  private WfsService mWfsService;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // authentication with an API key or named user is required to access basemaps and other
    // location services
    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY);

    mMapView = findViewById(R.id.mapView);
    mAxisCheckbox = findViewById(R.id.axisCheckbox);
    mLayersRecyclerView = findViewById(R.id.layersRecyclerView);
    mLoadingView = findViewById(R.id.loadingView);

    View bottomSheet = findViewById(R.id.bottomSheet);
    mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

    mMapView.setOnTouchListener(new DefaultMapViewOnTouchListener(this, mMapView) {
      @Override public boolean onTouch(View view, MotionEvent motionEvent) {
        if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
          mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
          return true;
        }
        return super.onTouch(view, motionEvent);
      }
    });

    mAxisCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
      if (mSelectedWfsLayerInfo != null) {
        updateMap(mSelectedWfsLayerInfo);
      }
    });

    ArcGISMap map = new ArcGISMap(BasemapStyle.ARCGIS_IMAGERY);
    mMapView.setMap(map);

    // create WFS service
    mWfsService = new WfsService(getString(R.string.wfs_service_url));
    mWfsService.addDoneLoadingListener(() -> {
      if (mWfsService.getLoadStatus() == LoadStatus.FAILED_TO_LOAD) {
        logErrorToUser(getString(R.string.error_wfs_service_load_failure, mWfsService.getLoadError().getMessage()));
      } else {
        setupRecyclerView(mWfsService.getServiceInfo().getLayerInfos());
      }
    });
    mWfsService.loadAsync();
  }

  @Override public void onItemSelected(WfsLayerInfo wfsLayerInfo) {
    mSelectedWfsLayerInfo = wfsLayerInfo;
    updateMap(wfsLayerInfo);
  }

  private void updateMap(WfsLayerInfo wfsLayerInfo) {
    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    mLoadingView.setVisibility(View.VISIBLE);

    // clear existing layer infos
    mMapView.getMap().getOperationalLayers().clear();

    // create feature table
    WfsFeatureTable featureTable = new WfsFeatureTable(wfsLayerInfo);

    // set the axis order dependant on whether the checkbox is ticked or not
    featureTable.setAxisOrder(mAxisCheckbox.isChecked() ? OgcAxisOrder.SWAP : OgcAxisOrder.NO_SWAP);

    // set the table's feature request mode
    featureTable.setFeatureRequestMode(ServiceFeatureTable.FeatureRequestMode.MANUAL_CACHE);

    // create a feature layer from the table
    FeatureLayer featureLayer = new FeatureLayer(featureTable);

    // set a renderer to the table once is loaded, since the renderer is chosen based on the table's geometry type
    featureTable.addDoneLoadingListener(() -> featureLayer.setRenderer(getRandomRendererForTable(featureTable)));

    // add the layer to the map
    mMapView.getMap().getOperationalLayers().add(featureLayer);

    // populate the table
    ListenableFuture<FeatureQueryResult> featureQueryResultFuture = featureTable
        .populateFromServiceAsync(new QueryParameters(), false, null);

    // run when the table has been populated
    featureQueryResultFuture.addDoneListener(() -> {
      // zoom to the extent of the layer
      mMapView.setViewpointGeometryAsync(featureLayer.getFullExtent(), 50);
      mLoadingView.setVisibility(View.GONE);
    });
  }

  /**
   * Create a {@link SimpleRenderer} to render the {@link Feature}s in the {@link FeatureLayer}
   *
   * @param table containing the data
   * @return a {@link SimpleRenderer} to render the features
   */
  private Renderer getRandomRendererForTable(FeatureTable table) {
    if (table.getGeometryType() == GeometryType.POINT || table.getGeometryType() == GeometryType.MULTIPOINT) {
      return new SimpleRenderer(
          new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, getRandomColor(), 2));
    } else if (table.getGeometryType() == GeometryType.POLYGON || table.getGeometryType() == GeometryType.ENVELOPE) {
      return new SimpleRenderer(new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, getRandomColor(), null));
    } else {
      return new SimpleRenderer(new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, getRandomColor(), 1));
    }
  }

  private int getRandomColor() {
    Random random = new Random();
    return Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));
  }

  /**
   * Setup {@link RecyclerView} with an adapter and add {@link WfsLayerInfo}s to the adapter
   *
   * @param wfsLayerInfos to display in adapter
   */
  private void setupRecyclerView(List<WfsLayerInfo> wfsLayerInfos) {
    WfsLayerInfoAdapter layersAdapter = new WfsLayerInfoAdapter(this);
    mLayersRecyclerView.setAdapter(layersAdapter);

    for (WfsLayerInfo wfsLayerInfo : wfsLayerInfos) {
      layersAdapter.addLayer(wfsLayerInfo);
    }
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_layer_list, menu);
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
