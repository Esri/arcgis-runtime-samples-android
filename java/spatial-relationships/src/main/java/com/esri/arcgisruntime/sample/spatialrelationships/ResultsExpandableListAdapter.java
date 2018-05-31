package com.esri.arcgisruntime.sample.spatialrelationships;

import android.content.Context;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

public class ResultsExpandableListAdapter extends BaseExpandableListAdapter{

  private final Context context;
  private final List<String> mGroupList;
  private final LinkedHashMap<String,List<String>> mChildList;

  public ResultsExpandableListAdapter(Context context, List<String> header,LinkedHashMap<String,List<String>> child){
    this.context = context;
    mGroupList = new ArrayList<>(header);
    mChildList = child;

    Log.e("check", "+" + mChildList.get("Point").size());
  }

  @Override public int getGroupCount() {
    return mGroupList.size();
  }

  @Override public int getChildrenCount(int groupPosition) {
    return mChildList.get(mGroupList.get(groupPosition)).size();
  }

  @Override public Object getGroup(int groupPosition)  {
    return mGroupList.get(groupPosition);
  }

  @Override public Object getChild(int groupPosition, int childPosition){
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
    Log.e("groupTitle",groupTitle+ "");

    if(convertView == null){
      LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      convertView = layoutInflater.inflate(R.layout.header,null);
    }

    TextView listTitleTextView = convertView.findViewById(R.id.header);
    listTitleTextView.setText(groupTitle);
    return convertView;

  }

  @Override public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView,
      ViewGroup parent) {
    String childText = (String) getChild(groupPosition,childPosition);

    if(convertView == null){
      LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      convertView = layoutInflater.inflate(R.layout.childs,null);
    }

    TextView childView = convertView.findViewById(R.id.child);
    childView.setText(childText);

    return convertView;
  }

  @Override public boolean isChildSelectable(int groupPosition, int childPosition) {
    return true;
  }
}
