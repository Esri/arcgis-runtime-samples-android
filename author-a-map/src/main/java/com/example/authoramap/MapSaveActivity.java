package com.example.authoramap;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;

import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.portal.Portal;
import com.esri.arcgisruntime.security.OAuthTokenCredential;

import java.util.ArrayList;

public class MapSaveActivity extends AppCompatActivity {

    public static final String KEY = "OAUTH_CREDENTIAL";
    private static String TAG = "MapSaveActivity";
    FloatingActionButton addAttachmentFab;
    private OAuthTokenCredential mOAuthCred;
    private EditText mTitleEditText, mTagsEditText, mDescEditText;
    private ArrayList<String> mTagsList = new ArrayList<>();
    private String mDescription;
    private String mTitle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_save);
        Log.d("MapSaveActivity", " here");

        mTitleEditText = (EditText) findViewById(R.id.titleText);
        mTagsEditText = (EditText) findViewById(R.id.tagText);
        mDescEditText = (EditText) findViewById(R.id.descText);

        // Check if the activity is started from the other oauth activity
        String json = getIntent().getStringExtra(KEY);
        if (json != null) {
            // An OAuth credential is embedded in the intent. Deserialize the credential
            mOAuthCred = (OAuthTokenCredential) OAuthTokenCredential.fromJson(json);

        }

        mTitleEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mTitle = s.toString();
                Log.d(TAG + "afterTC", mTitle);
            }
        });

        mTagsEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mTagsList.add(s.toString());
                Log.d(TAG + "afterTC", mTagsList.toArray().toString());

            }
        });

        mDescEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mDescription = s.toString();
                Log.d(TAG + "afterTC", mDescription);

            }
        });


    }

    protected void savMap() {
        // Load a webmap
        if (mOAuthCred != null) {
            //loadPortalItem(mOAuthCred);
            Log.d("mOAuthCred", mOAuthCred.getUsername());

            String[] portal_settings = getResources().getStringArray(R.array.portal);
            final Portal portal = new Portal(portal_settings[1], true);
            portal.setCredential(mOAuthCred);

            portal.addDoneLoadingListener(new Runnable() {
                @Override
                public void run() {
                    if (portal.getLoadStatus() == LoadStatus.LOADED) {

                        Log.d(TAG, " you are logged in!!");

                    }
                }
            });

        }
    }


}
