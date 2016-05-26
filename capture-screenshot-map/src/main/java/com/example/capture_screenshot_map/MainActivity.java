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

package com.example.capture_screenshot_map;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaActionSound;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.MapView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private MapView mMapView;
    private String TAG = "CaptureScreenshotMap";


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
        //if-else is used because this sample is used elsewhere as a Library module
        int itemId = item.getItemId();
        if (itemId == R.id.CaptureMap) {
            // take a screenshot of the image
            captureScreenshotAsync();
        }

        return true;
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
                    Log.d(TAG,"Captured the image!!");
                    // save the exported bitmap to an image file
                    SaveImageTask saveImageTask = new SaveImageTask();
                    saveImageTask.execute(currentMapImage);
                } catch (Exception e) {
                    Log.d(TAG, "Fail to export map image: " + e.getMessage());
                }
            }
        });
    }

    /**
     * AsyncTask class to save the bitmap as an image
     */
    public class SaveImageTask extends AsyncTask<Bitmap, Void, File> {

        protected void onPreExecute() {
            // display a toast message to inform saving the map as an image
            Toast.makeText(getApplicationContext(), "Exporting Map as an image!", Toast.LENGTH_SHORT).show();
        }
        /**
         * save the file using a worker thread
         */
        protected File doInBackground(Bitmap... mapBitmap) {

            try {
                return saveToFile(mapBitmap[0]);
            } catch (Exception e) {
                Log.d(TAG, "Fail to export map image: " + e.getMessage());
            }

            return null;

        }

        /**
         *  Perform the work on UI thread to open the exported map image
         */
        protected void onPostExecute(File file) {
            // Open the file to view
            Intent i = new Intent();
            i.setAction(android.content.Intent.ACTION_VIEW);
            i.setDataAndType(Uri.fromFile(file), "image/png");
            startActivity(i);
        }
    }


    /**
     * save the bitmap image to file and open it
     *
     * @param bitmap
     * @throws IOException
     */
    private File saveToFile(Bitmap bitmap) throws IOException {

        // create a directory ArcGIS to save the file
        File root = null;
        String fileName = "map-export-image"+System.currentTimeMillis() + ".png";
        root = Environment.getExternalStorageDirectory();
        File fileDir = new File(root.getAbsolutePath()+"/ArcGIS/");
        fileDir.mkdirs();

        // create the file inside the directory
        File file = new File(fileDir, fileName);

        // write the bitmap to PNG file
        FileOutputStream fos = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);

        // close the stream
        fos.flush();
        fos.close();

        return file;

    }


}
