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

package com.esri.arcgis.android.samples.nearby;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.android.map.Callout;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.LocationDisplayManager;
import com.esri.android.map.LocationDisplayManager.AutoPanMode;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.LinearUnit;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.geometry.Unit;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.PictureMarkerSymbol;
import com.esri.core.symbol.Symbol;

/**
 * The purpose of this sample is to leverage your devices GPS position to zoom
 * to your location on the map. When you click on the either of the buttons on
 * the action bar, coffee denoting local coffee shops or beer mug denoting bars,
 * the respective shops in your area are displayed on the map. Check device
 * setting to make sure that location services are enabled for the app.
 * 
 */
public class Nearby extends Activity {

	final static int TITLE_ID = 1;
	final static int REVIEW_ID = 2;
	// The circle area specified by search_radius and input lat/lon serves
	// searching purpose. It is also used to construct the extent which
	// map zooms to after the first GPS fix is retrieved.
	final static double SEARCH_RADIUS = 5;

	MapView mMapView = null;
	ArcGISTiledMapServiceLayer mBaseMap;
	GraphicsLayer graphicsLayer = null;
	PictureMarkerSymbol coffeeIcon, barIcon;
	JSONObject results = null;
	ProgressDialog progress;
	LocationDisplayManager lDisplayManager;

	View content;
	Callout callout;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mMapView = (MapView) findViewById(R.id.map);
		mBaseMap = new ArcGISTiledMapServiceLayer(getResources().getString(R.string.basemap_url));
		mMapView.addLayer(mBaseMap);

		callout = mMapView.getCallout();
		callout.setStyle(R.xml.calloutstyle);

		content = createContent();

		graphicsLayer = new GraphicsLayer();
		mMapView.addLayer(graphicsLayer);

		coffeeIcon = new PictureMarkerSymbol(getApplicationContext(), this
				.getResources().getDrawable(R.drawable.ic_coffee_dark));
		barIcon = new PictureMarkerSymbol(getApplicationContext(), this
				.getResources().getDrawable(R.drawable.ic_bar_dark));


