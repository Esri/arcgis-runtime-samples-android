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

package com.esri.arcgisruntime.sample.editfeatureattachments;

import java.util.List;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.ArcGISFeature;
import com.esri.arcgisruntime.data.Attachment;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.data.ServiceGeodatabase;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.GeoElement;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.Callout;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.IdentifyLayerResult;
import com.esri.arcgisruntime.mapping.view.MapView;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private static final int REQUEST_CODE = 100;
  private ProgressDialog progressDialog;
  private RelativeLayout mCalloutLayout;

  private MapView mMapView;
  private FeatureLayer mFeatureLayer;
  private ArcGISFeature mSelectedArcGISFeature;
  private Callout mCallout;
  private android.graphics.Point mTapPoint;

  private List<Attachment> attachments;
  private String mSelectedArcGISFeatureAttributeValue;
  private String mAttributeID;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // authentication with an API key or named user is required to access basemaps and other
    // location services
    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY);

    // get a reference to the map view
    mMapView = findViewById(R.id.mapView);
    // create a map with the streets basemap
    ArcGISMap map = new ArcGISMap(BasemapStyle.ARCGIS_STREETS);

    progressDialog = new ProgressDialog(this);
    progressDialog.setTitle(getApplication().getString(R.string.fetching_no_attachments));
    progressDialog.setMessage(getApplication().getString(R.string.wait));
    createCallout();
    // get callout, set content and show
    mCallout = mMapView.getCallout();

    // create and load the service geodatabase
    ServiceGeodatabase serviceGeodatabase =  new ServiceGeodatabase(getString(R.string.sample_service_url));
    serviceGeodatabase.loadAsync();
    serviceGeodatabase.addDoneLoadingListener(() -> {
      // create a feature layer using the first layer in the ServiceFeatureTable
      ServiceFeatureTable serviceFeatureTable = serviceGeodatabase.getTable(0);
      serviceFeatureTable.setFeatureRequestMode(ServiceFeatureTable.FeatureRequestMode.ON_INTERACTION_CACHE);
      // create a feature layer from table
      mFeatureLayer = new FeatureLayer(serviceFeatureTable);
      // add the layer to the map
      map.getOperationalLayers().add(mFeatureLayer);
      // set the map to be displayed in the map view
      mMapView.setMap(map);
      mMapView.setViewpoint(new Viewpoint(40.0, -95.0, 100000000));
    });

    mMapView.setOnTouchListener(new DefaultMapViewOnTouchListener(this, mMapView) {
      @Override
      public boolean onSingleTapConfirmed(MotionEvent motionEvent) {

        // get the point that was clicked and convert it to a point in map coordinates
        mTapPoint = new android.graphics.Point(Math.round(motionEvent.getX()), Math.round(motionEvent.getY()));

        // clear any previous selection
        mFeatureLayer.clearSelection();
        mSelectedArcGISFeature = null;
        mCallout.dismiss();

        // identify the GeoElements in the given layer
        final ListenableFuture<IdentifyLayerResult> futureIdentifyLayer = mMapView
            .identifyLayerAsync(mFeatureLayer, mTapPoint, 5, false, 1);

        // add done loading listener to fire when the selection returns
        futureIdentifyLayer.addDoneListener(() -> {
          try {
            // call get on the future to get the result
            IdentifyLayerResult layerResult = futureIdentifyLayer.get();
            List<GeoElement> resultGeoElements = layerResult.getElements();
            if (!resultGeoElements.isEmpty()) {
              if (resultGeoElements.get(0) instanceof ArcGISFeature) {
                progressDialog.show();
                mSelectedArcGISFeature = (ArcGISFeature) resultGeoElements.get(0);
                // highlight the selected feature
                mFeatureLayer.selectFeature(mSelectedArcGISFeature);
                mAttributeID = mSelectedArcGISFeature.getAttributes().get("objectid").toString();
                // get the number of attachments
                final ListenableFuture<List<Attachment>> attachmentResults = mSelectedArcGISFeature.fetchAttachmentsAsync();
                attachmentResults.addDoneListener(() -> {
                  try {
                    attachments = attachmentResults.get();
                    // show callout with the value for the attribute "typdamage" of the selected feature
                    mSelectedArcGISFeatureAttributeValue = (String) mSelectedArcGISFeature.getAttributes().get("typdamage");
                    if (progressDialog.isShowing()) {
                      progressDialog.dismiss();
                    }
                    showCallout(mSelectedArcGISFeatureAttributeValue, attachments.size());
                    Toast.makeText(MainActivity.this, getApplication().getString(R.string.info_button_message), Toast.LENGTH_SHORT).show();
                  } catch (Exception e) {
                    String error = "Error getting attachment results: " + e.getMessage();
                    Log.e(TAG, error);
                    Toast.makeText(MainActivity.this, error, Toast.LENGTH_LONG).show();
                  }
                });
              }
            } else {
              // none of the features on the map were selected
              mCallout.dismiss();
            }
          } catch (Exception e1) {
            Log.e(TAG, "Select feature failed: " + e1.getMessage());
          }
        });
        return super.onSingleTapConfirmed(motionEvent);
      }
    });
  }

  /**
   * Display the callout
   *
   * @param title           the damage type text
   * @param noOfAttachments attachment count of the selected feature
   */
  private void showCallout(String title, int noOfAttachments) {

    TextView calloutContent = mCalloutLayout.findViewById(R.id.calloutTextView);
    calloutContent.setText(title);

    TextView calloutAttachment = mCalloutLayout.findViewById(R.id.attachTextView);
    String attachmentText = getString(R.string.attachment_info_message) + noOfAttachments;
    calloutAttachment.setText(attachmentText);

    mCallout.setGeoElement(mSelectedArcGISFeature, null);
    mCallout.setContent(mCalloutLayout);
    mCallout.show();
  }

  /**
   * Create a Layout for callout
   */
  private void createCallout() {

    // create content text view for the callout
    mCalloutLayout = new RelativeLayout(getApplicationContext());
    TextView calloutContent = new TextView(getApplicationContext());
    calloutContent.setId(R.id.calloutTextView);
    calloutContent.setTextColor(Color.BLACK);
    calloutContent.setTextSize(18);

    RelativeLayout.LayoutParams relativeParamsBelow = new RelativeLayout.LayoutParams(
        RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
    relativeParamsBelow.addRule(RelativeLayout.BELOW, calloutContent.getId());

    // create attachment text view for the callout
    TextView calloutAttachment = new TextView(getApplicationContext());
    calloutAttachment.setId(R.id.attachTextView);
    calloutAttachment.setTextColor(Color.BLACK);
    calloutAttachment.setTextSize(13);
    calloutContent.setPadding(0, 20, 20, 0);
    calloutAttachment.setLayoutParams(relativeParamsBelow);

    RelativeLayout.LayoutParams relativeParamsRightOf = new RelativeLayout.LayoutParams(
        RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
    relativeParamsRightOf.addRule(RelativeLayout.RIGHT_OF, calloutAttachment.getId());

    // create image view for the callout
    ImageView imageView = new ImageView(getApplicationContext());
    imageView.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_info));
    imageView.setLayoutParams(relativeParamsRightOf);
    imageView.setOnClickListener(new ImageViewOnclickListener());

    mCalloutLayout.addView(calloutContent);
    mCalloutLayout.addView(imageView);
    mCalloutLayout.addView(calloutAttachment);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    // Check which request we're responding to
    if (requestCode == REQUEST_CODE) {
      int noOfAttachments = data.getExtras().getInt(getApplication().getString(R.string.noOfAttachments));
      // update the callout with attachment count
      showCallout(mSelectedArcGISFeatureAttributeValue, noOfAttachments);
    }
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

  /**
   * Defines the listener for the ImageView clicks
   */
  private class ImageViewOnclickListener implements View.OnClickListener {

    @Override
    public void onClick(View v) {
      // start EditAttachmentActivity to view/edit the attachments
      Intent myIntent = new Intent(MainActivity.this, EditAttachmentActivity.class);
      myIntent.putExtra(getString(R.string.attribute), mAttributeID);
      myIntent.putExtra(getString(R.string.noOfAttachments), attachments.size());
      Bundle bundle = new Bundle();
      startActivityForResult(myIntent, REQUEST_CODE, bundle);
    }
  }
}
