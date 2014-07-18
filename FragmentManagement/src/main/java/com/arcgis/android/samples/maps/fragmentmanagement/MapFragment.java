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

import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISFeatureLayer;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;


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

        // Retrieve arguments
        if (mBasemapName == null && getArguments().containsKey(ARG_BASEMAP_ID)) {
            mBasemapName = getArguments().getString(ARG_BASEMAP_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Reinstate saved instance state (if any)
        if (savedInstanceState != null) {
            mMapState = savedInstanceState.getString(KEY_MAP_STATE, null);
        }

        // Restore map state (center and resolution) if a previously saved state is available,
        // otherwise set initial extent
        if (mMapState == null) {
            SpatialReference mSR = SpatialReference.create(3857);
            Point p1 = GeometryEngine.project(-120.0, 0.0, mSR);
            Point p2 = GeometryEngine.project(-60.0, 50.0, mSR);
            Envelope mInitExtent = new Envelope(p1.getX(), p1.getY(), p2.getX(), p2.getY());
            mMapView.setExtent(mInitExtent);
        } else {
            mMapView.restoreState(mMapState);
        }

        return null;
    }

    /**
     * Creates a basemap layer.
     *
     * @param basemapName String ID of the basemap to use.
     * @return ArcGISTiledMapServiceLayer for the requested basemap.
     */
    private ArcGISTiledMapServiceLayer createBasemapLayer(String basemapName) {
        String url = "http://services.arcgisonline.com/ArcGIS/rest/services/World_Topo_Map/MapServer";
        if (basemapName.equalsIgnoreCase(BASEMAP_NAME_STREETS)) {
            url = "http://server.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer";
        } else if (basemapName.equalsIgnoreCase(BASEMAP_NAME_GRAY)) {
            url = "http://server.arcgisonline.com/ArcGIS/rest/services/Canvas/World_Light_Gray_Base/MapServer";
        } else if (basemapName.equalsIgnoreCase(BASEMAP_NAME_OCEANS)) {
            url = "http://services.arcgisonline.com/ArcGIS/rest/services/Ocean_Basemap/MapServer";
        }
        return new ArcGISTiledMapServiceLayer(url);
    }

}
