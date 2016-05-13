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

package com.esri.arcgisruntime.sample.displaydevicelocation;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.LocationDisplay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.sample.spinner.ItemData;
import com.esri.arcgisruntime.sample.spinner.SpinnerAdapter;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private MapView mMapView;
    private LocationDisplay mLocationDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Spinner spinner;

        // inflate MapView from layout
        mMapView = (MapView) findViewById(R.id.mapView);

        // get the MapView's LocationDisplay
        mLocationDisplay = mMapView.getLocationDisplay();

        // Populate the list for the Location display options for the spinner's Adapter
        ArrayList<ItemData> list = new ArrayList<>();
        list.add(new ItemData("Stop", R.drawable.locationdisplaydisabled));
        list.add(new ItemData("On", R.drawable.locationdisplayon));
        list.add(new ItemData("Re-Center", R.drawable.locationdisplayrecenter));
        list.add(new ItemData("Navigation", R.drawable.locationdisplaynavigation));
        list.add(new ItemData("Compass", R.drawable.locationdisplayheading));

        // inflate the Spinner from layout
        spinner = (Spinner) findViewById(R.id.spinner);
        SpinnerAdapter adapter = new SpinnerAdapter(this,
                R.layout.spinner_layout, R.id.txt, list);
        spinner.setAdapter(adapter);

        // create a map with the BasemapType Imagery
        ArcGISMap mMap = new ArcGISMap(Basemap.createImagery());
        // set the map to be displayed in this view
        mMapView.setMap(mMap);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()

                                           {
                                               @Override
                                               public void onItemSelected(AdapterView<?> parent, View view, int position,
                                                                          long id) {
                                                   switch (position) {
                                                       case 0:
                                                           // Stop Location Display
                                                           if (mLocationDisplay.isStarted())
                                                               mLocationDisplay.stop();
                                                           break;
                                                       case 1:
                                                           // Start Location Display
                                                           if (!mLocationDisplay.isStarted())
                                                               mLocationDisplay.startAsync();
                                                           break;
                                                       case 2:
                                                           // Re-Center MapView on Location
                                                           // In this mode, the MapView attempts to keep the location symbol on-screen by re-centering
                                                           // the location symbol when the symbol moves outside a "wander extent." The location symbol
                                                           // may move freely within the wander extent, but as soon as the symbol exits the wander extent,
                                                           // the MapView re-centers the map on the symbol.
                                                           mLocationDisplay.setAutoPanMode(LocationDisplay.AutoPanMode.RECENTER);
                                                           if (!mLocationDisplay.isStarted())
                                                               mLocationDisplay.startAsync();
                                                           break;
                                                       case 3:
                                                           // Start Navigation Mode
                                                           // This mode is best suited for in-vehicle navigation.
                                                           mLocationDisplay.setAutoPanMode(LocationDisplay.AutoPanMode.NAVIGATION);
                                                           if (!mLocationDisplay.isStarted())
                                                               mLocationDisplay.startAsync();
                                                           break;
                                                       case 4:
                                                           // Start Compass Mode
                                                           // This mode is better suited for waypoint navigation when the user is walking.
                                                           mLocationDisplay.setAutoPanMode(LocationDisplay.AutoPanMode.COMPASS_NAVIGATION);
                                                           if (!mLocationDisplay.isStarted())
                                                               mLocationDisplay.startAsync();
                                                           break;
                                                   }

                                               }

                                               @Override
                                               public void onNothingSelected(AdapterView<?> parent) {

                                               }
                                           }

        );
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