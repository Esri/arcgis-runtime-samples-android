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

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.ArcGISFeature;
import com.esri.arcgisruntime.data.FeatureEditResult;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.GeoElement;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.Callout;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.IdentifyLayerResult;
import com.esri.arcgisruntime.mapping.view.MapView;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private Callout mCallout;
  private ServiceFeatureTable mServiceFeatureTable;
  private FeatureLayer mFeatureLayer;
  private ArcGISFeature mSelectedArcGISFeature;
  private MapView mMapView;
  private android.graphics.Point mClickPoint;

  private Snackbar mSnackbarSuccess;
  private Snackbar mSnackbarFailure;
  private String mSelectedArcGISFeatureAttributeValue;
  private boolean mFeatureUpdated;
  private View mCoordinatorLayout;
  private ProgressDialog mProgressDialog;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // authentication with an API key or named user is required to access basemaps and other
    // location services
    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY);

    mCoordinatorLayout = findViewById(R.id.snackbarPosition);

    // get a reference to the map view
    mMapView = findViewById(R.id.mapView);

    // create a map with the streets basemap
    final ArcGISMap map = new ArcGISMap(Basemap.createStreets());

    //set an initial viewpoint
    map.setInitialViewpoint(new Viewpoint(new Point(-100.343, 34.585, SpatialReferences.getWgs84()), 1E8));

    // set the map to be displayed in the map view
    mMapView.setMap(map);

    // get callout, set content and show
    mCallout = mMapView.getCallout();

    mProgressDialog = new ProgressDialog(this);
    mProgressDialog.setTitle(getResources().getString(R.string.progress_title));
    mProgressDialog.setMessage(getResources().getString(R.string.progress_message));

    // create feature layer with from the service feature table
    mServiceFeatureTable = new ServiceFeatureTable(getResources().getString(R.string.sample_service_url));
    mFeatureLayer = new FeatureLayer(mServiceFeatureTable);

    // add the layer to the map
    map.getOperationalLayers().add(mFeatureLayer);

    // set an on touch listener to listen for click events
    mMapView.setOnTouchListener(new DefaultMapViewOnTouchListener(this, mMapView) {
      @Override
      public boolean onSingleTapConfirmed(MotionEvent e) {

        // get the point that was clicked and convert it to a point in map coordinates
        mClickPoint = new android.graphics.Point((int) e.getX(), (int) e.getY());

        // clear any previous selection
        mFeatureLayer.clearSelection();
        mSelectedArcGISFeature = null;
        mCallout.dismiss();

        // identify the GeoElements in the given layer
        final ListenableFuture<IdentifyLayerResult> identifyFuture = mMapView
            .identifyLayerAsync(mFeatureLayer, mClickPoint, 5, false, 1);

        // add done loading listener to fire when the selection returns
        identifyFuture.addDoneListener(new Runnable() {
          @Override
          public void run() {
            try {
              // call get on the future to get the result
              IdentifyLayerResult layerResult = identifyFuture.get();
              List<GeoElement> resultGeoElements = layerResult.getElements();
              if (!resultGeoElements.isEmpty()) {
                if (resultGeoElements.get(0) instanceof ArcGISFeature) {
                  mSelectedArcGISFeature = (ArcGISFeature) resultGeoElements.get(0);
                  // highlight the selected feature
                  mFeatureLayer.selectFeature(mSelectedArcGISFeature);
                  // show callout with the value for the attribute "typdamage" of the selected feature
                  mSelectedArcGISFeatureAttributeValue = (String) mSelectedArcGISFeature.getAttributes()
                      .get("typdamage");
                  showCallout(mSelectedArcGISFeatureAttributeValue);
                  Toast.makeText(MainActivity.this, "Tap on the info button to change attribute value",
                      Toast.LENGTH_SHORT).show();
                }
              } else {
                // none of the features on the map were selected
                mCallout.dismiss();
              }
            } catch (Exception e) {
              Log.e(TAG, "Select feature failed: " + e.getMessage());
            }
          }
        });
        return super.onSingleTapConfirmed(e);
      }
    });

    mSnackbarSuccess = Snackbar
        .make(mCoordinatorLayout, "Feature successfully updated", Snackbar.LENGTH_LONG)
        .setAction("UNDO", new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            String snackBarText = updateAttributes(mSelectedArcGISFeatureAttributeValue) ?
                "Feature is restored!" :
                "Feature restore failed!";
            Snackbar snackbar1 = Snackbar.make(mCoordinatorLayout, snackBarText, Snackbar.LENGTH_SHORT);
            snackbar1.show();
          }
        });
    mSnackbarFailure = Snackbar.make(mCoordinatorLayout, "Feature update failed", Snackbar.LENGTH_LONG);
  }

  /**
   * Function to read the result from newly created activity
   */
  @Override
  protected void onActivityResult(int requestCode,
      int resultCode, final Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (resultCode == 100) {
      // display progress dialog while updating attribute callout
      mProgressDialog.show();
      updateAttributes(data.getStringExtra("typdamage"));
    }
  }

  /**
   * Applies changes to the feature, Service Feature Table, and server.
   */
  private boolean updateAttributes(final String typeDamage) {

    // load the selected feature
    mSelectedArcGISFeature.loadAsync();

    // update the selected feature
    mSelectedArcGISFeature.addDoneLoadingListener(new Runnable() {
      @Override public void run() {
        if (mSelectedArcGISFeature.getLoadStatus() == LoadStatus.FAILED_TO_LOAD) {
          Log.e(TAG, "Error while loading feature");
        }

        // update the Attributes map with the new selected value for "typdamage"
        mSelectedArcGISFeature.getAttributes().put("typdamage", typeDamage);

        try {
          // update feature in the feature table
          ListenableFuture<Void> mapViewResult = mServiceFeatureTable.updateFeatureAsync(mSelectedArcGISFeature);
          /*mServiceFeatureTable.updateFeatureAsync(mSelectedArcGISFeature).addDoneListener(new Runnable() {*/
          mapViewResult.addDoneListener(new Runnable() {
            @Override
            public void run() {
              // apply change to the server
              final ListenableFuture<List<FeatureEditResult>> serverResult = mServiceFeatureTable.applyEditsAsync();

              serverResult.addDoneListener(new Runnable() {
                @Override
                public void run() {
                  try {

                    // check if server result successful
                    List<FeatureEditResult> edits = serverResult.get();
                    if (!edits.isEmpty()) {
                      if (!edits.get(0).hasCompletedWithErrors()) {
                        Log.e(TAG, "Feature successfully updated");
                        mSnackbarSuccess.show();
                        mFeatureUpdated = true;
                      }
                    } else {
                      Log.e(TAG, "The attribute type was not changed");
                      mSnackbarFailure.show();
                      mFeatureUpdated = false;
                    }
                    if (mProgressDialog.isShowing()) {
                      mProgressDialog.dismiss();
                      // display the callout with the updated value
                      showCallout((String) mSelectedArcGISFeature.getAttributes().get("typdamage"));
                    }
                  } catch (Exception e) {
                    Log.e(TAG, "applying changes to the server failed: " + e.getMessage());
                  }
                }
              });
            }
          });
        } catch (Exception e) {
          Log.e(TAG, "updating feature in the feature table failed: " + e.getMessage());
        }
      }
    });
    return mFeatureUpdated;
  }

  /**
   * Displays Callout
   *
   * @param title the text to show in the Callout
   */
  private void showCallout(String title) {

    // create a text view for the callout
    RelativeLayout calloutLayout = new RelativeLayout(getApplicationContext());

    TextView calloutContent = new TextView(getApplicationContext());
    calloutContent.setId(R.id.textview);
    calloutContent.setTextColor(Color.BLACK);
    calloutContent.setTextSize(18);
    calloutContent.setPadding(0, 10, 10, 0);

    calloutContent.setText(title);

    RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(
        RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
    relativeParams.addRule(RelativeLayout.RIGHT_OF, calloutContent.getId());

    // create image view for the callout
    ImageView imageView = new ImageView(getApplicationContext());
    imageView
        .setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_info_outline_black_18dp));
    imageView.setLayoutParams(relativeParams);
    imageView.setOnClickListener(new ImageViewOnclickListener());

    calloutLayout.addView(calloutContent);
    calloutLayout.addView(imageView);

    mCallout.setGeoElement(mSelectedArcGISFeature, null);
    mCallout.setContent(calloutLayout);
    mCallout.show();
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

    @Override public void onClick(View v) {
      Intent myIntent = new Intent(MainActivity.this, DamageTypesListActivity.class);
      startActivityForResult(myIntent, 100);
    }
  }
}
