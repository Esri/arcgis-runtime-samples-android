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

package com.esri.arcgis.android.samples.GeoJSONEarthquakeMap;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.android.map.Callout;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleMarkerSymbol;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * This sample demonstrates how to use information from a GEOJson feed. For this
 * example, seven days significant earthquakes feed from USGS is used which is
 * updated every minute. Json parser is used to parse the GEOJson feed.
 * 
 */

public class GeoJSONEarthquakeMapActivity extends Activity {

	MapView mMapView = null;
	ArcGISTiledMapServiceLayer basemapTileLayer;
	GraphicsLayer graphicsLayer = null;
	Graphic g_selected;
	ProgressDialog dialog;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// Retrieve the map and initial extent from XML layout
		mMapView = (MapView) findViewById(R.id.map);

		// Create a grahics layer and add it to the map
		graphicsLayer = new GraphicsLayer();
		mMapView.addLayer(graphicsLayer);

		// attribute map
		mMapView.setEsriLogoVisible(true);
		// enable map to cross dateline
		mMapView.enableWrapAround(true);

		MyOnSingleTapListener listener = new MyOnSingleTapListener(this);
		mMapView.setOnSingleTapListener(listener);

		FetchEvents earthQuakeEevents = new FetchEvents();
		earthQuakeEevents.execute();

	}

	private class MyOnSingleTapListener implements OnSingleTapListener {

		// Here, we use a single tap to popup the attributes for a report...
		Context _ctx;
		private static final long serialVersionUID = 1L;

		public MyOnSingleTapListener(Context ctx) {
			_ctx = ctx;
		}

		@Override
		public void onSingleTap(float x, float y) {
			SimpleMarkerSymbol symbol;
			float mag;
			int size;

			// To reset the color
			if (g_selected != null) {
				mag = Float.valueOf(g_selected.getAttributeValue("Magnitude")
						.toString());
				size = getSizefromMag(mag);
				symbol = new SimpleMarkerSymbol(Color.rgb(255, 128, 64), size,
						SimpleMarkerSymbol.STYLE.CIRCLE);
				graphicsLayer.updateGraphic(g_selected.getUid(), symbol);
			}

			Callout mapCallout = mMapView.getCallout();
			mapCallout.hide();
			Point pnt = mMapView.toMapPoint(x, y);

			int[] grs = graphicsLayer.getGraphicIDs(x, y, 20);

			if (grs.length > 0) {

				// Getting the nearest graphic
				g_selected = graphicsLayer.getGraphic(grs[0]);

				// Changing the color of the selected graphic
				mag = Float.valueOf(g_selected.getAttributeValue("Magnitude")
						.toString());
				size = getSizefromMag(mag);
				symbol = new SimpleMarkerSymbol(Color.BLUE, size,
						SimpleMarkerSymbol.STYLE.CIRCLE);
				graphicsLayer.updateGraphic(grs[0], symbol);

				Map<String, Object> atts = g_selected.getAttributes();

				String text = "";
				for (int i = 0; i < atts.size(); i++) {
					text = text + atts.keySet().toArray()[i] + ": "
							+ atts.values().toArray()[i] + "\n";

				}

				TextView tv = new TextView(_ctx);
				tv.setText(text);
				tv.setTextSize(10);

				// Here, we populate the Callout with the attribute information
				// from the report.
				mapCallout.setOffset(0, 0);
				mapCallout.setCoordinates(pnt);
				mapCallout.setMaxWidth(1300);
				mapCallout.setMaxHeight(200);

				mapCallout.setStyle(R.xml.mycalloutprefs);
				mapCallout.setContent(tv);

				mapCallout.show();
			}

		}
	}

	private class FetchEvents extends AsyncTask<Void, Void, Void> {

		private ProgressDialog mProgDialog;

		@Override
		protected void onPreExecute() {
			// remove any previous callouts
			if (mMapView.getCallout().isShowing()) {
				mMapView.getCallout().hide();
			}
			// show progress dialog while searching for events
			mProgDialog = ProgressDialog.show(
					GeoJSONEarthquakeMapActivity.this, "",
					"Fetching GeoJson, Please wait....", true);

		}

		@Override
		protected Void doInBackground(Void... params) {

			graphicsLayer.removeAll();
			getEarthquakeEvents();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {

			// remove dialog
			if (mProgDialog.isShowing()) {
				mProgDialog.dismiss();
			}

			if (graphicsLayer != null
					&& graphicsLayer.getNumberOfGraphics() == 0) {
				// update UI with notice that no results were found
				Toast.makeText(GeoJSONEarthquakeMapActivity.this,
						"There are no earthquake events to be shown.",
						Toast.LENGTH_SHORT).show();
			}

		}

	}

	private void getEarthquakeEvents() {
		int size;
		String url = this.getResources().getString(R.string.earthquake_url);
		try {
			// Making the request and getting the response
			HttpClient client = new DefaultHttpClient();
			HttpGet req = new HttpGet(url);
			HttpResponse res = client.execute(req);

			// Converting the response stream to string
			HttpEntity jsonentity = res.getEntity();
			InputStream in = jsonentity.getContent();
			String json_str = convertStreamToString(in);

			JSONObject jsonobj = new JSONObject(json_str);
			JSONArray feature_arr = jsonobj.getJSONArray("features");

			SimpleMarkerSymbol symbol = new SimpleMarkerSymbol(Color.rgb(255,
					128, 64), 15, SimpleMarkerSymbol.STYLE.CIRCLE);

			for (int i = 0; i < feature_arr.length(); i++) {

				// Getting the coordinates and projecting them to map's spatial
				// reference
				
				JSONObject obj_geometry = feature_arr.getJSONObject(i)
						.getJSONObject("geometry");
				double lon = Double.parseDouble(obj_geometry.getJSONArray("coordinates")
						.get(0).toString());
				double lat = Double.parseDouble(obj_geometry.getJSONArray("coordinates")
						.get(1).toString());
				Point point = (Point) GeometryEngine.project(
						new Point(lon, lat), SpatialReference.create(4326),
						mMapView.getSpatialReference());

				JSONObject obj_properties = feature_arr.getJSONObject(i)
						.getJSONObject("properties");
				Map<String, Object> attr = new HashMap<String, Object>();

				String place = obj_properties.getString("place").toString();
				attr.put("Location", place);

				// Setting the size of the symbol based upon the magnitude
				float mag = Float.valueOf(obj_properties.getString("mag")
						.toString());
				size = getSizefromMag(mag);
				symbol.setSize(size);
				attr.put("Magnitude", mag);

				// Converting time from unix time to date format
				long timeStamp = Long.valueOf(obj_properties.getString("time")
						.toString());
				java.util.Date time = new java.util.Date((long) timeStamp);
				attr.put("Time ", time.toString());

				attr.put("Rms ", obj_properties.getString("rms").toString());
				attr.put("Gap ", obj_properties.getString("gap").toString());

				// Add graphics to the graphic layer
				graphicsLayer.addGraphic(new Graphic(point, symbol, attr));

			}

		} catch (ClientProtocolException e) {
			// Catch exceptions here
			e.printStackTrace();
		} catch (IOException e) {
			// Catch exceptions here
			e.printStackTrace();
		} catch (JSONException e) {
			// Catch exceptions here
			e.printStackTrace();
		}

	}

	public String convertStreamToString(InputStream in) {
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		StringBuilder jsonstr = new StringBuilder();
		String line;
		try {
			while ((line = br.readLine()) != null) {
				String t = line + "\n";
				jsonstr.append(t);
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return jsonstr.toString();
	}

	private int getSizefromMag(float mag) {
		int size;
		if (mag < 5)
			size = 7;
		else if (mag >= 5 && mag < 6.5)
			size = 10;
		else
			size = 15;

		return size;
	}

	@Override
	protected void onPause() {
		super.onPause();
		mMapView.pause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mMapView.unpause();
	}

}