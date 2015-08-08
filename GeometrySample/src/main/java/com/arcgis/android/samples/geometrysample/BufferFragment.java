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

package com.arcgis.android.samples.geometrysample;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.LinearUnit;
import com.esri.core.geometry.MultiPath;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.geometry.Unit;
import com.esri.core.geometry.Unit.UnitType;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;

public class BufferFragment extends Fragment {
	final static String ARG_POSITION = "position";

	int mCurrentPosition = -1;

	MapView mapView;

	GraphicsLayer firstGeomLayer = null;

	GraphicsLayer resultGeomLayer = null;

	Button resetButton = null;

	Button bufferExecute = null;

	RadioGroup geomType = null;

	SeekBar bufferDistance = null;

	Button btn2ndGeometry = null;

	Button operatorButton = null;

	Geometry firstGeometry = null;

	volatile int countTap = 0;

	int geomNumWorkon = -1;

	GEOMETRY_TYPE firstGeoType = GEOMETRY_TYPE.point;

	boolean isStartPointSet1 = false;

	int selectedGeomID = 0;

	double bufferDist = 3000;

	SpatialReference spatialRef = SpatialReference.create(102100);

	TextView bufferDistTextValue = null;

	enum GEOMETRY_TYPE {
		point, polyline, polygon
	}

	boolean enableSketching = true;

	/**
	 * Create a new instance of BufferFragment, initialized to show the text at
	 * 'index'.
	 */
	public static BufferFragment newInstance(int index) {
		BufferFragment f = new BufferFragment();

		// Supply index input as an argument.
		Bundle args = new Bundle();
		args.putInt("index", index);
		f.setArguments(args);

		return f;
	}

	public int getShownIndex() {
		return getArguments().getInt("index", 0);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		// If activity recreated (such as from screen rotate), restore
		// the previous article selection set by onSaveInstanceState().
		// This is primarily necessary when in the two-pane layout.
		if (savedInstanceState != null) {
			mCurrentPosition = savedInstanceState.getInt(ARG_POSITION);
		}

		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.buffer, container, false);

