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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

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