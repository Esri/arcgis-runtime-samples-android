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
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.esri.android.map.Callout;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISFeatureLayer;


public class MainActivity extends Activity {

    MapView mMapView;
    ArcGISFeatureLayer featureLayer;
    GraphicsLayer graphicsLayer;
    private Callout mCallout;

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
                return true;
            case R.id.Query_CA:
                mQueryCaMenuItem.setChecked(true);
                return true;
            case R.id.Query_FR:
                mQueryFrMenuItem.setChecked(true);
                return true;
            case R.id.Query_AU:
                mQueryAuMenuItem.setChecked(true);
                return true;
            case R.id.Query_BR:
                mQueryBrMenuItem.setChecked(true);
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
