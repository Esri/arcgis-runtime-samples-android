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

package com.esri.arcgisruntime.sample.takescreenshot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaActionSound;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.MapView;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();
  private final int requestCode = 2;
  private final String[] permission = new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE };
  private MapView mMapView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // inflate MapView from layout
    mMapView = (MapView) findViewById(R.id.mapView);
    // create a map with the BasemapType Imagery with Labels
    ArcGISMap mMap = new ArcGISMap(Basemap.createImageryWithLabels());
    // set the map to be displayed in this view
    mMapView.setMap(mMap);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // handle menu item selection

    int itemId = item.getItemId();
    if (itemId == R.id.CaptureMap) {
      // Check permissions to see if failure may be due to lack of permissions.
      boolean permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, permission[0]) ==
          PackageManager.PERMISSION_GRANTED;

      if (!permissionCheck) {
        // If permissions are not already granted, request permission from the user.
        ActivityCompat.requestPermissions(MainActivity.this, permission, requestCode);
      } else {
        captureScreenshotAsync();
      }
    }

    return true;
  }

  /**
   * capture the map as an image
   */
  private void captureScreenshotAsync() {

    // export the image from the mMapView
    final ListenableFuture<Bitmap> export = mMapView.exportImageAsync();
    export.addDoneListener(new Runnable() {
      @Override
      public void run() {
        try {
          Bitmap currentMapImage = export.get();
          // play the camera shutter sound
          MediaActionSound sound = new MediaActionSound();
          sound.play(MediaActionSound.SHUTTER_CLICK);
          Log.d(TAG, "Captured the image!!");
          // save the exported bitmap to an image file
          SaveImageTask saveImageTask = new SaveImageTask();
          saveImageTask.execute(currentMapImage);
        } catch (Exception e) {
          Toast
              .makeText(getApplicationContext(), getResources().getString(R.string.map_export_failure) + e.getMessage(),
                  Toast.LENGTH_SHORT).show();
          Log.e(TAG, getResources().getString(R.string.map_export_failure) + e.getMessage());
        }
      }
    });
  }

  /**
   * save the bitmap image to file and open it
   *
   * @param bitmap
   * @throws IOException
   */
  private File saveToFile(Bitmap bitmap) throws IOException {

    // create a directory ArcGIS to save the file
    File root;
    File file = null;
    String fileName = "map-export-image" + System.currentTimeMillis() + ".png";
    root = getExternalFilesDir(null);
    File fileDir = new File(root.getAbsolutePath() + "/ArcGIS Export/");
    boolean isDirectoryCreated = fileDir.exists();
    if (!isDirectoryCreated) {
      isDirectoryCreated = fileDir.mkdirs();
    }
    if (isDirectoryCreated) {
      file = new File(fileDir, fileName);
      // write the bitmap to PNG file
      FileOutputStream fos = new FileOutputStream(file);
      bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);

      // close the stream
      fos.flush();
      fos.close();
    }
    return file;

  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    // If request is cancelled, the result arrays are empty.
    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      // Location permission was granted. This would have been triggered in response to failing to start the
      // LocationDisplay, so try starting this again.
      captureScreenshotAsync();
    } else {
      // If permission was denied, show toast to inform user what was chosen. If LocationDisplay is started again,
      // request permission UX will be shown again, option should be shown to allow never showing the UX again.
      // Alternative would be to disable functionality so request is not shown again.
      Toast.makeText(MainActivity.this, getResources().getString(R.string.storage_permission_denied), Toast
          .LENGTH_SHORT).show();

    }
  }

  @Override
  protected void onPause() {
    super.onPause();
    mMapView.pause();
  }

  @Override
  protected void onResume() {
    super.onResume();
    mMapView.resume();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mMapView.dispose();
  }

  /**
   * AsyncTask class to save the bitmap as an image
   */
  private class SaveImageTask extends AsyncTask<Bitmap, Void, File> {

    @Override
    protected void onPreExecute() {
      // display a toast message to inform saving the map as an image
      Toast.makeText(getApplicationContext(), getResources().getString(R.string.map_export_message), Toast.LENGTH_SHORT)
          .show();
    }

    /**
     * save the file using a worker thread
     */
    @Override
    protected File doInBackground(Bitmap... mapBitmap) {

      try {
        return saveToFile(mapBitmap[0]);
      } catch (Exception e) {
        Log.e(TAG, getResources().getString(R.string.map_export_failure) + e.getMessage());
      }

      return null;

    }

    /**
     * Perform the work on UI thread to open the exported map image
     */
    @Override
    protected void onPostExecute(File file) {
      // Open the file to view
      Intent i = new Intent();
      i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
      i.setAction(Intent.ACTION_VIEW);
      i.setDataAndType(
          FileProvider.getUriForFile(MainActivity.this, getApplicationContext().getPackageName() + ".provider", file),
          "image/png");
      startActivity(i);
    }
  }

  public static class ScreenshotFileProvider extends FileProvider {}
}
