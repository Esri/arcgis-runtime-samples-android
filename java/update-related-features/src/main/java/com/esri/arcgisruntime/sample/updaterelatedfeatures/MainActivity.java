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

import java.util.List;
import java.util.concurrent.ExecutionException;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.ArcGISFeature;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureEditResult;
import com.esri.arcgisruntime.data.RelatedFeatureQueryResult;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.Callout;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.IdentifyLayerResult;
import com.esri.arcgisruntime.mapping.view.MapView;

public class MainActivity extends AppCompatActivity {

  private MapView mMapView;

  private ServiceFeatureTable mParksFeatureTable;
  private FeatureLayer mParksFeatureLayer;
  private ArcGISFeature mSelectedArcGISFeature;
  private ServiceFeatureTable mPreservesFeatureTable;
  private ArcGISFeature mSelectedRelatedFeature;

  private Point mTappedPoint;
  private Callout mCallout;

  private ProgressDialog mProgressDialog;
  private String mAttributeValue;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // create MapView from layout
    mMapView = (MapView) findViewById(R.id.mapView);
    // center the map over AK
    ArcGISMap map = new ArcGISMap(Basemap.Type.TOPOGRAPHIC, 65.399121, -151.521682, 4);
    // get callout and set style
    mCallout = mMapView.getCallout();
    Callout.Style calloutStyle = new Callout.Style(this, R.xml.callout_style);
    mCallout.setStyle(calloutStyle);
    // create progress dialog and set title
    mProgressDialog = new ProgressDialog(this);
    mProgressDialog.setTitle(getResources().getString(R.string.app_name));
    // set up feature tables and layers
    mParksFeatureTable = new ServiceFeatureTable(getResources().getString(R.string.parks_feature_table));
    mParksFeatureLayer = new FeatureLayer(mParksFeatureTable);
    mParksFeatureLayer.setSelectionColor(Color.YELLOW);
    mParksFeatureLayer.setSelectionWidth(5);
    mPreservesFeatureTable = new ServiceFeatureTable(getResources().getString(R.string.preserves_feature_table));
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
        mProgressDialog.setMessage(getResources().getString(R.string.progress_identify));
        mProgressDialog.show();
        // tapped point
        mTappedPoint = new Point((int) me.getX(), (int) me.getY());
        // clear any selected features or callouts
        mParksFeatureLayer.clearSelection();
        if (mCallout.isShowing()) {
          mCallout.dismiss();
        }

        final ListenableFuture<IdentifyLayerResult> identifyFuture = mMapView.identifyLayerAsync(
            mParksFeatureLayer, mTappedPoint, 5, false, 1);
        identifyFuture.addDoneListener(new Runnable() {
          @Override
          public void run() {
            try {
              mProgressDialog.dismiss();
              // call get on the future to get the result
              IdentifyLayerResult layerResult = identifyFuture.get();

              if (layerResult.getElements().size() > 0) {
                mSelectedArcGISFeature = (ArcGISFeature) layerResult.getElements().get(0);
                // highlight the selected feature
                mParksFeatureLayer.selectFeature(mSelectedArcGISFeature);
                mProgressDialog.setMessage(getResources().getString(R.string.progress_query));
                mProgressDialog.show();
                queryRelatedFeatures(mSelectedArcGISFeature);
              }
            } catch (InterruptedException e) {
              e.printStackTrace();
            } catch (ExecutionException e) {
              e.printStackTrace();
            }
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

    relatedFeatureQueryResultFuture.addDoneListener(new Runnable() {
      @Override
      public void run() {
        try {
          mProgressDialog.dismiss();
          List<RelatedFeatureQueryResult> relatedFeatureQueryResultList = relatedFeatureQueryResultFuture.get();

          // iterate over returned RelatedFeatureQueryResults
          for (RelatedFeatureQueryResult relatedQueryResult : relatedFeatureQueryResultList) {
            // iterate over Features returned
            for (Feature relatedFeature : relatedQueryResult) {
              // persist selected related feature
              mSelectedRelatedFeature = (ArcGISFeature) relatedFeature;
              // get preserve park name
              String parkName = mSelectedRelatedFeature.getAttributes().get("UNIT_NAME").toString();
              // use the Annual Visitors field to use as filter on related attributes
              mAttributeValue = mSelectedRelatedFeature.getAttributes().get("ANNUAL_VISITORS").toString();
              showCallout(parkName);
              // center on tapped point
              mMapView.setViewpointCenterAsync(mMapView.screenToLocation(mTappedPoint));
            }
          }
        } catch (InterruptedException e) {
          e.printStackTrace();
        } catch (ExecutionException e) {
          e.printStackTrace();
        }
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
    View calloutLayout = LayoutInflater.from(getApplicationContext()).inflate(R.layout.related_features_callout, null);
    // create a text view and add park name
    TextView parkText = (TextView) calloutLayout.findViewById(R.id.park_name);
    String parkLabel = String.format(getResources().getString(R.string.callout_label), parkName);
    parkText.setText(parkLabel);
    // create spinner with selection options
    final Spinner visitorSpinner = (Spinner) calloutLayout.findViewById(R.id.visitor_spinner);
    // create an array adapter using the string array and default spinner layout
    final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
        getApplicationContext(), R.array.visitors_range, android.R.layout.simple_spinner_item);
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
          mProgressDialog.setMessage(getResources().getString(R.string.progress_update));
          mProgressDialog.show();
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
    mSelectedRelatedFeature.addDoneLoadingListener(new Runnable() {
      @Override
      public void run() {
        if (mSelectedRelatedFeature.getLoadStatus() == LoadStatus.LOADED) {
          // put new attribute value
          mSelectedRelatedFeature.getAttributes().put("ANNUAL_VISITORS", visitors);
          // persist the attribute value
          mAttributeValue = visitors;
          // update feature in the related feature table
          ListenableFuture<Void> updateFeature = mPreservesFeatureTable.updateFeatureAsync(mSelectedRelatedFeature);
          updateFeature.addDoneListener(new Runnable() {
            @Override
            public void run() {
              // apply update to the server
              final ListenableFuture<List<FeatureEditResult>> serverResult = mPreservesFeatureTable.applyEditsAsync();
              serverResult.addDoneListener(new Runnable() {
                @Override
                public void run() {
                  try {
                    // check if server result successful
                    List<FeatureEditResult> edits = serverResult.get();
                    if (edits.size() > 0) {
                      if (!edits.get(0).hasCompletedWithErrors()) {
                        mParksFeatureLayer.clearSelection();
                        mProgressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.update_success), Toast.LENGTH_SHORT).show();
                        // show callout with new value
                        mCallout.show();
                      } else {
                        mProgressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.update_fail), Toast.LENGTH_LONG).show();
                      }
                    }
                  } catch (InterruptedException e) {
                    e.printStackTrace();
                  } catch (ExecutionException e) {
                    e.printStackTrace();
                  }
                }
              });
            }
          });
        }
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

  @Override
  protected void onDestroy() {
    super.onDestroy();
    // dispose MapView
    mMapView.dispose();
  }
}
