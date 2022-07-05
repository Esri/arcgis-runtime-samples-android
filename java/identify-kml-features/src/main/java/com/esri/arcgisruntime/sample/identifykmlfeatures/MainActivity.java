/*
 *  Copyright 2019 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.esri.arcgisruntime.sample.identifykmlfeatures;

import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.KmlLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.GeoElement;
import com.esri.arcgisruntime.mapping.view.Callout;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.IdentifyLayerResult;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.ogc.kml.KmlDataset;
import com.esri.arcgisruntime.ogc.kml.KmlPlacemark;

import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private MapView mMapView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // authentication with an API key or named user is required to access basemaps and other
    // location services
    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY);

    // create a map and add it to the map view
    ArcGISMap map = new ArcGISMap(BasemapStyle.ARCGIS_DARK_GRAY);
    mMapView = findViewById(R.id.mapView);
    mMapView.setMap(map);

    // start zoomed in over the US
    mMapView.setViewpointGeometryAsync(
        new Envelope(-19195297.778679, 512343.939994, -3620418.579987, 8658913.035426, 0.0, 0.0,
            SpatialReferences.getWebMercator()));

    // create a KML dataset of weather forecasts
    KmlDataset forecastKmlDataset = new KmlDataset("https://www.wpc.ncep.noaa.gov/kml/noaa_chart/WPC_Day1_SigWx_latest.kml");

    // create a KML layer and add it as an operational layer
    KmlLayer forecastKmlLayer = new KmlLayer(forecastKmlDataset);
    map.getOperationalLayers().add(forecastKmlLayer);

    // add a click listener to identify clicked features
    mMapView.setOnTouchListener(new DefaultMapViewOnTouchListener(this, mMapView) {

      @Override public boolean onSingleTapConfirmed(MotionEvent e) {
        // hide the callout if it's showing
        mMapView.getCallout().dismiss();

        // get the identified geoelements at the clicked location
        android.graphics.Point screenPoint = new android.graphics.Point(Math.round(e.getX()), Math.round(e.getY()));
        ListenableFuture<IdentifyLayerResult> identify = mMapView
            .identifyLayerAsync(forecastKmlLayer, screenPoint, 5, false);
        identify.addDoneListener(() -> {
          try {
            IdentifyLayerResult result = identify.get();
            // find the first geoElement that is a KML placemark
            for (GeoElement geoElement : result.getElements()) {
              if (geoElement instanceof KmlPlacemark) {
                // show a callout at the placemark with custom content using the placemark's "balloon content"
                KmlPlacemark placemark = (KmlPlacemark) geoElement;
                // Google Earth only displays the placemarks with description or extended data. To
                // match its behavior, add a description placeholder if the data source is empty
                if (placemark.getDescription().isEmpty()) {
                  placemark.setDescription("Weather condition");
                }
                TextView calloutContent = new TextView(getApplicationContext());
                calloutContent.setText(Html.fromHtml(placemark.getBalloonContent()));

                // get callout, set content and show
                Callout callout = mMapView.getCallout();
                callout.setLocation(mMapView.screenToLocation(screenPoint));
                callout.setContent(calloutContent);
                callout.show();
                break;
              }
            }
          } catch (InterruptedException | ExecutionException ex) {
            String error = "Error identifying features in layer: " + ex.getMessage();
            Toast.makeText(MainActivity.this, error, Toast.LENGTH_LONG).show();
            Log.e(TAG, error);
          }
        });
        return true;
      }
    });
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

  @Override
  protected void onDestroy() {
    mMapView.dispose();
    super.onDestroy();
  }
}
