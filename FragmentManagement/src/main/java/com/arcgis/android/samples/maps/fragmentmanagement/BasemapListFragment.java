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

import android.app.Activity;
import android.os.Bundle;
import android.app.ListFragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;


/**
 * A fragment representing a list of basemaps. This fragment also supports a two-pane view by
 * allowing list items to be given an 'activated' state upon selection. This helps indicate
 * which item is currently being viewed in a {@link MapFragment}.
 * Activities containing this fragment MUST implement the {@code BasemapListListener} interface.
 */
public class BasemapListFragment extends ListFragment {

    /** Fragment argument representing currently selected position in the list */
    public static final String ARG_ACTIVATED_POSITION = "ActivatedPosition";
    private static final String KEY_ACTIVATED_POSITION = "ActivatedPosition";

    private BasemapListListener mBasemapListListener = sDummyListener;
    private int mActivatedPosition = AdapterView.INVALID_POSITION;

    private OnFragmentInteractionListener mListener;

    /**
     * A callback interface that all activities containing this fragment must implement.
     * This mechanism allows activities to be notified of basemap selections.
     */
    public interface BasemapListListener {
        /**
         * Callback for when a basemap has been selected.
         *
         * @param position Position of selected basemap in list.
         * @param id String identifier of selected basemap.
         */
        public void onBasemapSelected(int position, String id);
    }

    /**
     * A dummy implementation of the {@link BasemapListListener} interface that does nothing.
     * Used only when this fragment is not attached to an activity.
     */
    private static BasemapListListener sDummyListener = new BasemapListListener() {
        @Override
        public void onBasemapSelected(int position, String id) {
        }
    };

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public BasemapListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            mListener.onFragmentInteraction(DummyContent.ITEMS.get(position).id);
        }
    }

    /**
    * This interface must be implemented by activities that contain this
    * fragment to allow an interaction in this fragment to be communicated
    * to the activity and potentially other fragments contained in that
    * activity.
    * <p>
    * See the Android Training lesson <a href=
    * "http://developer.android.com/training/basics/fragments/communicating.html"
    * >Communicating with Other Fragments</a> for more information.
    */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(String id);
    }

}
