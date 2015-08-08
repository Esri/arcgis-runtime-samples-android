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
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
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

import com.esri.core.portal.Portal;
import com.esri.core.portal.PortalGroup;
import com.esri.core.portal.PortalItem;
import com.esri.core.portal.PortalItemType;
import com.esri.core.portal.PortalQueryParams;
import com.esri.core.portal.PortalQueryResultSet;

import java.util.ArrayList;

/**
 * This fragment is launched when the user chooses a group from the list of featured groups. The
 * Portal and the chosen PortalGroup are passed in using the {@link #setParams(Portal, PortalGroup)}
 * method.
 * <p>
 * It displays a list of the webmap items in the chosen group and launches MapActivity when the user
 * picks an item.
 */
public class ItemsFragment extends Fragment {
  private static final String TAG = "ItemsFragment";

  PortalItemListAdapter mAdapter;

  ArrayList<PortalItemData> mItems;

  static Portal mPortal;

  PortalGroup mPortalGroup;

  ProgressDialog mProgressDialog;

  /**
   * Mandatory empty constructor for the fragment manager to instantiate the fragment (e.g. upon
   * device configuration changes).
   */
  public ItemsFragment() {
  }

  /**
   * Passes the Portal and the chosen PortalGroup into this fragment.
   * 
   * @param portal
   * @param portalGroup
   */
  public void setParams(Portal portal, PortalGroup portalGroup) {
    mPortal = portal;
    mPortalGroup = portalGroup;
  }

  /**
   * Returns the Portal object for the portal we are using. Used by MapActivity to get access to the
   * Portal object. Ideally this object would be passed in the Intent used to start MapActivity, but
   * it�s not currently possible to serialize a Portal object for passing in an Intent.
   */
  public static Portal getPortal() {
    return mPortal;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Calling setRetainInstance() causes the Fragment instance to be retained when its Activity is
    // destroyed and recreated. This allows some ArcGIS objects (Portal, PortalGroup and PortalItem)
    // to be retained so data will not need to be fetched from the network again.
    setRetainInstance(true);

    // Setup list view and list adapter
    if (mItems == null || mAdapter == null) {
      mItems = new ArrayList<PortalItemData>();
      mAdapter = new PortalItemListAdapter(getActivity(), mItems);
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
    textView.setText(R.string.itemListTitle);

    // Setup list view - maybe a ListView or a GridView
    AbsListView list = (AbsListView) view.findViewById(android.R.id.list);
    list.setAdapter(mAdapter);
    list.setOnItemClickListener(new OnItemClickListener() {

      @Override
      public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        // Start new activity to handle the portal item the user has selected
        Intent intent = new Intent(getActivity(), MapActivity.class);
        intent.putExtra(MapActivity.KEY_PORTAL_ITEM_ID, mItems.get(position).portalItem.getItemId());
        startActivity(intent);
      }

    });

    // Setup progress dialog
    mProgressDialog = new ProgressDialog(getActivity()) {
      @Override
      public void onBackPressed() {
        // Back key pressed - dismiss the dialog and kill this fragment by popping back stack
        dismiss();
        getFragmentManager().popBackStack();
      }
    };
    return view;
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    if (mItems == null || mItems.size() == 0) {
      // Execute an async task to fetch and display the items
      new PortalItemsAsyncTask().execute();
    }
  }

  /**
   * This class provides an AsyncTask that fetches info about portal items from the server on a
   * background thread and displays it in a list on the UI thread.
   */
  private class PortalItemsAsyncTask extends AsyncTask<Void, Void, Void> {
    private Exception mException;

    public PortalItemsAsyncTask() {
    }

    @Override
    protected void onPreExecute() {
      // Display progress dialog on UI thread
      mProgressDialog.setOnDismissListener(new OnDismissListener() {
        @Override
        public void onDismiss(DialogInterface arg0) {
          // Cancel the task if it's not finished yet
          PortalItemsAsyncTask.this.cancel(true);
        }
      });
      mProgressDialog.setMessage(getString(R.string.fetchingItems));
      mProgressDialog.show();
    }

    @Override
    protected Void doInBackground(Void... params) {
      mException = null;
      try {
        // Do a query for all web maps in the given group
        PortalQueryParams queryParams = new PortalQueryParams();
        queryParams.setQuery(PortalItemType.WEBMAP, mPortalGroup.getGroupId(), null);
        PortalQueryResultSet<PortalItem> results = mPortal.findItems(queryParams);
        if (isCancelled()) {
          return null;
        }

        // Loop through query results
        for (PortalItem item : results.getResults()) {
          Log.d(TAG, "[item title] " + item.getTitle());

          // Fetch item thumbnail (if any) from server
          byte[] data = item.fetchThumbnail();
          if (isCancelled()) {
            return null;
          }
          if (data != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            PortalItemData portalItemData = new PortalItemData(item, bitmap);
            mItems.add(portalItemData);
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
        getFragmentManager().popBackStack(); // kill this fragment
        return;
      }
      mAdapter.notifyDataSetChanged();
    }

  }

  /**
   * This class provides the adapter for the list of portal items.
   */
  private class PortalItemListAdapter extends ArrayAdapter<PortalItemData> {

    public PortalItemListAdapter(Context context, ArrayList<PortalItemData> items) {
      super(context, 0, items);
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

      // Setup item thumbnail
      PortalItemData item = getItem(position);
      ImageView image = (ImageView) view.findViewById(R.id.itemThumbnailImageView);
      image.setImageBitmap(item.itemThumbnail);

      // Setup item title
      TextView text = (TextView) view.findViewById(R.id.itemTitleTextView);
      text.setText(item.portalItem.getTitle());
      return view;
    }

  }

  /**
   * This class holds data for a portal item.
   */
  private class PortalItemData {
    PortalItem portalItem;

    Bitmap itemThumbnail;

    public PortalItemData(PortalItem item, Bitmap bt) {
      this.portalItem = item;
      this.itemThumbnail = bt;
    }
  }

}
