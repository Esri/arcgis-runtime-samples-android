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

package com.arcgis.android.samples.maps.maprotation;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.esri.android.map.MapView;
import com.esri.android.map.event.OnSingleTapListener;

/**
 * The Map Rotation sample app shows how to allow a user to rotate a map, and also shows a compass that displays the
 * current map rotation angle. The setAllowRotationByPinch method allows rotation of the map using a pinch gesture; the
 * current angle of rotation is then retrieved from the MapView using getRotationAngle. A custom View showing a compass
 * image is added to the map, which rotates itself in response to the OnPinchListener set on the MapView. An
 * OnSingleTapListener allows the map rotation angle to be reset to 0 by tapping on the map.
 */
public class MainActivity extends Activity {

    MapView mMapView;
    Compass mCompass;

    int mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get a reference to the MapView.
        mMapView = (MapView) findViewById(R.id.map);

        // Set the MapView to allow the user to rotate the map when as part of a pinch gesture.
        mMapView.setAllowRotationByPinch(true);

        // Enabled wrap around map.
        mMapView.enableWrapAround(true);

        // Create the Compass custom view, and add it onto the MapView.
        mCompass = new Compass(this, null, mMapView);
        mMapView.addView(mCompass);

        // Set a single tap listener on the MapView.
        mMapView.setOnSingleTapListener(new OnSingleTapListener() {

            public void onSingleTap(float x, float y) {

                // When a single tap gesture is received, reset the map to its default rotation angle,
                // where North is shown at the top of the device.
                mMapView.setRotationAngle(0);

                // Also reset the compass angle.
                mCompass.setRotationAngle(0);
            }
        });

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

    @Override
    protected void onPause() {
        super.onPause();
        // Call MapView.pause to suspend map rendering while the activity is paused
        mMapView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Call MapView.unpause to resume map rendering when the activity returns to the foreground.
        mMapView.unpause();
    }

}
