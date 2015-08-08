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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.http.SslError;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.webkit.SslErrorHandler;

import com.esri.android.oauth.OAuthView;
import com.esri.android.oauth.OAuthView.OnSslErrorListener;
import com.esri.core.io.UserCredentials;
import com.esri.core.map.CallbackListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.crypto.SecretKey;

/**
 * THIS SAMPLE APP DOES NOT IMPLEMENT AN ENCRYPTION PATTERN. THE USER CREDENTIALS ARE NOT ENCRYPTED WHEN 
 * THEY ARE SAVED ON DISK.
 * IT IS THE RESPONSIBILITY OF THE APP DEVELOPER TO IMPLEMENT A SUITABLE ENCRYPTION PATTERN WHEN STORING USER
 * CREDENTIALS ON DISK TO MAKE SURE THE APP IS SECURE.
 */

/**
 * This sample shows how to use OAuth2 to authenticate against an ArcGIS Portal.
 * Follow these steps to get a client id:
 * <ol>
 * <li>Browse to https://developers.arcgis.com.</li>
 * <li>Sign in with your ArcGIS developer account.</li>
 * <li>Create an application. This will give you access to a client id string.</li>
 * <li>Replace CLIENT_ID constant in your string resource file with the client
 * id string and run the sample.</li>
 * </ol>
 */

public class OAuth2Sample extends FragmentActivity {

  protected static final String TAG = "OAuth2Sample";

  public static final String EXIT = "EXIT";

  SecretKey mSecretKey;

  // UI components
  private AlertDialog mAlertDialog;
  private AlertDialog mImplementEncryptionDialog;

  // File based componentes
  public static String mCredentialsFileName;

  // ArcGIS components

  private static UserCredentials mUserLoginCredentials;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    // Intent sent from UserContentActivity to Quit the Activity and app
    if (getIntent().getBooleanExtra(EXIT, false)) {
      finish();
      return;
    }

    // Get the credentials file name
    mCredentialsFileName = getResources().getString(R.string.credentials_filename);

    // Setup the alert dialog for determining new login or login with
    // credentials from internal storage
    setupLoginTypeAlertDialog();
    
    // Setup the alert dialog to inform users to implement 
    setupEncryptionPatternAlertDialog();


