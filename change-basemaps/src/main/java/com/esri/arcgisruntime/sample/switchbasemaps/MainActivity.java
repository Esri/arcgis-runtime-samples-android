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

package com.esri.arcgisruntime.sample.switchbasemaps;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Map;
import com.esri.arcgisruntime.mapping.view.MapView;

public class MainActivity extends AppCompatActivity {

    private MapView mMapView;
    private Map mMap;

    // The basemap switching menu items.
    private MenuItem mStreetsMenuItem = null;
    private MenuItem mTopoMenuItem = null;
    private MenuItem mGrayMenuItem = null;
    private MenuItem mOceansMenuItem = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // inflate MapView from layout
        mMapView = (MapView) findViewById(R.id.mapView);
        // create a map with Topographic Basemap
        mMap = new Map(Basemap.createTopographic());
        // set the map to be displayed in this view
        mMapView.setMap(mMap);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Get the basemap switching menu items.
        mStreetsMenuItem = menu.getItem(0);
        mTopoMenuItem = menu.getItem(1);
        mGrayMenuItem = menu.getItem(2);
        mOceansMenuItem = menu.getItem(3);

        // set the topo menu item checked by default
        mTopoMenuItem.setChecked(true);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // handle menu item selection
        switch(item.getItemId()){
            case R.id.World_Street_Map:
                // create a map with Streets Basemap
                mMap.setBasemap(Basemap.createStreets());
                mStreetsMenuItem.setChecked(true);
                return true;
            case R.id.World_Topo:
                // create a map with Topographic Basemap
                mMap.setBasemap(Basemap.createTopographic());
                mTopoMenuItem.setChecked(true);
                return true;
            case R.id.Gray:
                // create a map with Gray Basemap
                mMap.setBasemap(Basemap.createLightGrayCanvas());
                mGrayMenuItem.setChecked(true);
                return true;
            case R.id.Ocean_Basemap:
                // create a map with Oceans Basemap
                mMap.setBasemap(Basemap.createOceans());
                mOceansMenuItem.setChecked(true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        mMapView.resume();
    }

    @Override
    protected void onPause(){
        super.onPause();
        mMapView.pause();

    }
}
