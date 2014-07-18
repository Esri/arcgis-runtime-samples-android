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
        setContentView(R.layout.activity_main);

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
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
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