    // If the credentials file exists on internal storage, give user an option
    // to load those credentials
    if (fileExists(mCredentialsFileName)) {
      mAlertDialog.show();
    } else {
      // Prompt user to login
      showOAuth(OAuth2Sample.this);
    }
  }

  /**
   * display OAuthView in a popup and prompt user to login
   * 
   * @param context
   */
  void showOAuth(Context context) {

    // Create an instance of OAuthView
    // set client_id in string resource
    OAuthView oAuthView = new OAuthView(context, getResources().getString(R.string.portal_url), getResources()
        .getString(R.string.client_id), new CallbackListener<UserCredentials>() {

      @Override
      public void onError(Throwable e) {
        Log.e(TAG, "", e);
      }

      @Override
      public void onCallback(UserCredentials credentials) {

        // set UserCredentials
        setCredentials(credentials);
        
        try {
          // Save the credentials on the internal storage
          encryptAndSaveCredentials();
        } catch (Exception e) {
          Log.e(TAG, "Exception while saving User Credentials.", e);
        }

        runOnUiThread(new Runnable() {
          public void run() {
            mImplementEncryptionDialog.show();
          }
        });
        
      }
    });

    // handle SSL errors
    oAuthView.setOnSslErrorListener(new OnSslErrorListener() {
      @Override
      public void onReceivedSslError(OAuthView view, SslErrorHandler handler, SslError error) {
        Log.d(TAG, "" + error);
      }

    });

    // add OAuthview to the Activity
    ((ViewGroup) findViewById(R.id.main)).addView(oAuthView, new LayoutParams(LayoutParams.MATCH_PARENT,
        LayoutParams.MATCH_PARENT));

  }

  public boolean fileExists(String fname) {
    File file = getBaseContext().getFileStreamPath(fname);
    return file.exists();
  }

  /**
   * Encrypt and save user credentials on internal storage
   * 
   * THIS METHOD DOES NOT IMPLEMENT AN ENCRYPTION PATTERN. ITS THE RESPONSIBILITY OF THE 
   * DEVELOPER TO IMPLEMENT A SUITABLE ENCRYPTION PATTERN TO MAKE SURE THE APP IS SECURE.
   * READ ABOUT ENCRYPTION PATTERNS HERE:
   * TODO : Paste blogpost link here
   * 
   */
  void encryptAndSaveCredentials() throws Exception {

    // TODO : Implement encrypting the credentials before saving them to disk
    
    // write the encrypted user credentials to internal storage
    writeToFile(mCredentialsFileName, getCredentials());
  }

  /**
   * Method for writing object to a file
   * 
   * @param filename
   * @param object
   * @throws Exception
   */
  private void writeToFile(String filename, Object object) throws Exception {
    FileOutputStream fos = null;
    ObjectOutputStream os = null;

    try {
      fos = openFileOutput(filename, Context.MODE_PRIVATE);
      os = new ObjectOutputStream(fos);
      os.writeObject(object);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (os != null) {
        try {
          os.close();
          fos.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * Setter for UserCredentials
   * 
   * @param credentials
   */
  public void setCredentials(UserCredentials credentials) {
    OAuth2Sample.mUserLoginCredentials = credentials;
  }

  /**
   * returns user credentials saved on internal storage
   * 
   * @return UserCredentials
   */
  public static UserCredentials getCredentials() {
    return mUserLoginCredentials;
  }

  /**
   * Loads the saved credentials from internal storage for re-login
   * 
   * THIS METHOD DOES NOT IMPLEMENT DECRYPTION PATTERN. ITS THE RESPONSIBILITY OF THE 
   * DEVELOPER TO IMPLEMENT A SUITABLE DECRYPTION PATTERN TO MAKE SURE THE APP IS SECURE.
   * READ ABOUT ENCRYPTION PATTERNS HERE:
   * TODO : Paste blogpost link here
   * 
   */
  void decryptAndloadCredentials() throws Exception {

    // TODO Decrypt the user credentials stored earlier on disk

    // set the credentials read from the file on interal storage
    setCredentials((UserCredentials) readFromFile(mCredentialsFileName));

  }

  /**
   * Method for reading object from a file
   * 
   * @param filename
   * @return
   * @throws Exception
   */
  private Object readFromFile(String filename) throws Exception {

    Object object = null;
    FileInputStream fis = null;
    ObjectInputStream is = null;

    try {
      fis = openFileInput(filename);
      is = new ObjectInputStream(fis);
      object = is.readObject();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (is != null) {
        try {
          is.close();
          fis.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    // }

    return object;
  }

  public void setupLoginTypeAlertDialog() {

    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(OAuth2Sample.this);

    // set title
    alertDialogBuilder.setTitle(getResources().getString(R.string.OAuth2_Sample));

    // set dialog message
    alertDialogBuilder.setMessage(getResources().getString(R.string.continue_with_saved_credentials))
        .setCancelable(false)
        .setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int id) {
            // if this button is clicked, load credentials
            // from internal storage
            try {
              decryptAndloadCredentials();
            } catch (Exception e) {
              e.printStackTrace();
            }
            // Start UserContentActivity
            Intent i = new Intent(getApplicationContext(), UserContentActivity.class);
            startActivity(i);

          }
        }).setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int id) {
            // close the dialog box and showOAuthview again
            dialog.cancel();
            // Start OAuthView again
            showOAuth(OAuth2Sample.this);
          }
        });

    // create alert dialog
    mAlertDialog = alertDialogBuilder.create();

  }
  
  public void setupEncryptionPatternAlertDialog() {

    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(OAuth2Sample.this);

    // set title
    alertDialogBuilder.setTitle(getResources().getString(R.string.OAuth2_Sample));

    // set dialog message
    alertDialogBuilder.setMessage(getResources().getString(R.string.implement_an_encryption_pattern))
        .setCancelable(false)
        .setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int id) {
            
            // Start UserContentActivity
            Intent i = new Intent(getApplicationContext(), UserContentActivity.class);
            startActivity(i);

          }
        });

    // create alert dialog
    mImplementEncryptionDialog = alertDialogBuilder.create();

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
