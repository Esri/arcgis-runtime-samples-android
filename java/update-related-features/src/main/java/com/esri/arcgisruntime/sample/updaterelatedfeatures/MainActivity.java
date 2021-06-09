/* Copyright 2017 Esri
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

package com.esri.arcgisruntime.sample.updaterelatedfeatures;

import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.ArcGISFeature;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureEditResult;
import com.esri.arcgisruntime.data.RelatedFeatureQueryResult;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.Callout;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.IdentifyLayerResult;
import com.esri.arcgisruntime.mapping.view.MapView;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private MapView mMapView;
  private ServiceFeatureTable mParksFeatureTable;
  private FeatureLayer mParksFeatureLayer;
  private ArcGISFeature mSelectedArcGISFeature;
  private ServiceFeatureTable mPreservesFeatureTable;
  private ArcGISFeature mSelectedRelatedFeature;
  private Point mTappedPoint;
  private Callout mCallout;

  private String mAttributeValue;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // authentication with an API key or named user is required to access basemaps and other
    // location services
    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY);

    // create MapView from layout
    mMapView = findViewById(R.id.mapView);

    // create a map and set the map view viewpoint
    ArcGISMap map = new ArcGISMap(BasemapStyle.ARCGIS_TOPOGRAPHIC);
    mMapView.setMap(map);
    mMapView.setViewpoint(new Viewpoint(65.399121, -151.521682, 50000000));

    // get callout and set style
    mCallout = mMapView.getCallout();
    Callout.Style calloutStyle = new Callout.Style(this, R.xml.callout_style);
    mCallout.setStyle(calloutStyle);

    // set up feature tables and layers
    mParksFeatureTable = new ServiceFeatureTable(getString(R.string.parks_feature_table));
    mParksFeatureLayer = new FeatureLayer(mParksFeatureTable);
    mPreservesFeatureTable = new ServiceFeatureTable(getString(R.string.preserves_feature_table));
    FeatureLayer preservesFeatureLayer = new FeatureLayer(mPreservesFeatureTable);

    // add feature layers to map
    map.getOperationalLayers().add(mParksFeatureLayer);
    map.getOperationalLayers().add(preservesFeatureLayer);

    // set the mArcGISMap to be displayed in this view
    mMapView.setMap(map);

    // identify feature
    mMapView.setOnTouchListener(new DefaultMapViewOnTouchListener(this, mMapView) {
      @Override
      public boolean onSingleTapConfirmed(MotionEvent me) {
        // tapped point
        mTappedPoint = new Point((int) me.getX(), (int) me.getY());
        // clear any selected features or callouts
        mParksFeatureLayer.clearSelection();
        if (mCallout.isShowing()) {
          mCallout.dismiss();
        }

        final ListenableFuture<IdentifyLayerResult> identifyLayerResultFuture = mMapView
            .identifyLayerAsync(
                mParksFeatureLayer, mTappedPoint, 5, false, 1);
        identifyLayerResultFuture.addDoneListener(() -> {
          try {
            // call get on the future to get the result
            IdentifyLayerResult identifyLayerResult = identifyLayerResultFuture.get();

            if (!identifyLayerResult.getElements().isEmpty()) {
              mSelectedArcGISFeature = (ArcGISFeature) identifyLayerResult.getElements().get(0);
              // highlight the selected feature
              mParksFeatureLayer.selectFeature(mSelectedArcGISFeature);
              queryRelatedFeatures(mSelectedArcGISFeature);
            }
          } catch (InterruptedException | ExecutionException e) {
            String error = "Error getting identify layer result: " + e.getMessage();
            Toast.makeText(MainActivity.this, error, Toast.LENGTH_LONG).show();
            Log.e(TAG, error);
          }
        });
        return super.onSingleTapConfirmed(me);
      }
    });
  }

  /**
   * Query related features from selected feature
   *
   * @param feature selected feature
   */
  private void queryRelatedFeatures(ArcGISFeature feature) {
    final ListenableFuture<List<RelatedFeatureQueryResult>> relatedFeatureQueryResultFuture = mParksFeatureTable
        .queryRelatedFeaturesAsync(feature);

    relatedFeatureQueryResultFuture.addDoneListener(() -> {
      try {
        List<RelatedFeatureQueryResult> relatedFeatureQueryResultList = relatedFeatureQueryResultFuture
            .get();

        // iterate over returned RelatedFeatureQueryResults
        for (RelatedFeatureQueryResult relatedQueryResult : relatedFeatureQueryResultList) {
          // iterate over Features returned
          for (Feature relatedFeature : relatedQueryResult) {
            // persist selected related feature
            mSelectedRelatedFeature = (ArcGISFeature) relatedFeature;
            // get preserve park name
            String parkName = mSelectedRelatedFeature.getAttributes().get("UNIT_NAME").toString();
            // use the Annual Visitors field to use as filter on related attributes
            mAttributeValue = mSelectedRelatedFeature.getAttributes().get("ANNUAL_VISITORS")
                .toString();
            showCallout(parkName);
            // center on tapped point
            mMapView.setViewpointCenterAsync(mMapView.screenToLocation(mTappedPoint));
          }
        }
      } catch (InterruptedException | ExecutionException e) {
        String error = "Error getting related feature query result: " + e.getMessage();
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        Log.e(TAG, error);
      }
    });
  }

  /**
   * Show a callout with Attribute Key and editable Value
   *
   * @param parkName preserves park name
   */
  private void showCallout(String parkName) {
    // create a text view for the callout
    View calloutLayout = LayoutInflater.from(this).inflate(R.layout.related_features_callout, null);
    // create a text view and add park name
    TextView parkText = calloutLayout.findViewById(R.id.park_name);
    String parkLabel = String.format(getString(R.string.callout_label), parkName);
    parkText.setText(parkLabel);
    // create spinner with selection options
    final Spinner visitorSpinner = calloutLayout.findViewById(R.id.visitor_spinner);
    // create an array adapter using the string array and default spinner layout
    final ArrayAdapter<CharSequence> adapter = ArrayAdapter
        .createFromResource(this, R.array.visitors_range, android.R.layout.simple_spinner_item);
    // Specify the layout to use when the list of choices appear
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    // apply the adapter to the spinner
    visitorSpinner.setAdapter(adapter);
    visitorSpinner.setSelection(getIndex(visitorSpinner, mAttributeValue));
    // show callout at tapped location
    mCallout.setLocation(mMapView.screenToLocation(mTappedPoint));
    mCallout.setContent(calloutLayout);
    mCallout.show();
    // respond to user interaction
    visitorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // check if selection has changed
        String selectedValue = visitorSpinner.getSelectedItem().toString();
        if (!selectedValue.equalsIgnoreCase(mAttributeValue)) {
          // selection changed, update the related feature
          mCallout.dismiss();
          updateRelatedFeature(selectedValue);
        }
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {
      }
    });
  }

  /**
   * Update the related feature table and apply update on the server
   *
   * @param visitors annual visitors value
   */
  private void updateRelatedFeature(final String visitors) {
    // load the related feature
    mSelectedRelatedFeature.loadAsync();
    mSelectedRelatedFeature.addDoneLoadingListener(() -> {
      if (mSelectedRelatedFeature.getLoadStatus() == LoadStatus.LOADED) {
        // put new attribute value
        mSelectedRelatedFeature.getAttributes().put("ANNUAL_VISITORS", visitors);
        // persist the attribute value
        mAttributeValue = visitors;
        // update feature in the related feature table
        ListenableFuture<Void> updateFeature = mPreservesFeatureTable
            .updateFeatureAsync(mSelectedRelatedFeature);
        updateFeature.addDoneListener(() -> {
          // apply update to the server
          final ListenableFuture<List<FeatureEditResult>> serverResult = mPreservesFeatureTable
              .applyEditsAsync();
          serverResult.addDoneListener(() -> {
            try {
              // check if server result successful
              List<FeatureEditResult> edits = serverResult.get();
              if (!edits.isEmpty()) {
                if (!edits.get(0).hasCompletedWithErrors()) {
                  mParksFeatureLayer.clearSelection();
                  Toast.makeText(this, getString(R.string.update_success), Toast.LENGTH_SHORT)
                      .show();
                  // show callout with new value
                  mCallout.show();
                } else {
                  Toast.makeText(this, getString(R.string.update_fail), Toast.LENGTH_LONG).show();
                }
              }
            } catch (InterruptedException | ExecutionException e) {
              String error = "Error getting feature edit result: " + e.getMessage();
              Toast.makeText(this, error, Toast.LENGTH_LONG).show();
              Log.e(TAG, error);
            }
          });
        });
      }
    });
  }

  /**
   * Get the position of attribute value
   *
   * @param spinner spinner with list of selection options
   * @param value   attribute value
   * @return position of attribute value
   */
  private int getIndex(Spinner spinner, String value) {
    if (value == null || spinner.getCount() == 0) {
      return -1;
    } else {
      for (int i = 0; i < spinner.getCount(); i++) {
        if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
          return i;

        }
      }
    }
    return -1;
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