		// return inflater.inflate(R.layout.article_view, container, false);
	}

	@Override
	public void onStart() {
		super.onStart();

		// During startup, check if there are arguments passed to the fragment.
		// onStart is a good place to do this because the layout has already
		// been
		// applied to the fragment at this point so we can safely call the
		// method
		// below that sets the article text.
		Bundle args = getArguments();
		if (args != null) {
			// Set article based on argument passed in
			updateArticleView(args.getInt(ARG_POSITION));
		} else if (mCurrentPosition != -1) {
			// Set article based on saved instance state defined during
			// onCreateView
			updateArticleView(mCurrentPosition);
		}
	}

	public void updateArticleView(int position) {

		mapView = (MapView) getActivity().findViewById(R.id.map);

		// Add Tile layer to the MapView
		String tileURL = getActivity().getResources().getString(
				R.string.tileServiceURL);
		ArcGISTiledMapServiceLayer tilelayer = new ArcGISTiledMapServiceLayer(
				tileURL);
		mapView.addLayer(tilelayer);

		// Set the envelope for the map
		Envelope env = new Envelope(-8139237.214629, 5016257.541842,
				-8090341.387563, 5077377.325675);
		mapView.setExtent(env);

		// Add the graphics layer for user to draw on the map
		firstGeomLayer = new GraphicsLayer();
		mapView.addLayer(firstGeomLayer);

		// Add the graphics layer to display results on the map
		resultGeomLayer = new GraphicsLayer();
		mapView.addLayer(resultGeomLayer);

		/**
		 * Single tap listener for MapView ***************
		 */
		mapView.setOnSingleTapListener(new OnSingleTapListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSingleTap(float x, float y) {
				// Check if sketching is enabled
				if (enableSketching) {
					try {
						singleTapAct(x, y);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		});

		// Display instructions to the user
		Toast.makeText(
				getActivity(),
				"Sketch a geometry and tap the buffer button to see the result",
				Toast.LENGTH_LONG).show();

		// set the behavior of all buttons
		setButtons();

		mCurrentPosition = position;
	}

	/**
	 * SET BUTTONS **************************
	 */
	void setButtons() {

		geomType = (RadioGroup) getActivity().findViewById(R.id.geometrytype);

		geomType.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// Set the geometry type to draw on the map

				switch (checkedId) {
				case R.id.point:
					firstGeoType = GEOMETRY_TYPE.point;
					break;
				case R.id.line:
					firstGeoType = GEOMETRY_TYPE.polyline;
					break;
				case R.id.polygon:
					firstGeoType = GEOMETRY_TYPE.polygon;
					break;
				}

			}
		});

		resetButton = (Button) getActivity().findViewById(R.id.reset);
		resetButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Set all the default values

				firstGeometry = null;

				firstGeomLayer.removeAll();

				resultGeomLayer.removeAll();

				enableSketching = true;
			}
		});

		bufferExecute = (Button) getActivity().findViewById(R.id.buffer);
		bufferExecute.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// execute buffer
				doBuffer();
			}
		});

		bufferDistance = (SeekBar) getActivity().findViewById(R.id.distance);
		// set default progress
		bufferDistance.setMax(5000);
		bufferDistance.setProgress(3000);

		bufferDistTextValue = (TextView) getActivity().findViewById(
				R.id.bufferdistance);

		bufferDistance
				.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {

					}

					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {

					}

					@Override
					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						// Use progress as the distance and calculate the buffer
						// geometry again and plot it on the map
						bufferDist = progress;
						bufferDistTextValue.setText((Integer.toString(progress))
								+ "m");
						doBuffer();

					}
				});

	}

	public void doBuffer() {

		Geometry geom[] = { firstGeometry };

		resultGeomLayer.removeAll();

		if (firstGeometry != null) {
			try {

				Unit unit = spatialRef.getUnit();

				double adjustedAccuracy = bufferDist;

				if (unit.getUnitType() == UnitType.ANGULAR) {
					adjustedAccuracy = metersToDegrees(bufferDist);
				} else {
					unit = Unit.create(LinearUnit.Code.METER);
				}
				// get the result polygon from the buffer operation
				Polygon p = GeometryEngine.buffer(geom[0], spatialRef,
						adjustedAccuracy, unit);

				// Render the polygon on the result graphic layer
				SimpleFillSymbol sfs = new SimpleFillSymbol(Color.GREEN);
				sfs.setOutline(new SimpleLineSymbol(Color.RED, 4,
						com.esri.core.symbol.SimpleLineSymbol.STYLE.SOLID));
				sfs.setAlpha(25);
				Graphic g = new Graphic(p, sfs);
				resultGeomLayer.addGraphic(g);

			} catch (Exception ex) {
				Log.d("Test buffer", ex.getMessage());

			}
			enableSketching = false;
		}

	}

	private final double metersToDegrees(double distanceInMeters) {
		return distanceInMeters / 111319.5;
	}

	void displayResultGeometry(Geometry[] geometry) throws Exception {
		if (geometry == null)
			return;
		Geometry[] geometries = new Geometry[1];
		geometries[0] = geometry[0];
		resultGeomLayer.removeAll();

	}

	/**
	 * action to take on single taping.
	 * 
	 * @param x
	 * @param y
	 * @throws Exception
	 */

	void singleTapAct(float x, float y) throws Exception {
		countTap++;
		Point point = mapView.toMapPoint(x, y);
		Log.d("sigle tap on screen:", "[" + x + "," + y + "]");
		Log.d("sigle tap on map:", "[" + point.getX() + "," + point.getY()
				+ "]");

		if (firstGeometry == null) {
			if (firstGeoType == GEOMETRY_TYPE.point) {
				firstGeometry = point;

			} else if (firstGeoType == GEOMETRY_TYPE.polygon) {
				firstGeometry = new Polygon();
				((MultiPath) firstGeometry).startPath(point);
				isStartPointSet1 = true;
				Log.d("geometry step " + countTap,
						GeometryEngine.geometryToJson(
								mapView.getSpatialReference(), firstGeometry));

			} else if (firstGeoType == GEOMETRY_TYPE.polyline) {
				isStartPointSet1 = true;
				firstGeometry = new Polyline();
				((MultiPath) firstGeometry).startPath(point);
			}

		}

		if (firstGeoType == null)
			return;
		int color1 = Color.BLUE;
		drawGeomOnGraphicLyr(firstGeometry, firstGeomLayer, point,
				firstGeoType, color1, isStartPointSet1);
		Log.d("geometry step " + countTap, GeometryEngine.geometryToJson(
				mapView.getSpatialReference(), firstGeometry));

	}

	void drawGeomOnGraphicLyr(Geometry geometryToDraw, GraphicsLayer glayer,
			Point point, GEOMETRY_TYPE geoTypeToDraw, int color,
			boolean startPointSet) {

		if (geoTypeToDraw == GEOMETRY_TYPE.point) {
			geometryToDraw = point;

		} else {

			if (startPointSet) {

				if (geoTypeToDraw == GEOMETRY_TYPE.polygon) {
					((Polygon) geometryToDraw).lineTo(point);
				} else if (geoTypeToDraw == GEOMETRY_TYPE.polyline) {
					((Polyline) geometryToDraw).lineTo(point);
				}

			}
		}

		Geometry[] geoms = new Geometry[1];
		geoms[0] = geometryToDraw;

		try {
			glayer.removeAll();
			GeometryUtil.highlightGeometriesWithColor(geoms, glayer, color);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		// Save the current article selection in case we need to recreate the
		// fragment
		outState.putInt(ARG_POSITION, mCurrentPosition);
	}
}