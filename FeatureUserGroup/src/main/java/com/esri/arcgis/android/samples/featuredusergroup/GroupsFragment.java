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

package com.esri.arcgis.android.samples.featuredusergroup;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.core.io.UserCredentials;
import com.esri.core.portal.Portal;
import com.esri.core.portal.PortalGroup;
import com.esri.core.portal.PortalInfo;
import com.esri.core.portal.PortalQueryParams;
import com.esri.core.portal.PortalQueryResultSet;

import java.util.ArrayList;
import java.util.List;

/**
 * This fragment is launched when FeaturedGroupsActivity starts. It logs in to the portal and
 * displays a list of the featured groups. It launches ItemsFragment when the user picks a group.
 */
public class GroupsFragment extends Fragment {

  private static final String TAG = "GroupsFragment";

  private static final String URL = "http://arcgis.com";

  private static final String USER_NAME = "democsf";

  private static final String PASSWORD = "devdemo";

  static Portal mPortal;

  FeaturedGroupListAdapter mAdapter;

  ArrayList<FeaturedGroup> mFeaturedGroups;

  ProgressDialog mProgressDialog;

  /**
   * Mandatory empty constructor for the fragment manager to instantiate the fragment (e.g. upon
   * device configuration changes).
   */
  public GroupsFragment() {
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Calling setRetainInstance() causes the Fragment instance to be retained when its Activity is
    // destroyed and recreated. This allows some ArcGIS objects (Portal and PortalGroup) to be
    // retained so data will not need to be fetched from the network again.
    setRetainInstance(true);

    // Setup list view and list adapter
    if (mFeaturedGroups == null || mAdapter == null) {
      mFeaturedGroups = new ArrayList<FeaturedGroup>();
      mAdapter = new FeaturedGroupListAdapter(getActivity(), mFeaturedGroups);
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // Creates the view from the list_layout layout resource. The layout/list_layout.xml file
    // contains a ListView for use in portrait orientation, but layout-land/list_layout.xml contains
    // a GridView for use in landscape orientation
    View view = inflater.inflate(R.layout.list_layout, container, false);

    // Setup title
    TextView textView = (TextView) view.findViewById(R.id.listTitleTextView);
    textView.setText(R.string.featuredGroupsTitle);

    // Setup list view - maybe a ListView or a GridView
    AbsListView list = (AbsListView) view.findViewById(android.R.id.list);
    list.setAdapter(mAdapter);
    list.setOnItemClickListener(new OnItemClickListener() {

      @Override
      public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        // Launch an ItemsFragment to handle the group the user has selected
        FragmentManager fragMgr = getFragmentManager();
        ItemsFragment fragment = new ItemsFragment();
        // Pass Portal and PortalGroup objects to the new fragment
        fragment.setParams(mPortal, mFeaturedGroups.get(position).group);
        fragMgr.beginTransaction().replace(R.id.main_fragment_container, fragment, null)
            .addToBackStack(null).commit();
      }

    });

    // Setup progress dialog
    mProgressDialog = new ProgressDialog(getActivity()) {
      @Override
      public void onBackPressed() {
        // Back key pressed - dismiss the dialog and finish the activity
        dismiss();
        getActivity().finish();
      }
    };
    return view;
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    if (mFeaturedGroups == null || mFeaturedGroups.size() == 0) {
      // Execute an async task to fetch and display the featured groups
      new FeaturedGroupsAsyncTask().execute();
    }
  }

  /**
   * This class provides an AsyncTask that fetches info about featured groups from the server on a
   * background thread and displays it in a list on the UI thread.
   */
  private class FeaturedGroupsAsyncTask extends AsyncTask<Void, Void, Void> {
    private Exception mException;

    public FeaturedGroupsAsyncTask() {
    }

    @Override
    protected void onPreExecute() {
      // Display progress dialog on UI thread
      mProgressDialog.setOnDismissListener(new OnDismissListener() {
        @Override
        public void onDismiss(DialogInterface arg0) {
          // Cancel the task if it's not finished yet
          FeaturedGroupsAsyncTask.this.cancel(true);
        }
      });
      mProgressDialog.setMessage(getString(R.string.fetchingGroups));
      mProgressDialog.show();
    }

    @Override
    protected Void doInBackground(Void... params) {
      mException = null;

      // Create UserCredentials with USER_NAME and PASSWORD
      UserCredentials credentials = new UserCredentials();
      credentials.setUserAccount(USER_NAME, PASSWORD);

      // Create a Portal object
      mPortal = new Portal(URL, credentials);

      try {
        // Fetch portal info from server. This logs in with the credentials set above
        PortalInfo portalInfo = mPortal.fetchPortalInfo();
        if (isCancelled()) {
          return null;
        }

        // Get list of queries to use to fetch the featured groups
        List<String> querys = portalInfo.getFeaturedGroupsQueries();

        // Loop through query list to find each featured group
        for (String query : querys) {
          Log.d(TAG, "[query] " + query);
          PortalQueryResultSet<PortalGroup> result = mPortal
              .findGroups(new PortalQueryParams(query));
          if (isCancelled()) {
            return null;
          }

          // Loop through query results
          for (PortalGroup group : result.getResults()) {
            Log.d(TAG, "[group title] " + group.getTitle());

            // Fetch group thumbnail (if any) from server
            byte[] data = group.fetchThumbnail();
            if (isCancelled()) {
              return null;
            }
            if (data != null) {
              // Add group to list only if we have a thumbnail
              Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
              mFeaturedGroups.add(new FeaturedGroup(group, bitmap));
            }
          }
        }
      } catch (Exception e) {
        mException = e;
      }
      return null;
    }

    @Override
    protected void onPostExecute(Void result) {
      // Display results on UI thread
      mProgressDialog.dismiss();
      if (mException != null) {
        Log.w(TAG, mException.toString());
        Toast.makeText(getActivity(), getString(R.string.fetchDataFailed), Toast.LENGTH_LONG)
            .show();
        getActivity().finish();
        return;
      }
      mAdapter.notifyDataSetChanged();
    }

  }

  /**
   * This class provides the adapter for the list of featured groups.
   */
  private class FeaturedGroupListAdapter extends ArrayAdapter<FeaturedGroup> {

    public FeaturedGroupListAdapter(Context context, ArrayList<FeaturedGroup> groupinfo) {
      super(context, 0, groupinfo);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      View view = convertView;

      // Inflate view unless we've been given an existing view to reuse
      if (convertView == null) {
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(
            Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.list_item, null);
      }

      // Setup group thumbnail
      FeaturedGroup item = getItem(position);
      ImageView image = (ImageView) view.findViewById(R.id.itemThumbnailImageView);
      image.setImageBitmap(item.groupThumbnail);

      // Setup group title
      TextView text = (TextView) view.findViewById(R.id.itemTitleTextView);
      text.setText(item.group.getTitle());
      return view;
    }

  }

  /**
   * This class holds data for a featured group.
   */
  private class FeaturedGroup {
    PortalGroup group;

    Bitmap groupThumbnail;

    public FeaturedGroup(PortalGroup pg, Bitmap bt) {
      this.group = pg;
      this.groupThumbnail = bt;
    }
  }

}
