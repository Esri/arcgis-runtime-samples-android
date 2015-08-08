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

package com.arcgis.android.samples.oauth2sample;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.esri.android.map.MapView;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.core.map.CallbackListener;
import com.esri.core.portal.WebMap;

public class MapFragment extends Fragment {

  protected static final String TAG = "MapFragment";

  private String mItemId;

  @SuppressWarnings("unused")
  private OnFragmentInteractionListener_MapFragment mListener;

  View mUserMapView;

  MapView mMap;

  ProgressBar mProgressBar;

  protected static final int CLOSE_LOADING_WINDOW = 0;

  /**
   * factory method to create a new instance of this fragment using the provided
   * parameters.
   * 
   * @param itemId
   * @return A new instance of fragment MapFragment.
   */
  public static MapFragment newInstance(String itemId) {
    MapFragment fragment = new MapFragment();
    Bundle args = new Bundle();
    args.putString("ItemId", itemId);
    fragment.setArguments(args);
    return fragment;
  }

  public MapFragment() {
    // Required empty public constructor
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {
      mItemId = getArguments().getString("ItemId");
    }

    // create a new instance of the webmap from item
    // the webmap will be created in the callback
    WebMap.newInstance(mItemId, UserContentActivity.mMyPortal, new CallbackListener<WebMap>() {

      @Override
      public void onError(Throwable e) {

        Log.e(TAG, "Error instantiating WebMap", e);
      }

      @Override
      public void onCallback(final WebMap webmap) {

        // Add the mapview in the ui thread.
        getActivity().runOnUiThread(new Runnable() {

          @Override
          public void run() {

            if (webmap != null) {
              mMap = new MapView(getActivity(), webmap, null, null);

              mMap.setOnStatusChangedListener(new OnStatusChangedListener() {

                private static final long serialVersionUID = 1L;

                @Override
                public void onStatusChanged(Object source, STATUS status) {
                  if (status.getValue() == EsriStatusException.INIT_FAILED_WEBMAP_UNSUPPORTED_LAYER) {

                    Toast.makeText(getActivity(), "Webmap failed to load", Toast.LENGTH_SHORT).show();
                  }

                }
              });
              // set the visibility of progress bar to
              // invisible
              mProgressBar.setVisibility(View.INVISIBLE);
              // add the mapview to the fragment
              ((ViewGroup) getView()).addView(mMap, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                  LayoutParams.MATCH_PARENT));
            }

          }
        });

      }
    });

  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    mUserMapView = inflater.inflate(R.layout.fragment_map, container, false);
    mProgressBar = (ProgressBar) mUserMapView.findViewById(R.id.progress);
    mProgressBar.setVisibility(View.VISIBLE);
    return mUserMapView;
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    try {
      mListener = (OnFragmentInteractionListener_MapFragment) activity;
    } catch (ClassCastException e) {
      throw new ClassCastException(activity.toString() + " must implement OnFragmentInteractionListener");
    }
  }

  @Override
  public void onDetach() {
    super.onDetach();
    mListener = null;
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();

  }

  public interface OnFragmentInteractionListener_MapFragment {
    public void onFragmentInteraction(Uri uri);
  }

}
