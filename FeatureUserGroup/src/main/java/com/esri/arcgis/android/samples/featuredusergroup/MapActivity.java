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

package com.esri.arcgis.android.samples.featuredusergroup;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.esri.android.map.MapView;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.core.map.CallbackListener;
import com.esri.core.portal.Portal;
import com.esri.core.portal.WebMap;

/**
 * This activity is launched when the user chooses an item from the list of webmaps in a group. The
 * portal item ID is passed in via the Intent that starts this activity.
 * <p>
 * It fetches the webmap and displays it in a MapView.
 * <p>
 * NOTE: The reason for using a separate activity to display the map is to optimize behavior on
 * device configuration changes. GroupsFragment and ItemsFragment use Fragment.setRetainInstance()
 * to retain Portal, PortalGroup and PortalItem objects when their host activity is destroyed on
 * configuration changes. However this technique would not work well for the WebMap object used by
 * MapActivity because it becomes tied to the MapView used to display it and a new MapView must be
 * created if the host activity is destroyed and recreated.
 * <p>
 * We solve this problem by isolating use of our WebMap in a separate activity, MapActivity, and
 * specifying the android:configChanges attribute for MapActivity as follows in the app�s manifest
 * file:
 * <p>
 * android:configChanges="orientation|screenSize|keyboard|keyboardHidden�
 * <p>
 * This stops MapActivity from being destroyed and restarted when orientation and keyboard
 * configuration changes occur.
 */
public class MapActivity extends Activity {
  public static final String KEY_PORTAL_ITEM_ID = "com.esri.arcgis.android.samples.ItemId";

  private static final String TAG = "MapActivity";

  ProgressDialog mProgressDialog = null;

  
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);

    // Get the item ID from the Intent that started this activity
    String itemId = getIntent().getExtras().getString(KEY_PORTAL_ITEM_ID);

    // Setup and show progress dialog
    mProgressDialog = new ProgressDialog(this) {
      @Override
      public void onBackPressed() {
        // Back key pressed - dismiss the dialog and finish the activity
        mProgressDialog.dismiss();
        finish();
      }
    };
    mProgressDialog.setMessage(getString(R.string.fetchingMap));
    mProgressDialog.show();

    // Get Portal object from ItemsFragment, but beware it could be null if the system has forced
    // ItemsFragment to be destroyed and recreated
    Portal portal = ItemsFragment.getPortal();
    if (portal == null) {
      // Just finish this activity if no Portal object available
      finish();
      return;
    }

    // Create a new instance of WebMap
    WebMap.newInstance(itemId, portal, new CallbackListener<WebMap>() {

      @Override
      public void onError(Throwable e) {
        Log.w(TAG, e);
        finish();
      }

      @Override
      public void onCallback(final WebMap webmap) {

        // The WebMap has been created - switch to UI thread to create MapView
        runOnUiThread(new Runnable() {

          @Override
          public void run() {

            // Create a MapView from the WebMap
            if (webmap != null) {
              MapView map = new MapView(MapActivity.this, webmap, null, null);

              map.setOnStatusChangedListener(new OnStatusChangedListener() {

                private static final long serialVersionUID = 1L;

                @Override
                public void onStatusChanged(Object source, STATUS status) {
                  switch (status) {
                    case INITIALIZED:
                      // MapView initialization complete so dismiss the progress dialog
                      mProgressDialog.dismiss();
                      break;
                    case INITIALIZATION_FAILED:
                      Toast.makeText(MapActivity.this, getString(R.string.webmapLoadFailed),
                          Toast.LENGTH_LONG).show();
                      break;
                    case LAYER_LOADED:
                    case LAYER_LOADING_FAILED:
                      break;
                  }
                }
              });

              // Display the MapView
              setContentView(map);
            }

          }
        });

      }
    });

  }
}
