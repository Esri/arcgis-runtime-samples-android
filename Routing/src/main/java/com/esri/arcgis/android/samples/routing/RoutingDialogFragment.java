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

import java.util.List;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.tasks.geocode.Locator;
import com.esri.core.tasks.geocode.LocatorFindParameters;
import com.esri.core.tasks.geocode.LocatorGeocodeResult;

/*
 * This fragment displays a dialog box which contains text fields for source and destination addresses.
 * It also contains two icons "My Location" and "Swap Addresses". When user clicks on My Location icon, the focused
 * text field displays the text "My Location". When "Swap Addresses" icon is clicked the addresses in the text boxes are swapped.
 * When the user clicks on the "Route" button, the addresses are geocoded to points which are then used by the RoutingSample activity to
 * display the route.
 * 
 */

public class RoutingDialogFragment extends DialogFragment implements
		OnFocusChangeListener, OnClickListener {

	EditText et_source;
	EditText et_destination;
	Locator locator;
	static ProgressDialog dialog;
	static Handler handler;
	Button bGetRoute;

	// For storing the result of Geocoding Task
	List<LocatorGeocodeResult> result_origin = null;
	List<LocatorGeocodeResult> result_destination = null;

	// Interface to be implemented by the activity
	onGetRoute mCallback;

	// To check if the edit text contains "My Location"
	boolean src_isMyLocation = false;
	boolean dest_isMyLocation = false;

	final SpatialReference wm = SpatialReference.create(102100);
	final SpatialReference egs = SpatialReference.create(4326);
	String source;
	String destination;
	// Image views for the icons
	ImageView img_sCancel, img_dCancel, img_myLocaion, img_swap;

	// Runnable to dismiss the process dialog
	static public class MyRunnable implements Runnable {
		public void run() {
			dialog.dismiss();
		}
	}

	// Interface
	public interface onGetRoute {
		void onDialogRouteClicked(Point p1, Point p2);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		handler = new Handler();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mCallback = (onGetRoute) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement onDrawerListSelectedListener");
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.dialog_layout, container);

		// Set the views from the XML layout
		et_source = (EditText) view.findViewById(R.id.et_source);
		et_destination = (EditText) view.findViewById(R.id.et_destination);
		img_sCancel = (ImageView) view.findViewById(R.id.iv_cancelSource);
		img_dCancel = (ImageView) view.findViewById(R.id.iv_cancelDestination);
		img_swap = (ImageView) view.findViewById(R.id.iv_interchange);
		img_myLocaion = (ImageView) view.findViewById(R.id.iv_myDialogLocation);
		bGetRoute = (Button) view.findViewById(R.id.bGetRoute);

		// Adding custom Listener to edit text views
		et_source.addTextChangedListener(new MyTextWatcher(et_source));
		et_destination
				.addTextChangedListener(new MyTextWatcher(et_destination));

		// Adding Focus Change listener to the edit text views
		et_source.setOnFocusChangeListener(this);
		et_destination.setOnFocusChangeListener(this);

		// Setting onClick listener for the icons on the dialog
		img_dCancel.setOnClickListener(this);
		img_sCancel.setOnClickListener(this);
		img_swap.setOnClickListener(this);
		img_myLocaion.setOnClickListener(this);

		// Adding onClick listener for the button
		bGetRoute.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				source = et_source.getText().toString();
				destination = et_destination.getText().toString();

				// If either of the edit text is empty, display the toast
				if (source.equals("") || destination.equals("")) {
					Toast.makeText(getActivity(), "Place cannot be empty",
							Toast.LENGTH_LONG).show();
					return;
				}

				// Checking if the edit text views contain "My Location"
				if (source.equals("My Location"))
					src_isMyLocation = true;
				if (destination.equals("My Location"))
					dest_isMyLocation = true;

				// If source and destination are not same then geocode the
				// addresses
				if (!source.equals(destination)) {
					// Geocode the addresses
					geocode(source, destination);
				} else {
					Toast.makeText(getActivity(),
							"Source and Destination should be different",
							Toast.LENGTH_LONG).show();
				}

			}
		});

		// Removing title from the dialog box
		getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

		return view;
	}

	private void geocode(String address1, String address2) {
		try {
			// create Locator parameters from single line address string
			LocatorFindParameters findParams_source = new LocatorFindParameters(
					address1);
			// set the search country to USA
			findParams_source.setSourceCountry("USA");
			// limit the results to 2
			findParams_source.setMaxLocations(2);
			// set address spatial reference to match map
			findParams_source.setOutSR(RoutingSample.map.getSpatialReference());
			// execute async task to geocode address

			LocatorFindParameters findParams_dest = new LocatorFindParameters(
					address2);
			findParams_dest.setSourceCountry("USA");
			findParams_dest.setMaxLocations(2);
			findParams_dest.setOutSR(RoutingSample.map.getSpatialReference());

			Geocoder gcoder = new Geocoder(findParams_source, findParams_dest);
			gcoder.execute();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private class Geocoder extends AsyncTask<Void, Void, Void> {

		// Location Find Parameters for both source and destination addresses
		LocatorFindParameters lfp_start, lfp_dest;

		// Constructor
		public Geocoder(LocatorFindParameters findParams_start,
				LocatorFindParameters findParams_dest) {
			lfp_start = findParams_start;
			lfp_dest = findParams_dest;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			// Displaying the Process Dialog
			dialog = ProgressDialog.show(getActivity(), "Routing Sample",
					"Geocoding the addresses ...");

		}

		@Override
		protected Void doInBackground(Void... params) {

			// set the geocode service

			locator = Locator.createOnlineLocator(getResources().getString(
					R.string.geocode_url));
			try {
				// pass address to find method to return point representing
				// address
				if (!src_isMyLocation)
					result_origin = locator.find(lfp_start);
				if (!dest_isMyLocation)
					result_destination = locator.find(lfp_dest);

			} catch (Exception e) {
				e.printStackTrace();
			}

			return null;
		}

		// The result of geocode task is passed as a parameter to map the
		// results

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			handler.post(new MyRunnable());
			Point p1 = null;
			Point p2 = null;

			// Projecting the current location to the output spatial ref
			Point currLocation = (Point) GeometryEngine.project(
					RoutingSample.mLocation, egs, wm);

			// Assignign current location to the field with value as
			// "My Location"
			if (src_isMyLocation)
				p1 = currLocation;
			else if (result_origin.size() > 0)
				p1 = result_origin.get(0).getLocation();

			if (dest_isMyLocation)
				p2 = currLocation;
			else if (result_destination.size() > 0)
				p2 = result_destination.get(0).getLocation();

			if (p1 == null) {
				Toast.makeText(getActivity(), "Not a valid source address",
						Toast.LENGTH_LONG).show();
			} else if (p2 == null) {
				Toast.makeText(getActivity(),
						"Not a valid destination address", Toast.LENGTH_LONG)
						.show();
			} else
				mCallback.onDialogRouteClicked(p1, p2);

		}
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

	}

	// Custom Listener for the Edit text views
	private class MyTextWatcher implements TextWatcher {

		private View view;

		private MyTextWatcher(View view) {
			this.view = view;
		}

		public void beforeTextChanged(CharSequence charSequence, int i, int i1,
				int i2) {
		}

		public void onTextChanged(CharSequence charSequence, int i, int i1,
				int i2) {
		}

		public void afterTextChanged(Editable editable) {

			// Displaying the cross icon when the edit text views are not empty
			String text = editable.toString();
			switch (view.getId()) {
			case R.id.et_source:
				if (text.length() > 0)
					img_sCancel.setVisibility(View.VISIBLE);
				else
					img_sCancel.setVisibility(View.INVISIBLE);
				break;
			case R.id.et_destination:
				if (text.length() > 0)
					img_dCancel.setVisibility(View.VISIBLE);
				else
					img_dCancel.setVisibility(View.INVISIBLE);

				break;
			}
		}
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {

		// Checking the focus of the edit text and displaying the cross icon if
		// it is not empty
		switch (v.getId()) {
		case R.id.et_source:
			if (hasFocus && et_source.getText().toString().length() > 0)
				img_sCancel.setVisibility(View.VISIBLE);
			else
				img_sCancel.setVisibility(View.INVISIBLE);
			break;
		case R.id.et_destination:
			if (hasFocus && et_destination.getText().toString().length() > 0)
				img_dCancel.setVisibility(View.VISIBLE);
			else
				img_dCancel.setVisibility(View.INVISIBLE);

			break;
		}
	}

	// OnClick events for the icons on the dialog box
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv_cancelSource:
			// Clearing the text
			et_source.getText().clear();
			break;
		case R.id.iv_cancelDestination:
			// Clearing the text
			et_destination.getText().clear();
			break;
		case R.id.iv_myDialogLocation:

			// Putting "My Location" in the edit text that is in focus
			if (et_source.hasFocus())
				et_source.setText("My Location");
			else
				et_destination.setText("My Location");
			break;
		case R.id.iv_interchange:

			// Swapping the values
			String temp = et_source.getText().toString();
			et_source.setText(et_destination.getText().toString());
			et_destination.setText(temp);
			break;

		}

	}

}
