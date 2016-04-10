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

package com.arcgis.android.samples.cloudportal.querycloudfeatureservice;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.TextView;

import com.esri.android.map.Callout;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.Layer;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISFeatureLayer;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Point;
import com.esri.core.map.Feature;
import com.esri.core.map.FeatureResult;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.tasks.query.QueryParameters;
import com.esri.core.tasks.query.QueryTask;


public class MainActivity extends Activity {

    private MapView mMapView;
    private ArcGISFeatureLayer mFeatureLayer;
    private GraphicsLayer mGraphicsLayer;
    private Callout mCallout;
    private Graphic mIdentifiedGraphic;

    private ViewGroup mCalloutContent;
    private boolean mIsMapLoaded;
    private String mFeatureServiceURL;

    private ProgressDialog progress;

    // The query params switching menu items.
    private MenuItem mQueryUsMenuItem = null;
    private MenuItem mQueryCaMenuItem = null;
    private MenuItem mQueryFrMenuItem = null;
    private MenuItem mQueryAuMenuItem = null;
    private MenuItem mQueryBrMenuItem = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Retrieve the map and initial extent from XML layout
        mMapView = (MapView) findViewById(R.id.map);
        // Get the feature service URL from values->strings.xml
        mFeatureServiceURL = this.getResources().getString(R.string.featureServiceURL);
        // Add Feature layer to the MapView
        mFeatureLayer = new ArcGISFeatureLayer(mFeatureServiceURL, ArcGISFeatureLayer.MODE.ONDEMAND);
        mMapView.addLayer(mFeatureLayer);
        // Add Graphics layer to the MapView
        mGraphicsLayer = new GraphicsLayer();
        mMapView.addLayer(mGraphicsLayer);

        // Set the Esri logo to be visible, and enable map to wrap around date line.
        mMapView.setEsriLogoVisible(true);
        mMapView.enableWrapAround(true);

        LayoutInflater inflater = getLayoutInflater();
        mCallout = mMapView.getCallout();
        // Get the layout for the Callout from
        // layout->identify_callout_content.xml
        mCalloutContent = (ViewGroup) inflater.inflate(R.layout.identify_callout_content, null);
        mCallout.setContent(mCalloutContent);

        mMapView.setOnStatusChangedListener(new OnStatusChangedListener() {

            public void onStatusChanged(Object source, STATUS status) {
                // Check to see if map has successfully loaded
                if ((source == mMapView) && (status == STATUS.INITIALIZED)) {
                    // Set the flag to true
                    mIsMapLoaded = true;
                }
            }
        });

