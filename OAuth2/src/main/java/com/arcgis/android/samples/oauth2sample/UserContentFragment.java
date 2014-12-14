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

package com.arcgis.android.samples.oauth2sample;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.TextView;

/**
 * A fragment representing a list of Items. Activities containing this fragment
 * MUST implement the {@link Callbacks} interface.
 */
public class UserContentFragment extends Fragment implements AbsListView.OnItemClickListener {

  private OnFragmentInteractionListener mListener;

  public static ArrayList<UserWebmaps> mUserPortalDataList;

  /**
   * The fragment's ListView.
   */
  private AbsListView mListView;

  private UserContentArrayAdapter mAdapter;

  public static UserContentFragment newInstance() {
    UserContentFragment fragment = new UserContentFragment();
    return fragment;
  }

  /**
   * Mandatory empty constructor for the fragment manager to instantiate the
   * fragment (e.g. upon screen orientation changes).
   */
  public UserContentFragment() {
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Adapter to display your content
    mUserPortalDataList = UserContentActivity.mUserPortalDataList;
    mAdapter = new UserContentArrayAdapter(getActivity(), mUserPortalDataList);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_usercontent, container, false);

    // Set the adapter
    mListView = (AbsListView) view.findViewById(android.R.id.list);
    mListView.setAdapter(mAdapter);

    // Set OnItemClickListener so we can be notified on item clicks
    mListView.setOnItemClickListener(this);

    return view;
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    try {
      mListener = (OnFragmentInteractionListener) activity;
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
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    if (null != mListener) {
      // Notify the active callbacks interface (the activity, if the
      // fragment is attached to one) that an item has been selected.

      // listener for item selected to open webmap
      mListener.onFragmentInteraction(mUserPortalDataList.get(position).item.getItemId());
    }
  }

  /**
   * The default content for this Fragment has a TextView that is shown when the
   * list is empty. If you would like to change the text, call this method to
   * supply the text it should use.
   */
  public void setEmptyText(CharSequence emptyText) {
    View emptyView = mListView.getEmptyView();

    if (emptyText instanceof TextView) {
      ((TextView) emptyView).setText(emptyText);
    }
  }

  public interface OnFragmentInteractionListener {
    public void onFragmentInteraction(String id);
  }

}
