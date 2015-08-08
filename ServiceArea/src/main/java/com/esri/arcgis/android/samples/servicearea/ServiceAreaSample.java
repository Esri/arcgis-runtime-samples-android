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

package com.esri.arcgis.android.samples.servicearea;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.app.ActionBar;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.tasks.na.NAFeaturesAsFeature;
import com.esri.core.tasks.na.ServiceAreaParameters;
import com.esri.core.tasks.na.ServiceAreaResult;
import com.esri.core.tasks.na.ServiceAreaTask;

/**
 * A service area is a region that encompasses all accessible streets. 
 * For instance, the 5-minute service area for a point on a network includes all 
 * the streets that can be reached within five minutes from that point.
 * The user has a leverage to select breakpoints from the action bar to perform
 * the search as desired by the user. It uses the Async Task to perform the
 * activity and return the results via a graphics service layer on the
 * map.Implemented Fragments to add the actions to set the break points.
 * 
 */
public class ServiceAreaSample extends Activity implements
		EditFragment.OnDialogClickedListener {

	MapView mMapView = null;
	ArcGISTiledMapServiceLayer baseMap = null;

	// Graphics layer for displaying the service area polygons
	GraphicsLayer serviceAreaLayer = null;

	// Spatial references used for projecting points
	final SpatialReference wm = SpatialReference.create(102100);
	final SpatialReference egs = SpatialReference.create(4326);

	// Three text boxes for specifying the break values
	EditText break1, break2, break3;

	// The action bar
	ActionBar action = null;

	// Dialog to check progress for the service task
	static ProgressDialog dialog = null;
	static AlertDialog.Builder alertDialogBuilder = null;
	static AlertDialog alertDialog = null;

	// Fragment Manager to add interfaces for fragments
	FragmentManager fm = getFragmentManager();

	// Adds Fragments concerned to the Fragment Manager
	FragmentTransaction trans = fm.beginTransaction();
	
	// Edit Fragment
    EditFragment editFragment = new EditFragment();
	
	// default value
	double breakValue1 = 1.0, breakValue2 = 2.0, breakValue3 = 3.0;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// Retrieve the map and initial extent from XML layout
		mMapView = (MapView) findViewById(R.id.map);

		// Add the service area graphic layer (shows the SA polygons)
		serviceAreaLayer = new GraphicsLayer();
		mMapView.addLayer(serviceAreaLayer);

		// Adding the action bar
		action = getActionBar();

		/**
		 * On single tapping the map, calculate the three service area polygons
		 * defined by our three break value EditTexts, using the location
		 * clicked as the source facility
		 */
		mMapView.setOnSingleTapListener(new OnSingleTapListener() {
			private static final long serialVersionUID = 1L;

			public void onSingleTap(float x, float y) {
				// retrieve the user clicked location
				final Point location = mMapView.toMapPoint(x, y);

				SolveServiceArea solveArea = new SolveServiceArea();
				solveArea.execute(location);

			}
		});
	}

	/**
	 * Creates the menu items on the action bar
	 */
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.action, menu);
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * Action for the item Selected from the menu.
	 */
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {

		case R.id.action_refresh:
			Toast.makeText(this, getString(R.string.action_refresh),
					Toast.LENGTH_LONG).show();
			serviceAreaLayer.removeAll();
			return true;

		case R.id.set_breaks:
			fm = getFragmentManager();
			trans = fm.beginTransaction();
			editFragment = new EditFragment();

			trans.add(editFragment, "Set Breaks");
			trans.commit();

			Toast.makeText(this, getString(R.string.set_breaks),
					Toast.LENGTH_LONG).show();
			return true;
		}
		return false;
	}

	/**
	 * Create a TextView containing the message
	 * 
	 * @param text
	 *            The text view's content
	 * @return The TextView
	 */
	TextView message(String text) {

		final TextView msg = new TextView(this);
		msg.setText(text);
		msg.setTextSize(12);
		msg.setTextColor(Color.BLACK);
		return msg;

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

	/**
	 * The Task perform on the service layer has 3 methods to perform each time
	 * i.e. Pre-Execute, Do-In-Background and the Post-Execute.
	 * 
	 * @author Esri Android Team
	 */
	class SolveServiceArea extends AsyncTask<Point, Void, ServiceAreaResult> {

		protected void onPreExecute() {
			dialog = ProgressDialog.show(ServiceAreaSample.this, "",
					"Calculating the extent of Service Area");
		}

		@Override
		protected ServiceAreaResult doInBackground(Point... params) {
			Point startLocation = params[0];

			try {
				// Start building up service area parameters
				ServiceAreaParameters sap = new ServiceAreaParameters();
				NAFeaturesAsFeature nfaf = new NAFeaturesAsFeature();
				// Convert point to EGS (decimal degrees)
				Point p = (Point) GeometryEngine
						.project(startLocation, wm, egs);
				nfaf.addFeature(new Graphic(p, null));
				sap.setFacilities(nfaf);
				// Set the service area output SR to our map service's SR
				sap.setOutSpatialReference(wm);

				// Set the default break values with our entered values

				sap.setDefaultBreaks(new Double[] { breakValue1, breakValue2,
						breakValue3 });

				// Create a new service area task pointing to an NAService
				// (null credentials -> free service)
				ServiceAreaTask sat = new ServiceAreaTask(
						ServiceAreaSample.this.getResources().getString(
								R.string.naservice_url), null);

				// Solve the service area and retrieve the result.
				ServiceAreaResult saResult = sat.solve(sap);
				return saResult;

			} catch (Exception e) {

				e.printStackTrace();
				mMapView.getCallout().show(startLocation,
						message("Exception occurred"));
				return null;
			}

		}

		protected void onPostExecute(ServiceAreaResult result) {
			dialog.dismiss();

			ServiceAreaResult saResult = result;

			if (saResult != null) {
				// Symbol for the smallest service area polygon
				SimpleFillSymbol smallSymbol = new SimpleFillSymbol(Color.GREEN);
				smallSymbol.setAlpha(128);
				// Symbol for the medium service area polygon
				SimpleFillSymbol mediumSymbol = new SimpleFillSymbol(
						Color.YELLOW);
				mediumSymbol.setAlpha(128);
				// Symbol for the largest service area polygon
				SimpleFillSymbol largeSymbol = new SimpleFillSymbol(Color.RED);
				largeSymbol.setAlpha(128);

				// Create and add the service area graphics to the service
				// area Layer
				Graphic smallGraphic = new Graphic(
						saResult.getServiceAreaPolygons().getGraphics()[2]
								.getGeometry(),
						smallSymbol);
				Graphic mediumGraphic = new Graphic(
						saResult.getServiceAreaPolygons().getGraphics()[1]
								.getGeometry(),
						mediumSymbol);
				Graphic largeGraphic = new Graphic(
						saResult.getServiceAreaPolygons().getGraphics()[0]
								.getGeometry(),
						largeSymbol);
				serviceAreaLayer.addGraphics(new Graphic[] { smallGraphic,
						mediumGraphic, largeGraphic });
				// Zoom to the extent of the service area polygon with a
				// padding
				mMapView.setExtent(largeGraphic.getGeometry(), 50);
			} else {
				// send response to user
				alertDialogBuilder = new AlertDialog.Builder(
						ServiceAreaSample.this);
				alertDialogBuilder.setTitle("Query Response");
				alertDialogBuilder.setMessage(ServiceAreaSample.this
						.getResources().getString(R.string.sa_null_response));
				alertDialogBuilder.setCancelable(true);
				// create alert dialog
				alertDialog = alertDialogBuilder.create();
				alertDialog.show();
			}

		}

	}

	/**
	 * Invoked after you tap OK on the dialog.
	 */
	@Override
	public void onDialogClicked(double a1, double a2, double a3) {
		// TODO Auto-generated method stub
		FragmentTransaction trans1 = fm.beginTransaction();
		trans1.remove(fm.findFragmentByTag("Set Breaks"));
		trans1.commit();

		breakValue1 = a1;
		breakValue2 = a2;
		breakValue3 = a3;

	}

}