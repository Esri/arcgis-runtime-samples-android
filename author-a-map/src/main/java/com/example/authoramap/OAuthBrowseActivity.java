package com.example.authoramap;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.security.OAuthLoginManager;
import com.esri.arcgisruntime.security.OAuthTokenCredential;

public class OAuthBrowseActivity extends AppCompatActivity {
    private final static String TAG = "OAuthBrowseActivity";
    private OAuthTokenCredential oauthCred;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Intent intent = getIntent();
        // Get the OAuthLoginManager object from the main activity.
        final OAuthLoginManager oauthLoginManager = MainActivity.getOAuthLoginManagerInstance();
        if (oauthLoginManager == null) {
            return;
        }

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

                Log.i(TAG, "cred " + oauthCred.getUsername() + "; " + oauthCred.getAccessToken());
                // Serialize the oauth credential and embed it in an intent.
                Intent mainIntent = new Intent(getBaseContext(), MapSaveActivity.class);
                mainIntent.putExtra(MapSaveActivity.KEY, oauthCred.toJson());
                startActivity(mainIntent);
                finish();
            }
        });

    }

}
