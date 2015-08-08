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
