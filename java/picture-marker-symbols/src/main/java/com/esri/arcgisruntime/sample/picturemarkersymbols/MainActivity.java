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

package com.esri.arcgisruntime.sample.picturemarkersymbols;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.PictureMarkerSymbol;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();
  private final static int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 101;
  MapView mMapView;
  GraphicsOverlay mGraphicsOverlay;
  String mArcGISTempFolderPath;
  String mPinBlankOrangeFilePath;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // authentication with an API key or named user is required to access basemaps and other
    // location services
    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY);

    // inflate MapView from layout
    mMapView = findViewById(R.id.mapView);

    // create a map with the imagery basemap
    ArcGISMap map = new ArcGISMap(BasemapStyle.ARCGIS_TOPOGRAPHIC);

    // set the map to be displayed in the mapview
    mMapView.setMap(map);

    // create an initial viewpoint using an envelope (of two points, bottom left and top right)
    Envelope envelope = new Envelope(new Point(-228835, 6550763, SpatialReferences.getWebMercator()),
        new Point(-223560, 6552021, SpatialReferences.getWebMercator()));
    //set viewpoint on map view
    mMapView.setViewpointGeometryAsync(envelope, 100.0);

    // create a new graphics overlay and add it to the mapview
    mGraphicsOverlay = new GraphicsOverlay();
    mMapView.getGraphicsOverlays().add(mGraphicsOverlay);

    //[DocRef: Name=Picture Marker Symbol URL, Category=Fundamentals, Topic=Symbols and Renderers]
    //Create a picture marker symbol from a URL resource
    //When using a URL, you need to call load to fetch the remote resource
    PictureMarkerSymbol campsiteSymbol = new PictureMarkerSymbol(
        "http://sampleserver6.arcgisonline.com/arcgis/rest/services/Recreation/FeatureServer/0/images/e82f744ebb069bb35b234b3fea46deae");
    //Optionally set the size, if not set the image will be auto sized based on its size in pixels,
    //its appearance would then differ across devices with different resolutions.
    campsiteSymbol.setHeight(18);
    campsiteSymbol.setWidth(18);
    //[DocRef: END]
    // add a new graphic to the graphic overlay
    Point campsitePoint = new Point(-223560, 6552021, SpatialReferences.getWebMercator());
    Graphic campsiteGraphic = new Graphic(campsitePoint, campsiteSymbol);
    mGraphicsOverlay.getGraphics().add(campsiteGraphic);

    //[DocRef: Name=Picture Marker Symbol Drawable-android, Category=Fundamentals, Topic=Symbols and Renderers]
    //Create a picture marker symbol from an app resource
    BitmapDrawable pinStarBlueDrawable = (BitmapDrawable) ContextCompat.getDrawable(this, R.drawable.pin_star_blue);
    ListenableFuture<PictureMarkerSymbol> pinStarBlueSymbolFuture = PictureMarkerSymbol.createAsync(pinStarBlueDrawable);

    pinStarBlueSymbolFuture.addDoneListener(() -> {

      try {
      PictureMarkerSymbol pinStarBlueSymbol = pinStarBlueSymbolFuture.get();

      //Optionally set the size, if not set the image will be auto sized based on its size in pixels,
      //its appearance would then differ across devices with different resolutions.
      pinStarBlueSymbol.setHeight(40);
      pinStarBlueSymbol.setWidth(40);
      //Optionally set the offset, to align the base of the symbol aligns with the point geometry
      pinStarBlueSymbol.setOffsetY(11); //The image used for the symbol has a transparent buffer around it, so the offset is not simply height/2
      pinStarBlueSymbol.loadAsync();
      //[DocRef: END]
      //add a new graphic with the same location as the initial viewpoint
      Point pinStarBluePoint = new Point(-226773, 6550477, SpatialReferences.getWebMercator());
      Graphic pinStarBlueGraphic = new Graphic(pinStarBluePoint, pinStarBlueSymbol);
      mGraphicsOverlay.getGraphics().add(pinStarBlueGraphic);

    createPictureMarkerSymbolFromFile();

      } catch (Exception e) {
        String error = "Error loading picture marker symbol: " + e.getMessage();
        Log.e(TAG, error);
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
      }
    });



  }

  /**
   * Create a picture marker symbol from an image on disk. Called from checkSaveResourceToExternalStorage() or
   * onRequestPermissionsResult which validate required external storage and permissions
   */
  private void createPictureMarkerSymbolFromFile() {

    //[DocRef: Name=Picture Marker Symbol File-android, Category=Fundamentals, Topic=Symbols and Renderers]
    //Create a picture marker symbol from a file on disk
    BitmapDrawable pinBlankOrangeDrawable = (BitmapDrawable) Drawable.createFromPath(mPinBlankOrangeFilePath);
    PictureMarkerSymbol pinBlankOrangeSymbol = new PictureMarkerSymbol(pinBlankOrangeDrawable);
    //Optionally set the size, if not set the image will be auto sized based on its size in pixels,
    //its appearance would then differ across devices with different resolutions.
    pinBlankOrangeSymbol.setHeight(20);
    pinBlankOrangeSymbol.setWidth(20);
    //Optionally set the offset, to align the base of the symbol aligns with the point geometry
    pinBlankOrangeSymbol.setOffsetY(10); //The image used has not buffer and therefore the Y offset is height/2
    pinBlankOrangeSymbol.loadAsync();
    //[DocRef: END]

    //add a new graphic with the same location as the initial viewpoint
    Point pinBlankOrangePoint = new Point(-228835, 6550763, SpatialReferences.getWebMercator());
    Graphic pinBlankOrangeGraphic = new Graphic(pinBlankOrangePoint, pinBlankOrangeSymbol);
    mGraphicsOverlay.getGraphics().add(pinBlankOrangeGraphic);

  }

  private boolean saveFileToExternalStorage() {

    //build paths
    mArcGISTempFolderPath = getExternalFilesDir(null) + File.separator + getResources()
        .getString(R.string.pin_blank_orange_folder_name);
    mPinBlankOrangeFilePath =
        mArcGISTempFolderPath + File.separator + getResources().getString(R.string.pin_blank_orange_file_name);

    //get drawable resource
    Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.pin_blank_orange);

    //create new ArcGIS temp folder
    File folder = new File(mArcGISTempFolderPath);
    if (folder.mkdirs()) {
      Log.d(TAG, "Temp folder created");
    } else {
      Toast.makeText(this, "Could not create temp folder", Toast.LENGTH_LONG).show();
    }

    //create file on disk
    File file = new File(mPinBlankOrangeFilePath);

    try {
      OutputStream outStream = new FileOutputStream(file);
      bm.compress(Bitmap.CompressFormat.PNG, 100, outStream);
      outStream.flush();
      outStream.close();

      return true;

    } catch (Exception e) {
      Log.e("picture-marker-symbol", "Failed to write image to external directory: message = " + e.getMessage());
      return false;
    }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    // dispose MapView
    mMapView.dispose();

    //Clean up file and folders we saved to disk
    try {
      File file = new File(mPinBlankOrangeFilePath);

      if (file.delete()) {
        Log.i(TAG, "Temp folder created");
      } else {
        Toast.makeText(this, "Could not create temp folder", Toast.LENGTH_LONG).show();
      }

      File tempFolder = new File(mArcGISTempFolderPath);

      if (tempFolder.delete()) {
        Log.i(TAG, "Temp folder created");
      } else {
        Toast.makeText(this, "Could not create temp folder", Toast.LENGTH_LONG).show();
      }

    } catch (Exception e) {
      Log.e("picture-marker-symbol",
          "Failed to delete temp files and directory written to external storage: message = " + e.getMessage());
    }
  }

  @Override
  protected void onPause() {
    super.onPause();
    // pause MapView
    mMapView.pause();
  }

  @Override
  protected void onResume() {
    super.onResume();
    // resume MapView
    mMapView.resume();
  }
}
