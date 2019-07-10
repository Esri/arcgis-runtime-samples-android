/*
 *  Copyright 2019 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.esri.arcgisruntime.sample.listkmlcontents;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.esri.arcgisruntime.ogc.kml.KmlContainer;
import com.esri.arcgisruntime.ogc.kml.KmlNetworkLink;
import com.esri.arcgisruntime.ogc.kml.KmlNode;

/**
 * Expandable list view which displays grouped results from a LinkedHashMap.
 */
class ExpandableListViewAdapter extends BaseExpandableListAdapter {

  private static final String TAG = ExpandableListViewAdapter.class.getSimpleName();

  private final Context context;
  private final List<KmlNode> mKmlNodes;

  public ExpandableListViewAdapter(Context context, List<KmlNode> kmlNodes) {
    this.context = context;
    mKmlNodes = kmlNodes;
    Log.d("numNodes", String.valueOf(mKmlNodes.size()));
  }

  @Override
  public Object getChild(int groupListPosition, int childPosition) {
    return getChildrenFromKmlNode(mKmlNodes.get(groupListPosition)).get(childPosition).getName();
  }

  @Override
  public long getChildId(int groupPosition, int childPosition) {
    return childPosition;
  }

  @Override
  public View getChildView(int groupListPosition, final int childPosition, boolean isLastChild, View convertView,
      ViewGroup parent) {
    KmlNode thisKmlNode = mKmlNodes.get(groupListPosition);
    // if the kml node has children
    if (!getChildrenFromKmlNode(thisKmlNode).isEmpty()) {
      // create a new ExpandableListView where the child is
      ExpandableListView nestedExpandableListView = new ExpandableListView(context);
      ExpandableListViewAdapter nestedExpandableListViewAdapter = new ExpandableListViewAdapter(context,
          getChildrenFromKmlNode(thisKmlNode));
      nestedExpandableListView.setAdapter(nestedExpandableListViewAdapter);
      return nestedExpandableListView;
    } else {
      Log.d(TAG, thisKmlNode.getName() +  " has no children");
      final String expandedListText = (String) getChild(groupListPosition, childPosition);
      if (convertView == null) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = layoutInflater.inflate(android.R.layout.simple_expandable_list_item_1, null);
      }
      TextView expandedListTextView = convertView.findViewById(android.R.id.text1);
      expandedListTextView.setText(expandedListText);
      return convertView;
    }

  }

  @Override
  public int getChildrenCount(int listPosition) {
    return getChildrenFromKmlNode(mKmlNodes.get(listPosition)).size();
  }

  @Override
  public Object getGroup(int listPosition) {
    return mKmlNodes.get(listPosition);
  }

  @Override
  public int getGroupCount() {
    return mKmlNodes.size();
  }

  @Override
  public long getGroupId(int listPosition) {
    return listPosition;
  }

  @Override
  public View getGroupView(int listPosition, boolean isExpanded, View convertView, ViewGroup parent) {
    String listTitle = ((KmlNode) getGroup(listPosition)).getName();
    if (convertView == null) {
      LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      convertView = layoutInflater.inflate(android.R.layout.simple_expandable_list_item_1, null);
    }
    TextView listTitleTextView = convertView.findViewById(android.R.id.text1);
    listTitleTextView.setTypeface(null, Typeface.BOLD);
    listTitleTextView.setText(listTitle);
    return convertView;
  }

  @Override
  public boolean hasStableIds() {
    return false;
  }

  @Override
  public boolean isChildSelectable(int listPosition, int expandedListPosition) {
    return true;
  }

  private List<KmlNode> getChildrenFromKmlNode(KmlNode kmlNode) {
    List<KmlNode> childNodes = new ArrayList<>();
    if (kmlNode instanceof KmlContainer) {
      childNodes.addAll(((KmlContainer) kmlNode).getChildNodes());
    }
    if (kmlNode instanceof KmlNetworkLink) {
      childNodes.addAll(((KmlNetworkLink) kmlNode).getChildNodes());
    }
    for (KmlNode childNode : childNodes) {
      Log.d("gotChildNodes", childNode.getName());
    }
    return childNodes;
  }
}
