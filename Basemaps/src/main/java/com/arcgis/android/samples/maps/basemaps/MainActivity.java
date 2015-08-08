/* Copyright 2015 Esri
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

package com.arcgis.android.samples.maps.basemaps;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.esri.android.map.MapOptions;
import com.esri.android.map.MapView;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.core.geometry.Polygon;


public class MainActivity extends Activity {

    // The MapView.
    private MapView mMapView = null;

    // The basemap switching menu items.
    private MenuItem mStreetsMenuItem = null;
    private MenuItem mTopoMenuItem = null;
    private MenuItem mGrayMenuItem = null;
    private MenuItem mOceansMenuItem = null;

    // Create MapOptions for each type of basemap.
    private final MapOptions mTopoBasemap = new MapOptions(MapOptions.MapType.TOPO);
    private final MapOptions mStreetsBasemap = new MapOptions(MapOptions.MapType.STREETS);
    private final MapOptions mGrayBasemap = new MapOptions(MapOptions.MapType.GRAY);
    private final MapOptions mOceansBasemap = new MapOptions(MapOptions.MapType.OCEANS);

    // The current map extent, use to set the extent of the map after switching basemaps.
    private Polygon mCurrentMapExtent = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Retrieve the map and initial extent from XML layout
        mMapView = (MapView) findViewById(R.id.map);

        // Set the Esri logo to be visible, and enable map to wrap around date line.
        mMapView.setEsriLogoVisible(true);
        mMapView.enableWrapAround(true);

        // Set a listener for map status changes; this will be called when switching basemaps.
        mMapView.setOnStatusChangedListener(new OnStatusChangedListener() {

            private static final long serialVersionUID = 1L;

            public void onStatusChanged(Object source, STATUS status) {
                // Set the map extent once the map has been initialized, and the basemap is added
                // or changed; this will be indicated by the layer initialization of the basemap layer. As there is only
                // a single layer, there is no need to check the source object.
                if (STATUS.LAYER_LOADED == status) {
                    mMapView.setExtent(mCurrentMapExtent);
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        // Get the basemap switching menu items.
        mStreetsMenuItem = menu.getItem(0);
        mTopoMenuItem = menu.getItem(1);
        mGrayMenuItem = menu.getItem(2);
        mOceansMenuItem = menu.getItem(3);

        // Also set the topo basemap menu item to be checked, as this is the default.
        mTopoMenuItem.setChecked(true);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Save the current extent of the map before changing the map.
        mCurrentMapExtent = mMapView.getExtent();

        // Handle menu item selection.
        switch (item.getItemId()) {
            case R.id.World_Street_Map:
                mMapView.setMapOptions(mStreetsBasemap);
                mStreetsMenuItem.setChecked(true);
                return true;
            case R.id.World_Topo:
                mMapView.setMapOptions(mTopoBasemap);
                mTopoMenuItem.setChecked(true);
                return true;
            case R.id.Gray:
                mMapView.setMapOptions(mGrayBasemap);
                mGrayMenuItem.setChecked(true);
                return true;
            case R.id.Ocean_Basemap:
                mMapView.setMapOptions(mOceansBasemap);
                mOceansMenuItem.setChecked(true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void onPause() {
        super.onPause();
        mMapView.pause();
    }

    protected void onResume() {
        super.onResume();
        mMapView.unpause();
    }

}
