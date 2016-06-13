package com.example.authoramap;

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
import com.esri.arcgisruntime.portal.PortalInfo;
import com.esri.arcgisruntime.portal.PortalItem;
import com.esri.arcgisruntime.security.OAuthTokenCredential;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class MapSaveActivity extends AppCompatActivity {

    public static final String KEY = "OAUTH_CREDENTIAL";
    private static String TAG = "MapSaveActivity";
    FloatingActionButton addAttachmentFab;
    private OAuthTokenCredential mOAuthCred;
    private EditText mTitleEditText, mTagsEditText, mDescEditText;
    private ArrayList<String> mTagsList = new ArrayList<>();
    private String mDescription;
    private String mTitle;
    private Portal portal;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_save);

        mTitleEditText = (EditText) findViewById(R.id.titleText);
        mTagsEditText = (EditText) findViewById(R.id.tagText);
        mDescEditText = (EditText) findViewById(R.id.descText);

        addAttachmentFab = (FloatingActionButton) findViewById(R.id.saveFab);

        // Check if the activity is started from the other oauth activity
        String json = getIntent().getStringExtra(KEY);
        if (json != null) {
            // An OAuth credential is embedded in the intent. Deserialize the credential
            mOAuthCred = (OAuthTokenCredential) OAuthTokenCredential.fromJson(json);

        }

        addAttachmentFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // get Title, Tags and Description from the UI
                boolean flag = getMapAdditionalInfo();

                // if Title and tags are present
                if (flag) {
                    String[] portalSettings = getResources().getStringArray(R.array.portal);
                    portal = new Portal(portalSettings[1], true);
                    portal.setCredential(mOAuthCred);
                    portal.addDoneLoadingListener(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("Portal", portal.getLoadStatus().name());

                            if (portal.getLoadStatus() == LoadStatus.LOADED) {
                                PortalInfo portalInformation = portal.getPortalInfo();
                                // Save the map to an authenticated Portal, with specified title, tags, description, and thumbnail.
                                // Passing 'null' as portal folder parameter saves this to users root folder.
                                final ListenableFuture<PortalItem> saveAsFuture = MainActivity.mMap.saveAsAsync(portal, null,
                                        mTitle, mTagsList,
                                        mDescription, null);
                                saveAsFuture.addDoneListener(new Runnable() {
                                    @Override
                                    public void run() {
                                        // Check the result of the save operation.
                                        try {
                                            PortalItem newMapPortalItem = saveAsFuture.get();
                                            String portalItemId = newMapPortalItem.getId();
                                            Toast.makeText(getApplicationContext(), getString(R.string.map_successful), Toast.LENGTH_SHORT).show();
                                            openMapToView(portalItemId);
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

    protected void openMapToView(String portalItemId) {
        // Load a webmap
        final PortalItem mPortalItem = new PortalItem(portal, portalItemId);
        mPortalItem.loadAsync();

        mPortalItem.addDoneLoadingListener(new Runnable() {
            @Override
            public void run() {
                if (mPortalItem.getLoadStatus() == LoadStatus.LOADED) {
                    Log.d("Portal", mPortalItem.getName() + " " + mPortalItem.getTitle() + " " + mPortalItem.getId());
                }
            }
        });


    }


}
