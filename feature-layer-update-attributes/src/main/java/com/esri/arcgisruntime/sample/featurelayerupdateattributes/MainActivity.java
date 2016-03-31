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

package com.esri.arcgisruntime.sample.featurelayerupdateattributes;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.datasource.FeatureQueryResult;
import com.esri.arcgisruntime.datasource.QueryParameters;
import com.esri.arcgisruntime.datasource.arcgis.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Map;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.Callout;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.MapView;

public class MainActivity extends AppCompatActivity {

  private MapView mMapView;
  private Callout mCallout;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // inflate MapView from layout
    mMapView = (MapView) findViewById(R.id.mapView);

    // create a map with the streets basemap
    final Map map = new Map(Basemap.createStreets());
    //set an initial viewpointf
    map.setInitialViewpoint(new Viewpoint(new Point(544871.19, 6806138.66, SpatialReferences
        .getWebMercator()), 2E6));
    // set the map to be displayed in the mapview
    mMapView.setMap(map);

    // get callout, set content and show
    mCallout = mMapView.getCallout();

    // create feature layer with its service feature table
    // create the service feature table
    final ServiceFeatureTable serviceFeatureTable = new ServiceFeatureTable(getResources().getString(R.string.sample_service_url));
    // create the feature layer using the service feature table
    final FeatureLayer featureLayer = new FeatureLayer(serviceFeatureTable);
    featureLayer.setSelectionColor(Color.rgb(0, 255, 255)); //cyan, fully opaque
    featureLayer.setSelectionWidth(3);
    // add the layer to the map
    map.getOperationalLayers().add(featureLayer);

    // set an on touch listener to listen for click events
    mMapView.setOnTouchListener(new DefaultMapViewOnTouchListener(this, mMapView) {
      @Override
      public boolean onSingleTapConfirmed(MotionEvent e) {

        // get the point that was clicked and convert it to a point in map coordinates
        final Point clickPoint = mMapView.screenToLocation(new android.graphics.Point((int) e.getX(), (int) e.getY()));
        int tolerance = 5;
        double mapTolerance = tolerance * mMapView.getUnitsPerPixel();

        // create objects required to do a selection with a query
        Envelope envelope = new Envelope(clickPoint.getX() - mapTolerance, clickPoint.getY() - mapTolerance, clickPoint.getX() + mapTolerance, clickPoint.getY() + mapTolerance, map.getSpatialReference());
        QueryParameters query = new QueryParameters();
        query.setGeometry(envelope);
        query.setMaxFeatures(1);

        // call select features
        final ListenableFuture<FeatureQueryResult> future = featureLayer.selectFeatures(query, FeatureLayer.SelectionMode.NEW);
        // add done loading listener to fire when the selection returns
        future.addDoneListener(new Runnable() {
          @Override
          public void run() {
            try {
              //call get on the future to get the result
              FeatureQueryResult result = future.get();

              String title = null;

              //find out how many items there are in the result
              int i = 0;
              for (; result.iterator().hasNext(); ++i) {
                title = (String) result.iterator().next().getAttributes().get("typdamage");
              }
              Toast.makeText(getApplicationContext(), i + " features selected", Toast.LENGTH_SHORT).show();

              // create a textview for the callout
              RelativeLayout calloutLayout = new RelativeLayout(getApplicationContext());
              RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(
                  RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
              relativeParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);

              TextView calloutContent = new TextView(getApplicationContext());
              calloutContent.setId(R.id.textview);
              calloutContent.setTextColor(Color.BLACK);
              calloutContent.setSingleLine();
              calloutContent.setText(title);
//              calloutContent.setText("X:" +  (String.format("%.2f", clickPoint.getX()))
//                  + ", y:" + (String.format("%.2f", clickPoint.getY())));

              relativeParams.addRule(RelativeLayout.RIGHT_OF, calloutContent.getId());

              ImageView imageView = new ImageView(getApplicationContext());
              imageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_info_outline_black_18dp));

              calloutLayout.addView(calloutContent);
              calloutLayout.addView(imageView);

              mCallout.setLocation(clickPoint);
              mCallout.setContent(calloutLayout);
              mCallout.show();

            } catch (Exception e) {
              Log.e(getResources().getString(R.string.app_name), "Select feature failed: " + e.getMessage());
            }
          }
        });
        return super.onSingleTapConfirmed(e);
      }
    });


  }
}
