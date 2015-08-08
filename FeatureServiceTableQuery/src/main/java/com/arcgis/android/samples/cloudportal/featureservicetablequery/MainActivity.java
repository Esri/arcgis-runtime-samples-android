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

package com.arcgis.android.samples.cloudportal.featureservicetablequery;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.esri.android.map.FeatureLayer;
import com.esri.android.map.MapView;
import com.esri.core.geodatabase.GeodatabaseFeatureServiceTable;
import com.esri.core.map.CallbackListener;
import com.esri.core.map.CodedValueDomain;
import com.esri.core.map.Feature;
import com.esri.core.map.FeatureResult;
import com.esri.core.map.Field;
import com.esri.core.tasks.query.QueryParameters;


public class MainActivity extends Activity {

    public FeatureLayer featureLayer;
    public GeodatabaseFeatureServiceTable featureServiceTable;

    MapView mMapView;

    Spinner mDamageSpinner;
    Spinner mCauseSpinner;
    ArrayAdapter<String> damageAdapter;
    ArrayAdapter<String> causeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // populate strings from resource
        String FEATURE_SERVICE_URL = getResources().getString(R.string.feature_service_url);
        final String DAMAGE_FIELD_NAME = getResources().getString(R.string.damage_field_name);
        final String CAUSE_FIELD_NAME = getResources().getString(R.string.cause_field_name);

        // get reference to the map in the layout
        mMapView = (MapView) findViewById(R.id.map);
        // create a GeodatabaseFeatureServiceTable from a feature service url
        featureServiceTable = new GeodatabaseFeatureServiceTable(FEATURE_SERVICE_URL, 0);
        // initialize the GeodatabaseFeatureService and populate it with features from the service
        featureServiceTable.initialize(new CallbackListener<GeodatabaseFeatureServiceTable.Status>() {

            @Override
            public void onCallback(GeodatabaseFeatureServiceTable.Status status) {
                // create a FeatureLayer from teh initialized GeodatabaseFeatureServiceTable
                featureLayer = new FeatureLayer(featureServiceTable);
                // emphasize the selected features by increasing the selection halo size and color
                featureLayer.setSelectionColor(Color.GREEN);
                featureLayer.setSelectionColorWidth(20);
                // add feature layer to map
                mMapView.addLayer(featureLayer);
                // set up spinners to contain values from the layer to query against
                setupQuerySpinners();
                // Get the fields that will be used to query the layer.
                Field damageField = featureServiceTable.getField(DAMAGE_FIELD_NAME);
                Field causeField = featureServiceTable.getField(CAUSE_FIELD_NAME);

                // Retrieve the possible domain values for each field and add to the spinner data adapters.
                CodedValueDomain damageDomain = (CodedValueDomain) damageField.getDomain();
                CodedValueDomain causeDomain = (CodedValueDomain) causeField.getDomain();
                damageAdapter.addAll(damageDomain.getCodedValues().values());
                causeAdapter.addAll(causeDomain.getCodedValues().values());

                // On the main thread, connect up the spinners with the filled data adapters.
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mDamageSpinner.setAdapter(damageAdapter);
                        mCauseSpinner.setAdapter(causeAdapter);
                    }
                });

            }

            @Override
            public void onError(Throwable throwable) {
                showToast("Error initializing FeatureServiceTable");

            }
        });

    }

    public void onClick_okButton(View v) {
        // Build and execute the query.

        // First, check layer exists, and clear any previous selection from the layer.
        if (featureLayer == null) {
            showToast("Feature layer is not set.");
            return;
        }
        featureLayer.clearSelection();

        // Build query predicates to construct a query where clause from selected values.
        String damageType = String.valueOf(mDamageSpinner.getSelectedItem());
        String primCause = String.valueOf(mCauseSpinner.getSelectedItem());
        String whereClause = "typdamage LIKE '" + damageType + "' AND primcause LIKE '" + primCause + "'";

        // Create query parameters, based on the constructed where clause.
        QueryParameters queryParams = new QueryParameters();
        queryParams.setWhere(whereClause);

        // Execute the query and create a callback for dealing with the results of the query.
        featureServiceTable.queryFeatures(queryParams, new CallbackListener<FeatureResult>() {

            @Override
            public void onError(Throwable ex) {
                // Highlight errors to the user.
                showToast("Error querying FeatureServiceTable");
            }

            @Override
            public void onCallback(FeatureResult objs) {

                // If there are no query results, inform user.
                if (objs.featureCount() < 1) {
                    showToast("No results");
                    return;
                }

                // Report number of results to user.
                showToast("Found " + objs.featureCount() + " features.");

                // Iterate the results and select each feature.
                for (Object objFeature : objs) {
                    Feature feature = (Feature) objFeature;
                    featureLayer.selectFeature(feature.getId());
                }
            }
        });
    }

    public void setupQuerySpinners() {
        // Get the spinner controls from the layout.
        mDamageSpinner = (Spinner) findViewById(R.id.damageSpinner);
        mCauseSpinner = (Spinner) findViewById(R.id.causeSpinner);

        // Set up array adapters to contain the values in the spinners.
        damageAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item);
        causeAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item);
    }

    public void showToast(final String message) {
        // Show toast message on the main thread only; this function can be
        // called from query callbacks that run on background threads.
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.unpause();
    }
}
