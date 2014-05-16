/* Copyright 2014 ESRI
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
    MapView mMapView = null;

    // The basemap switching menu items.
    MenuItem mStreetsMenuItem = null;
    MenuItem mTopoMenuItem = null;
    MenuItem mGrayMenuItem = null;
    MenuItem mOceansMenuItem = null;

    // Create MapOptions for each type of basemap.
    final MapOptions mTopoBasemap = new MapOptions(MapOptions.MapType.TOPO);
    final MapOptions mStreetsBasemap = new MapOptions(MapOptions.MapType.STREETS);
    final MapOptions mGrayBasemap = new MapOptions(MapOptions.MapType.GRAY);
    final MapOptions mOceansBasemap = new MapOptions(MapOptions.MapType.OCEANS);

    // The current map extent, use to set the extent of the map after switching basemaps.
    Polygon mCurrentMapExtent = null;

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
