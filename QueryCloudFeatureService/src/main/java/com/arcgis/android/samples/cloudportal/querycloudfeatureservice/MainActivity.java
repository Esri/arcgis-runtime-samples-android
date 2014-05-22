/* Copyright 2014 ESRI
 *
 * All rights reserved under the copyright laws of the United States
 * and applicable international laws, treaties, and conventions.
 *
 * You may freely redistribute and use this sample code, with or
 * without modification, provided you include the original copyright
 * notice and use restrictions.
 *
 * See the Sample code usage restrictions document for further information.
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

    MapView mMapView;
    ArcGISFeatureLayer featureLayer;
    GraphicsLayer graphicsLayer;
    private Callout mCallout;
    private Graphic mIdentifiedGraphic;

    private int mCalloutStyle;
    private ViewGroup calloutContent;
    boolean mIsMapLoaded;
    String featureServiceURL;

    ProgressDialog progress;

    // The query params switching menu items.
    MenuItem mQueryUsMenuItem = null;
    MenuItem mQueryCaMenuItem = null;
    MenuItem mQueryFrMenuItem = null;
    MenuItem mQueryAuMenuItem = null;
    MenuItem mQueryBrMenuItem = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Retrieve the map and initial extent from XML layout
        mMapView = (MapView) findViewById(R.id.map);
        // Get the feature service URL from values->strings.xml
        featureServiceURL = this.getResources().getString(R.string.featureServiceURL);
        // Add Feature layer to the MapView
        featureLayer = new ArcGISFeatureLayer(featureServiceURL, ArcGISFeatureLayer.MODE.ONDEMAND);
        mMapView.addLayer(featureLayer);
        // Add Graphics layer to the MapView
        graphicsLayer = new GraphicsLayer();
        mMapView.addLayer(graphicsLayer);

        // Set the Esri logo to be visible, and enable map to wrap around date line.
        mMapView.setEsriLogoVisible(true);
        mMapView.enableWrapAround(true);

        // Get the MapView's callout from xml->identify_calloutstyle.xml
        mCalloutStyle = R.xml.identify_calloutstyle;
        LayoutInflater inflater = getLayoutInflater();
        mCallout = mMapView.getCallout();
        // Get the layout for the Callout from
        // layout->identify_callout_content.xml
        calloutContent = (ViewGroup) inflater.inflate(R.layout.identify_callout_content, null);
        mCallout.setContent(calloutContent);

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
    void identifyLocation(float x, float y) {

        // Hide the callout, if the callout from previous tap is still showing
        // on map
        if (mCallout.isShowing()) {
            mCallout.hide();
        }

        // Find out if the user tapped on a feature
        SearchForFeature(x, y);

        // If the user tapped on a feature, then display information regarding
        // the feature in the callout
        if (mIdentifiedGraphic != null) {
            Point mapPoint = mMapView.toMapPoint(x, y);
            // Show Callout
            ShowCallout(mCallout, mIdentifiedGraphic, mapPoint);
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
    private void SearchForFeature(float x, float y) {

        Point mapPoint = mMapView.toMapPoint(x, y);

        if (mapPoint != null) {

            for (Layer layer : mMapView.getLayers()) {
                if (layer == null)
                    continue;

                if (layer instanceof ArcGISFeatureLayer) {
                    ArcGISFeatureLayer fLayer = (ArcGISFeatureLayer) layer;
                    // Get the Graphic at location x,y
                    mIdentifiedGraphic = GetFeature(fLayer, x, y);
                } else
                    continue;
            }
        }
    }

    /**
     * Returns the Graphic present the location of screen tap
     *
     * @param fLayer
     * @param x
     *          x co-ordinate of point
     * @param y
     *          y co-ordinate of point
     * @return Graphic at location x,y
     */
    private Graphic GetFeature(ArcGISFeatureLayer fLayer, float x, float y) {

        // Get the graphics near the Point.
        int[] ids = fLayer.getGraphicIDs(x, y, 10, 1);
        if (ids == null || ids.length == 0) {
            return null;
        }
        Graphic g = fLayer.getGraphic(ids[0]);
        return g;
    }

    /**
     * Shows the Attribute values for the Graphic in the Callout
     *
     * @param calloutView
     * @param graphic
     * @param mapPoint
     */
    private void ShowCallout(Callout calloutView, Graphic graphic, Point mapPoint) {

        // Get the values of attributes for the Graphic
        String cityName = (String) graphic.getAttributeValue("NAME");
        String countryName = (String) graphic.getAttributeValue("COUNTRY");
        String cityPopulationValue = ((Double) graphic.getAttributeValue("POPULATION")).toString();

        // Set callout properties
        calloutView.setCoordinates(mapPoint);
        calloutView.setStyle(mCalloutStyle);
        calloutView.setMaxWidth(325);

        // Compose the string to display the results
        StringBuilder cityCountryName = new StringBuilder();
        cityCountryName.append(cityName);
        cityCountryName.append(", ");
        cityCountryName.append(countryName);

        TextView calloutTextLine1 = (TextView) findViewById(R.id.citycountry);
        calloutTextLine1.setText(cityCountryName);

        // Compose the string to display the results
        StringBuilder cityPopulation = new StringBuilder();
        cityPopulation.append("Population: ");
        cityPopulation.append(cityPopulationValue);

        TextView calloutTextLine2 = (TextView) findViewById(R.id.population);
        calloutTextLine2.setText(cityPopulation);
        calloutView.setContent(calloutContent);
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

            String whereClause = "COUNTRY='" + params[0] + "'";

            // Define a new query and set parameters
            QueryParameters mParams = new QueryParameters();
            mParams.setWhere(whereClause);
            mParams.setReturnGeometry(true);

            // Define the new instance of QueryTask
            QueryTask queryTask = new QueryTask(featureServiceURL);
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
            graphicsLayer.removeAll();

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
                    graphicsLayer.addGraphic(graphic);
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
                new QueryFeatureLayer().execute("US");
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
