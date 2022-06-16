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

        // get a reference to the map view
        mMapView = findViewById(R.id.mapView);

        // create a map with the imagery basemap
        ArcGISMap map = new ArcGISMap(BasemapStyle.ARCGIS_TOPOGRAPHIC);

        // set the map to be displayed in the mapview
        mMapView.setMap(map);

        // create an initial viewpoint using an envelope (of two points, bottom left and top right)
        Envelope envelope = new Envelope(new Point(-228835, 6550763, SpatialReferences.getWebMercator()),
                new Point(-223560, 6552021, SpatialReferences.getWebMercator()));
        // set viewpoint on map view
        mMapView.setViewpointGeometryAsync(envelope, 100.0);

        // create a new graphics overlay and add it to the mapview
        mGraphicsOverlay = new GraphicsOverlay();
        mMapView.getGraphicsOverlays().add(mGraphicsOverlay);

        createPictureMarkerSymbolFromURL();

        createPictureMarkerSymbolFromDrawable();
    }

    /**
     * Create a picture marker symbol from a URL. Set it's height and width, and add use it to
     * create a graphic.
     */
    private void createPictureMarkerSymbolFromURL() {
        // [DocRef: Name=Picture Marker Symbol URL, Category=Fundamentals, Topic=Symbols and Renderers]
        // create a picture marker symbol from a URL resource
        PictureMarkerSymbol campsiteSymbol = new PictureMarkerSymbol(
                "http://sampleserver6.arcgisonline.com/arcgis/rest/services/Recreation/FeatureServer/0/images/e82f744ebb069bb35b234b3fea46deae");
        // set the size, if not set the image will be auto sized based on its size in pixels
        campsiteSymbol.setHeight(40);
        campsiteSymbol.setWidth(40);
        // [DocRef: END]
        // add a new graphic to the graphic overlay
        Point campsitePoint = new Point(-223560, 6552021, SpatialReferences.getWebMercator());
        Graphic campsiteGraphic = new Graphic(campsitePoint, campsiteSymbol);
        mGraphicsOverlay.getGraphics().add(campsiteGraphic);
    }

    /**
     * Create a picture marker symbol from a Bitmap Drawable. Set it's height and width, and add use
     * it to create a graphic.
     */
    private void createPictureMarkerSymbolFromDrawable() {
        // [DocRef: Name=Picture Marker Symbol Drawable-android, Category=Fundamentals, Topic=Symbols and Renderers]
        // create a picture marker symbol from an app resource
        BitmapDrawable pinStarBlueDrawable = (BitmapDrawable) ContextCompat.getDrawable(this, R.drawable.pin_star_blue);
        if (pinStarBlueDrawable != null) {
            // create the picture marker symbol asynchronously
            ListenableFuture<PictureMarkerSymbol> pinStarBlueSymbolFuture = PictureMarkerSymbol.createAsync(pinStarBlueDrawable);
            // listen for the create to finish
            pinStarBlueSymbolFuture.addDoneListener(() -> {
                try {
                    // get the created picture marker symbol
                    PictureMarkerSymbol pinStarBlueSymbol = pinStarBlueSymbolFuture.get();

                    // set the size, if not set the image will be auto sized based on its size in pixels
                    pinStarBlueSymbol.setHeight(60);
                    pinStarBlueSymbol.setWidth(60);
                    // set the offset, to align the base of the symbol aligns with the point geometry
                    pinStarBlueSymbol.setOffsetY(30);
                    //[DocRef: END]
                    // add a new graphic with the same location as the initial viewpoint
                    Point pinStarBluePoint = new Point(-226773, 6550477, SpatialReferences.getWebMercator());
                    Graphic pinStarBlueGraphic = new Graphic(pinStarBluePoint, pinStarBlueSymbol);
                    mGraphicsOverlay.getGraphics().add(pinStarBlueGraphic);
                } catch (Exception e) {
                    String error = "Error loading picture marker symbol: " + e.getMessage();
                    Log.e(TAG, error);
                    Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        mMapView.dispose();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        mMapView.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.resume();
    }
}
