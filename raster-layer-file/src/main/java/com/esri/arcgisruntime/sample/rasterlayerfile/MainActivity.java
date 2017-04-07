/* Copyright 2016 Esri
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

package com.esri.arcgisruntime.sample.rasterlayerfile;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.esri.arcgisruntime.layers.RasterLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.raster.Raster;

import java.io.File;

/**
 * A sample class which demonstrates loading a Raster from the local device.
 */
public class MainActivity extends AppCompatActivity {

    private MapView mMapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // retrieve the MapView from layout
        mMapView = (MapView) findViewById(R.id.mapView);

        // define permission to request
        String[] reqPermission = new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE };
        int requestCode = 2;
        // For API level 23+ request permission at runtime
        if(ContextCompat.checkSelfPermission(MainActivity.this,
                reqPermission[0]) == PackageManager.PERMISSION_GRANTED){
            loadRaster();
        } else {
            // request permission
            ActivityCompat.requestPermissions(MainActivity.this, reqPermission, requestCode);
        }
    }

    /**
     * Loads Shasta.tif as a Raster and adds it to a new RasterLayer. The RasterLayer is then added
     * to the map as an operational layer. Map viewpoint is then set based on the raster's geometry.
     */
    private void loadRaster() {
        // create a raster from a local raster file
        Raster raster = new Raster(
                new File("/storage/emulated/0/ArcGIS/samples/Shasta.tif").getAbsolutePath());
        // create a raster layer
        final RasterLayer rasterLayer = new RasterLayer(raster);
        // create a Map with imagery basemap
        ArcGISMap map = new ArcGISMap(Basemap.createImagery());
        // add the map to a map view
        mMapView.setMap(map);
        // add the raster as an operational layer
        map.getOperationalLayers().add(rasterLayer);
        // set viewpoint on the raster
        rasterLayer.addDoneLoadingListener(new Runnable() {
            @Override
            public void run() {
                mMapView.setViewpointGeometryAsync(rasterLayer.getFullExtent(), 50);
            }
        });
    }

    /**
     * Handle the permissions request response.
     */
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults){
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadRaster();
        } else {
            // report to user that permission was denied
            Toast.makeText(MainActivity.this,
                    getResources().getString(R.string.location_permission_denied),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        mMapView.pause();
    }

    @Override
    protected void onResume(){
        super.onResume();
        mMapView.resume();
    }
}
