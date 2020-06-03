/* Copyright 2018 Esri
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

package com.esri.arcgisruntime.sample.portaluserinfo;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.portal.Portal;
import com.esri.arcgisruntime.portal.PortalInfo;
import com.esri.arcgisruntime.portal.PortalUser;
import com.esri.arcgisruntime.security.AuthenticationManager;
import com.esri.arcgisruntime.security.DefaultAuthenticationChallengeHandler;

public class MainActivity extends AppCompatActivity {

  private final String TAG = MainActivity.class.getSimpleName();

  private TextView mUserText;
  private TextView mEmailText;
  private TextView mPortalNameText;
  private TextView mCreateDate;
  private ImageView mUserImage;
  private Portal mPortal;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // Set the DefaultAuthenticationChallengeHandler to allow authentication with the portal.
    //[DocRef: Name=Set DefaultAuthenticationChallengeHandler, Category=Cloud and servers, Topic=Access the ArcGIS Platform]
    // Create a DefaultAuthenticationChallengeHandler, passing in an Android Context (e.g. the current Activity)
    DefaultAuthenticationChallengeHandler handler = new DefaultAuthenticationChallengeHandler(this);
    // Set the challenge handler onto the AuthenticationManager
    AuthenticationManager.setAuthenticationChallengeHandler(handler);
    //[DocRef: END]

    // Set loginRequired to true always prompt for credential,
    // When set to false to only login if required by the portal
    mPortal = new Portal(getString(R.string.portal_url), true);
    mPortal.addDoneLoadingListener(() -> {
      if (mPortal.getLoadStatus() == LoadStatus.LOADED) {
        // Get the portal information
        PortalInfo portalInformation = mPortal.getPortalInfo();
        String portalName = portalInformation.getPortalName();
        mPortalNameText = (TextView) findViewById(R.id.portal);
        mPortalNameText.setText(portalName);

        // this portal does not require authentication, if null send toast message
        if (mPortal.getUser() != null) {
          // Get the authenticated portal user
          PortalUser user = mPortal.getUser();
          // get the users full name
          String userName = user.getFullName();
          // update the textview
          mUserText = findViewById(R.id.userName);
          mUserText.setText(userName);
          // get the users email
          String email = user.getEmail();
          // update the textview
          mEmailText = findViewById(R.id.email);
          mEmailText.setText(email);
          // get the created date
          Calendar startDate = user.getCreated();
          // format date
          SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getString(R.string.date_format), Locale.US);
          // get string format
          String formatDate = simpleDateFormat.format(startDate.getTime());
          // update textview
          mCreateDate = findViewById(R.id.create_date);
          mCreateDate.setText(formatDate);
          // check if user profile thumbnail exists
          if (user.getThumbnailFileName() == null) {
            return;
          }
          // fetch the thumbnail
          final ListenableFuture<byte[]> thumbnailFuture = user.fetchThumbnailAsync();
          thumbnailFuture.addDoneListener(() -> {
            // get the thumbnail image data
            byte[] itemThumbnailData;
            try {
              itemThumbnailData = thumbnailFuture.get();

              if ((itemThumbnailData != null) && (itemThumbnailData.length > 0)) {
                // create a Bitmap to use as required
                Bitmap itemThumbnail = BitmapFactory
                    .decodeByteArray(itemThumbnailData, 0, itemThumbnailData.length);
                // set the Bitmap onto the ImageView
                mUserImage = (ImageView) findViewById(R.id.userImage);
                mUserImage.setImageBitmap(itemThumbnail);
              }
            } catch (InterruptedException | ExecutionException e) {
              String errorMessage = getString(R.string.get_thumbnail_error);
              Log.e(TAG, errorMessage + e.getMessage());
              Toast.makeText(getApplicationContext(), errorMessage + "\n" + e.getMessage(), Toast.LENGTH_LONG)
                  .show();
            }
          });
        } else {
          // send message that user did not authenticate
          String authErrorMessage = getString(R.string.authenticate_error) + portalName;
          Log.e(TAG, authErrorMessage);
          Toast.makeText(getApplicationContext(), authErrorMessage, Toast.LENGTH_LONG).show();
        }
      }
    });
    mPortal.loadAsync();
  }
}

