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

package com.esri.arcgis.android.samples.routing;

import java.util.ArrayList;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/*
 * Custom Adapter for the list view
 * 
 */
public class MyAdapter extends ArrayAdapter<String> {

	Context context;
	ArrayList<String> directions;

	MyAdapter(Context c, ArrayList<String> currDirections) {
		super(c, R.layout.list_item, R.id.segment, currDirections);
		this.context = c;
		directions = currDirections;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View row = inflater.inflate(R.layout.list_item, parent, false);
		ImageView myImage = (ImageView) row.findViewById(R.id.imageView1);

		String segment = directions.get(position);

		//For segment icons on the list.
		if (segment.contains("left")) {
			myImage.setImageResource(R.drawable.nav_left);

		} else if (segment.contains("right")) {
			myImage.setImageResource(R.drawable.nav_right);

		} else if (segment.contains("Continue") || segment.contains("straight")
				|| segment.contains("Go")) {
			myImage.setImageResource(R.drawable.nav_straight);

		} else if (segment.contains("U-turn")) {
			myImage.setImageResource(R.drawable.nav_uturn);
		}

		//For Starting and Ending Point icons
		if (position == 0) {
			myImage.setImageResource(R.drawable.ic_action_location_found);
		}
		if (position == directions.size() - 1) {
			myImage.setImageResource(R.drawable.ic_action_place);

		}

		TextView myTitle = (TextView) row.findViewById(R.id.segment);
		myTitle.setText(segment);
		return row;
	}

}
