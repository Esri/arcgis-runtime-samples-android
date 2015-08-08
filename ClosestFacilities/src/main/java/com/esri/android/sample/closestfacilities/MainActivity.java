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

package com.esri.android.sample.closestfacilities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.widget.Toast;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISFeatureLayer;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polyline;
import com.esri.core.io.EsriSecurityException;
import com.esri.core.io.UserCredentials;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.PictureMarkerSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.tasks.na.ClosestFacilityParameters;
import com.esri.core.tasks.na.ClosestFacilityResult;
import com.esri.core.tasks.na.ClosestFacilityTask;
import com.esri.core.tasks.na.NAFeaturesAsFeature;
import com.esri.core.tasks.na.NATravelDirection;
import com.esri.core.tasks.na.Route;

public class MainActivity extends Activity {

	// mapview definition
	MapView mMapView;
	// basemap layer
	ArcGISTiledMapServiceLayer basemapStreet;
	// feature layer
	ArcGISFeatureLayer fLayer;
	// closest facility task
	ClosestFacilityTask closestFacilityTask;
	// route definition
	Route route;
	// graphics layer to show route
	GraphicsLayer routeLayer;

	// create UI components
	static ProgressDialog dialog;
	boolean auth;

	// string urls
	// feature service query url for facilities
	String queryUrl = "http://sampleserver6.arcgisonline.com/arcgis/rest/services/Recreation/FeatureServer/0/query?where=Facility%3E2&geometry=xmin%3A+-116.6%2C+ymin%3A+32.6%2C+xmax%3A+-117.36%2C+ymax%3A+32.9&geometryType=esriGeometryEnvelope&inSR=4236&spatialRel=esriSpatialRelIntersects&returnGeometry=true&outSR=102100&returnDistinctValues=false&returnIdsOnly=false&returnCountOnly=false&returnZ=false&returnM=false&f=pjson";
	// use this service with user credentials
	String closestFacilityProdUrl = "http://route.arcgis.com/arcgis/rest/services/World/ClosestFacility/NAServer/ClosestFacility_World";
	// default sample service, does not require credentials
	String closestFacilitySampleUrl = "http://sampleserver6.arcgisonline.com/arcgis/rest/services/NetworkAnalysis/SanDiego/NAServer/ClosestFacility";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// add map view with web map
		mMapView = (MapView) findViewById(R.id.map);

		// wait for web map to load prior to adding graphic layer
		mMapView.setOnStatusChangedListener(new OnStatusChangedListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void onStatusChanged(Object source, STATUS status) {
				if (source == mMapView && status == STATUS.INITIALIZED) {
					// Add the route graphic layer (shows the full route)
					routeLayer = new GraphicsLayer();
					mMapView.addLayer(routeLayer);
				}
			}

		});

		mMapView.setOnSingleTapListener(new OnSingleTapListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void onSingleTap(float x, float y) {
				// create a graphic for facility
				final SimpleMarkerSymbol sms = new SimpleMarkerSymbol(
						Color.BLACK, 13, SimpleMarkerSymbol.STYLE.DIAMOND);
				// set start location
				Point point = mMapView.toMapPoint(x, y);
				// create graphic
				final Graphic graphic = new Graphic(point, sms);
				// set parameters graphic and query url
				setParameters(graphic, queryUrl);

			}
		});

	}

	void setParameters(Graphic graphic, String url) {
		// create graphic for location to start
		NAFeaturesAsFeature myLocationFeature = new NAFeaturesAsFeature();
		myLocationFeature.addFeature(graphic);
		myLocationFeature.setSpatialReference(mMapView.getSpatialReference());
		// create feature based on query url limiting stations by city
		NAFeaturesAsFeature nafaf = new NAFeaturesAsFeature();
		nafaf.setSpatialReference(mMapView.getSpatialReference());
		nafaf.setURL(url);

		// set up parameters
		ClosestFacilityParameters cfp = new ClosestFacilityParameters();
		cfp.setReturnFacilities(true);
		cfp.setOutSpatialReference(mMapView.getSpatialReference());
		// route direction to facility
		cfp.setTravelDirection(NATravelDirection.TO_FACILITY);
		// set incident to single tap location
		cfp.setIncidents(myLocationFeature);
		// set facilities to query url
		cfp.setFacilities(nafaf);
		cfp.setDefaultTargetFacilityCount(Integer.valueOf(1));
		FindClosestFacilities find = new FindClosestFacilities();
		// execute task
		find.execute(cfp);
	}

	private class FindClosestFacilities extends
			AsyncTask<ClosestFacilityParameters, Void, ClosestFacilityResult> {

		// default constructor
		public FindClosestFacilities() {
		}

		@Override
		protected void onPreExecute() {
			// remove any previous routes
			routeLayer.removeAll();
			// show progress dialog while geocoding address
			dialog = ProgressDialog.show(mMapView.getContext(),
					"ClosestFacilities",
					"Searching for route to closest facility ...");
		}

		@Override
		protected ClosestFacilityResult doInBackground(
				ClosestFacilityParameters... params) {

			ClosestFacilityResult result = null;

			// check if credentials set and choose service
			if (user == "" && pass == "") {
				auth = false;
				closestFacilityTask = new ClosestFacilityTask(
						closestFacilitySampleUrl, null);
			} else {
				auth = true;
				UserCredentials uc = new UserCredentials();
				uc.setUserAccount(user, pass);
				closestFacilityTask = new ClosestFacilityTask(
						closestFacilityProdUrl, uc);
			}

			try {
				// solve the route
				result = closestFacilityTask.solve(params[0]);
			} catch (EsriSecurityException ese) {
				// username and password incorrect
				auth = false;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return result;
		}

		@Override
		protected void onPostExecute(ClosestFacilityResult result) {
			// dismiss dialog
			if (dialog.isShowing()) {
				dialog.dismiss();
			}

			// The result of FindClosestFacilities task is passed as a parameter
			// to map the results
			if (result == null && auth) {
				// update UI with notice that no results were found
				Toast toast = Toast.makeText(MainActivity.this,
						"User name and password not valid", Toast.LENGTH_LONG);
				toast.show();
			} else if (result == null && !auth) {
				// update UI with notice that user was not authenticated
				Toast toast = Toast.makeText(MainActivity.this,
						"No result found.", Toast.LENGTH_LONG);
				toast.show();

			} else {
				route = result.getRoutes().get(0);
				// Symbols for the route and the destination
				SimpleLineSymbol routeSymbol = new SimpleLineSymbol(Color.BLUE,
						3);
				PictureMarkerSymbol destinationSymbol = new PictureMarkerSymbol(
						mMapView.getContext(), getResources().getDrawable(
								R.drawable.route_destination));

				Graphic routeGraphic = new Graphic(route.getRouteGraphic()
						.getGeometry(), routeSymbol);
				Graphic endGraphic = new Graphic(
						((Polyline) routeGraphic.getGeometry()).getPoint(((Polyline) routeGraphic
								.getGeometry()).getPointCount() - 1),
						destinationSymbol);
				// Get the full route summary
				routeLayer
						.addGraphics(new Graphic[] { routeGraphic, endGraphic });
				// Zoom to the extent of the entire route with a padding
				mMapView.setExtent(route.getEnvelope(), 100);
			}
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
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

	// create user/pass to use commercial service
	static String user = "";
	static String pass = "";

}
