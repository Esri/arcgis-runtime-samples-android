package com.esri.arcgisruntime.sample.spatialrelationships;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ResultsExpandableListAdapter extends BaseExpandableListAdapter{

  private final Context context;
  private final List<String> mGroupList;
  private final HashMap<String,List<String>> mChildList;

  public ResultsExpandableListAdapter(Context context, List<String> header,HashMap<String,List<String>> child){
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
