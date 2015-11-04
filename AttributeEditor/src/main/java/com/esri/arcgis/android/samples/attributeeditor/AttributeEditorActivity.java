/* Copyright 2015 Esri
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

package com.esri.arcgis.android.samples.attributeeditor;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISDynamicMapServiceLayer;
import com.esri.android.map.ags.ArcGISFeatureLayer;
import com.esri.android.map.ags.ArcGISFeatureLayer.MODE;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.arcgis.android.samples.attributeeditor.FeatureLayerUtils.FieldType;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Point;
import com.esri.core.map.CallbackListener;
import com.esri.core.map.FeatureEditResult;
import com.esri.core.map.FeatureSet;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.tasks.SpatialRelationship;
import com.esri.core.tasks.ags.query.Query;

import java.util.HashMap;
import java.util.Map;

/**
 * Main activity class for the Attribute Editor Sample
 */
public class AttributeEditorActivity extends Activity {

    // arcgis components
    private MapView mapView;
    private ArcGISFeatureLayer featureLayer;
    private ArcGISDynamicMapServiceLayer operationalLayer;
    private Point pointClicked;
    private Envelope initextent;

    // android components
    private LayoutInflater inflator;
    private AttributeListAdapter listAdapter;
    private ListView listView;
    private View listLayout;

    public static final String TAG = "AttributeEditorSample";
    private static final int ATTRIBUTE_EDITOR_DIALOG_ID = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mapView = new MapView(this);
        initextent = new Envelope(-10868502.895856911, 4470034.144641369,
                -10837928.084542884, 4492965.25312689);
        mapView.setExtent(initextent, 0);
        setContentView(mapView);

        // Tiled Layer Basemap
        ArcGISTiledMapServiceLayer basemap = new ArcGISTiledMapServiceLayer(getResources().getString(R.string.basemap));
        mapView.addLayer(basemap);

        // Operational Layer showing Kansas petroleum field production
        operationalLayer = new ArcGISDynamicMapServiceLayer(getResources().getString(R.string.operational_layer));
        mapView.addLayer(operationalLayer);

        // feature service representing the field production layer to query and highlight selections
        featureLayer = new ArcGISFeatureLayer(
                getResources().getString(R.string.feature_layer),
                MODE.SELECTION);
        mapView.addLayer(featureLayer);

        // Show feature selected with outline symbol
        SimpleFillSymbol sfs = new SimpleFillSymbol(Color.TRANSPARENT);
        sfs.setOutline(new SimpleLineSymbol(Color.YELLOW, 5));
        featureLayer.setSelectionSymbol(sfs);

        // set up local variables
        inflator = LayoutInflater.from(getApplicationContext());
        listLayout = inflator.inflate(R.layout.list_layout, null);
        listView = (ListView) listLayout.findViewById(R.id.list_view);

        // Create a new AttributeListAdapter when the feature layer is initialized
        if (featureLayer.isInitialized()) {
            listAdapter = new AttributeListAdapter(this, featureLayer.getFields(), featureLayer.getTypes(),
                    featureLayer.getTypeIdField());
        } else {
            featureLayer.setOnStatusChangedListener(new OnStatusChangedListener() {
                private static final long serialVersionUID = 1L;

                public void onStatusChanged(Object source, STATUS status) {
                    if (status == STATUS.INITIALIZED) {
                        listAdapter = new AttributeListAdapter(AttributeEditorActivity.this, featureLayer.getFields(), featureLayer
                                .getTypes(), featureLayer.getTypeIdField());
                    }
                }
            });
        }

        // Set tap listener for MapView
        mapView.setOnSingleTapListener(new OnSingleTapListener() {
            private static final long serialVersionUID = 1L;

            public void onSingleTap(float x, float y) {
                // convert event into screen click
                pointClicked = mapView.toMapPoint(x, y);

                // build a query to select the clicked feature
                Query query = new Query();
                query.setOutFields(new String[]{"*"});
                query.setSpatialRelationship(SpatialRelationship.INTERSECTS);
                query.setGeometry(pointClicked);
                query.setInSpatialReference(mapView.getSpatialReference());

                // call the select features method and implement the callbacklistener
                featureLayer.selectFeatures(query, ArcGISFeatureLayer.SELECTION_METHOD.NEW, new CallbackListener<FeatureSet>() {

                    // handle any errors
                    public void onError(Throwable e) {
                        Log.d(TAG, "Select Features Error" + e.getLocalizedMessage());
                    }

                    public void onCallback(FeatureSet queryResults) {
                        if (queryResults.getGraphics().length > 0) {
                            Log.d(
                                    TAG,
                                    "Feature found id="
                                            + queryResults.getGraphics()[0].getAttributeValue(featureLayer.getObjectIdField()));

                            // set new data and notify adapter that data has changed
                            listAdapter.setFeatureSet(queryResults);
                            listAdapter.notifyDataSetChanged();

                            // This callback is not run in the main UI thread. All GUI
                            // related events must run in the UI thread,
                            // therefore use the Activity.runOnUiThread() method. See
                            // http://developer.android.com/reference/android/app/Activity.html#runOnUiThread(java.lang.Runnable)
                            // for more information.
                            AttributeEditorActivity.this.runOnUiThread(new Runnable() {

                                public void run() {

                                    // show the editor dialog.
                                    showDialog(ATTRIBUTE_EDITOR_DIALOG_ID);

                                }
                            });
                        }
                    }
                });
            }
        });

