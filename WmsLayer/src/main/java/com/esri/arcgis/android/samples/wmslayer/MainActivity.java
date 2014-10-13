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

package com.esri.arcgis.android.samples.wmslayer;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.esri.android.map.MapView;
import com.esri.android.map.ogc.WMSLayer;

/**
 * Sample showing an Esri Basemap with an WMS layer overlay
 * The WMS Layer is provided by:
 *    <a href="http://openweathermap.org/">Open Weather Map</a>
 */
public class MainActivity extends Activity {

    MapView mMapView;
    WMSLayer wmsLayer;
    String wmsURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // after the content of this activity is set
        // the map can be accessed from the layout
        mMapView = (MapView)findViewById(R.id.map);

        // set up the wms url
        wmsURL = "http://wms.openweathermap.org/service";
        wmsLayer = new WMSLayer(wmsURL);
        wmsLayer.setImageFormat("image/png");
        // available layers
        String[] visibleLayers = {"clouds", "precipitation"};
        wmsLayer.setVisibleLayer(visibleLayers);
        wmsLayer.setOpacity(0.5f);
        mMapView.addLayer(wmsLayer);

        // Set the Esri logo to be visible, and enable map to wrap around date line.
        mMapView.setEsriLogoVisible(true);
        mMapView.enableWrapAround(true);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
