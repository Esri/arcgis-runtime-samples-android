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

package com.arcgis.android.samples.maps.switchmaps;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.esri.android.map.MapView;

/* This sample shows a way to switch between two different maps in a single app by leveraging the Android Fragments.
 * In this app, a single MapActivity contains a MapFragment that in turn contains a MapView. When the button on the
 * action bar is pressed, the existing MapFragment is replaced by a new instance of the MapFragment that has
 * different map contents. The current extent of the MapView is preserved when switching between fragments, by
 * making use of the retainState and restoreState methods on the MapView.
 */
public class MainActivity extends Activity {

    // service url string
    String topoUrl;
    String streetsUrl;

    // Action bar, and items for switching between maps. Visibility of these items
    // is changed, so that only one option is available at a time.
    ActionBar mActionBar;
    MenuItem mSwitchTo1MenuItem;
    MenuItem mSwitchTo2MenuItem;

    // Current map fragment state.
    boolean map1Active = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // get service url from string resource
        topoUrl = getResources().getString(R.string.topo);
        streetsUrl = getResources().getString(R.string.streets);

        if (savedInstanceState == null) {
            // If no saved state exists, add a map fragment with the initial map
            // service layer.
            MapFragment mapFrag = new MapFragment();
            // arguments to send to MapFragment
            Bundle args = new Bundle();
            // service url
            args.putString("MAPURL", topoUrl);
            // opening extent null as it is set by view xml
            args.putString("MAPSTATE", null);

            mapFrag.setArguments(args);

            getFragmentManager().beginTransaction().add(R.id.fragmentContainer, mapFrag).commit();
        } else {
            // If there is saved state, then the fragment will be re-created by the android framework.
            // Extract the saved state of the activity from the bundle parameter.
            map1Active = savedInstanceState.getBoolean("map1Active", true);
        }

        // Set up the action bar.
        mActionBar = getActionBar();
        mActionBar.setTitle(R.string.app_name);

    }

    // Store temporary state so that this can be reinstated, for example if the device is rotated.
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save the activity state - which map fragment is currently active.
        outState.putBoolean("map1Active", map1Active);
    }

    // Update the action bar based on the saved state.
    private void updateActionBar() {
        // Update the action bar subtitle to indicate the current map.
        if (mActionBar != null) {
            mActionBar.setSubtitle(map1Active ? R.string.map1 : R.string.map2);
        }
        // Update the visible menu items to allow correctly switching maps.
        if (mSwitchTo1MenuItem != null) {
            mSwitchTo1MenuItem.setVisible(!map1Active);
        }
        if (mSwitchTo2MenuItem != null) {
            mSwitchTo2MenuItem.setVisible(map1Active);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        // Get references to the buttons on the menu for use in onOptionsItemSelected.
        mSwitchTo1MenuItem = menu.findItem(R.id.switchToMap1Button);
        mSwitchTo2MenuItem = menu.findItem(R.id.switchToMap2Button);

        // Initialize the state of the subtitles and buttons from the saved state.
        updateActionBar();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle user pressing the action bar menu items.
        boolean retVal = false;

        // Get the current map directly from this activity.
        MapView currentMap = (MapView) this.findViewById(R.id.map);

        // Based on the menu item selected, switch the map fragment.
        String mapUrl = null;
        switch (item.getItemId()) {
            case R.id.switchToMap2Button:
                // Use the alternative map service as a layer.
                mapUrl = streetsUrl;
                map1Active = false;
                break;

            case R.id.switchToMap1Button:
                // Use the first map service as a layer.
                mapUrl = topoUrl;
                map1Active = true;
                break;

            default:
                retVal = super.onOptionsItemSelected(item);
                break;
        }

        if (mapUrl != null) {
            // Create a new fragment with a specific map service layer.
            MapFragment mapFrag = new MapFragment();
            // arguments to be sent to MapFragment
            Bundle args = new Bundle();
            // service url
            args.putString("MAPURL", mapUrl);
            // current extent
            args.putString("MAPSTATE", currentMap.retainState());
            mapFrag.setArguments(args);

            // Create a transaction and replace existing fragment with this new
            // fragment.
            FragmentTransaction ft2 = getFragmentManager().beginTransaction();
            ft2.replace(R.id.fragmentContainer, mapFrag);
            ft2.commit();

            // Update ActionBar subtitle, and action bar button visibility.
            updateActionBar();
            retVal = true;
        }

        return retVal;
    }
}
