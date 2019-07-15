/*
 *  Copyright 2019 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.esri.arcgisruntime.sample.annotationsublayer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.arcgisruntime.layers.AnnotationLayer;
import com.esri.arcgisruntime.layers.AnnotationSublayer;
import com.esri.arcgisruntime.layers.Layer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.MobileMapPackage;
import com.esri.arcgisruntime.mapping.view.MapView;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private MapView mMapView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // get a reference to the map view
    mMapView = findViewById(R.id.mapView);
    // create a map with the BasemapType topographic
    ArcGISMap map = new ArcGISMap(Basemap.Type.TOPOGRAPHIC, 34.056295, -117.195800, 16);
    // set the map to be displayed in this view
    mMapView.setMap(map);
    // show current map scale
    TextView currentMapScaleTextView = findViewById(R.id.mapScale);
    mMapView.addMapScaleChangedListener(mapScaleChangedEvent -> currentMapScaleTextView
        .setText(getString(R.string.map_scale, Math.round(mMapView.getMapScale()))));

    requestReadPermission();

  }

  private void addSublayersWithAnnotation() {

    MobileMapPackage mobileMapPackage = new MobileMapPackage(
        Environment.getExternalStorageDirectory() + "/ArcGIS/Samples/MapPackage/LothianRiversAnno.mmpk");
    mobileMapPackage.loadAsync();
    mobileMapPackage.addDoneLoadingListener(() -> {
      if (mobileMapPackage.getLoadStatus() == LoadStatus.LOADED) {
        // set the mobile map package's map to the map view
        mMapView.setMap(mobileMapPackage.getMaps().get(0));
        // find the annotation layer within the map
        for (Layer layer : mMapView.getMap().getOperationalLayers()) {
          if (layer instanceof AnnotationLayer) {
            // load the annotation layer. The layer must be loaded in order to access sub-layer contents
            layer.loadAsync();
            layer.addDoneLoadingListener(() -> {
              // bind water metadata to views
              bindSublayerMetadataToViews((AnnotationSublayer) layer.getSubLayerContents().get(0),
                  findViewById(R.id.waterMetadata));
              // bind burn metadata to views
              bindSublayerMetadataToViews((AnnotationSublayer) layer.getSubLayerContents().get(1),
                  findViewById(R.id.burnMetadata));
            });
          }
        }
      } else {
        Toast.makeText(this, "MMPK didn't load: " + mobileMapPackage.getLoadError().getMessage(), Toast.LENGTH_LONG)
            .show();
      }
    });
  }

  private void bindSublayerMetadataToViews(AnnotationSublayer annotationSublayer, View view) {
    // update the layer title
    TextView layerNameTextView = view.findViewById(R.id.sublayerName);
    layerNameTextView.setText((getString(R.string.sublayer_name, annotationSublayer.getName())));
    // update the min scale
    TextView minScaleTextView = view.findViewById(R.id.minScale);
    minScaleTextView.setText(getString(R.string.min_scale, Math.round(annotationSublayer.getMinScale())));
    // update the max scale
    TextView maxScaleTextView = view.findViewById(R.id.maxScale);
    maxScaleTextView.setText(getString(R.string.max_scale, Math.round(annotationSublayer.getMaxScale())));
    Log.d(TAG, annotationSublayer.getName() + annotationSublayer.getMinScale() + annotationSublayer.getMaxScale());
    // update the is visible boolean on map scale changes
    TextView isVisibleTextView = view.findViewById(R.id.isVisible);
    isVisibleTextView.setText(getString(R.string.is_visible, annotationSublayer.isVisibleAtScale(mMapView.getMapScale())));
    mMapView.addMapScaleChangedListener(mapScaleChangedEvent -> isVisibleTextView
        .setText(getString(R.string.is_visible, annotationSublayer.isVisibleAtScale(mMapView.getMapScale()))));
  }

  /**
   * Request read external storage for API level 23+.
   */
  private void requestReadPermission() {
    // define permission to request
    String[] reqPermission = { Manifest.permission.READ_EXTERNAL_STORAGE };
    int requestCode = 2;
    if (ContextCompat.checkSelfPermission(this, reqPermission[0]) == PackageManager.PERMISSION_GRANTED) {
      // do something
      addSublayersWithAnnotation();
    } else {
      // request permission
      ActivityCompat.requestPermissions(this, reqPermission, requestCode);
    }
  }

  /**
   * Handle the permissions request response.
   */
  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      // do something
      addSublayersWithAnnotation();
    } else {
      // report to user that permission was denied
      Toast.makeText(this, "TODO FAIL", Toast.LENGTH_SHORT).show();
    }
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
  }

  @Override
  protected void onPause() {
    super.onPause();
    mMapView.pause();
  }

  @Override
  protected void onResume() {
    super.onResume();
    mMapView.resume();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mMapView.dispose();
  }

}
