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

package com.arcgis.android.samples.maps.switchmaps;

import android.os.Bundle;
import android.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;


/* This fragment contains a MapView, and is added to the MapActivity. Temporary state such as map contents
 * and extent are preserved if, for example, the device is rotated.
 */
public class MapFragment extends Fragment {

    // MapView in this fragment.
    private MapView mMapView;
    // URL of the map service that is added as a layer to the MapView.
    private String mTiledServiceUrl;
    // Center and resolution of the MapView.
    private String mMapState;
    // Keys used to store temporary map state and map data url.
    private final String SERVICE_URL = "serviceUrl";
    private final String MAP_STATE = "mapState";

    public MapFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the map fragment from the XML layout and get the MapView.
        View fragmentView = inflater.inflate(R.layout.fragment_map, container, false);
        mMapView = (MapView) fragmentView.findViewById(R.id.map);

        if (savedInstanceState != null) {
            // If there is saved state, then the fragment will be re-created by the
            // android framework. Extract the saved state
            // of the fragment from the bundle parameter.
            mTiledServiceUrl = savedInstanceState.getString(SERVICE_URL, null);
            mMapState = savedInstanceState.getString(MAP_STATE, null);
        } else {
            // Retrieve the service url and extent from arguments provided by
            // MapActivity
            Bundle args = getArguments();
            mTiledServiceUrl = args.getString("MAPURL");
            mMapState = args.getString("MAPSTATE");
        }

        // If a service URL has been set, add a map layer based on that service.
        // After the layer is added, this will ensure
        // the map has a spatial reference, and the full extent covers the entire
        // world, so wrap around map can be set.
        if (!TextUtils.isEmpty(mTiledServiceUrl)) {
            mMapView.addLayer(new ArcGISTiledMapServiceLayer(mTiledServiceUrl));
            mMapView.enableWrapAround(true);
        }
        // If map state (center and resolution) has been stored, update the MapView
        // with this state.
        if (!TextUtils.isEmpty(mMapState)) {
            mMapView.restoreState(mMapState);
        }

        // Return the view for this Fragment.
        return fragmentView;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save the URL of map service layer, and map state (map center and resolution).
        outState.putString(SERVICE_URL, mTiledServiceUrl);
        outState.putString(MAP_STATE, mMapView.retainState());
    }

    public void onPause() {
        super.onPause();

        // Call MapView.pause to suspend map rendering while the activity containing
        // this fragment is paused, which can save
        // battery usage.
        mMapView.pause();
    }

    public void onResume() {
        super.onResume();

        // Call MapView.unpause to resume map rendering when the activity containing
        // this fragment returns to the
        // foreground.
        mMapView.unpause();
    }

}