        mMapView.setOnSingleTapListener(new OnSingleTapListener() {

            @Override
            public void onSingleTap(float x, float y) {

                if (mIsMapLoaded) {
                    // If map is initialized and Single tap is registered on screen
                    // identify the location selected
                    identifyLocation(x, y);
                }
            }
        });

    }

    /**
     * Takes in the screen location of the point to identify the feature on map.
     *
     * @param x
     *          x co-ordinate of point
     * @param y
     *          y co-ordinate of point
     */
    private void identifyLocation(float x, float y) {

        // Hide the callout, if the callout from previous tap is still showing
        // on map
        if (mCallout.isShowing()) {
            mCallout.hide();
        }

        // Find out if the user tapped on a feature
        searchForFeature(x, y);

        // If the user tapped on a feature, then display information regarding
        // the feature in the callout
        if (mIdentifiedGraphic != null) {
            Point mapPoint = mMapView.toMapPoint(x, y);
            // Show Callout
            showCallout(mCallout, mIdentifiedGraphic, mapPoint);
        }
    }

    /**
     * Sets the value of mIdentifiedGraphic to the Graphic present on the
     * location of screen tap
     *
     * @param x
     *          x co-ordinate of point
     * @param y
     *          y co-ordinate of point
     */
    private void searchForFeature(float x, float y) {

        Point mapPoint = mMapView.toMapPoint(x, y);

        if (mapPoint != null) {

            for (Layer layer : mMapView.getLayers()) {
                if (layer == null)
                    continue;

                if (layer instanceof ArcGISFeatureLayer) {
                    ArcGISFeatureLayer fLayer = (ArcGISFeatureLayer) layer;
                    // Get the Graphic at location x,y
                    mIdentifiedGraphic = getFeature(fLayer, x, y);
                }
            }
        }
    }

    /**
     * Returns the Graphic present the location of screen tap
     *
     * @param fLayer
     *          ArcGISFeatureLayer to get graphics ids
     * @param x
     *          x co-ordinate of point
     * @param y
     *          y co-ordinate of point
     * @return Graphic at location x,y
     */
    private Graphic getFeature(ArcGISFeatureLayer fLayer, float x, float y) {

        // Get the graphics near the Point.
        int[] ids = fLayer.getGraphicIDs(x, y, 10, 1);
        if (ids == null || ids.length == 0) {
            return null;
        }
        return fLayer.getGraphic(ids[0]);
    }

    /**
     * Shows the Attribute values for the Graphic in the Callout
     *
     * @param calloutView a callout to show
     * @param graphic selected graphic
     * @param mapPoint point to show callout
     */
    private void showCallout(Callout calloutView, Graphic graphic, Point mapPoint) {

        // Get the values of attributes for the Graphic
        String cityName = (String) graphic.getAttributeValue("CITY_NAME");
        String countryName = (String) graphic.getAttributeValue("CNTRY_NAME");
        String cityPopulationValue = graphic.getAttributeValue("POP").toString();

        // Set callout properties
        calloutView.setCoordinates(mapPoint);

        // Compose the string to display the results
        StringBuilder cityCountryName = new StringBuilder();
        cityCountryName.append(cityName);
        cityCountryName.append(", ");
        cityCountryName.append(countryName);

        TextView calloutTextLine1 = (TextView) findViewById(R.id.tv_city);
        calloutTextLine1.setText(cityCountryName);

        // Compose the string to display the results
        StringBuilder cityPopulation = new StringBuilder();
        cityPopulation.append(cityPopulationValue);

        TextView calloutTextLine2 = (TextView) findViewById(R.id.tv_pop);
        calloutTextLine2.setText(cityPopulation);
        calloutView.setContent(mCalloutContent);
        calloutView.show();
    }

    /**
     * Run the query task on the feature layer and put the result on the map.
     */
    private class QueryFeatureLayer extends AsyncTask<String, Void, FeatureResult> {

        // default constructor
        public QueryFeatureLayer() {
        }

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(MainActivity.this, "", "Please wait....query task is executing");
        }

        @Override
        protected FeatureResult doInBackground(String... params) {

            String whereClause = "CNTRY_NAME='" + params[0] + "'";

            // Define a new query and set parameters
            QueryParameters mParams = new QueryParameters();
            mParams.setWhere(whereClause);
            mParams.setReturnGeometry(true);

            // Define the new instance of QueryTask
            QueryTask queryTask = new QueryTask(mFeatureServiceURL);
            FeatureResult results;

            try {
                // run the querytask
                results = queryTask.execute(mParams);
                return results;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(FeatureResult results) {

            // Remove the result from previously run query task
            mGraphicsLayer.removeAll();

            // Define a new marker symbol for the result graphics
            SimpleMarkerSymbol sms = new SimpleMarkerSymbol(Color.BLUE, 10, SimpleMarkerSymbol.STYLE.CIRCLE);

            // Envelope to focus on the map extent on the results
            Envelope extent = new Envelope();

            // iterate through results
            for (Object element : results) {
                // if object is feature cast to feature
                if (element instanceof Feature) {
                    Feature feature = (Feature) element;
                    // convert feature to graphic
                    Graphic graphic = new Graphic(feature.getGeometry(), sms, feature.getAttributes());
                    // merge extent with point
                    extent.merge((Point)graphic.getGeometry());
                    // add it to the layer
                    mGraphicsLayer.addGraphic(graphic);
                }
            }

            // Set the map extent to the envelope containing the result graphics
            mMapView.setExtent(extent, 100);
            // Disable the progress dialog
            progress.dismiss();

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        // Get the query params menu items.
        mQueryUsMenuItem = menu.getItem(0);
        mQueryCaMenuItem = menu.getItem(1);
        mQueryFrMenuItem = menu.getItem(2);
        mQueryAuMenuItem = menu.getItem(3);
        mQueryBrMenuItem = menu.getItem(4);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle menu item selection.
        switch (item.getItemId()) {
            case R.id.Query_US:
                mQueryUsMenuItem.setChecked(true);
                new QueryFeatureLayer().execute("United States");
                return true;
            case R.id.Query_CA:
                mQueryCaMenuItem.setChecked(true);
                new QueryFeatureLayer().execute("Canada");
                return true;
            case R.id.Query_FR:
                mQueryFrMenuItem.setChecked(true);
                new QueryFeatureLayer().execute("France");
                return true;
            case R.id.Query_AU:
                mQueryAuMenuItem.setChecked(true);
                new QueryFeatureLayer().execute("Australia");
                return true;
            case R.id.Query_BR:
                mQueryBrMenuItem.setChecked(true);
                new QueryFeatureLayer().execute("Brazil");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void onPause() {
        super.onPause();
        mMapView.pause();
    }

    protected void onResume() {
        super.onResume();
        mMapView.unpause();
    }
}
