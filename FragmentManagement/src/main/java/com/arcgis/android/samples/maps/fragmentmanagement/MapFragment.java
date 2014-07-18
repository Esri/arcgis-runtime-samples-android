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

package com.arcgis.android.samples.maps.fragmentmanagement;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISFeatureLayer;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;


/**
 * A fragment representing a map view. Displays a basemap layer
 * and a number of feature layers in a MapView.
 */
public class MapFragment extends Fragment {
    /** Fragment argument representing a String ID of the currently selected basemap */
    public static final String ARG_BASEMAP_ID = "BasemapId";
    public static final String BASEMAP_NAME_STREETS = "Streets";
    public static final String BASEMAP_NAME_TOPO = "Topographic";
    public static final String BASEMAP_NAME_GRAY = "Gray";
    public static final String BASEMAP_NAME_OCEANS = "Oceans";
    private final String KEY_MAP_STATE = "MapState";

    private String mBasemapName;

    // the mapview
    private MapView mMapView = null;
    // basemap layer
    private ArcGISTiledMapServiceLayer mBasemapLayer;
    // feature layers
    private ArcGISFeatureLayer mFeatureLayer0 = null;
    private ArcGISFeatureLayer mFeatureLayer1 = null;
    private ArcGISFeatureLayer mFeatureLayer2 = null;
    // current map state
    private String mMapState = null;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the fragment
     * (e.g. upon screen orientation changes).
     */
    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /** Calling setRetainInstance() causes the Fragment instance to be retained
         * when its Activity is destroyed and recreated. This allows map Layer
         * objects to be retained so data will not need to be fetched from the network again.
         */
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        TextView textView = new TextView(getActivity());

        return textView;
    }

}