		mMapView.setOnStatusChangedListener(new OnStatusChangedListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void onStatusChanged(Object source, STATUS status) {
				if (source == mMapView && status == STATUS.INITIALIZED) {
					lDisplayManager = mMapView.getLocationDisplayManager();
					lDisplayManager.setAutoPanMode(AutoPanMode.LOCATION);
					lDisplayManager.setLocationListener(new LocationListener() {

						boolean locationChanged = false;

						// Zooms to the current location when first GPS fix arrives.
						@Override
						public void onLocationChanged(Location loc) {
							if (!locationChanged) {
								locationChanged = true;
								double locy = loc.getLatitude();
								double locx = loc.getLongitude();
								Point wgspoint = new Point(locx, locy);
								Point mapPoint = (Point) GeometryEngine
										.project(wgspoint,
												SpatialReference.create(4326),
												mMapView.getSpatialReference());

								Unit mapUnit = mMapView.getSpatialReference()
										.getUnit();
								double zoomWidth = Unit.convertUnits(
										SEARCH_RADIUS,
										Unit.create(LinearUnit.Code.MILE_US),
										mapUnit);
								Envelope zoomExtent = new Envelope(mapPoint,
										zoomWidth, zoomWidth);
								mMapView.setExtent(zoomExtent);

							}

						}

						@Override
						public void onProviderDisabled(String arg0) {

						}

						@Override
						public void onProviderEnabled(String arg0) {
						}

						@Override
						public void onStatusChanged(String arg0, int arg1,
								Bundle arg2) {

						}
					});
					lDisplayManager.start();

				}

			}
		});

		mMapView.setOnSingleTapListener(new OnSingleTapListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void onSingleTap(float x, float y) {

				callout.hide();

				// Handles the tapping on Graphic

				int[] graphicIDs = graphicsLayer.getGraphicIDs(x, y, 25);
				if (graphicIDs != null && graphicIDs.length > 0) {
					Graphic gr = graphicsLayer.getGraphic(graphicIDs[0]);
					updateContent((String) gr.getAttributeValue("Rating"),
							(String) gr.getAttributeValue("Title"));
					Point location = (Point) gr.getGeometry();
					callout.setOffset(0, -15);
					callout.show(location, content);
				}

			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_layout, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.bar:
			if (mMapView.isLoaded()) {
				try {
					AsyncLocalSearch asycst = new AsyncLocalSearch();
					String[] searchCriteria = { "bar" };
					asycst.execute(searchCriteria);

				} catch (Exception e) {
					e.printStackTrace();
				}

			}
			return true;
		case R.id.coffee:
			if (mMapView.isLoaded()) {
				try {
					AsyncLocalSearch asycst = new AsyncLocalSearch();
					String[] searchCriteria = { "coffee" };
					asycst.execute(searchCriteria);

				} catch (Exception e) {
					e.printStackTrace();
				}

			}

			return true;
		default:
			return super.onOptionsItemSelected(item);

		}
	}

	/**
	 * Creates a LinearLayout which contains tile and rating.
	 * 
	 * @return content view to be added to the callout.
	 */

	public View createContent() {
		// create linear layout for the entire view
		LinearLayout layout = new LinearLayout(this);
		layout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		layout.setOrientation(LinearLayout.VERTICAL);

		// create TextView for the title
		TextView titleView = new TextView(this);
		titleView.setId(TITLE_ID);
		titleView.setTextColor(Color.GRAY);
		layout.addView(titleView);

		StarView sv = new StarView(this);
		sv.setId(REVIEW_ID);
		layout.addView(sv);

		return layout;
	}

	/**
	 * Populates the content view with rating and title
	 * 
	 * @param rating
	 *            rating for the business
	 * @param title
	 *            title of the business
	 */
	public void updateContent(String rating, String title) {
		if (content == null)
			return;

		TextView txt = (TextView) content.findViewById(TITLE_ID);
		txt.setText(title);

		StarView sv = (StarView) content.findViewById(REVIEW_ID);

		try {
			int val = (int) Double.parseDouble(rating);

			switch (val) {
			case 1:
				sv.setLevel(1);
				break;

			case 2:
				sv.setLevel(2);
				break;

			case 3:
				sv.setLevel(3);
				break;

			case 4:
				sv.setLevel(4);
				break;

			case 5:
				sv.setLevel(5);
				break;

			default:
				sv.setLevel(0);
				break;
			}

		} catch (Exception e) {
			e.printStackTrace();

		}

	}

	/*
	 * Executes yahoo local search task asynchronously. The first parameter is
	 * the query criteria like'coffee', 'shop', etc.
	 */

	private class AsyncLocalSearch extends AsyncTask<String, Void, Boolean> {
		// Determine if the query returned an array of results
		boolean success = false;

		@Override
		protected void onPostExecute(Boolean result) {
			progress.dismiss();
			callout.hide();
			// send toast message based on results of query
			if (!success) {
				// No search results
				Toast toast = Toast.makeText(Nearby.this, "No search results",
						Toast.LENGTH_LONG);
				toast.show();
			} else {
				// Search results
				Toast toast = Toast.makeText(Nearby.this,
						"Please tap on graphic for detailed information",
						Toast.LENGTH_LONG);
				toast.show();
			}
		}

		@Override
		protected void onPreExecute() {
			// show progress bar while executing task
			progress = ProgressDialog.show(Nearby.this, "",
					"Please wait for search results coming back....", true);
		}

		@SuppressWarnings("boxing")
		@Override
		protected Boolean doInBackground(String... params) {
			// handle case of no parameters
			if (params == null || params.length == 0) {
				success = false;
				return success;
			}
			// remove any previous graphics
			graphicsLayer.removeAll();

			// Creates URL that is passed to yahoo local search web service end
			// point
			String criteria = params[0];

			// base url
			String baseURL = "http://query.yahooapis.com/v1/public/yql?q=";
			// base query
			String query = "select * from local.search where";
			// get user location
			Location location = mMapView.getLocationDisplayManager()
					.getLocation();
			// convert to lat/lon
			String latStr = Double.toString(location.getLatitude());
			String lonStr = Double.toString(location.getLongitude());
			// create query from user location and defined SEARCH_RADIUS
			query += (" latitude=" + latStr + " and " + "longitude=" + lonStr
					+ " and " + "radius=" + Double.toString(SEARCH_RADIUS)
					+ " and query='" + criteria + "'");
			// encode the query part of the url
			String encodeURL = baseURL + Uri.encode(query)
					+ "&format=json&diagnostics=true&callback=";

			// Uses URLConnection to communicate with yahool local search web
			// service end point. Once results come back, create a JSON object
			// and get a JSON array from its content. While iterating the JSON
			// array, we extract each result's attributes that we are interested
			// in,for example, location, title, and rating.
			// A graphic is created by using these attributes for each result,
			// and therefore added into a GraphicsLayer.

			URL url = null;
			try {
				// create the URL from encoded string
				url = new URL(encodeURL);
				// open the connection
				URLConnection urlResponse = null;
				urlResponse = url.openConnection();
				// read the response from URLConnection
				BufferedReader br = new BufferedReader(new InputStreamReader(
						urlResponse.getInputStream()));
				String jsonString = "";
				String line;
				while ((line = br.readLine()) != null) {
					jsonString += line;
				}

				br.close();

				// parse the return JSON.
				JSONObject queryResponse = new JSONObject(jsonString);
				// every response from YQL includes a query element
				queryResponse = queryResponse.getJSONObject("query");
				// query element contains a results element
				JSONObject jsonResults = queryResponse.getJSONObject("results");
				// create a JSONArray to hold elements in results
				JSONArray result = null;

				// Get the JSONArray value associated with the Result key
				try {
					result = jsonResults.getJSONArray("Result");
				} catch (JSONException e) {
					// Result element not an array
					success = false;
					Log.d("ERROR", "JSON Object not an array");
					return success;
				}
				// Result element is an array to parse
				success = true;

				// Get the number of search results in this set
				int resultCount = queryResponse.length();

				// Loop over each result and print the title, summary, and URL
				for (int i = 0; i < resultCount; i++) {
					try {
						JSONObject resultObject = result.getJSONObject(i);
						jsonResults = resultObject;
						Point p = new Point(
								Float.valueOf((String) (resultObject
										.get("Longitude"))),
								Float.valueOf((String) (resultObject
										.get("Latitude"))));
						String title = resultObject.getString("Title");
						JSONObject object = resultObject
								.getJSONObject("Rating");
						String rating = (String) object.get("AverageRating");

						Point point = (Point) GeometryEngine.project(p,
								SpatialReference.create(4326),
								mMapView.getSpatialReference());
						Symbol symbol = null;
						if (criteria == "coffee")
							symbol = coffeeIcon;
						else
							symbol = barIcon;

						HashMap<String, Object> attrMap = new HashMap<String, Object>();
						attrMap.put("Title", title);
						attrMap.put("Rating", rating);
						graphicsLayer.addGraphic(new Graphic(point, symbol,
								attrMap));

					} catch (JSONException r) {
						r.printStackTrace();
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			return success;
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mMapView = null;
	}

	/**
	 * 
	 * Custom view to draw the rating star
	 * 
	 */
	private class StarView extends View {

		private Bitmap mBitmap;
		private Paint mPaint = new Paint();
		private Canvas mCanvas = new Canvas();
		private Path mPath = new Path();
		private RectF mRect = new RectF();
		int width = 200;
		int height = 40;
		private int mLevel = 0;
		float[] points = new float[10];
		float dy;

		public StarView(Context context) {
			super(context);

			mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
			mRect.set(-6f, -6f, 6f, 6f);

			double cDegree = 180.0;
			for (int i = 0; i < 5; i++) {
				points[2 * i] = (float) Math.cos(cDegree * 3.1415926 / 180);
				points[2 * i + 1] = (float) Math.sin(cDegree * 3.1415926 / 180);
				cDegree += 144;
			}

		}

		public void setLevel(int level) {
			mLevel = level;

		}

		@Override
		protected void onSizeChanged(int w, int h, int oldw, int oldh) {
			mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
			mCanvas.setBitmap(mBitmap);

			dy = height * 0.5f;

			mPath.moveTo(points[0], points[1]);
			for (int i = 1; i < 5; i++) {
				mPath.lineTo(points[i * 2], points[i * 2 + 1]);
			}
			mPath.close();

			super.onSizeChanged(w, h, oldw, oldh);

		}

		@Override
		protected void onDraw(Canvas canvas) {

			mCanvas.drawColor(0xbf1e1d1d);
			float sx = 15;
			for (int i = 0; i < 5; i++) {
				mCanvas.save(Canvas.MATRIX_SAVE_FLAG);

				mCanvas.translate(sx, dy);

				mCanvas.scale(15, 15);
				mCanvas.rotate(18.0f);
				mPaint.setStyle(Paint.Style.FILL);
				if (i < mLevel) {
					mPaint.setColor(0xffffff00);
				} else {
					mPaint.setColor(Color.WHITE);
				}
				mCanvas.drawPath(mPath, mPaint);

				mCanvas.restore();
				sx += 30;
			}
			canvas.drawBitmap(mBitmap, 0, 0, null);

		}

		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

			setMeasuredDimension(width, height);
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		if (lDisplayManager != null)
			lDisplayManager.start();
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (lDisplayManager != null)
			lDisplayManager.stop();

	}

}