/*
 * Copyright 2016 Esri
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

package com.esri.arcgisruntime.sample.featurelayershowattributes;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.GeoElement;
import com.esri.arcgisruntime.mapping.view.Callout;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.IdentifyLayerResult;
import com.esri.arcgisruntime.mapping.view.MapView;

public class MainActivity extends AppCompatActivity {

  private MapView mMapView;
  private Callout mCallout;

  private ServiceFeatureTable mServiceFeatureTable;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // inflate MapView from layout
    mMapView = (MapView) findViewById(R.id.mapView);
    // create an ArcGISMap with BasemapType topo
    final ArcGISMap map = new ArcGISMap(Basemap.Type.TOPOGRAPHIC, 34.057386, -117.191455, 14);
    // set the ArcGISMap to the MapView
    mMapView.setMap(map);
    // get the callout that shows attributes
    mCallout = mMapView.getCallout();
    // create the service feature table
    mServiceFeatureTable = new ServiceFeatureTable(getResources().getString(R.string.sample_service_url));
    // create the feature layer using the service feature table
    final FeatureLayer featureLayer = new FeatureLayer(mServiceFeatureTable);
    // add the layer to the map
    map.getOperationalLayers().add(featureLayer);

    // set an on touch listener to listen for click events
    mMapView.setOnTouchListener(new DefaultMapViewOnTouchListener(this, mMapView) {
      @Override
      public boolean onSingleTapConfirmed(MotionEvent e) {
        // remove any existing callouts
        if (mCallout.isShowing()) {
          mCallout.dismiss();
        }
        // get the point that was clicked and convert it to a point in map coordinates
        final Point screenPoint = new Point(Math.round(e.getX()), Math.round(e.getY()));
        // create a selection tolerance
        int tolerance = 10;
        // use identifyLayerAsync to get tapped features
        final ListenableFuture<IdentifyLayerResult> identifyLayerResultListenableFuture = mMapView
            .identifyLayerAsync(featureLayer, screenPoint, tolerance, false, 1);
        identifyLayerResultListenableFuture.addDoneListener(new Runnable() {
          @Override public void run() {
            try {
              IdentifyLayerResult identifyLayerResult = identifyLayerResultListenableFuture.get();
              // create a textview to display field values
              TextView calloutContent = new TextView(getApplicationContext());
              calloutContent.setTextColor(Color.BLACK);
              calloutContent.setSingleLine(false);
              calloutContent.setVerticalScrollBarEnabled(true);
              calloutContent.setScrollBarStyle(View.SCROLLBARS_INSIDE_INSET);
              calloutContent.setMovementMethod(new ScrollingMovementMethod());
              calloutContent.setLines(5);
              for (GeoElement element : identifyLayerResult.getElements()) {
                Feature feature = (Feature) element;
                // create a map of all available attributes as name value pairs
                Map<String, Object> attr = feature.getAttributes();
                Set<String> keys = attr.keySet();
                for (String key : keys) {
                  Object value = attr.get(key);
                  // format observed field value as date
                  if (value instanceof GregorianCalendar) {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMM-yyyy", Locale.US);
                    value = simpleDateFormat.format(((GregorianCalendar) value).getTime());
                  }
                  // append name value pairs to text view
                  calloutContent.append(key + " | " + value + "\n");
                }
                // center the mapview on selected feature
                Envelope envelope = feature.getGeometry().getExtent();
                mMapView.setViewpointGeometryAsync(envelope, 200);
                // show callout
                mCallout.setLocation(envelope.getCenter());
                mCallout.setContent(calloutContent);
                mCallout.show();
              }
            } catch (Exception e) {
              Log.e(getResources().getString(R.string.app_name), "Select feature failed: " + e.getMessage());
            }
          }
        });
        return super.onSingleTapConfirmed(e);
      }
    });

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
}
