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

import android.app.Activity;
import android.os.Bundle;
import android.app.ListFragment;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;


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

        // Retrieve arguments
        if (mActivatedPosition == AdapterView.INVALID_POSITION && getArguments().containsKey(ARG_ACTIVATED_POSITION)) {
            mActivatedPosition = getArguments().getInt(ARG_ACTIVATED_POSITION);
        }

        // Setup list adapter
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.map_types,
                android.R.layout.simple_list_item_activated_1);
        setListAdapter(adapter);

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Reinstate saved instance state (if any)
        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(KEY_ACTIVATED_POSITION));
        }

    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Check the host activity implements the mandatory callback listener.
        if (!(activity instanceof BasemapListListener)) {
            throw new IllegalStateException("Activity must implement BasemapListListener");
        }

        mBasemapListListener = (BasemapListListener) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        // Reset the active listener interface to the dummy implementation
        mBasemapListListener = sDummyListener;
    }


    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);

        // Notify the active listener interface (the activity, if the fragment is attached to one)
        // that an item has been selected
        CharSequence text = ((TextView) view).getText();
        mBasemapListListener.onBasemapSelected(position, text.toString());
        mActivatedPosition = position;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != AdapterView.INVALID_POSITION) {
            outState.putInt(KEY_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    /**
     * Turns on activate-on-click mode. When this mode is on, list items will be given the
     * 'activated' state when touched.
     */
    public void setActivateOnItemClick(boolean activateOnItemClick) {
        getListView().setChoiceMode(activateOnItemClick ? AbsListView.CHOICE_MODE_SINGLE : AbsListView.CHOICE_MODE_NONE);
        if (activateOnItemClick && mActivatedPosition != AdapterView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, true);
        }
    }

    /**
     * Sets the activated position and highlights it in the list.
     *
     * @param position The activated position.
     */
    private void setActivatedPosition(int position) {
        if (position == AdapterView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            getListView().setItemChecked(position, true);
        }
        mActivatedPosition = position;
    }

}
