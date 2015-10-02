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
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.LocationDisplayManager;
import com.esri.android.map.LocationDisplayManager.AutoPanMode;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.event.OnLongPressListener;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polyline;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.PictureMarkerSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.tasks.na.NAFeaturesAsFeature;
import com.esri.core.tasks.na.Route;
import com.esri.core.tasks.na.RouteDirection;
import com.esri.core.tasks.na.RouteParameters;
import com.esri.core.tasks.na.RouteResult;
import com.esri.core.tasks.na.RouteTask;
import com.esri.core.tasks.na.StopGraphic;

public class RoutingSample extends Activity implements
		RoutingListFragment.onDrawerListSelectedListener,
		RoutingDialogFragment.onGetRoute {
	public static MapView map = null;
	ArcGISTiledMapServiceLayer tileLayer;
	GraphicsLayer routeLayer, hiddenSegmentsLayer;
	public LocationManager manager;
	// Symbol used to make route segments "invisible"
	SimpleLineSymbol segmentHider = new SimpleLineSymbol(Color.WHITE, 5);
	// Symbol used to highlight route segments
	SimpleLineSymbol segmentShower = new SimpleLineSymbol(Color.RED, 5);
	// Label showing the current direction, time, and length
	TextView directionsLabel;
	// List of the directions for the current route (used for the ListActivity)
	public static ArrayList<String> curDirections = null;
	// Current route, route summary, and gps location
	Route curRoute = null;
	String routeSummary = null;
	public static Point mLocation = null;
	// Global results variable for calculating route on separate thread
	RouteTask mRouteTask = null;
	RouteResult mResults = null;
	// Variable to hold server exception to show to user
	Exception mException = null;

	ImageView img_cancel;
	ImageView img_currLocation;
	ImageView img_getDirections;
	public static DrawerLayout mDrawerLayout;
	LocationDisplayManager ldm;
	// Handler for processing the results
	final Handler mHandler = new Handler();
	final Runnable mUpdateResults = new Runnable() {
		public void run() {
			updateUI();
		}
	};

	// Progress dialog to show when route is being calculated
	ProgressDialog dialog;
	// Spatial references used for projecting points
	final SpatialReference wm = SpatialReference.create(102100);
	final SpatialReference egs = SpatialReference.create(4326);
	// Index of the currently selected route segment (-1 = no selection)
	int selectedSegmentID = -1;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

		if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
			buildAlertMessageNoGps();
		}
		// Retrieve the map and initial extent from XML layout
		map = (MapView) findViewById(R.id.map);
		// Add tiled layer to MapView
		tileLayer = new ArcGISTiledMapServiceLayer(
				"http://services.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer");
		map.addLayer(tileLayer);





		// Add the route graphic layer (shows the full route)
		routeLayer = new GraphicsLayer();
		map.addLayer(routeLayer);

		// Initialize the RouteTask
		try {
			mRouteTask = RouteTask
					.createOnlineRouteTask(
							"http://sampleserver3.arcgisonline.com/ArcGIS/rest/services/Network/USA/NAServer/Route",
							null);
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		// Add the hidden segments layer (for highlighting route segments)
		hiddenSegmentsLayer = new GraphicsLayer();
		map.addLayer(hiddenSegmentsLayer);

		// Make the segmentHider symbol "invisible"
		segmentHider.setAlpha(1);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		img_cancel = (ImageView) findViewById(R.id.iv_cancel);
		img_currLocation = (ImageView) findViewById(R.id.iv_myLocation);
		img_getDirections = (ImageView) findViewById(R.id.iv_getDirections);
		mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

		// Get the location display manager and start reading location. Don't
		// auto-pan
		// to center our position
		ldm = map.getLocationDisplayManager();
		ldm.setLocationListener(new MyLocationListener());
		ldm.start();
		ldm.setAutoPanMode(AutoPanMode.OFF);

		// Set the directionsLabel with initial instructions.
		directionsLabel = (TextView) findViewById(R.id.directionsLabel);
		directionsLabel.setText(getString(R.string.route_label));

		/**
		 * On single clicking the directions label, start a ListActivity to show
		 * the list of all directions for this route. Selecting one of those
		 * items will return to the map and highlight that segment.
		 * 
		 */
		directionsLabel.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				if (curDirections == null)
					return;

				mDrawerLayout.openDrawer(Gravity.END);

				String segment = directionsLabel.getText().toString();

				ListView lv = RoutingListFragment.mDrawerList;
				for (int i = 0; i < lv.getCount() - 1; i++) {
					String lv_segment = lv.getItemAtPosition(i).toString();
					if (segment.equals(lv_segment)) {
						lv.setSelection(i);
					}
				}
			}

		});
		img_cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {


				clearAll();
			}
		});

		img_currLocation.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Point p = (Point) GeometryEngine.project(mLocation, egs, wm);
				map.zoomToResolution(p, 20.0);

			}
		});

		img_getDirections.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				FragmentManager fm = getFragmentManager();
				FragmentTransaction ft = fm.beginTransaction();
				RoutingDialogFragment frag_dialog = new RoutingDialogFragment();
				ft.add(frag_dialog, "Dialog");
				ft.commit();

			}
		});

		/**
		 * On single tapping the map, query for a route segment and highlight
		 * the segment and show direction summary in the label if a segment is
		 * found.
		 */
		map.setOnSingleTapListener(new OnSingleTapListener() {
			private static final long serialVersionUID = 1L;

			public void onSingleTap(float x, float y) {
				// Get all the graphics within 20 pixels the click
				int[] indexes = hiddenSegmentsLayer.getGraphicIDs(x, y, 20);
				// Hide the currently selected segment
				hiddenSegmentsLayer.updateGraphic(selectedSegmentID,
						segmentHider);

				if (indexes.length < 1) {
					// If no segments were found but there is currently a route,
					// zoom to the extent of the full route
					if (curRoute != null) {
						map.setExtent(curRoute.getEnvelope(), 250);
						directionsLabel.setText(routeSummary);
					}
					return;
				}
				// Otherwise update our currently selected segment
				selectedSegmentID = indexes[0];
				Graphic selected = hiddenSegmentsLayer
						.getGraphic(selectedSegmentID);
				// Highlight it on the map
				hiddenSegmentsLayer.updateGraphic(selectedSegmentID,
						segmentShower);
				String direction = ((String) selected.getAttributeValue("text"));
				double time = (Double) selected.getAttributeValue("time");
				double length = (Double) selected.getAttributeValue("length");
				// Update the label with this direction's information
				String label = String.format("%s%n%.1f minutes (%.1f miles)",
						direction, time, length);
				directionsLabel.setText(label);
				// Zoom to the extent of that segment
				map.setExtent(selected.getGeometry(), 50);
			}

		});

		/**
		 * On long pressing the map view, route from our current location to the
		 * pressed location.
		 * 
		 */
		map.setOnLongPressListener(new OnLongPressListener() {

			private static final long serialVersionUID = 1L;

			public boolean onLongPress(final float x, final float y) {
				final Point loc = map.toMapPoint(x, y);

				Point p = (Point) GeometryEngine.project(loc, wm, egs);

				clearAll();
				QueryDirections(mLocation, p);
				return true;
			}

		});
	}

	private void QueryDirections(final Point mLocation, final Point p) {

		// Show that the route is calculating
		dialog = ProgressDialog.show(RoutingSample.this, "Routing Sample",
				"Calculating route...", true);
		// Spawn the request off in a new thread to keep UI responsive
		Thread t = new Thread() {
			@Override
			public void run() {
				try {
					// Start building up routing parameters
					RouteParameters rp = mRouteTask
							.retrieveDefaultRouteTaskParameters();
					NAFeaturesAsFeature rfaf = new NAFeaturesAsFeature();
					// Convert point to EGS (decimal degrees)
					// Create the stop points (start at our location, go
					// to pressed location)
					StopGraphic point1 = new StopGraphic(mLocation);
					StopGraphic point2 = new StopGraphic(p);
					rfaf.setFeatures(new Graphic[] { point1, point2 });
					rfaf.setCompressedRequest(true);
					rp.setStops(rfaf);
					// Set the routing service output SR to our map
					// service's SR
					rp.setOutSpatialReference(wm);

					// Solve the route and use the results to update UI
					// when received
					mResults = mRouteTask.solve(rp);
					mHandler.post(mUpdateResults);
				} catch (Exception e) {
					mException = e;
					mHandler.post(mUpdateResults);
				}
			}
		};
		// Start the operation
		t.start();

	}

	/**
	 * If GPS is disabled, app won't be able to route. Hence display a dialoge window to enable the GPS
	 */
	private void buildAlertMessageNoGps() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Please enable your GPS before proceeding")
				.setCancelable(false)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
						startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
					}
				});
		final AlertDialog alert = builder.create();
		alert.show();
	}

	/**
	 * Updates the UI after a successful rest response has been received.
	 */
	void updateUI() {
		dialog.dismiss();

		if (mResults == null) {
			Toast.makeText(RoutingSample.this, mException.toString(),
					Toast.LENGTH_LONG).show();
			curDirections = null;
			return;
		}

		// Creating a fragment if it has not been created
		FragmentManager fm = getFragmentManager();
		if (fm.findFragmentByTag("Nav Drawer") == null) {
			FragmentTransaction ft = fm.beginTransaction();
			RoutingListFragment frag = new RoutingListFragment();
			ft.add(frag, "Nav Drawer");
			ft.commit();
		} else {
			FragmentTransaction ft = fm.beginTransaction();
			ft.remove(fm.findFragmentByTag("Nav Drawer"));
			RoutingListFragment frag = new RoutingListFragment();
			ft.add(frag, "Nav Drawer");
			ft.commit();
		}

		// Unlock the NAvigation Drawer
		mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);

		// Making visible the cancel icon
		img_cancel.setVisibility(View.VISIBLE);

		curRoute = mResults.getRoutes().get(0);
		// Symbols for the route and the destination (blue line, checker flag)
		SimpleLineSymbol routeSymbol = new SimpleLineSymbol(Color.BLUE, 3);
		PictureMarkerSymbol destinationSymbol = new PictureMarkerSymbol(
				map.getContext(), getResources().getDrawable(
						R.drawable.ic_action_place));

		// Add all the route segments with their relevant information to the
		// hiddenSegmentsLayer, and add the direction information to the list
		// of directions
		for (RouteDirection rd : curRoute.getRoutingDirections()) {
			HashMap<String, Object> attribs = new HashMap<>();
			attribs.put("text", rd.getText());
			attribs.put("time", Double.valueOf(rd.getMinutes()));
			attribs.put("length", Double.valueOf(rd.getLength()));
			curDirections.add(String.format("%s%n%.1f minutes (%.1f miles)",
					rd.getText(), rd.getMinutes(), rd.getLength()));
			Graphic routeGraphic = new Graphic(rd.getGeometry(), segmentHider,
					attribs);
			hiddenSegmentsLayer.addGraphic(routeGraphic);
		}
		// Reset the selected segment
		selectedSegmentID = -1;

		// Add the full route graphics, start and destination graphic to the
		// routeLayer
		Graphic routeGraphic = new Graphic(curRoute.getRouteGraphic()
				.getGeometry(), routeSymbol);
		Graphic endGraphic = new Graphic(
				((Polyline) routeGraphic.getGeometry()).getPoint(((Polyline) routeGraphic
						.getGeometry()).getPointCount() - 1), destinationSymbol);
		routeLayer.addGraphics(new Graphic[] { routeGraphic, endGraphic });
		// Get the full route summary and set it as our current label
		routeSummary = String.format("%s%n%.1f minutes (%.1f miles)",
				curRoute.getRouteName(), curRoute.getTotalMinutes(),
				curRoute.getTotalMiles());

		directionsLabel.setText(routeSummary);
		// Zoom to the extent of the entire route with a padding
		map.setExtent(curRoute.getEnvelope(), 250);

		// Replacing the first and last direction segments
		curDirections.remove(0);
		curDirections.add(0, "My Location");

		curDirections.remove(curDirections.size() - 1);
		curDirections.add("Destination");
	}

	private class MyLocationListener implements LocationListener {

		public MyLocationListener() {
			super();
		}

		/**
		 * If location changes, update our current location. If being found for
		 * the first time, zoom to our current position with a resolution of 20
		 */
		public void onLocationChanged(Location loc) {
			if (loc == null)
				return;
			boolean zoomToMe = (mLocation == null);
			mLocation = new Point(loc.getLongitude(), loc.getLatitude());
			if (zoomToMe) {
				Point p = (Point) GeometryEngine.project(mLocation, egs, wm);
				map.zoomToResolution(p, 20.0);
			}
		}

		public void onProviderDisabled(String provider) {
			Toast.makeText(getApplicationContext(), "GPS Disabled",
					Toast.LENGTH_SHORT).show();
			buildAlertMessageNoGps();
		}

		public void onProviderEnabled(String provider) {
			Toast.makeText(getApplicationContext(), "GPS Enabled",
					Toast.LENGTH_SHORT).show();
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}

	}

	@Override
	protected void onPause() {
		super.onPause();
		map.pause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		map.unpause();
	}

	/*
	 * When the user selects the segment from the listview, it gets highlighted
	 * on the map
	 */
	@Override
	public void onSegmentSelected(String segment) {

		if (segment == null)
			return;
		// Look for the graphic that corresponds to this direction
		for (int index : hiddenSegmentsLayer.getGraphicIDs()) {
			Graphic g = hiddenSegmentsLayer.getGraphic(index);
			if (segment.contains((String) g.getAttributeValue("text"))) {
				// When found, hide the currently selected, show the new
				// selection
				hiddenSegmentsLayer.updateGraphic(selectedSegmentID,
						segmentHider);
				hiddenSegmentsLayer.updateGraphic(index, segmentShower);
				selectedSegmentID = index;
				// Update label with information for that direction
				directionsLabel.setText(segment);
				// Zoom to the extent of that segment
				map.setExtent(hiddenSegmentsLayer.getGraphic(selectedSegmentID)
						.getGeometry(), 50);
				break;
			}
		}

	}

	@Override
	public void onDialogRouteClicked(Point p1, Point p2) {

		FragmentManager fm = getFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		ft.remove(fm.findFragmentByTag("Dialog")).commit();

		Point p_start = (Point) GeometryEngine.project(p1, wm, egs);
		Point p_dest = (Point) GeometryEngine.project(p2, wm, egs);

		clearAll();

		// Adding the symbol for start point
		SimpleMarkerSymbol startSymbol = new SimpleMarkerSymbol(Color.DKGRAY,
				15, SimpleMarkerSymbol.STYLE.CIRCLE);
		Graphic gStart = new Graphic(p1, startSymbol);
		routeLayer.addGraphic(gStart);

		QueryDirections(p_start, p_dest);

	}

	/*
	 * Clear the graphics and empty the directions list
	 */

	public void clearAll() {
		
		//Removing the graphics from the layer
		routeLayer.removeAll();
		hiddenSegmentsLayer.removeAll();
		
		curDirections = new ArrayList<>();
		mResults = null;
		curRoute = null;
		
		//Setting to default text
		directionsLabel.setText(getString(R.string.route_label));
		
		//Locking the Drawer
		mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
		
		//Removing the cancel icon
		img_cancel.setVisibility(View.GONE);

		//Removing the RoutingListFragment if present
		FragmentManager fm = getFragmentManager();
		if (fm.findFragmentByTag("Nav Drawer") != null) {
			FragmentTransaction ft = fm.beginTransaction();
			ft.remove(fm.findFragmentByTag("Nav Drawer"));
			ft.commit();
		}

	}

}