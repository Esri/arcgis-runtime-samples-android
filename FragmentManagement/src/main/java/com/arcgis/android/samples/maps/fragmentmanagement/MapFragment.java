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

        // Create new MapView object. Note that, unlike Layers objects, the MapView can't be
        // retained when the Activity is destroyed and recreated, because the old MapView
        // is tied to the old Activity.
        mMapView = new MapView(getActivity());

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

        // Create layers unless retained objects are available
        if (mBasemapLayer == null) {
            mBasemapLayer = createBasemapLayer(mBasemapName);
        }

        if (mFeatureLayer0 == null) {
            mFeatureLayer0 = new ArcGISFeatureLayer(
                    "http://sampleserver5.arcgisonline.com/ArcGIS/rest/services/LocalGovernment/Recreation/FeatureServer/0",
                    ArcGISFeatureLayer.MODE.ONDEMAND);
        }

        if (mFeatureLayer1 == null) {
            mFeatureLayer1 = new ArcGISFeatureLayer(
                    "http://sampleserver5.arcgisonline.com/ArcGIS/rest/services/LocalGovernment/Recreation/FeatureServer/1",
                    ArcGISFeatureLayer.MODE.ONDEMAND);
        }

        if (mFeatureLayer2 == null) {
            mFeatureLayer2 = new ArcGISFeatureLayer(
                    "http://sampleserver5.arcgisonline.com/ArcGIS/rest/services/LocalGovernment/Recreation/FeatureServer/2",
                    ArcGISFeatureLayer.MODE.ONDEMAND);
        }

        // Add layers to MapView
        mMapView.addLayer(mBasemapLayer);
        mMapView.addLayer(mFeatureLayer0);
        mMapView.addLayer(mFeatureLayer1);
        mMapView.addLayer(mFeatureLayer2);

        // set logo and enable wrap around
        mMapView.enableWrapAround(true);
        mMapView.setEsriLogoVisible(true);

        return mMapView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the map state (map center and resolution).
        if (mMapState != null) {
            outState.putString(KEY_MAP_STATE, mMapState);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        // Save map state and pause the MapView to save battery
        mMapState = mMapView.retainState();
        mMapView.pause();
    }

    @Override
    public void onResume() {
        super.onResume();

        // Start the MapView threads running again
        mMapView.unpause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Must remove our layers from MapView before calling recycle(), or we won't be able to reuse them
        mMapView.removeLayer(mBasemapLayer);
        mMapView.removeLayer(mFeatureLayer0);
        mMapView.removeLayer(mFeatureLayer1);
        mMapView.removeLayer(mFeatureLayer2);

        // Release MapView resources
        mMapView.recycle();
        mMapView = null;
    }

    /**
     * Changes the basemap.
     *
     * @param basemapName String ID of the basemap to use.
     */
    public void changeBasemap(String basemapName) {
        mBasemapName = basemapName;
        if (mMapView == null) {
            mBasemapLayer = null;
        } else {
            // Remove old basemap layer and add a new one as the first layer to be drawn
            mMapView.removeLayer(mBasemapLayer);
            mBasemapLayer = createBasemapLayer(mBasemapName);
            mMapView.addLayer(mBasemapLayer, 0);
        }
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
            url = "http://services.arcgisonline.com/arcgis/rest/services/Ocean/World_Ocean_Base/MapServer";
        }
        return new ArcGISTiledMapServiceLayer(url);
    }

}