        // TODO handle rotation
    }

    /**
     * Overidden method from Activity class - this is the recommended way of creating dialogs
     */
    @Override
    protected Dialog onCreateDialog(int id) {

        switch (id) {

            case ATTRIBUTE_EDITOR_DIALOG_ID:

                // create the attributes dialog
                Dialog dialog = new Dialog(this);
                listView.setAdapter(listAdapter);
                dialog.setContentView(listLayout);
                dialog.setTitle("Edit Attributes");

                // set button on click listeners, setting as XML attributes doesn't work
                // due to a scope/thread issue
                Button btnEditCancel = (Button) listLayout.findViewById(R.id.btn_edit_discard);
                btnEditCancel.setOnClickListener(returnOnClickDiscardChangesListener());

                Button btnEditApply = (Button) listLayout.findViewById(R.id.btn_edit_apply);
                btnEditApply.setOnClickListener(returnOnClickApplyChangesListener());

                return dialog;
        }
        return null;
    }

    /**
     * Helper method to return an OnClickListener for the Apply button
     */
    private OnClickListener returnOnClickApplyChangesListener() {

        return new OnClickListener() {

            public void onClick(View v) {

                boolean isTypeField = false;
                boolean hasEdits = false;
                boolean updateMapLayer = false;
                Map<String, Object> attrs = new HashMap<>();

                // loop through each attribute and set the new values if they have
                // changed
                for (int i = 0; i < listAdapter.getCount(); i++) {

                    AttributeItem item = (AttributeItem) listAdapter.getItem(i);
                    String value;

                    // check to see if the View has been set
                    if (item.getView() != null) {

                        // determine field type and therefore View type
                        if (item.getField().getName().equals(featureLayer.getTypeIdField())) {
                            // drop down spinner

                            Spinner spinner = (Spinner) item.getView();
                            // get value for the type
                            String typeName = spinner.getSelectedItem().toString();
                            value = FeatureLayerUtils.returnTypeIdFromTypeName(featureLayer.getTypes(), typeName);

                            // update map layer as for this featurelayer the type change will
                            // change the features symbol.
                            isTypeField = true;

                        } else if (FieldType.determineFieldType(item.getField()) == FieldType.DATE) {
                            // date

                            Button dateButton = (Button) item.getView();
                            value = dateButton.getText().toString();

                        } else {
                            // edit text

                            EditText editText = (EditText) item.getView();
                            value = editText.getText().toString();

                        }

                        // try to set the attribute value on the graphic and see if it has
                        // been changed
                        boolean hasChanged = FeatureLayerUtils.setAttribute(attrs, listAdapter.featureSet.getGraphics()[0],
                                item.getField(), value, listAdapter.formatter);

                        // if a value has for this field, log this and set the hasEdits
                        // boolean to true
                        if (hasChanged) {

                            Log.d(TAG, "Change found for field=" + item.getField().getName() + " value = " + value
                                    + " applyEdits() will be called");
                            hasEdits = true;

                            // If the change was from a Type field then set the dynamic map
                            // service to update when the edits have been applied, as the
                            // renderer of the feature will likely change
                            if (isTypeField) {

                                updateMapLayer = true;

                            }
                        }

                        // check if this was a type field, if so set boolean back to false
                        // for next field
                        if (isTypeField) {

                            isTypeField = false;
                        }
                    }
                }

                // check there have been some edits before applying the changes
                if (hasEdits) {

                    // set objectID field value from graphic held in the featureset
                    attrs.put(featureLayer.getObjectIdField(), listAdapter.featureSet.getGraphics()[0].getAttributeValue(featureLayer.getObjectIdField()));
                    Graphic newGraphic = new Graphic(null, null, attrs);
                    featureLayer.applyEdits(null, null, new Graphic[]{newGraphic}, createEditCallbackListener(updateMapLayer));
                }

                // close the dialog
                dismissDialog(ATTRIBUTE_EDITOR_DIALOG_ID);

            }
        };

    }

    /**
     * OnClick method for the Discard button
     */
    private OnClickListener returnOnClickDiscardChangesListener() {

        return new OnClickListener() {

            public void onClick(View v) {

                // close the dialog
                dismissDialog(ATTRIBUTE_EDITOR_DIALOG_ID);

            }
        };

    }

    /**
     * Helper method to create a CallbackListener<FeatureEditResult[][]>
     *
     * @return CallbackListener<FeatureEditResult[][]>
     */
    private CallbackListener<FeatureEditResult[][]> createEditCallbackListener(final boolean updateLayer) {

        return new CallbackListener<FeatureEditResult[][]>() {

            public void onCallback(FeatureEditResult[][] result) {

                // check the response for success or failure
                if (result[2] != null && result[2][0] != null && result[2][0].isSuccess()) {

                    Log.d(AttributeEditorActivity.TAG, "Success updating feature with id=" + result[2][0].getObjectId());

                    // see if we want to update the dynamic layer to get new symbols for
                    // updated features
                    if (updateLayer) {

                        operationalLayer.refresh();

                    }
                }
            }

            public void onError(Throwable e) {

                Log.d(AttributeEditorActivity.TAG, "error updating feature: " + e.getLocalizedMessage());

            }
        };
    }
}