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

package com.esri.arcgis.android.samples.offlineeditor;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;

import com.esri.android.map.FeatureLayer;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.Layer;
import com.esri.android.map.MapOnTouchListener;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISFeatureLayer;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.core.geodatabase.GeodatabaseFeature;
import com.esri.core.geodatabase.GeodatabaseFeatureTable;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.MultiPath;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;
import com.esri.core.map.FeatureTemplate;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.symbol.Symbol;
import com.esri.core.table.TableException;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Allows you to make edits on the map being offline.
 */
public class OfflineEditorActivity extends Activity {

	protected static final String TAG = "OfflineEditorActivity";

	private static final int POINT = 0;

	private static final int POLYLINE = 1;

	private static final int POLYGON = 2;

	private static MapView mapView;

	GraphicsLayer graphicsLayer;

	GraphicsLayer graphicsLayerEditing;

	GraphicsLayer highlightGraphics;

	boolean featureUpdate = false;

	boolean mDatabaseInitialized = false;

	boolean onlineData = true;

	long featureUpdateId;

	int addedGraphicId;

	MyTouchListener myListener;

	private TemplatePicker tp;

	ArrayList<Point> points = new ArrayList<Point>();

	ArrayList<Point> mpoints = new ArrayList<Point>();

	boolean midpointselected = false;

	boolean vertexselected = false;

	int insertingindex;

	int editingmode;

	static ProgressDialog progress;

	MenuItem editMenuItem;

	MenuItem offlineMenuItem;

	MenuItem onlineMenuItem;

	ArrayList<EditingStates> editingstates = new ArrayList<EditingStates>();

	FeatureTemplate template;

