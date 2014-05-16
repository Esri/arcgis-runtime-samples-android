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

package com.arcgis.android.samples.maps.switchmaps;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

/* This sample shows a way to switch between two different maps in a single app by leveraging the Android Fragments.
 * In this app, a single MapActivity contains a MapFragment that in turn contains a MapView. When the button on the
 * action bar is pressed, the existing MapFragment is replaced by a new instance of the MapFragment that has
 * different map contents. The current extent of the MapView is preserved when switching between fragments, by
 * making use of the retainState and restoreState methods on the MapView.
 */
public class MainActivity extends Activity {

    // service url string
    private final String topoUrl;
    private final String streetsUrl;

    // Action bar, and items for switching between maps. Visibility of these items
    // is changed, so that only one option is available at a time.
    private ActionBar mActionBar;
    private MenuItem mSwitchTo1MenuItem;
    private MenuItem mSwitchTo2MenuItem;

    // Current map fragment state.
    private boolean map1Active = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
}
