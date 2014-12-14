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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class UserContentArrayAdapter extends ArrayAdapter<UserWebmaps> {
  private final Context mContext;
  private final ArrayList<UserWebmaps> mUserWebMaps;

  public UserContentArrayAdapter(Context context, ArrayList<UserWebmaps> values) {
    super(context, R.layout.rowlayout, values);
    this.mContext = context;
    this.mUserWebMaps = values;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View rowView = inflater.inflate(R.layout.rowlayout, parent, false);
    TextView webMapName = (TextView) rowView.findViewById(R.id.firstLine);
    TextView webMapDescription = (TextView) rowView.findViewById(R.id.secondLine);
    ImageView webMapThumbnail = (ImageView) rowView.findViewById(R.id.icon);

    UserWebmaps userWebMap = mUserWebMaps.get(position);
    webMapName.setText(userWebMap.item.getTitle());
    webMapDescription.setText("Description : " + userWebMap.item.getDescription());
    webMapThumbnail.setImageBitmap(userWebMap.itemThumbnail);

    return rowView;
  }

}