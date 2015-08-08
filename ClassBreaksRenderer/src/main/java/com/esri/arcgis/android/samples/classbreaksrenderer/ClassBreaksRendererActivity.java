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

package com.esri.arcgis.android.samples.classbreaksrenderer;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.android.map.Callout;
import com.esri.android.map.FeatureLayer;
import com.esri.android.map.MapView;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.core.geodatabase.GeodatabaseFeatureServiceTable;
import com.esri.core.geodatabase.GeodatabaseFeatureServiceTable.Status;
import com.esri.core.geometry.Point;
import com.esri.core.map.CallbackListener;
import com.esri.core.map.Feature;
import com.esri.core.renderer.ClassBreaksRenderer;

/**
 * This app uses GeodatabaseFeatureServiceTable and Feature Layer. It
 * demonstrates the use of Class breaks renderer. Wind stations are displayed
 * using arrow markers whose size depends upon the wind speeds and rotation
 * depends on the wind direction. On clicking an arrow marker, a callout is
 * displayed which shows the station name, country, temperature and wind speed.
 * 
 * Please be aware that the Feature Layer service is not guaranteed to be
 * running.
 * 
 * 
 */

public class ClassBreaksRendererActivity extends Activity {

	MapView mMapView;
	GeodatabaseFeatureServiceTable table;
	FeatureLayer feature_layer;
	ClassBreaksRenderer wind_renderer;
	static int LAYER_ID = 0;
	View calloutView;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mMapView = (MapView) findViewById(R.id.map);
		mMapView.setEsriLogoVisible(true);
		mMapView.enableWrapAround(true);

		// Inflate the view for the callouts
		calloutView = View.inflate(this, R.xml.callout, null);

		String url = this.getResources().getString(R.string.featurelayer_url);

		// Create the GeodatabaseFeatureServiceTable from service url and layer
		// id
		table = new GeodatabaseFeatureServiceTable(url, LAYER_ID);
			
		// Initializing the GeodatabaseFeatureServiceTable asynchronously
		table.initialize(new CallbackListener<GeodatabaseFeatureServiceTable.Status>() {

			@Override
			public void onCallback(Status objs) {
				if (objs == Status.INITIALIZED) {

					// Creating a feature table
					feature_layer = new FeatureLayer(table);
					// Adding feature layer to the map
					mMapView.addLayer(feature_layer);
				}
			}

			@Override
			public void onError(Throwable e) {
				// Get the error using getInitializationError() method
				Toast.makeText(getApplicationContext(), "Feature Layer not available", Toast.LENGTH_LONG).show();
			}
		});

		// Identify on single tap of map
		mMapView.setOnSingleTapListener(new OnSingleTapListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void onSingleTap(final float x, final float y) {

				// Check if there is a feature present
				if (feature_layer.getFeatureIDs(x, y, 20).length != 0) {

					feature_layer.clearSelection();
					Point mapPoint = mMapView.toMapPoint(x, y);

					long[] iDs = feature_layer.getFeatureIDs(x, y, 20);

					// Get the particular feature values and highlight it
					feature_layer.selectFeature(iDs[0]);
					Feature f = feature_layer.getFeature(iDs[0]);
					String station_name = (String) f
							.getAttributeValue("STATION_NAME");
					String country_name = (String) f
							.getAttributeValue("COUNTRY");
					String temp = (String) f.getAttributeValue("TEMP")
							.toString();
					String wind_speed = (String) f.getAttributeValue(
							"WIND_SPEED").toString();

					// Update the content of the callout
					updateContent(station_name, country_name, temp, wind_speed);
					// Create callout from mapview
					Callout mapCallout = mMapView.getCallout();
					// populate callout with updated content
					mapCallout.setCoordinates(mapPoint);
					mapCallout.setOffset(0,  -3);
					mapCallout.setContent(calloutView);
					// show callout
					mapCallout.show();
					
				}
			}

		});

	}

	/*
	 * Updating the content of the callout with the feature values
	 */

	private void updateContent(String station, String country, String temp,
			String wind_speed) {

		if (calloutView == null)
			return;

		TextView tv_station = (TextView) calloutView.findViewById(R.id.tv_station);
		tv_station.setText(station);

		TextView tv_country = (TextView) calloutView.findViewById(R.id.tv_country);
		tv_country.setText(country);

		TextView tv_temp = (TextView) calloutView.findViewById(R.id.tv_temp);
		tv_temp.setText(temp + "F");

		TextView tv_speed = (TextView) calloutView.findViewById(R.id.tv_speed);
		tv_speed.setText(wind_speed);

	}

	/*
	 * Called when the activity is destroyed
	 */

	@Override
	protected void onDestroy() {
		super.onDestroy();

	}

	/*
	 * Called when the activity pauses
	 */

	@Override
	protected void onPause() {

		super.onPause();
		mMapView.pause();

	}

	/*
	 * Called when the activity resumes
	 */

	@Override
	protected void onResume() {

		super.onResume();
		mMapView.unpause();

	}

}