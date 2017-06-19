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

package com.esri.arcgisruntime.sample.rasterservicelayer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.layers.RasterLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.raster.ImageServiceRaster;

public class MainActivity extends AppCompatActivity {

    private MapView mMapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // create MapView from layout
        mMapView = (MapView) findViewById(R.id.mapView);
        // create a Dark Gray Canvas Vector basemap
        final ArcGISMap map = new ArcGISMap(Basemap.createDarkGrayCanvasVector());
        // create image service raster as raster layer
        final ImageServiceRaster imageServiceRaster = new ImageServiceRaster(getResources().getString(R.string.image_service_url));
        final RasterLayer rasterLayer = new RasterLayer(imageServiceRaster);
        // zoom to the extent of the raster service
        rasterLayer.addDoneLoadingListener(new Runnable() {
            @Override
            public void run() {
                if(rasterLayer.getLoadStatus() == LoadStatus.LOADED){
                    // get the center point
                    Point centerPnt = imageServiceRaster.getServiceInfo().getFullExtent().getCenter();
                    mMapView.setViewpointCenterAsync(centerPnt, 55000000);
                }
            }
        });

        // add raster layer as map operational layer
        map.getOperationalLayers().add(rasterLayer);
        // add the map to a map view
        mMapView.setMap(map);
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
}
