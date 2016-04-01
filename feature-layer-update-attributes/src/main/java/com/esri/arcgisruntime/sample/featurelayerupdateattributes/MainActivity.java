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

import java.util.List;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.datasource.arcgis.ArcGISFeature;
import com.esri.arcgisruntime.datasource.arcgis.FeatureEditResult;
import com.esri.arcgisruntime.datasource.arcgis.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.GeoElement;
import com.esri.arcgisruntime.mapping.Map;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.Callout;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.IdentifyLayerResult;
import com.esri.arcgisruntime.mapping.view.MapView;

public class MainActivity extends AppCompatActivity {

  private MapView mMapView;
  private Callout mCallout;
  private FeatureLayer mFeatureLayer;
  private ArcGISFeature mResultArcGISFeature;
  private android.graphics.Point mClickPoint;
  private ServiceFeatureTable mServiceFeatureTable;

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
    mServiceFeatureTable = new ServiceFeatureTable(getResources().getString(R.string.sample_service_url));
    // create the feature layer using the service feature table
    mFeatureLayer = new FeatureLayer(mServiceFeatureTable);
    mFeatureLayer.setSelectionColor(Color.rgb(0, 255, 255)); //cyan, fully opaque
    mFeatureLayer.setSelectionWidth(3);
    // add the layer to the map
    map.getOperationalLayers().add(mFeatureLayer);

    // set an on touch listener to listen for click events
    mMapView.setOnTouchListener(new DefaultMapViewOnTouchListener(this, mMapView) {
      @Override
      public boolean onSingleTapConfirmed(MotionEvent e) {

        // get the point that was clicked and convert it to a point in map coordinates
        mClickPoint = new android.graphics.Point((int) e.getX(), (int) e.getY());


        // call identifylayerAsync

        final ListenableFuture<IdentifyLayerResult> future = mMapView.identifyLayerAsync(mFeatureLayer, mClickPoint, 5, 1);


        // add done loading listener to fire when the selection returns
        future.addDoneListener(new Runnable() {
          @Override
          public void run() {
            try {
              //call get on the future to get the result
              IdentifyLayerResult result = future.get();

              List<GeoElement> resultGeoElements = result.getIdentifiedElements();
              mResultArcGISFeature = (ArcGISFeature) resultGeoElements.get(0);
              String title = (String) mResultArcGISFeature.getAttributes().get("typdamage");
              showCallout(title);

//              Toast.makeText(getApplicationContext(), i + " features selected", Toast.LENGTH_SHORT).show();



            } catch (Exception e) {
              Log.e(getResources().getString(R.string.app_name), "Select feature failed: " + e.getMessage());
            }
          }
        });
        return super.onSingleTapConfirmed(e);
      }
    });


  }

  // Function to read the result from newly created activity
  @Override
  protected void onActivityResult(int requestCode,
      int resultCode, final Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if(resultCode == 100){

      // Storing result in a variable called myvar
      // get("website") 'website' is the key value result data
//      String mywebsite = data.getExtras().get("result");

      mResultArcGISFeature.loadAsync();
      mResultArcGISFeature.addDoneLoadingListener(new Runnable() {
        @Override public void run() {
          if (mResultArcGISFeature.getLoadStatus() == LoadStatus.FAILED_TO_LOAD) {
            Log.d("MainActivity", "Error while loading feature");
          }
          mResultArcGISFeature.getAttributes().put("typdamage", data.getStringExtra("typdamage"));
          final ListenableFuture<Boolean> updateFeatureFuture = mServiceFeatureTable.updateFeatureAsync(mResultArcGISFeature);
          updateFeatureFuture.addDoneListener(new Runnable() {
            @Override public void run() {
              try {
                if (updateFeatureFuture.get()) {
                  final ListenableFuture<List<FeatureEditResult>> serverResult = mServiceFeatureTable.applyEditsAsync();
                  serverResult.addDoneListener(new Runnable() {
                    @Override public void run() {
                      try {
                        List<FeatureEditResult> edits = serverResult.get();
                        if (edits.size() > 0) {
                          if (!edits.get(0).hasCompletedWithErrors()) {
                            Log.e("Main Activity","Feature successfully updated");
                          }
                        } else {
                          Log.e( "Main Activity","The attribute type was not changed");
                        }

                        showCallout((String) mResultArcGISFeature.getAttributes().get("typdamage"));
                      }catch (Exception e) {
                        Log.e(getResources().getString(R.string.app_name), "Select feature failed: " + e.getMessage());
                      }

                    }
                  });
                }

              } catch (Exception e) {
              Log.e(getResources().getString(R.string.app_name), "Select feature failed: " + e.getMessage());
            }
            }
          });
        }
      });
    }
  }

  public void showCallout(String title){

    // create a textview for the callout
    RelativeLayout calloutLayout = new RelativeLayout(getApplicationContext());
    RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(
        RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
    //              relativeParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);

    TextView calloutContent = new TextView(getApplicationContext());
    calloutContent.setId(R.id.textview);
    calloutContent.setTextColor(Color.BLACK);
    //              calloutContent.setSingleLine();
    calloutContent.setText(title);
    //              calloutContent.setText("X:" +  (String.format("%.2f", clickPoint.getX()))
    //                  + ", y:" + (String.format("%.2f", clickPoint.getY())));

    relativeParams.addRule(RelativeLayout.RIGHT_OF, calloutContent.getId());

    ImageView imageView = new ImageView(getApplicationContext());
    imageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_info_outline_black_18dp));
    imageView.setLayoutParams(relativeParams);
    imageView.setOnClickListener(new ImageViewOnclickListener());

    calloutLayout.addView(calloutContent);
    calloutLayout.addView(imageView);

    mCallout.setLocation(mMapView.screenToLocation(mClickPoint));
    mCallout.setContent(calloutLayout);
    mCallout.show();

  }

  class ImageViewOnclickListener implements View.OnClickListener {

    @Override public void onClick(View v) {
      Log.e("imageview", "tap");
      Intent myIntent = new Intent(MainActivity.this, DamageTypesListActivity.class);
      //                  myIntent.putExtra("key", value); //Optional parameters
      MainActivity.this.startActivityForResult(myIntent, 100);

    }
  }

}