	@SuppressWarnings("unused")
	private Menu menu;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.offlineeditor);

		/*
		 * Initialize ArcGIS Android MapView, tiledMapServiceLayer, and Graphics
		 * Layer
		 */
		mapView = ((MapView) findViewById(R.id.map));
		mapView.addLayer(new ArcGISTiledMapServiceLayer(
				GDBUtil.DEFAULT_BASEMAP_SERVICE_URL));

		for (int i : GDBUtil.FEATURE_SERVICE_LAYER_IDS) {

			mapView.addLayer(new ArcGISFeatureLayer(
					GDBUtil.DEFAULT_FEATURE_SERVICE_URL + "/" + i,
					ArcGISFeatureLayer.MODE.ONDEMAND));
		}

		Envelope env = new Envelope(-122.514731, 37.762135, -122.433192,
				37.787237);
		mapView.setExtent(env);

		/**
		 * When the basemap is initialized the status will be true.
		 */
		mapView.setOnStatusChangedListener(new OnStatusChangedListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void onStatusChanged(final Object source, final STATUS status) {

				if (STATUS.INITIALIZED == status) {

					if (source instanceof MapView) {
						graphicsLayer = new GraphicsLayer();
						highlightGraphics = new GraphicsLayer();
						mapView.addLayer(graphicsLayer);
						mapView.addLayer(highlightGraphics);
					}
				}
				if (STATUS.LAYER_LOADED == status) {
					if (source instanceof ArcGISFeatureLayer) {
						GDBUtil.showProgress(OfflineEditorActivity.this, false);
					}
				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		this.menu = menu;
		getMenuInflater().inflate(R.menu.action, menu);
		editMenuItem = menu.findItem(R.id.edit);
		offlineMenuItem = menu.findItem(R.id.go_offline);
		onlineMenuItem = menu.findItem(R.id.go_online);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.go_offline:
			item.setVisible(false);
			try {
				new ConnectToServer().execute("downloadGdb").get(5,
						TimeUnit.SECONDS);
			} catch (Exception e) {
				System.out.println("you are out");
				e.printStackTrace();
			}
			editMenuItem.setVisible(true);
			onlineMenuItem.setVisible(true);
			return true;

		case R.id.go_online:
			item.setVisible(false);
			GDBUtil.goOnline(OfflineEditorActivity.this, mapView);
			offlineMenuItem.setVisible(true);
			editMenuItem.setVisible(false);
			return true;

		case R.id.edit:
			OfflineActions offlineActions = new OfflineActions(
					OfflineEditorActivity.this);
			startActionMode(offlineActions);
			showEditTemplatePicker();
			return true;

		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	/**
	 * Synchronizes the edits
	 */
	public void syncGdb() {
		new ConnectToServer().execute("syncGdb");
	}

	/**
	 * Removes the edits
	 */
	public void remove() {
		if (!vertexselected)
			points.remove(points.size() - 1); // remove last vertex
		else
			points.remove(insertingindex);
		midpointselected = false;
		vertexselected = false;
		editingstates.add(new EditingStates(points, midpointselected,
				vertexselected, insertingindex));
		refresh();

	}

	/**
	 * Shows the edit templates for all the feature layers in the map
	 */
	public void showEditTemplatePicker() {

		GDBUtil.showProgress(OfflineEditorActivity.this, true);
		clear();
		int layerCount = 0;
		for (Layer layer : mapView.getLayers()) {
			if (layer instanceof FeatureLayer) {
				layerCount++;
			}

		}
		if (layerCount > 0) {
			if (myListener == null) {
				myListener = new MyTouchListener(OfflineEditorActivity.this,
						mapView);
				mapView.setOnTouchListener(myListener);
			}
			if (getTemplatePicker() != null) {
				getTemplatePicker().showAtLocation(mapView, Gravity.BOTTOM, 0,
						0);
			} else {
				new TemplatePickerTask().execute();
			}
		} else {
			GDBUtil.showMessage(OfflineEditorActivity.this,
					"No Editable Local Feature Layers.");

		}
		GDBUtil.showProgress(OfflineEditorActivity.this, false);

	}

	/**
	 * Cancel the last change
	 */
	public void cancel() {
		midpointselected = false;
		vertexselected = false;
		refresh();

	}

	/**
	 * Revert back to the last state of the edit
	 */
	public void undo() {
		// only undo when more than one edit has been made
		if (editingstates.size() > 1) {
			editingstates.remove(editingstates.size() - 1);
			EditingStates state = editingstates.get(editingstates.size() - 1);
			points.clear();
			points.addAll(state.points1);
			Log.d(TAG, "# of points = " + points.size());
			midpointselected = state.midpointselected1;
			vertexselected = state.vertexselected1;
			insertingindex = state.insertingindex1;
			refresh();
		}
	}

	/**
	 * An instance of this class is created when a new point is to be
	 * added/moved/deleted. Hence we can describe this class as a container of
	 * points selected. Points, vertexes, or mid points.
	 */
	public class EditingStates {
		ArrayList<Point> points1 = new ArrayList<Point>();

		boolean midpointselected1 = false;

		boolean vertexselected1 = false;

		int insertingindex1;

		public EditingStates(ArrayList<Point> points, boolean midpointselected,
				boolean vertexselected, int insertingindex) {
			this.points1.addAll(points);
			this.midpointselected1 = midpointselected;
			this.vertexselected1 = vertexselected;
			this.insertingindex1 = insertingindex;
		}
	}

	/*
	 * MapView's touch listener
	 */
	public class MyTouchListener extends MapOnTouchListener {
		MapView map;

		Context context;

		boolean redrawCache = true;

		public MyTouchListener(Context context, MapView view) {
			super(context, view);
			this.context = context;
			map = view;
		}

		@Override
		public boolean onDragPointerMove(MotionEvent from, final MotionEvent to) {
			if (tp != null && !onlineData) {
				if (getTemplatePicker().getselectedTemplate() != null) {
					setEditingMode();
				}
			}
			return super.onDragPointerMove(from, to);
		}

		@Override
		public boolean onDragPointerUp(MotionEvent from, final MotionEvent to) {
			if (tp != null && !onlineData) {
				if (getTemplatePicker().getselectedTemplate() != null) {
					setEditingMode();
				}
			}
			return super.onDragPointerUp(from, to);
		}

		/**
		 * In this method we check if the point clicked on the map denotes a new
		 * point or means an existing vertex must be moved.
		 */
		@Override
		public boolean onSingleTap(final MotionEvent e) {
			if (tp != null && !onlineData) {

				Point point = map.toMapPoint(new Point(e.getX(), e.getY()));
				if (getTemplatePicker().getselectedTemplate() != null) {
					setEditingMode();

				}
				if (getTemplatePicker().getSelectedLayer() != null) {
					long[] featureIds = ((FeatureLayer) mapView
							.getLayerByID(getTemplatePicker()
									.getSelectedLayer().getID()))
							.getFeatureIDs(e.getX(), e.getY(), 25);
					if (featureIds.length > 0 && (!featureUpdate)) {
						featureUpdateId = featureIds[0];
						GeodatabaseFeature gdbFeatureSelected = (GeodatabaseFeature) ((FeatureLayer) mapView
								.getLayerByID(getTemplatePicker()
										.getSelectedLayer().getID()))
								.getFeature(featureIds[0]);
						if (editingmode == POLYLINE || editingmode == POLYGON) {
							if (gdbFeatureSelected.getGeometry().getType()
									.equals(Geometry.Type.POLYLINE)) {
								Polyline polyline = (Polyline) gdbFeatureSelected
										.getGeometry();
								for (int i = 0; i < polyline.getPointCount(); i++) {
									points.add(polyline.getPoint(i));
								}

								refresh();

								editingstates.add(new EditingStates(points,
										midpointselected, vertexselected,
										insertingindex));

							} else if (gdbFeatureSelected.getGeometry()
									.getType().equals(Geometry.Type.POLYGON)) {
								Polygon polygon = (Polygon) gdbFeatureSelected
										.getGeometry();
								for (int i = 0; i < polygon.getPointCount(); i++) {
									points.add(polygon.getPoint(i));
								}

								refresh();
								editingstates.add(new EditingStates(points,
										midpointselected, vertexselected,
										insertingindex));

							}
							featureUpdate = true;
						}
					} else {
						if (editingmode == POINT) {

							GeodatabaseFeature g;
							try {
								graphicsLayer.removeAll();
								// this needs to to be created from FeatureLayer
								// by
								// passing template
								g = ((GeodatabaseFeatureTable) ((FeatureLayer) mapView
										.getLayerByID(getTemplatePicker()
												.getSelectedLayer().getID()))
										.getFeatureTable())
										.createFeatureWithTemplate(
												getTemplatePicker()
														.getselectedTemplate(),
												point);
								Symbol symbol = ((FeatureLayer) mapView
										.getLayerByID(getTemplatePicker()
												.getSelectedLayer().getID()))
										.getRenderer().getSymbol(g);

								Graphic gr = new Graphic(g.getGeometry(),
										symbol, g.getAttributes());

								addedGraphicId = graphicsLayer.addGraphic(gr);
							} catch (TableException e1) {
								e1.printStackTrace();
							}

							points.clear();
						}
						if (!midpointselected && !vertexselected) {
							// check if user tries to select an existing point.
							int idx1 = getSelectedIndex(e.getX(), e.getY(),
									mpoints, map);
							if (idx1 != -1) {
								midpointselected = true;
								insertingindex = idx1;
							}

							if (!midpointselected) { // check vertices
								int idx2 = getSelectedIndex(e.getX(), e.getY(),
										points, map);
								if (idx2 != -1) {
									vertexselected = true;
									insertingindex = idx2;
								}

							}
							if (!midpointselected && !vertexselected) {
								// no match, add new vertex at the location
								points.add(point);
								editingstates.add(new EditingStates(points,
										midpointselected, vertexselected,
										insertingindex));
							}

						} else if (midpointselected || vertexselected) {
							int idx1 = getSelectedIndex(e.getX(), e.getY(),
									mpoints, map);
							int idx2 = getSelectedIndex(e.getX(), e.getY(),
									points, map);
							if (idx1 == -1 && idx2 == -1) {
								movePoint(point);
								editingstates.add(new EditingStates(points,
										midpointselected, vertexselected,
										insertingindex));
							} else {

								if (idx1 != -1) {
									insertingindex = idx1;
								}
								if (idx2 != -1) {
									insertingindex = idx2;
								}

								editingstates.add(new EditingStates(points,
										midpointselected, vertexselected,
										insertingindex));

							}
						} else {
							// an existing point has been selected previously.
							movePoint(point);
						}
						refresh();
						redrawCache = true;
						return true;
					}
				}
			}
			return true;
		}
	}

	/**
	 * The edits made are applied and hence saved on the server.
	 */
	public void save() {
		Graphic addedGraphic;
		MultiPath multipath;

		if (editingmode == POINT)
			try {
				addedGraphic = graphicsLayer.getGraphic(addedGraphicId);
				((FeatureLayer) mapView.getLayerByID(getTemplatePicker()
						.getSelectedLayer().getID())).getFeatureTable()
						.addFeature(addedGraphic);
				graphicsLayer.removeAll();
			} catch (TableException e1) {
				e1.printStackTrace();
			}
		else {
			if (editingmode == POLYLINE)
				multipath = new Polyline();
			else if (editingmode == POLYGON)
				multipath = new Polygon();
			else
				return;
			multipath.startPath(points.get(0));
			for (int i = 1; i < points.size(); i++) {
				multipath.lineTo(points.get(i));
			}

			// Simplify the geometry that is to be set on the graphics.
			// Note this call is local not made to the server.
			Geometry geom = GeometryEngine.simplify(multipath,
					mapView.getSpatialReference());
			if (featureUpdate) {
				try {
					GeodatabaseFeature g = ((GeodatabaseFeatureTable) ((FeatureLayer) mapView
							.getLayerByID(getTemplatePicker()
									.getSelectedLayer().getID()))
							.getFeatureTable()).createFeatureWithTemplate(
							getTemplatePicker().getselectedTemplate(), geom);
					Symbol symbol = ((FeatureLayer) mapView
							.getLayerByID(getTemplatePicker()
									.getSelectedLayer().getID())).getRenderer()
							.getSymbol(g);
					addedGraphic = new Graphic(geom, symbol, g.getAttributes());
					((FeatureLayer) mapView.getLayerByID(getTemplatePicker()
							.getSelectedLayer().getID())).getFeatureTable()
							.updateFeature(featureUpdateId, addedGraphic);
				} catch (TableException e) {
					e.printStackTrace();
				}
			} else {
				try {
					GeodatabaseFeature g = ((GeodatabaseFeatureTable) ((FeatureLayer) mapView
							.getLayerByID(getTemplatePicker()
									.getSelectedLayer().getID()))
							.getFeatureTable()).createFeatureWithTemplate(
							getTemplatePicker().getselectedTemplate(), geom);
					Symbol symbol = ((FeatureLayer) mapView
							.getLayerByID(getTemplatePicker()
									.getSelectedLayer().getID())).getRenderer()
							.getSymbol(g);
					addedGraphic = new Graphic(geom, symbol, g.getAttributes());
					((FeatureLayer) mapView.getLayerByID(getTemplatePicker()
							.getSelectedLayer().getID())).getFeatureTable()
							.addFeature(addedGraphic);
				} catch (TableException e) {
					e.printStackTrace();
				}
			}
		}
	}

	void movePoint(Point point) {

		if (midpointselected) {
			// Move mid-point to the new location and make it a vertex.
			points.add(insertingindex + 1, point);
			editingstates.add(new EditingStates(points, midpointselected,
					vertexselected, insertingindex));
		} else if (vertexselected) {
			ArrayList<Point> temp = new ArrayList<Point>();
			for (int i = 0; i < points.size(); i++) {
				if (i == insertingindex)
					temp.add(point);
				else
					temp.add(points.get(i));
			}
			points.clear();
			points.addAll(temp);
			editingstates.add(new EditingStates(points, midpointselected,
					vertexselected, insertingindex));
		}
		midpointselected = false; // back to the normal drawing mode.
		vertexselected = false;

	}

	void refresh() {

		if (editingmode != POINT) {
			if (graphicsLayerEditing != null && graphicsLayer != null) {
				graphicsLayerEditing.removeAll();
				graphicsLayer.removeAll();
			}

			drawPolyline();
			drawMidPoints();
			drawVertices();
		}
	}

	private void drawMidPoints() {
		int index;
		Graphic graphic;
		if (graphicsLayerEditing == null) {
			graphicsLayerEditing = new GraphicsLayer();
			mapView.addLayer(graphicsLayerEditing);
		}
		// draw mid-point
		if (points.size() > 1) {
			mpoints.clear();
			for (int i = 1; i < points.size(); i++) {
				Point p1 = points.get(i - 1);
				Point p2 = points.get(i);
				mpoints.add(new Point((p1.getX() + p2.getX()) / 2,
						(p1.getY() + p2.getY()) / 2));
			}
			if (editingmode == POLYGON) { // complete the circle
				Point p1 = points.get(0);
				Point p2 = points.get(points.size() - 1);
				mpoints.add(new Point((p1.getX() + p2.getX()) / 2,
						(p1.getY() + p2.getY()) / 2));
			}
			index = 0;
			for (Point pt : mpoints) {

				if (midpointselected && insertingindex == index)
					graphic = new Graphic(pt, new SimpleMarkerSymbol(Color.RED,
							20, SimpleMarkerSymbol.STYLE.CIRCLE));
				else
					graphic = new Graphic(pt, new SimpleMarkerSymbol(
							Color.GREEN, 15, SimpleMarkerSymbol.STYLE.CIRCLE));
				graphicsLayerEditing.addGraphic(graphic);
				index++;
			}
		}
	}

	private void drawVertices() {
		int index;
		// draw vertices
		index = 0;

		if (graphicsLayerEditing == null) {
			graphicsLayerEditing = new GraphicsLayer();
			mapView.addLayer(graphicsLayerEditing);
		}

		for (Point pt : points) {
			if (vertexselected && index == insertingindex) {
				Graphic graphic = new Graphic(pt, new SimpleMarkerSymbol(
						Color.RED, 20, SimpleMarkerSymbol.STYLE.CIRCLE));
				Log.d(TAG, "Add Graphic vertex");
				graphicsLayerEditing.addGraphic(graphic);
			} else if (index == points.size() - 1 && !midpointselected
					&& !vertexselected) {
				Graphic graphic = new Graphic(pt, new SimpleMarkerSymbol(
						Color.RED, 20, SimpleMarkerSymbol.STYLE.CIRCLE));

				int id = graphicsLayer.addGraphic(graphic);

				Log.d(TAG,
						"Add Graphic mid point" + pt.getX() + " " + pt.getY()
								+ " id = " + id);

			} else {
				Graphic graphic = new Graphic(pt, new SimpleMarkerSymbol(
						Color.BLACK, 20, SimpleMarkerSymbol.STYLE.CIRCLE));
				Log.d(TAG, "Add Graphic point");
				graphicsLayerEditing.addGraphic(graphic);
			}

			index++;
		}
	}

	private void drawPolyline() {

		if (graphicsLayerEditing == null) {
			graphicsLayerEditing = new GraphicsLayer();
			mapView.addLayer(graphicsLayerEditing);
		}
		if (points.size() <= 1)
			return;
		Graphic graphic;
		MultiPath multipath;
		if (editingmode == POLYLINE)
			multipath = new Polyline();
		else
			multipath = new Polygon();
		multipath.startPath(points.get(0));
		for (int i = 1; i < points.size(); i++) {
			multipath.lineTo(points.get(i));
		}
		Log.d(TAG, "DrawPolyline: Array coutn = " + points.size());
		if (editingmode == POLYLINE)
			graphic = new Graphic(multipath, new SimpleLineSymbol(Color.BLACK,
					4));
		else {
			SimpleFillSymbol simpleFillSymbol = new SimpleFillSymbol(
					Color.YELLOW);
			simpleFillSymbol.setAlpha(100);
			simpleFillSymbol.setOutline(new SimpleLineSymbol(Color.BLACK, 4));
			graphic = new Graphic(multipath, (simpleFillSymbol));
		}
		Log.d(TAG, "Add Graphic Line in DrawPolyline");
		graphicsLayerEditing.addGraphic(graphic);
	}

	public void clear() {
		if (graphicsLayer != null) {
			graphicsLayer.removeAll();
		}

		if (graphicsLayerEditing != null) {
			graphicsLayerEditing.removeAll();
		}
		if (highlightGraphics != null) {
			highlightGraphics.removeAll();
			mapView.getCallout().hide();

		}

		featureUpdate = false;
		points.clear();
		mpoints.clear();
		midpointselected = false;
		vertexselected = false;
		insertingindex = 0;
		editingstates.clear();

	}

	/**
	 * return index of point in array whose distance to touch point is minimum
	 * and less than 40.
	 */
	int getSelectedIndex(double x, double y, ArrayList<Point> points1,
			MapView map) {

		if (points1 == null || points1.size() == 0)
			return -1;

		int index = -1;
		double distSQ_Small = Double.MAX_VALUE;
		for (int i = 0; i < points1.size(); i++) {
			Point p = map.toScreenPoint(points1.get(i));
			double diffx = p.getX() - x;
			double diffy = p.getY() - y;
			double distSQ = diffx * diffx + diffy * diffy;
			if (distSQ < distSQ_Small) {
				index = i;
				distSQ_Small = distSQ;
			}
		}

		if (distSQ_Small < (40 * 40)) {
			return index;
		}
		return -1;

	}

	private void setEditingMode() {
		if (getTemplatePicker() != null) {
			if (getTemplatePicker().getSelectedLayer().getGeometryType()
					.equals(Geometry.Type.POINT)
					|| getTemplatePicker().getSelectedLayer().getGeometryType()
							.equals(Geometry.Type.MULTIPOINT)) {
				editingmode = POINT;
				template = getTemplatePicker().getselectedTemplate();
			} else if (getTemplatePicker().getSelectedLayer().getGeometryType()
					.equals(Geometry.Type.POLYLINE)) {
				editingmode = POLYLINE;
				template = getTemplatePicker().getselectedTemplate();
			} else if (getTemplatePicker().getSelectedLayer().getGeometryType()
					.equals(Geometry.Type.POLYGON)) {
				editingmode = POLYGON;
				template = getTemplatePicker().getselectedTemplate();
			}
		}
	}

	public MapView getMapView() {
		return mapView;
	}

	/**
	 * Connect to server to synchronize edits back or download features locally
	 */
	public class ConnectToServer extends AsyncTask<String, Void, Void> {

		@Override
		protected void onPreExecute() {
			progress = new ProgressDialog(OfflineEditorActivity.this);
			progress = ProgressDialog.show(OfflineEditorActivity.this, "",
					"Processing... Please wait...");
		}

		@Override
		protected Void doInBackground(String... params) {
			if (params[0].equals("syncGdb")) {
				GDBUtil.synchronize(OfflineEditorActivity.this);
			} else if (params[0].equals("downloadGdb")) {
				GDBUtil.downloadData(OfflineEditorActivity.this);
			}
			return null;
		}

	}

	/**
	 * This is responsible for retrieving the template types for the edits.
	 */
	public class TemplatePickerTask extends AsyncTask<Void, Void, Void> {

		ProgressDialog progressDialog;

		@Override
		protected Void doInBackground(Void... params) {

			setTemplatePicker(new TemplatePicker(OfflineEditorActivity.this,
					mapView));
			return null;
		}

		@Override
		protected void onPreExecute() {
			progressDialog = ProgressDialog
					.show(OfflineEditorActivity.this,
							"Loading Edit Templates",
							"Might take more time for layers with many templates",
							true);
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(Void result) {
			progressDialog.dismiss();
			getTemplatePicker().showAtLocation(mapView, Gravity.BOTTOM, 0, 0);

			super.onPostExecute(result);
		}

	}

	public TemplatePicker getTemplatePicker() {
		return tp;
	}

	public void setTemplatePicker(TemplatePicker tp) {
		this.tp = tp;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mapView.pause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mapView.unpause();

	}
}
