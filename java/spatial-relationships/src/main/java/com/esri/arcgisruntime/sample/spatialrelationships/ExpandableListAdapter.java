/* Copyright 2018 Esri
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

package com.esri.arcgisruntime.sample.spatialrelationships;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

/**
 * Expandable list view adapter groups the results from a HashMap.
 */
class ExpandableListAdapter extends BaseExpandableListAdapter {

  private final Context context;

  private final ArrayList<String> mGroupList;

  private final HashMap<String, ArrayList<String>> mChildList;

  public ExpandableListAdapter(Context context, ArrayList<String> header, HashMap<String, ArrayList<String>> child) {
    this.context = context;
    mGroupList = new ArrayList<>(header);
    mChildList = child;
  }

  @Override public int getGroupCount() {
    return mGroupList.size();
  }

  @Override public int getChildrenCount(int groupPosition) {
    return mChildList.get(mGroupList.get(groupPosition)).size();
  }

  @Override public Object getGroup(int groupPosition) {
    return mGroupList.get(groupPosition);
  }

  @Override public Object getChild(int groupPosition, int childPosition) {
    return mChildList.get(mGroupList.get(groupPosition)).get(childPosition);
  }

  @Override public long getGroupId(int groupPosition) {
    return groupPosition;
  }

  @Override public long getChildId(int groupPosition, int childPosition) {
    return childPosition;
  }

  @Override public boolean hasStableIds() {
    return false;
  }

  @Override public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
    String groupTitle = mGroupList.get(groupPosition);

    if (convertView == null) {
      LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      convertView = layoutInflater.inflate(R.layout.header, null);
    }

    TextView listTitleTextView = convertView.findViewById(R.id.header);
    listTitleTextView.setText(groupTitle);
    return convertView;
  }

  @Override public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView,
      ViewGroup parent) {
    String childText = (String) getChild(groupPosition, childPosition);

    if (convertView == null) {
      LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      convertView = layoutInflater.inflate(R.layout.childs, null);
    }
    TextView childView = convertView.findViewById(R.id.child);
    childView.setText(childText);

    return convertView;
  }

  @Override public boolean isChildSelectable(int groupPosition, int childPosition) {
    return true;
  }
}
