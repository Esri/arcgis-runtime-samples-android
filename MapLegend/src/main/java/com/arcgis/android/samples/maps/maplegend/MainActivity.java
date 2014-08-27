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

package com.arcgis.android.samples.maps.maplegend;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISDynamicMapServiceLayer;
import com.esri.core.geometry.Envelope;


public class MainActivity extends Activity {

    private ArcGISDynamicMapServiceLayer mDynamicServiceLayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MapView mMapView = new MapView(this);
        mDynamicServiceLayer = new ArcGISDynamicMapServiceLayer(getResources().getString(R.string.map_service_url));
        mMapView.addLayer(mDynamicServiceLayer);
        Envelope mapExtent = new Envelope(-122.97, 26.27, -80.62, 47.99);
        mMapView.setExtent(mapExtent);

        mMapView.setEsriLogoVisible(true);
        mMapView.enableWrapAround(true);

        FrameLayout container = (FrameLayout) findViewById(R.id.container);
        container.addView(mMapView);

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
        if (id == R.id.action_legend) {
            // show the legend dialog
            new LegendDialogFragment().show(getFragmentManager(), LegendDialogFragment.TAG);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public ArcGISDynamicMapServiceLayer getLayer() {
        return mDynamicServiceLayer;
    }
}
