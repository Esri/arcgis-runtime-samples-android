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

package com.esri.arcgisruntime.sample.authoramap;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.portal.Portal;
import com.esri.arcgisruntime.portal.PortalItem;
import com.esri.arcgisruntime.security.OAuthLoginManager;
import com.esri.arcgisruntime.security.OAuthTokenCredential;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class MapSaveActivity extends AppCompatActivity {
    FloatingActionButton saveFab;
    OAuthLoginManager oauthLoginManager;
    private OAuthTokenCredential oauthCred;
    private EditText mTitleEditText, mTagsEditText, mDescEditText;
    private ArrayList<String> mTagsList = new ArrayList<>();
    private String mDescription;
    private String mTitle;
    private Portal portal;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        // Get the OAuthLoginManager object from the main activity.
        oauthLoginManager = MainActivity.getOAuthLoginManagerInstance();
        if (oauthLoginManager == null) {
            return;
        }
        fetchCredentials(intent);

        setContentView(R.layout.activity_map_save);

        // inflate the EditText boxes
        mTitleEditText = (EditText) findViewById(R.id.titleText);
        mTagsEditText = (EditText) findViewById(R.id.tagText);
        mDescEditText = (EditText) findViewById(R.id.descText);
        progressDialog = new ProgressDialog(this);
        saveFab = (FloatingActionButton) findViewById(R.id.saveFab);

        // add a click listener for Floating Action Button
        saveFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // get Title, Tags and Description from the UI
                boolean flag = getMapAdditionalInfo();

                // if Title and tags are present
                if (flag) {
                    // inflate portal settings from array
                    String[] portalSettings = getResources().getStringArray(R.array.portal);
                    // create a Portal using the portal url from the array
                    portal = new Portal(portalSettings[1], true);
                    // set the credentials from the browser
                    portal.setCredential(oauthCred);


                    progressDialog.setTitle(getString(R.string.author_map_message));
                    progressDialog.setMessage(getString(R.string.wait));
                    progressDialog.show();

                    portal.addDoneLoadingListener(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("Portal", portal.getLoadStatus().name());
                            // if portal is LOADED, save the map to the portal
                            if (portal.getLoadStatus() == LoadStatus.LOADED) {
                                // Save the map to an authenticated Portal, with specified title, tags, description, and thumbnail.
                                // Passing 'null' as portal folder parameter saves this to users root folder.
                                final ListenableFuture<PortalItem> saveAsFuture = MainActivity.mMap.saveAsAsync(portal, null, mTitle, mTagsList, mDescription, null);
                                saveAsFuture.addDoneListener(new Runnable() {
                                    @Override
                                    public void run() {
                                        // Check the result of the save operation.
                                        try {
                                            if (progressDialog.isShowing()) {
                                                progressDialog.dismiss();
                                            }
                                            PortalItem newMapPortalItem = saveAsFuture.get();
                                            Toast.makeText(getApplicationContext(), getString(R.string.map_successful), Toast.LENGTH_SHORT).show();
                                        } catch (InterruptedException | ExecutionException e) {
                                            // If saving failed, deal with failure depending on the cause...
                                            Log.e("Exception", e.toString());
                                        }
                                    }
                                });
                            }
                        }
                    });
                    portal.loadAsync();
                }
            }
        });


    }

    /**
     * fetch the OAuth token from the OAuthLogin Manager
     *
     * @param intent
     */
    private void fetchCredentials(Intent intent) {
        // Fetch oauth access token.
        final ListenableFuture<OAuthTokenCredential> future = oauthLoginManager.fetchOAuthTokenCredentialAsync(intent);
        future.addDoneListener(new Runnable() {
            @Override
            public void run() {
                try {
                    oauthCred = future.get();
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }

            }
        });
    }

    /**
     * Get the Title, Tags and Description. Set error messages if title or tags are empty while saving a map
     *
     * @return true - both Title and Tags fields are non empty else returns false
     */
    private boolean getMapAdditionalInfo() {
        mTitle = mTitleEditText.getText().toString();
        mDescription = mDescEditText.getText().toString();
        String[] tags = (mTagsEditText.getText().toString()).split(",");
        for (String tag : tags) {
            mTagsList.add(tag);
        }
        if (TextUtils.isEmpty(mTitle)) {
            mTitleEditText.setError(getString(R.string.title_error));
            return false;
        }
        if (TextUtils.isEmpty(mTagsEditText.getText().toString())) {
            mTagsEditText.setError(getString(R.string.tags_error));
            return false;
        }

        return true;
    }

}
