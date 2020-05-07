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

package com.esri.arcgisruntime.sample.rasterrenderingrule;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import com.esri.arcgisruntime.arcgisservices.RenderingRuleInfo;
import com.esri.arcgisruntime.layers.RasterLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.raster.ImageServiceRaster;
import com.esri.arcgisruntime.raster.RenderingRule;

public class MainActivity extends AppCompatActivity {

  private MapView mMapView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // create MapView from layout
    mMapView = findViewById(R.id.mapView);

    // create a Streets BaseMap
    ArcGISMap map = new ArcGISMap(Basemap.createStreets());
    // set the map to be displayed in this view
    mMapView.setMap(map);

    // create image service raster as raster layer and add to map
    final ImageServiceRaster imageServiceRaster = new ImageServiceRaster(
        getResources().getString(R.string.image_service_url));
    final RasterLayer imageRasterLayer = new RasterLayer(imageServiceRaster);
    map.getOperationalLayers().add(imageRasterLayer);

    Spinner spinner = findViewById(R.id.spinner);
    final List<String> renderRulesList = new ArrayList<>();
    final ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
        renderRulesList);
    spinner.setAdapter(spinnerAdapter);
    // zoom to the extent of the raster service
    imageRasterLayer.addDoneLoadingListener(new Runnable() {
      @Override
      public void run() {
        if (imageRasterLayer.getLoadStatus() == LoadStatus.LOADED) {
          // zoom to extent of raster
          mMapView.setViewpointGeometryAsync(imageServiceRaster.getServiceInfo().getFullExtent());
          // get the predefined rendering rules and add to spinner
          List<RenderingRuleInfo> renderingRuleInfos = imageServiceRaster.getServiceInfo().getRenderingRuleInfos();
          for (RenderingRuleInfo renderRuleInfo : renderingRuleInfos) {
            String renderingRuleName = renderRuleInfo.getName();
            renderRulesList.add(renderingRuleName);
            // update array adapter with list update
            spinnerAdapter.notifyDataSetChanged();
          }
        }
      }
    });

    // listen to the spinner
    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        applyRenderingRule(imageServiceRaster, position);
      }

      @Override
      public void onNothingSelected(AdapterView<?> adapterView) {
        Log.d("MainActivity", "Spinner nothing selected");
      }
    });

  }

  /**
   * Apply a rendering rule on a Raster and add it to the map
   *
   * @param imageServiceRaster image service raster to apply rendering on
   * @param index              spinner selected position representing the rule to apply
   */
  private void applyRenderingRule(ImageServiceRaster imageServiceRaster, int index) {
    // clear all rasters
    mMapView.getMap().getOperationalLayers().clear();
    // get the rendering rule info at the selected index
    RenderingRuleInfo renderRuleInfo = imageServiceRaster.getServiceInfo().getRenderingRuleInfos().get(index);
    // create a rendering rule object using the rendering rule info
    RenderingRule renderingRule = new RenderingRule(renderRuleInfo);
    // create a new image service raster
    ImageServiceRaster appliedImageServiceRaster = new ImageServiceRaster(
        getResources().getString(R.string.image_service_url));
    // apply the rendering rule
    appliedImageServiceRaster.setRenderingRule(renderingRule);
    // create a raster layer using the image service raster
    RasterLayer rasterLayer = new RasterLayer(appliedImageServiceRaster);
    // add the raster layer to the map
    mMapView.getMap().getOperationalLayers().add(rasterLayer);
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
