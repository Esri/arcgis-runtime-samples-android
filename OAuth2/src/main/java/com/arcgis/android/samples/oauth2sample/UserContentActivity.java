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

package com.arcgis.android.samples.oauth2sample;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.arcgis.android.samples.oauth2sample.MapFragment.OnFragmentInteractionListener_MapFragment;
import com.arcgis.android.samples.oauth2sample.UserContentFragment.OnFragmentInteractionListener;
import com.esri.core.io.UserCredentials;
import com.esri.core.portal.Portal;
import com.esri.core.portal.PortalItem;
import com.esri.core.portal.PortalItemType;
import com.esri.core.portal.PortalUser;
import com.esri.core.portal.PortalUserContent;

import java.util.ArrayList;
import java.util.List;

public class UserContentActivity extends FragmentActivity implements OnFragmentInteractionListener,
    OnFragmentInteractionListener_MapFragment {

  protected static final String TAG = "UserContentActivity";

  // ArcGIS components
  public static ArrayList<UserWebmaps> mUserPortalDataList;

  UserCredentials mValidLoginCredentials;

  public static Portal mMyPortal;

  // UI components
  AlertDialog mAlertDialog;

  static ProgressDialog mProgressDialog;

  protected static final int CLOSE_LOADING_WINDOW = 0;

  // Handler to close loading window
  final Handler uihandler = new HandlerExtension();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Log.d("UserContentActivity", "inside UserContentActivity");

    mValidLoginCredentials = OAuth2Sample.getCredentials();

    setupQuitAppAlertDialog();

    mProgressDialog = ProgressDialog
        .show(this, "WebMaps from arcgis.com ", "Getting User WebMaps from portal ........");

    new GroupTask().execute();

  }

  private final static class HandlerExtension extends Handler {

    // default constructor
    public HandlerExtension() {
    }

    @Override
    public void handleMessage(Message msg) {
      switch ((msg.what)) {
      case CLOSE_LOADING_WINDOW:

        if (mProgressDialog != null)
          mProgressDialog.dismiss();
        break;

      default:
        break;
      }

    }
  }

  public class GroupTask extends AsyncTask<Void, Void, Void> {

    @Override
    protected Void doInBackground(Void... params) {
      try {
        getUserContentFromPortal();
      } catch (Exception e) {
        Log.e(TAG, "Exception while getting content from portal", e);
      }

      return null;
    }

  }

  public void getUserContentFromPortal() throws Exception {

    UserWebmaps userWebMaps;
    mMyPortal = new Portal(getResources().getString(R.string.portal_url), mValidLoginCredentials);
    mUserPortalDataList = new ArrayList<UserWebmaps>();

    // Fetch user from the portal and get user contents

    PortalUser user = mMyPortal.fetchUser();
    PortalUserContent puc = user.fetchContent();
    List<PortalItem> items = puc.getItems();

    if (items == null) {
      Log.e(TAG, "No items returned by Portal for the user");
      return;
    }

    // Get only the webmaps in the user account and add them in the ArrayList
    for (PortalItem item : items) {
      Log.i(TAG, "Item id = " + item.getTitle());
      if (item.getType() == PortalItemType.WEBMAP) {
        byte[] data = item.fetchThumbnail();
        Bitmap bitmap;
        if (data != null) {
          bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        } else {
          bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.map);
        }
        userWebMaps = new UserWebmaps(item, bitmap);
        Log.i(TAG, "Item id = " + item.getTitle());
        mUserPortalDataList.add(userWebMaps);
      }

    }

    Log.d(TAG, "userPortalDataList" + mUserPortalDataList);
    uihandler.sendEmptyMessage(CLOSE_LOADING_WINDOW);
    UserContentActivity.this.runOnUiThread(new Runnable() {

      @Override
      public void run() {

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        UserContentFragment ucf = UserContentFragment.newInstance();
        ft.add(android.R.id.content, ucf, "user_content_fragment");
        ft.addToBackStack("user_content_fragment");
        ft.commit();
      }
    });

  }

  public void setupQuitAppAlertDialog() {

    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(UserContentActivity.this);

    // set title
    alertDialogBuilder.setTitle(getResources().getString(R.string.OAuth2_Sample));

    // set dialog message
    alertDialogBuilder.setMessage(getResources().getString(R.string.quit_app)).setCancelable(false)
        .setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int id) {
            // if this button is clicked, close
            // current activity
            Intent intent = new Intent(getApplicationContext(), OAuth2Sample.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra(OAuth2Sample.EXIT, true);
            startActivity(intent);
          }
        }).setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int id) {
            // if this button is clicked, just close
            // the dialog box and do nothing
            dialog.cancel();
          }
        });

    // create alert dialog
    mAlertDialog = alertDialogBuilder.create();

    // show it

  }

  @Override
  public void onFragmentInteraction(String id) {

    Log.d(TAG, "on Fragment Interaction");

    MapFragment mapFragment = MapFragment.newInstance(id);

    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
    transaction.replace(android.R.id.content, mapFragment, "mapfragment");
    transaction.addToBackStack("mapfragment");

    // Commit the transaction
    transaction.commit();

  }

  // Fragment interaction listener for MapFragment
  // we don't do anything in this method as we don't want to perform any
  // action on the map
  @Override
  public void onFragmentInteraction(Uri uri) {

  }

  @Override
  public void onBackPressed() {

    Fragment f = getSupportFragmentManager().findFragmentByTag("user_content_fragment");
    if (f.isVisible()) {
      mAlertDialog.show();
      return;
    }
    super.onBackPressed();

  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
  }

  @Override
  protected void onPause() {
    super.onPause();
    mAlertDialog.dismiss();
  }

  @Override
  protected void onResume() {
    super.onResume();
  }

}