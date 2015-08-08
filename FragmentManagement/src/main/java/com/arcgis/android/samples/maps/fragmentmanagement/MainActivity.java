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

package com.arcgis.android.samples.maps.fragmentmanagement;

import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends Activity implements BasemapListFragment.BasemapListListener{

    private static final String TAG_LIST_FRAGMENT = "BasemapListFragment";
    private static final String TAG_MAP_FRAGMENT = "MapFragment";
    private static final String KEY_NUM_PANES = "NumPanes";
    private static final String KEY_ONLY_THE_MAP = "OnlyTheMap";
    private static final String KEY_ACTIVATED_POSITION = "ActivatedPosition";

    private boolean mTwoPane;
    private boolean mOnlyTheMapIsDisplayed;

    private BasemapListFragment mListFragment;
    private MapFragment mMapFragment;

    private int mActivatedPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        FragmentManager fragMgr = getFragmentManager();
        getActionBar().setDisplayHomeAsUpEnabled(false);
        getActionBar().setHomeButtonEnabled(false);

        // Reinstate saved instance state (if any)
        int numPanes = 0;
        if (savedInstanceState != null) {
            numPanes = savedInstanceState.getInt(KEY_NUM_PANES);
            mOnlyTheMapIsDisplayed = savedInstanceState.getBoolean(KEY_ONLY_THE_MAP);
            mActivatedPosition = savedInstanceState.getInt(KEY_ACTIVATED_POSITION);
        }

        // Find existing fragments (if any)
        mMapFragment = (MapFragment) fragMgr.findFragmentByTag(TAG_MAP_FRAGMENT);
        mListFragment = (BasemapListFragment) fragMgr.findFragmentByTag(TAG_LIST_FRAGMENT);

        // Check how many panes we have
        if (findViewById(R.id.map_fragment_container_twopane) != null) {
            // We have 2 panes - list on left and map on right
            mTwoPane = true;
            mOnlyTheMapIsDisplayed = false;

            // The system will display the fragments for us if numPanes indicates the activity is being recreated and there
            // were 2 panes beforehand
            if (numPanes != 2) {
                // Display list fragment in one pane
                displayListFragment();

                if (mMapFragment == null) {
                    // There's no existing map fragment, so create one
                    createMapFragment(MapFragment.BASEMAP_NAME_STREETS);
                } else {
                    // There's an existing map fragment - need to remove it from main_fragment_container before we can add it to
                    // map_fragment_container_twopane
                    fragMgr.beginTransaction().remove(mMapFragment).commit();
                    fragMgr.executePendingTransactions();
                }
                // Display map fragment in map_fragment_container_twopane
                fragMgr.beginTransaction().add(R.id.map_fragment_container_twopane, mMapFragment, TAG_MAP_FRAGMENT).commit();
            }
        } else {
            // We have just one pane
            mTwoPane = false;
            switch (numPanes) {
                case 0:
                    // It's a fresh start - just display the list fragment
                    displayListFragment();
                    break;
                case 2:
                    // The activity is being recreated and there were 2 panes beforehand.
                    // If there's an existing map fragment, move it from map_fragment_container_twopane to main_fragment_container
                    if (mMapFragment != null) {
                        // Need to remove it from its previous container before we can add it to a different container
                        fragMgr.beginTransaction().remove(mMapFragment).commit();
                        fragMgr.executePendingTransactions();
                        fragMgr.beginTransaction().replace(R.id.main_fragment_container, mMapFragment, TAG_MAP_FRAGMENT).commit();
                        mOnlyTheMapIsDisplayed = true;
                        getActionBar().setDisplayHomeAsUpEnabled(true);
                    }
                    break;
                default:
                    // The activity is being recreated and there was just 1 pane beforehand. The system displays the appropriate
                    // fragment for us
            }
        }

    }

    /**
     * Callback method from {@link BasemapListFragment.BasemapListListener} indicating that the basemap with the given
     * position and ID was selected from the list.
     */
    @Override
    public void onBasemapSelected(int position, String id) {
        mActivatedPosition = position;
        FragmentManager fragMgr = getFragmentManager();
        boolean newFragment = false;

        // Create new map fragment or pass ID of selected basemap to existing fragment
        if (mMapFragment == null) {
            createMapFragment(id);
            newFragment = true;
        } else {
            mMapFragment.changeBasemap(id);
        }

        if (mTwoPane) {
            // Two-pane mode - if new map fragment created, display it in map_fragment_container_twopane
            if (newFragment) {
                fragMgr.beginTransaction().replace(R.id.map_fragment_container_twopane, mMapFragment, TAG_MAP_FRAGMENT)
                        .commit();
            }
        } else {
            // Single-pane mode - replace the list fragment in main_fragment_container by the map fragment
            fragMgr.beginTransaction().replace(R.id.main_fragment_container, mMapFragment, TAG_MAP_FRAGMENT).commit();
            mOnlyTheMapIsDisplayed = true;
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // If map fragment was previously detached from the UI, in displayListFragment(), need to attach it again
        if (mMapFragment.isDetached()) {
            fragMgr.beginTransaction().attach(mMapFragment).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // This ID represents the Home or Up button. In the case of this activity,
                // the Home button is shown only when we're in single-pane mode and the map
                // fragment is displayed.
                // Disable Home button and display list fragment in place of map fragment.
                getActionBar().setHomeButtonEnabled(false);
                getActionBar().setDisplayHomeAsUpEnabled(false);
                displayListFragment();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        getActionBar().setHomeButtonEnabled(false);
        getActionBar().setDisplayHomeAsUpEnabled(false);

        if (mOnlyTheMapIsDisplayed) {
            // Single-pane mode and map fragment displayed - Back returns us to list fragment
            displayListFragment();
        } else {
            // Otherwise Back finishes the activity
            finish();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        int numPanes = 1;
        if (mTwoPane) {
            numPanes = 2;
        }
        outState.putInt(KEY_NUM_PANES, numPanes);
        outState.putBoolean(KEY_ONLY_THE_MAP, mOnlyTheMapIsDisplayed);
        outState.putInt(KEY_ACTIVATED_POSITION, mActivatedPosition);
    }

    @Override
    public void onResume() {
        super.onResume();
        // If in two-pane mode, tell list fragment to highlight the currently selected item
        if (mTwoPane && mListFragment != null) {
            mListFragment.setActivateOnItemClick(true);
        }
    }

    /**
     * Displays the list fragment.
     */
    private void displayListFragment() {
        FragmentManager fragMgr = getFragmentManager();

        // Create the list fragment only if it's not created yet (platform recreates fragments after they're destroyed)
        if (mListFragment == null) {
            Bundle arguments = new Bundle();
            arguments.putInt(BasemapListFragment.ARG_ACTIVATED_POSITION, mActivatedPosition);
            mListFragment = new BasemapListFragment();
            mListFragment.setArguments(arguments);
        }

        // If there's a map fragment, detach it now to ensure it's not lost when the list fragment is displayed below
        if (mMapFragment != null) {
            fragMgr.beginTransaction().detach(mMapFragment).commit();
            fragMgr.executePendingTransactions();
        }

        fragMgr.beginTransaction().replace(R.id.main_fragment_container, mListFragment, TAG_LIST_FRAGMENT).commit();
        mOnlyTheMapIsDisplayed = false;
    }

    /**
     * Creates a new map fragment.
     *
     * @param id String identifier of basemap to display.
     */
    private void createMapFragment(String id) {
        Bundle arguments = new Bundle();
        arguments.putString(MapFragment.ARG_BASEMAP_ID, id);
        mMapFragment = new MapFragment();
        mMapFragment.setArguments(arguments);
    }

}
